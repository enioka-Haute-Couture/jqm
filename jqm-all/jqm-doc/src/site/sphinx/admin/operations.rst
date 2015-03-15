Operations
#############

Starting
************

.. note:: there is a safeguard mechanism which prevents two engines (JQM java processes) to run with the same node name.
	In case of engine crash (kill -9) the engine will ask you to wait (max. 2 minutes) to restart so as to be sure
	there is no other engine running with the same name. On the other hand, cleanly stopping the engine is totally transparent without ever 
	any need to wait.
	
Windows
+++++++++

The regular installation is as a service. Just do, inside a PowerShell prompt with elevated (admin) rights::

	Start-Service JQM*

It is also possible to start an engine inside a command prompt. In that case, the engine stops when the prompt is closed.
This is mainly useful for debugging purposes. ::

	java -jar jqm.jar -startnode $env:COMPUTERNAME
	
(change the node name at will - by default, the computer name is used for the node name).

Unix/Linux
+++++++++++++

A provided script will launch the engine in "nohup &" and store the pid inside a file. ::

	./jqm.sh start

Under \*x systems, the default node name is the username.

The script respects the general conventions of init.d scripts.

Stopping
**************

A stop operation will wait for all running jobs to complete, with a two minutes (parameter) timeout.
No new jobs are taken as soon as the stop order is thrown.

Windows
++++++++++

The regular installation is as a service. Just do, inside a PowerShell prompt with elevated (admin) rights::

	Stop-Service JQM*

For console nodes, just do Ctrl+C or close the console.

Unix
+++++++++

::

	./jqm.sh stop
	
The clean stop sequence is actually triggered by a SIGTERM (normal kill) - the jqm.sh script simply stores the PID at startup and 
does a kill to shutdown.

Restarting
****************

There should never be any need for restarting an engine, save for the few configuration changes that are 
listed in :doc:`parameters`.

Windows::

	Restart-Service JQM*

\*X::

	./jqm.sh restart

In both cases, it is strictly equivalent to stopping and then starting again manually (including the two-minutes timeout).

Pausing and resuming
***********************

An engine in pause runs normally but does not take new job instances anymore. Job instances already running at the time of pause go on normally.

Pausing and resuming methods are available at two levels:

* per node (pause all queues for an engine)
* per binding (pause only one queue for an engine)

These methods are available through JMX and though the database (in which case modifications are only applied after at most 
parameter internalPollingPeriodMs). The database method is also exposed by the admin GUI.

Backup
************

Elements to backup are:

* the database (unless the history is not precious)
* files created

Standard tools can be used, there is nothing JQM specific here.

Purges
************

The logs of the engine are automatically purged. Job instance logs and created files, however, are not.

The History table should be purged too - see :doc:`history`.

