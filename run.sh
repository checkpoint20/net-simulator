#!/bin/sh

java -Djava.util.logging.config.file=bin/logging.properties \
-classpath bin:lib/commons-cli-1.0.jar:lib/xercesImpl.jar:lib/serializer.jar:lib/commons-lang-2.3.jar \
org.netsimulator.Netsimulator
