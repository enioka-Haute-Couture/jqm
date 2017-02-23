JQM features
###############

* The only dedicated Java batch server
* Open Source under the Apache 2 licence, a business-friendly licence securing your investments in JQM
* No-cost ready to use solution. Paying support can if needed be purchased from the original authors 
  at contact@enioka.com or at any other firm open to doing maintenance on the tool.
* Fully documented

Batch code is easy to create:

* Runs existing Java 1.6 code, without need for programming specifically for JQM
* Possible (but not required) to use a specific framework of your choice (Spring batch, etc.)
* Possible (but not required) to easily tweak class loading to enable advance scenarios
* Many samples for all features (inside JQM's own integration tests)
* Specific API to handle file creation and easy retrieval (a file can be created on any server and retrieved from another in a single call)
* Embedded standard JNDI directory with JDBC connection pooling for jobs needing database connectivity
* Jobs can be tested as if they were running inside a JQM node thanks to a test library which can be 
  used in jQuery tests.
* Can easily report an advancement status to users or administrators
* All (always optional) JQM artefacts are published on Maven Central and therefore easily integrate with most build systems

Interacting with batch jobs is simple:

* Query API enabling to easily create client applications (with two full samples included in the distribution), such as 
  web pages listing all the jobs for given user, for a given module, etc.
* Feature rich API with provided Java clients, which can be used out of the box for launching jobs,
  cancelling them, changing their priorities

Batch packaging: just use your own

* Full Maven 3 support: as a Maven-created jar contains its pom.xml, JQM is able to retrieve all the dependencies, simplifying packaging libraries.
* It is even possible to directly run Maven coordinates without providing any jar file!
* More classic packaging also supported (library directory, or putting all libraries inside the jar)

Administration is a breathe:

* Both command line and web-based graphic user interface for administration
* Can run as a Windows service or a Linux /etc/init.d script
* Fully ready to run out of the box without complicated configuration
* Supported on most OSes and databases
* Log files can be accessed easily through a distributed web GUI
* Easy definition of service classes (VIP jobs, standard jobs, ...) through queues
* Easy integration with schedulers and CLI
* Most configuration change are hot-applied, with little to no need for server restarts
* Resists most environment failures (database failure, network failure, ...)
* Maintenance jobs are integrated
* Can be fully monitored through JMX - which make it compatible with most monitoring systems out of the box (a Zabbix template is provided)
* Authentication and permissions handling is fully in-box, including an optional PKI to create
  client certificates.