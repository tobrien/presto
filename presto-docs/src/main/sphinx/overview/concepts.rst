===============
Presto Concepts
===============

To understand Presto you first have to understand the terms used
through the Presto documentation.  This section was written to provide
a solid definition for the core concepts referenced throughout Presto.

-------------
Statement
-------------

Presto executes ANSI-compatible SQL statements.  When Presto document
refers to a statement we are refering to statement as defined in the
ANSI SQL standard which consists of clauses, expressions, and
predicates.

-------------
Query
-------------

When Presto parses a statement it converts it into a query and creates
a distributed query plan which is then realized as a series of
interconnected stages running on Presto Workers. 

-------------
Coordinator
-------------

The Presto coordinator is responsible for parsing statements, plannign
queries, and managing Presto work nodes.  It is the intelligence in a
Presto installation and it also the node to which a client connects to
submit statemetns for execution.

-------------
Worker
-------------

A Presto Worker is a node in a Presto installation which is
responsible for executing a stage and processing splits. Workers nodes
consume data and produce results for either other workers involved in
the same query or a coordinator acting as a go-between for a Presto
client.

-------------
Catalog
-------------
	
A Presto Catalog is related to a connector to a specific type of data
source.  For example, the JMX catalog is a built-in catalog in Presto
which provides access to JMX information via a JMX connector.  When
you run a SQL statement in Presto, you are running it against a
catalog.  Other examples of catalogs include the hive catalog to
connect to a Hive data source.

Catalogs are defined in properties files stored in the Presto
configuration directory, and they correspond to technologies.

-------------
Connector
-------------

A catalog is associated with a connector.  You can think of a
connector the same way you would think of a driver. It is an
implementation of Presto's SPI which let's Presto interact with a
resource using a well-defined API. Presto contains several built-in
connectors including a connector for JMX, a "system" connector which
provides access to built-in system tables, and a "dual" connector
which is used as a placeholder.

If you examine a catalog configuration file, you will see that each
contains a mandatory property "connector.name" which is used by the
Catalog manager to create a connector for a given catalog.

-------------
Schema
-------------

A Catalog and Schema together define a set of tables that can be
queried.  When accessing Hive or a relational database with Presto, a
schema refers to the same concept in the target database.  When
accessing a catalog such as JMX, schema simply refers to a set of
tables used to represent JMX information and does not directly
correspond to a similar concept in the underlying technology.

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

-------------
Task
-------------

.. TBD

-------------
Operator
-------------

.. TBD


-------------
Driver
-------------

A Driver is a low-level object responsible for driving input
to a stage.  Think of a driver as a component that is keeping an eye
on a task in a Stage. Once this tasks needs more input, it is the
driver's responsibility to drive input (in the form of splits) to
operators in a task.

-------------
Split
-------------

Stages operate on splits, and splits are portions of a larger data
set. Take the following as an example, if you are attempting to
aggregate several billion rows from Hive, Presto will create a
hierarchy of stages and begin to disribute chunks of data to each
stage.  These chunks of data are known as splits and each stage is
responsible for retrieving one or more splits from an underlying data
source.

