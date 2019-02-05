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

package za.co.absa.enceladus.testutils.dataGeneration.columnTypes

import org.apache.spark.sql.types.{DoubleType, FloatType, IntegerType, LongType}

case class IntegerColumn() extends ColumnType[Int]("Integer", IntegerType){
  override def generateRandomValue: Int = randomGenerator.nextInt
}

case class FloatColumn() extends ColumnType[Float]("Float", FloatType){
  override def generateRandomValue: Float = randomGenerator.nextFloat
}

case class DoubleColumn() extends ColumnType[Double]("Double", DoubleType){
  override def generateRandomValue: Double = randomGenerator.nextDouble
}

case class LongColumn() extends ColumnType[Long]("Long", LongType){
  override def generateRandomValue: Long = randomGenerator.nextLong
}
