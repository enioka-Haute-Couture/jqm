Execution context
######################################

Launching the process
*****************************

The new process is created according to a parameter of the job definition.

Default shell
+++++++++++++++++++++++++++++++

Under Linux, this is the shell at ``/bin/sh``. The actual shell behind this depends on the Linux/Unix distribution. Under
Windows, this is always ``cmd.exe``.


The command provided will simply be run as ``/bin/sh -c COMMAND``. The command is always passed as a single argument,
so there is no need to enclose it inside quotes. This means that the command should be typed exactly as it would be in
an interactive shell - no need for double escaping.


The command can be up to 1000 caracters long. Beyond that, please write a script file and launch that script instead.


In case there are explicit parameters (beyond what is inside of the command itself) as allowed by the deployment descriptor,
the parameter values are sorted by key and added to the command sperated by spaces. The keys are only used for sorting,
and are not present inside the resulting command, so ``--file FILENAME`` is two parameters. However, using this is
strongly discouraged - a shell command should be something simple. The administration GUI does not expose this ability on purpose.

Powershell
+++++++++++++++++++++++++++++++

Same as default shell, but powershell is always used as the shell. This means powershell core under Linux.
Job instances will obviously crash if powershell is not present in the PATH seen by JQM.

Direct executable
+++++++++++++++++++++++++++++++

In this case, the commmand given must be an executable path (absolute or relative to JQM_HOME) without parameters.
Parameters should be placed inside the explicit parameter section of the deployment descriptor. The value of parameters
is added sorted by key. The keys are only used for sorting, and are not present inside the resulting command, so
``--file FILENAME`` is two parameters.

Note that in this case there is no shell available, and shell usual functions are not available. For example, there
is no wildcard expansion, no environment variable is usable in the command or its parameters, internal shell commands
are not available and the command cannot be a shell pipe or multiple chained commands.

Of course, it is possible to specify ``/bin/bash`` or whatever shell as the command and ``-c`` and ``my shell commands`` as
two parameters, but in this case it is easier to use the default shell or powershell modes.

.. _shell-envvars:

Environment variables
**************************************

The following variables can be used by the created process (and inside the shell command if launched through a shell).

+----------------------------+------------------------------------------------------------------------------------+
| Name                       | Description                                                                        |
+============================+====================================================================================+
| JQM_JD_APPLICATION_NAME    | Name of the job definition as defined by thje deployment descriptor                |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JD_KEYWORD_1           | Tag from the deployment descriptor                                                 |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JD_KEYWORD_2           | Tag from the deployment descriptor                                                 |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JD_KEYWORD_3           | Tag from the deployment descriptor                                                 |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JD_MODULE              | Tag from the deployment descriptor                                                 |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JD_PRIORITY            | Default job priority from the deployment descriptor                                |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_ID                  | ID assigned by JQM to this particular job instance. Unique througout the cluster   |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_PARENT_ID           | ID of the parent job instance, if any                                              |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_KEYWORD_1           | Tag assigned inside the execution request                                          |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_KEYWORD_2           | Tag assigned inside the execution request                                          |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_KEYWORD_3           | Tag assigned inside the execution request                                          |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_MODULE              | Tag assigned inside the execution request                                          |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_USER_NAME           | Tag assigned inside the execution request                                          |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_TEMP_DIR            | An already existing directory, purged at the end by JQM                            |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_JI_DELIVERY_DIR        | An already existing directory in which to store produced files                     |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_NODE_NAME              | Name (from configuration) of the node having launched the job instance             |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_NODE_APPLICATION_ROOT  | Path (from node configuration) to the application repository                       |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_NODE_LOG_LEVEL         | DEBUG, WARN, INFO, ERROR                                                           |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_Q_NAME                 | Name (from configuration) of the queue having launched the job instance            |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_API_LOGIN              | A user which can call the JQM web APIs. See below.                                 |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_API_PASSWORD           | Password for JQM_API_LOGIN                                                         |
+----------------------------+------------------------------------------------------------------------------------+
| JQM_API_LOCAL_URL          | A URL (using localhost) pointing to the JQM web APIs                               |
+----------------------------+------------------------------------------------------------------------------------+


Permissions
********************************

The process is launched by JQM under the account JQM itself is running.

Result
***************

A job instance is considered as failed if its return code is different than zero.

Stopping a job
******************

When a kill order is given, JQM will (asynchronously) send a SIGTERM or equivalent to the process and all its children.
