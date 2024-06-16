Parameters
##########

Engine parameters
*****************

These parameters gouvern the behaviour of the JQM engines.

There are three sets of engine parameters:

* node parameters, for parameters that are specific to a single engine (for example, the TCP ports to use).
  These are stored inside the database.
* global parameters, for parameters concerning all engines (for example, a list of Nexus repositories).
  These are stored inside the database.
* bootstrap parameters: as all the previous elements are stored inside the database, an engine needs a minimal set
  of parameters to access the database and start.

.. note:: also of interest in regard to engine configuration is the :doc:`queues configuration<queues>`.

Bootstrap
+++++++++

This is a file named JQM_ROOT/conf/resource.xml. It contains the definition of the connection pool that is used by
JQM to access its own database. See :doc:`resources` for more details on the different parameters - it is exactly the same
as a resource defined inside the JQM database, save it is inside a file read before trying to connect to the JQM database.

Actually, resources.xml can contain any resource, not just the connection to the JQM database. However, it is not
recommended - the resource would only be available to the local node, while resources defined in the database are
available to any node.

A second file exists, named JQM_ROOT/conf/jqm.properties. It is used specifically for database-specific bootstrap parameters, and is only useful in very specific use cases. It can be - and should be - safely deleted otherwise. The parameters are:

* com.enioka.jqm.jdbc.tablePrefix: a prefix to add to all table names (if value is "MARSU\_", tables will be named MARSU_HISTORY, MARSU_NODE...). Default is empty.
* com.enioka.jqm.jdbc.datasource: JNDI name of the datasource from resource.xml to use as the main JQM database connection. Default is jdbc/jqm.

Finally, it is possible to change the JQM listening interface (for web services and GUI) by adding `-D"com.enioka.jqm.interface=0.0.0.0"` (or any other value) inside the `JAVA_OPTS` environment variable. If not set, the normal interface choice is done (by choosing all interfaces corresponding to the node's DNS parameter). This is mostly useful in container deployments - inside the official images, this parameter is always set to 0.0.0.0.

**Changes to bootstrap files require an engine restart**.

Node parameters
+++++++++++++++

These parameters are set inside the JQM database table named NODE. They
have to be altered through the GUI, through the CLI options or directly inside the database with your tool of choice.

+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| Name            | Description                                                                        | Default               | Nullable | Restart |
+=================+====================================================================================+=======================+==========+=========+
| DNS             | The interface name on which JQM will listen for its network-related functions      | first hostname        | No       | No      |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| PORT            | Port for the basic servlet API                                                     | Random free           | No       | No      |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| dlRepo          | Storage directory for files created by payloads                                    | JQM_ROOT\\outputfiles | No       | Yes     |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| REPO            | Storage directory for all payloads jars and libs                                   | JQM_ROOT\\jobs        | No       | Yes     |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| ROOTLOGLEVEL    | The log level for this engine (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)             | INFO                  | No       | Yes     |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| EXPORTREPO      | Not used                                                                           |                       |          |         |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| JMXREGISTRYPORT | TCP port on which the JMX registry will listen. Remote JMX disabled if NULL or <1. | NULL                  | Yes      | Yes     |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+
| JMXSERVERPORT   | Same with server port                                                              | NULL                  | Yes      | Yes     |
+-----------------+------------------------------------------------------------------------------------+-----------------------+----------+---------+

('restart' means: restarting the engine in question is needed to take the new value into account)

Global parameters
+++++++++++++++++

These parameters are set inside the JQM database table named GLOBALPARAMETER. There is no CLI to modify these, therefore they
have to be altered directly inside the database with your tool of choice or through the GUI.

+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| Name                    | Description                                                                                         | Default       | Restart | Nullable     |
+=========================+=====================================================================================================+===============+=========+==============+
| mavenRepo               | A comma-separated list of Maven repositories to use for dependency resolution                       | Maven Central | No      | No           |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| mavenSettingsCL         | an alternate Maven settings.xml to use. If absent, the usual file inside ~/.m2 is used.             | NULL          | No      | Yes          |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| defaultConnection       | the JNDI alias returned by the engine API getDefaultConnection method.                              | jdbc/jqm      | No      | No           |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| logFilePerLaunch        | if 'true', one log file will be created per launch. If 'false', job stdout/stderr is lost.          | true          | Yes     | No           |
|                         | if 'both', one log file will be created per launch PLUS one common file concatening all these files |               |         |              |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| internalPollingPeriodMs | Period in ms for checking stop orders. Also period at which the "I'm a alive" signal is sent.       | 60000         | Yes     | No           |
|                         | Also used for checking and applying  parameter modifications (new queues, global prm changes...)    |               |         |              |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| disableWsApi            | Disable all HTTP interfaces on all nodes. This takes precedence over node per node settings.        | false         | No      | Yes          |
|                         | Absent means false, i.e. not forbidden.                                                             |               |         |              |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| enableWsApiSsl          | All HTTP communications will be HTTPS and not HTTP.                                                 | false         | No      | No           |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| enableWsApiAuth         | Use HTTP basic authentication plus RBAC backend for all WS APIs                                     | true          | No      | No           |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| disableWsApiSimple      | Forbids the simple API from loading on any node. This takes precedence over node per node settings. | NULL          | Yes     | Yes          |
|                         | Absent means false, i.e. not forbidden.                                                             |               |         |              |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| disableWsApiClient      | Forbids the client API from loading on any node. This takes precedence over node per node settings. | NULL          | Yes     | Yes          |
|                         | Absent means false, i.e. not forbidden.                                                             |               |         |              |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| disableWsApiAdmin       | Forbids the admin API from loading on any node. This takes precedence over node per node settings.  | NULL          | Yes     | Yes          |
|                         | Absent means false, i.e. not forbidden.                                                             |               |         |              |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| enableInternalPki       | Use the internal (database-backed) PKI for issuing certificates and trusting presented certificates | true          | No      | No           |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| pfxPassword             | Password of the private key file (if not using internal PKI).                                       | SuperPassword | No      | Yes          |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+
| deleteStoppedNodes      | If true, stopped nodes are removed from configuration. Useful when nodes are transient, like in an  | false         | Yes     | Yes          |
|                         | orchestrator as Kubernetes.                                                                         |               |         |              |
+-------------------------+-----------------------------------------------------------------------------------------------------+---------------+---------+--------------+

Here, nullable means the parameter can be absent from the table. New values are taken into account asynchronously by running engines.

Parameter name is case-sensitive.

.. note:: There must be at least one Maven repository specified.
	If using Maven central, please specify 'http://repo1.maven.org/maven2/' and not one the numerous other aliases that exist.
	Maven Central is only used if explicitly specified (which is the default).

.. note:: Some parameters about web service or web interface do not require node reboot. However, as they actually change how the
    services are exposed (HTTP to HTTPS, certificate root...) they will trigger a very short service interruption of the web
    services. This should be taken into account especially by script writers.

Also, as a side note, mail notifications use the JNDI resource named mail/default, which is created on node startup if it does not exist.
See resource documentation to set it up.
