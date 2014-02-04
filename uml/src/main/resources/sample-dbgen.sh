#!/bin/sh

jar="javaz-util-full-0.95.jar"
current="2"
prev="1"
cmd="java -cp ${jar} org.javaz.uml.RenderFtl versions/out-current.json"

#variants to proceed templates
bean_whole="1"
bean_by_one="2"
bean_diff="4"

${cmd} create-mysql     $bean_whole     -DtemplatePath=/path/to/templates

${cmd} orm              $bean_by_one    -DtemplatePath=/path/to/templates -Dout=src/java

#one more additional parameter - prev file.
${cmd} update-mysql     $bean_diff      versions/out-ver${prev}.json -DtemplatePath=/path/to/templates -Dv1=${current} -Dv2=${prev}

