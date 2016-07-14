Quickstart
###############

.. highlight:: bash

This guide will show how to run a job inside JQM with the strict minimum of operations.
The resulting installation is not suitable for production at all, but perfect for development environments.
It also gives pointers to the general documentation.

Windows
************

Prerequisites:

* A directory where JQM will be installed, named JQM_ROOT afterwards.
* An account with full permissions in JQM_ROOT. Not need for admin or special rights - it just needs to be able to open a PowerShell session.

The following script will download and copy the binaries (adapt the first two lines). ::

	$JQM_ROOT = "C:\TEMP\jqm" ## Change this
	$JQM_VERSION = "1.3.3"  ## Change this
	mkdir -Force $JQM_ROOT; cd $JQM_ROOT
	Invoke-RestMethod https://github.com/enioka/jqm/releases/download/jqm-all-$JQM_VERSION/jqm-$JQM_VERSION.zip -OutFile jqm.zip
	$shell = new-object -com shell.application
	$zip = $shell.NameSpace((Resolve-Path .\jqm.zip).Path)
	foreach($item in $zip.items()) { $shell.Namespace($JQM_ROOT).copyhere($item) }
	rm jqm.zip; mv jqm*/* .

The following script will create a database and reference the test jobs (i.e. :term:`payloads<payload>`) inside a test database::

	./jqm.ps1 createnode # This will create a new node named after the computer name
	./jqm.ps1 allxml  # This will import all the test job definitions
    
The following script will enable the web console with account root/test (do not use this in production!)::

    ./jqm.ps1 enablegui -RootPassword test

The following script will :term:`enqueue` an execution request for one of the test jobs::

	./jqm.ps1 -Enqueue DemoEcho

Finally this will start an engine inside the console.::

	./jqm.ps1 startconsole

Just check the JQM_ROOT/logs directory - a numbered log file should have appeared, containing the log of the test job.

The log inside the console should give you an indication "Jetty has started on port <PORT>". You can now use your preferred browser
to go to localhost:port and browse the administration console. Use Ctrl+C inside the PowerShell console to stop the engine.


Linux / Unix
************

Prerequisites:

* A directory where JQM will be installed, named JQM_ROOT afterwards.
* An account with full permissions in JQM_ROOT. Not need for administrative or special permissions.

The following script will download and install the binaries (adapt the first two lines). ::

        wget  https://github.com/enioka/jqm/releases/download/jqm-all-1.3.3/jqm-1.3.3.tar.gz # For 1.3.3 release. Adapt it to the one you want.
        tar xvf jqm-1.3.3.tar.gz


The following script will create a database and reference the test jobs (i.e. :term:`payloads<payload>`) inside a test database::

        cd jqm-1.3.3
        ./jqm.sh createnode
        ./jqm.sh allxml  # This will import all the test job definitions

The following script will enable the web console with account root/test (do not use this in production!)::

        ./jqm.sh enablegui test
    
The following script will :term:`enqueue` an execution request for one of the test jobs::

        ./jqm.sh enqueue DemoEcho

Finally this will start an engine inside the console.::

        ./jqm.sh startconsole

Just check the JQM_ROOT/logs directory - a numbered log file should have appeared, containing the log of the test job.

Next steps...
**************

.. note:: Congratulations, you've just run your first JQM batch! This batch is simply a jar with a main function doing an echo - a totally
        usual Java JSE program with no extensions whatsoever. If using standard JSE is not enough, just read the :doc:`jobs/index` chapter.

To exit the engine, simply do Ctrl+C or close your console.

*To go further*: engines under Windows should be installed as services. This is easily done and explained in the :doc:`full 
install documentation<admin/install>`. Moreover, this test install is using a very limited (and limiting) database - the full doc also 
explains how to use fully fledged databases.
