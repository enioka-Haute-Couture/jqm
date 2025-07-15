
JQM Debug from the Web Service
##############################

Install the Java Debugger Extension
***********************************

Download and install the official Java Debugger extension for Visual Studio Code:

`Java Debugger Extension <https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-debug>`_

Modify Your JQM Set-up
======================

Configure VS Code
-----------------

Add the following debug configuration to your ``launch.json`` file:

.. code-block:: json

    {
      "type": "java",
      "name": "JQM Debug",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }

Configure Debug
===============

To run JQM in debug mode, you need to add the debug agent option to the JVM command.

Add this option:

.. code-block:: bash

    -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005

Run the Debug Mode
==================

Run your modified command to start JQM.

You should see this message:

.. code-block:: text

    Listening for transport dt_socket at address: 5005

In VS Code, go to the **Run and Debug** panel, select “JQM Remote Debug” and start the debugger (F5).

JQM will remain paused until the debugger attaches. Once it's attached, press **Ctrl+R** in the browser to reload and continue.

Now you can use JQM Front and put breakpoints in the Java backend.

Debug linked processes
======================

Add in the launch.json file of your process:

.. code-block:: json

    {
      "type": "java",
      "name": "JQM Debug",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }

You should be able to debug your process when running JQM Web Service.




