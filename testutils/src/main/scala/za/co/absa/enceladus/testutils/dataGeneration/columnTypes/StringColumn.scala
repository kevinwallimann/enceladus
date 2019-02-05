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

import org.apache.spark.sql.types.StringType

import scala.collection.mutable

case class StringColumn(numberOfChars: Int, includeSpecialChars: Boolean)
  extends ColumnType[String]("String", StringType){
  final private val range = 110
  final private val bigCharSeq: Seq[Char] = 'A' to 'Z'
  final private val smallCharSeq: Seq[Char] = 'a' to 'z'
  final private val specialChars: Seq[Char] = (' ' to '/') ++ ('{' to '~') ++ ('[' to '`') ++ (':' to '@')
  final private val otherChars: Seq[Char] = if (includeSpecialChars) specialChars else Seq(' ')

  override def generateRandomValue: String = {
    val stringBuilder: mutable.StringBuilder = new mutable.StringBuilder()
    for (_ <- 0 until numberOfChars) {
      stringBuilder.append(randomGenerator.nextInt(range) match {
        case x if x % 11 == 0 => otherChars(randomGenerator.nextInt(otherChars.size))
        case x if x % 2  == 0 => bigCharSeq(randomGenerator.nextInt(bigCharSeq.size))
        case _                => smallCharSeq(randomGenerator.nextInt(smallCharSeq.size))
      })
    }
    stringBuilder.toString()
  }
}

object StringColumn {
  final private val defaultLength: Int = 30
  final private val defaultSpecialChars: Boolean = false

  def apply(numberOfChars: Int): StringColumn = new StringColumn(numberOfChars, defaultSpecialChars)
  def apply(specialChars: Boolean): StringColumn = new StringColumn(defaultLength, specialChars)
  def apply(numberOfChars: Int, specialChars: Boolean): StringColumn = new StringColumn(numberOfChars, specialChars)
  def apply(): StringColumn = new StringColumn(defaultLength, defaultSpecialChars)
}
