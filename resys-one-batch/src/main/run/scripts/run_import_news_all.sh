#!/bin/bash
# Author: wangyao

set -e -x

curdir=`cd $(dirname $0); pwd`
source $curdir/../conf/base.conf

if [ $# -ge 1 ]; then
    now_day=`date -d"$*" "+%Y-%m-%d %H:%M"`
else # default: today
    now_day=`date -d"today" "+%Y-%m-%d %H:%M"`
fi

jar_name="resys-one-batch"
jar_version="0.1"

now_job_name=$(get_job_name $0).$(get_job_day "${now_day}")
log_file=$LOG_PATH/${now_job_name}.log

hadoop jar $JAR_PATH/${jar_name}-${jar_version}-jar-with-dependencies.jar \
           xmt.resys.batch.load.ImportNewsAll \
           -D mapreduce.map.memory.mb=256 \
           -D mapreduce.reduce.memory.mb=256 \
           -t "news_all" \
           -i "/data/resys/news/news_all_2019-11-15.data"  > $log_file 2>&1

exit 0
