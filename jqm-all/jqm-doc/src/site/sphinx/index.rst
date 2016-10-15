JQM - Java Job Queue Manager
##################################

JQM (short for Job Queue Manager) is a Java batch job manager. It takes standard Java code (which does 
not need to be specifically tailored for JQM) and, when receiving an execution request, will run it 
asynchronously, taking care of everything that would otherwise be boilerplate code with no added value 
whatsoever: configuring libraries, logs, throttling processes, handling priorities between different classes of 
jobs, distributing the load over multiple servers, distributing the files created by the jobs, and much more...
Basically, it is a very lightweight application server specifically tailored for making it easier to run Java batch jobs. 

The rational behind JQM is that there are too many jobs that fall inside that uncomfortable middle ground between 
"a few seconds" (this could be done synchronously inside a web application server) and "a few hours" (in 
which case forking a new dedicated JVM is often the most adapted way). 
A traditional servlet or J2EE application server should not house this kind of jobs: they are designed to deal 
with very short running synchronous user requests, not asynchronous long running jobs. For example, creating a thread
in such a server is dangerous as they offer little control over it, and they do not offer job queuing (which is a basic
tenet of asynchronous execution).

JQM should also be considered just for its ability to untangle the execution itself from the program that requires it. 
Two of the most obvious applications are:

* relieving the application server, which often costs money - the front end stays on the licensed application 
  server on an expensive server, while the resource consuming jobs go inside JQM on low end servers.
* job execution request frequency adaptation. Often a job is requested to run multiple times at the same moment 
  (either by a human request, or an automated system reacting to frequent events, ...) while the job should 
  actually run only one at a time (e.g. the job handles all available data at the time of its 
  launch - so there is really no need for multiple instances in parallel). JQM will throttle these requests.

Most of the time, the code that will be run by JQM will be a direct reuse of existing code without any modifications 
(for jars including a classic main function, or Runnable threads). But it also optionally offers a rich API that 
allows running code to ask for another execution, to retrieve structured parameters, to send messages and other 
advancement notices... Also of note, JQM is pure Java Standard Edition 6 (JSE 1.6) to enable not only code but binary reuse.
Standard JSE code also means it is possible to use any framework within JQM, like Spring batch or Hibernate.

Interacting with JQM is also easy: an API, with two different implementations (JPA & REST web service, which can be 
used from a non-Java world) for different needs, is offered to do every imaginable operation (new execution request, 
querying the state of a request, retrieving files created by a job instance, ...).


It is also of note that JQM was created with compatibility in mind:

* uses either PostgreSQL, Oracle, MySQL or HSQLDB
* the client API is usable in all application servers and JSE code (tested with WebsSphere 8.x, Glassfish 3.x, Tomcat 7.x)
* one of the client API implementations is a REST-like API, callable from everywhere, not only Java but also .NET or shell scripts
  (which by the way allows very easy scheduler integration).
* under an Apache 2 license, which basically allows you to do anything you want with the product and its code in any environment


Finally, JQM is a free (as beer) and open source product backed by the IT consulting firm [Enioka](http://www.enioka.com) 
which first developed it for a multinational conglomerate. Enquiries about support, development of extensions, 
integration with other products, consulting and other commercial questions are more than welcome at contact@enioka.com. 
Community support is of course freely offered on GitHub using the bug-tracker.

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
