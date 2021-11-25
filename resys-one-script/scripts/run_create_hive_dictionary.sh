#!/bin/bash

set -e -x

curdir=`cd $(dirname $0); pwd`
source $curdir/../conf/base.conf
source $curdir/../util/time_util.sh
source $curdir/../util/job_util.sh

hbase_2_hive_db="hbase_external"
hbase_table_name="dic_all"
hive_table_name="dic_all"

hive -e "
    USE $hbase_2_hive_db;
    CREATE EXTERNAL TABLE IF NOT EXISTS $hive_table_name (id STRING, name STRING, nature STRING)
    STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
    WITH SERDEPROPERTIES (\"hbase.columns.mapping\" = \"data:name, data:nature\")
    TBLPROPERTIES(\"hbase.table.name\" = \"$hbase_table_name\");
"
hadoop fs -chmod 755 /user/hive/warehouse/${hbase_2_hive_db}.db/$hive_table_name

exit $?

