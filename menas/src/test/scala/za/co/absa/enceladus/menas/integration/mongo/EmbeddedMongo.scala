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

package za.co.absa.enceladus.menas.integration.mongo


import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.runtime.Network
import javax.annotation.{PostConstruct, PreDestroy}
import org.apache.naming.factory.FactoryBase
import org.mongodb.scala.{MongoClient, MongoDatabase}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.{Bean, Configuration}
import za.co.absa.enceladus.menas.utils.implicits.codecRegistry

/**
 * Provides an embedded local mongo. Spawn it before tests and shutdown after
 */
@TestConfiguration
class EmbeddedMongo {

  @Bean // tried mockbean, too
  def mongoDatabaseBean: MongoDatabaseFactoryBean = {
    new MongoDatabaseFactoryBean
  }

}

@Configuration
class MongoDatabaseFactoryBean extends FactoryBean[MongoDatabase] {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private var mongodExecutable: MongodExecutable = _
  private var mongoPort: Int = _

  def getMongoUri: String = s"mongodb://localhost:$mongoPort/?ssl=false"

  def getMongoPort: Int = mongoPort

  @Value("${menas.mongo.connection.database}")
  val database: String = ""

  //@PostConstruct
  def runDummyMongo(): Unit = {
    val starter = MongodStarter.getDefaultInstance

    synchronized {
      mongoPort = Network.getFreeServerPort()
      val mongodConfig = new MongodConfigBuilder()
        .version(Version.Main.V4_0)
        .net(new Net("localhost", mongoPort, Network.localhostIsIPv6()))
        .build()

      mongodExecutable = starter.prepare(mongodConfig)
    }

    mongodExecutable.start()
    //mongoDb = MongoClient(getMongoUri).getDatabase(database).withCodecRegistry(codecRegistry)
    logger.debug(s"*** mongod started at port $mongoPort")
  }

  //@PreDestroy
  def shutdownDummyMongo(): Unit = {
    mongodExecutable.stop()
  }

  override def getObject: MongoDatabase = {
    runDummyMongo
    val mongodb = MongoClient(getMongoUri).getDatabase(database).withCodecRegistry(codecRegistry)
    mongodb
  }

  override def getObjectType: Class[_] = classOf[MongoDatabase]
}
