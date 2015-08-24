#!/bin/sh

MY_JAVA=
JMSSTREAM_BIN=.
JMS_CLASSPATH=$JMSSTREAM_BIN/JmsStreamGUI.jar

if [ -z $MY_JAVA ]
then
   MY_JAVA=java
fi

nohup $MY_JAVA -cp $JMS_CLASSPATH -Xmx128m com.tibco.util.gui.JmsStreamGUI > jmsstreamgui.out 2>&1 &
