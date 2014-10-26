JQM features
###############

* The only dedicated Java batch server
* Open Source under the Apache 2 licence, a business-friendly licence securing your investments in JQM
* No-cost ready to use solution. Paying support can if needed be purchased from the original maintainer 
  at contact@enioka.com or at any other firm open to doing maintenance on the tool.
* Fully documented

Batch code

* Possible but not required to use a specific framework (Spring batch, etc.)
* Runs existing Java 1.6 code, without need for programming specifically for JQM
* Many samples for all features (inside JQM's integration tests)
* Specific API to handle file creation and easy retrieval (a file can be created on any server and retrieved from another in a single call)

Batch interactions

* Query API enabling to easily create client applications (with two full samples included in the distribution), such as 
  web pages listing all the jobs for given user, for a given module, etc.
* Feature rich API 

Batch packaging

* Full Maven 3 support: as a Maven-created jar contains its pom.xml, JQM is able to retrieve all the dependencies, simplifying packaging libraries.
* More classic packaging also supported

Administration

* Both command line and web-based graphic user interface for administration
* Can run as a Windows service or a Linux /etc/init.d script
* Fully ready to run out of the box without complicated configuration
* supported on most OSes and databases
* log files can be accessed easily through a central web GUI
* easy definition of class of service through queues
* easy integration with schedulers and CLI