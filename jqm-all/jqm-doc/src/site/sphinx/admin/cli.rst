Command Line Interface (CLI)
#################################

.. highlight:: bash

The CLI has commands to:

* create a new node inside the cluster
* export and import job definitions (deployment descriptors)
* import queues
* import cluster configuration
* create a new execution request
* get the status of an execution request
* create or reset user accounts, including root
* apply a configuration template to a node
* enable or disable web APIs, using TLS or not
* update the database

To get all details, use ``java -jar jqm.jar -h`` (or ``--help``).

The most useful options are exposed in a more user friendly way with the
script jqm.sh (or jqm.ps1 under Windows).

.. warning:: a few options exist which are not listed here. They are internal and are not part the API (may change without notice, etc).

.. note:: **Common options available for all commands:**

   * ``-s, --settings``: Path (inside JQM_ROOT/conf/) to JQM properties file. Default is resources.xml.
   * ``-h, --help, /?, -?``: Display command help.

Common usage examples
*********************

Node management
===============

.. code-block:: bash

    # Create a new node with custom port and interface
    java -jar jqm.jar New-Node -n node01 -p 1789 -i 0.0.0.0

    # Get how many nodes are declared inside the central configuration
    java -jar jqm.jar Get-NodeCount

    # Start a node
    java -jar jqm.jar Start-Node -n node01

    # Copy configuration from one node to another
    java -jar jqm.jar Install-NodeTemplate -t template-node -n node02

Job definitions
===============

.. code-block:: bash

    # Import a single job definition
    java -jar jqm.jar Import-JobDef -f job-definitions.xml

    # Import multiple files
    java -jar jqm.jar Import-JobDef -f job1.xml -f job2.xml -f job3.xml

    # Import all XML files from a directory
    java -jar jqm.jar Import-JobDef -f /path/to/job-definitions/

    # Export all job definitions for backup
    java -jar jqm.jar Export-JobDef -f backup-$(date +%Y%m%d).xml

Queue management
================

.. code-block:: bash

    # Import queue mappings
    java -jar jqm.jar Import-Queue -f queue-mappings.xml

    # Export queues for backup
    java -jar jqm.jar Export-Queue -f queues-backup.xml

Cluster configuration
=====================

.. code-block:: bash

    # Import cluster infrastructure configuration
    java -jar jqm.jar Import-ClusterConfiguration -f cluster-config.xml

Job execution
=============

.. code-block:: bash

    # Create a new job execution request
    java -jar jqm.jar New-Ji -a DemoEcho

    # Check job status
    java -jar jqm.jar Get-JiStatus -i 12345

User management
===============

.. code-block:: bash

    # Reset root password
    java -jar jqm.jar Reset-Root -p SuperPassword

    # Create a user with a single role
    java -jar jqm.jar Reset-User -l root -p test -r administrator

    # Create a user with multiple roles
    java -jar jqm.jar Reset-User -l root -p test -r administrator -r client

    # Fetch all roles defined in central configuration
    java -jar jqm.jar Get-Role

Web configuration
=================

.. code-block:: bash

    # Enable HTTP web GUI
    java -jar jqm.jar Set-WebConfiguration -c ENABLE_HTTP_GUI

    # Enable TLS/HTTPS
    java -jar jqm.jar Set-WebConfiguration -c ENABLE_TLS

    # Disable all web services
    java -jar jqm.jar Set-WebConfiguration -c DISABLE_ALL

Database management
===================

.. code-block:: bash

    # Updates the database schema
    java -jar jqm.jar Update-Schema

    # Test if the database schema is up to date
    java -jar jqm.jar Test-Schema

    # Displays the database schema SQL commands needed to make it up to date
    java -jar jqm.jar Get-SchemaSql

General commands
================

.. code-block:: bash

    # Fetch the version of the command line tool
    java -jar jqm.jar Get-Version
