#!/bin/bash
# Author: wangyao

set -e -x

curdir=`cd $(dirname $0); pwd`
source $curdir/../conf/base.conf
source $curdir/../util/time_util.sh
source $curdir/../util/job_util.sh

if [ $# -ge 1 ]; then
    now_day=`date -d"$*" "+%Y-%m-%d %H:%M"`
else # default: today
    now_day=`date -d"today" "+%Y-%m-%d %H:%M"`
fi

jar_name="resys-one-batch"
jar_version="0.1"
job_jar_name=$JAR_PATH/${jar_name}-${jar_version}-jar-with-dependencies.jar

now_job_name=$(get_job_name $0).$(get_job_day ${now_day})
log_file=$LOG_PATH/${now_job_name}.log

# from yesterday to now_day[00:00]
hadoop jar $job_jar_name \
           xmt.resys.batch.algo.AlgoCalNewsClick \
           -D mapreduce.map.memory.mb=256 \
           -D mapreduce.reduce.memory.mb=256 \
           -ul_click "user_list_click" \
           -nw_click "news_all" \
           -start_time $(date -d"$now_day 1 day ago" "+%Y-%m-%d") \
           -end_time $(date -d"$now_day" "+%Y-%m-%d")

exit 0
