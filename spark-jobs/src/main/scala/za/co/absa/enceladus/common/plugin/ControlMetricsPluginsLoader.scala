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

package za.co.absa.enceladus.common.plugin

import com.typesafe.config.Config
import org.apache.log4j.{LogManager, Logger}
import za.co.absa.enceladus.plugins.api.control.{ControlMetricsPlugin, ControlMetricsPluginFactory}
import za.co.absa.enceladus.utils.general.ClassLoaderUtils

import scala.collection.mutable.ListBuffer

/**
 * This object is responsible for instantiating a chain of plugins that process control metrics on each checkpoints.
 */
object ControlMetricsPluginsLoader {
  private val log: Logger = LogManager.getLogger(this.getClass)

  /**
   * Loads control metric plugins according to configuration.
   *
   * @param config          A configuration.
   * @param configKeyPrefix A key prefix to be used to search for plugins.
   *                        For example, 'standardization.plugin.control.metrics' or 'conformance.plugin.control.metrics'
   * @return A list of loaded postprocessor plugins.
   */
  def loadPlugins(config: Config, configKeyPrefix: String): Seq[ControlMetricsPlugin] = {
    val plugins = new ListBuffer[ControlMetricsPlugin]
    var i = 1

    while (config.hasPath(s"$configKeyPrefix.$i")) {
      val key = s"$configKeyPrefix.$i"
      val factoryName = config.getString(key)
      log.info(s"Going to load a metrics plugin factory for configuration: '$key'. Factory name: $factoryName")
      plugins += buildPlugin(factoryName, config)
      i += 1
    }
    plugins
  }

  private def buildPlugin(factoryName: String, config: Config): ControlMetricsPlugin = {
    val factory = ClassLoaderUtils.loadSingletonClassOfType[ControlMetricsPluginFactory](factoryName)
    factory.apply(config)
  }
}