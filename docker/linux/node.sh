#!/bin/sh

###############################################################################
## Startup script for Linux docker deployments.
###############################################################################


# set resource file from env variables
echo "<resource 
    name='jdbc/jqm' 
    auth='Container' 
    type='javax.sql.DataSource' 
    factory='org.apache.tomcat.jdbc.pool.DataSourceFactory' 
    testWhileIdle='true' 
    testOnBorrow='false' 
    testOnReturn='true' 
    validationQuery='${JQM_POOL_VALIDATION_QUERY}' 
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

if [ "${JQM_INIT_MODE}" = "SINGLE" ]
then
    if [ ! -f /jqm/db/${JQM_NODE_NAME} ]
    then
        echo "#### Node does not exist (as seen by the container). Single node mode."

        echo "### Updating database schema"
        java -jar jqm.jar -u

        echo "### Creating node ${JQM_NODE_NAME}"
        java -jar jqm.jar -createnode ${JQM_NODE_NAME}

        # mark the node as existing
        echo 1 > /jqm/db/${JQM_NODE_NAME}

        # Apply template
        if [ ! "${JQM_CREATE_NODE_TEMPLATE}" = "" ]
        then
            echo "#### Applying template ${JQM_CREATE_NODE_TEMPLATE} to new JQM node"
            java -jar jqm.jar -t ${JQM_CREATE_NODE_TEMPLATE},${JQM_NODE_NAME}
        fi

        # Jobs
        echo "### Importing local job definitions inside database"
        java -jar jqm.jar -importjobdef ./jobs/
    fi
fi

if [ "${JQM_INIT_MODE}" = "CLUSTER" ]
then
    if [ ! -f /jqm/db/${JQM_NODE_NAME} ]
    then
        echo "#### Node does not exist (as seen by the container). Cluster mode."

        echo "### Waiting for templates import"
        while [ $? -eq 0 ]
        do
            # no templates yet - wait one second and retry
            sleep 1
            java -jar jqm.jar -nodecount >/jqm/tmp/nodes.txt
            grep "Existing nodes: 0" /jqm/tmp/nodes.txt
        done

        echo "### Creating node ${JQM_NODE_NAME}"
        java -jar jqm.jar -createnode ${JQM_NODE_NAME}

        # mark the node as existing
        echo 1 > /jqm/db/${JQM_NODE_NAME}

        # Apply template
        if [ ! "${JQM_CREATE_NODE_TEMPLATE}" = "" ]
        then
            echo "#### Applying template ${JQM_CREATE_NODE_TEMPLATE} to new JQM node"
            java -jar jqm.jar -t ${JQM_CREATE_NODE_TEMPLATE},${JQM_NODE_NAME}
        fi
    fi
fi

if [ "${JQM_INIT_MODE}" = "UPDATER" ]
then
    echo "### Updating database schema"
    java -jar jqm.jar -u

    echo "### Importing local job definitions inside database"
    java -jar jqm.jar -importjobdef ./jobs/

    echo "### Listing nodes"
    java -jar jqm.jar -nodecount >/jqm/tmp/nodes.txt
    type /jqm/tmp/nodes.txt

    # If first node, upload template and initial configuration.
    grep "Existing nodes: 0" /jqm/tmp/nodes.txt
    if [ $? -eq 0 ]
    then
        echo "### No nodes yet - uploading templates and initial configuration to database"
        java -jar jqm.jar -c selfConfig.swarm.xml
    fi

    return 0
fi

# Go!
echo "### Starting JQM node ${JQM_NODE_NAME}"
java ${JAVA_OPTS} -jar jqm.jar -startnode ${JQM_NODE_NAME}
