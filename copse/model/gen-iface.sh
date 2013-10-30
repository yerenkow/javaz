#!/bin/sh

jar="../../build/javaz-util-full-0.95.jar"
ver="1"
java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-ver${ver}.json create-iface 2 -DtemplatePath=templates -DoutPath=../src/main/java/org/javaz/copse/iface -Dpackage=org.javaz.copse -DtablePrefix=copse_
java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-ver${ver}.json create-bean 2 -DtemplatePath=templates -DoutPath=../src/main/java/org/javaz/copse/bean -Dpackage=org.javaz.copse -DtablePrefix=copse_
