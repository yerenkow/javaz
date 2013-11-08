#!/bin/sh

#just don't forget increment here.
ver="1"

jar="../../build/javaz-util-full-0.95.jar"
java -cp ${jar} org.javaz.uml.VioletParser copse-exceptions.class.violet.html versions/copse-exceptions-ver${ver}.json
cp versions/copse-exceptions-ver${ver}.json versions/copse-exceptions-current.json
