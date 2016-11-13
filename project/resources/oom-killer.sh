#!/bin/sh

echo "$(date) killing $1..." >> oom.log
kill -9 $1
echo "$(date) process $1 killed" >> oom.log

if [[ -f java_pid_singleton.hprof ]]; then
    echo "overriding latest_heap_dump.hprof as requested"
    mv java_pid_singleton.hprof latest_heap_dump.hprof
fi
