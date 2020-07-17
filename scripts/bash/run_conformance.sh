#!/bin/bash

# Copyright 2018 ABSA Group Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

SRC_DIR=`dirname "$0"`

source ${SRC_DIR}/enceladus_env.sh

export CLASS=${CONF_CLASS}
export JAR=${SPARK_JOBS_JAR_OVERRIDE:-$SPARK_JOBS_JAR}

export DEFAULT_DRIVER_MEMORY="$CONF_DEFAULT_DRIVER_MEMORY"
export DEFAULT_DRIVER_CORES="$CONF_DEFAULT_DRIVER_CORES"
export DEFAULT_EXECUTOR_MEMORY="$CONF_DEFAULT_EXECUTOR_MEMORY"
export DEFAULT_EXECUTOR_CORES="$CONF_DEFAULT_EXECUTOR_CORES"
export DEFAULT_NUM_EXECUTORS="$CONF_DEFAULT_NUM_EXECUTORS"

export DEFAULT_DRA_ENABLED="$CONF_DEFAULT_DRA_ENABLED"

export DEFAULT_DRA_MIN_EXECUTORS="$CONF_DEFAULT_DRA_MIN_EXECUTORS"
export DEFAULT_DRA_MAX_EXECUTORS="$CONF_DEFAULT_DRA_MAX_EXECUTORS"
export DEFAULT_DRA_ALLOCATION_RATIO="$CONF_DEFAULT_DRA_ALLOCATION_RATIO"
export DEFAULT_ADAPTIVE_TARGET_POSTSHUFFLE_INPUT_SIZE="$CONF_DEFAULT_ADAPTIVE_TARGET_POSTSHUFFLE_INPUT_SIZE"

source ${SRC_DIR}/run_enceladus.sh
