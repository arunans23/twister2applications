#!/usr/bin/env bash

APP_DIR=/home/supun/dev/projects/twister2/twister2applications

./bin/twister2 submit nodesmpi jar $APP_DIR/twister2/performance/target/twister2-performance-1.0-SNAPSHOT-jar-with-dependencies.jar edu.iu.dsc.tws.apps.Program 4 100 1000 0 false "8,1"
