#!/bin/sh

jar="javaz-util-full-0.95.jar"
java -cp ${jar} org.javaz.uml.RenderFtl newmodel.json create-mysql 1 -DtemplatePath=/path/to/templates
