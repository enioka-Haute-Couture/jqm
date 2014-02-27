Release notes
######################

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


