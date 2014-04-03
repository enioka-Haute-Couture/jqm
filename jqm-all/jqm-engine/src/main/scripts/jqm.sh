#!/bin/sh
# SRE - 2014

##############################
##### Check and setup Env ####
##############################

ACTION=$1

# Go to JQM DIR. This is needed as JQM builds its path from its starting dir
cd $(dirname $0)

JQM_JAR="jqm.jar"

if [[ $JQM_NODE == "" ]]
then
	echo "Defaulting to $USER as node name. Set JQM_NODE to use another node name"
	JQM_NODE=$USER
fi

JAVA=${JAVA_HOME:-/usr/java6_64/jre/}/bin/java
$JAVA -version  > /dev/null 2>&1
if [[ $? -ne 0 ]]
then
        echo "No java found. Please define JAVA_HOME"
        exit 1
fi


JQM_PID_FILE=jqm_${JQM_NODE}.pid
JQM_LOG_OUT_FILE=logs/jqm_${JQM_NODE}_out.log
JQM_LOG_ERR_FILE=logs/jqm_${JQM_NODE}_err.log
JQM_LOG_HISTORY=30  # Days of log history to keep


#############################################
#### Start/Stop/Restart/Status functions ####
#############################################

jqm_start() {
	echo Starting JQM...
	test -e ${JQM_PID_FILE}
        if [[ $? -eq 0 ]]
        then
                echo "PID file found (${JQM_PID_FILE})."
		JQM_PID=$(cat ${JQM_PID_FILE})
        	ps -p $JQM_PID > /dev/null 2>&1
        	if [[ $? -ne 0 ]]
        	then
                	echo "PID file is here (${JQM_PID_FILE}) but JQM daemon is gone..."
                	echo "Cleaning up pid file"
                	rm ${JQM_PID_FILE}
        	else
               		echo "JQM is already running with PID ${JQM_PID} for node ${JQM_NODE}"
                	exit 1
        	fi
        fi
	# Rotate previous logs if it exceeds 10 Mo
	for JQM_LOG_FILE in $JQM_LOG_OUT_FILE $JQM_LOG_ERR_FILE
	do
		if [[ -e $JQM_LOG_FILE ]] && [[  $(du -sm $JQM_LOG_FILE | awk '{ print $1 }') -gt 10 ]]
		then
			mv $JQM_LOG_FILE ${JQM_LOG_FILE}.$(date +%Y%m%H%M%S)
			find logs -name "$(basename ${JQM_LOG_FILE})*" -mtime +${JQM_LOG_HISTORY} | xargs -r rm
		fi
	done
	# We can go on...
	if [[ $1 == "console" ]]
	then
		$JAVA -jar $JQM_JAR -startnode $JQM_NODE
	else
		nohup $JAVA -jar $JQM_JAR -startnode $JQM_NODE > $JQM_LOG_OUT_FILE 2> $JQM_LOG_ERR_FILE &
		JQM_PID=$!
		echo $JQM_PID > ${JQM_PID_FILE}
		echo "JQM Started with pid ${JQM_PID}"
	fi
}

jqm_stop() {
	echo Stopping JQM...
	test -e ${JQM_PID_FILE}
	if [[ $? -ne 0 ]]
	then
        	echo "PID file not found (${JQM_PID_FILE})."
		echo "Here are all jqm engine running on this host, choose the good one and kill it yourself:"
		ps -ef |grep $JQM_JAR | grep -v grep
        	exit 1
	fi
	JQM_PID=$(cat ${JQM_PID_FILE})
	echo "Killing process $JQM_PID"
	kill $JQM_PID
	rm ${JQM_PID_FILE}
}

jqm_status() {
	test -e ${JQM_PID_FILE}
	if [[ $? -ne 0 ]]
	then
        	echo "PID file not found (${JQM_PID_FILE})."
		echo "Here are all jqm engine running on this host"
		ps -ef |grep $JQM_JAR | grep -v grep
        	exit 1
	fi
	JQM_PID=$(cat ${JQM_PID_FILE})
	ps -p $JQM_PID > /dev/null 2>&1
	if [[ $? -ne 0 ]]
	then
		echo "PID file is here (${JQM_PID_FILE}) but JQM daemon is gone..."
		echo "Cleaning up pid file"
		rm ${JQM_PID_FILE}
		exit 1
	else
		echo "JQM is running with PID ${JQM_PID}"
	fi
}

jqm_createnode() {
	$JAVA -jar $JQM_JAR -createnode $JQM_NODE
}

jqm_enqueue() {
	$JAVA -jar $JQM_JAR -enqueue $1
}

jqm_import_xml() {
	$JAVA -jar $JQM_JAR -importjobdef $1
}

jqm_import_all_xml() {
	$JAVA -jar $JQM_JAR -importjobdef $(find jobs -name "*xml" -type f | tr "\\n" ",")
}


###############################
##### Decide what to do... ####
###############################
case "$ACTION" in
	start)
		jqm_start
		;;
	startconsole)
		jqm_start "console"
		;;
	stop)
		jqm_stop
		;;
	restart)
		jqm_stop
		jqm_start
		;;
	status)
		jqm_status
		;;
	createnode)
		jqm_createnode
		;;
	enqueue|run|execute)
		jqm_enqueue $2
		;;
	importxml|import|xml)
		jqm_import_xml $2
		;;
	importallxml|allxml)
		jqm_import_all_xml
		;;
	*)
		echo "Usage: $0 {start|stop|restart|status|createnode|importxml|allxml|enqueue}"
		;;
esac
