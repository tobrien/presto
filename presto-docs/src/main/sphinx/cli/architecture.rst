=======================================
Presto Command-line Client Architecture
=======================================

When you run a query against a Presto Server the CLI tool is
completing a number of steps and directly interacting with a Presto
coordinator to retrieve results from Presto workers.  This section
gives a brief overview of how the Presto command-line interface is
workign to execute queries and retrieve results from the Presto
Server.  It has been written to help both understand how your client
is interacting with the server and to help you troubleshoot any issues
with the tool.

How the CLI Interface Works
---------------------------

When you start the Presto CLI, you can run the tool in one of three
modes:

* You can start an interactive client session.

* You can execute a list of statements from a file.

* You can specific statements to be executed on the command-line.




