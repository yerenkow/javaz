#!/bin/sh

jar="../../build/javaz-util-full-0.95.jar"
ver="1"

java -cp ${jar} org.javaz.uml.VioletParser copse.class.violet.html versions/copse-ver${ver}.json
