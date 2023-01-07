Testing
###############

JQM is tested through an series of automated JUnit tests. These tests are usage-oriented (*integration tests*)
rather than unit oriented. This means: every single functionality of JQM must have (at least) one automated test
that involves running a job inside a full engine.

Automated builds
***********************

Travis
++++++++

The project has a public CI server on http://travis-ci.org/enioka/jqm.

Selenium
++++++++++++++

The project has a public Selenium server at https://saucelabs.com/u/marcanpilami

Tests
************

Standard tests
+++++++++++++++++++++

These are the tests that **should always be run before any commit**. Any failure fails the build.

They are run though Maven (mvn clean install test) and should be able to run without any specific configuration.
They are always run by the CI.

Standard tests on other databases
+++++++++++++++++++++++++++++++++++

The same tests can be run on another database (default is an HSQLDB database created by the test itself). The CI does run these tests too.

In this case the database must be created by the user (or the CI server) before starting the tests with Maven. The database is selected by setting the environment variable `DB`.

As a helper, these Docker commands may be used to create the test database on the developer computer:

* `docker run -it --rm --name mysql -e MYSQL_ROOT_PASSWORD=jqm -e MYSQL_DATABASE=jqm -e MYSQL_USER=jqm -e MYSQL_PASSWORD=jqm -p 3306:3306 mysql:5`
* `docker run -it --rm --name mysql -e MYSQL_ROOT_PASSWORD=jqm -e MYSQL_DATABASE=jqm -e MYSQL_USER=jqm -e MYSQL_PASSWORD=jqm -p 3306:3306 mariadb:10.6`
* `docker run -it --rm --name psql -e POSTGRES_PASSWORD=jqm -e "POSTGRES_USER=jqm" -e "POSTGRES_DB=jqm" -e "POSTGRES_HOST_AUTH_METHOD=password" -p 5432:5432 postgres:15-bullseye`
* `docker run -it --rm --name oracle -e DB_SID=JQM -e ORACLE_SID=JQM -e ORACLE_PWD=jqm -e ORACLE_EDITION=standard -p 1521:1521 container-registry.oracle.com/database/enterprise:12.2.0.1-slim`
  * followed by `docker exec oracle /bin/bash -c ". /home/oracle/.bashrc ; printf 'alter session set \"_ORACLE_SCRIPT\"=true; \n CREATE USER JQM IDENTIFIED BY jqm DEFAULT TABLESPACE SYSAUX QUOTA UNLIMITED ON SYSAUX ACCOUNT UNLOCK;\n GRANT CONNECT, RESOURCE, ALTER SYSTEM TO JQM;\n GRANT SELECT ON v_\$session TO JQM;\n ALTER SYSTEM SET OPEN_CURSORS=9999 SCOPE=BOTH;\n ' | sqlplus / as sysdba";`
* `docker run --privileged=true -it --rm --name db2 -p 50000:50000 -e "LICENSE=accept" -e "DB2INST1_PASSWORD=superpassword" -e "DBNAME=jqm" -e "ARCHIVE_LOGS=false" ibmcom/db2`
  * followed by `docker exec db2 /bin/bash -c 'useradd -G db2iadm1 jqm; echo "jqm:jqm" | chpasswd'`
  * followed by `docker exec --user db2inst1 db2 /bin/bash -c 'export DB2INSTANCE=db2inst1; printf "connect to jqm\n create schema jqm AUTHORIZATION jqm\n grant execute on function SYSPROC.MON_GET_CONNECTION to user jqm;\n" | /opt/ibm/db2/V11.5/bin/db2 -v -'`

Note that for Oracle & DB2 the helpers may not be up to date, refer to the CI configuration file for the latest test configuration. ALso note that additional permissions are given to the JQM user to allow killing sessions in some tests - this is not needed for production.

Selenium tests
++++++++++++++++++++

The UI also has a few dedicated tests that run inside Selenium. To avoid configuration and ease test reproducibility,
we use Sauce Labs' cloud Selenium. The Travis build uses the maintainer's account.

As this account is personal, its credentials are not included inside the build descriptor and these tests are disabled by default
(they are inside a specific Maven profile). In order to use them, a free account on Sauce Labs is required, as well as
putting this inside Maven's settings.xml::

    <profile>
        <id>selenium</id>
        <activation>
            <activeByDefault>false</activeByDefault>
        </activation>
        <properties>
            <SAUCE_USERNAME>YOUR_USER_NAME</SAUCE_USERNAME>
            <SAUCE_ACCESS_KEY>YOUR_ACCESS_KEY</SAUCE_ACCESS_KEY>
            <SAUCE_URL>localhost:4445/wd/hub</SAUCE_URL>
        </properties>
    </profile>

Moreover, as the web application actually runs on the developer's computer and not on the Selenium server,
a tunnel must be activated, using `Sauce Connect <https://docs.saucelabs.com/reference/sauce-connect/>`_. The URL above reflects this.

.. note:: the Sauce Connect Maven plugin was not included in the pom, because it implies starting
    and stopping the tunnel on each test run - and this is a very long process. It's easier on the nerves to simply start the
    tunnel and forget it.

Finally, running the tests is simply done by going inside the jqm-wstst project and running the classic "mvn test -Pselenium" command.
Obviously, if in the settings.xml file the profile was marked as active by default, the -P option can be omitted.

Web-services dev and tests
++++++++++++++++++++++++++++++++

The admin GUI as well as all the web services are inside the jqm-ws project.

To develop and test this project in Eclipse, one needs a fully working JQM database. The easiest way to get it is to
install a local node following the documentation. Then enable the admin GUI & create the root account with the command line. Do not enable SSL.

The node can be stopped - it won't be needed anymore.

Then, inside Eclipse, install a Tomcat 7. (not 8 - this would require Java 7).

The project contains a context.xml file inside src/test/webapp/META-INF that must be updated with the connection string to your database.
Please do not commit these modifications.

.. warning:: you must ensure the src/test/webapp/META-INF directory is inside the "deployment assembly" inside Eclipse's project properties.

Then the database driver to the the lib directory of Tomcat

Everything is ready - the project can now be "run on server". The URL will be http://localhost:8080/jqm-ws
