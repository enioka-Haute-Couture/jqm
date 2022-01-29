Logging
############

For JQM itself
*******************

JQM itself uses the `slf4j` logging API everywhere. Behind the API, it uses `org.apache.felix.logback` which is an implementation of the OSGi logging specification using logback.

.. note:: why not use directly the OSGi logging service to log instead of slf4j? Because we avoid too many dependencies on OSGi when the subject is not directly about modularity.

In turn, we have a logging configuration which will create a file JQM_ROOT/jqm.log per day (max size 1GB) and archive the latest five files, purging the older ones.

Also of note, the shell script used in some Linux environments to launch the JQM daemon will redirect stdout and stderr to files, using `log_rotate` to clean them.

For Java payloads
********************

As specified in :doc:`the payload developper documentation<../jobs/logging>` any Java payload can use whatever it wants for its own logging needs, as it is a standard Java program free to do whatever it wants.
JQM however tries to capture the stdout/stderr of the running job instances in order to simplify the work of administrators and allow standardized log retrieval.

This is done by:

* taking over ``System.out`` and ``System.err`` at startup by setting them to a proxy ``PrintStream`` named :class:`MultiplexPrintStream`.
* on each Java payload job instance launch, the corresponding Java thread plus :class:`JobInstance` are registered inside the :class:`MultiplexPrintStream` (which is easy to find since it is a singleton registered inside ``System.out``).
* when the stream receives data to write, it:

  * writes the data as supposed to to the actual stdout (it is always a proxy)
  * looks at the current thread doing the stdout write: if it is registered, it can perform additional writing according to the :class:`JobInstance` parameters. The goal here is to create a file per launch, named after the job instance ID.

As we are dealing with JVM-wide singletons, this is obviously not very module-friendly and great care is taken to unload our proxy when the containing module is unloaded.

Also as we try to add any log created by JQM related to the execution inside the specific job instance log file, the logger registration must be the first action taken by the Java job runner.
