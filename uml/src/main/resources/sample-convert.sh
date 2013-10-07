#!/bin/sh

jar="javaz-all.jar"
ver="1"

java -jar ${jar} org.javaz.uml.VioletParser in.class.violet out-ver${ver}.json
