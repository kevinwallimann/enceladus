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

package za.co.absa.enceladus.migrations.framework

object ObjectIdTools {
  /**
    * Gets a MongoDB Object Id from a JSON string if it has one.
    * The format of an ObjectId is expected to be like this:
    *
    * `"_id" : { "$oid" : "5b98eea5a43a28a6154a2453" }"`
    *
    * @param document A JSON document as a string
    * @return A substring representing an ObjectId extracted from the document, if present
    */
  def getObjectIdFromDocument(document: String): Option[String] = {
    val oidRegExp = """(\"_id\"[.\s]*\:[.\s]*\{[a-zA-Z0-9\s\"\$\:]*\})""".r
    oidRegExp.findFirstIn(document)
  }

  /**
    * Puts an ObjectId (if provided) into a JSON document if there is no ObjectId there already.
    * The format of an ObjectId is expected to be like this:
    *
    * `"_id" : { "$oid" : "5b98eea5a43a28a6154a2453" }"`
    *
    * @param document    A JSON document as a string
    * @param objectIdOpt an optional ObjectId to put into the document
    * @return A substring representing an ObjectId extracted from the document, if present
    */
  def putObjectIdIfNotPresent(document: String, objectIdOpt: Option[String]): String = {
    if (getObjectIdFromDocument(document).isEmpty) {
      val newDocument = objectIdOpt match {
        case Some(id) =>
          val i = document.indexOf('{')
          if (i >= 0) {
            val start = document.slice(0, i + 1)
            val end = document.slice(i + 1, document.length)
            s"$start$id,$end"
          } else {
            document
          }
        case None =>
          document
      }
      newDocument
    } else {
      document
    }
  }
}
