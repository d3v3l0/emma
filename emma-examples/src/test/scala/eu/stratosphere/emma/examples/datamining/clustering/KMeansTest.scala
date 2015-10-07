package eu.stratosphere.emma.examples.datamining.clustering

import java.io.File
import eu.stratosphere.emma.runtime
import eu.stratosphere.emma.testutil._
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import scala.io.Source

@Category(Array(classOf[ExampleTest]))
@RunWith(classOf[JUnitRunner])
class KMeansTest extends FlatSpec with Matchers with BeforeAndAfter {
  // default parameters
  val dir = "/clustering/kmeans"
  val path = tempPath(dir)
  val epsilon = 1e-3
  val iterations = 50
  // initialize resources
  val delimiter = "\t"

  before {
    new File(path).mkdirs()
    materializeResource(s"$dir/points.tsv")
    materializeResource(s"$dir/clusters.tsv")
  }

  after {
    deleteRecursive(new File(path))
  }

  "KMeans" should "cluster points around the corners of a hypercube" in withRuntime() { rt =>
    val expected = clusters(Source
      .fromFile(s"$path/clusters.tsv")
      .getLines()
      .map { line =>
        val record = line.split(delimiter).map { _.toLong }
        record.head -> record(1)
      }.toStream)

    val solution = new KMeans(expected.size, epsilon, iterations,
      s"$path/points.tsv", s"$path/solutions.tsv", rt).algorithm.run(rt).fetch()

    val actual = clusters(solution)
    actual should contain theSameElementsAs expected
  }

  private def clusters(centers: Seq[(Long, Long)]) =
    centers.toSet[(Long, Long)].groupBy { _._2 }.values.map { _.map { _._1 } }
}
