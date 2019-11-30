#!/bin/bash

function get_job_day
{
    specified_time=`date +"%Y-%m-%d %H:%M:%S"`
    if [ $# -ge 1 ]; then
        specified_time=$1
    fi
    echo `date -d"$specified_time" '+%Y-%m-%d'`
}

# set on the hour
function get_job_hour
{
    specified_time=`date +"%Y-%m-%d %H:%M:%S"`
    if [ $# -ge 1 ]; then
        specified_time=$1
    fi
    echo `date -d"$specified_time" '+%Y-%m-%d-%H'`
}

# should offer period or period=10
function get_job_minute
{
    specified_time=`date +"%Y-%m-%d %H:%M:%S"`
    freq=20
    if [ $# -ge 2 ]; then 
        freq=$1
        specified_time=$2
    elif [ $# -ge 1 ]; then
        specified_time=$1
    fi
    now_min=`date -d"$specified_time" '+%-M'`
    now_min=`printf "%02d" $((now_min/freq*freq))`
    echo `date -d"$specified_time" '+%Y-%m-%d-%H'`-$now_min
}

