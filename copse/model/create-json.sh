#!/bin/sh

#just don't forget increment here.
ver="4"

jar="../../build/javaz-util-full-0.95.jar"
java -cp ${jar} org.javaz.uml.VioletParser copse.class.violet.html versions/copse-ver${ver}.json
cp versions/copse-ver${ver}.json versions/copse-current.json
