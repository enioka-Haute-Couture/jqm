Web Service Client API
#############################

**Client API** is the name of the API offered to the end users of JQM: it allows to interact with running jobs, offering operations
such as creating a new execution request, cancelling a request, viewing all currently running jobs, etc. Read :doc:`client API<client>` 
before this chapter, as it gives the definition of many terms used here as well as the general way to use clients.

The main client API is the Hibernate Client API, which runs directly against the JQM central database. As JQM is database centric,
finding jobs to run by polling the database, this is most efficient. However, this API has a serious drawback: it forces the user of the API to
use Hibernate. This can be a huge problem in EE6 applications, as most containers (WebSphere, Glassfish, JBoss...) offer their own implementation
of the JPA standard which is not compatible with Hibernate and cannot coexist with it (there must be only one JPA implementation at the same time,
and a database created for Hibernate is very difficult to reuse in another JPA provider). Moreover, even outside the EE6 field,
the client may already have chosen a JPA implementation that is not Hibernate. This is why JQM also offers an optional **REST Web Service Client API**.

Client side
********************

There are two ways to use the WS Client API:

* Using the Java client
* Directly using the web service

Using the Java client
++++++++++++++++++++++++++++

.. highlight:: xml

This is a standard client API implementing the JqmClient interface. Like all clients, it is used by putting its jar on your classpath.
The client uses the JAX-RS 2 API, so it needs an implementation such as Jersey (obviously not provided, the one provided by the container will
be used).

For Maven users::

	<dependency>
		<groupId>com.enioka.jqm</groupId>
		<artifactId>jqm-api-client-jersey</artifactId>
		<version>${jqm.version}</version>
	</dependency>	

and then using the API::

	JqmClient jqm = JqmClientFactory.getClient();

As with any client (see the JavaDoc) clients are cached by the API, so it is not necessary to cache them yourself.

Interrogating the API is then also exactly the same as with any other client. For example, to list all running jobs::

	JqmClientFactory.getClient().getJobs()

The specific parameters are:

+----------------------------------+------------+----------------------------------------------+------------------------------+
| Name                             | Compulsory | Description                                  | Example                      |
+==================================+============+==============================================+==============================+
| com.enioka.ws.url                | YES        | The base URL of the web service              | http://localhost:1789/api/ws |
+----------------------------------+------------+----------------------------------------------+------------------------------+
| com.enioka.jqm.ws.login          |            | If auth is used only.                        | mylogin                      |
+----------------------------------+------------+----------------------------------------------+------------------------------+
| com.enioka.jqm.ws.password       | if login   |                                              | password                     |
+----------------------------------+------------+----------------------------------------------+------------------------------+
| com.enioka.jqm.ws.keystoreFile   | if CSA     | Store for client certificates authentication | ./conf/client.pfx            |
+----------------------------------+------------+----------------------------------------------+------------------------------+
| com.enioka.jqm.ws.keystoreType   |            | Type of the previous store                   | PKCS12                       |
+----------------------------------+------------+----------------------------------------------+------------------------------+
| com.enioka.jqm.ws.keystorePass   |            | Password of the store                        | MyPassword                   |
+----------------------------------+------------+----------------------------------------------+------------------------------+
| com.enioka.jqm.ws.truststoreFile | if SSL     | Trust roots for server certificates          | ./conf/client.pfx            |
+----------------------------------+------------+----------------------------------------------+------------------------------+
| com.enioka.jqm.ws.truststorePass |            | Password of the store                        | NoPassword                   |
+----------------------------------+------------+----------------------------------------------+------------------------------+

and can be set:

* (specific to this client) with a jqm.properties file inside the META-INF directory
* (as for every other client) using Java code, before creating any client::

	Properties p = new Properties();
	p.put("com.enioka.ws.url", "http://localhost:9999/marsu/ws");
	JqmClientFactory.setProperties(p);
* through a system parameter (-Dcom.enioka.ws.url=http://...)
	
Interrogating the service directly
++++++++++++++++++++++++++++++++++++++++

The previous client is only a use of the JAX-RS 2 API. You can also create your own web service proxy
by interrogating the web service  with the library of your choice (including the simple commons-http). See the :ref:`web_service_ref` for that.

.. highlight:: xml

Should that specific implementation need the interface objects, they are present in the jqm-api-client jar (the pure API jar without any 
implementation nor dependencies). ::

	<dependency>
		<groupId>com.enioka.jqm</groupId>
		<artifactId>jqm-api-client</artifactId>
		<version>${jqm.version}</version>
	</dependency>

Choosing between the two approaches
+++++++++++++++++++++++++++++++++++++++++++

When using Java, the recommended approach is to **use the provided client**. This will allow you to:

* ignore completely all the plumbing needed to interrogate a web service
* change your client type at will, as all clients implement the same interface
* go faster with less code to write!

The only situations when it is recommended to build your own WS client are:

* when using another language
* when you don't want or can't place the WS client library Jersey on your classpath. For example, in an EE6 server that provides JAX-RS 1 and
  just don't want to work with version 2.

Server side
********************

The web service is not active on any engine by default. To activate it, see the administration guide.

It is not necessary to enable the service on all JQM nodes. It is actually recommended to dedicate a node that will not host jobs (or few) to the WS.
Moreover, it is a standard web application with purely stateless sessions,
so the standard mechanisms for load balancing or high availability apply if you want them.

.. _web_service_ref:

Service reference
***********************

All objects are serialized to XML by default or to JSON if required. The service is a REST-style web service, so no need for SOAP and other bubbly things.

+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| URL pattern           | Method | Non-URL arguments     | Return type         | Return MIME         | Interface equivalent | Description                                                    |
+=======================+========+=======================+=====================+=====================+======================+================================================================+
| /ji                   | GET    |                       | List\<JobInstance\> | application/xml     | getJobs              | List all known job instances                                   |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji                   | POST   | JobRequest            | JobInstance         | application/xml     | enqueue              | New execution request                                          |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/query             | POST   | Query                 | Query               | application/xml     | getJobs(Query)       | Returns the executed query                                     |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/{jobId}           | GET    |                       | JobInstance         | application/xml     | getJob(int)          | Details of a Job instance                                      |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/{jobId}/messages  | GET    |                       | List\<String\>      | application/xml     | getJobMessages(int)  | Retrieve messages created by a Job Instance                    |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/{jobId}/files     | GET    |                       | List\<Deliverables\>| application/xml     | getJobDeliverables   | Retrieve  the description of all files created by a JI         |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/{jobId}/stdout    | GET    |                       | InputStream         | application/os      | getJobLogStdOut      | Retrieve the stdout log file of the (ended) instance           |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/{jobId}/stderr    | GET    |                       | InputStream         | application/os      | getJobLogStdErr      | Retrieve the stderr log file of the (ended) instance           |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
|/ji/{jobId}/position/{}| POST   |                       | void                |                     | setJobQueuePosition  | Change the position of a waiting job instance inside a queue.  |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/active            | GET    |                       | List\<JobInstance\> | application/xml     | getActiveJobs        | List all waiting or running job instances                      |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/cancelled/{jobId} | POST   |                       | void                |                     | cancelJob(int)       | Cancel a waiting Job Instance (leaves history)                 |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/killed/{jobId}    | POST   |                       | void                |                     | killJob(int)         | Stop (crashes) a running job instance if possible              |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/paused/{jobId}    | POST   |                       | void                |                     | pauseQueuedJob(int)  | Pause a waiting job instance                                   |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/paused/{jobId}    | DELETE |                       | void                |                     | resumeJob(int)       | Resume a paused job instance                                   |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/waiting/{jobId}   | DELETE |                       | void                |                     | deleteJob(int)       | Completely cancel/remove a waiting Job Instance (even history) |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /ji/crashed/{jobId}   | DELETE |                       | JobInstance         | application/xml     | restartCrashedJob    | Restarts a crashed job instance (deletes failed history)       |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /q                    | GET    |                       | List\<Queue\>       | application/xml     | getQueues            | List all queues defined in the JQM instance                    |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /q/{qId}/{jobId}      | POST   |                       | void                |                     | setJobQueue          | Puts an existing waiting JI into a given queue.                |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /user/{uname}/ji      | GET    |                       | List\<JobInstance\> | application/xml     | getActiveJobs        | List all waiting or running job instances for a user           |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /jd                   | GET    |                       | List\<JobDef\>      | application/xml     | getActiveJobs        | List all job definitions                                       |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /jd/{appName}         | GET    |                       | List\<JobInstance\> | application/xml     | getActiveJobs        | List all job definitions  for a given application              |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+
| /jr                   | GET    |                       | JobRequest          | application/xml     | N/A                  | Returns an empty JobRequest. Usefull for scripts.              |
+-----------------------+--------+-----------------------+---------------------+---------------------+----------------------+----------------------------------------------------------------+

Note: application/os = application/output-stream.

Used HTTP error codes are:

* 400 (bad request) when responsibility for the failure hangs on the user (trying to delete an already running instance, instance does not exist, etc)
* 500 when it hangs on the server (unexpected error)

On the full Java client side, these are respectively translated to :class:`JqmInvalidRequestException` and :class:`JqmClientException`.

The body of the response contains an XML or JSON item giving details on the error.::

    1	404	GET on absent object
    2	500	
    3	404	DELETE on absent object
    4	400	Update user with absent role
    5	500	Could not create certificate
    6	400	Invalid enqueue parameters
    7	400	Simple API  is only available when the application runs on top of JQM itself and not a web application server.
    8	400	File does not exist
    9	500	Generic internal exception
    10	400	Generic bad request


Script sample
****************

PowerShell script. Logics is the same in any language, script or compiled.::
    
    # Note: we use JSON as a demonstration of how to use it over the default XML. Obviously, PowerShell deals with XML very well and does not need this.
    
    # Authentication?
    $cred = Get-Credential root
    
    #################################
    ## Enqueue demonstration
    #################################
    
    $request = @{applicationName="DemoApi"; user=$env:USERNAME} | ConvertTo-Json
    $jobInstance = Invoke-RestMethod http://localhost:62948/ws/client/ji -Method Post -Body $request -Credential $cred -ContentType "application/json" -Headers @{Accept="application/json"}
    $jobInstance.id
    
    
    #################################
    ## Query demonstration
    #################################
    
    $query = @{applicationName="DemoApi"} | ConvertTo-Json
    $res = Invoke-RestMethod http://localhost:62948/ws/client/ji/query -Method Post -Body $query -Credential $cred -ContentType "application/json" -Headers @{Accept="application/json"}
    $res.instances | Format-Table -AutoSize	
