#!/usr/bin/env bash
for i in {1..65535}; do (exec 2>&-; echo > /dev/tcp/localhost/$i && echo $i is open); done