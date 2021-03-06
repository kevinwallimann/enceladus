/*
 * Copyright 2018 ABSA Group Limited
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

package za.co.absa.enceladus.standardization.interpreter

import java.sql.{Date, Timestamp}

import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.scalatest.FunSuite
import za.co.absa.enceladus.standardization.interpreter.stages.SchemaChecker
import za.co.absa.enceladus.standardization.samples.TestSamples
import za.co.absa.enceladus.utils.error.ErrorMessage
import za.co.absa.enceladus.utils.testUtils.{LoggerTestBase, SparkTestBase}
import za.co.absa.enceladus.utils.udf.UDFLibrary
import za.co.absa.enceladus.utils.validation.field.FieldValidationFailure
import za.co.absa.enceladus.utils.validation.{SchemaValidator, ValidationError, ValidationException, ValidationWarning}

import scala.io.Source

class DateTimeSuite extends FunSuite with SparkTestBase with LoggerTestBase{
  import spark.implicits._

  lazy val data: DataFrame = spark.createDataFrame(TestSamples.dateSamples)
  lazy val schemaWrong: StructType = DataType.fromJson(Source
    .fromFile("src/test/resources/data/dateTimestampSchemaWrong.json")
    .getLines().mkString("\n"))
    .asInstanceOf[StructType]
  lazy val schemaOk: StructType = DataType.fromJson(Source
    .fromFile("src/test/resources/data/dateTimestampSchemaOk.json")
    .getLines().mkString("\n"))
    .asInstanceOf[StructType]

  private implicit val udfLib: UDFLibrary = new UDFLibrary()

  test("Validation should return critical errors") {
    logger.debug(data.schema.prettyJson)
    val validationErrors = SchemaValidator.validateSchema(schemaWrong)
    val exp = List(
      FieldValidationFailure("dateSampleWrong1", "DD-MM-yyyy", List(
        ValidationWarning("No day placeholder 'dd' found."),
        ValidationWarning("Rarely used DayOfYear placeholder 'D' found. Possibly DayOfMonth 'd' intended."))),
      FieldValidationFailure("dateSampleWrong2", "Dy", List(
        ValidationWarning("No day placeholder 'dd' found."),
        ValidationWarning("Rarely used DayOfYear placeholder 'D' found. Possibly DayOfMonth 'd' intended."),
        ValidationWarning("No month placeholder 'MM' found."))),
      FieldValidationFailure("dateSampleWrong3", "rrr", List(
        ValidationError("Illegal pattern character 'r'"))),
      FieldValidationFailure("timestampSampleWrong1", "yyyyMMddTHHmmss", List(
        ValidationError("Illegal pattern character 'T'"))),
      FieldValidationFailure("timestampSampleWrong3", "yyyy-MM-dd", List(
        ValidationWarning("No hour placeholder 'HH' found."),
        ValidationWarning("No minute placeholder 'mm' found."),
        ValidationWarning("No second placeholder 'ss' found."))),
      FieldValidationFailure("timestampNullDefaultWrong", "", List(
        ValidationError("null is not a valid value for field 'timestampNullDefaultWrong'")))
    )
    assert(validationErrors == exp)
  }

  test("Validation for this data should return critical errors") {
    val errors = SchemaChecker.validateSchemaAndLog(schemaWrong)
    assert(errors._1.nonEmpty)
  }

  test("Date Time Standardization Example Test should throw an exception") {
    val std = intercept[ValidationException] {
      StandardizationInterpreter.standardize(data, schemaWrong, "dates")
    }
  }

  test("Date Time Standardization Example with fixed schema should work") {
    val date0 = new Date(0)
    val ts = Timestamp.valueOf("2017-10-20 08:11:31")
    val ts0 = new Timestamp(0)
    val exp = List((
      1L,
      Date.valueOf("2017-10-20"),
      Date.valueOf("2017-10-20"),
      Date.valueOf("2017-12-29"),
      date0,
      date0,
      null,
      ts, ts, ts, null, ts0, ts0,
      List(
        ErrorMessage.stdCastErr("dateSampleWrong1","10-20-2017"),
        ErrorMessage.stdCastErr("dateSampleWrong2","201711"),
        ErrorMessage.stdCastErr("dateSampleWrong3",""),
        ErrorMessage.stdCastErr("timestampSampleWrong1", "20171020T081131"),
        ErrorMessage.stdCastErr("timestampSampleWrong2", "2017-10-20t081131"),
        ErrorMessage.stdCastErr("timestampSampleWrong3", "2017-10-20")
      )
    ))
    val std: Dataset[Row] = StandardizationInterpreter.standardize(data, schemaOk, "dates")
    logDataFrameContent(std)
    assertResult(exp)(std.as[Tuple14[Long, Date, Date, Date, Date, Date, Date, Timestamp, Timestamp, Timestamp, Timestamp, Timestamp,Timestamp, Seq[ErrorMessage]]].collect().toList)
  }
}
