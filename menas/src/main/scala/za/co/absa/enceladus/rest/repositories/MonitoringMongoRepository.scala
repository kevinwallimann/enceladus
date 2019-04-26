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
import java.util

import org.json4s.jackson.Json
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Field

import scala.collection.immutable.HashMap
//import org.mongodb.scala.{AggregateObservable, Completed, Document, MapReduceObservable, MongoDatabase}
//import org.mongodb.scala.bson.BsonDocument
//import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
//import org.mongodb.scala.model.{FindOneAndUpdateOptions, ReturnDocument, Updates}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import za.co.absa.atum.model.{Checkpoint, ControlMeasure, RunStatus}
import za.co.absa.atum.utils.ControlUtils
import za.co.absa.enceladus.model.{Run, SplineReference}
import za.co.absa.enceladus.rest.models.{MonitoringDataPoint, MonitoringDataPointWrapper}
import za.co.absa.enceladus.rest.models.RunWrapper
import scala.concurrent.Future
import org.mongodb.scala.Document

object MonitoringMongoRepository {
  val collectionName = "run"
}

@Repository
class MonitoringMongoRepository @Autowired()(mongoDb: MongoDatabase)
  extends MongoRepository[Run](mongoDb) {

  import scala.concurrent.ExecutionContext.Implicits.global
  private[repositories] override def collectionName: String = MonitoringMongoRepository.collectionName


  def getMonitoringDataPoints(datasetName: String): Future[Seq[String]] = {
    // scala mongodb driver does not yet support all mql features, so we use Document() with aggregate pipelines
    val observable: AggregateObservable[Document] = collection
      .aggregate(Seq(
        // filter by dataset name
        filter(equal("dataset", datasetName)),
        //add casted dates
        Document("""{$addFields: {
                   |          startDateTimeCasted: {
                   |              $dateFromString: {
                   |                  dateString: "$startDateTime",
                   |                  format: "%d-%m-%Y %H:%M:%S %z",
                   |                  onError: {$dateFromString: {
                   |                      dateString: "$startDateTime",
                   |                      format: "%d-%m-%Y %H:%M:%S",
                   |                      onError: "wrongFormat"
                   |                  }}
                   |              }
                   |          },
                   |          informationDateCasted: {
                   |              $dateFromString: {
                   |                  dateString: "$controlMeasure.metadata.informationDate",
                   |                  format: "%Y-%m-%d",
                   |                  onError: {$dateFromString: {
                   |                      dateString: "$controlMeasure.metadata.informationDate",
                   |                      format: "%d-%m-%Y",
                   |                      onError: "wrongFormat"
                   |                  }}
                   |              }
                   |          },
                   | }},""".stripMargin),
        // TODO: filter by informationDateCasted in order to limit the number of elements
        // bring the raw checkpoint to root for further access
        Document(""" {$addFields: {
                   |           raw_checkpoint : {
                   |              $arrayElemAt : [ {
                   |                  $filter : {
                   |                      input : "$controlMeasure.checkpoints",
                   |                      as : "checkpoint",
                   |                      cond : { $eq : [ "$$checkpoint.name", "Raw"] }
                   |                  }
                   |              }, 0 ]
                   |          }
                   |      }}""".stripMargin),
        // add the raw recordcount
        Document(""" {$addFields: {
                   |           raw_recordcount_control : {
                   |              $arrayElemAt : [ {
                   |                  $filter : {
                   |                      input : "$raw_checkpoint.controls",
                   |                      as : "control",
                   |                      cond : { $eq : [ "$$control.controlName", "recordcount"] }
                   |                  }
                   |              }, 0 ]
                   |          }
                   |      }}""".stripMargin),
        // sort intermidiate results before further grouping (needed as we use $first to keep the latest run only)
        Document(
          """{$sort: {
            |    informationDateCasted : -1,
            |    "controlMeasure.metadata.version" : -1,
            |    startDateTimeCasted : -1
            |} }""".stripMargin),
        // group, so that we have a single object per infoDate and report version, which corresponds to the latest run
        // here we also project the data of interest
        Document(""" {$group : {
                   |          "_id": {informationDateCasted: "$informationDateCasted", reportVersion: "$controlMeasure.metadata.version"},
                   |          "runObjectId" : {$first: "$_id"},
                   |          "startDateTime" : {$first: "$startDateTimeCasted"},
                   |          "datasetVersion" : {$first: "$datasetVersion"},
                   |          informationDate : {$first: "$controlMeasure.metadata.informationDate"},
                   |          informationDateCasted : {$first: "$informationDateCasted"},
                   |          reportVersion : {$first: "$controlMeasure.metadata.version"},
                   |          "runId" : {$first: "$runId"} ,
                   |          "status" : {$first: "$runStatus.status"},
                   |          "std_records_succeeded" : {$first: "$controlMeasure.metadata.additionalInfo.std_records_succeeded"},
                   |          "std_records_failed" : {$first: "$controlMeasure.metadata.additionalInfo.std_records_failed"},
                   |          "conform_records_succeeded" : {$first: "$controlMeasure.metadata.additionalInfo.conform_records_succeeded"},
                   |          "conform_records_failed" : {$first: "$controlMeasure.metadata.additionalInfo.conform_records_failed"},
                   |          raw_recordcount : {$first: "$raw_recordcount_control.controlValue"},
                   |
                   |      }}""".stripMargin),
        // sort the final results
        Document("""{$sort: {informationDateCasted : -1, reportVersion: -1}}""".stripMargin),
        Document("""{$limit: 20}""".stripMargin)

      ))

    //val observable: AggregateObservable[Document] = collection.aggregate(Seq(filter(equal("dataset", datasetName))))

    observable.map(doc => doc.toJson).toFuture()
  }

  def getRecentCheckpointsPerDataset(datasetName: String): Future[Seq[String]] = {

    val observable: AggregateObservable[Document] = collection
      .aggregate(Seq(
        // filter by user name
        filter(equal("dataset", datasetName)),
        // expand to one document per checkpoint
        Document("""{$unwind: "$controlMeasure.checkpoints"}""".stripMargin),
        // add casted start and end time
        Document(
          """
            |{$addFields: {
            |          processStartTimeCasted: {
            |              $dateFromString: {
            |                  dateString: "$controlMeasure.checkpoints.processStartTime",
            |                  format: "%d-%m-%Y %H:%M:%S %z",
            |                  onError: {$dateFromString: {
            |                      dateString: "$controlMeasure.checkpoints.processStartTime",
            |                      format: "%d-%m-%Y %H:%M:%S",
            |                      onError: "wrongFormat"
            |                  }}
            |              }
            |          },
            |          processEndTimeCasted: {
            |              $dateFromString: {
            |                  dateString: "$controlMeasure.checkpoints.processEndTime",
            |                  format: "%d-%m-%Y %H:%M:%S %z",
            |                  onError: {$dateFromString: {
            |                      dateString: "$controlMeasure.checkpoints.processEndTime",
            |                      format: "%d-%m-%Y %H:%M:%S",
            |                      onError: "wrongFormat"
            |                  }}
            |              }
            |          }
            |  }}
          """.stripMargin),
        // sort by checkpoint start time, end time and runID
        Document("""{$sort: { processEndTimeCasted: -1, processStartTimeCasted : -1, runId : -1 } }""".stripMargin),
        // limit the number of output documents
        Document("""{$limit: 100}""".stripMargin),
        // project documents to desired format
        Document(
          """
            |{$project : {
            |     "_id": 1,
            |     "username" : 1,
            |     "dataset" : 1,
            |     "datasetVersion" : 1,
            |     "runId" : 1,
            |     "informationDate" : "$controlMeasure.metadata.informationDate",
            |     "reportVersion" : "$controlMeasure.metadata.version",
            |     "runStatus" : "$runStatus.status",
            |     "processStartTimeCasted" : 1,
            |     "processEndTimeCasted" : 1,
            |     "processStartTime" : "$controlMeasure.checkpoints.processStartTime",
            |     "processEndTime" : "$controlMeasure.checkpoints.processEndTime",
            |     "checkpointName" : "$controlMeasure.checkpoints.name",
            |     "workflowName" : "$controlMeasure.checkpoints.workflowName"
            |      }}
          """.stripMargin)
      ))
    observable.map(doc => doc.toJson).toFuture()
  }

  def getRecentCheckpointsPerUser(userName: String): Future[Seq[String]] = {
    // scala mongodb driver does not yet support all mql features, so we use Document() with aggregate pipelines
    val observable: AggregateObservable[Document] = collection
      .aggregate(Seq(
        // filter by user name
        filter(equal("username", userName)),
        // expand to one document per checkpoint
        Document("""{$unwind: "$controlMeasure.checkpoints"}""".stripMargin),
        // add casted start and end time
        Document(
          """
            |{$addFields: {
            |          processStartTimeCasted: {
            |              $dateFromString: {
            |                  dateString: "$controlMeasure.checkpoints.processStartTime",
            |                  format: "%d-%m-%Y %H:%M:%S %z",
            |                  onError: {$dateFromString: {
            |                      dateString: "$controlMeasure.checkpoints.processStartTime",
            |                      format: "%d-%m-%Y %H:%M:%S",
            |                      onError: "wrongFormat"
            |                  }}
            |              }
            |          },
            |          processEndTimeCasted: {
            |              $dateFromString: {
            |                  dateString: "$controlMeasure.checkpoints.processEndTime",
            |                  format: "%d-%m-%Y %H:%M:%S %z",
            |                  onError: {$dateFromString: {
            |                      dateString: "$controlMeasure.checkpoints.processEndTime",
            |                      format: "%d-%m-%Y %H:%M:%S",
            |                      onError: "wrongFormat"
            |                  }}
            |              }
            |          }
            |  }}
          """.stripMargin),
        // sort by checkpoint start time, end time and runID
        Document("""{$sort: { processEndTimeCasted: -1, processStartTimeCasted : -1, runId : -1 } }""".stripMargin),
        // limit the number of output documents
        Document("""{$limit: 100}""".stripMargin),
        // project documents to desired format
        Document(
          """
            |{$project : {
            |     "_id": 1,
            |     "username" : 1,
            |     "dataset" : 1,
            |     "datasetVersion" : 1,
            |     "runId" : 1,
            |     "informationDate" : "$controlMeasure.metadata.informationDate",
            |     "reportVersion" : "$controlMeasure.metadata.version",
            |     "runStatus" : "$runStatus.status",
            |     "processStartTimeCasted" : 1,
            |     "processEndTimeCasted" : 1,
            |     "processStartTime" : "$controlMeasure.checkpoints.processStartTime",
            |     "processEndTime" : "$controlMeasure.checkpoints.processEndTime",
            |     "checkpointName" : "$controlMeasure.checkpoints.name",
            |     "workflowName" : "$controlMeasure.checkpoints.workflowName"
            |      }}
          """.stripMargin)
      ))
    observable.map(doc => doc.toJson).toFuture()
  }
}

