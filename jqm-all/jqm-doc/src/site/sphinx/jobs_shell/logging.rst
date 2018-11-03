Logging
#######################

Executables may of course use whatever logging they wish. For increased administrability, it is recommended to simply write to the
standard output and standard error flows, as those are captured by JQM and made available through the client APIs.

It is also possible to specify a log file as a deliverable file.

Finally, JQM exposes its log level as environment variable JQM_NODE_LOG_LEVEL which the executable may choose to respect.
