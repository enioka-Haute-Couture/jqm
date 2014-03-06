JQM - Java Job Queue Manager
##################################

Introduction
*****************

JQM (short for Job Queue Manager) is a middleware allowing to run arbitrary Java code asynchronously on a distributed network of servers.
It was designed as an application server specifically tailored for making it easier to run (Java) batch jobs asynchronously, removing all the hassle of configuring
libraries, throttling executions, handling logs, forking new processes & monitoring them, dealing with created files, and much more... It should be considered
for batch jobs that fall inside that uncomfortable middle ground between "a few seconds" (this could be done synchronously inside a web application
server) and "a few hours" (in which case forking a new dedicated JVM is often the most adapted way).

It should also be considered for its ability to untangle the execution itself from the program that requires it. Two of the most obvious cases are:

* getting long running jobs out of the application server. An application server is not supposed to handle these, which fill up its 
  queues and often end in weird timeouts and runaway threads. JQM will host these externalized jobs in an async way, not requiring the application server 
  to wait for completion. Execution can happen on another server/VM, freeing resources (and potentially licence costs).
* job execution request frequency adaptation. Often a job is requested to run multiple times at the same moment (either by a human request, or an automated 
  system reacting to frequent events, ...) while the job should actually run only one at a time (e.g. the job handles all available data at the time of its 
  launch - so there is really no need for multiple instances in parallel). JQM will throttle these requests.

Most of the time, the code that will be run by JQM will be a direct reuse of existing code without any modifications (for jars including a classic main function,
or Runnable threads). But it also optionaly offers a rich API that allows running code to ask for another execution, to retrieve structured parameters,
to send messages and other advancement notices... Also of note, JQM is pure Java Standard Edition 6 (JSE 1.6) to enable not only code but binary reuse.

Interacting with JQM is also easy: an API, with two different implementations (JPA & REST web service, which can be used from a non-Java world) for different needs, 
is offered to do every imaginable operation (new execution request, querying the state of a request, retrieving files created by a job instance, ...).

Documentation
******************

:doc:`Latest release notes<release_notes>`.

.. toctree::
	:numbered:
	:titlesonly:
	:maxdepth: 2

	archi
	admin/install
	
	jobs/index
	client/index
	
	admin/index	
	admin/troubleshooting
	
	dev/index
	
	glossary



To be added:

* admin/logs
* admin/basic operations (start, stop, ...)
* quick run guide (install will be moved to admin)

  
Bug report
**************

Please direct all bug reports or feature requests at `GitHub <https://github.com/enioka/jqm/issues>`_.
