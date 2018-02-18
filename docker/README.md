# JQM Docker image

This is the official image for JQM (Job Queue Manager). It is available on Windows Nano 1709+ and Linux under the Apache Public License, v2.


## 1. What is JQM

JQM is an open source Java batch manager. 

It takes standard Java code (which does not need to be specifically tailored for JQM) and, when receiving an execution request, will run it asynchronously, taking care of everything that would otherwise be boilerplate code with no added value whatsoever: configuring libraries, logs, throttling processes, handling priorities between different classes of jobs, distributing the load over multiple servers, distributing the files created by the jobs, and much more… 

Basically, it is a very lightweight application server specifically tailored for making it easier to run Java batch jobs.

Full product description and documentation is available at [read the docs](https://jqm.readthedocs.io), code is on [GitHub](https://github.com/enioka/jqm). Maintainer (Enioka company) website (in French) is at http://www.enioka.com.


## 2. Tags

* latest: the highest stable release (not the most recent one, as the most recent one may be a patch on an older major version)
* nightly: a build of the master branch, with no guarantees nor support.
* specific release tags: each release has its own tag with the release version, like 2.1.0.
* major release tags: this tracks major (first digit) releases. Minor releases for a given major release (e.g. minor 2.2.x for major 2.x) have no breaking changes and do have an upgrade path, so this should be the default type of tag to use for most deployments.

The first tag is 2.1.0, previous versions have no official images.


## 3. Main usage scenarios

### 3.1 Simple test run

Run: `docker run -it --rm -p 1789:1789 enioka/jqm`

A single-node, fully functional server starts after a few seconds. The web UI is available on the local machine, port 1789.

Use CTRL+C to exit. All changes made to the server are lost on exit.


### 3.2 Developer computer: non production standalone node, with stock image only.

In this scenario, a developer uses his preferred IDE or build tool and the resulting product of his work is automatically made available to a local JQM server.

It uses a ready to use single-node JQM install configured with sensible options for a developer: web-UI is enabled, authentication is disabled, sample jobs are ready to launch. It relies on a self-contained HSQLDB database, so it has no dependencies whatsoever.


Run: `docker run -it -p 1789:1789 -v /path/to/target/build/directory:/jqm/hotdeploy enioka/jqm`

Adapt pathes to your environments (and do not forget C:/ on WIndows). The web UI is made available on the local machine, port 1789.

Configure your build system (Maven, Gradle, Ant...) to copy your job and deployment descriptor (jars and the XML) inside /path/to/target/build/directory/myjob (myjob is a name of your choosing. Do not copy files directly inside the mount directory). Files should disappear after a few seconds, and jobs should appear as ready to run inside the web UI.

Use CTRL+C to exit the node (obviously, running the node in the background then using `docker stop` also works).

> In more details...
> 
> To deploy your batch jobs, copy both the jar and the XML deployment descriptor inside the /jqm/hotdeploy mount.
> * A hotdeploy sub-directory is a `deployment unit`. It contains the deployment descriptor and the jar files referenced by this deployment descriptor.
>   * The name of this sub-directory is important, as subsequent deployments with the same name will replace each other.
>   * Only first-level sub-directories are scanned.
> * The deployment mechanism is triggered by the deployment descriptor.
>   * When copying files into a subdirectory it is therefore preferable to copy the descriptor last.
>   * To avoid using `inotify` which has cross-platform issues, the directories are simply scanned every 30s.
> * There can only be a single deployment descriptor per sub-directory. Directories with multiple XML files are ignored.
> * The jqm.jar.path inside the deployment descriptor is interpreted without its full path (JQM will use its own directory structure). The file name (base name in Unix terms) inside this path is however still very important, as there may be multiple jar files per deployment unit. This allows to reuse the "production" deployment descriptor directly.
> * All files are moved regardless of their nature, respecting existing directory structure. (such as an eventual `lib` directory with libraries, see [the packaging documentation](https://jqm.readthedocs.io/en/latest/jobs/packaging.html#libraries-handling))
> * The files are removed from hotdeploy on success. (sub-directory is emptied, but not removed)
>
> This deployment mechanism is a Docker image specificity, and is **not available on non-Docker deployments**. It is also only available in single-node deployments.

> Note: in this scenario, the database is inside the container. So deleting the container deletes the database and all configuration changes with it.


### 3.3 Redistributable batch test

In this scenario, batch jobs are packaged inside a custom image. The server still uses an internal HSQLDB database. The result is a ready to run server already containing the jobs (which can in turn be triggered manually, or simply be scheduled).

The only thing needed is to copy the batch files (jar(s) and deployment descriptor(s)) inside the job repository, and import the job decsriptor as in any deployment.

An example Dockerfile would be:
```
FROM enioka/jqm

# Copy jars and deployment descriptors inside the job definition repository, as in a normal deployment
COPY buildresult/* C:/jqm/jobs/
RUN java -jar jqm.jar -importjobdef ./jobs
```

The resulting image can then be run just as the base JQM image.


### 3.4 Single node or multiple nodes production deployment

In this scenario, the goal is to have an easy to scale and deploy image with Swarm (or any orchestrator) on a production or production-like environment.

The batch jobs are directly inside a custom image. The driver required to access the external central database is inside /jqm/ext.
So the image builder just has to copy the jar and deployment descriptors inside /jqm/jobs[/subdirectory...].

An example Dockerfile would be:
```
FROM enioka/jqm

ENV JQM_POOL_DRIVER="oracle.jdbc.OracleDriver" \
    JQM_POOL_VALIDATION_QUERY="SELECT SYSDATE FROM DUAL" \
    JAVA_OPTS="-Xms1g -Xmx1g -XX:MaxMetaspaceSize=128m" \
    JQM_NODE_NAME=%COMPUTERNAME%
    JQM_CREATE_NODE_IF_MISSING=1

COPY ojdbc4.jar C:/jqm/ext/
COPY buildtarget/* C:/jqm/jobs/
```

Note the `JQM_CREATE_NODE_IF_MISSING` variable: it tells JQM to create the node (as named by `JQM_NODE_NAME`, here the container name) in the configuration if it does not exists. That way starting the container is enough to add the new node to the cluster. Also, database information should be provided on the command line or in a compose file (which would be used in a swarm anyway). For example:

`docker run -p 1789:1789 --env "JQM_POOL_USER=login" --env "JQM_POOL_PASSWORD=secret" --env "JQM_POOL_CONNSTR=jdbc:oracle:thin:@//oraclehost:1521/MYINSTANCE"`

When using JQM_CREATE_NODE_IF_MISSING, all deployment descriptors inside /jqm/jobs are also imported at node creation time. Also, the database schema is upgraded if needed.

> For production environments, the first thing to do should be to enable security on the web services.

> Rather than building directly upon the enioka/jqm image, it can be useful to build upon the result of the "Redistributable batch test" scenario in many integration pipelines.

## 4. Image reference

* Environment variables
  * JQM_NODE_NAME: name of the node in the configuration. Default is ContainerNode. If using the internal HSQLDB database, a node named ContainerNode is already configured. If using another database, take care to add a node of this name to your configuration (or/and use JQM_CREATE_NODE_IF_MISSING).
  * JQM_POOL_CONNSTR: connection string to the database. Default is an HSQLDB file database inside the container. If using another database, make sure the database driver is inside JQM_ROOT/ext (or subdirectory).
  * JQM_POOL_USER, JQM_POOL_PASSWORD: login and password for your database.
  * JQM_POOL_DRIVER: driver class name. Default is `org.hsqldb.jdbcDriver`.
  * JQM_POOL_VALIDATION_QUERY: the query to run to validate a connection is OK. Default is `SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS`.
  * JQM_POOL_MAX: the maximum number of connections the pool may contain. Default value of 10 should be OK in all cases.
  * JAVA_OPTS: parameters passed as-is to the JVM. Default is: -Xms128m -Xmx512m -XX:MaxMetaspaceSize=128m
  * JQM_CREATE_NODE_IF_MISSING: if set to 1, the node JQM_NODE_NAME is created if it is missing with a default configuration (only polling the default queue). This allows easy swarm scale-out.
  * JQM_ROOT: the JQM installation root. It should not be changed - it exists solely to simplify some scripts.
* Mounts
  * /jqm/ext/drivers: an empty directory inside which JDBC drivers (and actually also other shared libraries) may be placed. Mostly useful for developer-computer deployments, as in other deployment types images are created which can directly include these drivers in /jqm/ext.
  * /jqm/hotdeploy: an empty directory in which to copy your batch jobs. Same remark as above.
* Events
  * ONBUILD: none.
* Ports
  * 1789: main web service and web UI
  * 1790: JMX registry
  * 1791: JMX server
* Health check: none for now (Goal was to have : every minute, the health of the node is checked through the standard JMX node bean. See [the monitoring documentation](https://jqm.readthedocs.io/en/latest/admin/jmx.html) for more details.) - it proved way too costly to recreate a JVM + a JMX connection every minute.
