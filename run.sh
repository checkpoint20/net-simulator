#!/bin/bash

export JAVA_HOME="/opt/java"
#export JAVA_HOME="/opt/jdk1.5.0_11.x64"
#export JAVA_HOME="/opt/jdk1.5.0_10.i386"

#export LANG=C

$JAVA_HOME/bin/java -Djava.util.logging.config.file=bin/logging.properties \
-classpath bin:lib/commons-cli-1.0.jar:lib/xercesImpl.jar:lib/serializer.jar:lib/commons-lang-2.3.jar \
org.netsimulator.Netsimulator ##> startlog.txt 2>&1