Managing queues
###################

.. highlight:: xml

JQM is a Queue Manager (the enqueued objects being payload execution requests). There can be as
many queues as needed, and each JMQ node can be set to poll a given set of queues, each with
different parameters.

By default, JQM creates one queue named "default" and every new node will poll that queue every ten seconds.
This is obviously very limited - this chapter details how to create new queues and set nodes to poll it.

Please read the :doc:`concepts overview<../archi>` chapter before dealing with this paragraph, as it 
defines the underlying concepts and gives examples.


Defining queues
*****************

Queues are defined inside the JQM database table QUEUE. It can be directly modified though the web administration console,
directly in the database, or an XML export/import system can be used.
Basically, a queue only has an internal technical ID, a name and a description. All fields are compulsory.

The XML is in the form::

	<jqm>
		<queues>
			<queue>
				<name>XmlQueue</name>
				<description>Queue to test the xml import</description>
				<timeToLive>10</timeToLive>
				<jobs>
					<applicationName>Fibo</applicationName>
					<applicationName>Geo</applicationName>
				</jobs>
			</queue>
			<queue>
				<name>XmlQueue2</name>
				<description>Queue 2 to test the xml import</description>
				<timeToLive>42</timeToLive>
				<jobs>
					<applicationName>DateTime</applicationName>
				</jobs>
			</queue>
		</queues>
	</jqm>


The XML does more than simply specify a queue: it also specify which job definitions should use the queue by default.
The XML can be created manually or exported from a JQM node. (See the :doc:`CLI reference<cli>` for import and export commands)

The timeToLive parameter is not used any more.

Defining pollers
********************

Having a queue is enough to enqueue job requests in it but nothing will happen to these requests if no node polls the queue
to retrieve the requests...

The association between a node and a queue is done inside the JQM database table DEPLOYMENTPARAMETER (or the GUI). 
It defines the following elements:

* ID: A technical unique ID
* CLASSID: unused (and nullable)
* NODE: the technical ID of the Node
* QUEUE: the technical ID of the Queue
* NBTHREAD: the maximum number of requests that can be treaded at the same time
* POLLINGINTERVAL: the number of milliseconds between two peeks on the queue. **Never go below 1000ms.**

