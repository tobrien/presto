***************
Presto REST API
***************

This chapter defines the Presto REST API.  Presto uses REST for all
communication within a Presto installation.  JSON-based REST services
facilitate communication between the client and the Presto coordinator
as well as for communicate between a Presto coordinator and multiple
Presto workers.  In this chapter you will find detailed descriptions
of the APIs offered by Presto as well as example requests and
responses.

.. toctree::
    :maxdepth: 1

    rest/execute
    rest/node
    rest/query
    rest/shard
    rest/stage
    rest/statement
    rest/task

REST API Overview
-----------------

Just about everything in Presto is exposed as a REST API. 


Execute Resource

    The execute resource is what the client sends queries to.  It is available at the path ``/v1/execute``, accepts a query as a POST and returns JSON.

Query Resource

    The query resource takes a SQL query.  It is available at the path ``/v1/query`` and accepts several HTTP methods.

Node Resource

    The node resource returns information about worker nodes in a Presto installation.  It is available at the path ``/v1/node``.

Shard Resource

    The shard resource TBD.

Stage Resource

    The stage resource TBD

Statement Resource

    The statement resource TBD

Task Resource

    The task resource TBD