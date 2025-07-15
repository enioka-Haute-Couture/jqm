Monitor JQM Jobs with Apache SkyWalking
#######################################

Install SkyWalking Docker
*************************

Run the following command:

.. code-block:: bash

    bash <(curl -sSL https://skywalking.apache.org/quickstart-docker.sh)

The SkyWalking web interface should now be accessible at: ``http://localhost:8080``

Install SkyWalking Agent
*************************

From `this URL <https://dlcdn.apache.org/skywalking/java-agent>`_, go to the latest version available and download both the **release** and its **source code**. For example:

- ``apache-skywalking-java-agent-9.4.0.tgz``
- ``apache-skywalking-java-agent-9.4.0-src.tgz``

Extract both of these archives into a folder. It should then contain 2 sub-directories:

- ``apache-skywalking-java-agent``: contains the ``skywalking-agent.jar`` file and associated plugin folders
- ``apache-skywalking-java-agent-src``: source code for Java-based agents, with helper classes and wrappers to make method interception easier.

In the Source Code Folder
==========================

Clone `the JQM plugin for SkyWalking repository <https://github.com/enioka-Haute-Couture/JQM_Skywalking_Agent>`_, then copy the ``jqm-3.0.x-plugin`` folder from the repository you just cloned into the SkyWalking Java agent codebase under ``apm-sniffer/apm-sdk-plugin``.

Then, update the file:

``apm-sniffer/apm-sdk-plugin/pom.xml``

to reference the plugin you just copied.

Build the project with:

.. code-block:: bash

    ./mvnw install -pl :jqm-3.x.x-plugin -am -Dmaven.test.skip

In the Release Folder
======================

Copy the compiled ``.jar`` file (from ``~/.m2/repository/org/apache/skywalking/``) into the ``plugin`` folder of the SkyWalking agent.

Run the Plugin
**************

When launching JQM, add to your command


.. code-block:: bash

   -javaagent:path_to_file/skywalking-agent.jar
