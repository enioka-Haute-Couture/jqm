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

