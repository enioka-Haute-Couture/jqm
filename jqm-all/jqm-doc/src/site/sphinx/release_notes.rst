Release notes
######################

3.1.0
*************

Maintenance release, with a few new features and many fixes after the v3 release.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major changes
++++++++++++++++++++++++++++

* Engine: fixed double logging with logFilePerLaunch=both
* Engine: reduced standalone mode IDs size that were too long for the UI
* GUI: added Class Loaders page
* GUI: improved runs page
    * Added a "run time" column
    * Improved job logs display (shortcut, auto-refresh, fixed lines display)
    * In launch form the job definitions are sorted and can be filtered
* GUI: save table state in the runs page when switching between pages
* Web APIs: fixed renaming of a cluster-wide parameter
* Engine & web APIs: fixed configuration trap when two default queues are set
* Documentation: updated some out-of-date parts.

Minor changes
++++++++++++++++++++++++++++

* Global: fixed Javadoc
* Global: migrated to Vite
* Documentation: fixed readthedocs warnings
* GUI: login page redirection when trying to access a page without being logged in
* GUI: added favicon
* GUI: added page titles
* Web APIs: a user created without an expiration date can now have one later

3.0.0
*************

Release goal
++++++++++++++++

We are excited to present JQM 3.0.0, the first release in the 3.x line, aiming at making the already ten years old JQM ready for the next decade!
It brings a major overhaul of the JQM internals, aiming at delivering better performances, a more modern and maintenable codebase as well as enabling easier delivery of more features in future releases.
The most visible change is the new GUI, which was fully rewritten. It is more reactive and user-friendly.
It also deals with the obsolescence of many components: old Java versions are dropped, new TLS ciphers are supported, IPv6 is now supported, the CLI was rewritten...

Major changes
++++++++++++++++++++++++++++

* CLI: entirely re-written using a more understandable `verb-noun --options` style for commands (and a far more maintainable code behind this).
* GUI: totally rewritten, with a fresher look and better useability.
* Engine: the engine was cut into plugins, creating a much more modular codebase.
* Engine: added Java 11-21 compatibility. Please consider using 21 for long term projects as it is an LTS release.
* Packaging: docker image now uses Java 21.
* Web APIs: added IPv6 compatibility and latest TLS versions (on Java versions supporting them).
* Web APIs: better multiple interface handling.

Minor changes
++++++++++++++++++++++++++++

* Engine: Updated to latest Maven engine, adding latest pom.xml format compatibility.
* Engine: MySQL >= 8 and MariaDB >= 10.3 do not use a special ID generator anymore, thanks to improvements in InnoDB.
* Engine: a new "share nothing" mode without a central database was added in preview. Documentation will be added in a later version.
* Global: all libraries have been updated to their latest versions. This is particularly important for anyone exposing JQM web services, as this comes with much increased security. This includes J2EE to Jakarta migration.
* Test: client-side certificate authentication is now properly tested.
* Test: MariaDB, MySQL and PostgreSQL have been added to the already present HSQLDB in the automated test suite run on each commit or PR.

Breaking changes
+++++++++++++++++++

As the semantic versioning designation entails, this version contains a few breaking changes.
We tried to limit them, but we allowed breaks where it was necessary to improve the codebase and especially the boudaries between modules.
The most visible impact is the removal of the initialisation "magic" of JQM clients, which was a hack and the source of many issues while not really needed.

The breaking changes are:

* CLI : the internal CLI (the one called with `java -jar jqm.jar options…`) has been fully revamped. All scripts using it should be revised with the new, clearer options. If you use the .sh or .ps1 provided scripts, no changes.
* Client API: new namespaces for JqmClientFactory. It used to be `com.enioka.jqm.api`, it is now ` `com.enioka.jqm.client.jdbc` or `com.enioka.jqm.client.jersey` depending on the client you wish to use.
* Client API: change in the construction of Query instances. Used to be `new Query()`or `Query.create()`. Is now only `JqmClient.createQuery()`
* Client API: change in the execution of a query. Used to be either `Query.submit()` or `JqmClient.getJobs(Query)`. Is now only `Query.invoke()`.
* Client API: change in the construction of JobRequest instances. Used to be `new JobRequest()` or `JobRequest.create()`. Is now only `JqmClient.createJobRequest()`.
* Client API: change in the enqueing of a JobRequest. Used to be either `JobRequest.submit()` or `JqmClient.enqueue(JobRequest)`. Is now only `JobRequest.enqueue()`.
* Engine API: JobManager fields must now be public (as they should always have been - this is an interface between payload and engine, not a private implementation detail)
* Global: Java 1.6, 1.7, 8, 9 and 10 are dropped in all JQM components - either for the JQM engine or the provided Java libraries. Note that JQM actively uses the newer Java features: there is no hope to recompile this version with 1.6. 1.6 and 1.7 users should either migrate to 11 (or better 21) or stays with JQM 2.x.
* Global: support for old database versions was dropped, see our new compatibility matrix.
* Packaging: Windows docker images were dropped for lack of use. Only Linux images remain.
* Web APIs: SSL and encryptions and broken ciphers have been dropped. Only recent TLS with recent ciphers are now supported.

Deprecated
+++++++++++++++

* Java 11-16 are deprecated. JQM 4 will only support Java 17+. The best long term choice is to use Java 21, which is an LTS release.
* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-client-core" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).

Changes in management
+++++++++++++++++++++++++++

JQM is still maintained by the IT firm Enioka, but the governance has changed. The historical lead is welcoming the new leads overtaking the project:

* Backend lead: Arnaud Chirat (Enioka Haute Couture)
* Frontend lead: Paulin de Naurois (Enioka Haute Couture).

Thanks to them for taking the project over.

Also, thanks to all the PR contributors for this release!

2.2.9
*************

Maintenance release, with a lot of fixes oriented towards Kubernetes (and equivalent container orchestrators) hosting.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major changes
++++++++++++++++++++++++++++

* Engine: added `deleteStoppedNodes` global parameter, which triggers node deletion from configuration on shutdown. This is used when nodes are transient, like inside an orchestrator. (#435)
* Build: partial retrofit of GitHub Action automations from 3.0 branch, Travis CI was removed.

Minor changes
++++++++++++++++++++++++++++

* Engine: the `System.exit` check is now disabled in Java versions above 17 as Security Managers are now deprecated. (#471)
* Engine: fixed process not stopping on "engine shutdown" API call due to JMX non-daemon thread.
* Engine: can now specify the web service listening interface through the command line, overriding the node's "DNS" parameter.
* Admin GUI: global parameter latest change date no longer change if saving with the same value, preventing some useless engine restarts. (#472)
* Admin GUI: fixed remove node command (SQL error).
* Packaging: can now specify initSQL for SQL pool configuration in container deployments. Used in HSQLDB demo swarm configuration to set session timezone.
* Packaging: Docker images now listen to all network interfaces by default. This fixes the image healthcheck in all deployment configurations.
* Build: fixed Maven URL in Dockerfile (build only).
* Build: updated supported Windows images.

2.2.8
*************

Maintenance release.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Users of MySQL/MariaDB beware: after the application of this patch JQM will always store times in UTC inside the database, as it was always intended.
It used to be imposed through a connection property set in resources.xml and not through code.
If that property was not present before, you will have time zone inconsistencies inside the database after upgrade.
This will not hamper JQM operations at all, but may produce weird reports in the administration GUI on the day of upgrade.

Major changes
++++++++++++++++++++++++++++

* Admin GUI: can now be deployed in a subdirectory (for example a reverse proxy may expose the GUI on domain.com/jqm instead of simply domain.com)

Minor changes
++++++++++++++++++++++++++++

* Engine: MySQL/MariaDB now enforce UTC in code. Configuration files were updated to remove the previous method. Thanks to Ihor Herasymenko for this PR.
* Engine: better MySQL/MariaDB connection failure handling. Thanks to Eugene Echipachenko for this PR.
* Engine: fixed a nasty random database deadlock when using Highlander mode. (#432)


2.2.7
*************

Maintenance release.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major changes
++++++++++++++++++++++++++++

* Build: it is now possible to use the JQM web services, including job logs retrieval, inside a Swarm or Kubernetes cluster. A new environment variable must be set (#425)

Minor changes
++++++++++++++++++++++++++++

* Admin GUI: the page selection sliders in "runs" page had a weird interaction with the date criterions of the query (#424)
* Build: updated Docker base images
* Client API (JDBC only): when used with the Wildfly/JBoss JDBC pool, the JDBC implementation triggered pool warnings due to statement caching (#403 - new fix, thanks Mr Pool)
* Engine: the "disable node" function failed since the introduction of resource managers in 2.2.0 (#427)
* Engine: workaround for MariaDB 10.4 bug MDEV-20695 (#391)


2.2.6
*************

Maintenance release.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Minor changes
++++++++++++++++++++++++++++

* Build: updated docker base images
* Client API: fixed a concurrency issue when using highlander mode
* Client API (JDBC only): when used with the Wildfly/JBoss JDBC pool, the JDBC implementation triggered pool warnings due to statement caching (#403 - new fix)
* Engine: JMX "late jobs count" counter was wrong (#413)

Deprecated
+++++++++++++++

No new entries - same list as for 2.2.5.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.
* The Spring runner will soon no longer set the runtimeParameters bean. Use runtimeParametersProvider instead (see the JQM+Spring doc page for details).


2.2.5
*************

Maintenance release, with both fixes and small quality of life improvements.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Minor changes
++++++++++++++++++++++++++++

* Admin GUI: the logout button now redirects to the login page on all browsers (#398)
* Admin GUI: the redirection after a valid login could fail (#399)
* Admin GUI: files created by job instances (deliverables) were not listed in the job instance detail dialog (#402)
* Admin GUI: added an option to clear job parameters in the new job instance launch dialog (#411)
* Engine: on Oracle DB, there was a timezone inconsistency (JQM normally only deals with UTC times) (#400)
* Client API: when used with the Wildfly/JBoss JDBC pool, the JDBC implementation triggered pool warnings due to statement caching (#403)

Deprecated
+++++++++++++++

No new entries - same list as for 2.2.4.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.
* The Spring runner will soon no longer set the runtimeParameters bean. Use runtimeParametersProvider instead (see the JQM+Spring doc page for details).


2.2.4
*************

Maintenance release.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Minor changes
++++++++++++++++++++++++++++

* CLI: fixed import of XML files with a lot of job definitions, which opened too many database cursors.
* CLI: fixed update of JobDef default queue during XML import (was not updated by new value).

Deprecated
+++++++++++++++

No new entries - same list as for 2.2.3.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.
* The Spring runner will soon no longer set the runtimeParameters bean. Use runtimeParametersProvider instead (see the JQM+Spring doc page for details).


2.2.3
*************

Maintenance release.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

Due to bug #390, users of the Spring Runner should stop using bean named runtimeParameters and instead use a new bean named runtimeParametersProvider.
Users who do not use Spring, the runtimeParameters bean and a persistent execution context (all three needed) are not concerned by this.

No database modification in this release - upgrade can be done by simply replacing engine files.

Minor changes
++++++++++++++++++++++++++++

* Engine: fixed injected Spring execution parameters could be shared between instances and never change after the first run (#390)
* Engine: fixed rare crash on startup when trying to determine database type (#384)
* Engine: fixed shell runner which did not allow an empty "module name" tag in job instances on some platforms and Java versions (#383)
* Packaging: added Windows 1909 image

Deprecated
+++++++++++++++

Only last entry is new since 2.0.x.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.
* The Spring runner will soon no longer set the runtimeParameters bean. Use runtimeParametersProvider instead (see the JQM+Spring doc page for details).


2.2.2
*************

Release goal
++++++++++++++++

Maintenance release, mostly consisting in backports from version 3.

Upgrade notes
+++++++++++++++++++

No API breaking changes (nor any API changes for that matter).

There is one database modification in this release: a column was added to the History table. Migration is applied
when running `jqm(.sh|.ps1) createnode`. Note this is the first time the migration mechnanism is used in JQM.

Upgrade procedure for standard installation is therefore:

* Stop all nodes to avoid locks on DB tables (which could prevent schema upgrades)
* Replace all binaries with new version (keep your resources.xml configuration file!)
* Run `jqm(.sh|.ps1) createnode` (only once for the whole cluster) - this will not recreate existing nodes, simply upgrade the schema
* Restart all nodes

For those using the Docker images in a cluster, refer yourself to the Docker-specific documentation.

Major changes
++++++++++++++++++++++++++++

* All components: Java compatibility from 1.6 to 1.11 included, with automated tests (1.12 and later are not tested on the 2.x branch) (#381).
* All components: older mysql/mariadb versions do not use stored procedures anymore for sequence emulation and can now run on instances with binary logging enabled (newer versions already use sequences).

Minor changes
++++++++++++++++++++++++++++

* All components: better version detection for mariadb and mysql.
* Engine: fixed DB timezone issue which could cause unwanted Jetty restarts.
* CLI: fixed XML import error on postgresql.
* Admin API: fixed global cluster parameter update date not being updated.
* Admin GUI: fixed application name dropdown width in new launch dialog box (#366).
* Admin GUI: added runAfter to job instance history dialog box (#369).
* Packaging: sample job definitions can now be run without access to Maven Central at runtime (#347).
* Packaging: Docker images for Windows 1809, 1903.
* Packaging: Docker images were updated with latest Java 8 version.

Deprecated
+++++++++++++++

No new entries - same list as for 2.0.x.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.


2.2.1
*************

Maintenance release.

Upgrade notes
+++++++++++++++++++

No API breaking changes.

No database modification in this release - upgrade can be done by simply replacing engine files.

Minor changes
++++++++++++++++++++++++++++

* Client API: fixed connection leak and missing error message when enqueueing a job request on a queue which does not exists (#344)

Deprecated
+++++++++++++++

No new entries - same list as for 2.0.x.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.


2.2.0
*************

Release goal
++++++++++++++++

This release aimed at making it easier to launch non-Java jobs. Launching external processes (shell commands, binaries...) was always supported through the use of a special payload... which was never included
in the public distribution, and existed in many versions with different possibilities. The sum of all their functionalities, documentation and admin UI were added to the JQM engine itself, making processes first class
job definitions, on equal footing with Java.

Also of note, the admin UI was fully refactored using es6 and recent library versions. All functionalities and appearance should be the same as before - this move is mostly to prepare for the demise of
a framework which has overstayed its welcome. However, a few tweaks and fixes were included alongside the refactor and should make administrators' lives easier.

It is a simple upgrade with no breaking change.

Major changes
++++++++++++++++++++++++++++

* Engine: added process runner, making process and shell jobs first class citizen.
* Engine: laid the foundations for more diversity in polling mechanisms, starting next release.
* Build: migrated the UI build to modern npm toolchain (controled by Maven). This allows slightly better startup JS performances, and removes all stale cache issues when upgrading.
* GUI: rewritten the job definition page to use a master/detail view, as the previous tabular view was becoming impractical with too many columns.
* GUI: better login & logout experience.

Minor changes
++++++++++++++++++++++++++++

* GUI: updated all libraries and refactored javascript code using a component pattern in nearly pure ES6, in preparation for AngularJS removal from JQM.
* GUI: now uses sessions. This enables visibly better performances when security is enabled. Web services are untouched and still use either a certificate or a basic HTTP password in a purely stateless way.
* GUI: Our longest standing bug has been squashed! It is now possible to scroll horizontally while viewing a log.
* GUI: added the possibility to view node log files, not only job instance log files.
* GUI: slightly tweaked appearance (new icons, fonts… but nothing major).
* GUI: added deployment descriptor export from job definition.
* GUI: added an experimental graphic visualization of runs.
* GUI: added option to filter job instances by session ID.

Deprecated
+++++++++++++++

No new entries - same list as for 2.0.x.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs).
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.


2.1.0
*************

Release goal
++++++++++++++++

This release aimed at increasing compatibility with various development ecosystems, chief of which Docker and newer Java versions. Just run `docker run -it --rm -p 1789:1789 enioka/jqm` and go to http://localhost:1789 !

It is a simple upgrade with no breaking change.

Major changes
++++++++++++++++++++++++++++

* Docker compatibility. Official images (Linux Alpine & Windows Nano) are released on the Docker Hub at https://hub.docker.com/r/enioka/jqm/ and are usable for many development and production scenarios.
  Read the documentation on the Docker Hub for more details - this is the pièce de résistance of the release.
* Java 9 and 10 compatibility. Note that Java 6 & 7 are still supported, but also still deprecated and will be removed in the next version.
  * Note that using the WS client will require to change the Jersey dependencies to newer one on Java 9+, as the older Java 6 compatible libraries used by default are not compatible with 9+.
* Oracle compatibility is back.
* Engine: on Java >= 7, the job instance class loader are now closed. On Windows, this means no more file locks remaining after run and therefore job jars are now hot swap-able.

Minor changes
++++++++++++++++++++++++++++

* Engine: better db failure handling on MySQL and Oracle.
* Engine: will now wait for the database to be available on startup, allowing easier startup sequences.
* Engine: drivers and other libraries can now be placed in sub-folders of the "ext" directory (used to be: only at the root of ext).
* Client API: can now switch scheduled job instances from one queue to another, and cancel them.
* Simple API: new easier health check by an HTTP GET (equivalent to calling JMX bean AreAllPollersPolling).
* CLI: added possibility to apply a node template to a given node, allowing it to poll specific queues and other parameters.

Deprecated
+++++++++++++++

No new entries - same list as for 2.0.x.

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs)
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.


2.0.0
*************

Release goal
++++++++++++++++

We are excited to announce the release of JQM 2.0.0. This release is the first of the 2.x series. It is at core a major refactoring of the 1.4 code, which has enabled a few big new features and will allow many more in future versions.

Important note: Oracle support is not present in the initial release. It will be added again in the next release.

Major changes
++++++++++++++++++++++++++++

Better integration with big frameworks:

* More class loading options: it is now possible to specify CL options on transient CL.
* New "starting job instance" event which can be used in user-provided handlers.
* New Spring context management, using the aforementioned event. JQM can now be a fully-fledged Spring container!

Client APIs:

* Many new client APIs to modify job instances.
* Running job instances can now be paused (in addition to being killed).
* New client APIs on queues : pause a queue, resume it…
* New client API to enqueue an instance in a frozen state (and unfreeze it).
* Queues, which used to be purely FIFO, can now use an optional priority parameter. This priority is also translated in Thread priority (the CPU quota for the job instance).

Performances:

* All but one explicit database locks have been eliminated. This means greater JQM cluster scalability and performance.
* Less memory usage. JQM 1.4 was about 40MN idle, 2.0 is 25MB.
* Startup time is now below one second without web services
* Far less libraries used, including in the tester module. (this includes removing Hibernate - JQM does not need an ORM anymore).

Administration:

* New integrated cron-like scheduler - no need anymore for a scheduler in simple cases.
* Beginning with the next version, upgrade scripts are provided when the database schema changes.
* Support for DB2 databases (v 10.5+).

Minor additions
++++++++++++++++++++

* All components: it is now possible to prefix the name of the database tables.
* All components: no more log4j in the different modules - purely slf4j-api.
* Engine: better external launch logs.
* JDBC client API: no need anymore to specify the datasource name to use the Tomcat hack.
* WS client API: lots of reliability fixes and better logging both on client and server side.

Breaking changes
+++++++++++++++++++

As the semantic versioning designation entails, this version contains a few breaking changes. However, it should be noted that the code API (the Java interfaces) themselves have no breaking changes from version 1.4, so impact should be minimal - most changes are behind the scenes, and have consequences for the administrators only.

The breaking changes are:

* The client API implementation named "jqm-api-hibernate" has been replaced by the "jqm-api-jdbc" implementation (with a Maven redirection). The parameters have changed. If you were not using specific parameter (like a specific datasource JNDI name) it should be transparent, as defaults are the same.
* When using the client API, note that validation of the parameters is now stricter (this means failures now occur earlier). It may mean that a JqmInvalidRequestException is now thrown instead of a JqmClientException. If you were catching JqmException,  it has no impact as it is the mother class of the two other.
* The JSF sample has been dropped (it was a demonstration of using the full client API in the context of a JSF2/PrimeFaces web application). Users may still look at the sample in version 1.4, as the API used have not changed. This was done because we do not want anyone to believe we encourage to use JSF for creating user interfaces with JQM.
* Web API user login is now case sensitive, as it should always have been.
* Then "mavenRepo" global parameter cannot be specified multiple times anymore. It now takes a list (comma separated) instead. All global parameters keys are now unique.
* Class loading options are no more given per job definition, but have a declaration of their own. This allows for a more consistent configuration, and should reduce confusion over how to configure class loaders. This impacts the deployment descriptor XML (XSD change).
* For those using the client API Webservice implementation, note that the system properties com.enioka.ws.url has been renamed com.enioka.jqm.ws.url, making it consistent with all the other properties.
* Killed jobs now consistently report as CRASHED. KILLED is no longer a job status, as instructions to running jobs are now handled properly outside the status of the job instance.

Also, a few changes may be breaking for those who were doing explicitly forbidden things, as a lot of internals have changed.

* The database schema has changed a lot. This was never an official API (and likely won't ever be one), but we know a few users were directly making changes in the database so we are listing it here.
* As a consequence the Java classes used to map the database have changed (or disappeared altogether). Same remark: was not an API.
* If you were using an unsupported database, it is it will very likely not work anymore - JQM has dropped using an ORM and therefore does not benefit from the abstraction it provided anymore. Supported databases (HSQLDB, Oracle, MySQL, PostgreSQL, DB2) of course continue to work.


Deprecated
+++++++++++++++

* The Maven artifact named "jqm-api-client-hibernate" has been removed, and replaced by a redirection to the jqm-api-cient-jdbc" artifact. The redirection will be removed in a future release.
* JqmClient.resumeJob is deprecated in favor of the strictly equivalent resumeQueuedJob (to avoid confusion between the different pause/resume verbs)
* Java 6 & 7, which are no longer supported, are considered deprecated in this release. Support for these versions will be removed in the next major version. The 2.x release is the last JQM version to fully support Java 6 & 7.


1.4.1
*************

Release goal
++++++++++++++++++

This is a feature release aiming at giving more control over the class loaders used by the engine.

Many other features are also included, see details below.

Upgrade notes
+++++++++++++++++++

All API changes are backward compatible: 1.3.x APIs will work with 1.4.1 engines.
However, everyone is strongly encouraged to upgrade to the latest version.

There are database structure modifications in this release, so the standard upgrade path must be used (with database drop).

Major
+++++++++++++++++

* Engine: added possibility (at job definition level) to share non-transient class loader with other jobs instances (created from the same job definition or from other job definitions). Default behaviour is still to use one isolated transient class loader per launch.
* Engine: added possibility (at job definition level) to use a child first or parent first class loader.
* Engine: added possibility (at job definition level) to trace the classes loaded by a job instance.
* Engine: added possibility (at job definition level) to hide classes from a job.
* Engine: added new "Maven" type of job - this type is fetched directly from a Maven repository without any need for local deployment.
* Engine: MySQL is now fully supported without reserves, and do not need a startup script anymore.
* GUI: updated to expose the new CL options.
* GUI: major frameworks upgrade - it should be more reactive.
* CLI: added option to export job definition XML (the deployment descriptor). This should help developers to create and maintain it.
* Dev API: added a helper class to embed a full JQM node in the JUnit tests of payloads.

Minor
++++++++++++++++

* Query API: better handling of pagination.
* Client API: on enqueue, the job instance creation date now comes from the DB to avoid issues with time differences between servers.
* CLI: can now specify a port when creating a node.
* CLI: fixed 'root' account creation which was not in the right profile.
* GUI: added favicon to prevent browser warnings.
* Documentation: clarified some notions.
* Test: the 'send mail on completion' function is now correctly tested.
* Test: added testing on OpenJDK 8.


1.3.6
************

Release goal
++++++++++++++++++

Maintenance release with a few optimizations concerning the client API.

Upgrade notes
+++++++++++++++++++

All API changes are backward compatible: 1.2.x and 1.3.x APIs will work with 1.3.6 engines.
However, everyone is strongly encouraged to upgrade to the latest version.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major
+++++++++++++++++

* Engine: a new JMX counter has been added so as to detect jobs longer than desired (a parameter set in the job definition).
* Engine: added an option to create an additional log file containing all the logs of all jobs. This should ease job log parsing by monitoring tools.
* Client API: extended QUery API results so as to return all the keywords (those set in the job definition and those set at enqueue time).
* Client API & Engine API can now cohabit inside a payload for the rare cases when the engine API is not enough.

Minor
++++++++++++++++

* Client API: the job definition XSD is now included inside the jqm-api artifact to ease validation by payload developers.
* Client API: enqueue method should now run faster with less memory consumed.
* Client API: fixed a very rare race condition in file retrieval methods when WS authentication is enabled.
* Test: migrated to SonarQube+Jacoco & added necessary variables.

1.3.5
************

Release goal
++++++++++++++++++

Maintenance release for the integration scripts (jqm.sh and jqm.ps1).

Upgrade notes
+++++++++++++++++++

No API change (APIs version 1.3.5 are the same as version 1.3.3). 1.2.x and 1.3.x APIs will work with 1.3.4 engines.
However, everyone is strongly encouraged to upgrade to the latest version.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major
+++++++++++++++++

Nothing.

Minor
++++++++++++++++

* Scripts: The automatic kill on OutOfMemoryError now works on more Linux variants and on Windows.
* Scripts: JAVA_OPTS is now used in the Linux script in all commands (used to be used only on startup commands).
* Engine: fixed a case that had jobs with end date < start date (now everything uses the time of the central DB).
* Engine: better error message on Job Definition XML import error.
* Simplified Travis builds.

1.3.4
************

Release goal
++++++++++++++++++

Maintenance release.

Upgrade notes
+++++++++++++++++++

No API change (APIs version 1.3.4 are the same as version 1.3.3). 1.2.x and 1.3.x APIs will work with 1.3.4 engines. However, everyone is strongly encouraged to upgrade to the latest version.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major
+++++++++++++++++

* Engine: in some situations, highlander job execution requests could clog a queue. This has been fixed.

Minor
++++++++++++++++

* Engine: A nagging transaction bug that only showed up in automated Travis builds has finally been squashed.
* GUI: double-clicking on "next page" in history screen will no longer open a detail window.
* GUI: a regression from 1.3.3 has been fixed - pagination no longer worked in history screen. (the refresh button had to be pressed after clicking the next page button)
* Test: Selenium is no longer used in the automated build.

1.3.3
************

Release goal
++++++++++++++++++

Maintenance release.

Upgrade notes
+++++++++++++++++++

All APIs have been upgraded and **do not contain any breaking change**. 1.2.x and 1.3.x APIs will work with 1.3.3 engines. However, everyone is strongly encouraged to upgrade.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major
+++++++++++++++++

* Admin UI: the history page was enhanced with more filters including date filters.
* Engine: the Unix/Linux startup script was modified so as to kill automatically the engine when an OutOfMemoryError occurs. This can be overridden with environment variables.

Minor
++++++++++++++++

* CLI: XML schema of deployment descriptors is now validated on installations (was disabled previously due to issues on IBM J9 JVM).
* Client API: files downloaded are now briefly stored in the system temp directory instead of a subdirectory. This makes it easier to have multiple JQM engines running with different accounts on the same server.
* Client API: can now filter by node name.
* Engine: highlander status is now correctly archived in the history table (used to be always false).

1.3.2
************

Release goal
++++++++++++++++++

Maintenance release.

Upgrade notes
+++++++++++++++++++

All APIs have been upgraded and **do not contain any breaking change**. 1.2.1 & 1.2.2 and 1.3.1 apis will work with 1.3.2 engines. However, as 1.2.2 contains fixes and 1.3.1 new functionalities, everyone is strongly encouraged to upgrade.

No database modification in this release - upgrade can be done by simply replacing engine files.

Major
+++++++++++++++++

Nothing.

Minor
++++++++++++++++

* Engine: added a JDBC connection leak hunter to prevent some leak cases
* CLI: added a CLI option to modify an administration JQM user
* GUI: fixed randomly hidden JNDI resource parameters
* Client API: fixed hedge case in which a job instance may not be found by getJob()
* Providers: fixed print job name and added option to specify requesting user name


1.3.1
************

Release goal
++++++++++++++++++

This release had one goal: reducing the need for engine restart. Other administration usability tweaks are also included.

Upgrade notes
+++++++++++++++++++

All APIs have been upgraded and **do not contain any breaking change**. 1.2.1 & 1.2.2 apis will work with 1.3.1 engines. However, as 1.2.2 contains fixes and 1.3.1 new functionalities, everyone is strongly encouraged to upgrade.

Database must be rebuilt for version 1.3.1, this means History purge.

Major
+++++++++++++++++

* Engine: will automatically reload some parameters when they change, reducing the need for engine restarts
* Engine: now resists better database failures
* Engine API: shouldKill method is now throttled, reducing the database hammering (as this method is called by all other methods)
* Admin API: added a method to retrieve the engine logs
* Client API & GUI: can now download files created by a job instance even if it has not finished yet

Minor
++++++++++++++++

* Engine: added sample purge job
* GUI: added an online log viewer for job instance logs (no need to download log files anymore)
* GUI: added an online log viewer for engine logs (which were not retrievable through the GUI before)
* GUI: allowed column resize on History panel
* GUI: added an option to view only KO job instances
* Engine: small code refactor


1.2.2
************

Release goal
++++++++++++++++++

This is a maintenance release, containing mostly bugfixes and very few new features that could not be included in the previous
version (mostly administration GUI tweaks).

Upgrade notes
+++++++++++++++++++

All APIs have been upgraded and **do not contain any breaking change**. 1.2.1 apis will work with 1.2.2 engines. However, as 1.2.2 contains fixes, everyone is strongly encouraged to upgrade.

Database must be rebuilt for version 1.2.2, this means History purge.

Major
+++++++++++++++++

* Engine: can now resist a temporary database failure

Minor
++++++++++++++++

* Engine: access log now logs failed authentications
* Engine: various minor bugfix in extreme performance scenarios
* Engine: there is now one log file per node
* Client API: various fixes
* Client API: now support retrieval of running job instance logs
* GUI: various minor improvements
* CLI: jobdef reimport fixes
* Tests: major refactoring with 3x less Maven artifacts

1.2.1
************

Release goal
++++++++++++++++++

The main goal of this release was to simplify the use of JQM. First for people who dislike command line interfaces, by adding a graphical user interface both for administration and for daily use (enqueue, check job status, etc). Second, for payload developers by adding a few improvements concerning testing and reporting.

Upgrade notes
+++++++++++++++++++

All APIs have been upgraded and **do not contain any breaking change**. Please note that the only version that will work with engine and database in version 1.2.1 is API version 1.2.1: upgrade is compulsory.

Database must be rebuilt for version 1.2.1, this means History purge.

Major
+++++++++++++++++

* Client API: Added a fluid version of the JobRequest API
* GUI: Added an administration web console (present in the standard package but disabled by default)
* All APIs: Added an authentication system for all web services, with an RBAC back-end and compatible with HTTP authentication as well as SSL certificate authentication
* Tests: Added a payload unit tester
* General: Added mail session JNDI resource type

Minor
++++++++++++++++

* Client API: Client APIs file retrieval will now set a file name hint inside an attachment header
* Client API: Added an IN option for applicationName in Query API
* Client API: Query API optimization
* Engine: Unix/Linux launch script is now more complete and robust (restart works!)
* Engine: JAVA_OPTS environment variable is now used by the engine launch script
* Engine: Added special "serverName" JNDI String resource
* Engine: All automatic messages (was enqueued, has begun...) were removed as they provided no information that wasn't already available
* Engine: In case of crash, a job instance now creates a message containing "Status changed: CRASHED due to " + first characters of the stacktrace
* Engine: Log levels and content were slightly reviewed (e.g.: stacktrace of a failing payload is now INFO instead of DEBUG)
* Engine API: Added more methods to the engine API (JobManager)
* Tests: Refactored all engine tests
* Documentation: clarified class loading structure
* Documentation: general update. Please read the doc. Thanks!
* General: Jobs can now easily be disabled

1.1.6
***********

Release goal
++++++++++++++++++

This release was aimed at making JQM easier to integrate in production environments, with new features like
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

Please read the rest of the release notes and check the updated documentation at https://github.com/enioka-Haute-Couture/jqm/blob/master/doc/index.md

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


