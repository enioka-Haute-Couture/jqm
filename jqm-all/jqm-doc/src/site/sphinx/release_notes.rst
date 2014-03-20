Release notes
######################

1.1.6
***********

Release goal
++++++++++++++++++

This release was aimed at making JQM easier to intagrate in production environments, with new features like
JMX monitoring, better log file handling, JDBC connection pooling, etc.

A very few developer features slipped inside the release.

Upgrade notes
+++++++++++++++++++

No breaking changes. 

Compatibility matrix:

+-------------------------------+----------+------------+------------+
| Version 1.1.6 / Other version | Engine   | Client API | Engine API |
+===============================+==========+============+============+
| Engine                        |          | >= 1.1.4   | >= 1.1.4   |
+-------------------------------+----------+------------+------------+
| Client API                    | == 1.1.6 |            |            |
+-------------------------------+----------+------------+------------+
| Engine API                    | >= 1.1.5 |            |            |
+-------------------------------+----------+------------+------------+

How to read the compatibility matrix: each line corresponds to one JQM element in version 1.1.6. 
The different versions given correspond to the minimal version of other components for version 1.1.6 to work.
A void cell means there is no constraint between these components.

For exemple : a payload using engine API 1.1.6 requires at least an engine 1.1.5 to work.

Major
++++++++++++

* Documentation: now in human readable form and on https://jqm.readthedocs.org
* Distribution: releases now published on Maven Central, snapshots on Sonatype OSSRH.
* Engine: added JDBC connection pooling
* Engine: added JMX monitoring (local & remote on fixed ports). See http://jqm.readthedocs.org/en/latest/admin/jmx.html for details
* Engine: each job instance now has its own logfile
* Engine: it is now impossible to launch two engines with the same node name (prevent startup cleanup issues creating data loss)
* Engine: failed job requests due to engine kill are now reported as crashed jobs on next engine startup
* Engine: added UrlFactory to create URL JNDI resources
* Engine: dependencies/libs are now reloaded when the payload jar file is modified or lib folder is modified. No JQM restart needed anymore.

Minor
+++++++++++++

* Engine API: legacy JobBase class can now be inherited through multiple levels
* Engine: incomplete payload classes (missing parent class or lib) are now correctly reported instead of failing silently
* Engine: refactor of main engine classes
* Engine: races condition fixes in stop sequence (issue happening only in JUnit tests)
* Engine: no longer any permanent database connection
* Engine: Oracle db connections now report V$SESSION program, module and user info
* Engine: logs are less verbose, default log level is now INFO, log line formatting is now cleaner and more readable
* General: Hibernate minor version upgrade due to major Hibernate bugfixes
* General: cleaned test build order and artifact names

1.1.5
***********

Release goal
++++++++++++++++++

Bugfix release. 

Upgrade notes
+++++++++++++++++++

No breaking changes. 

Major
++++++++++++

*Nothing*

Minor
+++++++++++++

* Engine API: engine API enqueue works again
* Engine API: added get ID method
* Db: index name shortened to please Oracle

1.1.4
**************

Release goal
++++++++++++++++++

This release aimed at fulfilling all the accepted enhancement requests that involved breaking changes, so as to clear up the path for future evolutions.

Upgrade notes
++++++++++++++++++

Many breaking changes in this release in all components. Upgrade of engine, upgrade of all libraries are required plus rebuild of database. *There
is no compatibiliy whatsoever between version 1.1.4 of the libraries and previous versions of the engine and database.*

Please read the rest of the release notes and check the updated documentation at https://github.com/enioka/jqm/blob/master/doc/index.md 

Major
++++++++++++++++++

* Documentation: now fully on Github
* Client API: - **breaking** - is no longer static. This allows:
   * to pass it parameters at runtime
   * to use it on Tomcat as well as full EE6 containers without configuration changes
   * to program against an interface instead of a fully implemented class and therefore to have multiple implementations and less breaking changes in the times to come
* Client API: - **breaking** - job instance status is now an enum instead of a String
* Client API: added a generic query method
* Client API: added a web service implementation in addition to the Hibernate implementation
* Client API: no longer uses log4j. Choice of logger is given to the user through the slf4j API (and still works without any logger).
* Client API: in scenarios where the client API is the sole Hibernate user, configuration was greatly simplified without any need for a custom persistence.xml
* Engine: can now run as a service in Windows.
* Engine: - **breaking** - the engine command line, which was purely a debug feature up to now, is officialized and was made usable and documented.
* Engine API: now offers a File resource through the JNDI API
* Engine API: payloads no longer need to use the client or engine API. A simple static main is enough, or implementing Runnable. 
  Access to the API is done through injection with a provided interface.
* Engine API: added a method to provide a temporary work directory


Minor
++++++++++++++++++

* Engine: various code refactoring, including cleanup according to Sonar rules.
* Engine: performance enhancements (History is now insert only, classpaths are truly cached, no more unzipping at every launch)
* Engine: can now display engine version (CLI option or at startup time)
* Engine: web service now uses a random free port at node creation (or during tests)
* Engine: node name and web service listeing DNS name are now separate notions
* Engine: fixed race condition in a rare high frequency scenario
* Engine: engine will now properly crash when Jetty fails to start
* Engine: clarified CLI error messages when objects do not exist or when database connection cannot be established
* Engine: - **breaking** - when resolving the dependencies of a jar, a lib directory (if present) now has priority over pom.xml
* Engine tests: test fixes on non-Windows platforms
* Engine tests: test optimization with tests no longer waiting an arbitrary amount of time
* Client API: full javadoc added
* Engine API: calling System.exit() inside payloads will now throw a security ecveption (not marked as breaking as it was already forbidden)
* General: - **breaking** - tags fields (other1, other2, ...) were renamed "keyword" to make their purpose clearer
* General: packaging now done with Maven

1.1.3
***********

Release goal
++++++++++++++++++

Fix release for the client API.

Major
++++++++++++++++++

* No more System.exit() inside the client API.

Minor
++++++++++++++++++

*Nothing*


