JPA Client API
###################

**Client API** is the name of the API offered to the end users of JQM: it allows to interact with running jobs, offering operations
such as creating a new execution request, cancelling a request, viewing all currently running jobs, etc. Read :doc:`client API<client>` 
before this chapter, as it gives the definition of many terms used here as well as the general way to use clients.

JQM is very database-centric, with (nearly) all communications going through the database. It was therefore
logical for the first client implementation to be a direct to database API, using the same ORM named Hibernate as in the engine.

.. note:: actually, even if this API uses a direct connection to the database for nearly everything, there is one API method 
	which does not work that way: file retrieval.
	Files produced by job instances (business files or simply logs) are stored locally on each node - therefore retrieving these files requires
	connecting directly (HTTP GET) to the nodes. Therefore, talk of HTTP connection parameters should not come as a surprise.

.. highlight:: xml
	
Parameters
**********************************

The API uses a JPA persistence unit to interact to the database. Long story short, it needs a JNDI resource named
jdbc/jqm to connect to the database. This name can be overloaded.

It is possible to overload persistence unit properties either:

* (specific to this client) with a jqm.properties file inside the META-INF directory
* (as for every other client)by using Java code, before creating any client::

	Properties p = new Properties();
	p.put("javax.persistence.nonJtaDataSource", "jdbc/houbahop");
	JqmClientFactory.setProperties(p);

The different properties possible are JPA2 properties (http://download.oracle.com/otndocs/jcp/persistence-2.0-fr-eval-oth-JSpec/) and 
Hibernate properties (http://docs.jboss.org/hibernate/orm/4.2/manual/en-US/html/ch03.html#configuration-optional). 
The preceding example changed (or set in the first place) the <non-jta-datasource\> to some JNDI alias. Dafault is jdbc/jqm.

If the file retrieval abilities are used, some connection data may also be provided through the same systems when SSL is used:

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
		<artifactId>jqm-api-client-hibernate</artifactId>
		<version>${jqm.version}</version>
	</dependency>

If the file retrieval APIs are not used, it is possible to remove one library with itself a lot of dependencies from the API. In Maven terms::

	<dependency>
		<groupId>com.enioka.jqm</groupId>
		<artifactId>jqm-api-client-hibernate</artifactId>
		<version>${jqm.version}</version>
		<exclusions>
			<exclusion>
				<groupId>org.apache.httpcomponents</groupId>
				<artifactId>httpclient<artifactId>
			<exclusion>
		</exclusions>
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

Container integration
*****************************

There may be nefast interactions between the persistence unit contained inside the API and the rest of the environment.

In a JNDI-enabled container without other JPA use
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

Hypothesis: 

* deployment inside an EE6 container such as WebSphere, JBoss, Glassfish, or deployment inside a JSE container with
  JNDI abilities (Tomcat, **JQM itself**, ...)
* There is no use of any JPA provider in the application (no persistence.xml)

In this case, using the API is just a matter of providing the API as a dependency, plus the Hibernate implementation of your choice
(compatible with 3.5.6-Final onwards to 4.2.x).

Please note that if your container provides a JPA2 provider by itself, there is obviously no need for providing a JPA2 implementation
but beware: this client is **only compatible with Hibernate**, not OpenJPA, EclipseLink/TopLink or others. So if you are provided 
another provider, you may need to play with the options of your application server to replace it with Hibernate. This has been tested with
WebSphere 8.x and Glassfish 3.x. JBoss comes with Hibernate. If changing this provider is not possible or desirable, use the :doc:`webservice` instead.

Then it is just a matter of declaring the JNDI alias "jdbc/jqm" pointing to the JQM database (refer to your container's documentation)
and the API is ready to use. There is no need for parameters in this case (everything is already declared inside the persistence.xml of the API).


With other JPA use
++++++++++++++++++++++++++++

.. warning:: this paragraph is not needed for recent versions of Hibernate (4.x) as they extend the JPA specification by allowing
	multiple persistence units. Therefore, only the previous paragraph applies.

Hypothesis: 

* deployment inside an EE6 container such as WebSphere, JBoss, Glassfish, or deployment inside a JSE container with
  JNDI abilities (Tomcat, **JQM itself**, ...), or no JNDI abilities (plain Sun JVM)
* There is already a persistence.xml in the project that will use the client API

This case is a sub-case of the previous paragraph - so first thing first, everything stated in the previous paragraph 
should be applied.

Then, an issue must be solved: there can only be (as per JPA2 specification) one persistence.xml used. The API needs
its persistence unit, and the project using the client needs its own. So we have two! The classpath mechanisms of containers (servlet or EE6)
guarantee that the persistence.xml that will be used is the one from the caller, not the API. Therefore, it is necesseray to 
redeclare the JQM persistence unit inside the final persistence.xml like this::

	<persistence-unit name="jobqueue-api-pu">
		<provider>org.hibernate.ejb.HibernatePersistence</provider>
		<non-jta-data-source>jdbc/jqm2</non-jta-data-source>

		<jar-file>../jqm-model/target/jqm-model-VERSION.jar</jar-file>

		<properties>
			<property name="javax.persistence.validation.mode" value="none" />
		</properties>
	</persistence-unit>

	<persistence-unit name="whatever-pu-needed-by-your-application">
		<provider>org.hibernate.ejb.HibernatePersistence</provider>
		<non-jta-data-source>jdbc/test</non-jta-data-source>
		<class>jpa.Entity</class>
	</persistence-unit>

Note the use of "jar-file" to reference a jar containing a declared persistence unit. The name of the persistence unit must 
always be "jobqueue-api-pu". The **file path inside the jar tag must be adapted to your context and packaging, as well as JQM
version**. The non-jta-datasource alias can be named anything you want (you may even want to redefine completely the datasource here,
not using JNDI - see the Hibernate reference for the properties to set to do so).

.. warning:: the use of the <jar-file> tag is only allowed if the application package is an ear file, not a war.

Making it work with both Tomcat and Glassfish/WebSphere
***************************************************************

Servlet containers such as Tomcat have a different way of handling JNDI alias contexts than full JEE containers. Basically, a developper would use java:/comp/env/jdbc/datasource inside Tomcat
and simply jdbc/datasource in Glassfish. JQM implements a hack to make it work anyway in both cases. To enable it, it is compulsory to specify the JNDI alias inside the configuration file
or inside the Properrty object, just like above.

TL;DR: to make it work in both cases, don't write anything specific inside your web.xml and use this in your code before making any API call::

	Properties p = new Properties();
	p.put("javax.persistence.nonJtaDataSource", "jdbc/jqm");
	JqmClientFactory.setProperties(p);

