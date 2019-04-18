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

package za.co.absa.enceladus.migrations.migrations

import za.co.absa.enceladus.migrations.framework.migration.CollectionMigration

/**
  * This object is the initial migration from "nothing" to version 0. Basically, it just lists the collections that are
  * expected to be at version 0. No JSON transformations are possible at this stage, but queries can be ran.
  */
object MigrationToV0 extends CollectionMigration {

  override val targetVersion: Int = 0

  addCollection("dataset")
  addCollection("schema")
  addCollection("mapping_table")
  addCollection("run")
}