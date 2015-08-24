#!/bin/sh

MY_JAVA=
JMSSTREAM_BIN=.
JMS_CLASSPATH=$JMSSTREAM_BIN/JmsStream.jar

if [ -z $MY_JAVA ]
then
   MY_JAVA=java
fi

$MY_JAVA -cp $JMS_CLASSPATH -Xmx32m com.tibco.util.JmsStream $@
