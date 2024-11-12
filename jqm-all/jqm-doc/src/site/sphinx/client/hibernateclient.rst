JDBC Client API
###################

**Client API** is the name of the API offered to the end users of JQM: it allows to interact with running jobs, offering operations
such as creating a new execution request, cancelling a request, viewing all currently running jobs, etc. Read :doc:`client API<client>`
before this chapter, as it gives the definition of many terms used here as well as the general way to use clients.

JQM is very database-centric, with (nearly) all communications going through the database. It was therefore
logical for the first client implementation to be a direct to database API.

.. note:: Actually, even if this API uses a direct connection to the database for nearly everything, there is one API method
	which does not work that way: file retrieval.
	Files produced by job instances (business files or simply logs) are stored locally on each node - therefore retrieving these files requires
	connecting directly (HTTP GET) to the nodes. Therefore, talk of HTTP connection parameters should not come as a surprise.

.. highlight:: xml

Parameters
**********************************

The API uses a JDBC connection to interact with the database. Long story short, it needs a JNDI resource named
jdbc/jqm to connect to the database. This name can be overloaded.

It is possible to overload persistence unit properties either:

* (specific to this client) with a jqm.properties file inside the META-INF directory
* (as for every other client) by using Java code, before creating any client::

	Properties p = new Properties();
	p.put("javax.persistence.nonJtaDataSource", "jdbc/houbahop");
	JqmClientFactory.setProperties(p);

If the file retrieval abilities are used, some connection data may also be provided through the same system when SSL is used:

* com.enioka.jqm.ws.truststoreFile: in case SSL is used, this will be the trustStore to use. Default is: system trust store (inside Java installation).
* com.enioka.jqm.ws.truststoreType: same as above - type of the store. Default is JKS.
* com.enioka.jqm.ws.truststorePass: same as above. Default is empty.

There is no need to specify user/passwords/certificate even if API authentication is enabled as the API will grant itself permissions inside the database.
(see :doc:`/admin/security`)

Libraries
***********************

In Maven terms, only one library is needed::

	<dependency>
		<groupId>com.enioka.jqm</groupId>
		<artifactId>jqm-api-client-jdbc</artifactId>
		<version>${jqm.version}</version>
	</dependency>

Logs
*********

The API uses slf4j to log information. It only provides slf4j-api, without any implementation to avoid polluting the user's class path.
Therefore, out of the box, the only log it will ever create is a warning on startup that an implementation is required in order to view log messages.

If logs are needed, an implementation must be provided (such as slf4j-log4j12) and configured to retrieve data from classes in the *com.enioka.jqm* namespace.

For example, this may be used as an implementation::

	<dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-log4j12</artifactId>
		<version>${slf4j.version}</version>
	</dependency>

and then the following log4j configuration file will set reasonable log levels on the console standard output::

	# define the console appender
	log4j.appender.consoleAppender = org.apache.log4j.ConsoleAppender

	# now define the layout for the appender
	log4j.appender.consoleAppender.layout = org.apache.log4j.PatternLayout
	log4j.appender.consoleAppender.layout.ConversionPattern=%d{dd/MM HH:mm:ss.SSS}|%-5p|%-40.40t|%-17.17c{1}|%x%m%n

	# now map our console appender as a root logger, means all log messages will go to this appender
	log4j.rootLogger = INFO, consoleAppender
	log4j.logger.com.enioka.jqm = INFO

Making it work with both Tomcat and Glassfish/WebSphere
***************************************************************

Servlet containers such as Tomcat have a different way of handling JNDI alias contexts than full JEE containers. Basically, a developper would use java:/comp/env/jdbc/datasource inside Tomcat
and simply jdbc/datasource in Glassfish. JQM implements a hack to make it work anyway in both cases. To enable it, it is compulsory to specify the JNDI alias inside the configuration file
or inside the Property object, just like above.

TL;DR: to make it work in both cases, don't write anything specific inside your web.xml and use this in your code before making any API call::

	Properties p = new Properties();
	p.put("javax.persistence.nonJtaDataSource", "jdbc/jqm");
	JqmClientFactory.setProperties(p);

