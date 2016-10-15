Installation
###################

Please follow the paragraph specific to your OS and then go through the common chapter.

.. highlight:: bash

Binary install
***************

Windows
====================

Prerequisites:

* A directory where JQM will be installed, named JQM_ROOT afterwards
* An admin account (for installation only)
* A service account with minimal permissions: LOGON AS SERVICE + full permissions on JQM_ROOT.

The following script will download and copy the binaries (adapt the first two lines). Run it with admin rights. ::

    $JQM_ROOT = "C:\TEMP\jqm"
    $JQM_VERSION = "${project.version}"
    mkdir -Force $JQM_ROOT; cd $JQM_ROOT
    Invoke-RestMethod https://github.com/enioka/jqm/releases/download/jqm-all-$JQM_VERSION/jqm-$JQM_VERSION.zip -OutFile jqm.zip
    # Unzipping is finally coming in POSH 5... so not yet.
    $shell = new-object -com shell.application
    $zip = $shell.NameSpace((Resolve-Path .\jqm.zip).Path)
    foreach($item in $zip.items()) { $shell.Namespace($JQM_ROOT).copyhere($item) }
    rm jqm.zip; mv jqm*/* .

Then create a service (adapt user, password and desired node name)::

    ./jqm.ps1 installservice -ServiceUser marsu -ServicePassword marsu -NodeName $env:COMPUTERNAME
    ./jqm.ps1 start

And it's done, a JQM service node is now running.

Linux / Unix
====================

Prerequisites:

* A directory where JQM will be installed, named JQM_ROOT afterwards
* A group for containing the user which will actually be allowed to run jqm. Default name is jqm. (Debian-like: `sudo groupadd jqm`)
* A user account owning JQM_ROOT (not necessarily root). Default name is jqmadm. (Debian-like: `sudo useradd -m -g jqm jqmadm`)
* A user for running the engine, no specific permissions (and certainly NOT root). Inside the group above. Default name is jqm. (Debian-like: `sudo useradd -m -g jqm jqm`)

The following script will download and copy the binaries (adapt the first two lines). Run as jqmadm ::

    JQM_ROOT="/opt/jqm"
    JQM_VERSION="1.2.2"
    cd $JQM_ROOT
    wget https://github.com/enioka/jqm/releases/download/jqm-all-$JQM_VERSION/jqm-$JQM_VERSION.tar.gz
    tar xvf jqm-*.tar.gz
    rm jqm-*.tar.gz
    mv jqm-*/* .
    rmdir jqm-*
    ./bin/permissions.sh
    ./jqm.sh createnode
    ./jqm.sh start    

And it's done, a JQM node is now running.

As root (optional, only if run as a service)::

    ## Ensure JQM is not running
    ln -s $JQM_ROOT/jqm.sh /etc/init.d/jqm
    chmod 700 /etc/init.d/jqm
    vi /etc/init.d/jqm
    ## Change line 5 to the value of JQM_ROOT (cd /opt/...)
    ## Purge the directory JQM_ROOT/logs
    /etc/init.d/jqm start


Testing
====================

The following will import the definition of some test jobs included in the distribution, then launch one. (no admin rights necessary nor variables).

Windows::

	./jqm.ps1 stop  ## Default database is a single file... that is locked by the engine if started
	./jqm.ps1 allxml  # This will import all the test job definitions
	./jqm.ps1 enqueue -JobDef DemoEcho
	./jqm.ps1 start

Linux / Unix::

	./jqm.sh stop  ## Default database is a single file... that is locked by the engine if started
	./jqm.sh allxml  # This will import all the test job definitions
	./jqm.sh enqueue DemoEcho
	./jqm.sh start


Check the JQM_ROOT/logs directory: two log files (stdout, stderr) should have been created (and contain no errors). Success!

Enabling the web interface
****************************

By default the web interface is disabled. This will enable it (on all network interfaces) and create a user named "root".

The server listens to a random free port shown in the main log. It can be changed later.

Windows
=========

./jqm.ps1 enablegui -RootPassword mypassword

Linux
=========

./jqm.sh enablegui mypassword


Database configuration
************************

The node created in the previous step has serious drawbacks:

* it uses an HSQLDB database with a local file that can be only used by a single process
* it cannot be used in a network as nodes communicate through the database
* General low performances and persistence issues inherent to HSQLDB

Just edit JQM_ROOT/conf/resources.xml file to reference your own database and delete or comment JQM_ROOT/conf/db.properties.
It contains by default sample configuration for Oracle, PostgreSQL, HSQLDB and MySQL which are the four supported databases. (HSQLDB is not supported
in production environments)

.. note:: the database is intended to be shared by all JQM nodes - you should not create a schema/database per node.

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
	./jqm.ps1 enqueue -JobDef DemoEcho

Linux / Unix::

	./jqm.sh allxml
	./jqm.sh enqueue DemoEcho

Database support
====================

Oracle
------------------

Oracle 10gR2 & 11gR2 are supported. No specific configuration is required in JQM: no options inside jqm.properties (or absent file). No specific database configuration is required.


PostgreSQL
------------------

PostgreSQL 9 is supported (tested with PostgreSQL 9.3). It is the recommended open source database to work with JQM.
No specific configuration is required in JQM: no options inside jqm.properties (or absent file). No specific database configuration is required. 

Here's a quickstart to setup a test database. As postgres user::

    $ psql
    postgres=# create database jqm template template1;
    CREATE DATABASE
    postgres=# create user jqm with password 'jqm';
    CREATE ROLE
    postgres=# grant all privileges on database jqm to jqm;
    GRANT


MySQL
------------------

MySQL 5.6+ is supported with InnoDB (the default). No specific configuration is required in JQM: no options inside jqm.properties (or absent file).
    
These commands can be used to setup a database.::

    $ mysql -u root -p
    mysql> create database jqm;
    mysql> grant all privileges on jqm.* to jqm@'%' identified by 'jqm';
    mysql> flush privileges;

.. note:: before version 1.4, a startup script was needed to align sequences between tables on database startup. This is no longer needed and if present, this script should be removed.
    
HSQLDB
------------------

HSQLDB 2.3.x is supported in test environments only.

As Hibernate support of HSQLDB has a bug, the jqm.properties file must contain the following line::

	hibernate.dialect=com.enioka.jqm.tools.HSQLDialect7479
	
No specific HSQLDB configuration is required. Please note that if using a file database, HSQLDB prevents multiple processes from accessing it
so it will cause issues for creating multi node environments.


Global configuration
**********************

When the first node is created inside a database, some parameters are automatically created. You may want to change them using your preferred 
database editing tool or the web console. See :doc:`parameters` for this.

Many users will immediately enable the web administration console in order to easily change this configuration::

    ./jqm.sh enablegui <rootpassword>
    ./jqm.sh restart

The console is then available at http://localhost:xxxxx (where the port is a free port chosen randomly. It is written inside the main log at startup).

JNDI configuration
*******************

See :doc:`/jobs/resources`.
