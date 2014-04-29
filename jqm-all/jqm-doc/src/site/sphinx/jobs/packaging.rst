Packaging
################

JQM is able to load :term:`payloads<payload>` from jar files (in case your code is actually inside a war, it is possible to simply rename the file), which gives
a clear guidance as to how the code should be packaged. However, there are also other elements that JQM needs to run the code.

For exemple, when a client requests the :term:`payload` to run, it must be able to refer to the code unambiguously, therefore JQM must know
an "application name" corresponding to the code. This name, with other data, is to be put inside an XML file that will be imported
by JQM. A code can only run if its XML has been imported (or the corresponding values manually entered inside the database, a fully 
unsupported alternative way to do it).

Should some terms prove to be obscure, please refer to the :doc:`../glossary`.

Libraries handling
**************************

JQM itself is hidden from the payloads - payloads cannot see any of its internal classes and resources. So JQM itself does not provide anything to 
payloads in terms of libraries (with the exception of libraries explicitly added to the ext directory, see below).

But there are two ways, each with two variants, to make sure that required libraires are present at runtime.

.. note:: All the four variants are exclusive. **Only one libray source it used at the same time**.

Maven POM
++++++++++++++++

A jar created with Maven always contains the pom.xml hidden inside META-INF. JQM will extract it, read it and download the dependencies,
putting them on the payload's class path. (the repositories used can be parameterized)

It is also possible to put a pom.xml file in the same directory as the jar, in which case it will have priority over the one inside the jar.

JQM uses the Maven 3 engine internally, so the pom resolution should be exactly similar to one done with the command line.

Conclusion: in that case, no packaging to do.

.. warning:: using this means the pom is fully resolvable from the engine server. This includes every parent pom.xml used.

lib directory
+++++++++++++++++

If using Maven is not an option (not the build system, no access to a Nexus/Maven central, etc), it is possible to simply put a directory
named "lib" in the same directory as the jar file. 

POM files are ignored if a lib directory is present. An empty lib directory is valid (allows to ignore a pom).

The lib directory may also be situated at the root of the jar file (lower priority than external lib directory).

Conclusion: in that case, libraries must be packaged.

Shared libraries
*******************

It is possible to copy jars inside the JQM_ROOT/ext directory. In that case, these resources will be loaded by a
classloader common to all libraries and will therefore be available to all payloads. 

This should only be used very rarely, and is not to be considered in packaging. This exists mostly for shared JNDI resources
such as JDBC connection pools. Note that a lib in ext has priority over one provided by the payload (through Maven or lib directory).

.. note:: JQM actually makes use of this priority to always provide the latest version of the jqm-api and jqm-client-api to payloads. The APIs can
	therefore be referenced as "provided" dependencies if using Maven.

Creating a JobDef
*********************

Structure 
++++++++++++++++

.. highlight:: xml

The full XSD is given inside the lib directory of the JQM distribution.

An XML can contain as many Job Definitions as needed. Moreover, a single jar file can contain as many payloads as needed, therefore there
can be multiple job definitions with the same referenced jar file.

The general XML structure is this::

	<jqm>
		<jar>
			<path>jqm-test-fibo/jqm-test-fibo.jar</path>

			<jobdefinitions>
				<jobDefinition>
					...
				</jobDefinition>
				... other job definitions ...
			</jobdefinitions>
		</jar>
		<jar>... as many jars as needed ...</jar>
	</jqm>


Jar attributes
+++++++++++++++++++++

+------------+-------------------------------------------------------------------------------------------------------------+
| name       | description                                                                                                 |
+============+=============================================================================================================+
| path       | the path to the jar. It must be relative to the "repo" attribute of the nodes. (default is installdir/jobs) |
+------------+-------------------------------------------------------------------------------------------------------------+

.. versionadded:: 1.1.6
	There used to be a field named "filePath" that was redundant. It is no longer used and should not be specified in new xmls.
	For existing files, the field is simply ignored so there is no need to modify the files.

JobDef attributes
+++++++++++++++++++++++

All JobDefinition attributes are mandatory, yet the tag fields (keyword, module, ...) can be empty.

All attributes are case sensitive.

+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| name           | description                                                                                                                    |
+================+================================================================================================================================+
| name           | the name that will be used everywhere else to designate the payload. (can be seen as the primary key).                         |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| description    | a short description that can be reused inside GUIs                                                                             |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| canBeRestarted | some payloads should never be almlowed to restarted after a crash                                                              |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| javaClassName  | the fully qualified name of the main class of the payload (this is how JQM can launch a payload even without any jar manifest) |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| maxTimeRunning | currently ignored                                                                                                              |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| application    | An open classification. Not used by the engine, only offered to ease querying and GUI creation.                                |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| module         | see above                                                                                                                      |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| keyword1       | see above                                                                                                                      |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| keyword2       | see above                                                                                                                      |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| keyword3       | see above                                                                                                                      |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+
| highlander     | if true, there can only be one running instance at the same time (and queued instances are consolidated)                       |
+----------------+--------------------------------------------------------------------------------------------------------------------------------+

It is also possible to define parameters, as key/value pairs. Note that it is also possible to give parameters inside the :term:`Job Request` (i.e. at runtime).
If a parameter specified inside the request has the same name as one from the :term:`JobDef`, the runtime value wins.

There is an optional parameter named "queue" in which it is possible ot specify the name of the queue to use for all instances created from this job definition. If not
specified (the default), JQM will use the default queue.

XML example
+++++++++++++++++++

Other examples are inside the jobs/xml directory of the JQM distribution.

This shows a single jar containing two payloads. ::

	<jqm>
		<jar>
			<path>jqm-test-fibo/jqm-test-fibo.jar</path>

			<jobdefinitions>
				<jobDefinition>
					<name>Fibo</name>
					<description>Test based on the Fibonachi suite</description>
					<canBeRestarted>true</canBeRestarted>
					<javaClassName>com.enioka.jqm.tests.App</javaClassName>
					<application>CrmBatchs</application>
					<module>Consolidation</module>
					<keyword1>nightly</keyword1>
					<keyword2>buggy</keyword2>
					<keyword3></keyword3>
					<highlander>false</highlander>
					<parameters>
						<parameter>
							<key>p1</key>
							<value>1</value>
						</parameter>
						<parameter>
							<key>p2</key>
							<value>2</value>
						</parameter>
					</parameters>
				</jobDefinition>
				<jobDefinition>
					<name>Fibo2</name>
					<description>Test to check the xml implementation</description>
					<canBeRestarted>true</canBeRestarted>
					<javaClassName>com.enioka.jqm.tests.App</javaClassName>
					<application>ApplicationTest</application>
					<module>TestModule</module>
					<keyword1></keyword1>
					<keyword2></keyword2>
					<keyword3></keyword3>
					<highlander>false</highlander>
					<parameters>
						<parameter>
							<key>p1</key>
							<value>1</value>
						</parameter>
						<parameter>
							<key>p2</key>
							<value>2</value>
						</parameter>
					</parameters>
				</jobDefinition>
			</jobdefinitions>
		</jar>
	</jqm>

Importing
+++++++++++++++

The XML can be imported through the command line. ::

	java -jar jqm.jar -importjobdef /path/to/xml.file

Please note that if your JQM deployment has multiple engines, it is not necessary to import the file on each node - only once is enough
(all nodes share the same configuration). However, the jar file must obviously still be present on the nodes that will run it.

Also, jmq.ps1 or jqm.sh scripts have an "allxml" option that will reimport all xml found under JQM_ROOT/jobs and subdirectories.
