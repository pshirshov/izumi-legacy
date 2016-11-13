#!/bin/sh

set -eu
set -o pipefail

SCRIPT_DIR=$(dirname $(readlink -f "$0"))
APP_DIR=${APP_DIR-$SCRIPT_DIR}
HEAPDUMP_DIR=${HEAPDUMP_DIR-$APP_DIR}
GC_LOG_DIR=${GC_LOG_DIR-$APP_DIR}
OOM_COMMAND="/bin/sh ${APP_DIR}/oom-killer.sh %p"
JPROFILER=${JPROFILER-0}

cd $SCRIPT_DIR

echo "Working in  : $PWD"
echo "OOM Command : $OOM_COMMAND"
echo "GC Logs     : $GC_LOG_DIR"

if [ "$JPROFILER" = "1" ]
then
    JPFOFILER_LIB_FIRST=$(find ${APP_DIR} -name libjprofilerti.so -print | head -n 1)
    JAVA_AGENT_ARGS="-agentpath:${JPFOFILER_LIB-$JPFOFILER_LIB_FIRST}=port=8084"
fi

JAVA_ARGS="
    ${JAVA_ARGS-}
    ${JAVA_AGENT_ARGS-}
    -d64
    -server
    -XX:+UseConcMarkSweepGC
    -XX:+UseParNewGC
    -XX:+DoEscapeAnalysis
    -XX:+HeapDumpOnOutOfMemoryError
    -verbose:gc
    -XX:+PrintTenuringDistribution
    -XX:+PrintGCDetails
    -XX:+PrintGCDateStamps
    -XX:+PrintGCTimeStamps
    -XX:+PrintGCApplicationStoppedTime
    -XX:+UseGCLogFileRotation
    -XX:NumberOfGCLogFiles=10
    -XX:GCLogFileSize=1M
    -Dcom.sun.management.jmxremote
    -Dcom.sun.management.jmxremote.authenticate=false
    -Xmx4g
    -XX:MaxMetaspaceSize=256m
    -XX:OnOutOfMemoryError=\"${OOM_COMMAND}\"
    -XX:HeapDumpPath=${HEAPDUMP_DIR}
    -Xloggc:${GC_LOG_DIR}/gc.log
    ${JAVA_ARGS_TAIL-}
    -jar $@"

JAVA_ARGS="$(echo ${JAVA_ARGS} | tr '\n' ' ')"

COMMAND="java $JAVA_ARGS"

echo "Cron..."
crond &

#echo "Telegraf..."
#telegraf ${TELEGRAF_ARGS-} &

echo "Command:"
echo $COMMAND

eval $COMMAND
