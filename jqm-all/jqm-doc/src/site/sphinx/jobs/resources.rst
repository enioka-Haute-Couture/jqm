Using resources
###################

.. highlight:: java

Most programs use some sort of resource - some read files, other write to a relational database, etc. 
In this document, we will refer to a "resource" as the description containing all the necessary data 
to use it (a file path, a database connection string + password, ...)

There are many approaches to define these resources (directly in the code, in a configuration file...) but they all have caveats
(mostly: they are not easy to use in a multi environment context, where resource descriptions change from one environment to another).
All thses approches can be used with JQM since JQM runs all JSE code.
Yet, Java has standardized JNDI as a way to define these resources, and JQM provides a limited JNDI directory implementation that can be used by 
the :term:`payloads<payload>`.

JQM JNDI can be used for:

* JDBC connections
* JMS resources
* Files
* URLs

.. warning:: JNDI is actually part of JEE, not JSE, but it is so useful in the context of JQM use cases that it was implemented. The fact
	that it is present does **not** mean that JQM is a JEE container. Notably, there is no injection mechanism and JNDI resources have to be
	manualy looked up.

.. note:: An object returned by a JNDI lookup (in JQM or elsewhere) is just a description. The JNDI system has not checked if the object existed, if
	all parameters are present, etc. It also means that it is the client's respsonsbility to open files, database connections... and close them
	in the end.

The JNDI system is totally independent from the JQM API described in :ref:`accessing_jqm_api`. It is always
present, whatever type your payload is and even if the jqm-api jar is not present.

JNDI resources
***************************************

Using
+++++++++++++

This is vanilla JNDI inside the root JNDI context: ::

	DataSource ds = (DataSource) NamingManager.getInitialContext(null).lookup("jdbc/superalias");


Please note the "null" for the context lookup: JQM only uses a root context. See below for details.
	
Defining
++++++++++++

Resources are defined inside the JQM database, and are therefore accessible from all JQM nodes.
By 'resource' JNDI means an object that can be created through a (provided) 
`ObjectFactory <http://docs.oracle.com/javase/7/docs/api/javax/naming/spi/ObjectFactory.html>`_. There are multiple factories provided with JQM, concerning databases,
files & URLs which are detailed below. Moreover, the :term:`payload` may provide whatever factories it needs, such as a JMS driver (example also below).

The main JNDI directory table is named :class:`JndiObjectResource` and the object parameters belong to the table :class:`JndiObjectResourceParameter`.

The following elements are needed for every resource, and are defined in the main table:

+----------------+-----------------------------------------------------------------------------------------+------------------------------------------------+
| Name           | Description                                                                             | Example                                        |
+================+=========================================================================================+================================================+
| name           | The JNDI alias - the string used to refer to the resource in the :term:`payload` code   | jdbc/mydatasource                              |
+----------------+-----------------------------------------------------------------------------------------+------------------------------------------------+
| description    | a short string giving the admin every info he needs                                     | connection to main db                          |
+----------------+-----------------------------------------------------------------------------------------+------------------------------------------------+
| type           | the class name of the desired resource                                                  | com.ibm.mq.jms.MQQueueConnectionFactory        |
+----------------+-----------------------------------------------------------------------------------------+------------------------------------------------+
| factory        | the class name of the ObjectFactory able to create the desired resource                 | com.ibm.mq.jms.MQQueueConnectionFactoryFactory |
+----------------+-----------------------------------------------------------------------------------------+------------------------------------------------+

For every resource type (and therefore, every ObjectFactory), there may be different parameters: connection strings, paths, ports, ... These
parameters are to be put inside the table JndiObjectResourceParameter.

The JNDI alias is free to choose - even if conventions exist. Please note that JQM only provides a root context, and no subcontexts. Therefore, in all 
lookups, the given alias will searched 'as provided' (including case) inside the database.

Singletons
-------------

One parameter is special: it is named "singleton". If not present, it is considered to be 'false'. If 'true', the creation and caching of the
resource is made by the engine itself in its own class context, and not inside the payload's context (i.e. classloader). It is useful for the
following reasons:

* Many resources are actually to be shared between payloads, such as a connection pool
* Very often, the payload will expect to be returned the same resource when making multiple JNDI lookups, not a different one on each call. Once again, 
  one would expect to be returned the same connection pool on each call, and definitiely not to have a new pool created on each call!
* Some resources are dangerous to create inside the payload's context. As stated in :doc:`writing_payloads`, loading a JDBC driver creates
  memory leaks (actually, classloader leaks). By delegating this to the engine, the issue disappears.

Singleton resources are created the first time they are looked up, and kept forever afterwards.

As singleton resources are created by the engine, the jar files containing resource & resource factory must be available to its classloader.
For this reason, the jar files must be placed manually inside the $JQM_ROOT/ext directory (and they do not need to be placed inside the 
dependencies of the payload, even if it does not hurt to have them there). For a resource which provider is within the payload, being
a singleton is impossible - the engine context has no access to the payload context.


Examples
***************

JDBC
+++++++++++++

Connection pools to databases through JDBC is provided by an ObjectFactory embedded with JQM named tomcat-jdbc.
Connection pools should always be singletons.

Using
---------
::

	DataSource ds = (DataSource) NamingManager.getInitialContext(null).lookup("jdbc/superalias");

It could of interest to note that the JQM NamingManager is standard - it can be used from wherever is needed, such as a JPA provider configuration:
in a persistence.xml, it is perfectly valid to use <non-jta-datasource>jdbc/superalias</non-jta-datasource>.

If all programs running inside a JQM cluster always use the same database, it is possible to define a JDBC alias as the "default 
connection" (cf. :doc:`../admin/parameters`). It can then be retrieved directly through the getDefaultConnection method of the JQM API.
(this is the only JNDI-related element that requires the API).

Defining
---------

.. note:: the recommended naming pattern for JDBC aliases is jdbc/name

+-----------------------------------------+-------------------------------------------------+
| Classname                               | Factory class name                              |
+=========================================+=================================================+
| javax.sql.DataSource                    | org.apache.tomcat.jdbc.pool.DataSourceFactory   |
+-----------------------------------------+-------------------------------------------------+

+----------------+-----------------------------------------+
| Parameter name | Value                                   |
+================+=========================================+
| maxActive      | max number of pooled connections        |
+----------------+-----------------------------------------+
| driverClassName| class of the db JDBC driver             |
+----------------+-----------------------------------------+
| url            | database url (see db documentation)     |
+----------------+-----------------------------------------+
| singleton      | always true (since engine provider)     |
+----------------+-----------------------------------------+
| username       | database account name                   |
+----------------+-----------------------------------------+
| password       | password for the database account       |
+----------------+-----------------------------------------+

There are many options, detailed in the `Tomcat JDBC documentation <https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html>`_.

JMS
++++++++++++

Connecting to a JMS broker to send or receive messages, such as ActiveMQ or MQSeries, requires 
first a QueueConnectionFactory, then a Queue object. The implementation of these interfaces
changes with brokers, and are not provided by JQM - they must be provided with the payload or put inside ext.

Using
---------
::

	import javax.jms.Connection;
	import javax.jms.MessageProducer;
	import javax.jms.Queue;
	import javax.jms.QueueConnectionFactory;
	import javax.jms.Session;
	import javax.jms.TextMessage;
	import javax.naming.spi.NamingManager;
	import com.enioka.jqm.api.JobBase;

	public class SuperTestPayload extends JobBase
	{
		@Override
		public void start()
		{
			int nb = 0;
			try
			{
				// Get the QCF
				Object o = NamingManager.getInitialContext(null).lookup("jms/qcf");
				System.out.println("Received a " + o.getClass());

				// Do as cast & see if no errors
				QueueConnectionFactory qcf = (QueueConnectionFactory) o;

				// Get the Queue
				Object p = NamingManager.getInitialContext(null).lookup("jms/testqueue");
				System.out.println("Received a " + p.getClass());
				Queue q = (Queue) p;

				// Now that we are sure that JNDI works, let's write a message
				System.out.println("Opening connection & session to the broker");
				Connection connection = qcf.createConnection();
				connection.start();
				Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

				System.out.println("Creating producer");
				MessageProducer producer = session.createProducer(q);
				TextMessage message = session.createTextMessage("HOUBA HOP. SIGNED: MARSUPILAMI");

				System.out.println("Sending message");
				producer.send(message);
				producer.close();
				session.commit();
				connection.close();
				System.out.println("A message was sent to the broker");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}


Defining
---------

.. note:: the recommended naming pattern for JMS aliases is jms/name

*Exemple for MQ Series QueueConnectionFactory:*

+-----------------------------------------+-------------------------------------------------+
| Classname                               | Factory class name                              |
+=========================================+=================================================+
| com.ibm.mq.jms.MQQueueConnectionFactory | com.ibm.mq.jms.MQQueueConnectionFactoryFactory  |
+-----------------------------------------+-------------------------------------------------+

+----------------+-----------------------------------------+
| Parameter name | Value                                   |
+================+=========================================+
| HOST           | broker host name                        |
+----------------+-----------------------------------------+
| PORT           | mq broker listener port                 |
+----------------+-----------------------------------------+
| CHAN           | name of the channel to connect to       |
+----------------+-----------------------------------------+
| QMGR           | name of the queue manager to connect to |
+----------------+-----------------------------------------+
| TRAN           | always 1 (means CLIENT transmission)    |
+----------------+-----------------------------------------+

*Exemple for MQ Series Queue:*

+------------------------+-------------------------------+
| Classname              | Factory class name            |
+========================+===============================+
| com.ibm.mq.jms.MQQueue | com.ibm.mq.jms.MQQueueFactory |
+------------------------+-------------------------------+

+----------------+------------------+
| Parameter name | Value            |
+================+==================+
| QU             | queue name       |
+----------------+------------------+

*Exemple for ActiveMQ QueueConnexionFactory:*

+-----------------------------------------------+-----------------------------------------------+
| Classname                                     | Factory class name                            |
+===============================================+===============================================+
| org.apache.activemq.ActiveMQConnectionFactory | org.apache.activemq.jndi.JNDIReferenceFactory |
+-----------------------------------------------+-----------------------------------------------+

+----------------+--------------------------------+
| Parameter name | Value                          |
+================+================================+
| brokerURL      | broker URL (see ActiveMQ site) |
+----------------+--------------------------------+

*Exemple for ActiveMQ Queue:*

+-------------------------------------------+-----------------------------------------------+
| Classname                                 | Factory class name                            |
+===========================================+===============================================+
| org.apache.activemq.command.ActiveMQQueue | org.apache.activemq.jndi.JNDIReferenceFactory |
+-------------------------------------------+-----------------------------------------------+

+----------------+---------------+
| Parameter name | Value         |
+================+===============+
| physicalName   | queue name    |
+----------------+---------------+

Files
+++++++++++

Provided by the engine - these resources must therefore always be singletons.

Using
---------
::

	File f = (File) NamingManager.getInitialContext(null).lookup("fs/superalias");

Defining
---------

.. note:: the recommended naming pattern for files is fs/name

+-------------------+---------------------------------+
| Classname         | Factory class name              |
+===================+=================================+
| java.io.File.File | com.enioka.jqm.jndi.FileFactory |
+-------------------+---------------------------------+

+----------------+------------------------------------------------------+
| Parameter name | Value                                                |
+================+======================================================+
| PATH           | path that will be used to initialize the File object |
+----------------+------------------------------------------------------+


URL
+++++++++

Provided by the engine - these resources must therefore always be singletons.

Using
---------
::

	URL f = (URL) NamingManager.getInitialContext(null).lookup("url/testurl");

Defining
---------

.. note:: the recommended naming pattern for URL is url/name

+-------------------+---------------------------------+
| Classname         | Factory class name              |
+===================+=================================+
| java.io.URL       | com.enioka.jqm.jndi.UrlFactory  |
+-------------------+---------------------------------+

+----------------+------------------------------------------------------+
| Parameter name | Value                                                |
+================+======================================================+
| URL            | url that will be used to initialize the URL object   |
+----------------+------------------------------------------------------+
