@echo OFF

rem set resource file from env variables
echo ^<resource name='jdbc/jqm' auth='Container' type='javax.sql.DataSource' factory='org.apache.tomcat.jdbc.pool.DataSourceFactory' testWhileIdle='true' testOnBorrow='false' testOnReturn='true' validationQuery='%JQM_POOL_VALIDATION_QUERY%' validationInterval='1000' timeBetweenEvictionRunsMillis='60000' maxActive='%JQM_POOL_MAX%' minIdle="2" maxIdle="5" maxWait='30000' initialSize='5' removeAbandonedTimeout='3600' removeAbandoned='true' logAbandoned='true' minEvictableIdleTimeMillis='60000' jmxEnabled='true' username='%JQM_POOL_USER%' password='%JQM_POOL_PASSWORD%' driverClassName="%JQM_POOL_DRIVER%"  url='%JQM_POOL_CONNSTR%' connectionProperties='v$session.program=JQM;' singleton='true' /^> > %JQM_ROOT%/conf/resources.xml

rem helper for swarm deployment - use local host name for node name.
IF "%JQM_NODE_NAME%" == "_localhost_" (
    set JQM_NODE_NAME=%COMPUTERNAME%
)

IF "%JQM_INIT_MODE%" == "SINGLE" (
    IF NOT EXIST C:\jqm\db\%JQM_NODE_NAME% (
        echo #### Node does not exist (as seen by the container^). Single node mode.

        echo ### Updating database schema
        java -jar jqm.jar -u

        echo ### Creating node %JQM_NODE_NAME%
        java -jar jqm.jar -createnode %JQM_NODE_NAME%

        rem mark the node as existing
        echo 1 > C:\jqm\db\%JQM_NODE_NAME%

        rem Apply template
        IF defined JQM_CREATE_NODE_TEMPLATE (
            echo #### Applying template %JQM_CREATE_NODE_TEMPLATE% to new JQM node
            java -jar jqm.jar -t %JQM_CREATE_NODE_TEMPLATE%,%JQM_NODE_NAME%
        )

        rem Jobs
        echo ### Importing local job definitions inside database
        java -jar jqm.jar -importjobdef ./jobs/
    )
)

IF "%JQM_INIT_MODE%" == "CLUSTER" (
    IF NOT EXIST C:\jqm\db\%JQM_NODE_NAME% (
        echo #### Node does not exist (as seen by the container^). Cluster mode.

        echo ### Waiting for templates import
        :loop
        java -jar jqm.jar -nodecount >C:\jqm\tmp\nodes.txt
        type C:\jqm\tmp\nodes.txt | findstr /C:"Existing nodes: 0"
        IF %ERRORLEVEL% EQU 0 (
            rem no templates yet - wait one second and retry
            ping 127.0.0.1 -n 2 >NUL
            goto loop
        )

        echo ### Creating node %JQM_NODE_NAME%
        java -jar jqm.jar -createnode %JQM_NODE_NAME%

        rem mark the node as existing
        echo 1 > C:\jqm\db\%JQM_NODE_NAME%

        rem Apply template
        IF defined JQM_CREATE_NODE_TEMPLATE (
            echo #### Applying template %JQM_CREATE_NODE_TEMPLATE% to new JQM node
            java -jar jqm.jar -t %JQM_CREATE_NODE_TEMPLATE%,%JQM_NODE_NAME%
        )
    )
)

IF "%JQM_INIT_MODE%" == "UPDATER" (
    echo ### Updating database schema
    java -jar jqm.jar -u

    echo ### Importing local job definitions inside database
    java -jar jqm.jar -importjobdef ./jobs/

    echo ### Listing nodes
    java -jar jqm.jar -nodecount >C:\jqm\tmp\nodes.txt
    type C:\jqm\tmp\nodes.txt

    rem If first node, upload template and initial configuration.
    type C:\jqm\tmp\nodes.txt | findstr /C:"Existing nodes: 0"
    IF NOT errorlevel 1 (
        echo ### No nodes yet - uploading templates and initial configuration to database
        java -jar jqm.jar -c selfConfig.swarm.xml
    )

    exit 0
)


rem Go!
echo ### Starting JQM node %JQM_NODE_NAME%
java %JAVA_OPTS% -jar jqm.jar -startnode %JQM_NODE_NAME%
