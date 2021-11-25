#!/bin/bash
# Author: wangyao

set -e -x

curdir=`cd $(dirname $0); pwd`
source $curdir/../conf/base.conf
source $curdir/../util/logutil.sh

if [ $# -ge 1 ]; then
    now_day=`date -d"$*" "+%Y-%m-%d %H:%M"`
else # default: today
    now_day=`date -d"today" "+%Y-%m-%d %H:%M"`
fi

jar_name="resys-one-batch"
jar_version="0.1"
job_jar_name=$JAR_PATH/${jar_name}-${jar_version}-jar-with-dependencies.jar

now_job_name=`add_timestamp_suffix "$(level_name $0)" "day" ${now_day}`
log_file=$LOG_PATH/${now_job_name}.log

hadoop jar $job_jar_name \
           xmt.resys.batch.load.ImportDictionary \
           -D mapreduce.map.memory.mb=256 \
           -D mapreduce.reduce.memory.mb=256 \
           -t "dic_all" \
           -i "/data/dics/default.dic" > $log_file 2>&1

exit 0
