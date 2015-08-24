@echo off

set MY_JAVA=enter_your_jvm

set JMSSTREAM_BIN=.
set JMS_CLASSPATH=%JMSSTREAM_BIN%\JmsStream.jar

IF NOT EXIST %MY_JAVA% set MY_JAVA=java

%MY_JAVA% -cp %JMS_CLASSPATH% -Xmx32m com.tibco.util.JmsStream %*%
