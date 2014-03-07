Logs
#######################################

There are two kinds of logs in JQM: the engine log, and the job instances logs.


+-------+-----------------------------------------------------------------------------------------------------------------------------------+
| level | Signification in JQM                                                                                                              |
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
