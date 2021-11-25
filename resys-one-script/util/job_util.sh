#!/bin/bash

function get_job_name
{
    script_name=$1
    package_name=`basename $script_name`
    package_name=${get_package_name%.*}
    package_name=`echo $get_package_name | sed -e's/-/_/g'`
    package_name=${get_package_name##run_}
    echo $package_name
}