#!/bin/sh

###############################################################################
## Startup script for Linux docker deployments.
###############################################################################


# set resource file from env variables
if [ ! "x${JQM_POOL_INIT_SQL}" = "x" ]
then
    export JQM_POOL_INIT_SQL="initSQL=\"${JQM_POOL_INIT_SQL}\""
fi

echo "<resource
    name='jdbc/jqm'
    auth='Container'
    type='javax.sql.DataSource'
    factory='org.apache.tomcat.jdbc.pool.DataSourceFactory'
    testWhileIdle='true'
    testOnBorrow='false'
    testOnReturn='true'
    validationQuery=\"${JQM_POOL_VALIDATION_QUERY}\"
    ${JQM_POOL_INIT_SQL}
    logValidationErrors='true'
    validationInterval='1000'
    timeBetweenEvictionRunsMillis='60000'
    maxActive='${JQM_POOL_MAX}'
    minIdle='2'
    maxIdle='5'
    maxWait='30000'
    initialSize='5'
    removeAbandonedTimeout='3600'
    removeAbandoned='true'
    logAbandoned='true'
    minEvictableIdleTimeMillis='60000'
    jmxEnabled='true'
    username='${JQM_POOL_USER}'
    password='${JQM_POOL_PASSWORD}'
    driverClassName='${JQM_POOL_DRIVER}'
    url='${JQM_POOL_CONNSTR}'
    connectionProperties='v\$session.program=JQM;'
    singleton='true'
/>" > ${JQM_ROOT}/conf/resources.xml

# helper for swarm deployment - use local host name for node name.
if [ "${JQM_NODE_NAME}" = "_localhost_" ]
then
    export JQM_NODE_NAME=$(hostname)
fi
if [ "${JQM_NODE_WS_INTERFACE}" = "_localhost_" ]
then
    export JQM_NODE_WS_INTERFACE=$(hostname)
fi

if [ "${JQM_INIT_MODE}" = "SINGLE" ]
then
    echo "### Checking configuration in database for node ${JQM_NODE_NAME} - Single mode."
    if [ ! -f /jqm/db/${JQM_NODE_NAME} ]
    then
        echo "#### Node does not exist (as seen by the container). Single node mode."

        echo "### Updating database schema"
        java -jar jqm.jar Update-Schema

        echo "### Creating node ${JQM_NODE_NAME}"
        java -jar jqm.jar New-Node -n ${JQM_NODE_NAME}

        # mark the node as existing
        echo 1 > /jqm/db/${JQM_NODE_NAME}

        # Apply template
        if [ ! "x${JQM_CREATE_NODE_TEMPLATE}" = "x" ]
        then
            echo "#### Applying template ${JQM_CREATE_NODE_TEMPLATE} to new JQM node"
            java -jar jqm.jar Install-NodeTemplate -t ${JQM_CREATE_NODE_TEMPLATE} -n ${JQM_NODE_NAME} -i ${JQM_NODE_WS_INTERFACE}
        fi

        # Jobs
        echo "### Importing local job definitions inside database"
        java -jar jqm.jar Import-JobDef -f ./jobs/

        # WS
        echo "### Disabling WS auth"
        java -jar jqm.jar Set-WebConfiguration -c ENABLE_HTTP_GUI
        java -jar jqm.jar Set-WebConfiguration -c DISABLE_AUTHENTICATION
    else
        echo "### Node ${JQM_NODE_NAME} already exists inside database configuration, skipping config"
    fi
fi

if [ "${JQM_INIT_MODE}" = "CLUSTER" ]
then
    echo "### Checking configuration in database for node ${JQM_NODE_NAME} - Cluster mode."
    java -jar jqm.jar Get-NodeCount >/jqm/tmp/nodes.txt
    if [ ! $? -eq 0 ]
    then
        echo "cannot check node status, java failure"
        return 1
    fi

    cat /jqm/tmp/nodes.txt | grep "Already existing: ${JQM_NODE_NAME}"
    if [ $? -eq 0 ]
    then
        echo "### Node ${JQM_NODE_NAME} already exists inside database configuration, skipping config"
    else
        echo "#### Node does not exist (as seen by the database). Cluster mode."

        echo "### Waiting for templates import"
        $(exit 0)
        while [ $? -eq 0 ]
        do
            # no templates yet - wait one second and retry
            sleep 1
            java -jar jqm.jar Get-NodeCount >/jqm/tmp/nodes.txt
            grep "Existing nodes: 0" /jqm/tmp/nodes.txt
        done

        echo "### Creating node ${JQM_NODE_NAME}"
        java -jar jqm.jar New-Node -n ${JQM_NODE_NAME}

        # mark the node as existing
        echo 1 > /jqm/db/${JQM_NODE_NAME}

        # Apply template
        if [ ! "x${JQM_CREATE_NODE_TEMPLATE}" = "x" ]
        then
            echo "#### Applying template ${JQM_CREATE_NODE_TEMPLATE} to new JQM node"
            java -jar jqm.jar Install-NodeTemplate -t ${JQM_CREATE_NODE_TEMPLATE} -n ${JQM_NODE_NAME} -i ${JQM_NODE_WS_INTERFACE}
        fi
    fi
fi

if [ "${JQM_INIT_MODE}" = "UPDATER" ]
then
    echo "### Updating database schema"
    java -jar jqm.jar Update-Schema

    echo "### Importing local job definitions inside database"
    java -jar jqm.jar Import-JobDef -f ./jobs/

    echo "### Listing nodes"
    java -jar jqm.jar get-nodecount >/jqm/tmp/nodes.txt
    type /jqm/tmp/nodes.txt

    # If first node, upload template and initial configuration.
    grep "Existing nodes: 0" /jqm/tmp/nodes.txt
    if [ $? -eq 0 ]
    then
        echo "### No nodes yet - uploading templates and initial configuration to database"
        java -jar jqm.jar Import-ClusterConfiguration -f selfConfig.swarm.xml
    fi

    return 0
fi

# Go!
echo "### Starting JQM node ${JQM_NODE_NAME}"
java -D"com.enioka.jqm.interface=0.0.0.0" ${JAVA_OPTS} -jar jqm.jar Start-Node -n ${JQM_NODE_NAME}
