#!/bin/sh

jar="../../build/javaz-util-full-0.95.jar"
src="../src/main/java/"
bean_whole="1"
bean_by_one="2"
bean_difference="4"

java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-logic-current.json create-logic-iface ${bean_by_one} -DtemplatePath=templates -DoutPath=${src}org/javaz/copse/logic/iface -Dpackage=org.javaz.copse -DtablePrefix=copse_
java -cp ${jar} org.javaz.uml.RenderFtl versions/copse-logic-current.json create-logic-impl ${bean_by_one} -DtemplatePath=templates -DoutPath=${src}org/javaz/copse/logic/impl -Dpackage=org.javaz.copse -DtablePrefix=copse_



