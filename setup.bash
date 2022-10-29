#!/usr/bin/bash
set -eu

# 1. run containers
(cd docker; docker-compose up -d leader rep1 rep2 rep3)
sleep 10

# 2. add peers each other
function getUri() {
    cat docker/logs/${1}/pbft.log |\
     grep "Remoting now listens on addresses:" |\
     awk -F"\[" '{print $3}' |\
     sed -e 's/\]//g'
}

function echoCurl() {
    echo "curl -o /dev/null -w %{http_code}\n -s -X POST http://localhost:9000/addPeer -d \"${1}\""
}

function run() {
    r=`${1}`
    if [ $r -ne 200 ]; then
        echo "Oops..."
        exit 1
    fi
}

uri0=`getUri n0.log`
uri1=`getUri n1.log`
uri2=`getUri n2.log`
uri3=`getUri n3.log`

curl0=`echoCurl $uri0`
curl1=`echoCurl $uri1`
curl2=`echoCurl $uri2`
curl3=`echoCurl $uri3`

run $curl0
run $curl1
run $curl2
run $curl3
