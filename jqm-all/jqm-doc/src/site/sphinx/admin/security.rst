Security
############

JQM tries to be as simple as possible and to "just work". Therefore, things (like many security mechanisms)
that require compulsory configuration or which always fail on the first tries are disabled by default and always will be. 

**Out of the box, JQM is not secure**.

This does not mean that nothing can be done. The rest of this
chapter discusses the attack surface and remediation options.

The install files
********************

Inside the install directory, the conf directory should only be read-available to admin/root OS accounts and the account
which runs the engine. Indeed, it contains (clear text) the JQM database login and password.

The whole JMQ_ROOT directory and subdirectories should not be writable by anyone but the admin account and the service account
(modifying these files would allow to replace engine or payload binaries and therefore run arbitrary code).

The log files
****************

The JQM log does not contain any sensitive data. However, payloads may choose to display whatever thay want and their
logs may need to be secured.

The servlet API
*******************

This is the simple HTTP GET/POST API that allows to enqueue an execution request, to get the status of a request, to
retrieve a file created by a run request.

This API cannot be disabled and is accessible without authentication on a clear HTTP channel. However, it listens on
the interface specified in the Node parameters only, so setting it to localhost will prevent remote interaction.

(it was made mostly for local schedulers, so this should not impact functionality much to do so).

**Remediation:** using localhost or using firewall rules.

Adding an optional certificate auth on HTTPS is an open feature request.

The REST API
****************

This is full implementation of the client API. It is disabled by default. When enabled, it is accessible 
without authentication on a clear HTTP channel.

**Remediation:** not deploying the option or using a firewall.

Adding an optional certificate auth on HTTPS is an open feature request.

JMX
*********

Local JMX is always active (Java feature) and admins can connect to it.

Remote JMX is disabled by default. Once enabled, it is accessible without authentication nor encryption.

This is a huge security risk, as JMX allows to run arbitrary code. Firewalling is necessary in this case.

**Remediation**: using local JMX (through SSH for exemple) or using firewall rules.

Database
**************

The security of the data inside the database is subject to the database security systems, nothing there is JQM-specific.

Please note that it is critical to JQM security that the database be secure.
