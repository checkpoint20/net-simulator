#!/bin/sh

java \
    -Djava.util.logging.config.file=cfg/logging.properties \
    -jar netsimulator.jar
