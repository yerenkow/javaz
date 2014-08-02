#!/bin/sh

jar="../../uml/target/javaz-uml-1.4-SNAPSHOT-jar-with-dependencies.jar"
src="../src/main/java/"
bean_whole="1"
bean_by_one="2"
bean_difference="4"
params="-DtemplatePath=templates -Dpackage=org.javaz.copse -DtablePrefix=copse_"
java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-current.json create-iface ${bean_by_one} -DoutPath=${src}org/javaz/copse/iface ${params}
java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-current.json create-bean ${bean_by_one}  -DoutPath=${src}org/javaz/copse/abs ${params}
java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-current.json create-helper ${bean_by_one}  -DoutPath=${src}org/javaz/copse/helper ${params}
