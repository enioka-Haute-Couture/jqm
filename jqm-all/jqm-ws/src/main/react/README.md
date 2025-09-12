# JQM react web app

## Install JQM instance for development

Build JQM from latest 3.x.x:

```bash
mvn package -DskipTests
```

From the jqm-3.x.x-SNAPSHOT folder, run the following commands:

```bash
./jqm.sh createnode
java -jar jqm.jar Import-JobDef -f jobs/jqm-demo/xml/xml-jd-demo.xml
java -jar jqm.jar Set-WebConfiguration --change DISABLE_AUTHENTICATION
./jqm.sh startconsole
```

The legacy web console should now be accessible.

### API proxy configuration

If the web console port is different from `59977`, **set the environment variable `JQM_CONSOLE_PORT`** otherwise your local setup won't work.

## Start

* Use `yarn install` to install dependencies
* Use `yarn start`to start development server

## Troubleshooting

* If you get a white screen check the API calls being made ; 504 HTTP errors indicate the proxy is not properly configured ([see API proxy configuration](#api-proxy-configuration))


## JQM Debug from the Web Service

Install the Java Debugger Extension
***********************************

Download and install the official Java Debugger extension for Visual Studio Code:

`Java Debugger Extension <https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-debug>`_

### Configure VS Code

Add the following debug configuration to your ``launch.json`` file:

.. code-block:: json

    {
      "type": "java",
      "name": "JQM Debug",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }

### Configure Debug

To run JQM in debug mode, you need to add the debug agent option to the JVM command.

Add this option:

.. code-block:: bash

    -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005

### Run the Debug Mode

Run your modified command to start JQM.

You should see this message:

.. code-block:: text

    Listening for transport dt_socket at address: 5005

In VS Code, go to the **Run and Debug** panel, select “JQM Remote Debug” and start the debugger (F5).

JQM will remain paused until the debugger attaches. Once it's attached, press **Ctrl+R** in the browser to reload and continue.

Now you can use JQM Front and put breakpoints in the Java backend.

### Debug linked processes

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
