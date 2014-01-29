# Using resources

Most programs use some sort of resource - some read files, other write to a relational database, etc. 
In this document, we will refer to a "resource" as the description containing all the necessary data 
to use it (a file path, a database connection string + password, ...)

There are many approaches to define these resources (directly in the code, in a configuration file...) but they all have caveats
(mostly: they are not easy to use in a multi environment context, where resource descriptions change from one environment to another).
Java has standardized JNDI as a way to define these resources, and JQM provides a limited JNDI directory implementation that can be used by 
the [payloads](writing_payloads.md).

JQM JNDI can be used for:

* JDBC connections
* JMS resources
* Files

:warning: JNDI is actually part of JEE, not JSE, but it is so useful in the context of JQM use cases that it was implemented. The fact
that it is present does **not** mean that JQM is a JEE container. Notably, there is no injection mechanism and JNDI resources have to be
manually looked up.

:grey_exclamation: An object returned by a JNDI lookup is just a description. The JNDI system has not checked if the object existed, if
all parameters are present, etc. It also means that it is the client's respsonsbility to open files, database connections... and close them
in the end.

The JNDI system is totally independent from the JQM API described in [payload API](writing_payloads.md#Accessing the JQM engine API). It is always
present, whatever type your payload is and even if the jqm-api jar is not present.

## JDBC

### Using
```java
DataSource ds = (DataSource) NamingManager.getInitialContext(null).lookup("jdbc/superalias");
```

It could of interest to note that the JQM NamingManager is standard - it can be used from wherever is needed, such as a JPA provider configuration:
in a persistence.xml, it is perfectly valid to use <non-jta-datasource>jdbc/superalias</non-jta-datasource>.

If all programs running inside a JQM cluster always use the same database, it is possible to define a JDBC alias as the "default 
connection" (cf. [parameters](parameters.md)). It can then be retrieved directly through the getDefaultConnection method of the JQM API.
(this is the only JNDI-related element that requires the API).

### Defining

> :warning: JDBC aliases must begin with jdbc/

A line must be created inside the JQM database table named DatabaseProp. Fields are self-explanatory.

## JMS

### Using
```java
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
```

### Defining

> :warning: JMS aliases must begin with jms/

An entry must be created inside the JQM database table JndiObjectResource and the object parameters must be added to the table JndoObjectResourceParameter.

*Exemple for MQ Series QueueConnexionFactory:*

Classname | Factory class name
---------- | ---------------------
com.ibm.mq.jms.MQQueueConnectionFactory | com.ibm.mq.jms.MQQueueConnectionFactoryFactory

Parameter name | Value
----- | -----------------
HOST | broker host name
PORT | mq borker listener port
CHAN | name of the channel to connect to
QMGR | name of the queue manager to connect to
TRAN | always 1 (means CLIENT transmission)

*Exemple for MQ Series Queue:*

Classname | Factory class name
---------- | ---------------------
com.ibm.mq.jms.MQQueue | com.ibm.mq.jms.MQQueueFactory

Parameter name | Value
----- | -----------------
QU    | queue name

*Exemple for ActiveMQ QueueConnexionFactory:*

Classname | Factory class name
---------- | ---------------------
org.apache.activemq.ActiveMQConnectionFactory | org.apache.activemq.jndi.JNDIReferenceFactory

Parameter name | Value
----- | -----------------
brokerURL | broker URL (see ActiveMQ site)

*Exemple for ActiveMQ Queue:*

Classname | Factory class name
---------- | ---------------------
org.apache.activemq.command.ActiveMQQueue | org.apache.activemq.jndi.JNDIReferenceFactory

Parameter name | Value
----- | -----------------
physicalName    | queue name

## Files

### Using
```java
File f = (File) NamingManager.getInitialContext(null).lookup("fs/superalias");
```

### Defining

> :warning: file aliases must begin with fs/

Same tables as for JMS resources. (these tables can actually hold whatever JNDI object resource)

Classname | Factory class name
---------- | ---------------------
java.io.File.File | com.enioka.jqm.jndi.FileFactory

Parameter name | Value
----- | -----------------
PATH    | path that will be used to initialize the File object
