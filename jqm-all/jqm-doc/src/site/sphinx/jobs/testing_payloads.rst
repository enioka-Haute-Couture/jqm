Testing payloads
#######################

.. highlight:: bash

Unit testing
****************

By unit testing, we mean here running a single payload inside a JUnit test or any other form of test (including a 'main' Java program) without
needing a full JQM engine.

JQM provides a library named jqm-tst which allows tests that will run a **single job instance** in a stripped-down synchronous version of an embedded JQM engine requiring no configuration.
The engine is destroyed immediately after the run.
Single job instance also has for consequence that if your job enqueues new execution requests these will be ignored.

An example taken from JQM's own unit tests::

    @Test
    public void testOne()
    {
        JobInstance res = com.enioka.jqm.test.JqmTester.create("com.enioka.jqm.test.Payload1").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

Here, we just have to give the class of the payload, optionally parameters and that's it. The result returned is from the client API.

Refer to JqmTester javadoc for further details, including how to specify JNDI resource if needed.

Integration tests
************************

If you have to test interactions between jobs (for example, one job instance queueing another), it may be necessary to use a full JQM engine. This gives the basics on how to do it
(there are no supported embedded way to do it yet).

Prepare package
+++++++++++++++++++++++++++

Following the previous chapters, you should have:

* a JAR file containing the payload (potentially with libs)
* a descriptor XML file containing all the metadata

Morevoer, if you do not have a working engine at your disposal, please read :doc:`/admin/install`.

Copy files
+++++++++++++++++++++++++++++

Place the two files inside JQM_DIR/jobs/xxxxx where xxxxx is a directory of your choice.
Please note that the name of this directory must be the same as the one inside the "filePath" tag from the XML.

If there are libraries to copy (pom.xml is not used), they must be placed inside a directory named "lib": JQM_DIR/jobs/xxxxx/lib.

Example (with explicit libraries)::

	$JQM_DIR\
	$JQM_DIR\jobs\
	$JQM_DIR\jobs\myjob\myjob.xml
	$JQM_DIR\jobs\myjob\myjob.jar
	$JQM_DIR\jobs\myjob\lib\
	$JQM_DIR\jobs\myjob\lib\mylib1.jar
	$JQM_DIR\jobs\myjob\lib\mylib2.jar

.. note:: there is no need to restart the engine on any import, jar modification or whatever.

Import the metadata
+++++++++++++++++++++++++++++

.. note:: this only has to be done the first time. Later, this is only necessary if the XML changes.
	Each time the XML is imported, it overwrites the previous values so it can also be done at will.

Open a command line (bash, powershell, ksh...) and run the following commands (adapt JQM_DIR and xxxx)::

	cd $JQM_DIR
	java -jar jqm.jar -importjobdef ./jobs/xxxxx/xxxx.xml

Run the payload
+++++++++++++++++++++++++++++

This part can be run as many times as needed. (adapt the job name, it is the "name" attribute from the XML) ::

	java -jar jqm.jar -enqueue JOBNAME

The logs are inside JQM_ROOT/logs. The user may want to do "tail -f" (or "cat -Wait" in PowerShell) on these files
during tests. There are two files per launch: one containing the standard output flow, the other with the
standard error flow.
