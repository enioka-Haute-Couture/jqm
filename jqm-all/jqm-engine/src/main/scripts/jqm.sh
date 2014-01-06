#!/bin/sh
# SRE - 2014

##############################
##### Check and setup Env ####
##############################

ACTION=$1

LOCAL_DIR=$(dirname $0)
JQM_JAR=${LOCAL_DIR}/jqm.jar

if [[ $JQM_NODE == "" ]]
then
	echo "Defaulting to $USER as node name. Set JQM_NODE to use another node name"
	JQM_NODE=$USER
fi

JAVA=${JAVA_HOME:-/usr/java7_64/jre/}/bin/java
$JAVA -version  > /dev/null 2>&1
if [[ $? -ne 0 ]]
then
        echo "No java found. Please define JAVA_HOME"
        exit 1
fi


JQM_PID_FILE=jqm_${JQM_NODE}.pid
JQM_LOG_FILE=jqm_${JQM_NODE}.log


#############################################
#### Start/Stop/Restart/Status functions ####
#############################################

jqm_start() {
	echo Starting JQM...
	test -e ${LOCAL_DIR}/${JQM_PID_FILE}
        if [[ $? -eq 0 ]]
        then
                echo "PID file found (${LOCAL_DIR}/${JQM_PID_FILE})."
		JQM_PID=$(cat ${LOCAL_DIR}/${JQM_PID_FILE})
        	ps -p $JQM_PID > /dev/null 2>&1
        	if [[ $? -ne 0 ]]
        	then
                	echo "PID file is here (${JQM_PID_FILE}) but JQM daemon is gone..."
                	echo "Cleaning up pid file"
                	rm ${LOCAL_DIR}/${JQM_PID_FILE}
        	else
               		echo "JQM is already running with PID ${JQM_PID} for node ${JQM_NODE}"
                	exit 1
        	fi
        fi
	# We can go on...
	nohup $JAVA -jar $JQM_JAR $JQM_NODE 2>&1 > ${LOCAL_DIR}/$JQM_LOG_FILE &
	JQM_PID=$!
	echo $JQM_PID > ${LOCAL_DIR}/${JQM_PID_FILE}
	echo "JQM Started with pid ${JQM_PID}"
}

jqm_stop() {
	echo Stopping JQM...
	test -e ${LOCAL_DIR}/${JQM_PID_FILE}
	if [[ $? -ne 0 ]]
	then
        	echo "PID file not found (${LOCAL_DIR}/${JQM_PID_FILE})."
		echo "Here are all jqm engine running on this host, choose the good one and kill it yourself:"
		ps -ef |grep $JQM_JAR | grep -v grep
        	exit 1
	fi
	JQM_PID=$(cat ${LOCAL_DIR}/${JQM_PID_FILE})
	echo "Killing process $JQM_PID"
	kill $JQM_PID
	rm ${LOCAL_DIR}/${JQM_PID_FILE}
}

jqm_status() {
	test -e ${LOCAL_DIR}/${JQM_PID_FILE}
	if [[ $? -ne 0 ]]
	then
        	echo "PID file not found (${LOCAL_DIR}/${JQM_PID_FILE})."
		echo "Here are all jqm engine running on this host"
		ps -ef |grep $JQM_JAR | grep -v grep
        	exit 1
	fi
	JQM_PID=$(cat ${LOCAL_DIR}/${JQM_PID_FILE})
	ps -p $JQM_PID > /dev/null 2>&1
	if [[ $? -ne 0 ]]
	then
		echo "PID file is here (${JQM_PID_FILE}) but JQM daemon is gone..."
		echo "Cleaning up pid file"
		rm ${LOCAL_DIR}/${JQM_PID_FILE}
		exit 1
	else
		echo "JQM is running with PID ${JQM_PID}"
	fi
}



###############################
##### Decide what to do... ####
###############################
case "$ACTION" in
	'start')
		jqm_start
		;;
	'stop')
		jqm_stop
		;;
	'restart')
		jqm_stop
		jqm_start
		;;
	'status')
		jqm_status
		;;
	*)
		echo "Usage: $0 {start|stop|restart|status}"
		;;
esac
