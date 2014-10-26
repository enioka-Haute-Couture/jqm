JQM - Java Job Queue Manager
##################################

JQM (short for Job Queue Manager) is a middleware allowing to run arbitrary Java code asynchronously on a distributed network of servers.
It was designed as an application server specifically tailored for making it easier to run (Java) batch jobs asynchronously, removing all the hassle of configuring
libraries, throttling executions, handling logs, forking new processes & monitoring them, dealing with created files, and much more... It should be considered
for batch jobs that fall inside that uncomfortable middle ground between "a few seconds" (this could be done synchronously inside a web application
server) and "a few hours" (in which case forking a new dedicated JVM is often the most suitable way).

It should also be considered for its ability to untangle the execution itself from the program that requires it. Two of the most obvious cases are:

* getting long running jobs out of the application server. An application server is not supposed to handle these, which fill up its 
  queues and often end in weird time-outs and runaway threads. JQM will host these externalized jobs in an asynchronous way, not requiring the application server 
  to wait for completion. Execution can happen on another server/VM, freeing resources (and potentially licence costs, as JQM is free contrary to many application servers).
* job execution request frequency adaptation. Often a job is requested to run multiple times at the same moment (either by a human request, or an automated 
  system reacting to frequent events, ...) while the job should actually run only one at a time (e.g. the job handles all available data at the time of its 
  launch - so there is really no need for multiple instances in parallel). JQM will throttle these requests.

Most of the time, the code that will be run by JQM will be a direct reuse of existing code without any modifications (for jars including a classic main function,
or Runnable threads). But it also optionally offers a rich API that allows running code to ask for another execution, to retrieve structured parameters,
to send messages and other advancement notices... Interacting with JQM is also easy: an API, with two different implementations (including REST-style web services, which can be used from a non-Java world) for different needs, is offered to do every imaginable operation easily (new execution request, querying the state of a request, retrieving files created by a job instance, ...).


.. toctree::
    :numbered:
    :maxdepth: 3
    :titlesonly:

    features
    archi
    quickstart
    
    jobs/index
    client/index
    
    admin/index	
    
    trouble/index
    
    dev/index
    
    glossary
