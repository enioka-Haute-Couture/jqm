# Packaging

JQM is able to load jar files (in case your code is actually inside a war, it is possible to simply rename the file), which gives
a clear guidance as to how the code should be packaged. However, there are also other elements that JQM needs to run the code.
For exemple, when a client requests the code to run, it must be able to refer to the code unambiguously, therefore JQM must know
an "application name" corresponding to the code. This name, with other data, is to be put inside a XML file that will be imported
by JQM. A code can only run if its XML has been imported.

Should some terms prove to be obscure, please refer to the [definitions](index.md#Definitions).

## Libraries handling

JQM itself does not provide any libraries to the payloads - all its internal classes are hidden. But there are two ways to make sure the required
libraires are presnet at runtime.

### Maven POM

A jar created with Maven always contains the pom.xml hidden inside META-INF. JQM will extract it, read it and donload the dependencies,
putting them on the payload's class path.

It is also possible to put a pom.xml file in the same directory as the jar, in which case it will have priority over the one inside the jar.

Conclusion: in that case, no packaging to do.

> :exclamation: JQM does not currently support the use of variables (${my.var}) inside dependency definitions

### lib directory

If using Maven is not an option (not the build system, no access to a Nexus/Maven central, etc), it is possible to simply put a directory
named "lib" in the same directory as the jar file. 

POM files are ignored if a lib directory is present. An empty lib directory is valid (allows to ignore a pom).

Conclusion: in that case, libraries must be packaged.

## Creating a JobDef

### Structure 

The full XSD is given inside the lib directory of the JQM distribution.

An XML can contain as many Job Definitions as needed. Moreover, a single jar file can contain as many payloads as needed, therefore there
can be multiple job definitions with the same referenced jar file.

The general XML structure is this:

```XML
<jqm>
	<jar>
		<path>jqm-test-fibo/jqm-test-fibo.jar</path>
		<filePath>jqm-test-fibo/</filePath>

		<jobdefinitions>
			<jobDefinition>
				...
			</jobDefinition>
			... other job definitions ...
		</jobdefinitions>
	</jar>
	<jar>... as many jars as needed ...</jar>
</jqm>
```

### Jar attributes

| name | description |
| --- | --- |
| path | the path to the jar. It must be relative to the "repo" attribute of the nodes. (default is installdir/jobs) |
| filepath | directory part of the "path". Present for legacy reasons. |

### JobDef attributes

All JobDefinition attributes are mandatory, yet they can be void.

All attributes are case sensitive.

| name | description |
| --- | --- |
| name | the name that will be used everywhere else to designate the payload. (can be seen as the primary key). |
| description | a short description that can be reused inside GUIs |
| canBeRestarted | some payloads should never be almlowed to restarted after a crash |
| javaClassName | the fully qualified name of the main class of the payload (this is how JQM can launch a payload even without any jar manifest) |
| maxTimeRunning | currently ignored |
| application | An open classification. Not used by the engine, only offered to ease querying and GUI creation. |
| module | see above |
| keyword1 | see above |
| keyword2 | see above |
| keyword3 | see above |
| highlander | if true, there can only be one running instance at the same time (and queued instances are consolidated) |

It is also possible to define parameters, as key/value pairs. Note that it is also possible to give parameters inside the Job Request (i.e. at runtime).
If a parameter specified inside the request has the same name as one from the JobDef, is wins.


## XML example

Other examples are inside the jobs/xml directory of the JQM distribution.

This shows a single jar containing two payloads.

```XML
<jqm>
	<jar>
		<path>jqm-test-fibo/jqm-test-fibo.jar</path>
		<filePath>jqm-test-fibo/</filePath>

		<jobdefinitions>
			<jobDefinition>
				<name>Fibo</name>
				<description>Test based on the Fibonachi suite</description>
				<canBeRestarted>true</canBeRestarted>
				<javaClassName>com.enioka.jqm.tests.App</javaClassName>
				<maxTimeRunning>42</maxTimeRunning>
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
				<maxTimeRunning>42</maxTimeRunning>
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

```

### Importing

The XML can be imported through the command line.

```PowerShell
java -jar jqm.jar -importjobdef /path/to/xml.file
```

Please note that if your JQM deployment has multiple engines, it is not necessary to import the file on each node - only once is enough
(all nodes share the same configuration). However, the jar file must obviously still be present on the nodes that will run it.