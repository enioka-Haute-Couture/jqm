Using resources
###################

.. highlight:: java


Introduction
***************************************

Most programs use some sort of resource - some read files, other write to a relational database, etc. 
In this document, we will refer to a "resource" as the description containing all the necessary data 
to use it (a file path, a database connection string + password, ...)

There are many approaches to define these resources (directly in the code, in a configuration file...) but they all have caveats
(mostly: they are not easy to use in a multi environment context, where resource descriptions change from one environment to another).
All these approaches can be used with JQM since JQM runs all JSE code.
Yet, Java has standardized `JNDI <http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface>`_ as a way to retrieve these resources, and JQM provides a limited JNDI directory implementation that can be used by 
the :term:`payloads<payload>`.

JQM JNDI can be used for:

* JDBC connections
* JMS resources
* Files
* URLs
* every ObjectFactory provided by the payloads

.. warning:: JNDI is actually part of JEE, not JSE, but it is so useful in the context of JQM use cases that it was implemented. The fact
	that it is present does **not** mean that JQM is a JEE container. Notably, there is no injection mechanism and JNDI resources have to be
	manualy looked up.

.. note:: An object returned by a JNDI lookup (in JQM or elsewhere) is just a description. The JNDI system has not checked if the object existed, if
	all parameters are present, etc. It also means that it is the client's respsonsbility to open files, database connections... and close them
	in the end.

The JNDI system is totally independent from the JQM API described in :ref:`accessing_jqm_api`. It is always
present, whatever type your payload is and even if the jqm-api jar is not present.


To define resources, see :doc:`/admin/resources`.

Below, some samples for various cases.

JDBC
*****
Connection pools to databases through JDBC is provided by an ObjectFactory embedded with JQM named tomcat-jdbc.
Connection pools should always be singletons.

::

        DataSource ds = (DataSource) NamingManager.getInitialContext(null).lookup("jdbc/superalias");

Please note the "null" for the context lookup: JQM only uses a root context. See below for details.

It is interesting to note that the JQM NamingManager is standard - it can be used from wherever is needed, such as a JPA provider configuration:
in a persistence.xml, it is perfectly valid to use <non-jta-datasource>jdbc/superalias</non-jta-datasource>.

If all programs running inside a JQM cluster always use the same database, it is possible to define a JDBC alias as the "default 
connection" (cf. :doc:`../admin/parameters`). It can then be retrieved directly through the getDefaultConnection method of the JQM API.
(this is the only JNDI-related element that requires the API).

JMS
*******
Connecting to a JMS broker to send or receive messages, such as ActiveMQ or MQSeries, requires 
first a QueueConnectionFactory, then a Queue object. The implementation of these interfaces
changes with brokers, and are not provided by JQM - they must be provided with the payload or put inside ext.

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


        Provided by the engine - these resources must therefore always be singletons.

Files
************
Provided by the engine - these resources must therefore always be singletons.

::

        File f = (File) NamingManager.getInitialContext(null).lookup("fs/superalias");

        Provided by the engine - these resources must therefore always be singletons.

URL
***************
Provided by the engine - these resources must therefore always be singletons.

::

        URL f = (URL) NamingManager.getInitialContext(null).lookup("url/testurl");

