Logging
#########################

Once again, running Java code inside JQM is exactly as running the same code inside a bare JVM. Therefore, there is nothing specific concerning logging:
if some code was using log4j, logback or whatever, it will work. However, for more efficient logging, it may be useful to take some extra care in setting
the parameters of the loggers:

* the "current directory" is not defined (or rather, it is defined but is guaranteed to be the same each time), so absolute paths are better
* JQM captures the console output of a job to create a log file that can be retrieved later through APIs.

Therefore, **the recommended approach for logging in a JQM payload is to use a Console Appender and no explicit log file**.
