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
	Invoke-RestMethod https://github.com/enioka/jqm/archive/jqm-$JQM_VERSION.zip -OutFile jqm.zip
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

        JQM_ROOT = "/opt/jqm"
        JQM_VERSION = "1.1.6"
        mkdir -p $JQM_ROOT; cd $JQM_ROOT
        wget https://github.com/enioka/jqm/archive/jqm-$JQM_VERSION.taz.gz
        tar xvf jqm-$JQM_VERSION.taz.gz
        rm jqm-$JQM_VERSION.taz.gz
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
It contains by default sample configuration for Oracle, HSQLDB and MySQL which are the three supported databases. (HSQLDB is not supported
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


Global configuration
**********************

When the first node is created inside a database, some parameters are automatically created. You may want to change them using your prefered 
database editing tool. See :doc:`parameters` for this.

JNDI configuration
*******************

See :doc:`/jobs/resources`.
