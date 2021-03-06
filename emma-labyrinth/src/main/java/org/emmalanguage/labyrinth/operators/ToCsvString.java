/*
 * Copyright © 2014 TU Berlin (emma@dima.tu-berlin.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.emmalanguage.labyrinth.operators;

import org.emmalanguage.io.csv.CSV;
import org.emmalanguage.io.csv.CSVConverter;
import scala.util.Either;
import org.emmalanguage.labyrinth.util.SerializedBuffer;

abstract public class ToCsvString<T> extends BagOperator<Either<T, CSV>, String> {

	protected SerializedBuffer<Either<T, CSV>> buff;
	protected com.univocity.parsers.csv.CsvWriter csvWriter;
	protected CSVConverter<T> csvConv;
	protected CSV csvInfo;
	protected boolean csvWriterInitiated = false;
	protected boolean csvInfoInitiated = false;

	protected boolean[] closed = new boolean[2];

}
