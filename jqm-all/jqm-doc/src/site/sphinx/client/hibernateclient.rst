JPA Client API
###################

**Client API** is the name of the API offered to the end users of JQM: it allows to interact with running jobs, offering operations
such as creating a new execution request, cancelling a request, viewing all currently running jobs, etc. Read :doc:`client API<client>` 
before this chapter, as it gives the definition of many terms used here as well as the general way to use clients.

JQM is very database-centric, with (nearly) all communications to JQM servers going through the database. It was therefore
logical for the first client implementation to be a direct to database API, using the same ORM named Hibernate as in the engine.

Using the Hibernate client
**********************************

In a JNDI-enabled container without other JPA use
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

Hypothesis: 

* deployment inside an EE6 container such as WebSphere, JBoss, Glassfish, or deployment inside a JSE container with
  JNDI abilities (Tomcat, **JQM itself**, ...)
* There is no use of any JPA provider in the application (no persistence.xml)

.. highlight:: xml

In this case, using the API is just a matter of providing the API as a dependency, plus the Hibernate implementation of your choice
(compatible with 3.5.6-Final onwards to 4.2.x). In Maven terms::

	<dependency>
		<groupId>com.enioka.jqm</groupId>
		<artifactId>jqm-api-client-hibernate</artifactId>
		<version>${jqm.version}</version>
	</dependency>
	<dependency>
		<groupId>org.hibernate</groupId>
		<artifactId>hibernate-entitymanager</artifactId>
		<version>${hibernate.version}</version>
	</dependency>

Please note that if your container provides a JPA provider by itself, there is obviously no need for the second dependency 
but beware: this client is **only compatible with Hibernate**, not OpenJPA, EclipseLink/TopLink or others. So if you are provided 
another provider, you may need to play with the options of your application server to replace it with Hibernate. This has been tested with
WebSphere 8.x and Glassfish 3.x. If changing this provider is not possible or desirable, use the :doc:`webservice` instead.

Then it is just a matter of declaring the JNDI alias "jdbc/jqm" pointing to the JQM database (refer to your container's documentation)
and the API is ready to use. There is no need for parameters in this case (everything is already declared inside the persistence.xml of the API).

With other JPA use
++++++++++++++++++++++++++++

Hypothesis: 

* deployment inside an EE6 container such as WebSphere, JBoss, Glassfish, or deployment inside a JSE container with
  JNDI abilities (Tomcat, **JQM itself**, ...), or no JNDI abilities (plain Sun JVM)
* There is already a persistence.xml in the project that will use the client API

This case is a sub-case of the previous paragraph - so first thing first, everything stated in the previous paragraph 
must be applied.

Then, an issue must be solved: there can only be (as per JPA2 specification) one persistence.xml used. The API needs
its persistence unit, and the project using the client needs its own. So we have two! The classpath mechanisms of containers (servlet or EE6)
guarantee that the persistence.xml that will be used is the one from the caller, not the API. Therefore, it is necesseray to 
redeclare the JQM persistence unit inside the final persistence.xml like this::

	<persistence-unit name="jobqueue-api-pu">
		<provider>org.hibernate.ejb.HibernatePersistence</provider>
		<non-jta-data-source>jdbc/jqm2</non-jta-data-source>

		<jar-file>../jqm-model/target/jqm-model-1.1.4-SNAPSHOT.jar</jar-file>

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

Optional parameters
++++++++++++++++++++++++

In both cases, it is possible to overload persistence unit properties either:

* (specific to this client) with a jqm.properties file inside the META-INF directory
* (as for every other client) using Java code, before creating any client::

	Properties p = new Properties();
	p.put("javax.persistence.nonJtaDataSource", "jdbc/houbahop");
	JqmClientFactory.setProperties(p);

The different properties possible are JPA2 properties (http://download.oracle.com/otndocs/jcp/persistence-2.0-fr-eval-oth-JSpec/) and 
Hibernate properties (http://docs.jboss.org/hibernate/orm/4.2/manual/en-US/html/ch03.html#configuration-optional). 
The preceding exemple changed (or set in the first place) the <non-jta-datasource\> to some JNDI alias.

Making it work with both Tomcat and Glassfish/WebSphere
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

Servlet containers such as Tomcat have a different way of handling JNDI alias contexts than full JEE containers. Basically, a developper would use java:/comp/env/jdbc/datasource inside Tomcat
and simply jdbc/datasource in Glassfish. JQM implements a hack to make it work anyway in both cases. To enable it, it is compulsory to specify the JNDI alias inside the configuration file
or inside the Properrty object, just like above.

TL;DR: to make it work in both cases, don't write anything specific inside your web.xml and use this in your code before making any API call::

	Properties p = new Properties();
	p.put("javax.persistence.nonJtaDataSource", "jdbc/jqm");
	JqmClientFactory.setProperties(p);

