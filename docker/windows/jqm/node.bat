@echo OFF
setlocal EnableDelayedExpansion
if errorlevel 1 (
    echo unable to enable delayed expansion
    exit 1
)
setlocal enableextensions
if errorlevel 1 (
    echo unable to enable extensions
    exit 1
)

rem set resource file from env variables
IF DEFINED JQM_POOL_INIT_SQL set JQM_POOL_INIT_SQL=initSQL="%JQM_POOL_INIT_SQL%"
echo ^<resource name='jdbc/jqm' auth='Container' type='javax.sql.DataSource' factory='org.apache.tomcat.jdbc.pool.DataSourceFactory' testWhileIdle='true' testOnBorrow='false' testOnReturn='true' validationQuery='%JQM_POOL_VALIDATION_QUERY%' %JQM_POOL_INIT_SQL% logValidationErrors='true' validationInterval='1000' timeBetweenEvictionRunsMillis='60000' maxActive='%JQM_POOL_MAX%' minIdle="2" maxIdle="5" maxWait='30000' initialSize='5' removeAbandonedTimeout='3600' removeAbandoned='true' logAbandoned='true' minEvictableIdleTimeMillis='60000' jmxEnabled='true' username='%JQM_POOL_USER%' password='%JQM_POOL_PASSWORD%' url='%JQM_POOL_CONNSTR%' connectionProperties='v$session.program=JQM;' singleton='true' /^> > %JQM_ROOT%/conf/resources.xml

rem helper for swarm deployment - use local host name for node name.
IF "%JQM_NODE_NAME%" == "_localhost_" (
    set JQM_NODE_NAME=%COMPUTERNAME%
)
IF "%JQM_NODE_WS_INTERFACE%" == "_localhost_" (
    set JQM_NODE_WS_INTERFACE=%COMPUTERNAME%
)

IF "%JQM_INIT_MODE%" == "SINGLE" (
    IF NOT EXIST C:\jqm\db\%JQM_NODE_NAME% (
        echo #### Node %JQM_NODE_NAME% does not exist (as seen by the container^). Single node mode.

        echo ### Updating database schema
        java -jar jqm.jar Update-Schema

        echo ### Creating node %JQM_NODE_NAME%
        java -jar jqm.jar NewNode -n %JQM_NODE_NAME%

        rem mark the node as existing
        echo 1 > C:\jqm\db\%JQM_NODE_NAME%

        rem Apply template
        IF defined JQM_CREATE_NODE_TEMPLATE (
            echo #### Applying template %JQM_CREATE_NODE_TEMPLATE% to new JQM node
            java -jar jqm.jar Install-NodeTemplate -t %JQM_CREATE_NODE_TEMPLATE% -n %JQM_NODE_NAME% -i %JQM_NODE_WS_INTERFACE%
        )

        rem Jobs
        echo ### Importing local job definitions inside database
        java -jar jqm.jar Import-jobdef -n ./jobs/
    )
)

IF "%JQM_INIT_MODE%" == "CLUSTER" (
    echo ### Checking configuration in database for node %JQM_NODE_NAME% - Cluster mode.
    java -jar jqm.jar Get-NodeCount >C:\jqm\tmp\nodes.txt
    IF ERRORLEVEL 1 (
        echo cannot check node status, java failure
        exit 1
    )

    type C:\jqm\tmp\nodes.txt | findstr /C:"Already existing: %JQM_NODE_NAME%"
    IF !ERRORLEVEL! EQU 0 (
        echo ### Node %JQM_NODE_NAME% already exists inside database configuration, skipping config
        goto startup
    )

    echo #### Node %JQM_NODE_NAME% does not exist (as seen by the database^). Cluster mode.

    echo ### Waiting for templates import
    :loop
    type C:\jqm\tmp\nodes.txt | findstr /C:"Existing nodes: 0"
    IF !ERRORLEVEL! EQU 0 (
        rem no templates yet - wait one second and retry
        ping 127.0.0.1 -n 2 >NUL
        java -jar jqm.jar Get-NodeCount >C:\jqm\tmp\nodes.txt
        goto loop
    )

    echo ### Creating node %JQM_NODE_NAME%
    java -jar jqm.jar New-Node -n %JQM_NODE_NAME%

    rem Apply template
    IF defined JQM_CREATE_NODE_TEMPLATE (
        echo #### Applying template %JQM_CREATE_NODE_TEMPLATE% to new JQM node
        java -jar jqm.jar Apply-NodeTemplate -t %JQM_CREATE_NODE_TEMPLATE% -n %JQM_NODE_NAME% -i %JQM_NODE_WS_INTERFACE%
    )
)

IF "%JQM_INIT_MODE%" == "UPDATER" (
    echo ### Updating database schema
    java -jar jqm.jar Update-Schema

    echo ### Importing local job definitions inside database
    java -jar jqm.jar Import-jobdef -f ./jobs/

    echo ### Listing nodes
    java -jar jqm.jar get-nodecount >C:\jqm\tmp\nodes.txt
    type C:\jqm\tmp\nodes.txt

    rem If first node, upload template and initial configuration.
    type C:\jqm\tmp\nodes.txt | findstr /C:"Existing nodes: 0"
    IF NOT errorlevel 1 (
        echo ### No nodes yet - uploading templates and initial configuration to database
        java -jar jqm.jar Import-ClusterConfiguration -f selfConfig.swarm.xml
    )

    exit 0
)


rem Go!
:startup
echo ### Starting JQM node %JQM_NODE_NAME%
java -D"com.enioka.jqm.interface=0.0.0.0" %JAVA_OPTS% -jar jqm.jar Start-Node -n %JQM_NODE_NAME%
