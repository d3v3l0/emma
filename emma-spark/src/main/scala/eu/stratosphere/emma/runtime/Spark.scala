package eu.stratosphere.emma.runtime

import eu.stratosphere.emma.api.DataBag
import eu.stratosphere.emma.api.model.Identity
import eu.stratosphere.emma.codegen.spark.DataflowGenerator
import eu.stratosphere.emma.codegen.utils.DataflowCompiler
import eu.stratosphere.emma.ir._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.reflect.runtime.universe._

class Spark(val sc: SparkContext) extends Engine {

  logger.info(s"Initializing Spark execution environment")

  def this() = this {
    val conf = new SparkConf()
      .setAppName("Emma Spark App")
      .setIfMissing("spark.master", s"local[*]")
      .set("spark.hadoop.validateOutputSpecs", "false") // overwrite output files
      .set("spark.driver.allowMultipleContexts", "true")

    new SparkContext(conf)
  }

  sys addShutdownHook {
    closeSession()
  }

  override lazy val defaultDOP = sc.defaultParallelism

  val dataflowCompiler = new DataflowCompiler(runtimeMirror(getClass.getClassLoader))

  val dataflowGenerator = new DataflowGenerator(dataflowCompiler, envSessionID)

  override def executeFold[A: TypeTag, B: TypeTag](root: Fold[A, B], name: String, closure: Any*): A = {
    constructExecutionPlanJson(name, root)
    val dataflowSymbol = dataflowGenerator.generateDataflowDef(root, name)
    dataflowCompiler.execute[A](dataflowSymbol, Array[Any](sc) ++ closure ++ localInputs(root))
  }

  override def executeTempSink[A: TypeTag](root: TempSink[A], name: String, closure: Any*): DataBag[A] = {
    constructExecutionPlanJson(name, root)
    val dataflowSymbol = dataflowGenerator.generateDataflowDef(root, name)
    val rdd = dataflowCompiler.execute[RDD[A]](dataflowSymbol, Array[Any](sc) ++ closure ++ localInputs(root))
    DataBag(root.name, rdd, rdd.collect())
  }

  override def executeWrite[A: TypeTag](root: Write[A], name: String, closure: Any*): Unit = {
    constructExecutionPlanJson(name, root)
    val dataflowSymbol = dataflowGenerator.generateDataflowDef(root, name)
    dataflowCompiler.execute[RDD[A]](dataflowSymbol, Array[Any](sc) ++ closure ++ localInputs(root))
  }

  override def executeStatefulCreate[A <: Identity[K]: TypeTag, K: TypeTag](root: StatefulCreate[A, K], name: String, closure: Any*): AbstractStatefulBackend[A, K] = {
    constructExecutionPlanJson(name, root)
    val dataflowSymbol = dataflowGenerator.generateDataflowDef(root, name)
    dataflowCompiler.execute[AbstractStatefulBackend[A, K]](dataflowSymbol, Array[Any](sc) ++ closure ++ localInputs(root))
  }

  override protected def doCloseSession() = {
    super.doCloseSession()
    sc.stop()
  }
}
