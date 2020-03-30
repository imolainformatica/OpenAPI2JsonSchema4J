#!/bin/bash
JAVA_HOME=/nfsbackup/build/jdk1.8.0_201
PATH=$JAVA_HOME/bin:$PATH
export JAVA_HOME PATH
BASEDIR=$(dirname "$0")
DEBUG_API_CALL=false
cd "$BASEDIR"
java -jar -Dlogback.configurationFile=conf/logback.xml -DDEBUG_API=$DEBUG_API_CALL jar/frontend-api-definition-adapter*.jar --config conf/environment.properties "$@"