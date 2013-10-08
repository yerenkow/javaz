#!/bin/sh

jar="javaz-util-full-0.95.jar"
ver="1"

java -cp ${jar} org.javaz.uml.VioletParser in.class.violet out-ver${ver}.json
