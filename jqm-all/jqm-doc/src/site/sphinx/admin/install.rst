Installation
###################

Please follow the paragraph specific to your OS and then go through the common chapter.

.. highlight:: bash

Binary install
***************

Windows
------------------

Prerequisites:

* A directory where JQM will be installed, named JQM_ROOT afterwards
* An admin account (for installation only)
* A service account with minimal permissions: LOGON AS SERVICE + full permissions on JQM_ROOT.

The following script will download and copy the binaries (adapt the first two lines). Run it with admin rights. ::

	$JQM_ROOT = "C:\TEMP\jqm"
	$JQM_VERSION = "${project.version}"
	mkdir -Force $JQM_ROOT; cd $JQM_ROOT
	Invoke-RestMethod https://github.com/enioka/jqm/releases/download/jqm-all-$JQM_VERSION/jqm-engine-$JQM_VERSION.zip -OutFile jqm.zip
	$shell = new-object -com shell.application
	$zip = $shell.NameSpace((Resolve-Path .\jqm.zip).Path)
	foreach($item in $zip.items()) { $shell.Namespace($JQM_ROOT).copyhere($item) }
	rm jqm.zip; mv jqm*/* .

Then create a service (adapt user and password)::

	./jqm.ps1 createnode
	./jqm.ps1 -ServiceUser marsu -ServicePassword marsu
	./jqm.ps1 start

And it's done, a JQM service node is now running.

Linux / Unix
-------------

Prerequisites:

* A directory where JQM will be installed, named JQM_ROOT afterwards
* A user account with read/write rights on JQM_ROOT

The following script will download and copy the binaries (adapt the first two lines). ::

        JQM_ROOT="/opt/jqm"
        JQM_VERSION="1.1.6"
        mkdir -p $JQM_ROOT; cd $JQM_ROOT
        wget https://github.com/enioka/jqm/releases/download/jqm-all-$JQM_VERSION/jqm-engine-$JQM_VERSION.tar.gz
        tar xvf jqm-engine-$JQM_VERSION.tar.gz
        rm jqm-engine-$JQM_VERSION.tar.gz
        mv jqm-*/* .
        rmdir jqm-*
        

Then use the provided jqm.sh script::

        jqm.sh createnode
        jqm.sh start

And it's done, a JQM service node is now running.


Testing
-------------

The following will import the definition of three test jobs included in the distribution, then launch one. (no admin rights necessary nor variables).

Windows::

	./jqm.ps1 stop  ## Default database is a single file... that is locked by the engine if started
	./jqm.ps1 allxml  # This will import all the test job definitions
	./jqm.ps1 -Enqueue DemoEcho
	./jqm.ps1 start

Linux / Unix::

	./jqm.sh stop  ## Default database is a single file... that is locked by the engine if started
	./jqm.sh allxml  # This will import all the test job definitions
	./jqm.sh Enqueue DemoEcho
	./jqm.sh start


Check the JQM_ROOT/logs directory: two log files (stdout, stderr) should have been created (and contain no errors). Success!


Database configuration
************************

The node created in the previous step has serious drawbacks:

* it uses an HSQLDB database with a local file that can be only used by a single process
* it cannot be used in a network as nodes communicate through the database
* General low performances and persistence issues inherent to HSQLDB

Just edit JQM_ROOT/conf/resources.xml file to reference your own database and delete or comment JQM_ROOT/conf/db.properties.
It contains by default sample configuration for Oracle, PostgreSQL, HSQLDB and MySQL which are the three supported databases. (HSQLDB is not supported
in production environments)

.. note:: the database is intended to be shared between all JQM nodes - you should not create a schema/database per node.

Afterwards, place your JDBC driver inside the "ext" directory.

Then stop the service.

Windows::

	./jqm.ps1 stop
	./jqm.ps1 createnode
	./jqm.ps1 start

Linux / Unix::

	./jqm.sh stop
	./jqm.sh createnode
	./jqm.sh start

Then, test again (assuming this is not HSQLDB in file mode anymore, and therefore that there is no need to stop the engine).

Windows::

	./jqm.ps1 allxml
	./jqm.ps1 -Enqueue DemoEcho

Linux / Unix::

	./jqm.sh allxml
	./jqm.sh enqueue DemoEcho

Database support
++++++++++++++++++++

Oracle
----------

Oracle 10gR2 & 11gR2 are supported. No specific configuration is required in JQM: no options inside jqm.properties (or absent file). No specific database configuration is required.


PostgreSQL
--------------

PostgreSQL 9 is supported (tested with PostgreSQL 9.3). It is the recommanded open source database to work with JQM.
No specific configuration is required in JQM: no options inside jqm.properties (or absent file). No specific database configuration is required. 

Here's a quickstart to setup a test database. As postgres user::

        $ psql
        postgres=# create database jqm template template1;
        CREATE DATABASE
        postgres=# create user jqm with password 'jqm';
        CREATE ROLE
        postgres=# grant all privileges on database jqm to jqm;
        GRANT
        postgres=# grant all privileges on database jqm to jqm;
        GRANT


MySQL
-----------

MySQL 5.x is supported with InnoDB (the default). No specific configuration is required in JQM: no options inside jqm.properties (or absent file).

With InnoDB, a `startup script <http://dev.mysql.com/doc/refman/5.6/en/server-options.html#option_mysqld_init-file>`_ 
must be used to reset an auto-increment inside the database (InnoDB behaviour messes up with
JQM handling of keys, as it resets increment seeds with MAX(ID) on each startup even on empty tables). 
The idea is to initialize the auto increment for the JobInstance table at the same level as for the History table.
An example of script is (adapt the db name & path)::

	select concat('ALTER TABLE jqm.JobInstance AUTO_INCREMENT = ',max(ID)+1,';') as alter_stmt into outfile '/tmp/alter_JI_auto_increment.sql' from jqm.History;
	\. /tmp/alter_JI_auto_increment.sql
	\! rm -f /tmp/alter_JI_auto_increment.sql


HSQLDB
----------

HSQLDB 2.3.x is supported in test environments only.

As Hibernate support of HSQLDB has a bug, the jqm.properties file must contain the following line::

	hibernate.dialect=com.enioka.jqm.tools.HSQLDialect7479
	
No specific HSQLDB configuration is required. Please note that if using a file database, HSQLDB prevents multiple processes from accessing it
so it will cause issues for creating multi node environments.


Global configuration
**********************

When the first node is created inside a database, some parameters are automatically created. You may want to change them using your prefered 
database editing tool. See :doc:`parameters` for this.

JNDI configuration
*******************

See :doc:`/jobs/resources`.
