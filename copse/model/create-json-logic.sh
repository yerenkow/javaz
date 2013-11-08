#!/bin/sh

#just don't forget increment here.
ver="1"

jar="../../build/javaz-util-full-0.95.jar"
java -cp ${jar} org.javaz.uml.VioletParser copse-logic.class.violet.html versions/copse-logic-ver${ver}.json
cp versions/copse-logic-ver${ver}.json versions/copse-logic-current.json
