===========================
Presto Command-line Options
===========================

.. program:: presto

.. option:: --server 

   Presto server location. Defaults to "localhost:8080".

   Examples of valid values for the server option include:

   ``localhost``

      This value will be expanded to "http://localhost" assuming a
      default HTTP port of 80.

   ``server.example.com``

      This value will be expanded to "http://server.example.com"
      assuming a default HTTP port of 80.

   ``server.example.com:8092``

      This value will be expanded to "http://server.example.com:8092".

   ``https://server.example.com``

      If you are connecting to Presto over HTTPS the scheme must be
      specified in the server parameter

.. option:: --user

   Username to use for session. Defaults to the value of
   ``System.getProperty("user.name")``. The Presto client sends the
   query to the Presto server with a User HTTP header which can be
   used to keep track of which user is executing queries on a Presto
   server.

.. option:: --source

   Name of source making query. Defaults to "presto-cli". The Presto
   client sends the query to a Presto Server it sends a HTTP request
   header named "Source". This option sets the value the command-line
   interface will send to a Presto server. If you are querying Presto
   from several clients setting a unique source value can help you
   debug which client is associated with activity on a Presto server.

.. option:: --catalog

   Default catalog to execute statements against. Defaults to "default"

.. option:: --schema 

   Default schema to execute statements against. Defaults to "default"

.. option:: --debug

   Enables debug information.

.. option:: --execute

   Execute specified statement and exit.

   Note that only one of either "execute" of "file" options can be
   specified for the Presto client. If both are specified, the Presto
   client will return an error.

.. option:: -f filename, --file filename

   Executes statements from a file and exits. 

   Note that only one of either "execute" or "file" options can be
   specified for the Presto client. If both are specified, the Presto
   client will return an error.

.. option:: --output-format

   Output format for batch mode. Defaults to "CSV".

   Possible values include the following list:

   * ALIGN - Aligned tuples
   * VERTICAL - Vertical tuples
   * CSV - Comma-separated values
   * CSV_HEADER - Command-separated values with a header
   * TSV - Tab-separated values
   * TSV_HEADER - Tab-separated values with header


