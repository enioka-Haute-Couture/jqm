Administrating resources
###########################

Defining a resource
*************************

Resources are defined inside the JQM database, and are therefore accessible from all JQM nodes.
By 'resource' JNDI means an object that can be created through a (provided) 
`ObjectFactory <http://docs.oracle.com/javase/7/docs/api/javax/naming/spi/ObjectFactory.html>`_. There are multiple factories provided with JQM, concerning databases,
files & URLs which are detailed below. Moreover, the :term:`payload` may provide whatever factories it needs, such as a JMS driver (example also below).

The main JNDI directory table is named :class:`JndiObjectResource` and the object parameters belong to the table :class:`JndiObjectResourceParameter`.
Resources can be edited through the administration console.

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
| singleton      | see below                                                                               | false                                          |
+----------------+-----------------------------------------------------------------------------------------+------------------------------------------------+

For every resource type (and therefore, every ObjectFactory), there may be different parameters: connection strings, paths, ports, ... These
parameters are to be put inside the table JndiObjectResourceParameter.

The JNDI alias is free to choose - even if conventions exist. Please note that JQM only provides a root context, and no subcontexts. Therefore, in all 
lookups, the given alias will searched 'as provided' (including case) inside the database.

Singletons
**********

One parameter is special: it is named "singleton". Default is 'false'. If 'true', the creation and caching of the
resource is made by the engine itself in its own class context, and not inside the payload's context (i.e. classloader). It is useful for the
following reasons:

* Many resources are actually to be shared between payloads, such as a connection pool
* Very often, the payload will expect to be returned the same resource when making multiple JNDI lookups, not a different one on each call. Once again, 
  one would expect to be returned the same connection pool on each call, and definitely not to have a new pool created on each call!
* Some resources are dangerous to create inside the payload's context. As stated in :doc:`../jobs/writing_payloads`, loading a JDBC driver creates
  memory leaks (actually, class loader leaks). By delegating this to the engine, the issue disappears.

Singleton resources are created the first time they are looked up, and kept forever afterwards.

As singleton resources are created by the engine, the jar files containing resource & resource factory must be available to the engine class loader.
For this reason, the jar files must be placed manually inside the $JQM_ROOT/ext directory (and they do not need to be placed inside the 
dependencies of the payload, even if it does not hurt to have them there). For a resource which provider is within the payload, being
a singleton is impossible - the engine class context has no access to the payload class context.

By default, the $JQM_ROOT/ext directory contains the following providers, ready to be used as singleton (or not) resources:

* the File provider and URl provider inside a single jar named jqm-provider
* the JDBC pool, inside two jars (tomcat-jdbc and tomcat-juli)
* the HSQLDB driver

Besides the HSQLDB driver, which can be removed if another database is used, the provided jars should never be removed. Jars added
later (custom resources, other JDBC drivers, ...) can of course be removed. 
Also of note: it is not because a jar is inside 'ext' that the corresponding resources can only be singletons. They can be standard as well.


Examples
***************
Below, some examples of resources definition. To see how to actually use them in your code, look at :doc:`/jobs/resources`.

JDBC
+++++++++++++

.. note:: the recommended naming pattern for JDBC aliases is jdbc/name

Connection pools to databases through JDBC is provided by an ObjectFactory embedded with JQM named tomcat-jdbc.

As noted above, JDBC pool resources should always be singletons: it is stupid to create a new pool on each call AND it would
create class loader leaks otherwise.

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

There are many other options, detailed in the `Tomcat JDBC documentation <https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html>`_.

JMS
++++++++++++

.. note:: the recommended naming pattern for JMS aliases is jms/name

*Parameters for MQ Series QueueConnectionFactory:*

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

*Parameters for MQ Series Queue:*

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

*Parameters for ActiveMQ QueueConnexionFactory:*

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

*Parameters for ActiveMQ Queue:*

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

.. note:: the recommended naming pattern for files is fs/name

+-------------------+--------------------------------------+
| Classname         | Factory class name                   |
+===================+======================================+
| java.io.File.File | com.enioka.jqm.providers.FileFactory |
+-------------------+--------------------------------------+

+----------------+------------------------------------------------------+
| Parameter name | Value                                                |
+================+======================================================+
| PATH           | path that will be used to initialize the File object |
+----------------+------------------------------------------------------+


URL
+++++++++

.. note:: the recommended naming pattern for URL is url/name

+-------------------+--------------------------------------+
| Classname         | Factory class name                   |
+===================+======================================+
| java.io.URL       | com.enioka.jqm.providers.UrlFactory  |
+-------------------+--------------------------------------+

+----------------+------------------------------------------------------+
| Parameter name | Value                                                |
+================+======================================================+
| URL            | url that will be used to initialize the URL object   |
+----------------+------------------------------------------------------+

Mail session
++++++++++++++

Outgoing SMTP mail session.

.. note:: the recommended naming pattern is mail/name

+--------------------+----------------------------------------------+
| Classname          | Factory class name                           |
+====================+==============================================+
| javax.mail.Session | com.enioka.jqm.providers.MailSessionFactory  |
+--------------------+----------------------------------------------+

+----------------+--------------------------------------------------------------------+
| Parameter name | Value                                                              |
+================+====================================================================+
| smtpServerHost | Name or IP of the SMTP server. The only compulsory parameter       |
+----------------+--------------------------------------------------------------------+
| smtpServerPort | Optional, default is 25                                            |
+----------------+--------------------------------------------------------------------+
| useTls         | Default is false                                                   |
+----------------+--------------------------------------------------------------------+
| fromAddress    | Can be overloaded when sending a mail. Default is noreply@jobs.org |
+----------------+--------------------------------------------------------------------+
| smtpUser       | If SMTP server requires authentication.                            |
+----------------+--------------------------------------------------------------------+
| smtpPassword   |                                                                    |
+----------------+--------------------------------------------------------------------+

