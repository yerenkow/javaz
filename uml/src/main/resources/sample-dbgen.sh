#!/bin/sh

jar="javaz-all.jar"
java -jar ${jar} org.javaz.uml.RenderFtl newmodel.json create-mysql 1 -DtemplatePath=/path/to/templates
