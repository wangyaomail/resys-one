#!/bin/bash
# Author: wangyao

set -e -x

echo "upload hotnews list to couchbase"

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

now_job_name="cal_hot_score_two.$(get_job_day ${now_day})"
hot_file_path="/xmt/data/jobs/hot_${now_job_name}.data"

java -classpath ${HADOOP_CLASSPATH}:$job_jar_name xmt.resys.batch.serve.ServeHotNews \
     -cb_url xmt \
     -cb_bucket resys \
     -cb_username root \
     -cb_pwd 123456 \
     -cb_ttl 72 \
     -local_hotfile $hot_file_path \
     -cb_general hot_news_list \
     -cb_key hot_${now_day}

exit 0
