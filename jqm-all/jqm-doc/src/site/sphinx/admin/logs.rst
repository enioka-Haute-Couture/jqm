Logs
#######################################

There are two kinds of logs in JQM: the engine log, and the job instances logs.

Log levels
**************

+-------+-----------------------------------------------------------------------------------------------------------------------------------+
| Level | Meaning in JQM                                                                                                                    |
+=======+===================================================================================================================================+
| TRACE | Fine grained data only useful for debugging, exposing the innards of the steps of JMQ's state-machine                             |
+-------+-----------------------------------------------------------------------------------------------------------------------------------+
| DEBUG | For debugging. Gives the succession of the steps of the state-machine, but not what happens inside a step.                        |
+-------+-----------------------------------------------------------------------------------------------------------------------------------+
| INFO  | Important changes in the state of the engine (engine startup, poller shutdown, ...)                                               |
+-------+-----------------------------------------------------------------------------------------------------------------------------------+
| WARN  | An alert: something has gone wrong, analysis is needed but not urgent. Basically, the way to raise the admins' attention          |
+-------+-----------------------------------------------------------------------------------------------------------------------------------+
| ERROR | The engine may continue, but likely something really bad has happened. Immediate attention is required                            |
+-------+-----------------------------------------------------------------------------------------------------------------------------------+
| FATAL | The engine is dead. Immediate attention is required                                                                               |
+-------+-----------------------------------------------------------------------------------------------------------------------------------+

The default log-level is INFO.

In case of a classic two-level monitoring system ('something weird' & 'run for your life'), WARN whould be mapped to the first level while ERROR and FATAL should be mapped to the second one.


Engine log
************

It is named jqm.log. It is rotated as soon as it reaches 10MB. The five most recent files are kept.

It contains everything related to the engine - job instance launches leave no traces here.

Java Virtual Machine Log
*************************

Named jqm_<nodename>_std.log and jqm_<nodename>_err.log for respectively standard ouput and error output. It contains every log that the engine did not manage to catch. For instance low level JVM error statement such as OutOfMemoryException. It is rotated at startup when it reaches 10MB. 30 days of such logs are kept.

Payload logs
***************

One file named after the ID of the job instance is created per payload launch. It contains:

* the engine traces concerning this log (classloader creation, start, stop, ....)
* the stdout/stderr of the job instance. This means that if payloads use a ConsoleAppender for their logs (as is recommended)
  it will be fully here.
  
These files are **not purged** automatically. This is the admin's responsability.

Also of note, there are two log levels involved here:

* the engine log level, which will determine the verbosity of the traces concerning the luanch of the job itself.
* the payload log level: if the payload uses a logger (log4j, logback, whatever), it has its own log level. This log level
  is not related in any way to the engine log level. (remember: running a payload inside JQM is the same as running it inside
  a standard JVM. The engine has no more influence on the behaviour of the payload than a JVM would have)
