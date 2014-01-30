# Installation

## Windows

### Binary install
Prerequisites:

* A directory where JQM will be installed, named JQM_ROOT afterwards
* An admin account (for installation only)
* A service account with minimal permissions: LOGON AS SERVICE + full permissions on JQM_ROOT.

The following script will download and copy the binaries (adapt the first two lines). Run it with admin rights.
```PowerShell
$JQM_ROOT = "C:\TEMP\jqm"
$JQM_VERSION = "1.1.4"
mkdir -Force $JQM_ROOT; cd $JQM_ROOT
Invoke-RestMethod https://github.com/enioka/jqm/archive/jqm-$JQM_VERSION.zip -OutFile jqm.zip
$shell = new-object -com shell.application
$zip = $shell.NameSpace((Resolve-Path .\jqm.zip).Path)
foreach($item in $zip.items()) { $shell.Namespace($JQM_ROOT).copyhere($item) }
rm jqm.zip; mv jqm*/* .
```

Then create a service (adapt user and password):
```PowerShell
./jqm.ps1 createnode
./jqm.ps1 -ServiceUser marsu -ServicePassword marsu
./jqm.ps1 start
```
And it's done, a JQM service node is now running.

### Testing

The following will import the definition of three test jobs included in the distribution, then launch one. (no admin rights necessary nor variables)
```PowerShell
./jqm.ps1 stop  ## Default database is a single file... that is locked by the engine if started
./jqm.ps1 allxml  # This will import all the test job definitions
./jqm.ps1 -Enqueue TestEcho
./jqm.ps1 start
```

Check JQM_ROOT/logs/jqm.log: a job should have launched (and suceeded). Success!

### Database configuration

The node created in the previous step has serious drawbacks:

* it uses an HSQLDB database with a local file that can be only used by a single process
* it cannot be used in a network as nodes communicate through the database
* General low performances and persistence issues inherent to HSQLDB

Just edit JQM_ROOT/conf/db.properties file to reference your own database. 
It contains by default sample configuration for Oracle and HSQLDB, but MySQL is also supported.

> Note that the database is intended to be shared between all JQM nodes - you should not create a schema/database per node.

Afterwards, place your JDBC driver inside the "lib" directory. **It must be renamed jdbcdriver.jar**.

Then stop the service:
```PowerShell
./jqm.ps1 stop
./jqm.ps1 createnode
./jqm.ps1 start
```

Then, test again (assuming this is not HSQLDB in file mode anymore, and therefore that there is no need to stop the engine):
```PowerShell
./jqm.ps1 allxml
./jqm.ps1 -Enqueue TestEcho
```

### Global configuration

When the first node is created inside a database, some parameters are automatically created. You may want to change them using your prefered 
database editing tool.

Table GLOBALPARAMETER:

Name | default value | Description
--- | --- | ----
mavenRepo | http://repo1.maven.org/maven2/ | Maven/Nexus repositories to use to resolve dependencies. There can be as many entries with this name as needed.
defaultConnection | jdbc/jqm | JNDI alias that will be used by the getDefaultConnection function of the engine API.
deadline | 10 | purge limit, in days, of jobs stuck in queue

### JNDI configuration

See [resources](resources.md).

Note that one JDBC JNDI alias is created, named jdbc/jqm, referencing the JQM database but has no password - you should set it.



