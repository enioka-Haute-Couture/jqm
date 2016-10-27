#!/bin/sh
# SRE - 2014

# Go to JQM DIR. This is needed as JQM builds its path from its starting dir
cd $(dirname $0)

##############################
##### SUDO                ####
##############################

if [ $(whoami) = "root" ]
then
    if [ "x${JQM_SERVICE_ACCOUNT}" = "x" ]
    then        
        echo "Defaulting to account jqm for running the script. Set JQM_SERVICE_ACCOUNT to use a different account."
        JQM_SERVICE_ACCOUNT="jqm"
    fi
    echo "Re-launching script as ${JQM_SERVICE_ACCOUNT}"
    su - ${JQM_SERVICE_ACCOUNT} -c "export JQM_NODE=\${JQM_NODE:-${JQM_NODE}}; $(pwd -P)/$(basename $0) $*"
    exit $?
fi


##############################
##### Check and setup Env ####
##############################

ACTION=$1

JQM_JAR="jqm.jar"  # Name of JQM Java archive file
WAITING_TIME=70    # Seconds to wait for JQM to shutdown gracefully

if [ "x${JQM_NODE}" = "x" ]
then
 echo "Defaulting to $USER as node name. Set JQM_NODE to use another node name"
 JQM_NODE=$USER
fi

# Java extra options that can be used to customise memory settings
JAVA_OPTS=${JAVA_OPTS:--Xms128m -Xmx512m -XX:MaxPermSize=128m}

JAVA="false"
if [ ! "x${JAVA_HOME}" = "x" ]
then
 JAVA=${JAVA_HOME}/bin/java
else
 JAVA=$(which java 2>/dev/null)
 if [ $? -ne 0 ]
 then
  JAVA="/usr/java6_64/jre/bin/java"
 fi
fi
$JAVA -version > /dev/null 2>&1
if [ $? -ne 0 ]
then
        echo "No java found. Please define JAVA_HOME or put java inside PATH"
        exit 1
fi
JAVA="$JAVA $JAVA_OPTS "

OOM=${JAVA_OOM_ACTION:-""}
if [ "x${OOM}" = "x" ]
then
	if [ "$(uname)" = "AIX" ]
	then
		OOM='-Xdump:tool:events=throw,filter=java/lang/OutOfMemoryError,exec=kill -9 $PPID'
	fi
	if [ "$(uname)" = "Linux" -o "$(uname)" = "Darwin" ]
	then
        # Note that OOM action can fail on Linux if not enough system memory is available at the time of crash
        # See https://bugs.openjdk.java.net/browse/JDK-8027434
        OOM='-XX:OnOutOfMemoryError=kill -9 $PPID'
	fi
fi

JQM_PID_FILE=tmp/jqm_${JQM_NODE}.pid
JQM_LOG_OUT_FILE=logs/jqm_${JQM_NODE}_out
JQM_LOG_ERR_FILE=logs/jqm_${JQM_NODE}_err
STDOUT_NPIPE=logs/stdout_$$.pipe
STDERR_NPIPE=logs/stderr_$$.pipe
JQM_LOG_HISTORY=30  # Days of log history to keep

# Helper function to log output with rotation on time basis (per hour)
# Arg 1 is log file prefix
log_rotate() {
 while read i
    do
        echo "$i" >> $1_$(date +%Y.%m.%d-%H).log
    done
}

# Helper function to cleanup log pipes
remove_npipes() {
 rm logs/stdout_*.pipe logs/stderr_*.pipe > /dev/null 2>&1
}


#############################################
#### Start/Stop/Restart/Status functions ####
#############################################

jqm_start() {
 echo Starting JQM...
 test -e ${JQM_PID_FILE}
        if [ $? -eq 0 ]
        then
                echo "PID file found (${JQM_PID_FILE})."
  JQM_PID=$(cat ${JQM_PID_FILE})
         ps -p $JQM_PID > /dev/null 2>&1
         if [ $? -ne 0 ]
         then
                 echo "PID file is here (${JQM_PID_FILE}) but JQM daemon is gone..."
                 echo "Cleaning up pid file"
                 rm ${JQM_PID_FILE}
         else
                 echo "JQM is already running with PID ${JQM_PID} for node ${JQM_NODE}"
                 exit 1
         fi
        fi
 # Remove old logs
 for JQM_LOG_FILE in $JQM_LOG_OUT_FILE $JQM_LOG_ERR_FILE
 do
  find logs -name "$(basename ${JQM_LOG_FILE})*.log" -mtime +${JQM_LOG_HISTORY} -exec rm {} \;
 done
 # We can go on...
 if [ "$1" = "console" ]
 then
  $JAVA "$OOM" -jar $JQM_JAR -startnode $JQM_NODE
 else
  remove_npipes
  mkfifo $STDOUT_NPIPE
  mkfifo $STDERR_NPIPE
  log_rotate <$STDOUT_NPIPE $JQM_LOG_OUT_FILE &
  log_rotate <$STDERR_NPIPE $JQM_LOG_ERR_FILE &
  exec 1> $STDOUT_NPIPE
  exec 2> $STDERR_NPIPE
  nohup $JAVA "$OOM" -jar $JQM_JAR -startnode $JQM_NODE &
  JQM_PID=$!
  echo $JQM_PID > ${JQM_PID_FILE}
  echo "JQM Started with pid ${JQM_PID}"
 fi
}

jqm_stop() {
 echo Stopping JQM...
 test -e ${JQM_PID_FILE}
 if [ $? -ne 0 ]
 then
         echo "PID file not found (${JQM_PID_FILE})."
  echo "Here are all jqm engines running on this host, choose the good one and kill it yourself:"
  ps -ef |grep $JQM_JAR | grep -v grep
         exit 1
 fi
 JQM_PID=$(cat ${JQM_PID_FILE})
 echo "Sending SIGTERM to process $JQM_PID and waiting for graceful shutdown."
 GRACEFUL_KILL="no"
 kill $JQM_PID
 while [ $(( WAITING_TIME -= 1 )) -ge 0 ]
 do
  printf "."
  ps -p $JQM_PID > /dev/null 2>&1
  if [ $? -ne 0 ]
  then
   GRACEFUL_KILL="yes"
   break;
  fi
  sleep 1
 done
 echo ""
 if [ $GRACEFUL_KILL = "no" ]
 then
  echo "JQM Engine did not respond to SIGTERM. Killing (SIGKILL) JQM Engine..."
  kill -9 $JQM_PID
 else
  echo "JQM engine shutdown properly."
 fi
 rm ${JQM_PID_FILE}
 remove_npipes
}

jqm_status() {
 test -e ${JQM_PID_FILE}
 if [ $? -ne 0 ]
 then
        echo "PID file not found (${JQM_PID_FILE})."
  echo "Here are all jqm engines running on this host"
  ps -ef |grep $JQM_JAR | grep -v grep
        exit 1
 fi
 JQM_PID=$(cat ${JQM_PID_FILE})
 ps -p $JQM_PID > /dev/null 2>&1
 if [ $? -ne 0 ]
 then
  echo "PID file is here (${JQM_PID_FILE}) but JQM daemon is gone..."
  echo "Cleaning up pid file"
  rm ${JQM_PID_FILE}
  remove_npipes
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
 $JAVA -jar $JQM_JAR -importjobdef $(find jobs -name "*xml" -type f | grep -v pom.xml | tr "\\n" ",")
}

jqm_export_job_def_xml() {
 $JAVA -jar $JQM_JAR -exportjobdef $1
}

jqm_enable_gui() {
 $JAVA -jar $JQM_JAR -w enable
 $JAVA -jar $JQM_JAR -r "$1"
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
 exportjobdef)
  jqm_export_job_def_xml $2
  ;;
 enablegui)
  jqm_enable_gui "$2"
  ;;
 *)
  echo "Usage: $0 {start|stop|restart|status|createnode|importxml|allxml|enqueue <jobname>|startconsole|enablegui <rootpassword>}"
  ;;
esac
