===============
Presto Concepts
===============

To understand Presto you must first understand the terms and concepts
used throughout the Presto documentation. Presto is misleading. While
it uses the language of a database - "queries" and "statements" - it
also brings with it an entirely different model for executing these
queries across a distributed network of Presto coordinators and Presto
workers.

While it's easy to understand statements and queries, as an end-user
you should have familiarity with concepts such as stages and shards to
take full advantage of Presto to execute efficient queries.  As a
Presto administrator or a Presto contributor you should understand how
Presto's concepts of stages map to tasks and how tasks contain a set
of drivers which process data.

This section provides a solid definition for the core concepts
referenced throughout Presto, and these sections are sorted from most
general to most specific.

-------------
Statement
-------------

Presto executes ANSI-compatible SQL statements.  When Presto
documentation refers to a statement we are refering to statements as
defined in the ANSI SQL standard which consists of clauses,
expressions, and predicates.

Some readers might be curious why this section lists seperate concepts
for statements and queries. This is necessary because, in Presto,
statements simply refer to the textual representation of a SQL
statement. When a statement is executed, Presto creates a query along
with a query plan that is then distributed across a series of Presto
workers.

-------------
Query
-------------

When Presto parses a statement it converts it into a query and creates
a distributed query plan which is then realized as a series of
interconnected stages running on Presto Workers. When you retrieve
information about a query in Presto, you receive a snapshot of every
component that is involved in producing a result set in response to a
statement.

The difference between a statement and a query is simple. A statement
can be thought of as the string that is passed to Presto while a query
refers to the configuration and components instantiated to execute
that statement. A query encompasses stages, tasks, splits, catalogs,
and other components and data sources working in concert to produce a
result.

-------------
Coordinator
-------------

The Presto coordinator is a server that is responsible for parsing
statements, planning queries, and managing Presto worker nodes.  It is
the "brains" of a Presto installation and it also the node to which a
client connects to submit statements for execution.

Every Presto installation must have at least one Presto coordinator
alongside zero or more Presto workers. The coordinator keeps track of
the activity on each worker and coordinates the delivery of data to
the components created to execute a query. Coordinators keep track of
a logical model of a query involving a series of stages which is then
translated into a series of connected tasks running on a cluster of
Presto workers.

Coordinators communicate with both workers and clients using a REST
API.

-------------
Worker
-------------

A Presto worker is a server in a Presto installation which is
responsible for executing tasks and processing data. Worker nodes
consume data and produce results for either other workers involved in
the same query or a coordinator acting as a go-between for a Presto
client.

A Presto installation can have anywhere from zero Presto workers to an
unlimited number of Presto workers available to coordinate. When a
Presto worker becomes available, it advertises itself using a
discovery server making itself available to a Presto coordinator for
task execution.

Workers communicate with both other workers and Presto coordinators
using a REST API.

-------------
Catalog
-------------
	
A Presto Catalog is related to a connector which connects Presto
specific type of data source.  For example, the JMX catalog is a
built-in catalog in Presto which provides access to JMX information
via a JMX connector.  When you run a SQL statement in Presto, you are
running it against a catalog.  Other examples of catalogs include the
hive catalog to connect to a Hive data source.

Catalogs are defined in properties files stored in the Presto
configuration directory, and they correspond to technologies.

When addressing a table in Presto, one should use the fully-qualified table name. This name is a combination of the catalog, the project, and an identifer.

PRE-NOTE: Explain Fully Qualified name - catalog.schema.table...

-------------
Connector
-------------

A connector adapts Presto to a source of data, and every catalog is
associated with a specific connector.  You can think of a connector
the same way you think of a driver for a database. It is an
implementation of Presto's SPI which allows Presto to interact with a
resource using a standard API.

Presto contains several built-in connectors including a connector for
JMX, a "system" connector which provides access to built-in system
tables, a "dual" connector which is used as a placeholder, a native
connector, and a connector designed to serve TPC-H benchmark
data. Many third-party developers have contributed connectors so that
Presto can access data in a variety of data sources.



If you examine a catalog configuration file, you will see that each
contains a mandatory property "connector.name" which is used by the
Catalog manager to create a connector for a given catalog. While there
is a one to one association with a catalog and a connector, it is
possible to have more than one catalog use the same connector to
access two different instances of a similar database. For example, if
you have two Hive clusters, you can configure two catalogs which use
the Hive connector to query data from either database.

-------------
Schema
-------------

A Catalog and Schema together define a set of tables that can be
queried.  When accessing Hive or a relational database with Presto, a
schema refers to the same concept in the target database.  When
accessing a catalog such as JMX, schema simply refers to a set of
tables used to represent JMX information and does not directly
correspond to a similar concept in the underlying technology.

Grouping of tables.

FUTURE: Bind a schema from one catalog to another.  Create a virtual catalog, binding things in.

-------------
Stage
-------------

When Presto executes a statement it does do by breaking up the
execution into a hierarchy of stages.  For example, if Presto needs to
aggregate data from one billion rows stored in Hive it does so by
creating a root stage to aggregate the output of several other stages
all of which are designed to operate on specific portions of that data
set.  What differentiates Presto from Hive is that this processing is
being done in parallel and intermediate output between stages is
stored in memory and retrieved from a parent stage.

As mentioned previously, every query in Presto is executed on a
hierarchy of stages which resembles a tree.  Every query has a "root"
stage which is responsible for aggregating the output from other
stages executing on a network of Presto workers.

Stages can be in number of different states captured in the following
list:

* Planned
* Scheduling
* Scheduled
* Running
* Finished
* Canceled
* Failed

Stages have input positions and output positions they contain tasks
which assemble operators together and drivers which drive input in the
form of splits to these operators.  If it helps to imagine it as such
a Stage can be thought of a preconfigure "machine" that accepts inputs
and then drives outputs to other stages higher in a tree of stages.
The top-most stage in this tree is responsible for aggregating the
results of other stages and delivering them to the client.

NOTE: Single execution plan for a stage.  One or more machines that execute that plan.

NOTE: Stage is a coordinator concept.   Tasks are the manifestation of the parallel stage.

-------------
Task
-------------



A Presto tasks has inputs and outputs and it contains a series of
operators.  Tasks are the "work horse" in the Presto architecture as a
distributed query plan is deconstructed into a series of tasks run on
distributed stages which assemble the operators that act upon splits.

TASKS are also executing things in parallel.

-------------
Operator
-------------

An Operator in Presto encapsulates the functionality of functions and
other operations which take data as input and generate data as output.
Operators execute within the context of a task, as a task is simply an
assembly if different operators which are then applied to individual
pieces of data within a split.

One of the most critical features of Presto that allows it process
data so quickly is that several operators have been implemented as JVM
bytecode

NOTE: A sequence of operator instances for a driver.  A driver is a physical set of operators in memory.  A driver is like the lowest level of parallelism.   A driver has one input and one output.    InputOperator, moves through the 

NOTE: Stages are connected together using an exchange.  On the producer side there is an output buffer.  On the receiver side there is an exchange client.


-------------
Driver
-------------

NOTES: Query has stages and a stage has tasks.  Distributed execution plan.  System spreads the work over a bunch of workers.  Work in concert to process query.  the task has a similar kind of plan, but its a parallel plan where its going to divide the work 


A Driver is a low-level object responsible for driving input
to a stage.  Think of a driver as a component that is keeping an eye
on a task in a Stage. Once this tasks needs more input, it is the
driver's responsibility to drive input (in the form of splits) to
operators in a task.

-------------
Split
-------------

Tasks operate on splits, and splits are portions of a larger data
set. Take the following as an example, if you are attempting to
aggregate several billion rows from Hive, Presto will create a
hierarchy of stages and begin to disribute chunks of data to each
stage.  These chunks of data are known as splits and each stage is
responsible for retrieving one or more splits from an underlying data
source.

NOTE: A split represents a chunk of data to be processed.   there are intermediate stages.  You read and filtuer data there's a stage above it that doesn't have splits.

The connector (MISSING) there's a part of the query scheduling where it the coordinator asks the ocnnector what are all the splits for this table.  And the system returns a stream of all of the splits.

The connector can have access to all of the machines that are available

For the bottom operators where you are reading data from the connectors.   Source operator - not exchange.  I want to go and fetch this.   Read this file in HDFS stating at this offset. 


Intermediate 



A stage has spits and then those spits are assigned to tasks.  To execute those splits a task has to be created on a machine.


Coordinator gets the splits and then it doles them out to tasks.  The coordinator decides this split is going to run on this machine.    the creation and assignment are two separate events  - splits to tasks.


NOTE: Chapter for Connectors

NOTE: Explain how to use the Cassandra connector