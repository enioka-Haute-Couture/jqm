Data security
###############

JQM tries to be as simple as possible and to "just work". Therefore, things (like many security mechanisms)
that require compulsory configuration or which always fail on the first tries are disabled by default. 

**Therefore, out of the box, JQM is not as secure as it can be** (but still reasonably secure).

This does not mean that nothing can be done. The rest of this
chapter discusses the attack surface and remediation options.

Data & data flows
************************

Security needs are rated on a three ladder scale: low, medium, high.

.. note:: the administration GUI is not listed in this section, as it is simply a client based on the different web services.

Central Configuration
========================

This is the defintion of the JQM network: which JVM runs where, with which parameters. These parameters include the main security options.
Most data in here is easy to guess by simply doing a network traffic analysis (without having to know the actual content of the traffic - just routes will
tell the structure of the network).

*Integrity* need: high (as security mechanisms can be disabled here)

*Confidentiality* need: low (as data is public anyway. Exception: if the internal PKI isued, the root certificate is here. But this certificate actually protects... the central configuration so its not a real issue)

*Stored in*: central database

*Exposed by*: admin web service (R/W), direct db connection (R/W).

Node-specific configuration
===================================

Every node has a configuration file containing the connection information to the central database.

*Integrity* need: medium (rerouting the node on another db will allow to run arbitrary commands, but such a case means the server is compromised anyway)

*Confidentiality* need: high (exposes the central database)

*Stored in*: local file system

*Exposed by*: file system (R).


Job referential
========================

The definition of the different batch jobs that are run - basically shell command lines. 

*Integrity* need: high (as a modification of this data allows for arbitrary command line execution)

*Confidentiality* need: high (as people often store password and other critical data inside their command lines)

*Stored in*: central database

*Exposed by*: admin web service (R/W), client web service (R), direct db connection (R/W).


Tracking data
========================

Every queued, running or ended job instance has tracking objects inside the central database.

*Integrity* need: medium (a modification on an ended instance will simply make history wrong, but altering a yet to run instance will allow to modify its command line)

*Confidentiality* need: high (as people often store password and other critical data inside their command lines, which are stored in this data)

*Stored in*: central database

*Exposed by*: client web service (R/W), simple web service (enqueue execution request & get status), direct db connection (R/W).


Logs & batch created files
==============================

Every job instance creates a log file. It may, depending on the jobs, contain sensitive data. There is however no sensitive data inside 
JQM's own logs. Moreover, batch jobs can create file (reports, invoices, ...) that may be critical and are stored alongside logs.

*Integrity* need: depends

*Confidentiality* need: depends

*Stored in*: file system (on the node it was created)

*Exposed by*: simple web service (R), file system

Binaries
========================

Obviously, the JQM binaries are rather critical to its good operation.

*Integrity* need: high

*Confidentiality* need: low (on GitHub!)

*Stored in*: file system

*Exposed by*: file system



Summary of elements to protect
***********************************

**Central database**: it contains elements that are both confidential and which integrity is crucial.

**Admin web service**: it exposes (R/W) the same elements

**Client web service**: it exposes (R/W) all operational data

**Simple web service**: it exposes log files, business files, and allows for job execution submission.

**Binaries**: obvious

**Database connection configuration** on each node: exposes the central database.

.. warning:: in any way, compromising the central database means compromising the whole JQM cluster. Therefore
  protecting it should be the first step in any JQM hardening! This will not be detailed here, as this is database
  specific - ask your DBA for help.


Security mechanisms
**************************

Web services
========================

SSL
--------

All communications can be forced inside a SSL channel that will guarantee both confidentiality and integrity, provided
certificate chains are correctly set.

JQM provided its own Private Key Infrastructure (PKI), which allows it to start without need for any certificate configuration.
Its root certificate is stored inside the central database. The root key is created randomly at first startup.
It also allows for easy issuing of client certificates for authentication through a web service of the admin API (and the admin GUI). 

However, using the internal PKI is not compulsory. Indeed, it the limitation of not having a revocation mechanism.
If you don't want to use your own:

* put the private key and public certificate of each node inside JQM_ROOT/conf/keystore.pfx (PKCS12 store, password SuperPassword)
* put the public certificate chain of the CA inside JQM_ROOT/conf/trusted.jks (JKS store, password SuperPassword)
* set the global parameter enableInternalPki to 'false'.

SSL is **disabled** by default, as in most cases JQM is run inside a secure perimeter network where data flows are at acceptable risk.
It can be enabled by setting the global parameter enableWsApiSsl to 'true'. Once enabled, all nodes will switch to SSL-only mode on their
next reboot.


Authentication
----------------

JQM uses a Role Based Access Control (RBAC) system to control access to its web services, coupled with either basic HTTP authentication or
client certificate authentication (both being offered at the same time).

JQM comes with predefined roles that can be modified with the exception of the "administrator" role which is compulsory.

Passwords are stored inside the central database in hash+salt form. Accounts can have a validity limit or be disabled.

Authentication is **enabled** by default. The rational behind this is not really to protect data from evil minds, but to prevent accidents in multi user
environments. It can be disabled by setting the global parameter enableWsApiAuth to 'false'.

.. note:: as the web GUI is based on the admin web service, it also uses these. In particular, it can use SSL certificates for authentication.

Clients use of SSL and authentication
-------------------------------------------

JQM comes with two "ready to use" client libraries - one directly connecting to the central database, the other to the client web service API.

The web service client has a straightforward use of SSL and authentication - it must be provided a trust store, and either a user/password or a client certificate store.

The direct to database client does not use authentication - it has after all access to the whole database, so it would be rather ridiculous.
It has however a gotcha: file retrieval (log files as well as business files created by jobs) can only be done through the
simple web service API. Therefore, the client also needs auth data. As it has access to the database, it will create a temporary user with 24 hours validity
for this use on its own. As far as SSL is concerned, it must be provided a trust store too (or else will use system default stores). **This is only necessary if the
file retrieval abilities are to be used inside a SSL environment** - otherwise, this client library does not use the web services API at all.

File retrieval specific protection
------------------------------------

The simple API exposes the only API able to fetch a business file (report, invoice, etc - all files created by the jobs). To prevent predictability,
the ID given as the API parameter is not the sequential ID of the file as referenced inside the central database but a random 128 bits GUID.

Therefore, it will be hard for an intruder to retrieve the files created by a job instance even without SSL or authentication.

Switch off 'protection'
------------------------

If the web services are not needed, they can be suppressed by setting the disableWsApi global parameter to 'true'. This will simply prevent the web server
from starting at all on every node.

Web services can also be selectively disabled on all nodes by using the following global parameters: disableWsApiClient, disableWsApiAdmin, disableWsApiSimple.
These parameters are not set by default.

Finally, each node has three parameters allowing to choose which APIs should be active on it. By **default, simple API is enabled, client & admin APIs are disabled**.

.. warning:: disabling the simple API means file retrieval won't work.

Database 
========================

Please see your DBA. Once again, the database is the cornerstone of the JQM cluster and its compromission is the compromisson of every server/OS account on which a JQM node runs.

Binaries
========================

A script will soon be provided to set minimal permissions on files.

.. warning:: a useful reminder: JQM should never run as root/local system/administrator/etc. No special permissions under Unix-like systems, logon as service under windows. That's all. Thanks!



Monitoring access security
********************************

Local JMX is always active (it's a low level Java feature) and Unix admins can connect to it.

Remote JMX is disabled by default. Once enabled, it is accessible without authentication nor encryption. Tickets #68 an #69 are feature requests for this.

This is a huge security risk, as JMX allows to run arbitrary code. Firewalling is necessary in this case.

**Remediation**: using local JMX (through SSH for example) or using firewall rules.


Tracing
***************

To come. Feature request tickets already open. The goal will be to trace in a simple form all configuration modification and access to client APIs.

Currently, an access log lists all calls to the web services, but there is no equivalent for the JPA API (and logs are not centralized in any way).
