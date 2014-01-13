==============
Query Resource
==============

The Query REST service is the most complex of the rest services.  It
contains detailed information about nodes, slices, shards, and other
details that capture the state and history of a query being executed
on a Presto installation.

.. http:get:: /v1/query


   This service returns information and statistics about queries that
   are currently being executed on a Presto coordinator.


.. http:get:: /v1/query/{queryId}

   If you are looking to gather very detailed statistics about a
   query, this is the service you would call. If you load the web
   interface of a Presto coordinator you will see a list of current
   queries.  Click on a query will reveal a link to this service.  If
   you click on this service you should be prepared to see details
   that will cover all of the stages, tasks, and operators that are
   involved in a Presto query.

   **Example response**:

      .. sourcecode:: http

         {
  	    "queryId" : "20131229_211533_00017_dk5x2",
  	    "session" : {
    	       "user" : "tobrien",
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
  	    "outputStage" : ...
         }

