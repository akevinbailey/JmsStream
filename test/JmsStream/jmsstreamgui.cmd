@echo off

set MY_JAVA=enter_your_jvm

set JMSSTREAM_BIN=.
set JMS_CLASSPATH=%JMSSTREAM_BIN%\JmsStreamGUI.jar

IF NOT EXIST %MY_JAVA% set MY_JAVA=java

%MY_JAVA% -cp %JMS_CLASSPATH% -Xmx128m com.tibco.util.gui.JmsStreamGUI
