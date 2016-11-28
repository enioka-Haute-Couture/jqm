Command Line Interface (CLI)
#################################

.. highlight:: bash

.. versionadded:: 1.1.3
	Once a purely debug feature, JQM now offers a standard CLI for basic operations.

.. program jqm-engine.jar

::

	java -jar jqm-engine.jar -createnode <nodeName> [-port <portNumber>] | -enqueue <applicationname> | -exportallqueues <xmlpath> | -h | -importjobdef <xmlpath> | -importqueuefile <xmlpath> | -startnode <nodeName> | -v

.. option:: -createnode <nodeName>

	create a JQM node of this name (init the database if needed)

.. option:: -port <portNumber>

	port to use for the newly created node

.. option:: -enqueue <applicationname>   
	
	name of the application to launch
 
.. option:: -exportallqueues <xmlpath>
	
	export all queue definitions into an XML file

.. option:: -h, --help
	
	display help

.. option::	-importjobdef <xmlpath>      

	path of the XML configuration file to import
 
.. option:: -importqueuefile <xmlpath>   
	
	import all queue definitions from an XML file

.. option:: -startnode <nodeName>        
	
	name of the JQM node to start

.. option:: -v, --version               
	
	display JQM engine version


.. note:: Common options like start, createnode, importxml etc. can be used with convenience script jqm.sh / jqm.ps1