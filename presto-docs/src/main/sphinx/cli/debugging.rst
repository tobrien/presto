====================
Debugging Presto CLI
====================


$ ./presto --server http://54.204.239.155:8080 --catalog jmx --schema jmx --debug
2013-12-29T15:17:59.166-0600	  INFO	  main	   io.airlift.log.Logging     Logging to stderr
presto:jmx> select name from "java.lang:type=runtime";
            name             
-----------------------------
 4165@domU-12-31-39-0F-CC-72 
(1 row)

Query 20131229_211533_00017_dk5x2, FINISHED, 1 node
http://54.204.239.155:8080/v1/query/20131229_211533_00017_dk5x2?pretty
Splits: 2 total, 2 done (100.00%)
CPU Time: 0.0s total,  1000 rows/s, 26.4KB/s, 16% active
Per Node: 0.0 parallelism,     3 rows/s,   106B/s
Parallelism: 0.0
0:00 [1 rows, 27B] [3 rows/s, 106B/s]




http://54.204.239.155:8080/v1/query/20131229_211533_00017_dk5x2?pretty



{
  "queryId" : "20131229_211533_00017_dk5x2",
  "session" : {
    "user" : "tobrie1",
    "source" : "presto-cli",
    "catalog" : "jmx",
    "schema" : "jmx",
    "remoteUserAddress" : "173.15.79.89",
    "userAgent" : "StatementClient/0.55-SNAPSHOT",
    "startTime" : 1388351852026
  },
  "state" : "FINISHED",
  "self" : "http://10.193.207.128:8080/v1/query/20131229_211533_00017_dk5x2",
  "fieldNames" : [ "name" ],
  "query" : "select name from \"java.lang:type=runtime\"",
  "queryStats" : {
    "createTime" : "2013-12-29T16:17:32.027-05:00",
    "executionStartTime" : "2013-12-29T16:17:32.086-05:00",
    "lastHeartbeat" : "2013-12-29T16:17:44.561-05:00",
    "endTime" : "2013-12-29T16:17:32.152-05:00",
    "elapsedTime" : "125.00ms",
    "queuedTime" : "1.31ms",
    "analysisTime" : "4.84ms",
    "distributedPlanningTime" : "353.00us",
    "totalTasks" : 2,
    "runningTasks" : 0,
    "completedTasks" : 2,
    "totalDrivers" : 2,
    "queuedDrivers" : 0,
    "runningDrivers" : 0,
    "completedDrivers" : 2,
    "totalMemoryReservation" : "0B",
    "totalScheduledTime" : "5.84ms",
    "totalCpuTime" : "710.49us",
    "totalUserTime" : "0.00ns",
    "totalBlockedTime" : "27.38ms",
    "rawInputDataSize" : "27B",
    "rawInputPositions" : 1,
    "processedInputDataSize" : "32B",
    "processedInputPositions" : 1,
    "outputDataSize" : "32B",
    "outputPositions" : 1
  },
  "outputStage" : {
    "stageId" : "20131229_211533_00017_dk5x2.0",
    "state" : "FINISHED",
    "self" : "http://10.193.207.128:8080/v1/stage/20131229_211533_00017_dk5x2.0",
    "plan" : {
      "id" : "1",
      "root" : {
        "type" : "output",
        "id" : "5",
        "source" : {
          "type" : "exchange",
          "id" : "7",
          "sourceFragmentIds" : [ "0" ],
          "outputs" : [ "name" ]
        },
        "columns" : [ "name" ],
        "outputs" : [ "name" ]
      },
      "symbols" : {
        "name" : "VARCHAR"
      },
      "distribution" : "NONE",
      "outputPartitioning" : "NONE"
    },
    "tupleInfos" : [ [ "varchar" ] ],
    "stageStats" : {
      "getSplitDistribution" : {
        "maxError" : "NaN",
        "count" : 0.0,
        "total" : 0.0,
        "p01" : -9223372036854775808,
        "p05" : -9223372036854775808,
        "p10" : -9223372036854775808,
        "p25" : -9223372036854775808,
        "p50" : -9223372036854775808,
        "p75" : -9223372036854775808,
        "p90" : -9223372036854775808,
        "p95" : -9223372036854775808,
        "p99" : -9223372036854775808,
        "min" : 9223372036854775807,
        "max" : -9223372036854775808
      },
      "scheduleTaskDistribution" : {
        "maxError" : "NaN",
        "count" : 0.0,
        "total" : 0.0,
        "p01" : -9223372036854775808,
        "p05" : -9223372036854775808,
        "p10" : -9223372036854775808,
        "p25" : -9223372036854775808,
        "p50" : -9223372036854775808,
        "p75" : -9223372036854775808,
        "p90" : -9223372036854775808,
        "p95" : -9223372036854775808,
        "p99" : -9223372036854775808,
        "min" : 9223372036854775807,
        "max" : -9223372036854775808
      },
      "addSplitDistribution" : {
        "maxError" : "NaN",
        "count" : 0.0,
        "total" : 0.0,
        "p01" : -9223372036854775808,
        "p05" : -9223372036854775808,
        "p10" : -9223372036854775808,
        "p25" : -9223372036854775808,
        "p50" : -9223372036854775808,
        "p75" : -9223372036854775808,
        "p90" : -9223372036854775808,
        "p95" : -9223372036854775808,
        "p99" : -9223372036854775808,
        "min" : 9223372036854775807,
        "max" : -9223372036854775808
      },
      "totalTasks" : 1,
      "runningTasks" : 0,
      "completedTasks" : 1,
      "totalDrivers" : 1,
      "queuedDrivers" : 0,
      "runningDrivers" : 0,
      "completedDrivers" : 1,
      "totalMemoryReservation" : "0B",
      "totalScheduledTime" : "300.00us",
      "totalCpuTime" : "290.12us",
      "totalUserTime" : "0.00ns",
      "totalBlockedTime" : "27.38ms",
      "rawInputDataSize" : "32B",
      "rawInputPositions" : 1,
      "processedInputDataSize" : "32B",
      "processedInputPositions" : 1,
      "outputDataSize" : "32B",
      "outputPositions" : 1
    },
    "tasks" : [ {
      "taskId" : "20131229_211533_00017_dk5x2.0.0",
      "version" : 4,
      "state" : "FINISHED",
      "self" : "http://10.193.207.128:8080/v1/task/20131229_211533_00017_dk5x2.0.0",
      "lastHeartbeat" : "2013-12-29T21:17:32.143Z",
      "outputBuffers" : {
        "state" : "FINISHED",
        "masterSequenceId" : 0,
        "pagesAdded" : 1,
        "buffers" : [ {
          "bufferId" : "out",
          "finished" : true,
          "bufferedPages" : 0,
          "pagesSent" : 1
        } ]
      },
      "noMoreSplits" : [ "7" ],
      "stats" : {
        "createTime" : "2013-12-29T21:17:32.092Z",
        "startTime" : "2013-12-29T21:17:32.092Z",
        "endTime" : "2013-12-29T21:17:32.143Z",
        "elapsedTime" : "51.19ms",
        "queuedTime" : "848.00us",
        "totalDrivers" : 1,
        "queuedDrivers" : 0,
        "runningDrivers" : 0,
        "completedDrivers" : 1,
        "memoryReservation" : "0B",
        "totalScheduledTime" : "300.00us",
        "totalCpuTime" : "290.12us",
        "totalUserTime" : "0.00ns",
        "totalBlockedTime" : "27.38ms",
        "rawInputDataSize" : "32B",
        "rawInputPositions" : 1,
        "processedInputDataSize" : "32B",
        "processedInputPositions" : 1,
        "outputDataSize" : "32B",
        "outputPositions" : 1,
        "pipelines" : [ {
          "inputPipeline" : true,
          "outputPipeline" : true,
          "totalDrivers" : 1,
          "queuedDrivers" : 0,
          "runningDrivers" : 0,
          "completedDrivers" : 1,
          "memoryReservation" : "0B",
          "queuedTime" : {
            "maxError" : 0.0,
            "count" : 1.0,
            "total" : 564000.0,
            "p01" : 564000,
            "p05" : 564000,
            "p10" : 564000,
            "p25" : 564000,
            "p50" : 564000,
            "p75" : 564000,
            "p90" : 564000,
            "p95" : 564000,
            "p99" : 564000,
            "min" : 564000,
            "max" : 564000
          },
          "elapsedTime" : {
            "maxError" : 0.0,
            "count" : 1.0,
            "total" : 2.9186E7,
            "p01" : 29186000,
            "p05" : 29186000,
            "p10" : 29186000,
            "p25" : 29186000,
            "p50" : 29186000,
            "p75" : 29186000,
            "p90" : 29186000,
            "p95" : 29186000,
            "p99" : 29186000,
            "min" : 29186000,
            "max" : 29186000
          },
          "totalScheduledTime" : "300.00us",
          "totalCpuTime" : "290.12us",
          "totalUserTime" : "0.00ns",
          "totalBlockedTime" : "27.38ms",
          "rawInputDataSize" : "32B",
          "rawInputPositions" : 1,
          "processedInputDataSize" : "32B",
          "processedInputPositions" : 1,
          "outputDataSize" : "32B",
          "outputPositions" : 1,
          "operatorSummaries" : [ {
            "operatorId" : 0,
            "operatorType" : "ExchangeOperator",
            "addInputCalls" : 0,
            "addInputWall" : "0.00ns",
            "addInputCpu" : "0.00ns",
            "addInputUser" : "0.00ns",
            "inputDataSize" : "32B",
            "inputPositions" : 1,
            "getOutputCalls" : 1,
            "getOutputWall" : "153.00us",
            "getOutputCpu" : "147.09us",
            "getOutputUser" : "0.00ns",
            "outputDataSize" : "32B",
            "outputPositions" : 1,
            "blockedWall" : "27.38ms",
            "finishCalls" : 0,
            "finishWall" : "0.00ns",
            "finishCpu" : "0.00ns",
            "finishUser" : "0.00ns",
            "memoryReservation" : "0B",
            "info" : {
              "bufferedBytes" : 0,
              "averageBytesPerRequest" : 32,
              "bufferedPages" : 0,
              "pageBufferClientStatuses" : [ {
                "uri" : "http://10.193.207.128:8080/v1/task/20131229_211533_00017_dk5x2.1.0/results/ab68e201-3878-4b21-b6b9-f6658ddc408b",
                "state" : "closed",
                "lastUpdate" : "2013-12-29T16:17:32.121-05:00",
                "pagesReceived" : 1,
                "requestsScheduled" : 3,
                "requestsCompleted" : 3,
                "httpRequestState" : "queued"
              } ]
            }
          }, {
            "operatorId" : 1,
            "operatorType" : "TaskOutputOperator",
            "addInputCalls" : 1,
            "addInputWall" : "54.00us",
            "addInputCpu" : "54.20us",
            "addInputUser" : "0.00ns",
            "inputDataSize" : "32B",
            "inputPositions" : 1,
            "getOutputCalls" : 0,
            "getOutputWall" : "0.00ns",
            "getOutputCpu" : "0.00ns",
            "getOutputUser" : "0.00ns",
            "outputDataSize" : "32B",
            "outputPositions" : 1,
            "blockedWall" : "0.00ns",
            "finishCalls" : 1,
            "finishWall" : "93.00us",
            "finishCpu" : "88.83us",
            "finishUser" : "0.00ns",
            "memoryReservation" : "0B"
          } ],
          "drivers" : [ ]
        } ]
      },
      "failures" : [ ],
      "outputs" : { }
    } ],
    "subStages" : [ {
      "stageId" : "20131229_211533_00017_dk5x2.1",
      "state" : "FINISHED",
      "self" : "http://10.193.207.128:8080/v1/stage/20131229_211533_00017_dk5x2.1",
      "plan" : {
        "id" : "0",
        "root" : {
          "type" : "sink",
          "id" : "6",
          "source" : {
            "type" : "project",
            "id" : "1",
            "source" : {
              "type" : "tablescan",
              "id" : "0",
              "table" : {
                "type" : "jmx",
                "connectorId" : "jmx",
                "objectName" : "java.lang:type=Runtime",
                "columns" : [ {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "node",
                  "columnType" : "STRING",
                  "ordinalPosition" : 0
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "InputArguments",
                  "columnType" : "STRING",
                  "ordinalPosition" : 1
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "ManagementSpecVersion",
                  "columnType" : "STRING",
                  "ordinalPosition" : 2
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "SpecName",
                  "columnType" : "STRING",
                  "ordinalPosition" : 3
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "SpecVendor",
                  "columnType" : "STRING",
                  "ordinalPosition" : 4
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "SpecVersion",
                  "columnType" : "STRING",
                  "ordinalPosition" : 5
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "Uptime",
                  "columnType" : "LONG",
                  "ordinalPosition" : 6
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "StartTime",
                  "columnType" : "LONG",
                  "ordinalPosition" : 7
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "Name",
                  "columnType" : "STRING",
                  "ordinalPosition" : 8
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "ClassPath",
                  "columnType" : "STRING",
                  "ordinalPosition" : 9
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "SystemProperties",
                  "columnType" : "STRING",
                  "ordinalPosition" : 10
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "BootClassPath",
                  "columnType" : "STRING",
                  "ordinalPosition" : 11
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "LibraryPath",
                  "columnType" : "STRING",
                  "ordinalPosition" : 12
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "VmName",
                  "columnType" : "STRING",
                  "ordinalPosition" : 13
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "VmVendor",
                  "columnType" : "STRING",
                  "ordinalPosition" : 14
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "VmVersion",
                  "columnType" : "STRING",
                  "ordinalPosition" : 15
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "BootClassPathSupported",
                  "columnType" : "BOOLEAN",
                  "ordinalPosition" : 16
                }, {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "ObjectName",
                  "columnType" : "STRING",
                  "ordinalPosition" : 17
                } ]
              },
              "outputSymbols" : [ "name" ],
              "assignments" : {
                "name" : {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "columnName" : "Name",
                  "columnType" : "STRING",
                  "ordinalPosition" : 8
                }
              },
              "originalConstraint" : "true",
              "partitionDomainSummary" : "TupleDomain:ALL"
            },
            "assignments" : {
              "name" : "\"name\""
            }
          },
          "outputSymbols" : [ "name" ]
        },
        "symbols" : {
          "name" : "VARCHAR"
        },
        "distribution" : "SOURCE",
        "partitionedSource" : "0",
        "outputPartitioning" : "NONE"
      },
      "tupleInfos" : [ [ "varchar" ] ],
      "stageStats" : {
        "getSplitDistribution" : {
          "maxError" : 0.0,
          "count" : 1.0,
          "total" : 4000.0,
          "p01" : 4000,
          "p05" : 4000,
          "p10" : 4000,
          "p25" : 4000,
          "p50" : 4000,
          "p75" : 4000,
          "p90" : 4000,
          "p95" : 4000,
          "p99" : 4000,
          "min" : 4000,
          "max" : 4000
        },
        "scheduleTaskDistribution" : {
          "maxError" : 0.0,
          "count" : 1.0,
          "total" : 3178000.0,
          "p01" : 3178000,
          "p05" : 3178000,
          "p10" : 3178000,
          "p25" : 3178000,
          "p50" : 3178000,
          "p75" : 3178000,
          "p90" : 3178000,
          "p95" : 3178000,
          "p99" : 3178000,
          "min" : 3178000,
          "max" : 3178000
        },
        "addSplitDistribution" : {
          "maxError" : "NaN",
          "count" : 0.0,
          "total" : 0.0,
          "p01" : -9223372036854775808,
          "p05" : -9223372036854775808,
          "p10" : -9223372036854775808,
          "p25" : -9223372036854775808,
          "p50" : -9223372036854775808,
          "p75" : -9223372036854775808,
          "p90" : -9223372036854775808,
          "p95" : -9223372036854775808,
          "p99" : -9223372036854775808,
          "min" : 9223372036854775807,
          "max" : -9223372036854775808
        },
        "totalTasks" : 1,
        "runningTasks" : 0,
        "completedTasks" : 1,
        "totalDrivers" : 1,
        "queuedDrivers" : 0,
        "runningDrivers" : 0,
        "completedDrivers" : 1,
        "totalMemoryReservation" : "0B",
        "totalScheduledTime" : "5.54ms",
        "totalCpuTime" : "420.37us",
        "totalUserTime" : "0.00ns",
        "totalBlockedTime" : "0.00ns",
        "rawInputDataSize" : "27B",
        "rawInputPositions" : 1,
        "processedInputDataSize" : "32B",
        "processedInputPositions" : 1,
        "outputDataSize" : "32B",
        "outputPositions" : 1
      },
      "tasks" : [ {
        "taskId" : "20131229_211533_00017_dk5x2.1.0",
        "version" : 5,
        "state" : "FINISHED",
        "self" : "http://10.193.207.128:8080/v1/task/20131229_211533_00017_dk5x2.1.0",
        "lastHeartbeat" : "2013-12-29T21:17:32.143Z",
        "outputBuffers" : {
          "state" : "FINISHED",
          "masterSequenceId" : 0,
          "pagesAdded" : 1,
          "buffers" : [ {
            "bufferId" : "ab68e201-3878-4b21-b6b9-f6658ddc408b",
            "finished" : true,
            "bufferedPages" : 0,
            "pagesSent" : 1
          } ]
        },
        "noMoreSplits" : [ "0" ],
        "stats" : {
          "createTime" : "2013-12-29T21:17:32.041Z",
          "startTime" : "2013-12-29T21:17:32.081Z",
          "endTime" : "2013-12-29T21:17:32.143Z",
          "elapsedTime" : "101.49ms",
          "queuedTime" : "40.35ms",
          "totalDrivers" : 1,
          "queuedDrivers" : 0,
          "runningDrivers" : 0,
          "completedDrivers" : 1,
          "memoryReservation" : "0B",
          "totalScheduledTime" : "5.54ms",
          "totalCpuTime" : "420.37us",
          "totalUserTime" : "0.00ns",
          "totalBlockedTime" : "0.00ns",
          "rawInputDataSize" : "27B",
          "rawInputPositions" : 1,
          "processedInputDataSize" : "32B",
          "processedInputPositions" : 1,
          "outputDataSize" : "32B",
          "outputPositions" : 1,
          "pipelines" : [ {
            "inputPipeline" : true,
            "outputPipeline" : true,
            "totalDrivers" : 1,
            "queuedDrivers" : 0,
            "runningDrivers" : 0,
            "completedDrivers" : 1,
            "memoryReservation" : "0B",
            "queuedTime" : {
              "maxError" : 0.0,
              "count" : 1.0,
              "total" : 4.0087E7,
              "p01" : 40087000,
              "p05" : 40087000,
              "p10" : 40087000,
              "p25" : 40087000,
              "p50" : 40087000,
              "p75" : 40087000,
              "p90" : 40087000,
              "p95" : 40087000,
              "p99" : 40087000,
              "min" : 40087000,
              "max" : 40087000
            },
            "elapsedTime" : {
              "maxError" : 0.0,
              "count" : 1.0,
              "total" : 4.5992E7,
              "p01" : 45992000,
              "p05" : 45992000,
              "p10" : 45992000,
              "p25" : 45992000,
              "p50" : 45992000,
              "p75" : 45992000,
              "p90" : 45992000,
              "p95" : 45992000,
              "p99" : 45992000,
              "min" : 45992000,
              "max" : 45992000
            },
            "totalScheduledTime" : "5.54ms",
            "totalCpuTime" : "420.37us",
            "totalUserTime" : "0.00ns",
            "totalBlockedTime" : "0.00ns",
            "rawInputDataSize" : "27B",
            "rawInputPositions" : 1,
            "processedInputDataSize" : "32B",
            "processedInputPositions" : 1,
            "outputDataSize" : "32B",
            "outputPositions" : 1,
            "operatorSummaries" : [ {
              "operatorId" : 0,
              "operatorType" : "ScanFilterAndProjectOperator_3",
              "addInputCalls" : 0,
              "addInputWall" : "0.00ns",
              "addInputCpu" : "0.00ns",
              "addInputUser" : "0.00ns",
              "inputDataSize" : "27B",
              "inputPositions" : 1,
              "getOutputCalls" : 2,
              "getOutputWall" : "288.00us",
              "getOutputCpu" : "280.09us",
              "getOutputUser" : "0.00ns",
              "outputDataSize" : "32B",
              "outputPositions" : 1,
              "blockedWall" : "0.00ns",
              "finishCalls" : 0,
              "finishWall" : "0.00ns",
              "finishCpu" : "0.00ns",
              "finishUser" : "0.00ns",
              "memoryReservation" : "0B",
              "info" : {
                "type" : "jmx",
                "tableHandle" : {
                  "type" : "jmx",
                  "connectorId" : "jmx",
                  "objectName" : "java.lang:type=Runtime",
                  "columns" : [ {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "node",
                    "columnType" : "STRING",
                    "ordinalPosition" : 0
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "InputArguments",
                    "columnType" : "STRING",
                    "ordinalPosition" : 1
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "ManagementSpecVersion",
                    "columnType" : "STRING",
                    "ordinalPosition" : 2
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "SpecName",
                    "columnType" : "STRING",
                    "ordinalPosition" : 3
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "SpecVendor",
                    "columnType" : "STRING",
                    "ordinalPosition" : 4
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "SpecVersion",
                    "columnType" : "STRING",
                    "ordinalPosition" : 5
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "Uptime",
                    "columnType" : "LONG",
                    "ordinalPosition" : 6
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "StartTime",
                    "columnType" : "LONG",
                    "ordinalPosition" : 7
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "Name",
                    "columnType" : "STRING",
                    "ordinalPosition" : 8
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "ClassPath",
                    "columnType" : "STRING",
                    "ordinalPosition" : 9
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "SystemProperties",
                    "columnType" : "STRING",
                    "ordinalPosition" : 10
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "BootClassPath",
                    "columnType" : "STRING",
                    "ordinalPosition" : 11
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "LibraryPath",
                    "columnType" : "STRING",
                    "ordinalPosition" : 12
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "VmName",
                    "columnType" : "STRING",
                    "ordinalPosition" : 13
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "VmVendor",
                    "columnType" : "STRING",
                    "ordinalPosition" : 14
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "VmVersion",
                    "columnType" : "STRING",
                    "ordinalPosition" : 15
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "BootClassPathSupported",
                    "columnType" : "BOOLEAN",
                    "ordinalPosition" : 16
                  }, {
                    "type" : "jmx",
                    "connectorId" : "jmx",
                    "columnName" : "ObjectName",
                    "columnType" : "STRING",
                    "ordinalPosition" : 17
                  } ]
                },
                "addresses" : [ "10.193.207.128:8080" ]
              }
            }, {
              "operatorId" : 1,
              "operatorType" : "TaskOutputOperator",
              "addInputCalls" : 1,
              "addInputWall" : "3.88ms",
              "addInputCpu" : "82.26us",
              "addInputUser" : "0.00ns",
              "inputDataSize" : "32B",
              "inputPositions" : 1,
              "getOutputCalls" : 0,
              "getOutputWall" : "0.00ns",
              "getOutputCpu" : "0.00ns",
              "getOutputUser" : "0.00ns",
              "outputDataSize" : "32B",
              "outputPositions" : 1,
              "blockedWall" : "0.00ns",
              "finishCalls" : 1,
              "finishWall" : "1.37ms",
              "finishCpu" : "58.01us",
              "finishUser" : "0.00ns",
              "memoryReservation" : "0B"
            } ],
            "drivers" : [ ]
          } ]
        },
        "failures" : [ ],
        "outputs" : { }
      } ],
      "subStages" : [ ],
      "failures" : [ ]
    } ],
    "failures" : [ ]
  },
  "inputs" : [ {
    "connectorId" : "jmx",
    "schema" : "jmx",
    "table" : "java.lang:type=Runtime",
    "columns" : [ "name" ]
  } ]
}


