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

package za.co.absa.enceladus.rest.repositories

import org.mongodb.scala.{Completed, MongoDatabase}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import za.co.absa.enceladus.model.Dataset
import za.co.absa.enceladus.model.conformanceRule.MappingConformanceRule

import scala.concurrent.Future
import scala.reflect.ClassTag

object DatasetMongoRepository {
  val collectionName = "dataset"
}

@Repository
class DatasetMongoRepository @Autowired()(mongoDb: MongoDatabase)
  extends VersionedMongoRepository[Dataset](mongoDb)(ClassTag(classOf[Dataset])) {

  import scala.concurrent.ExecutionContext.Implicits.global

  private[repositories] override def collectionName: String = DatasetMongoRepository.collectionName

  override def getVersion(name: String, version: Int): Future[Option[Dataset]] = {
    super.getVersion(name, version).map(_.map(handleMappingRuleRead))
  }

  override def getAllVersions(name: String): Future[Seq[Dataset]] = {
    super.getAllVersions(name).map(_.map(handleMappingRuleRead))
  }

  override def create(item: Dataset, username: String): Future[Completed] = {
    super.create(handleMappingRuleWrite(item), username)
  }

  override def update(username: String, updated: Dataset): Future[Completed] = {
    super.update(username, handleMappingRuleWrite(updated))
  }

  private def handleMappingRuleWrite(dataset: Dataset): Dataset = {
    handleMappingRule(dataset, replaceForWrite)
  }

  private def handleMappingRuleRead(dataset: Dataset): Dataset = {
    handleMappingRule(dataset, replaceForRead)
  }

  private def handleMappingRule(dataset: Dataset, replace: String => String): Dataset = {
    val conformance = dataset.conformance.map {
      case mr: MappingConformanceRule =>
        val map = mr.attributeMappings.map {
          case (key, value) => (replace(key), value)
        }
        mr.copy(attributeMappings = map)
      case any => any
    }
    dataset.setConformance(conformance)
  }

  private val ORIGINAL_DELIMETER = '.'
  private val REPLACEMENT_DELIMETER = MappingConformanceRule.DOT_REPLACEMENT_SYMBOL

  private def replaceForWrite(key: String): String = key.replace(ORIGINAL_DELIMETER, REPLACEMENT_DELIMETER)

  private def replaceForRead(key: String): String = key.replace(REPLACEMENT_DELIMETER, ORIGINAL_DELIMETER)

}
