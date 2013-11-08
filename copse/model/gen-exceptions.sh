#!/bin/sh

jar="../../build/javaz-util-full-0.95.jar"
src="../src/main/java/"
bean_whole="1"
bean_by_one="2"
bean_difference="4"

java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-exceptions-current.json create-exc ${bean_by_one} -DtemplatePath=templates -DoutPath=${src}org/javaz/copse/exc -Dpackage=org.javaz.copse -DtablePrefix=copse_




