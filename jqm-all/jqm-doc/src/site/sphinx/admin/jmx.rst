JMX monitoring
###################

JQM fully embraces JMX as its main way of being monitored.

.. highlight:: java

Monitoring JQM through JMX
****************************

JQM exposes threee different level of details through JMX: the engine, the pollers inside the engine, and the job instances currently running
inside the pollers.

The first level will most often be enough: it has checks for seeing if the engine process is alive and if the pollers are polling. Basically, the two
elements needed to ensure the engine is doing its job. It also has a few interesting statistics.

The poller level offers the same checks but at its level.

Finally, it is possible to monitor each job individually. This should not be needed very often, the main use being killing a running job.

The JMX tree is as follow:

* com.enioka.jqm:type=Node,name=XXXX
* com.enioka.jqm:type=Node.Queue,Node=XXXX,name=YYYY
* com.enioka.jqm:type=Node.Queue.JobInstance,Node=XXXX,Queue=YYYY,name=ZZZZ

where XXXX is a node name (as given in configuration), YYYY is a queue name (same), and ZZZZ is an ID (the same ID as in History).

In JConsole, this shows as:

.. image:: /media/jmx.png

.. note:: there is another type of object which is exposed by JQM: the JDBC pools. Actually, the pool JMX beans come from tomcat-jdbc, and
	for more details please use their documentation at https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html. Suffice to say it is very complete,
	and exposes methods to recycle, free connections, etc.

Remote JMX access
************************

By default, JQM does not start the remote JMX server and the JMX beans can only be accessed locally. To start the JMX remote server, two :class:`Node` (i.e. the 
parameters of a :term:`JQM engine`) parameters must be set: jmxRegistryPort (the connection port) and jmxServerPort (the port on which the real communicate will occur).
If one of these two parameters is null or less than one, the JMX remote server is disabled. 

The connection string is displayed (INFO level) inside the main engine log at startup. It is in the style of ::

	service:jmx:rmi://dnsname:jmxServerPort/jndi/rmi://dnsname:jmxRegistryPort/jmxrmi

When using jConsole, it is possible to simply specify dnsname:jmxRegistryPort.

Remark: JMX usually uses a random port instead of a fixed jmxServerPort. As this is a hassle in an environment with firewalls, JQM includes a JMX server that uses a fixed port,
and specifying jmxServerPort in the configuration is therefore mandatory.

.. warning:: JQM does not implement any JMX authentication nor encryption. This is a huge security risk, as JMX allows to run arbitrary code remotely.
	**Only enable this in production within a secure network**. Making JQM secure is already an open enhancement request.

Beans detail
*****************

.. class:: JqmEngineMBean

	This bean tracks a JQM engine. 
	
	.. method:: getCumulativeJobInstancesCount
	
		The total number of job instances that were run on this node since the last history purge. (long)

	.. method:: getJobsFinishedPerSecondLastMinute
	
		On all queues, the number of job requests that ended last minute. (float)
		
	.. method:: getCurrentlyRunningJobCount
	
		The number of currently running job instances on all queues (long)
		
	.. method:: getUptime
	
		The number of seconds since engine start. (long)
		
	.. method:: isAllPollersPolling
		
		A must-be-monitored element: True if, for all pollers, the last time the poller looped was less than a polling period ago.
		Said the other way: will be false if at least one queue is late on evaluating job requests. (boolean)
		
	.. method:: isFull
	
		Will usually be a warning element inside monitoring. True if at least one queue is full. (boolean)

	.. method:: getVersion
	
		The engine version, in x.x.x form. (string)

	.. method:: stop
	
		Stops the engine, exactly as if stopping the service (see stop procedure for details).


		
.. class:: PollingMBean

	This bean tracks a local poller. A poller is basicaly a thread that polls a :term:`queue` inside the database at a given interval (defined in a :class:`DeploymentParameter`).
	
	.. method:: getCurrentActiveThreadCount
	
		The number of currently running job instances inside this queue.
		
	.. method:: stop
	
		Stops the poller. This means the queue won't be polled anympore by the engine, even if configuration says otherwise, until engine restart.
		
	.. method:: getPollingIntervalMilliseconds
	
		 Number of seconds between two database checks for new job instance to run. Purely configuration - it is present to help computations inside the monitoring system.
		 
	.. method:: getMaxConcurrentJobInstanceCount
	
		Max number of simultaneously running job instances on this queue on this engine. Purely configuration - it is present to help computations inside the monitoring system.
		
	.. method:: getCumulativeJobInstancesCount
	
		The total number of job instances that were run on this node/queue since the last history purge.
		
	.. method:: getJobsFinishedPerSecondLastMinute
	
		The number of job requests that ended last minute. (integer)
		
	.. method:: getCurrentlyRunningJobCount
	
		The number of currently running job instances inside this queue.
		
	.. method:: isActuallyPolling
	
		True if the last time the poller looped was less than a period ago. (the period can be retrived through :meth:`getPollingIntervalMilliseconds`)
		
	.. method:: isFull
	
		True if running count equals max job number. (the max count number can be retrieved through :meth:`getMaxConcurrentJobInstanceCount`)

		
		
.. class:: LoaderMBean

	This bean tracks a running job, allowing to query its properties and (try to) stop it. It is created just before the start of the :term:`payload` and destroyed when it ends.
	
	.. method:: kill()
	
		Tries to kill the job. As Java is not very good at killing threads, it will often fail to achieve anything. See :ref:`the job documentation<culling>` for more details.
		
	.. method:: getApplicationName();
	
		The name of the job. (String)
		
	.. method:: getEnqueueDate();
	
		Start time (Calendar)
		
	.. method:: getKeyword1();
	
		A fully customizable and optional tag to help sorting job requests. (String)
		
	.. method:: getKeyword2();
	
		A fully customizable and optional tag to help sorting job requests. (String)
		
	.. method:: getKeyword3();
	
		A fully customizable and optional tag to help sorting job requests. (String)
		
	.. method:: getModule();
	
		A fully customizable and optional tag to help sorting job requests. (String)
		
	.. method:: getUser();
		
		A fully customizable and optional tag to help sorting job requests. (String)
		
	.. method:: getSessionId();
	
		A fully customizable and optional tag to help sorting job requests. (int)
		
	.. method:: getId();
	
		The unique ID attributed by JQM to the execution request. (int)
		
	.. method:: getRunTimeSeconds();
	
		Time elapsed between startup and current time. (int)

