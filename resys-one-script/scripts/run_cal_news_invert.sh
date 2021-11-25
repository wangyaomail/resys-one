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

# from yesterday to now_day[00:00]
hadoop jar $job_jar_name \
           xmt.resys.batch.algo.AlgoNewsInvertedWithoutToken \
           -D mapreduce.map.memory.mb=256 \
           -D mapreduce.reduce.memory.mb=256 \
           -table_news "news_all" \
           -table_news_invert "news_invert" \
           -table_dic "dic_all" \
           -word_max_threshold  1000 \
           -word_min_threshold 3 \
           -recall_limit 200

exit 0
