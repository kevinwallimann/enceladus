/*
 * Copyright 2018-2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.enceladus.testutils.dataGeneration

import org.apache.spark.SparkContext
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SparkSession}
import za.co.absa.enceladus.testutils.dataGeneration.columnTypes._

import scala.collection.mutable

object RandomDataGenerator {
  final private val randomGenerator = scala.util.Random

  def main(args: Array[String]): Unit = {
    val enableWholeStage = false //disable whole stage code gen - the plan is too long

    implicit val sparkSession: SparkSession = SparkSession.builder()
      .appName(s"Dataset generation")
      .config("spark.sql.codegen.wholeStage", enableWholeStage)
      .getOrCreate()

    implicit val sc: SparkContext = sparkSession.sparkContext

    val numOfRows = 10
    val numOfColumns = 4
    val columns: List[ColumnType[_]] = generateColumns(numOfColumns - 1)
    val sequenceOfRows: Seq[List[Any]] = Seq.fill(numOfRows) { generateRow(columns) }
    val rows = sequenceOfRows.map(Row.fromSeq(_))
    val rdd = sc.makeRDD(rows)
    val schema: mutable.MutableList[StructField] = mutable.MutableList()

    for ((column, i) <- (HashCodeColumn() :: columns).view.zipWithIndex) {
      schema += StructField(s"${i}_${column.name}", column.dataType)
    }

    val dataFrame = sparkSession.createDataFrame(rdd, StructType(schema))
    dataFrame.write.parquet("/tmp/generatedData")
  }

  def generateRow(columns: List[ColumnType[_]]): List[Any] = {
    val listOfValues: mutable.MutableList[Any] = mutable.MutableList()
    var hashCode = 0

    for (columnName <- columns) {
      val newValue = columnName.generateRandomValue
      listOfValues += newValue
      hashCode += newValue.hashCode()
    }
    hashCode :: listOfValues.toList
  }

  def generateColumns(numberOfColumns: Int): List[ColumnType[_]] = {
    val listOfCoulmns: mutable.MutableList[ColumnType[_]] = mutable.MutableList()

    for (_ <- 0 until numberOfColumns) {
      listOfCoulmns += (randomGenerator.nextLong match {
        case x if x % 13 == 0 => BooleanColumn()
        case x if x % 11 == 0 => StringColumn()
        case x if x % 7  == 0 => DateColumn()
        case x if x % 5  == 0 => FloatColumn()
        case x if x % 3  == 0 => LongColumn()
        case x if x % 2  == 0 => IntegerColumn()
        case _                => DoubleColumn()
      })
    }

    listOfCoulmns.toList
  }
}


