#!/bin/sh

jar="../../build/javaz-util-full-0.95.jar"
ver="XXX"

java -cp ${jar} org.javaz.uml.VioletParser copse.class.violet versions/copse-ver${ver}.json
