#!/bin/sh

java \
    -Djava.util.logging.config.file=cfg/logging.properties \
    -jar bin/netsimulator-1.1-SNAPSHOT.jar
