================
Execute Resource
================

.. http:post:: /v1/execute
   
   Executes a query.       

   :query query: SQL Query to execute
   :reqheader user: User to execute statement on behalf of (optional)
   :reqheader source: Source of query
   :reqheader catalog: Catalog to execute query against
   :reqheader schema: Schema to execute query against



