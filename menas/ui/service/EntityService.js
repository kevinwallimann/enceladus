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

var EntityService = new function () {

  this.buildDisableFailureMsg = function(oData, sEntityType) {
    let err = "Disabling " + sEntityType + " failed. The following entities are dependent on it and should be disabled first. " +
      "More details can be found in the \"Used In\" tab:\n";

    let sDatasetMsg = buildDependenciesErrorMsg(oData["datasets"], "Datasets");
    let sMappingTableMsg = buildDependenciesErrorMsg(oData["mappingTables"], "\nMapping Tables");
    return err + sDatasetMsg + sMappingTableMsg
  };

  let buildDependenciesErrorMsg = function(aCollection, sHeader) {
    let message = "";
    let entities = new Set(aCollection.map(entity => entity.name));
    if (entities.size !== 0) {
      message += sHeader + ":\n- " + Array.from(entities).slice(0, 10).join("\n- ");
      if (entities.size > 10) {
        message += "\n+ " + (entities.size - 10) + " more..."
      }
    }
    return message;
  };

}();
