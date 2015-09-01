<html>
	<body>
		<h2><a name="intro"/> Introduction</h2>
		<p>JmsStream has both a command-line executable and a Graphical User Interface (GUI) wrapper that records and replays JMS messages from queues or topics. It can capture and send text messages, map message, and binary messages. It can capture, but not sent object messages. The GUI application also provides the ability to safely edit or create a JmsStream message capture file.</p>
		<p>JmsStream is similar in functionality to tools like RvStream, RvFlow, and RvScript, except it is created specifically for JMS. JmsStream also has functions for performance testing, and is compatible with most JMS servers. It is not intended as a replacement for JMS message visualization tools like TIBMon or <a href="http://www.hermesjms.com" target="_blank">Hermes</a>. </p>
		<p>JmsStream provides the following kind of functionality:</p>
		<ul>
			<li>Capturing JMS messages on selected queues/topics, using TCP or SSL connections.</li>
			<li>Capture and save very large messages to ZIP compressed files.</li>
			<li>Re-sending of captured messages, optionally with different destinations, reply destinations, and modified message data.</li>
			<li>Re-sending messages in request/reply mode to a service and capturing the reply either synchronously or asynchronously.</li>
			<li>Re-sending messages for debugging purposes.</li>
			<li>Performance testing of JMS Servers, request/reply services, or message consumers using TCP or SSL messages.</li>
			<li>Create JMS or XA transactional sessions for testing or message capturing/publication from a file.</li>
			<li>Compatible with JBoss Transaction Manager for testing XA behavior and connections.</li>
			<li>Security Testing of JNDI and destinations.</li>
			<li>Testing JMS Server setup.</li>
		</ul>
		<br/>
		<h2><a name="requirements"/>Requirements</h2>
		<p>JmsStream 2.7 is written in Java and compiled using JDK 1.8, but is backwards compatible with JRE version 1.5. Thus a Java run-time environment is needed and this should be at least version 1.5.0 or greater. All of the examples assume that the Java JRE bin directory is in the system path. However, the Windows EXE version of JmsStream and JmsStreamGUI do not require a JRE installation on the machine.</p>
		<p>JmsStream 2.7 supports TIBCO EMS 4.x - 8.x, Apache ActiveMQ 5, and JBoss HornetQ 2, but it should work with any JMS compliant server. JmsStream install file includes the TIBCO EMS 8.2, Apache ActiveMQ 5.5.0, and JBoss HornetQ 2.2.5 client libraries. Therefore no EMS, ActiveMQ, or HornetQ client installation is needed. In order to use other JMS servers you must install and include their client JARs in the CLASSPATH.</p>
		<p>To use the XA features of JmsStream you must download JBoss Transaction Manager 4.11.0 from:<br/>
			<a href="http://www.jboss.org/jbosstm/downloads/4.11.0.Final/binary/jbossts-jta-4.11.0.Final.zip" target="_blank">http://www.jboss.org/jbosstm/downloads/4.11.0.Final/binary/jbossts-jta-4.11.0.Final.zip</a>
		</p>
		<br/>
		<h2><a name="install"/>Installation</h2>
		<p>JmsStream uses 7Zip to compress the installation files. 7Zip files are about half the size of standard ZIP files. 7Zip is open-source and can be downloaded from <a href="http://www.7zip.org" title="7Zip Download">http://www.7zip.org</a>.</p>
		<p>Installation of JmsStream is basically a matter of unzipping <code>JmsStream_x.x.7z</code> file to a local directory. This 7Zip file includes all classes required to run JmsStream, except for the JBoss Transaction Manager 4.11.0 libraries and the Java JRE. To run JmsStream, <code>cd</code> to the directory containing <code>JmsStream.jar</code> and type <code>java -jar JmsStream.jar -?</code>.</p>
		<p>The GUI shell for JmsStream (JmsStreamGUI) requires the <code>JmsStream.jar</code> to be in the same directory as <code>JmsStreamGUI.jar</code> and can be started by typing <code>java -jar JmsStreamGUI.jar</code> with no command line options. </p>
		<p>
			<code>JmsStreamEXE.7z</code> contains the Windows EXE versions of JmsStream, <code>JmsStream.exe </code>and<code> JmsStreamGUI.exe</code>. <code>JmsStreamEXE.7z</code> also contains the JRE necessary to run the EXEs, and it does not require a Java installation. Just unzip <code>JmsStreamEXE.7z</code> file to a local directory and run the application.</p>
		<p>To enable the XA capabilities of JmsStream download <code>jbossts-jta-4.11.0.Final.zip</code> from the JBoss web site, and copy the following JAR files from the ZIP file to the <code>./lib/jbossts</code> directory of the JmsStream root:</p>
		<ul>
			<li>JBOSSTS_4_11_0_Final\lib\jbossjta.jar</li>
			<li>JBOSSTS_4_11_0_Final\lib\ext\commons-logging.jar</li>
			<li>JBOSSTS_4_11_0_Final\lib\ext\log4j.jar</li>
		</ul>
		<p>For example, if your JmsStream root is <code>C:\tibco\jmsstream</code>. Then you will put all of these files in <code>C:\tibco\jmsstream\lib\jbossts</code> directory. The other files in the <code>jbossts-jta-4.11.0.Final.zip</code> are not needed by JmsStream. </p>
		<p>See <a href="running.htm" target="_self">Running JmsStream</a> for more details.</p>
		<h2><a name="sourceCode"/>Source Code</h2>
		<p>JmsStream was developed over ten year period with several people contributing bits and pieces.  It was originally ported from C code, and evolved from a quick and dirty command line application for replaying message into a more sophisticated JMS testing tool.  No attempt was made to redesign or modularize the code.  However, through the years JmsStream's code has been optimized for performance and user functionality. </p>
		<hr/>
		<address>
			<a href="mailto:abailey@tibco.com">abailey@tibco.com</a>
		</address>
	</body>
</html>