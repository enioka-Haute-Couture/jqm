# JQM

**[Full documentation and feature list](http://jqm.readthedocs.org)**

**[Quickstart tutorial](https://jqm.readthedocs.io/en/master/quickstart.html)**

**[Release notes](https://jqm.readthedocs.io/en/master/release_notes.html)**


The aptly named Job Queue Manager, or JQM for short, is a queue manager. It has three goals:

* to optimize and streamline the execution of jobs, whatever they may be, by using queues with rich configuration
* to make job administration simple
* to be easy to include or embed in most environments.

The result is a small-footprint, easy to use grid execution system that takes care of everything which would be
boilerplate code or missing otherwise: configuring logs, throttling processes, handling priorities between different classes of 
jobs, distributing the load over multiple servers, distributing the files created by the jobs, and much more...

It is able to run anything that can be run on the command line without modifications. It also has a very rich Java integration which make it 
an ideal "job application server" for Java users - with no modifications required, allowing to directly use code from plain Main to
Spring Batch and other frameworks...

Jobs and users also optionally benefit from rich REST APIs exposing all JQM data and operations.

There are many use cases for JQM. Common real-world examples include:

* replacing another job execution manager, like the one inside OS/400 - a reference as far as job queuing is concerned
* adding a distributed execution capability to a scheduler or any application
* removing load from a paid application server
* removing asynchronous executions from a web application server, not designed to deal with long running threads
* throttling jobs, like allowing only one instance of the same job at a time



Also of note that JQM was created with compatibility in mind:

* uses either PostgreSQL, Oracle, MySQL, DB2 or an embedded HSQLDB
* one of the client API implementations is a REST-like API, callable from everywhere, not only Java but also .NET or shell scripts
* the Java implementation of the client API is usable in all application servers and JSE code (tested with WebSphere 8.x, Glassfish 3.x, Tomcat 7.x, JBoss 7+...)
* under an Apache 2 license, which basically allows you to do anything you want with the product and its code in any situation.


Finally, JQM is a free (as beer) and open source product backed by the IT consulting firm [Enioka](http://www.enioka.com) 
which first developed it for an international conglomerate. Enquiries about support, development of extensions, 
integration with other products, consulting and other commercial questions are more than welcome at contact@enioka.com. 
Community support is of course freely offered on GitHub using the bug-tracker.
