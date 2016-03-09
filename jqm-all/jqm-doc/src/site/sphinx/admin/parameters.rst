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

A second file exists, named JQM_ROOT/conf/jqm.properties. It is not currently used, except if you are using the (not
production grade) database HSQLDB, in which case the line it contains must be uncommented. It can be - and should be - safely deleted otherwise.

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
| mavenRepo               | A Maven repository to use for dependency resolution                                                 | Maven Central | No      | At least one |
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

Here, nullable means the parameter can be absent from the table.

Parameter name is case-sensitive.

.. note:: the mavenRepo is the only parameter that can be specified multiple times. There must be at least one repository specified.
	If using Maven central, please specify 'http://repo1.maven.org/maven2/' and not one the numerous other aliases that exist.
	Maven Central is only used if explicitly specified (which is the default).

Also, as a side note, mail notifications use the JNDI resource named mail/default, which is created on node startup if it does not exist.
See resource documentation to set it up.
