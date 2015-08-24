/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util;

import com.tibco.util.jmshelper.Base64;
import com.tibco.util.jmshelper.ConnectionHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.jms.*;
import javax.jms.Queue;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @deprecated  This class is deprecated and will be removed in a future release.
 *              It is only used for backward compatibility with JmsStream version 1.
 *
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "unchecked", "UnusedDeclaration", "UnnecessaryBoxing", "StatementWithEmptyBody", "unused"})
public final class JmsStreamXmlPublisher extends DefaultHandler implements Runnable
{
    // internal variables
    private String _buffer = "";
    private String _propertyName;
    private String _propertyType;
    private String _nodeName = "DATA";
    private String _nodeType;

    private final Hashtable _pubHash = new Hashtable();
    private final Hashtable _env;
    private ConnectionHelper _chFactory;
    private int _messageCount = 0;
    private long _intMillis = 1000;
    private int _intRate;
    private long _startTime = 0;
    private final boolean _isReqReply;
    private String _messageSelector = "";

    private final SimpleDateFormat _formatter;
    private XMLReader _producer;

    // JMS Properties default values
    private int _deliveryMode = DeliveryMode.NON_PERSISTENT;
    private int _priority = 4;
    private long _timeToLive = 0; // expiration in milliseconds

    // JMS Variables for TOPIC publisher and QUEUE sender
    private TopicConnectionFactory _topicFactory;
    private TopicConnection _topicConnection;
    private TopicSession _topicSession;
    private TopicPublisher _topicPublisher;
    private Topic _topic;

    private QueueConnectionFactory _queueFactory;
    private QueueConnection _queueConnection;
    private QueueSession _queueSession;
    private QueueSender _queueSender;
    private Queue _queue;
    private Message _msg;

    // Reply threads
    public ThreadGroup _replyThreadGroup;

    public JmsStreamXmlPublisher(Hashtable env)
    {
        int intAcknowledge;
        this._env = env;

        // Set the playback rate.
        // In order to allow fractional rates i.e. (0.5 msg/sec) we have to take the integer value and put it as the
        // rate.  Then take the decimal value a adjust the _intMillis wait time to get an accurate msg/sec rate.
        Float fltRate = ((Float)_env.get("rate"));
        if (fltRate == 0) _intRate = 0;
        else {
            _intRate = Math.round(fltRate.floatValue());
            if (fltRate < 1) {
                //  Send less than one msg a sec.
                _intRate = 1;
                _intMillis = Math.round(1000 / fltRate.doubleValue());
            }
            else if (fltRate.doubleValue() != Math.rint(fltRate.doubleValue())) {
                // fltRate is NOT an integer.  Round the value so we an just increase or decrease the _intMillis wait
                // by 50%.  i.e. between 500 and 1500 milliseconds.
                _intRate = Math.round(fltRate.floatValue());
                double dblAdj = fltRate.doubleValue() < Math.rint(fltRate) ?
                                (fltRate.doubleValue() - Math.floor(fltRate.doubleValue())) :
                                1 + (fltRate.doubleValue() - Math.floor(fltRate.doubleValue()));
                _intMillis = Math.round(1000 / dblAdj);
            }
            else {
                // fltRate is an integer.
                _intRate = fltRate.intValue();
                _intMillis = 1000;
            }
        }

        _isReqReply = (Boolean)env.get("requestreply");

        if (_isReqReply) {
            _replyThreadGroup = new ThreadGroup("ReplyThreads");
        }

        // get a readable dateformat for on-screen information
        _formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

        if (env.get("noack").equals(Boolean.TRUE))
            intAcknowledge = Session.CLIENT_ACKNOWLEDGE;
        else
            intAcknowledge = Session.AUTO_ACKNOWLEDGE;

        try {
            _chFactory = new ConnectionHelper(env);
            // Set the JMS type if given, otherwise ConnectionHelper derive it from the connection factory
            if (_env.containsKey("type")) {
                if (_env.get("type").toString().equalsIgnoreCase("topic")) _chFactory.setFactoryType(ConnectionHelper.TOPIC);
                else if (_env.get("type").toString().equalsIgnoreCase("queue")) _chFactory.setFactoryType(ConnectionHelper.QUEUE);
                else if (_env.get("type").toString().equalsIgnoreCase("generic")) _chFactory.setFactoryType(ConnectionHelper.GENERIC);
                else System.err.println("ERROR: Unsupported -type " + _env.get("type").toString());
            }

            // Create the connection factory
            _chFactory.createConnectionFactory();

            switch (_chFactory.getFactoryType()) {
                case ConnectionHelper.QUEUE:
                    _queueFactory = (QueueConnectionFactory)_chFactory.getConnectionFactory();
                    _queueConnection = _queueFactory.createQueueConnection(env.get("user").toString(),env.get("password").toString());
                    if (_env.containsKey("clientid")) _queueConnection.setClientID(_env.get("clientid").toString());
                    _queueSession = _queueConnection.createQueueSession(false, intAcknowledge);
                    //msg = _queueSession.createTextMessage(); // default msg type
                    break;
                case ConnectionHelper.TOPIC:
                    _topicFactory = (TopicConnectionFactory)_chFactory.getConnectionFactory();
                    _topicConnection = _topicFactory.createTopicConnection(env.get("user").toString(),env.get("password").toString());
                    if (_env.containsKey("clientid")) _topicConnection.setClientID(_env.get("clientid").toString());
                    _topicSession = _topicConnection.createTopicSession(false, intAcknowledge);
                    //msg = _queueSession.createTextMessage(); // default msg type
                    break;
                case ConnectionHelper.XA_QUEUE: // not implemented
                    _queueFactory = (QueueConnectionFactory)_chFactory.getConnectionFactory();
                    _queueConnection = _queueFactory.createQueueConnection(env.get("user").toString(),env.get("password").toString());
                    if (_env.containsKey("clientid")) _queueConnection.setClientID(_env.get("clientid").toString());
                    _queueSession = _queueConnection.createQueueSession(true, intAcknowledge);
                    //msg = _queueSession.createTextMessage(); // default msg type
                    break;
                case ConnectionHelper.XA_TOPIC: // not implemented
                    _topicFactory = (TopicConnectionFactory)_chFactory.getConnectionFactory();
                    _topicConnection = _topicFactory.createTopicConnection(env.get("user").toString(),env.get("password").toString());
                    if (_env.containsKey("clientid")) _topicConnection.setClientID(_env.get("clientid").toString());
                    _topicSession = _topicConnection.createTopicSession(true, intAcknowledge);
                    //msg = _queueSession.createTextMessage(); // default msg type
                    break;
                default:
                    // throw an exception
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // create the XMLReader
        try {
            _producer = XMLReaderFactory.createXMLReader(env.get("xmlreaderclass").toString());
        }
        catch (SAXException e) {
            System.err.println("Can't get parser, check configuration: " + e.getMessage());
            return;
        }

        // create and register the consumer for the events
        try {
            _producer.setContentHandler(this);
            _producer.setErrorHandler(this);
        }
        catch (Exception e) {
            System.err.println ("Can't setup consumer: " + e.getMessage ());
        }
    }

    public void run()
    {
        System.out.println(" " + JmsStream.APP_NAME + " started");
        System.out.println(" -----------------");
        System.out.println(" jms client      :  " + _env.get("jmsclient").toString());
        System.out.println(" provider url    :  " + _env.get(javax.naming.Context.PROVIDER_URL).toString());
        System.out.println(" factory name:  " + _env.get("connectionfactory").toString());
        System.out.println(" factory type:  " + _chFactory.getFactoryDescription());
        System.out.println(" message file:  " + _env.get("file").toString());
        System.out.println(" publishing started:  " + _formatter.format(new Date()));
        System.out.println();


        // start parsing the XML document
        // if we publish from a zip file, set up ZIP input stream and parse
        int intLoop = (Integer)_env.get("fileloop");
        for (int i=0; i < intLoop; i++) {
            if (_env.get("zip").equals(Boolean.TRUE)) {
                try {
                    ZipFile file = new ZipFile( _env.get("file").toString() );
                    System.out.println("File has " + file.size() + " entries");
                    Enumeration entries = file.entries();
                    Vector passedEntries = (Vector)_env.get("zipentries");
                    if (passedEntries.isEmpty()) {
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = (ZipEntry)entries.nextElement();
                            InputStream zipInput = file.getInputStream(entry);
                            System.out.println("reading from entry " + entry.getName());
                            _producer.parse(new InputSource(zipInput));
                        }
                    }
                    else {
                        for (Object passedEntry : passedEntries) {
                            String entryName = passedEntry.toString();
                            ZipEntry entry = new ZipEntry(entryName);
                            InputStream zipInput = file.getInputStream(entry);
                            if (zipInput != null) {
                                System.out.println("reading from entry " + entry.getName());
                                _producer.parse(new InputSource(zipInput));
                            }
                            else {
                                System.out.println("No entry found with such name: " + entry.getName());
                            }
                        }
                    }
                    file.close();
                }
                catch (IOException e) {
                    System.err.println(e.getMessage());
                    return;
                }
                catch (SAXException e) {
                    System.err.println("Parsing error: ");
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
            else {
                try {
                    _producer.parse(_env.get("file").toString());
                }
                catch (IOException e) {
                    System.err.println("I/O Error: ");
                    e.printStackTrace();
                }
                catch (SAXException e) {
                    System.err.println("Parsing error: ");
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        // If  request reply, waite for the threads to stop and close the listener files.
        if ((Boolean)_env.get("requestreply")) {
            Thread[] list = new Thread[_replyThreadGroup.activeCount()];
            _replyThreadGroup.enumerate(list);
            for (Thread aList : list) {
                if (aList.isAlive()) {
                    try {
                        aList.join();
                    }
                    catch (InterruptedException ie) {
                        // do something
                    }
                }
            }
            closeReplyFiles();
        }

        System.out.println();
        System.out.println(" finished:  " + _formatter.format(new Date()));
        System.out.println();
    }

    public void startElement(String uri, String local, String qName, Attributes atts) throws SAXException
    {
        _buffer = "";
        try {
            if (qName.equals("message")) {
                try {
                    _messageSelector = atts.getValue("messageSelector");
                    if (_chFactory.getFactoryType() == ConnectionHelper.TOPIC) {
                        if (atts.getValue("type").equalsIgnoreCase("TextMessage") || atts.getValue("type").equalsIgnoreCase("TibJmsTextMessage")) _msg = _topicSession.createTextMessage();
                        else if (atts.getValue("type").equalsIgnoreCase("MapMessage") || atts.getValue("type").equalsIgnoreCase("TibJmsMapMessage")) _msg = _topicSession.createMapMessage();
                        else if (atts.getValue("type").equalsIgnoreCase("BytesMessage") || atts.getValue("type").equalsIgnoreCase("TibJmsBytesMessage")) _msg = _topicSession.createBytesMessage();
                    }
                    else if (_chFactory.getFactoryType() == ConnectionHelper.QUEUE) {
                        if (atts.getValue("type").equalsIgnoreCase("TextMessage") || atts.getValue("type").equalsIgnoreCase("TibJmsTextMessage")) _msg = _queueSession.createTextMessage();
                        else if (atts.getValue("type").equalsIgnoreCase("MapMessage") || atts.getValue("type").equalsIgnoreCase("TibJmsMapMessage")) _msg = _queueSession.createMapMessage();
                        else if (atts.getValue("type").equalsIgnoreCase("BytesMessage") || atts.getValue("type").equalsIgnoreCase("TibJmsBytesMessage")) _msg = _queueSession.createBytesMessage();
                    }
                    else {
                        throw new SAXException("Unsupported JMS factory type.");
                    }
                }
                catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            else if (qName.equals( "header")) {
                for (int i = 0; i < atts.getLength(); i++) {
                    if (atts.getQName(i).equalsIgnoreCase("JMSDestination")) {
                        if (_chFactory.getFactoryType() == ConnectionHelper.TOPIC){
                            if (_pubHash.containsKey(atts.getValue("JMSDestination"))) {
                                _topicPublisher = (TopicPublisher)_pubHash.get(atts.getValue("JMSDestination"));
                            }
                            else {
                                _topic = _topicSession.createTopic(atts.getValue("JMSDestination"));
                                _topicPublisher = _topicSession.createPublisher(_topic);
                                _pubHash.put(atts.getValue("JMSDestination"), _topicPublisher);
                            }
                        }
                        else if (_chFactory.getFactoryType() == ConnectionHelper.QUEUE) {
                            if (_pubHash.containsKey(atts.getValue("JMSDestination"))) {
                                _queueSender = (QueueSender)_pubHash.get(atts.getValue("JMSDestination"));
                            }
                            else {
                                _queue = _queueSession.createQueue(atts.getValue("JMSDestination"));
                                _queueSender = _queueSession.createSender(_queue);
                                _pubHash.put(atts.getValue("JMSDestination"), _queueSender);
                            }
                        }
                        else {
                            throw new SAXException("Unsupported JMS factory type.");
                        }
                    }
                    else if (atts.getQName(i).equalsIgnoreCase("JMSType")) {
                        _msg.setJMSType(atts.getValue("JMSType"));
                    }
                    else if (atts.getQName(i).equalsIgnoreCase("JMSReplyTo")) {
                       // Request/Reply
                        String strReplyTo = atts.getValue("JMSReplyTo");
                        if (!strReplyTo.equals("")) {
                            if (_chFactory.getFactoryType() == ConnectionHelper.TOPIC) {
                                _msg.setJMSReplyTo(_topicSession.createTopic(strReplyTo));
                            }
                            else if (_chFactory.getFactoryType() == ConnectionHelper.QUEUE) {
                                _msg.setJMSReplyTo(_queueSession.createQueue(strReplyTo));
                            }
                            else {
                                throw new SAXException("Unsupported JMS factory type.");
                            }
                        }
                    }
                    else if (atts.getQName(i).equalsIgnoreCase("JMSCorrelationID")) {
                        _msg.setJMSCorrelationID(atts.getValue("JMSCorrelationID"));
                    }
                    else if (atts.getQName(i).equalsIgnoreCase("JMSDeliveryMode")) {
                        _deliveryMode = Integer.parseInt(atts.getValue("JMSDeliveryMode"));
                    }
                    else if (atts.getQName(i).equalsIgnoreCase("JMSPriority")) {
                        _priority = Integer.parseInt(atts.getValue("JMSPriority"));
                    }
                    else if (atts.getQName(i).equalsIgnoreCase("JMSExpiration")) {
                        _timeToLive = Long.parseLong(atts.getValue("JMSExpiration"));
                    }
                }
            }
            else if (qName.equals("property")) {
                        _propertyType = atts.getValue("type");
                        _propertyName = atts.getValue("name");
            }
            else if (qName.equals("node")) {
                        _nodeType = atts.getValue("type");
                        _nodeName = atts.getValue("name");
            }
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void endElement(String uri, String local, String qName) throws SAXException
    {
        try {
            if (qName.equals("wait") && _env.get("timed").equals(Boolean.TRUE)) {
                try {
                    long sleepTime;
                    sleepTime = (long)(Long.parseLong((_buffer)) / Double.parseDouble(_env.get("speed").toString()));
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (qName.equals("message")) {
                if (_env.containsKey("sndtimestamp")) {
                    if (_env.get("sndtimestamp").toString().equals("JMSCorrelationID"))
                        _msg.setJMSCorrelationID(_formatter.format(new Date()));
                    else if (_env.get("sndtimestamp").toString().equals("JMSType"))
                        _msg.setJMSType(_formatter.format(new Date()));
                    else
                        _msg.setStringProperty(_env.get("sndtimestamp").toString(), _formatter.format(new Date()));
                }

                if (_chFactory.getFactoryType() == ConnectionHelper.TOPIC) {
                    if (_isReqReply) {
                        if (_msg.getJMSReplyTo() == null) {
                            _msg.setJMSReplyTo(_topicSession.createTemporaryTopic());
                        }
                        // Start Thread
                        JmsStreamListener listener = new JmsStreamListener(_env);
                        listener.setJmsListener(_topicConnection, _msg.getJMSReplyTo(), _messageSelector);
                        listener.setOriginationTimestamp(new Date());
                        Thread tListener = new Thread(_replyThreadGroup, listener);
                        tListener.start();
                        _topicPublisher.publish(_msg, _deliveryMode, _priority, _timeToLive );
                        if (_env.get("asyncreply").equals(Boolean.FALSE)) {
                            try {
                                tListener.join();
                            }
                            catch (InterruptedException ie) {
                                throw new SAXException(ie);
                            }
                        }
                    }
                    else {
                        _topicPublisher.publish(_msg, _deliveryMode, _priority, _timeToLive );
                    }
                }
                else
                    if (_chFactory.getFactoryType() == ConnectionHelper.QUEUE) {
                    if (_isReqReply) {
                        if (_msg.getJMSReplyTo() == null) {
                            _msg.setJMSReplyTo(_queueSession.createTemporaryQueue());
                        }
                        // Start Thread
                        JmsStreamListener listener = new JmsStreamListener(_env);
                        listener.setJmsListener(_queueConnection, _msg.getJMSReplyTo(), _messageSelector);
                        listener.setOriginationTimestamp(new Date());
                        Thread tListener = new Thread(_replyThreadGroup, listener);
                        tListener.start();
                        _queueSender.send( _msg, _deliveryMode, _priority, _timeToLive );
                        if (_env.get("asyncreply").equals(Boolean.FALSE)) {
                            try {
                                tListener.join();
                            }
                            catch (InterruptedException ie) {
                                // do nothing
                            }
                        }
                    }
                    else {
                        _queueSender.send( _msg, _deliveryMode, _priority, _timeToLive );
                    }
                }
                else {
                    // throw exception
                }

                // echo message to stdout
                if (_env.get("verbose").equals(Boolean.TRUE))
                    System.out.println ("\n" + _formatter.format(new Date()) + "  --- " + _msg.getJMSDestination().toString()
                    + " --- " + _msg.toString());

                _messageCount++;

                // Throttle throughput to match "rate" number of messages per second.
                // Must publish in burst mode in order to keep up with high messages rates.
                if (_intRate != 0) {
                    if (_messageCount == _intRate) {
                        if ((System.currentTimeMillis() - _startTime) < _intMillis) {
                            try {
                                Thread.sleep(_intMillis - (System.currentTimeMillis() - _startTime));
                            }
                            catch (InterruptedException e) {
                                throw new SAXException("Interrupted exception.", e);
                            }
                            catch (NumberFormatException e) {
                                throw new SAXException("Number format exception.", e);
                            }
                        }
                        _messageCount = 0;
                        _startTime = System.currentTimeMillis();
                    }
                }

                //housekeeping: set default values for next use
                _deliveryMode = DeliveryMode.NON_PERSISTENT;
                _priority = 4;
                _timeToLive = 0;

            }
            else if (qName.equals("header")) {
                _msg.setJMSDeliveryMode(_deliveryMode);
                _msg.setJMSPriority(_priority);
                _msg.setJMSExpiration(_timeToLive);
            }
            else if (qName.equals("property")) {
                    if (_propertyType.equalsIgnoreCase("string")) {
                        _msg.setStringProperty(_propertyName, _buffer);
                    }
                    else if (_propertyType.equalsIgnoreCase("boolean")) {
                        _msg.setBooleanProperty(_propertyName,  Boolean.parseBoolean(_buffer));
                    }
                    else if (_propertyType.equalsIgnoreCase("byte")) {
                        _msg.setByteProperty(_propertyName, Byte.parseByte(_buffer));
                    }
                    else if (_propertyType.equalsIgnoreCase("short")) {
                        _msg.setShortProperty(_propertyName, Short.parseShort(_buffer));
                    }
                    else if (_propertyType.equalsIgnoreCase("integer")) {
                        _msg.setIntProperty(_propertyName, Integer.parseInt(_buffer));
                    }
                    else if (_propertyType.equalsIgnoreCase("long")) {
                        _msg.setLongProperty(_propertyName, Long.parseLong(_buffer));
                    }
                    else if (_propertyType.equalsIgnoreCase("float")) {
                        _msg.setFloatProperty(_propertyName, Float.parseFloat(_buffer));
                    }
                    else if (_propertyType.equalsIgnoreCase("double")) {
                        _msg.setDoubleProperty(_propertyName, Double.parseDouble(_buffer));
                    }
            }
            else if (qName.equals("node")) {
                if (TextMessage.class.isInstance(_msg)) {
                    // set text for TextMessage
                    ((TextMessage)_msg).setText(_buffer);
                }
                else if (BytesMessage.class.isInstance(_msg)) {
                    //set base64 for BytesMessage
                    ((BytesMessage)_msg).writeBytes(Base64.decode(_buffer));
                }
                else if (MapMessage.class.isInstance(_msg)) {
                    if (_nodeType.equalsIgnoreCase("string")) {
                        ((MapMessage)_msg).setString(_nodeName, _buffer );
                    }
                    else if (_nodeType.equalsIgnoreCase("boolean")) {
                        ((MapMessage)_msg).setBoolean(_nodeName,  Boolean.parseBoolean(_buffer));
                    }
                    else if (_nodeType.equalsIgnoreCase("byte")) {
                        ((MapMessage)_msg).setByte(_nodeName, Byte.parseByte(_buffer));
                    }
                    else if (_nodeType.equalsIgnoreCase("short")) {
                        ((MapMessage)_msg).setShort(_nodeName, Short.parseShort(_buffer));
                    }
                    else if (_nodeType.equalsIgnoreCase("char")) {
                        ((MapMessage)_msg).setChar(_nodeName, _buffer.charAt(0));
                    }
                    else if (_nodeType.equalsIgnoreCase("integer")) {
                        ((MapMessage)_msg).setInt(_nodeName, Integer.parseInt(_buffer));
                    }
                    else if (_nodeType.equalsIgnoreCase("long")) {
                        ((MapMessage)_msg).setLong(_nodeName, Long.parseLong(_buffer));
                    }
                    else if (_nodeType.equalsIgnoreCase("float")) {
                        ((MapMessage)_msg).setFloat(_nodeName, Float.parseFloat(_buffer));
                    }
                    else if (_nodeType.equalsIgnoreCase("double")) {
                        ((MapMessage)_msg).setDouble(_nodeName, Double.parseDouble(_buffer));
                    }
                    else if (_nodeType.equalsIgnoreCase("byte[]")) {
                        ((MapMessage)_msg).setBytes(_nodeName, _buffer.getBytes());
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SAXException(e);
        }

        _nodeName = "DATA";
        _buffer = "";
    }

    public void startDocument()
    {
        _startTime = System.currentTimeMillis();
        _messageCount = 0;
    }

    public void characters(char ch[], int start, int length)
    {
        for (int i = 0; i < length ; i++) {
            _buffer += ch[i + start];
        }
    }

    private void closeReplyFiles()
    {
        if (_env.containsKey("osfile")) {
            if (_env.containsKey("oszip")) {
                try {
                    if(_env.get("filetype").toString().equals("xml")) ((ZipOutputStream)_env.get("oszip")).write("</messages>".getBytes());
                    ((ZipOutputStream)_env.get("oszip")).closeEntry();
                    ((ZipOutputStream)_env.get("oszip")).close();
                    ((FileOutputStream)_env.get("osfile")).close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    if(_env.get("filetype").toString().equals("xml")) ((FileOutputStream)_env.get("osfile")).write("</messages>".getBytes());
                    ((FileOutputStream)_env.get("osfile")).close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (_env.containsKey("oscsvfile")) {
            try {
                ((FileOutputStream)_env.get("oscsvfile")).close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
