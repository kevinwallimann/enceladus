@ECHO OFF

:: Copyright 2018 ABSA Group Limited

:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::     http://www.apache.org/licenses/LICENSE-2.0

:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.

SETLOCAL EnableDelayedExpansion

:: Command line for the script itself

:: Show spark-submit command line without actually running it (--dry-run)
SET DRY_RUN=

:: Command line defaults for 'spark-submit'
SET DEPLOY_MODE=%DEFAULT_DEPLOY_MODE%
SET EXECUTOR_MEMORY=%DEFAULT_EXECUTOR_MEMORY%
SET DRIVER_CORES=%DEFAULT_DRIVER_CORES%
SET DRIVER_MEMORY=%DEFAULT_DRIVER_MEMORY%
SET EXECUTOR_CORES=%DEFAULT_EXECUTOR_CORES%
SET NUM_EXECUTORS=%DEFAULT_NUM_EXECUTORS%
SET FILES=%ENCELADUS_FILES%

:: DRA related defaults
SET DRA_ENABLED=%DEFAULT_DRA_ENABLED%

SET DRA_MIN_EXECUTORS=%DEFAULT_DRA_MIN_EXECUTORS%
SET DRA_MAX_EXECUTORS=%DEFAULT_DRA_MAX_EXECUTORS%
SET DRA_ALLOCATION_RATIO=%DEFAULT_DRA_ALLOCATION_RATIO%
SET ADAPTIVE_TARGET_POSTSHUFFLE_INPUT_SIZE=%DEFAULT_ADAPTIVE_TARGET_POSTSHUFFLE_INPUT_SIZE%

:: Command like default for the job
IF DEFINED SPARK_JOBS_JAR_OVERRIDE (
    SET JAR=%SPARK_JOBS_JAR_OVERRIDE%
) ELSE (
    SET JAR=%SPARK_JOBS_JAR%
)
SET DATASET_NAME=
SET DATASET_VERSION=
SET REPORT_DATE=
SET REPORT_VERSION=
SET RAW_FORMAT=
SET CHARSET=
SET ROW_TAG=
SET DELIMITER=
SET HEADER=
SET CSV_QUOTE=
SET CSV_ESCAPE=
SET TRIM_VALUES=
SET COBOL_IS_TEXT=
SET COBOL_ENCODING=
SET IS_XCOM=
SET MAPPING_TABLE_PATTERN=
SET FOLDER_PREFIX=
SET DEBUG_SET_RAW_PATH=
SET EXPERIMENTAL_MAPPING_RULE=
SET CATALYST_WORKAROUND=
SET AUTOCLEAN_STD_FOLDER=
SET PERSIST_STORAGE_LEVEL=
SET EMPTY_VALUES_AS_NULLS=
SET NULL_VALUE=

:: Spark configuration options
SET CONF_SPARK_EXECUTOR_MEMORY_OVERHEAD=
SET CONF_SPARK_MEMORY_FRACTION=

:: Security command line defaults
SET MENAS_CREDENTIALS_FILE=
SET MENAS_AUTH_KEYTAB=

:: Parse command line arguments
:CmdParse
IF "%1"=="" GOTO PastLoop
IF "%1"=="--dry-run" (
    SET DRY_RUN=1
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--num-executors" (
    SET NUM_EXECUTORS=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--executor-cores" (
    SET EXECUTOR_CORES=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--executor-memory" (
    SET EXECUTOR_MEMORY=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--master" (
    SET MASTER=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--deploy-mode" (
    SET DEPLOY_MODE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--driver-cores" (
    SET DRIVER_CORES=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--driver-memory" (
    SET DRIVER_MEMORY=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--files" (
    IF DEFINED ENCELADUS_FILES (
        SET FILES=%ENCELADUS_FILES%,%2
    ) ELSE (
        SET FILES=%2
    )
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--conf-spark-executor-memoryOverhead" (
    SET CONF_SPARK_EXECUTOR_MEMORY_OVERHEAD=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--conf-spark-memory-fraction" (
    SET CONF_SPARK_MEMORY_FRACTION=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--jar" (
    SET JAR=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--class" (
    SET CLASS=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="-D" (
    SET DATASET_NAME=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--dataset-name" (
    SET DATASET_NAME=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="-d" (
    SET DATASET_VERSION=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--dataset-version" (
    SET DATASET_VERSION=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="-R" (
    SET REPORT_DATE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--report-date" (
    SET REPORT_DATE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="-r" (
    SET REPORT_VERSION=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--report-version" (
    SET REPORT_VERSION=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--folder-prefix" (
    SET FOLDER_PREFIX=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="-f" (
    SET RAW_FORMAT=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--raw-format" (
    SET RAW_FORMAT=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--charset" (
    SET CHARSET=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--row-tag" (
    SET ROW_TAG=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--delimiter" (
    SET DELIMITER=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--header" (
    SET HEADER=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--csv-quote" (
    SET CSV_QUOTE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--csv-escape" (
    SET CSV_ESCAPE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--trimValues" (
    SET TRIM_VALUES=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--empty-values-as-nulls" (
    SET EMPTY_VALUES_AS_NULLS=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--null-value" (
    SET NULL_VALUE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--cobol-encoding" (
    SET COBOL_ENCODING=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--cobol-is-text" (
    SET COBOL_IS_TEXT=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--is-xcom" (
    SET IS_XCOM=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--mapping-table-pattern" (
    SET MAPPING_TABLE_PATTERN=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--std-hdfs-path" (
    SET STD_HDFS_PATH=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--debug-set-raw-path" (
    SET DEBUG_SET_RAW_PATH=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--menas-credentials-file" (
    SET MENAS_CREDENTIALS_FILE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--menas-auth-keytab" (
    SET MENAS_AUTH_KEYTAB=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--experimental-mapping-rule" (
    SET EXPERIMENTAL_MAPPING_RULE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--catalyst-workaround" (
    SET CATALYST_WORKAROUND=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--autoclean-std-folder" (
    SET AUTOCLEAN_STD_FOLDER=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--persist-storage-level" (
    SET PERSIST_STORAGE_LEVEL=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--conf-spark-dynamicAllocation-minExecutors" (
    SET DRA_MIN_EXECUTORS=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--conf-spark-dynamicAllocation-maxExecutors" (
    SET DRA_MAX_EXECUTORS=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--conf-spark-dynamicAllocation-executorAllocationRatio" (
    SET DRA_ALLOCATION_RATIO=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--conf-spark-sql-adaptive-shuffle-targetPostShuffleInputSize" (
    SET ADAPTIVE_TARGET_POSTSHUFFLE_INPUT_SIZE=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
IF "%1"=="--set-dra" (
    SET DRA_ENABLED=%2
    SHIFT
    SHIFT
    GOTO CmdParse
)
SHIFT
GOTO CmdParse

:PastLoop

:: Validation
SET VALID="1"

CALL :validate --dataset-name,%DATASET_NAME%
CALL :validate --dataset-version,%DATASET_VERSION%
CALL :validate --report-date,%REPORT_DATE%
CALL :validate_either --menas-credentials-file,%MENAS_CREDENTIALS_FILE%,--menas-auth-keytab,%MENAS_AUTH_KEYTAB%

:: For now this check is disabled in Windows version of the script
:: IF NOT "%MASTER%"=="yarn" (
::   ECHO "Master '%MASTER%' is not allowed. The only allowed master is 'yarn'."
::   SET VALID="0"
:: )

:: Validation failure check
IF %VALID%=="0" EXIT /B 1



:: ### Bellow construct the command line ###

:: Puts Spark configuration properties to the command line
:: Constructing the grand command line
:: Configuration passed to JVM

IF DEFINED MAPPING_TABLE_PATTERN (
    SET MT_PATTERN=-Dconformance.mappingtable.pattern=%MAPPING_TABLE_PATTERN%
) ELSE (
    SET MT_PATTERN=
)

SET SPARK_CONF=--conf spark.logConf=true

:: Dynamic Resource Allocation
:: check DRA safe prerequisites
IF %DRA_ENABLED%==true (
    IF DEFINED NUM_EXECUTORS (
        ECHO WARNING: num-executors should NOT be set when using Dynamic Resource Allocation. DRA is disabled.
        SET DRA_ENABLED=false
    )
    IF NOT DEFINED DRA_MAX_EXECUTORS (
        ECHO WARNING: maxExecutors should be set for Dynamic Resource Allocation. DRA is disabled
        SET DRA_ENABLED=false
    )
)

:: configure DRA and adaptive execution if enabled
IF %DRA_ENABLED%==true (
    ECHO Dynamic Resource Allocation enabled
    SET SPARK_CONF=%SPARK_CONF% --conf spark.dynamicAllocation.enabled=true
    SET SPARK_CONF=%SPARK_CONF% --conf spark.shuffle.service.enabled=true
    SET SPARK_CONF=%SPARK_CONF% --conf spark.sql.adaptive.enabled=true
    SET SPARK_CONF=%SPARK_CONF% --conf spark.dynamicAllocation.maxExecutors=%DRA_MAX_EXECUTORS%
    IF NOT "%DRA_MIN_EXECUTORS%"=="" SET SPARK_CONF=%SPARK_CONF% --conf spark.dynamicAllocation.minExecutors=%DRA_MIN_EXECUTORS%
    IF NOT "%DRA_ALLOCATION_RATIO%"=="" SET SPARK_CONF=%SPARK_CONF% --conf spark.dynamicAllocation.executorAllocationRatio=%DRA_ALLOCATION_RATIO%
    IF NOT "%ADAPTIVE_TARGET_POSTSHUFFLE_INPUT_SIZE%"=="" SET SPARK_CONF=%SPARK_CONF% --conf spark.sql.adaptive.shuffle.targetPostShuffleInputSize=%ADAPTIVE_TARGET_POSTSHUFFLE_INPUT_SIZE%
)

SET JVM_CONF=spark.driver.extraJavaOptions=-Dstandardized.hdfs.path=%STD_HDFS_PATH% -Dspline.mongodb.url=%SPLINE_MONGODB_URL% -Dspline.mongodb.name=%SPLINE_MONGODB_NAME% -Dhdp.version=%HDP_VERSION% %MT_PATTERN%

SET CMD_LINE=%SPARK_SUBMIT%

:: Adding command line parameters that go BEFORE the jar file
IF NOT "%MASTER%"=="" SET CMD_LINE=%CMD_LINE% --master %MASTER%
IF NOT "%DEPLOY_MODE%"=="" SET CMD_LINE=%CMD_LINE% --deploy-mode %DEPLOY_MODE%
IF NOT "%NUM_EXECUTORS%"=="" SET CMD_LINE=%CMD_LINE% --num-executors %NUM_EXECUTORS%
IF NOT "%EXECUTOR_MEMORY%"=="" SET CMD_LINE=%CMD_LINE% --executor-memory %EXECUTOR_MEMORY%
IF NOT "%EXECUTOR_CORES%"=="" SET CMD_LINE=%CMD_LINE% --executor-cores %EXECUTOR_CORES%
IF NOT "%DRIVER_CORES%"=="" SET CMD_LINE=%CMD_LINE% --driver-cores %DRIVER_CORES%
IF NOT "%DRIVER_MEMORY%"=="" SET CMD_LINE=%CMD_LINE% --driver-memory %DRIVER_MEMORY%
IF NOT "%FILES%"=="" SET CMD_LINE=%CMD_LINE% --files %FILES%

:: Adding Spark config options
CALL :add_spark_conf_cmd spark.executor.memoryOverhead %CONF_SPARK_EXECUTOR_MEMORY_OVERHEAD%
CALL :add_spark_conf_cmd spark.memory.fraction %CONF_SPARK_MEMORY_FRACTION%
IF NOT "%CONF_SPARK_EXECUTOR_MEMORY_OVERHEAD%"=="" SET SPARK_CONF=%SPARK_CONF% --conf spark.executor.memoryOverhead=%CONF_SPARK_EXECUTOR_MEMORY_OVERHEAD%
IF NOT "%CONF_SPARK_MEMORY_FRACTION%"=="" SET SPARK_CONF=%SPARK_CONF% --conf spark.memory.fraction=%CONF_SPARK_MEMORY_FRACTION%

:: Adding JVM configuration, entry point class name and the jar file
IF "%DEPLOY_MODE%"=="client" (
  SET ADDITIONAL_JVM_CONF=%ADDITIONAL_JVM_CONF_CLIENT%
) ELSE (
  SET ADDITIONAL_JVM_CONF=%ADDITIONAL_JVM_CONF_CLUSTER%
)
SET CMD_LINE=%CMD_LINE% %ADDITIONAL_SPARK_CONF% %SPARK_CONF% --conf "%JVM_CONF% %ADDITIONAL_JVM_CONF%" --class %CLASS% %JAR%

:: Adding command line parameters that go AFTER the jar file
IF NOT "%MENAS_AUTH_KEYTAB%"=="" SET CMD_LINE=%CMD_LINE% --menas-auth-keytab %MENAS_AUTH_KEYTAB%
IF NOT "%MENAS_CREDENTIALS_FILE%"=="" SET CMD_LINE=%CMD_LINE% --menas-credentials-file %MENAS_CREDENTIALS_FILE%
IF NOT "%DATASET_NAME%"=="" SET CMD_LINE=%CMD_LINE% --dataset-name %DATASET_NAME%
IF NOT "%DATASET_VERSION%"=="" SET CMD_LINE=%CMD_LINE% --dataset-version %DATASET_VERSION%
IF NOT "%REPORT_DATE%"=="" SET CMD_LINE=%CMD_LINE% --report-date %REPORT_DATE%
IF NOT "%REPORT_VERSION%"=="" SET CMD_LINE=%CMD_LINE% --report-version %REPORT_VERSION%
IF NOT "%RAW_FORMAT%"=="" SET CMD_LINE=%CMD_LINE% --raw-format %RAW_FORMAT%
IF NOT "%CHARSET%"=="" SET CMD_LINE=%CMD_LINE% --charset %CHARSET%
IF NOT "%ROW_TAG%"=="" SET CMD_LINE=%CMD_LINE% --row-tag %ROW_TAG%
IF NOT "%DELIMITER%"=="" SET CMD_LINE=%CMD_LINE% --delimiter %DELIMITER%
IF NOT "%HEADER%"=="" SET CMD_LINE=%CMD_LINE% --header %HEADER%
IF NOT "%CSV_QUOTE%"=="" SET CMD_LINE=%CMD_LINE% --csv-quote %CSV_QUOTE%
IF NOT "%CSV_ESCAPE%"=="" SET CMD_LINE=%CMD_LINE% --csv-escape %CSV_ESCAPE%
IF NOT "%TRIM_VALUES%"=="" SET CMD_LINE=%CMD_LINE% --trimValues %TRIM_VALUES%
IF NOT "%EMPTY_VALUES_AS_NULLS%"=="" SET CMD_LINE=%CMD_LINE% --empty-values-as-nulls %EMPTY_VALUES_AS_NULLS%
IF NOT "%NULL_VALUE%"=="" SET CMD_LINE=%CMD_LINE% --null-value %NULL_VALUE%
IF NOT "%COBOL_IS_TEXT%"=="" SET CMD_LINE=%CMD_LINE% --cobol-is-text %COBOL_IS_TEXT%
IF NOT "%COBOL_ENCODING%"=="" SET CMD_LINE=%CMD_LINE% --cobol-encoding %COBOL_ENCODING%
IF NOT "%IS_XCOM%"=="" SET CMD_LINE=%CMD_LINE% --is-xcom %IS_XCOM%
IF NOT "%FOLDER_PREFIX%"=="" SET CMD_LINE=%CMD_LINE% --folder-prefix %FOLDER_PREFIX%
IF NOT "%DEBUG_SET_RAW_PATH%"=="" SET CMD_LINE=%CMD_LINE% --debug-set-raw-path %DEBUG_SET_RAW_PATH%
IF NOT "%EXPERIMENTAL_MAPPING_RULE%"=="" SET CMD_LINE=%CMD_LINE% --experimental-mapping-rule %EXPERIMENTAL_MAPPING_RULE%
IF NOT "%CATALYST_WORKAROUND%"=="" SET CMD_LINE=%CMD_LINE% --catalyst-workaround %CATALYST_WORKAROUND%
IF NOT "%AUTOCLEAN_STD_FOLDER%"=="" SET CMD_LINE=%CMD_LINE% --autoclean-std-folder %AUTOCLEAN_STD_FOLDER%
IF NOT "%PERSIST_STORAGE_LEVEL%"=="" SET CMD_LINE=%CMD_LINE% --persist-storage-level %PERSIST_STORAGE_LEVEL%

ECHO Command line:
ECHO %CMD_LINE%

IF DEFINED DRY_RUN GOTO :eof

IF "%DEPLOY_MODE%"=="client" GOTO client_run

%CMD_LINE%
GOTO :eof

:client_run
CALL :temp_log_file TMP_PATH_NAME

:: Initializing Kerberos ticket
IF DEFINED MENAS_AUTH_KEYTAB (
    CALL :echoerr "Menas keytab authentication is not yet supported in Windows helper scripts."
    :: Get principle stored in the keyfile (Thanks @Zejnilovic)
    :: PR=`printf "read_kt $MENAS_AUTH_KEYTAB\nlist" | ktutil | grep -Pio "(?<=\ )[A-Za-z0-9\-\._]*?(?=@)" | head -1`
    :: Alternative way, might be less reliable
    :: PR=`printf "read_kt $MENAS_AUTH_KEYTAB\nlist" | ktutil | sed -n '5p' | awk '{print $3}' | cut -d '@' -f1`
    IF DEFINED PR (
        kinit -k -t "%MENAS_AUTH_KEYTAB%" "%PR%"
        klist 2>&1 | tee -a %TMP_PATH_NAME%
    ) ELSE (
        CALL :echoerr "WARNING!"
        CALL :echoerr "Unable to determine principle from the keytab file %MENAS_AUTH_KEYTAB%."
        CALL :echoerr "Please make sure Kerberos ticket is initialized by running 'kinit' manually."
        CALL :sleep 10
    )
)
ECHO The log will be saved to %TMP_PATH_NAME%
ECHO %CMD_LINE% >> %TMP_PATH_NAME%
:: Run the job and return exit status 1 if the %CMD_LINE% exited with non-zero (trick from https://stackoverflow.com/questions/877639)
SET ERROR_SIGNAL_FILE=%TEMP%\enc_failed.tmp
ECHO >%ERROR_SIGNAL_FILE%
(%CMD_LINE% && DEL %ERROR_SIGNAL_FILE%) | tee -a %TMP_PATH_NAME%
:: Save the exit status of spark submit subshell run
SET EXIT_STATUS=%ERRORLEVEL%
:: Test if the command executed successfully
IF EXIST %ERROR_SIGNAL_FILE% (
    DEL %ERROR_SIGNAL_FILE%
    SET EXIT_STATUS=1
    SET RESULT=failed
) ELSE (
    SET EXIT_STATUS=0
    SET RESULT=passed
)
:: Report the result and log location
ECHO ""
ECHO Job %RESULT%. Refer to logs at %TMP_PATH_NAME% | tee -a %TMP_PATH_NAME%
EXIT /B %EXIT_STATUS%

:: Functions

:add_to_cmd_line
    IF NOT "%~2"=="" SET CMD_LINE=%CMD_LINE% %~1 %~2
EXIT /B 0

:add_spark_conf_cmd
    SET VALUE=%~2
    IF DEFINED VALUE (
        SET SPARK_CONF=%SPARK_CONF% %~1 %VALUE%
    )
EXIT /B 0

:echoerr
    ECHO %~1 1>&2;
EXIT /B 0

:temp_log_file
    SET yyyy=%date:~-4%
    SET MM=%date:~3,2%
    SET DD=%date:~0,2%
    ::alternate date parsing
    ::SET YYYY=%date:~10,4%
    ::SET MM=%date:~4,2%
    ::SET DD=%date:~7,2%
    SET HH=%time:~0,2%
    IF %HH% lss 10 (SET HH=0%time:~1,1%)
    SET MI=%time:~3,2%
    SET SS=%time:~6,2%
    SET DATETIME=%YYYY%_%MM%_%DD%-%HH%_%MI%_%SS%
    CALL :last_part NAME,.,%CLASS%
    :loop_gtlf
    SET LOG_FILE=%LOG_DIR%\enceladus_%NAME%_%DATETIME%_%RANDOM%.log
    IF EXIST %LOG_FILE% GOTO loop_gtlf
    ECHO. > %LOG_FILE%
    SET %~1=%LOG_FILE%
EXIT /B 0

:last_part
    :: Returns the string after the last occurance of the delimiter
    :: %~1 result
    :: %~2 delimiter
    :: %~3 input string
    CALL :reverse REVERSED,%~3
    FOR /F " delims=%~2" %%a in ("%REVERSED%") do set PRERESULT=%%a
    CALL :reverse RESULT,%PRERESULT%
    SET %~1=%RESULT%
EXIT /B 0

:reverse
    :: Returns the string reversed (based on https://stackoverflow.com/questions/33048743/)
    :: %~1 result
    :: %~2 input string
    SET LINE=%~2
    SET NUM=0
    SET RLINE=
    :loop_reverse
    CALL SET TMPA=%%LINE:~%NUM%,1%%%
    SET /a NUM+=1
    IF NOT "%TMPA%"=="" (
        SET RLINE=%TMPA%%RLINE%
        GOTO loop_reverse
    )
    SET %~1=%RLINE%
EXIT /B 0

:sleep
    :: Somewhat a hack but should workaround
    ping 127.0.0.1 -n %~1 > nul
EXIT /B 0

:validate
    SET VALUE=%~2
    IF NOT DEFINED VALUE (
        ECHO Missing mandatory option %~1
        SET VALID="0"
    )
EXIT /B 0

:validate_either
    SET VALUE1=%~2
    IF NOT DEFINED VALUE1 (
        SET VALUE2=%~4
        IF NOT DEFINED VALUE2 (
            ECHO Either %~1 or %~3 should be specified
            SET VALID="0"
        )
    )
EXIT /B 0
