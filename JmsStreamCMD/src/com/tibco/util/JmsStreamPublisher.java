/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util;

import com.tibco.util.jmshelper.ConnectionHelper;
import com.tibco.util.jmshelper.MessageConstructor;
import com.tibco.util.math.StatFunctions;
import com.tibco.util.structs.MessageStruct;
import com.tibco.util.structs.PubRate;
import com.tibco.util.xa.TransactionManagerWrapper;

import javax.jms.*;
import javax.jms.Queue;
import javax.transaction.xa.XAResource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.4
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions", "ForLoopReplaceableByForEach", "FieldCanBeLocal", "CanBeFinal", "unchecked", "ProhibitedExceptionDeclared", "ProhibitedExceptionThrown", "NestedTryStatement", "WeakerAccess"})

public final class JmsStreamPublisher implements Runnable
{
    // Thread stop variables
    private boolean _blnLoop;
    private boolean _blnInterrupted;

    private JmsStreamClose _shutdownThread; // Thread will be started when SIGTERM is received
    public int _statsDelay; // For JmsStreamTimeTask
    public long _msgTimerCount;  // For JmsStreamTimerTask
    private Timer _timer; // For JmsStreamTimerTask

    private Hashtable _env;
    private ConnectionHelper _conHelper;
    private PubRate[] _pubRate;
    private int _intIntervalNum;
    private int _intAcknowledge;
    private boolean _blnReqReply;
    private MessageConstructor _msgConstructor;
    private boolean _blnTrans;
    private boolean _blnXaTrans;
    private boolean _blnCompress;

    private SimpleDateFormat _dateFormatter;

    private Connection _genericConnection;
    private Session _genericSession;
    private QueueConnection _queueConnection;
    private QueueSession _queueSession;
    private TopicConnection _topicConnection;
    private TopicSession _topicSession;
    private XASession _genericSessionXA;
    private XAQueueSession _queueSessionXA;
    private XATopicSession _topicSessionXA;
    private Connection _jmsConnection;
    private TransactionManagerWrapper _txwManager;
    private XAResource _xaResource;
    private boolean _blnCommitOnExit;
    private int _intNumInTrans;
    // For request/reply messages
    private boolean _blnOverrideListenDest;
    private Destination _destListen;
    // Reply threads
    private ThreadGroup _replyThreadGroup;

    private boolean _blnOverrideDelivery;
    private int _intDeliveryMode;
    private boolean _blnOverrideSendDest;
    private String _strSendDest;
    public int _intTotalSentMessages;

    // Zipped Files
    private boolean _blnZip;
    private boolean _blnMoreFiles;
    private ZipFile _fileZip;
    private Enumeration _entries;
    private Vector _vecZipEntries;

    // Text Encoding
    private final String _strEncoding;

    public JmsStreamPublisher(Hashtable env) throws Exception
    {
        _blnLoop = false;
        _blnInterrupted = false;
        _shutdownThread = null; // Thread will be started when SIGTERM is received
        _statsDelay = 0; // For JmsStreamTimeTask
        _msgTimerCount = 0;  // For JmsStreamTimerTask
        _timer = null; // For JmsStreamTimerTask
        _env = null;
        _conHelper = null;
        _pubRate = null;
        _intIntervalNum = 0;
        _blnReqReply = false;
        _msgConstructor = null;
        _blnTrans = false;
        _blnXaTrans = false;
        _blnCompress = false;
        _dateFormatter = null;
        _genericConnection = null;
        _genericSession = null;
        _queueConnection = null;
        _queueSession = null;
        _topicConnection = null;
        _topicSession = null;
        _genericSessionXA = null;
        _queueSessionXA = null;
        _topicSessionXA = null;
        _jmsConnection = null;
        _txwManager = null;
        _xaResource = null;
        _blnCommitOnExit = false;
        _intNumInTrans = 0;
        _blnOverrideDelivery = false;
        _intDeliveryMode = 0;
        _blnOverrideSendDest = false;
        _strSendDest = "";
        _intTotalSentMessages = 0;
        // For request/reply messages
        _blnOverrideListenDest = false;
        _destListen = null;
        // Reply threads
        _replyThreadGroup = null;
        // Zipped Files
        _blnZip = false;
        _blnMoreFiles = false;
        _fileZip = null;
        _entries = null;
        _vecZipEntries = null;

        _intAcknowledge = Session.AUTO_ACKNOWLEDGE;
        _env = env;
        _blnZip= _env.get("zip").equals(Boolean.TRUE);
        _blnTrans = _env.containsKey("trans");
        _blnXaTrans = _blnTrans && _env.get("trans").equals("xa");
        _blnCommitOnExit = _env.containsKey("commitonexit") && _env.get("commitonexit").equals(Boolean.TRUE);
        _statsDelay = (Integer)_env.get("stats") * 1000; // Convert seconds to milliseconds
        _blnReqReply = _env.containsKey("requestreply") && env.get("requestreply").equals(Boolean.TRUE);
        _blnCompress = _env.containsKey("compress") && env.get("compress").equals(Boolean.TRUE); 
        
        if (_blnReqReply) JmsStreamListener.clearCount(); // Reset static variables.

        // When the application is terminating we need to close the file.
        // This will be done by com.tibco.util.JmsStreamClose, which is registered here
        _shutdownThread = new JmsStreamClose(null, JmsStreamClose.TEXT_TYPE, null, null);
        Runtime.getRuntime().addShutdownHook(_shutdownThread);

        // Set encoding
        if (_env.containsKey("encoding")) {
            _strEncoding = _env.get("encoding").toString();
            com.tibco.tibjms.Tibjms.setEncoding(_strEncoding);
        }
        else
            _strEncoding = "UTF-8";

        // Set up publication for ZIP file
        if (_blnZip) {
            try {
                _fileZip = new ZipFile(_env.get("file").toString());
                _entries = _fileZip.entries();
                _vecZipEntries = (Vector)_env.get("zipentries");
                System.out.println("Loading ZIP message file '" + _fileZip.getName() + "' please wait...\n");
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }
        else {
            System.out.println("Loading message file '" + _env.get("file").toString() + "' ...\n");
        }

        // Set up number of msg in a transaction if using transaction.
        if (_env.containsKey("transmsgnum") && (Integer)_env.get("transmsgnum") > 0) {
            _intNumInTrans = (Integer)_env.get("transmsgnum");
        }

        Float fltRate = (Float)_env.get("rate");

        // From the Number Of Intervals and the Max Rate calculate the rate for each interval, and how many messages
        // to publish until moving to the next rate.  To increase the rate we need to adjust the _intRate and _intMillis.
        if (_env.containsKey("maxrate") && _env.containsKey("numberofintervals") && _env.containsKey("intervalsize")) {
            if (fltRate == null) fltRate = (float)0;
            float fltMaxRate = (Float)_env.get("maxrate");
            float fltMinRate = fltRate;
            int intInterval = (Integer)_env.get("numberofintervals");
            double[] dblRates = StatFunctions.linearIntervals(fltMaxRate, fltMinRate, intInterval);

            // Include minimum rate if not 0.
            if (fltMinRate > 0) {
                _pubRate = new PubRate[intInterval+1];
                _pubRate[0] = new PubRate(fltRate);
                // Calculate the rate for the interval
                for (int i=1; i < _pubRate.length; i++) _pubRate[i] = new PubRate(new Float(dblRates[i-1]));
            }
            else {
                _pubRate = new PubRate[intInterval];
                // Calculate the rate for the interval
                for (int i=0; i < _pubRate.length; i++) _pubRate[i] = new PubRate(new Float(dblRates[i]));
            }
            // Keep publishing until interval at max rate is finished
            _env.put("fileloop", Integer.MAX_VALUE);
        }
        else if (_env.containsKey("rate")) {
            _pubRate = new PubRate[1];
            _pubRate[0] = new PubRate(fltRate);
        }
        else {
            _pubRate = new PubRate[1];
            _pubRate[0] = new PubRate();
            _pubRate[0].intRate = 0;
            _pubRate[0].intMilli = 1000;
        }

        // Get a readable dateformat for on-screen information
        _dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

        if (_env.containsKey("ackmode")) {
            try {
                _intAcknowledge = ConnectionHelper.stringToAckType(_env.get("ackmode").toString(), _env.get("jmsclient").toString());
            }
            catch (Exception exc) {
                System.err.println("ERROR: Unrecognized acknowledgement mode " + _env.get("ackmode").toString());
                return;
            }
        }

        _conHelper = new ConnectionHelper(env);
        // Set the JMS type if given, otherwise ConnectionHelper derive it from the connection factory
        if (_env.containsKey("type")) {
            if (_env.get("type").toString().equalsIgnoreCase("topic")) {
                if (_blnTrans && _env.get("trans").equals("xa")) _conHelper.setFactoryType(ConnectionHelper.XA_TOPIC);
                else _conHelper.setFactoryType(ConnectionHelper.TOPIC);
            }
            else if (_env.get("type").toString().equalsIgnoreCase("queue")) {
                if (_blnTrans && _env.get("trans").equals("xa")) _conHelper.setFactoryType(ConnectionHelper.XA_QUEUE);
                else _conHelper.setFactoryType(ConnectionHelper.QUEUE);
            }
            else if (_env.get("type").toString().equalsIgnoreCase("generic")) {
                if (_blnTrans && _env.get("trans").equals("xa")) _conHelper.setFactoryType(ConnectionHelper.XA_GENERIC);
                else _conHelper.setFactoryType(ConnectionHelper.GENERIC);
            }
            else {
                throw new Exception("Unsupported -type " + _env.get("type").toString());
            }
        }

        // Create the connection factory
        _conHelper.createConnectionFactory();

        // If we expect an XA transactions, check to make sure we get an XA transaction factory
        if (_blnXaTrans) {
            if (!(_conHelper.getFactoryType() == ConnectionHelper.XA_GENERIC
             || _conHelper.getFactoryType() == ConnectionHelper.XA_QUEUE
             || _conHelper.getFactoryType() == ConnectionHelper.XA_TOPIC)) {
                throw new Exception("User indicated an XA transaction, but the \"" + _conHelper.getFactoryName() + "\" connection factory does not support XA transactions.");
            }
        }

        try {
            Session jmsSession = null;
            switch (_conHelper.getFactoryType()) {
                case ConnectionHelper.GENERIC:
                    ConnectionFactory genericFactory = _conHelper.getConnectionFactory();
                    if (_env.containsKey("user") && _env.containsKey("password"))
                        _genericConnection = genericFactory.createConnection(_env.get("user").toString(), _env.get("password").toString());
                    else if (_env.containsKey("user") && !_env.containsKey("password"))
                        _genericConnection = genericFactory.createConnection(_env.get("user").toString(), "");
                    else
                        _genericConnection = genericFactory.createConnection();
                    if (_env.containsKey("clientid")) _genericConnection.setClientID(_env.get("clientid").toString());
                    _genericSession = _genericConnection.createSession(_blnTrans, _intAcknowledge);
                    _msgConstructor = new MessageConstructor(_genericSession);
                    _jmsConnection = _genericConnection;
                    jmsSession = _genericSession;
                    break;
                case ConnectionHelper.QUEUE:
                    QueueConnectionFactory queueFactory = (QueueConnectionFactory) _conHelper.getConnectionFactory();
                    if (_env.containsKey("user") && _env.containsKey("password"))
                        _queueConnection = queueFactory.createQueueConnection(_env.get("user").toString(), _env.get("password").toString());
                    else if (_env.containsKey("user") && !_env.containsKey("password"))
                        _queueConnection = queueFactory.createQueueConnection(_env.get("user").toString(), "");
                    else
                        _queueConnection = queueFactory.createQueueConnection();
                    if (_env.containsKey("clientid")) _queueConnection.setClientID(_env.get("clientid").toString());
                    _queueSession = _queueConnection.createQueueSession(_blnTrans, _intAcknowledge);
                    _msgConstructor = new MessageConstructor(_queueSession);
                    _jmsConnection = _queueConnection;
                    jmsSession = _queueSession;
                    break;
                case ConnectionHelper.TOPIC:
                    TopicConnectionFactory topicFactory = (TopicConnectionFactory) _conHelper.getConnectionFactory();
                    if (_env.containsKey("user") && _env.containsKey("password"))
                        _topicConnection = topicFactory.createTopicConnection(_env.get("user").toString(), _env.get("password").toString());
                    else if (_env.containsKey("user") && !_env.containsKey("password"))
                        _topicConnection = topicFactory.createTopicConnection(_env.get("user").toString(), "");
                    else
                        _topicConnection = topicFactory.createTopicConnection();
                    if (_env.containsKey("clientid")) _topicConnection.setClientID(_env.get("clientid").toString());
                    _topicSession = _topicConnection.createTopicSession(_blnTrans, _intAcknowledge);
                    _msgConstructor = new MessageConstructor(_topicSession);
                    _jmsConnection = _topicConnection;
                    jmsSession = _topicSession;
                    break;
                case ConnectionHelper.XA_GENERIC:
                    XAConnectionFactory genericFactoryXA = _conHelper.getXAConnectionFactory();
                    XAConnection genericConnectionXA;
                    if (_env.containsKey("user") && _env.containsKey("password"))
                        genericConnectionXA = genericFactoryXA.createXAConnection(_env.get("user").toString(), _env.get("password").toString());
                    else if (_env.containsKey("user") && !_env.containsKey("password"))
                        genericConnectionXA = genericFactoryXA.createXAConnection(_env.get("user").toString(), "");
                    else
                        genericConnectionXA = genericFactoryXA.createXAConnection();
                    if (_env.containsKey("clientid")) genericConnectionXA.setClientID(_env.get("clientid").toString());
                    _genericSessionXA = genericConnectionXA.createXASession();

                    // Start the connection and begin the transaction
                    if (genericConnectionXA != null) genericConnectionXA.start();
                    // Enlist the XA resource in the current transaction.
                    _xaResource = _genericSessionXA.getXAResource();

                    _msgConstructor = new MessageConstructor(_genericSessionXA);
                    _jmsConnection = genericConnectionXA;
                    break;
                case ConnectionHelper.XA_QUEUE:
                    XAQueueConnectionFactory queueFactoryXA = (XAQueueConnectionFactory) _conHelper.getXAConnectionFactory();
                    XAQueueConnection queueConnectionXA;
                    if (_env.containsKey("user") && _env.containsKey("password"))
                        queueConnectionXA = queueFactoryXA.createXAQueueConnection(_env.get("user").toString(), _env.get("password").toString());
                    else if (_env.containsKey("user") && !_env.containsKey("password"))
                        queueConnectionXA = queueFactoryXA.createXAQueueConnection(_env.get("user").toString(), "");
                    else
                        queueConnectionXA = queueFactoryXA.createXAQueueConnection();
                    if (_env.containsKey("clientid")) queueConnectionXA.setClientID(_env.get("clientid").toString());
                    _queueSessionXA = queueConnectionXA.createXAQueueSession();

                    // Start the connection
                    if (queueConnectionXA != null) queueConnectionXA.start();
                    // Enlist the XA resource in the current transaction.
                    _xaResource = _queueSessionXA.getXAResource();

                    _msgConstructor = new MessageConstructor(_queueSessionXA);
                    _jmsConnection = queueConnectionXA;
                    break;
                case ConnectionHelper.XA_TOPIC:
                    XATopicConnectionFactory topicFactoryXA = (XATopicConnectionFactory) _conHelper.getXAConnectionFactory();
                    XATopicConnection topicConnectionXA;
                    if (_env.containsKey("user") && _env.containsKey("password"))
                        topicConnectionXA = topicFactoryXA.createXATopicConnection(_env.get("user").toString(), _env.get("password").toString());
                    else if (_env.containsKey("user") && !_env.containsKey("password"))
                        topicConnectionXA = topicFactoryXA.createXATopicConnection(_env.get("user").toString(), "");
                    else
                        topicConnectionXA = topicFactoryXA.createXATopicConnection();
                    if (_env.containsKey("clientid")) topicConnectionXA.setClientID(_env.get("clientid").toString());
                    _topicSessionXA = topicConnectionXA.createXATopicSession();

                    // Start the connection
                    if (topicConnectionXA != null) topicConnectionXA.start();
                    // Enlist the XA resource in the current transaction.
                    _xaResource = _topicSessionXA.getXAResource();

                    _msgConstructor = new MessageConstructor(_topicSessionXA);
                    _jmsConnection = topicConnectionXA;
                    break;
                default:
                    throw new Exception("Unsupported connection factory:  " + _conHelper.getFactoryDescription());
            }

            // Register the JMS Connection with the stop thread
            _shutdownThread.setJmsConnection(_jmsConnection);

            // Register the JMS Session with the stop thread if there are JMS Session transactions
            if (_blnTrans && !_blnXaTrans) _shutdownThread.setJmsSession(jmsSession, _blnCommitOnExit);

            _blnOverrideDelivery = _env.containsKey("deliverymode");
            if (_blnOverrideDelivery) {
                if (_env.get("deliverymode").toString().equalsIgnoreCase("NON_PERSISTENT")) _intDeliveryMode = DeliveryMode.NON_PERSISTENT;
                else if (_env.get("deliverymode").toString().equalsIgnoreCase("PERSISTENT")) _intDeliveryMode = DeliveryMode.PERSISTENT;
                else if (_env.get("deliverymode").toString().equalsIgnoreCase("RELIABLE_DELIVERY")) _intDeliveryMode = com.tibco.tibjms.Tibjms.RELIABLE_DELIVERY;
                else throw new Exception("ERROR: Unsupported DeliveryMode.");
            }

            _blnOverrideSendDest = _env.containsKey("senddest");
            if (_blnOverrideSendDest) {
                _strSendDest = _env.get("senddest").toString();
            }

            if (_blnReqReply) {
                _replyThreadGroup = new ThreadGroup("ReplyThreads");

                _blnOverrideListenDest = _env.containsKey("listendest");
                if (_blnOverrideListenDest) {
                    switch (_conHelper.getFactoryType()) {
                        case ConnectionHelper.GENERIC:
                            _destListen = _genericSession.createQueue(_env.get("listendest").toString());
                            break;
                        case ConnectionHelper.QUEUE:
                            _destListen = _queueSession.createQueue(_env.get("listendest").toString());
                            break;
                        case ConnectionHelper.TOPIC:
                            _destListen = _topicSession.createTopic(_env.get("listendest").toString());
                            break;
                        default:
                            throw new Exception("Unsupported connection factory:  " + _conHelper.getFactoryDescription());
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPublish()
    {
        _blnLoop = false; // gracefully stop sending messages.
    }

    public void stopReplyThread()
    {
        if (_blnReqReply) {
            _replyThreadGroup.interrupt();
            if (_replyThreadGroup.activeCount() == 0)
                _replyThreadGroup.destroy();
        }
    }

    public void closeConnections()
    {
        _blnLoop = false; // Stop sending messages gracefully.
        _shutdownThread.run();
    }

    public void run()
    {
        _blnLoop = true;

        ArrayList aryMsgStruct = readMessages(); // To hold the MessageStruct[] if there are more than one array group

        // If stats is turned on - start the metrics output
        if (_statsDelay > 0 && _blnLoop) {
            JmsStreamTimerTask task = new JmsStreamTimerTask(this);
            _timer = new Timer();
            _timer.scheduleAtFixedRate(task, _statsDelay, _statsDelay);
        }

        // Want to read file before displaying messages.
        System.out.println(" " + JmsStream.APP_NAME + " started");
        System.out.println(" -------------------");
        System.out.println(" jms client      :  " + _env.get("jmsclient").toString());
        System.out.println(" provider url    :  " + _env.get(javax.naming.Context.PROVIDER_URL).toString());
        System.out.println(" factory name    :  " + _env.get("connectionfactory").toString());
        System.out.println(" connection info :  " + _jmsConnection.toString());
        if (_blnXaTrans) System.out.println(" transaction type:  XA");
        else if (_blnTrans) System.out.println(" transaction type:  JMS");
        else System.out.println(" transaction type:  none");
        System.out.println(" message file    :  " + _env.get("file").toString());
        try {System.out.println(" ack mode        :  " + ConnectionHelper.ackTypeToString(_intAcknowledge));} catch (Exception exc) {exc.printStackTrace();}
        if (_blnOverrideSendDest) System.out.println(" destination     :  " + _strSendDest);
        if (_blnOverrideDelivery) System.out.println(" delivery mode   :  " + ConnectionHelper.deliveryModeToString(_intDeliveryMode));
        if (_blnReqReply) {
            System.out.println(" reply file      :  " + (_env.get("replyfile") == null ? "" : _env.get("replyfile").toString()));
            System.out.println(" request/reply started:  " + _dateFormatter.format(new Date()));
        }
        else
            System.out.println(" publishing started:  " + _dateFormatter.format(new Date()));
        if (_blnZip) System.out.println(" NOTE:  Loading ZIP entries can cause pauses in publication. Please be patient.");
        System.out.println();

        // Start publishing after displaying publishing messages.
        for (int i=0; i < aryMsgStruct.size() && _blnLoop; i++) publish((MessageStruct[])aryMsgStruct.get(i));

        // If there are more files in the ZIP file, continue with the next file.
        while (_blnMoreFiles) {
            aryMsgStruct.clear(); // Flush the memory.
            //Runtime.getRuntime().gc(); // Invoke garbage collector.
            aryMsgStruct = readMessages(); // To hold the MessageStruct[] if there are more than one array group
            for (int i=0; i < aryMsgStruct.size() && _blnLoop; i++) publish((MessageStruct[])aryMsgStruct.get(i));
        }

        if (_txwManager != null) {
            // Must suspend() the TransactionManager in this thread so the Transaction can be resume()
            // and completed with the TransactionManager in the shutdown thread.  Each thread has its
            // own TransactionManager in JTA.
            try {
                _txwManager.suspend();
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        if (_blnZip) {
            // Close ZIP file
            try {
                _fileZip.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }

        // If request reply, wait for the threads to stop and close the listener files.
        if (_blnReqReply) {
            Thread[] list = new Thread[_replyThreadGroup.activeCount()];
            _replyThreadGroup.enumerate(list);
            for (int i = 0; i < list.length; i++) {
                if (list[i].isAlive()) {
                    try {
                        list[i].join();
                    }
                    catch (InterruptedException ie) {
                        _blnInterrupted = true;
                        System.err.println(" Publisher interrupted...");
                    }
                }
            }
            closeReplyFiles();
        }

        if (_timer != null) _timer.cancel();

        System.out.println();
        System.out.println(" finished:  " + _dateFormatter.format(new Date()));
        if (_blnReqReply)
            System.out.println(" Produced " + _intTotalSentMessages + " request/reply messages.");
        else
            System.out.println(" Produced " + _intTotalSentMessages + " messages.");
        System.out.println();
    }

    private ArrayList readMessages()
    {
        // Currently, only one message file is supported, but the aryMsgStruct ArrayList is used for future support.
        ArrayList aryMsgStruct = new ArrayList();  // To hold the MessageStruct[] if there are more than one array group
        InputStreamReader zipInputReader = null;  // Need to create InputStreamReader from the InputStream in the ZipEntry.

        // If we publish from a zip file, set up ZIP input stream and parse
        if (_blnZip) {
            try {
                // ZIP Entries are from the user input for -zip [entry1 ...]
                if (_vecZipEntries == null || _vecZipEntries.isEmpty()) {
                    if (_entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry)_entries.nextElement();
                        InputStream zipInput = _fileZip.getInputStream(entry);
                        //System.out.println("reading from entry " + entry.getName());
                        if (entry.getSize() >= Integer.MAX_VALUE) {
                            throw new IOException("ERROR: File too large.  File size must be less than " + Integer.MAX_VALUE + " bytes.");
                        }
                        aryMsgStruct.add(_msgConstructor.getMessages(zipInput, _strEncoding));
                        _blnMoreFiles = _entries.hasMoreElements();
                    }
                }
                else {
                    Iterator iter = _vecZipEntries.iterator();
                    if (iter.hasNext()) {
                        String entryName = iter.next().toString();
                        ZipEntry entry = _fileZip.getEntry(entryName);
                        InputStream zipInput = _fileZip.getInputStream(entry);
                        if (zipInput != null) {
                            if (entry.getSize() >= Integer.MAX_VALUE) {
                                throw new IOException("ERROR: File too large.  File size must be less than " + Integer.MAX_VALUE + " bytes.");
                            }

                            zipInputReader = new InputStreamReader(zipInput);
                            aryMsgStruct.add(_msgConstructor.getMessages(zipInput, _strEncoding));
                        }
                        else {
                            System.err.println("No entry found with such name: " + entry.getName());
                        }
                        _blnMoreFiles = iter.hasNext();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            finally {
                try {
                    if (zipInputReader != null) zipInputReader.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            RandomAccessFile rafMsgFile = null;

            try {
                rafMsgFile = new RandomAccessFile(_env.get("file").toString(), "rwd");
                if (rafMsgFile.length() >= Integer.MAX_VALUE) {
                    throw new IOException("ERROR: File too large.  File size must be less than " + Integer.MAX_VALUE + " bytes.");
                }

                aryMsgStruct.add(_msgConstructor.getMessages(rafMsgFile, _strEncoding));
            }
            catch (FileNotFoundException fnf) {
                fnf.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            finally {
                try {
                    if (rafMsgFile != null) rafMsgFile.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return aryMsgStruct;
    }

    private void publish(MessageStruct[] msgArray)
    {
        // Declare all variables and hashmap constants outside the publish loop to improve speed.
        MessageProducer genericProducer = null;
        QueueSender queueSender = null;
        TopicPublisher topicPublisher = null;

        String strMessageSelector;
        int intDeliveryMode;
        long startTime = System.currentTimeMillis();
        int intMessageCount = 0;
        int intIntervalCount = 0;
        String strSndTimeProp = _env.containsKey("sndtimestamp") ? _env.get("sndtimestamp").toString() : null;
        String strMsgSequenceProp = _env.containsKey("sequence") ? _env.get("sequence").toString() : null;
        String strRateProp = _env.containsKey("ratestamp") ? _env.get("ratestamp").toString() : null;
        boolean blnTimed = _env.get("timed").equals(Boolean.TRUE);
        float fltTimedSpeed = (Float)_env.get("speed");
        boolean blnAsyncReply = _env.get("asyncreply").equals(Boolean.TRUE);
        boolean blnVerbose = _env.get("verbose").equals(Boolean.TRUE);
        boolean blnNoEcho = _env.containsKey("noecho") && _env.get("noecho").equals(Boolean.TRUE);

        int intIntervalSize = _env.containsKey("intervalsize") ? (Integer)_env.get("intervalsize") : 0;
        boolean blnContinue = true;
        boolean blnNoCorrelationId[] = new boolean[msgArray.length];
        boolean blnNoReplay[] = new boolean[msgArray.length];

        // Override message properties
        try {
            // Override message destination
            if (_blnOverrideSendDest) {
                switch (_conHelper.getFactoryType()) {
                    case ConnectionHelper.TOPIC:
                        topicPublisher = _topicSession.createPublisher(_topicSession.createTopic(_strSendDest));
                        break;
                    case ConnectionHelper.QUEUE:
                        queueSender = _queueSession.createSender(_queueSession.createQueue(_strSendDest));
                        break;
                    case ConnectionHelper.GENERIC:
                        genericProducer = _genericSession.createProducer(_genericSession.createQueue(_strSendDest));
                        break;
                    case ConnectionHelper.XA_TOPIC:
                        topicPublisher = _topicSessionXA.getTopicSession().createPublisher(_topicSessionXA.createTopic(_strSendDest));
                        break;
                    case ConnectionHelper.XA_QUEUE:
                        queueSender = _queueSessionXA.getQueueSession().createSender(_queueSessionXA.createQueue(_strSendDest));
                        break;
                    case ConnectionHelper.XA_GENERIC:
                        genericProducer = _genericSessionXA.createProducer(_genericSessionXA.createQueue(_strSendDest));
                        break;
                    default:
                        throw new JMSException("ERROR: Unsupported connection factory.");
                }
            }

            // If the connection is XA, connect to the transaction manager and start the transaction.
            if (_blnXaTrans) {
                _txwManager = new TransactionManagerWrapper(_env);
                _shutdownThread.setTransactionManagerWrapper(_txwManager);
                newXaTrans();
            }

            int intLoop = (Integer)_env.get("fileloop");
            int intCount = 0;
            // Loop over file if file loop is set
            for (int i=0; _blnLoop && i < intLoop && blnContinue; i++) {
                // Loop over each message in file
                for (int j=0; j < msgArray.length && blnContinue && _blnLoop; j++) {
                    // When publishing with Rates we need to exit loop after the max rate is finished
                    if (_intIntervalNum == _pubRate.length) {
                        blnContinue = false;
                        break;
                    }

                    // For request/reply we need to test if the ReplyTo or CorrelationID is blank before we start the loop.
                    // Otherwise it will get set in the first loop and never change.
                    if (i == 0) {
                        blnNoCorrelationId[j] = msgArray[j].jmsMessage.getJMSCorrelationID() == null;
                        blnNoReplay[j] = msgArray[j].jmsMessage.getJMSReplyTo() == null;
                    }

                    // If the user wants to compress the messages
                    if (_blnCompress) {
                        switch (_conHelper.getJmsClientTypeId()) {
                            case ConnectionHelper.JMS_SERVER_TIBCO_EMS_ID:
                                msgArray[j].jmsMessage.setBooleanProperty("JMS_TIBCO_COMPRESS", true);
                                break;
                            case ConnectionHelper.JMS_SERVER_APACHE_AMQ_ID:
                                ((org.apache.activemq.command.ActiveMQMessage)(msgArray[j].jmsMessage)).setCompressed(true);
                                break;
                            default:
                        }
                    }

                    // If the user wants to add a timestamp to the message
                    if (strSndTimeProp != null) {
                        if (strSndTimeProp.equals("JMSCorrelationID"))
                            msgArray[j].jmsMessage.setJMSCorrelationID(_dateFormatter.format(new Date()));
                        else if (strSndTimeProp.equals("JMSType"))
                            msgArray[j].jmsMessage.setJMSType(_dateFormatter.format(new Date()));
                        else
                            msgArray[j].jmsMessage.setStringProperty(strSndTimeProp, _dateFormatter.format(new Date()));
                    }
                    // If the user wants to add a sequence to the message
                    if (strMsgSequenceProp != null) {
                        if (strMsgSequenceProp.equals("JMSCorrelationID"))
                            msgArray[j].jmsMessage.setJMSCorrelationID(Integer.toString(_intTotalSentMessages + 1));
                        else if (strSndTimeProp.equals("JMSType"))
                            msgArray[j].jmsMessage.setJMSType(Integer.toString(_intTotalSentMessages + 1));
                        else
                            msgArray[j].jmsMessage.setIntProperty(strMsgSequenceProp, _intTotalSentMessages + 1);
                    }
                    // If the user wants to add a rate stamp to the message
                    if (strRateProp != null) {
                        if (strRateProp.equals("JMSCorrelationID"))
                            msgArray[j].jmsMessage.setJMSCorrelationID(String.valueOf(_pubRate[_intIntervalNum].intRate));
                        else if (strRateProp.equals("JMSType"))
                            msgArray[j].jmsMessage.setJMSType(String.valueOf(_pubRate[_intIntervalNum].intRate));
                        else
                            msgArray[j].jmsMessage.setIntProperty(strRateProp, _pubRate[_intIntervalNum].intRate);
                    }
                    // If the user wants to publish a -timed messages
                    if (blnTimed) {
                        try {
                            long sleepTime;
                            sleepTime = (long)(msgArray[j].sleepTime / fltTimedSpeed);
                            Thread.sleep(sleepTime);
                        }
                        catch (InterruptedException e) {
                            _blnInterrupted = true;
                            _blnLoop = false;
                            System.err.println(" Publisher interrupted...");
                        }
                    }

                    // Choose delivery mode
                    if (_blnOverrideDelivery) intDeliveryMode = _intDeliveryMode;
                    else intDeliveryMode = msgArray[j].jmsMessage.getJMSDeliveryMode();

                    if (_blnTrans && _blnReqReply) throw new Exception("ERROR: Request/Reply and Transactional messages are not compatible." +
                                                                       "  You cannot not use transactions with request/reply.");

                    // Publish the message
                    switch (_conHelper.getFactoryType()) {
                        case ConnectionHelper.TOPIC:
                        case ConnectionHelper.XA_TOPIC:
                            // Don't override message destination
                            if (!_blnOverrideSendDest) topicPublisher = _topicSession.createPublisher((Topic)msgArray[j].jmsMessage.getJMSDestination());

                            if (_blnReqReply) {
                                // For non-durable Topics, if the reply messages is sent before the listener has time to connect, the message will be missed and the
                                // listener will wait until the timeout.
                                Thread tListener;

                                if (blnNoReplay[j] && !_blnOverrideListenDest) {
                                    msgArray[j].jmsMessage.setJMSReplyTo(_topicSession.createTemporaryTopic());
                                    // Start Thread
                                    JmsStreamListener listener = new JmsStreamListener(_env);
                                    listener.setJmsListener(_topicConnection, msgArray[j].jmsMessage.getJMSReplyTo(), "");
                                    listener.setOriginationTimestamp(new Date());
                                    tListener = new Thread(_replyThreadGroup, listener);
                                    tListener.start();
                                    topicPublisher.publish(msgArray[j].jmsMessage, intDeliveryMode,
                                                            msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                }
                                else {
                                    JmsStreamListener listener = new JmsStreamListener(_env);
                                    // Start Thread
                                    // If there is no JMSCorrelationID in the message use the JMSMessageID, but we have to send the message before we listen for the reply
                                    if (blnNoCorrelationId[j] || _blnOverrideListenDest) {
                                        msgArray[j].jmsMessage.setJMSReplyTo(_destListen);
                                        topicPublisher.publish(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                        strMessageSelector = "JMSCorrelationID='" + msgArray[j].jmsMessage.getJMSMessageID() + "'";
                                        if (_blnOverrideListenDest) listener.setJmsListener(_topicConnection, _destListen, strMessageSelector);
                                        else listener.setJmsListener(_topicConnection, msgArray[j].jmsMessage.getJMSReplyTo(), strMessageSelector);
                                        listener.setOriginationTimestamp(new Date());
                                        tListener = new Thread(_replyThreadGroup, listener);
                                        tListener.start();
                                    }
                                    else {
                                        strMessageSelector = "JMSCorrelationID='" + msgArray[j].jmsMessage.getJMSCorrelationID() + "'";
                                        listener.setJmsListener(_topicConnection, msgArray[j].jmsMessage.getJMSReplyTo(), strMessageSelector);
                                        listener.setOriginationTimestamp(new Date());
                                        tListener = new Thread(_replyThreadGroup, listener);
                                        tListener.start();
                                        topicPublisher.publish(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                    }
                                }
                                if (!blnAsyncReply) {
                                    try {
                                        tListener.join();
                                    }
                                    catch (InterruptedException ie) {
                                        _blnInterrupted = true;
                                        _blnLoop = false;
                                        System.err.println(" Publisher interrupted...");
                                    }
                                }
                            }
                            else {
                                topicPublisher.publish(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                intCount++;
                                if (_blnTrans && ((_intNumInTrans != 0 && intCount % _intNumInTrans == 0) || msgArray[j].commitTrans)) { // If the transaction number is set then commit the trans every _intNumInTrans
                                    if (_blnXaTrans) {
                                        _txwManager.commit(); // Commit the XA transaction
                                        newXaTrans(); // Create new XA transaction
                                    }
                                    else _topicSession.commit();
                                }
                            }
                            break;
                        case ConnectionHelper.QUEUE:
                        case ConnectionHelper.XA_QUEUE:
                            // Don't override message destination
                            if (!_blnOverrideSendDest) queueSender = _queueSession.createSender((Queue)msgArray[j].jmsMessage.getJMSDestination());

                            if (_blnReqReply) {
                                Thread tListener;
                                if (blnNoReplay[j] && !_blnOverrideListenDest) {
                                    msgArray[j].jmsMessage.setJMSReplyTo(_queueSession.createTemporaryQueue());
                                    // Start Thread
                                    JmsStreamListener listener = new JmsStreamListener(_env);
                                    listener.setJmsListener(_queueConnection, msgArray[j].jmsMessage.getJMSReplyTo(), "");
                                    listener.setOriginationTimestamp(new Date());
                                    tListener = new Thread(_replyThreadGroup, listener);
                                    tListener.start();
                                    queueSender.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                }
                                else {
                                    // Start Thread
                                    JmsStreamListener listener = new JmsStreamListener(_env);
                                    // If there is no JMSCorrelationID in the message use the JMSMessageID, but we have to send the message before we
                                    // can get the JMSMessageID and listen for the reply
                                    if (blnNoCorrelationId[j] || _blnOverrideListenDest) {
                                        msgArray[j].jmsMessage.setJMSReplyTo(_destListen);
                                        queueSender.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                        strMessageSelector = "JMSCorrelationID='" + msgArray[j].jmsMessage.getJMSMessageID() + "'";
                                        if (_blnOverrideListenDest) listener.setJmsListener(_queueConnection, _destListen, strMessageSelector);
                                        else listener.setJmsListener(_queueConnection, msgArray[j].jmsMessage.getJMSReplyTo(), strMessageSelector);
                                        listener.setOriginationTimestamp(new Date());
                                        tListener = new Thread(_replyThreadGroup, listener);
                                        tListener.start();
                                    }
                                    else {
                                        strMessageSelector = "JMSCorrelationID='" + msgArray[j].jmsMessage.getJMSCorrelationID() + "'";
                                        listener.setJmsListener(_queueConnection, msgArray[j].jmsMessage.getJMSReplyTo(), strMessageSelector);
                                        listener.setOriginationTimestamp(new Date());
                                        tListener = new Thread(_replyThreadGroup, listener);
                                        tListener.start();
                                        queueSender.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                    }
                                }

                                if (!blnAsyncReply) {
                                    try {
                                        tListener.join();
                                    }
                                    catch (InterruptedException ie) {
                                        _blnInterrupted = true;
                                        _blnLoop = false;
                                        System.err.println(" Publisher interrupted...");
                                    }
                                }
                            }
                            else {
                                queueSender.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                intCount++;
                                if (_blnTrans && ((_intNumInTrans != 0 && (intCount % _intNumInTrans) == 0) || msgArray[j].commitTrans)) { // If the transaction number is set then commit the trans every _intNumInTrans
                                    if (_blnXaTrans) {
                                        _txwManager.commit(); // Commit the XA transaction
                                        newXaTrans(); // Create new XA transaction
                                    }
                                    else _queueSession.commit();
                                }
                            }
                            break;
                        case ConnectionHelper.GENERIC:
                        case ConnectionHelper.XA_GENERIC:
                            // Don't override message destination
                            if (!_blnOverrideSendDest) genericProducer = _genericSession.createProducer(msgArray[j].jmsMessage.getJMSDestination());

                            if (_blnReqReply) {
                                Thread tListener;

                                if (blnNoReplay[j] && !_blnOverrideListenDest) {
                                    if (msgArray[j].jmsMessage.getJMSDestination().getClass().getName().equals("javax.jms.Topic"))
                                        msgArray[j].jmsMessage.setJMSReplyTo(_genericSession.createTemporaryTopic());
                                    else if (msgArray[j].jmsMessage.getJMSDestination().getClass().getName().equals("javax.jms.Queue"))
                                        msgArray[j].jmsMessage.setJMSReplyTo(_genericSession.createTemporaryQueue());
                                    else
                                        throw new JMSException("ERROR: Unsupported destination type.");
                                    // Start Thread
                                    JmsStreamListener listener = new JmsStreamListener(_env);
                                    listener.setJmsListener(_genericConnection, msgArray[j].jmsMessage.getJMSReplyTo(), "");
                                    listener.setOriginationTimestamp(new Date());
                                    tListener = new Thread(_replyThreadGroup, listener);
                                    tListener.start();
                                    genericProducer.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                }
                                else {
                                    // Start Thread
                                    JmsStreamListener listener = new JmsStreamListener(_env);
                                    // If there is no JMSCorrelationID in the message use the JMSMessageID, but we have to send the message before we listen for the reply
                                    if (blnNoCorrelationId[j] || _blnOverrideListenDest) {
                                        msgArray[j].jmsMessage.setJMSReplyTo(_destListen);
                                        genericProducer.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                        strMessageSelector = "JMSCorrelationID='" + msgArray[j].jmsMessage.getJMSMessageID() + "'";
                                        if (_blnOverrideListenDest) listener.setJmsListener(_genericConnection, _destListen, strMessageSelector);
                                        else listener.setJmsListener(_genericConnection, msgArray[j].jmsMessage.getJMSReplyTo(), strMessageSelector);
                                        listener.setOriginationTimestamp(new Date());
                                        tListener = new Thread(_replyThreadGroup, listener);
                                        tListener.start();
                                    }
                                    else {
                                        strMessageSelector = "JMSCorrelationID='" + msgArray[j].jmsMessage.getJMSCorrelationID() + "'";
                                        listener.setJmsListener(_genericConnection, msgArray[j].jmsMessage.getJMSReplyTo(), strMessageSelector);
                                        listener.setOriginationTimestamp(new Date());
                                        tListener = new Thread(_replyThreadGroup, listener);
                                        tListener.start();
                                        genericProducer.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                    }
                                }
                                if (!blnAsyncReply) {
                                    try {
                                        tListener.join();
                                    }
                                    catch (InterruptedException ie) {
                                        _blnInterrupted = true;
                                        _blnLoop = false;
                                        System.err.println(" Publisher inrrupted...");
                                    }
                                }
                            }
                            else {
                                genericProducer.send(msgArray[j].jmsMessage, intDeliveryMode, msgArray[j].jmsMessage.getJMSPriority(), msgArray[j].jmsMessage.getJMSExpiration());
                                intCount++;
                                if (_blnTrans && ((_intNumInTrans != 0 && intCount % _intNumInTrans == 0) || msgArray[j].commitTrans)) { // If the transaction number is set then commit the trans every _intNumInTrans
                                    if (_blnXaTrans) {
                                        _txwManager.commit(); // Commit the XA transaction
                                        newXaTrans(); // Create new XA transaction
                                    }
                                    else _genericSession.commit();
                                }
                            }
                            break;
                    default:
                        throw new JMSException("ERROR: Unsupported connection factory.");
                    }

                    // echo message to stdout
                    if (blnVerbose) {
                        if (Topic.class.isInstance(msgArray[j].jmsMessage.getJMSDestination())) {
                            System.out.println (" " + _dateFormatter.format(new Date()) + "  --- " + ((Topic)msgArray[j].jmsMessage.getJMSDestination()).getTopicName()
                                                + " --- " + msgArray[j].jmsMessage.toString());
                        }
                        else if (Queue.class.isInstance(msgArray[j].jmsMessage.getJMSDestination())) {
                            System.out.println (" " + _dateFormatter.format(new Date()) + "  --- " + ((Queue)msgArray[j].jmsMessage.getJMSDestination()).getQueueName()
                                         + " --- " + msgArray[j].jmsMessage.toString());
                        }
                        else {
                            System.out.println (" " + _dateFormatter.format(new Date()) + "  --- " + msgArray[j].jmsMessage.getJMSDestination().toString()
                                                + " --- " + msgArray[j].jmsMessage.toString());
                        }
                    }
                    else if (!blnNoEcho) {
                        msgArray[j].jmsMessage.toString();
                    }

                    if (!_blnInterrupted) {
                        intMessageCount++;
                        intIntervalCount++;
                        _intTotalSentMessages++;
                        _msgTimerCount++;
                    }
                    if (_intIntervalNum < _pubRate.length && intIntervalCount == intIntervalSize) {
                        _intIntervalNum++;
                        intIntervalCount = 0;
                    }

                    // Throttle throughput to match "rate" number of messages per second.
                    // Must publish in burst mode in order to keep up with high messages rates.
                    if (_intIntervalNum < _pubRate.length && _pubRate[_intIntervalNum].intRate != 0) {
                        if (intMessageCount == _pubRate[_intIntervalNum].intRate) {
                            if ((System.currentTimeMillis() - startTime) < _pubRate[_intIntervalNum].intMilli) {
                                try {
                                    Thread.sleep(_pubRate[_intIntervalNum].intMilli - (System.currentTimeMillis() - startTime));
                                }
                                catch (InterruptedException e) {
                                    _blnInterrupted = true;
                                    _blnLoop = false;
                                    System.err.println(" Publisher interrupted...");
                                }
                                catch (NumberFormatException e) {
                                    throw new JMSException("Number format exception.");
                                }
                            }
                            intMessageCount = 0;
                            startTime = System.currentTimeMillis();
                        }
                    } // end if
                } // end for j
            } // end for i
        }
        catch (Exception e) {
            if (JMSException.class.isInstance(e) && ((JMSException)e).getLinkedException() instanceof InterruptedException)
                System.out.println(" Publisher interrupted...");
            else
                e.printStackTrace();
        }
    }

    private void newXaTrans() throws Exception
    {
        // Setup an new transaction when the current transaction is committed.
        if (_txwManager.getStatus() == javax.transaction.Status.STATUS_NO_TRANSACTION) {
            _txwManager.begin();
            //noinspection LawOfDemeter
            _txwManager.getTransaction().enlistResource(_xaResource);
        }
        // Register the Transaction with the shutdown thread.
        _shutdownThread.setXaTransaction(_txwManager.getTransaction(), _blnCommitOnExit);
    }

    /**
     *
     */
    private void closeReplyFiles()
    {
        if (_env.containsKey("osfile")) {
            if (_env.containsKey("oszip")) {
                try {
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
