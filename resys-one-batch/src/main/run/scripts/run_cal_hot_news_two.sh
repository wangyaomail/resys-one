#!/bin/bash

set -e -x

curdir=`cd $(dirname $0); pwd`
source $curdir/../conf/base.conf
source $curdir/../util/time_util.sh
source $curdir/../util/job_util.sh

hbase_2_hive_db="hbase_external"
hive_table_name="news_all"

now_job_name=$(get_job_name $0).$(get_job_day ${now_day})
hot_file_path="/xmt/data/jobs/hot_${now_job_name}"

if [ -d "$hot_file_path" ]; then
    rm -rf $hot_file_path
fi

hive -S -e "
    insert overwrite local directory '${hot_file_path}' row format delimited fields terminated by '\t'
    SELECT id, hotScore FROM ${hbase_2_hive_db}.${hive_table_name}
    ORDER BY hotScore DESC
    LIMIT 100;
"
# > $hot_file_path @Deprecated because MR info will write in

cat $hot_file_path/* > $hot_file_path.data
rm -rf $hot_file_path

exit $?

