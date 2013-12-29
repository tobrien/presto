=============
Node Resource
=============

.. http:get:: /v1/node
   
   Returns a list of nodes known to a Presto Server.

   **Example response**:

      .. sourcecode:: http

         HTTP/1.1 200 OK
         Vary: Accept
         Content-Type: text/javascript

         [
	   {
       	     "uri":"http://10.209.57.156:8080",
	     "recentRequests":25.181940555111073,
	     "recentFailures":0.0,
	     "recentSuccesses":25.195472984170983,
	     "lastRequestTime":"2013-12-22T13:32:44.673-05:00",
	     "lastResponseTime":"2013-12-22T13:32:44.677-05:00",
	     "age":"14155.28ms",
	     "recentFailureRatio":0.0,
	     "recentFailuresByType":{}
	   }
	 ]  

      .. sourcecode:: http

         HTTP/1.1 200 OK
	 Vry: Accept
	 Content-Type: text/javascript

	 [
	   {
	     "uri":"http://10.209.57.156:8080",
	     "recentRequests":117.0358348572745,
	     "recentFailures":8.452831267323281,
	     "recentSuccesses":108.58300358995123,
	     "lastRequestTime":"2013-12-23T02:00:40.382-05:00",
	     "lastResponseTime":"2013-12-23T02:00:40.383-05:00",
	     "age":"44882391.57ms",
	     "recentFailureRatio":0.07222430016952953,
	     "recentFailuresByType":
	     {
	       "java.io.IOException":0.9048374180359595,
	       "java.net.SocketTimeoutException":6.021822867514955E-269,
	       "java.net.ConnectException":7.54799384928732
	     }
	   }
	 ]

.. http:get:: /v1/node/failed

   Returns a list of nodes that have failed the last heartbeat check.


      .. sourcecode: http

         [{"uri":"http://10.209.57.156:8080","recentRequests":5.826871111529161,"recentFailures":0.4208416882082422,"recentSuccesses":5.406029423320919,"lastRequestTime":"2013-12-23T02:00:40.382-05:00","lastResponseTime":"2013-12-23T02:00:40.383-05:00","age":"45063192.35ms","recentFailureRatio":0.07222430016952952,"recentFailuresByType":{"java.io.IOException":0.0450492023935578,"java.net.SocketTimeoutException":2.998089068041336E-270,"java.net.ConnectException":0.3757924858146843}}]