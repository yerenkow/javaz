#!/bin/sh

#adjust path
jar="javaz-util-full-0.95.jar"
#write here current version, or provide it otherwise
ver="1"

java -cp ${jar} org.javaz.uml.VioletParser in.class.violet versions/out-ver${ver}.json
#make sure that "out-current.json" is always recent one.
cp versions/out-ver${ver}.json versions/out-current.json
