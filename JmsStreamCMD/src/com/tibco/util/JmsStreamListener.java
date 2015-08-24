/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util;

import com.tibco.util.jmshelper.Base64;
import com.tibco.util.jmshelper.ConnectionHelper;
import com.tibco.util.jmshelper.FormatHelper;
import com.tibco.util.xa.TransactionManagerWrapper;

import javax.jms.*;
import javax.jms.Queue;
import javax.transaction.xa.XAResource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.8
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "ProhibitedExceptionThrown", "ProhibitedExceptionDeclared", "NestedTryStatement", "ConstantConditions", "WeakerAccess", "unused"})
public class JmsStreamListener implements Runnable
{
    // Thread stop variables
    private boolean _blnLoop = false;

    // Internal variables.  static var are required so that multiple listening threads can keep the same data.
    private final JmsStreamClose _shutdownThread; // Thread will be started when SIGTERM is received
    public static int _statsDelay; // For JmsStreamTimeTask
    public static long _msgTimerCount;  // For JmsStreamTimerTask
    private Timer _timer; // For JmsStreamTimerTask
    public static long _intTotalCount; // For print out
    private static int _intMsgPerZip; // For zipmsgperentry
    private static int _intZipMsgCount; // For zipmsgperentry
    private static int _intZipEntry;  // For zipmsgperentry
    private static int _intStopAfter;  // Stop listening and exit after x messages.
    private static int _intThreadCount = 0; // The total number of JmsStreamListener threads.
    @SuppressWarnings("unused")
    public final long _statsStartTime = 0;

    // Output Streams
    private FileOutputStream _outFileStream;
    private ZipOutputStream _zippedStream;
    private FileOutputStream _outCsvStream;

    // For date display
    //protected final SimpleDateFormat _dfXML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpleDateFormat _dateFormatter;

    private int _intThreadNumber; // The JmsStreamListener thread number.

    private final Hashtable _env;
    private String _strMessageSelector;
    private String _strDestination;
    private final ConnectionHelper _conHelper;
    private Date _originationTimestamp;
    private final String _strRcvTimestamp;
    private boolean _blnTrans;
    private boolean _blnXaTrans;
    private boolean _blnRequestReply;
    private boolean _blnExtractMonMsg;
    private boolean _blnReplyTimeout;

    private Connection _genericConnection;
    private Session _genericSession;
    private MessageConsumer _msgConsumer;
    private Destination _destination;

    private QueueConnection _queueConnection;
    private QueueSession _queueSession;
    private QueueBrowser _queueBrowser;
    private QueueReceiver _queueReceiver;
    private Queue _queue;

    private TopicConnection _topicConnection;
    private TopicSession _topicSession;
    private TopicSubscriber _topicSubscriber;
    private Topic _topic;

    private XAConnection _genericConnectionXA;
    private XASession _genericSessionXA;

    private XAQueueConnection _queueConnectionXA;
    private XAQueueSession _queueSessionXA;

    private XATopicConnection _topicConnectionXA;
    private XATopicSession _topicSessionXA;

    private Connection _jmsConnection;
    private Session _jmsSession;

    private TransactionManagerWrapper _txwManager;
    private XAResource _xaResource;
    private int _intNumInTrans;

    // For CSV Title field
    private String _strCsvFields;

    // Conditions
    private boolean _blnRaw;
    private final boolean _blnVerbose;
    private final boolean _blnNoEcho;
    private final boolean _blnEchoXML;
    private final boolean _blnEchoCVS;
    private final boolean _blnFileText;
    private final boolean _blnFileCVS;
    private final boolean _blnCommitOnExit;
    private final String _strStringEncoding;
    private long _intReplyTimeout;

    // In case there are multiple threads of this class.
    private Lock _locScreen = new ReentrantLock();
    private Lock _locMsgWrite = new ReentrantLock();
    private Lock _locCsvWrite = new ReentrantLock();

    @SuppressWarnings("unchecked")
    public JmsStreamListener(Hashtable env) throws Exception
    {
        _env = env;

        // Need to reset variable is this is a new listener.  This is here because a response listener should keep the setting of the request.
        // Convert to boolean if necessary.
        if (_env.get("resetthreads") != null && _env.get("resetthreads").getClass().equals(String.class))
            _env.put("resetthreads", Boolean.valueOf((String)_env.get("resetthreads")));
        _intThreadNumber = _env.get("resetthreads") != null && ((Boolean)_env.get("resetthreads") )? 1 : ++_intThreadCount;
        if (_intThreadNumber == 1) {
            _msgTimerCount = 0;  // For JmsStreamTimerTask
            _intTotalCount = 0; // For print out
            _intMsgPerZip = 0; // For zipmsgperentry
            _intZipMsgCount = 0; // For zipmsgperentry
            _intZipEntry = 1;  // For zipmsgperentry
            _intStopAfter =0;  // Stop listening and exit after x messages.
            _statsDelay = (Integer)_env.get("stats") * 1000; // Convert seconds to milliseconds
            _env.put("resetthreads", Boolean.FALSE);
        }

        _outFileStream = null;
        _zippedStream = null;
        _outCsvStream = null;

        _timer = null; // For JmsStreamTimerTask
        _strMessageSelector = "";
        _strDestination = "";
        _originationTimestamp = null;
        _blnTrans = false;
        _blnXaTrans = false;
        _blnRequestReply = false;
        _blnExtractMonMsg = false;
        _blnReplyTimeout = false;
        _genericConnection = null;
        _genericSession = null;
        _msgConsumer = null;
        _destination = null;
        _queueConnection = null;
        _queueSession = null;
        _queueBrowser = null;
        _queueReceiver = null;
        _queue = null;
        _topicConnection = null;
        _topicSession = null;
        _topicSubscriber = null;
        _topic = null;
        _genericConnectionXA = null;
        _genericSessionXA = null;
        _queueConnectionXA = null;
        _queueSessionXA = null;
        _topicConnectionXA = null;
        _topicSessionXA = null;
        _jmsConnection = null;
        _jmsSession = null;
        _txwManager = null;
        _xaResource = null;
        _intNumInTrans = Integer.MAX_VALUE;
        _strCsvFields = null;
        _blnRaw = false;
        _intReplyTimeout = 0;

        _blnRaw = _env.get("raw").equals(Boolean.TRUE);
        _blnVerbose = _env.get("verbose").equals(Boolean.TRUE);
        _blnNoEcho = _env.containsKey("noecho") && _env.get("noecho").equals(Boolean.TRUE);
        _blnEchoXML = _env.get("echoxml").equals(Boolean.TRUE);
        _blnEchoCVS = _env.get("echocsv").equals(Boolean.TRUE);
        _blnFileText = _env.containsKey("file");
        _blnFileCVS = _env.containsKey("csvfile");
        _blnTrans = _env.containsKey("trans");
        _blnReplyTimeout = _env.containsKey("replytimeout");
        _blnXaTrans = _blnTrans && _env.get("trans").equals("xa");
        _blnRequestReply = _env.containsKey("requestreply") && _env.get("requestreply").equals(Boolean.TRUE);
        _blnCommitOnExit = _env.containsKey("commitonexit") && _env.get("commitonexit").equals(Boolean.TRUE);
        if (_env.containsKey("selector")) _strMessageSelector = _env.get("selector").toString();
        if (_env.containsKey("replytimeout")) _intReplyTimeout = Long.parseLong(_env.get("replytimeout").toString());
        if (_env.containsKey("listendest")) _strDestination = _env.get("listendest").toString();
        if (_env.containsKey("transmsgnum") && (Integer)_env.get("transmsgnum") > 0) _intNumInTrans = (Integer)_env.get("transmsgnum");
        if (_env.containsKey("rcvtimestamp")) _strRcvTimestamp = _env.get("rcvtimestamp").toString(); else _strRcvTimestamp = null;
        if (_env.containsKey("zipmsgperentry")) _intMsgPerZip = (Integer)_env.get("zipmsgperentry");
        if (_env.containsKey("stopafter")) _intStopAfter = (Integer)_env.get("stopafter");

        _blnExtractMonMsg =  _env.containsKey("extractmonmsg") && _env.get("extractmonmsg").equals(Boolean.TRUE) && env.get("jmsclient").equals(ConnectionHelper.JMS_SERVER_TIBCO_EMS);

        // Get a readable date format for on-screen information
        _dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

        // Set encoding
        if (_env.containsKey("encoding")) {
            _strStringEncoding = _env.get("encoding").toString();
            com.tibco.tibjms.Tibjms.setEncoding(_strStringEncoding);
        }
        else
            _strStringEncoding = "UTF-8";

        if (_env.containsKey("osfile")) {
            _outFileStream = (FileOutputStream)_env.get("osfile");
            // And setup zipped output stream
            if (_env.containsKey("oszip")) {
                _zippedStream = (ZipOutputStream)_env.get("oszip");
            }
        }
        if (_env.containsKey("oscsvfile")) {
            _outCsvStream = (FileOutputStream)_env.get("oscsvfile");
        }

        // When the application is terminating we need to close the file.
        // This will be done by com.tibco.util.JmsStreamClose, which is registered here
        _shutdownThread = new JmsStreamClose(_outFileStream, JmsStreamClose.TEXT_TYPE, _zippedStream, _outCsvStream);
        Runtime.getRuntime().addShutdownHook(_shutdownThread);

        _conHelper = new ConnectionHelper(_env);
        // Set the JMS type if given, otherwise ConnectionHelper derive it from the connection factory
        if (_env.containsKey("type")) {
            if (_env.get("type").toString().equalsIgnoreCase("topic")) {
                if (_blnXaTrans) _conHelper.setFactoryType(ConnectionHelper.XA_TOPIC);
                else _conHelper.setFactoryType(ConnectionHelper.TOPIC);
            }
            else if (_env.get("type").toString().equalsIgnoreCase("queue")) {
                if (_blnXaTrans) _conHelper.setFactoryType(ConnectionHelper.XA_QUEUE);
                else _conHelper.setFactoryType(ConnectionHelper.QUEUE);
            }
            else if (_env.get("type").toString().equalsIgnoreCase("generic")) {
                if (_blnXaTrans) _conHelper.setFactoryType(ConnectionHelper.XA_GENERIC);
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
    }

    public static void clearCount()
    {
        _msgTimerCount = 0;  // For JmsStreamTimerTask
        _intTotalCount = 0; // For print out
        _intZipMsgCount = 0; // For zipmsgperentry
        _intZipEntry = 1;  // For zipmsgperentry
    }

    public void closeConnections()
    {
        _shutdownThread.run();
    }

    public void stopListening()
    {
        _blnLoop = false;
    }

    public void run()
    {
        long startWait;
        long endWait;
        int intAcknowledge = Session.AUTO_ACKNOWLEDGE;
        boolean blnConfirm;

        if (_env.containsKey("ackmode")) {
            try {
                intAcknowledge = ConnectionHelper.stringToAckType(_env.get("ackmode").toString(), _env.get("jmsclient").toString());
            }
            catch (Exception exc) {
                System.err.println("ERROR: Unrecognized acknowledgement mode " + _env.get("ackmode").toString());
                return;
            }
        }
        blnConfirm = !_env.get("noconfirm").equals(Boolean.TRUE) && (intAcknowledge == Session.CLIENT_ACKNOWLEDGE
                                                                     || intAcknowledge == com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE);

        try {
            switch (_conHelper.getFactoryType()) {
                case ConnectionHelper.GENERIC:
                    if (_genericConnection == null && _destination == null) {
                        ConnectionFactory _genericFactory = _conHelper.getConnectionFactory();
                        if (_env.containsKey("user") && _env.containsKey("password"))
                            _genericConnection = _genericFactory.createConnection(_env.get("user").toString(), _env.get("password").toString());
                        else if (_env.containsKey("user") && !_env.containsKey("password"))
                            _genericConnection = _genericFactory.createConnection(_env.get("user").toString(), "");
                        else
                            _genericConnection = _genericFactory.createConnection();
                        if (_env.containsKey("clientid")) _genericConnection.setClientID(_env.get("clientid").toString());
                        _genericSession = _genericConnection.createSession(_blnTrans, intAcknowledge);
                        _destination = _genericSession.createQueue(_strDestination);
                    }
                    else if (_genericSession == null) {
                        _genericSession = _genericConnection.createSession(_blnTrans, intAcknowledge);
                    }

                    if (_env.get("browse").equals(Boolean.FALSE)) {
                        _msgConsumer = _genericSession.createConsumer(_destination, _strMessageSelector);
                    }
                    else {
                        _queueBrowser = _genericSession.createBrowser((Queue)_destination, _strMessageSelector);
                    }

                    // Start the connection
                    if (_genericConnection != null) {
                        _jmsConnection = _genericConnection;
                        _jmsSession = _genericSession;
                        _genericConnection.start();
                    }
                    break;
                case ConnectionHelper.QUEUE:
                    if (_queueConnection == null && _queue == null) {
                        QueueConnectionFactory _queueFactory = (QueueConnectionFactory) _conHelper.getConnectionFactory();
                        if (_env.containsKey("user") && _env.containsKey("password"))
                            _queueConnection = _queueFactory.createQueueConnection(_env.get("user").toString(), _env.get("password").toString());
                        else if (_env.containsKey("user") && !_env.containsKey("password"))
                            _queueConnection = _queueFactory.createQueueConnection(_env.get("user").toString(), "");
                        else
                            _queueConnection = _queueFactory.createQueueConnection();
                        if (_env.containsKey("clientid")) _queueConnection.setClientID(_env.get("clientid").toString());
                        _queueSession = _queueConnection.createQueueSession(_blnTrans, intAcknowledge);
                        _queue = _queueSession.createQueue(_strDestination);
                    }
                    else if (_queueSession == null) {
                        _queueSession = _queueConnection.createQueueSession(_blnTrans, intAcknowledge);
                    }

                    if (_env.get("browse").equals(Boolean.FALSE)) {
                        _queueReceiver = _queueSession.createReceiver(_queue, _strMessageSelector);
                    }
                    else {
                        _queueBrowser = _queueSession.createBrowser(_queue, _strMessageSelector);
                    }

                    // Start the connection
                    if (_queueConnection != null) {
                        _jmsConnection = _queueConnection;
                        _jmsSession = _queueSession;
                        _queueConnection.start();
                    }

                    break;
                case ConnectionHelper.TOPIC:
                    if (_topicConnection == null && _topic == null) {
                        TopicConnectionFactory _topicFactory = (TopicConnectionFactory) _conHelper.getConnectionFactory();
                        if (_env.containsKey("user") && _env.containsKey("password"))
                            _topicConnection = _topicFactory.createTopicConnection(_env.get("user").toString(), _env.get("password").toString());
                        else if (_env.containsKey("user") && !_env.containsKey("password"))
                            _topicConnection = _topicFactory.createTopicConnection(_env.get("user").toString(), "");
                        else
                            _topicConnection = _topicFactory.createTopicConnection();
                        if (_env.containsKey("clientid")) _topicConnection.setClientID(_env.get("clientid").toString());
                        _topicSession = _topicConnection.createTopicSession(_blnTrans, intAcknowledge);

                        // unsubscribe to a durable name
                        if (_env.get("unsubscribe").equals(Boolean.TRUE)) {
                            _locScreen.lock();
                            System.out.println(" " + JmsStream.APP_NAME + " started");
                            System.out.println(" -------------------");
                            System.out.println(" jms client      :  " + _env.get("jmsclient").toString());
                            System.out.println(" provider url    :  " + _env.get(javax.naming.Context.PROVIDER_URL).toString());
                            System.out.println(" factory name    :  " + _env.get("connectionfactory").toString());
                            System.out.println(" connection info :  " + _topicConnection.toString());
                            System.out.println(" transaction type:  none");
                            System.out.println(" listening to    :  " + _strDestination);
                            if (_env.containsKey("durablename")) System.out.println(" durable name    :  " + _env.get("durablename").toString());
                            System.out.println(" connected ...");
                            System.out.println("\n Unsubscribing durable " + _env.get("durablename").toString() + " ...");

                            try {
                                _topicSession.unsubscribe(_env.get("durablename").toString());
                                System.out.println(" Successfully unsubscribed " + _env.get("durablename").toString());
                            }
                            catch (javax.jms.InvalidDestinationException ide) {
                                System.out.println(" " + ide.getMessage() + "\n");
                            }

                            _topicConnection.close();
                            _locScreen.unlock();
                            return;
                        }
                        _topic = _topicSession.createTopic(_strDestination);
                    }
                    else if (_topicSession == null) {
                        _topicSession = _topicConnection.createTopicSession(_blnTrans, intAcknowledge);
                    }

                    if (_env.containsKey("durablename"))
                        _topicSubscriber = _topicSession.createDurableSubscriber(_topic, _env.get("durablename").toString(), _strMessageSelector, false);
                    else
                        _topicSubscriber = _topicSession.createSubscriber(_topic, _strMessageSelector, false);

                    // Start the connection
                    if (_topicConnection != null) {
                        _jmsConnection = _topicConnection;
                        _jmsSession = _topicSession;
                        _topicConnection.start();
                    }

                    break;
                case ConnectionHelper.XA_GENERIC:
                    if (_genericConnectionXA == null && _destination == null) {
                        XAConnectionFactory genericFactoryXA = _conHelper.getXAConnectionFactory();
                        if (_env.containsKey("user") && _env.containsKey("password"))
                            _genericConnectionXA = genericFactoryXA.createXAConnection(_env.get("user").toString(), _env.get("password").toString());
                        else if (_env.containsKey("user") && !_env.containsKey("password"))
                            _genericConnectionXA = genericFactoryXA.createXAConnection(_env.get("user").toString(), "");
                        else
                            _genericConnectionXA = genericFactoryXA.createXAConnection();
                        if (_env.containsKey("clientid")) _genericConnectionXA.setClientID(_env.get("clientid").toString());
                        _genericSessionXA = _genericConnectionXA.createXASession();
                        _destination = _genericSessionXA.createQueue(_strDestination);
                    }
                    else if (_genericSession == null) {
                        _genericSessionXA = _genericConnectionXA.createXASession();
                    }

                    if (_env.get("browse").equals(Boolean.FALSE)) {
                        _msgConsumer = _genericSessionXA.createConsumer(_destination, _strMessageSelector);
                    }
                    else {
                        _queueBrowser = _genericSessionXA.createBrowser((Queue)_destination, _strMessageSelector);
                    }
                    // Start the connection
                    if (_genericConnectionXA != null) {
                        _jmsConnection = _genericConnectionXA;
                        _genericConnectionXA.start();
                    }

                    // Enlist the XA resource in the current transaction.
                    _xaResource = _genericSessionXA.getXAResource();

                    break;
                case ConnectionHelper.XA_QUEUE:
                    if (_queueConnectionXA == null && _queue == null) {
                        XAQueueConnectionFactory queueFactoryXA = (XAQueueConnectionFactory) _conHelper.getXAConnectionFactory();
                        if (_env.containsKey("user") && _env.containsKey("password"))
                            _queueConnectionXA = queueFactoryXA.createXAQueueConnection(_env.get("user").toString(), _env.get("password").toString());
                        else if (_env.containsKey("user") && !_env.containsKey("password"))
                            _queueConnectionXA = queueFactoryXA.createXAQueueConnection(_env.get("user").toString(), "");
                        else
                            _queueConnectionXA = queueFactoryXA.createXAQueueConnection();
                        if (_env.containsKey("clientid")) _queueConnectionXA.setClientID(_env.get("clientid").toString());
                        _queueSessionXA = _queueConnectionXA.createXAQueueSession();
                        _queue = _queueSessionXA.createQueue(_strDestination);
                    }
                    else if (_queueSessionXA == null) {
                        _queueSessionXA = _queueConnectionXA.createXAQueueSession();
                    }

                    if (_env.get("browse").equals(Boolean.FALSE)) {
                        _queueReceiver = _queueSessionXA.getQueueSession().createReceiver(_queue, _strMessageSelector);
                    }
                    else {
                        _queueBrowser = _queueSessionXA.createBrowser(_queue, _strMessageSelector);
                    }

                    // Start the connection
                    if (_queueConnectionXA != null) {
                        _jmsConnection = _queueConnectionXA;
                        _queueConnectionXA.start();
                    }

                    // Enlist the XA resource in the current transaction.
                    _xaResource = _queueSessionXA.getXAResource();

                    break;
                case ConnectionHelper.XA_TOPIC:
                    if (_topicConnectionXA == null && _topic == null) {
                        XATopicConnectionFactory topicFactoryXA = (XATopicConnectionFactory) _conHelper.getXAConnectionFactory();
                        if (_env.containsKey("user") && _env.containsKey("password"))
                            _topicConnectionXA = topicFactoryXA.createXATopicConnection(_env.get("user").toString(), _env.get("password").toString());
                        else if (_env.containsKey("user") && !_env.containsKey("password"))
                            _topicConnectionXA = topicFactoryXA.createXATopicConnection(_env.get("user").toString(), "");
                        else
                            _topicConnectionXA = topicFactoryXA.createXATopicConnection();
                        if (_env.containsKey("clientid")) _topicConnectionXA.setClientID(_env.get("clientid").toString());
                        _topicSessionXA = _topicConnectionXA.createXATopicSession();

                        // unsubscribe to a durable name
                        if (_env.get("unsubscribe").equals(Boolean.TRUE)) {
                            _locScreen.lock();
                            System.out.println(" " + JmsStream.APP_NAME + " started");
                            System.out.println(" -------------------");
                            System.out.println(" jms client      :  " + _env.get("jmsclient").toString());
                            System.out.println(" provider url    :  " + _env.get(javax.naming.Context.PROVIDER_URL).toString());
                            System.out.println(" factory name    :  " + _env.get("connectionfactory").toString());
                            System.out.println(" connection info :  " + _topicConnectionXA.toString());
                            System.out.println(" transaction type:  XA");
                            System.out.println(" listening to    :  " + _strDestination);
                            if (_env.containsKey("durablename")) System.out.println(" durable name    :  " + _env.get("durablename").toString());
                            System.out.println(" connected ...");
                            System.out.println("\n Unsubscribing durable " + _env.get("durablename").toString() + " ...");
                            _topicSessionXA.unsubscribe(_env.get("durablename").toString());
                            System.out.println(" Successfully unsubscribed " + _env.get("durablename").toString());
                            _topicConnectionXA.close();
                            _locScreen.unlock();
                            return;
                        }
                        _topic = _topicSessionXA.createTopic(_strDestination);
                    }
                    else if (_topicSessionXA == null) {
                        _topicSessionXA = _topicConnectionXA.createXATopicSession();
                    }

                    if (_env.containsKey("durablename"))
                        _topicSubscriber = _topicSessionXA.createDurableSubscriber(_topic, _env.get("durablename").toString(), _strMessageSelector, false);
                    else
                        _topicSubscriber = _topicSessionXA.getTopicSession().createSubscriber(_topic, _strMessageSelector, false);

                    // Start the connection
                    if (_topicConnectionXA != null) {
                        _jmsConnection = _topicConnectionXA;
                        _topicConnectionXA.start();
                    }

                    // Enlist the XA resource in the current transaction.
                    _xaResource = _topicSessionXA.getXAResource();

                    break;
                default:
                    throw new Exception("Unsupported connection factory:  " + _conHelper.getFactoryDescription());
            }

            // Register the JMS Connection with the stop thread
            _shutdownThread.setJmsConnection(_jmsConnection);

            // Register the JMS Session with the stop thread if there are JMS Session transactions
            if (_blnTrans && !_blnXaTrans) _shutdownThread.setJmsSession(_jmsSession, _blnCommitOnExit);

            // Forces listener to stop after replay message is received. (for Request/Reply only)
            _blnLoop = !_blnRequestReply;

            // If stats is turned on - start the metrics output
            if (_statsDelay > 0 && _blnLoop) {
                JmsStreamTimerTask task = new JmsStreamTimerTask(this);
                _timer = new Timer();
                _timer.scheduleAtFixedRate(task, _statsDelay, _statsDelay);
            }

            if (_blnLoop) {
                _locScreen.lock();
                System.out.println(" " + JmsStream.APP_NAME + " started");
                System.out.println(" -------------------");
                System.out.println(" jms client      :  " + _env.get("jmsclient").toString());
                System.out.println(" provider url    :  " + _env.get(javax.naming.Context.PROVIDER_URL).toString());
                System.out.println(" factory name    :  " + _env.get("connectionfactory").toString());
                System.out.println(" connection info :  " + _jmsConnection.toString());
                if (_blnXaTrans) System.out.println(" transaction type:  XA");
                else if (_blnTrans) System.out.println(" transaction type:  JMS");
                else System.out.println(" transaction type:  none");
                if (_blnFileText) System.out.println(" message file    :  "
                                                     + (_env.get("fileappend").equals(Boolean.TRUE) ? "Appending to " : "")
                                                     + _env.get("file").toString());
                if (_blnFileCVS)  System.out.println(" CSV save        :  " + _env.get("csvfile").toString());
                System.out.println(" ack mode        :  " + ConnectionHelper.ackTypeToString(intAcknowledge) + " "
                                   + (!blnConfirm && (intAcknowledge == Session.CLIENT_ACKNOWLEDGE
                                                      || intAcknowledge == com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE)
                                      ? "with no confirmation" : ""));
                if (_env.containsKey("browse") && _env.get("browse").equals(Boolean.TRUE))
                    System.out.println(" browsing        :  " + _strDestination);
                else
                    System.out.println(" listening to    :  " + _strDestination);

                if (_env.containsKey("extractmonmsg")) System.out.println(" extract $sys msg:  " + _env.get("extractmonmsg").toString());
                if (_intStopAfter > 0) System.out.println(" stop after msg  :  " + _intStopAfter);
                if (_env.containsKey("selector")) System.out.println(" selector        :  " + _env.get("selector").toString());
                if (_env.containsKey("durablename")) System.out.println(" durable name    :  " + _env.get("durablename").toString());
                System.out.println(" connected ...");
                System.out.println();
                _locScreen.unlock();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            _locScreen.unlock();
            return;
        }

        // Create consumer for subscriber, receiver, and browser.
        // Also, we take the time between two messages, so we can replay in real-time
        try {
            int intLocalCount = 0; // Used to count the messages in a transaction

            // If the connection is XA, connect to the transaction manager and create a transaction.
            if (_blnXaTrans) {
                _txwManager = new TransactionManagerWrapper(_env);
                _shutdownThread.setTransactionManagerWrapper(_txwManager);
                newXaTrans(); // Create new XA transaction
            }

            if (_queueBrowser != null) {
                // Only browse the queue - do NOT take messages off the _queue
                try {
                    startWait = System.currentTimeMillis();
                    Enumeration messages = _queueBrowser.getEnumeration();
                    do {
                        while (messages.hasMoreElements() && _blnLoop) {
                            Message message = (Message)messages.nextElement();
                            endWait = System.currentTimeMillis();
                            intLocalCount++;
                            if (_blnTrans && (intLocalCount % _intNumInTrans == 0)) {
                                if (_blnXaTrans) {
                                    _txwManager.commit(); // Commit the XA transaction
                                    newXaTrans(); // Create new XA transaction
                                }
                                else _queueSession.commit();
                                writeMessage(message, (endWait - startWait), true);
                            }
                            else writeMessage(message, (endWait - startWait), false);
                            if (_intStopAfter > 0 && _intTotalCount >= _intStopAfter) _blnLoop = false; // -stopafter
                        }
                        try {
                            // If the enumeration is empty, sleep for a while. JMS will add message to the enumeration
                            // when they arrive. No need to "re-fetch"!
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {
                            System.err.println(" JmsStream Listener thread interrupted.");
                        }
                        // Need to stop the second loop also.
                        if (_intStopAfter > 0 && _intTotalCount >= _intStopAfter) _blnLoop = false; // -stopafter
                    } while (_blnLoop);
                }
                catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            else if (_msgConsumer != null) {
                try {
                    // Receive from _destination - this WILL pop messages of queues if the destination is a queue
                    do {
                        startWait = System.currentTimeMillis();
                        Message message;
                        if (_blnReplyTimeout)
                            message = _msgConsumer.receive(_intReplyTimeout);
                        else
                            message = _msgConsumer.receive();

                        if (message == null) break;

                        endWait = System.currentTimeMillis();
                        intLocalCount++;
                        if (_blnTrans && _intNumInTrans > 0) { // If using transaction, confirmation is automatic on commit.
                            if (intLocalCount % _intNumInTrans == 0) {
                                if (_blnXaTrans) {
                                    _txwManager.commit(); // Commit the XA transaction
                                    newXaTrans(); // Create new XA transaction
                                }
                                else _genericSession.commit();
                                writeMessage(message, (endWait - startWait), false);
                            }
                            else writeMessage(message, (endWait - startWait), false);
                        }
                        else {
                            writeMessage(message, (endWait - startWait), false);
                            if (blnConfirm) message.acknowledge();
                        }
                        if (_intStopAfter > 0 && _intTotalCount >= _intStopAfter) _blnLoop = false; // -stopafter
                    } while (_blnLoop);
                }
                catch (JMSException e) {
                    // If the thread was interrupted, ignore the error.
                    if (e.getMessage().equals("Thread has been interrupted")) {
                        // Must suspend() the TransactionManager in this thread so the Transaction can be resume()
                        // and completed with the TransactionManager in the shutdown thread.  Each thread has its
                        // own TransactionManager in JTA.
                        if (_txwManager != null) _txwManager.suspend();
                    }
                    else e.printStackTrace();
                }
            }
            else if (_topicSubscriber != null) {
                try {
                    do {
                        if (_intStopAfter > 0 && _intTotalCount >= _intStopAfter) return; // -stopafter
                        startWait = System.currentTimeMillis();

                        Message message;
                        if (_blnReplyTimeout)
                            message = _topicSubscriber.receive(_intReplyTimeout);
                        else
                            message = _topicSubscriber.receive();

                        if (message == null) break;

                        endWait = System.currentTimeMillis();

                        // If this is a TIBCO EMS Monitor message, extract and save the original message.
                        if (_blnExtractMonMsg) {
                            byte [] bytObject = ((MapMessage)message).getBytes("message_bytes");
                            message = com.tibco.tibjms.Tibjms.createFromBytes(bytObject);
                        }

                        intLocalCount++;
                        if (_blnTrans && _intNumInTrans > 0) { // If using transaction, confirmation is automatic on commit.
                            if (intLocalCount % _intNumInTrans == 0) {
                                if (_blnXaTrans) {
                                    _txwManager.commit(); // Commit the XA transaction
                                    newXaTrans(); // Create new XA transaction
                                }
                                else _topicSession.commit();
                                writeMessage(message, (endWait - startWait), true);
                            }
                            else writeMessage(message, (endWait - startWait), false);
                        }
                        else {
                            writeMessage(message, (endWait - startWait), false);
                            if (blnConfirm) message.acknowledge();
                        }
                        if (_intStopAfter > 0 && _intTotalCount >= _intStopAfter) _blnLoop = false; // -stopafter
                    } while (_blnLoop);
                }
                catch (JMSException e) {
                    // If the thread was interrupted, ignore the error.
                    if (e.getMessage().equals("Thread has been interrupted")) {
                        // Must suspend() the TransactionManager in this thread so the Transaction can be resume()
                        // and completed with the TransactionManager in the shutdown thread.  Each thread has its
                        // own TransactionManager in JTA.
                        if (_txwManager != null) _txwManager.suspend();
                    }
                    else e.printStackTrace();
                }
            }
            else if (_queueReceiver != null) {
                try {
                    // Receive from _queue - this WILL pop messages of queues
                    do {
                        startWait = System.currentTimeMillis();
                        Message message;
                        if (_blnReplyTimeout)
                            message = _queueReceiver.receive(_intReplyTimeout);
                        else
                            message = _queueReceiver.receive();

                        if (message == null) break;

                        endWait = System.currentTimeMillis();
                        intLocalCount++;
                        if (_blnTrans && _intNumInTrans > 0) { // If using transaction, confirmation is automatic on commit.
                            if (intLocalCount % _intNumInTrans == 0) {
                                if (_blnXaTrans) {
                                    _txwManager.commit(); // Commit the XA transaction
                                    newXaTrans(); // Create new XA transaction
                                }
                                else _queueSession.commit();
                                writeMessage(message, (endWait - startWait), true);
                            }
                            else writeMessage(message, (endWait - startWait), false);
                        }
                        else {
                            writeMessage(message, (endWait - startWait), false);
                            if (blnConfirm) message.acknowledge();
                        }

                        if (_intStopAfter > 0 && _intTotalCount >= _intStopAfter) _blnLoop = false; // -stopafter
                    } while (_blnLoop);
                }
                catch (JMSException e) {
                    // If the thread was interrupted, ignore the error.
                    if (e.getMessage().equals("Thread has been interrupted") || e.getMessage().equals("java.lang.InterruptedException")) {
                        // Must suspend() the TransactionManager in this thread so the Transaction can be resume()
                        // and completed with the TransactionManager in the shutdown thread.  Each thread has its
                        // own TransactionManager in JTA.
                        if (_txwManager != null) _txwManager.suspend();
                    }
                    else e.printStackTrace();
                }
            }
            else {
                throw new Exception("ERROR:  Unsupported consumer.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (_timer != null) _timer.cancel();
        if (!_blnRequestReply) System.out.println("\n Stopping... " + _intTotalCount + " messages received.");
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
     * The method setJmsListener is only used for Request/Reply call from JmsStreamPublisher.
     * No transactional support is provided in the method because JMS transactional messages are not supported for Request/Reply.
     *
     * @param connection  JMS Connection.
     * @param destination  JMS Destination.
     * @param strMessageSelector  JMS message selector String.
     * @throws Exception  Throws an exception on error.
     */
    public void setJmsListener(Connection connection, Destination destination, String strMessageSelector) throws Exception
    {
        _strMessageSelector = strMessageSelector;

        if (TopicConnection.class.isInstance(connection)) {
            _topicConnection = (TopicConnection)connection;
        }
        else if (QueueConnection.class.isInstance(connection)) {
            _queueConnection = (QueueConnection)connection;
        }
        else if (Connection.class.isInstance(connection)) {
            _genericConnection = connection;
        }
        else {
            throw new Exception("ERROR: Unsupported connection type.");
        }

        if (Topic.class.isInstance(destination)) {
            _topic = (Topic)destination;
        }
        else if (Queue.class.isInstance(destination)) {
            _queue = (Queue)destination;
        }
        else if (Destination.class.isInstance(destination)) {
            _destination = destination;
        }
        else {
            throw new Exception("ERROR: Unsupported destination type.");
        }
    }

    public void writeMessage(Message jmsMessage, long waitTime, boolean blnCommitTrans)
    {
        String strCsvBuffer = "";
        String strTextBuffer = "";
        String strXmlBuffer = "";
        String strCsvFields = "";
        String strMsg;

        _intTotalCount++;
        _msgTimerCount++;

        Date dateReceiveTime = new Date();

        // If there is no file output or echo then return
        if (_statsDelay > 0 && !_blnFileText && !_blnFileCVS) return;

        if (_blnRaw && !_blnFileText && !_blnFileCVS) {
            _locScreen.lock();
            try {
                System.out.println("\n#---------- #" + _intTotalCount + " : " + jmsMessage.getJMSMessageID() + " ----------#");
                System.out.println(jmsMessage.toString());
            }
            catch (JMSException jmsx) {
                jmsx.printStackTrace();
            }
            finally {
                _locScreen.unlock();
            }
            // No need to do anything else.  Exit this method
            return;
        }

        try {
            // Get the reply to destination name
            String strJMSReplyTo;
            if (Topic.class.isInstance(jmsMessage.getJMSReplyTo())) {
                strJMSReplyTo = ((Topic)jmsMessage.getJMSReplyTo()).getTopicName();
            }
            else if (Queue.class.isInstance(jmsMessage.getJMSReplyTo())) {
                strJMSReplyTo = ((Queue)jmsMessage.getJMSReplyTo()).getQueueName();
            }
            else {
                strJMSReplyTo = null;
            }

            String strJMSDestination;
            if (Topic.class.isInstance(jmsMessage.getJMSDestination())) {
                strJMSDestination = ((Topic)jmsMessage.getJMSDestination()).getTopicName();
            }
            else if (Queue.class.isInstance(jmsMessage.getJMSDestination())) {
                strJMSDestination = ((Queue)jmsMessage.getJMSDestination()).getQueueName();
            }
            else {
                strJMSDestination = null;
            }

            String strHeader;
            String strJMSMessageID = jmsMessage.getJMSMessageID();
            String strJMSCorrelationID = jmsMessage.getJMSCorrelationID();
            String strJMSDeliveryMode = String.valueOf(jmsMessage.getJMSDeliveryMode());
            String strJMSPriority = String.valueOf(jmsMessage.getJMSPriority());
            String strJMSType = jmsMessage.getJMSType();
            String strJMSExpiration = null;
            String strISOExpiration = null;
            String strJMSTimestamp = null;
            String strISOTimestamp = null;
            String strOriginationTimestamp;

            if (jmsMessage.getJMSExpiration() != 0) {
                strJMSExpiration = String.valueOf(jmsMessage.getJMSExpiration());
                strISOExpiration = _dateFormatter.format(new Date(jmsMessage.getJMSExpiration()));
            }
            if (jmsMessage.getJMSTimestamp() != 0) {
                strJMSTimestamp = String.valueOf(jmsMessage.getJMSTimestamp());
                strISOTimestamp = _dateFormatter.format(new Date(jmsMessage.getJMSTimestamp()));
            }
            Enumeration enuMap =  jmsMessage.getPropertyNames();

            // HEADER section of jmsMessage
            String strMessageType = "";
            if (javax.jms.BytesMessage.class.isInstance(jmsMessage)) strMessageType = "BytesMessage";
            if (javax.jms.MapMessage.class.isInstance(jmsMessage)) strMessageType = "MapMessage";
            if (javax.jms.ObjectMessage.class.isInstance(jmsMessage)) strMessageType = "ObjectMessage";
            if (javax.jms.StreamMessage.class.isInstance(jmsMessage)) strMessageType = "StreamMessage";
            if (javax.jms.TextMessage.class.isInstance(jmsMessage)) strMessageType = "TextMessage";

            //  Timestamp of the original request when testing request/reply performance.
            strOriginationTimestamp = _originationTimestamp == null ? null : _dateFormatter.format(_originationTimestamp);

            if (_blnVerbose || _blnEchoXML || _blnFileText) {
                strXmlBuffer += appendString(1, "<message type=\"" + strMessageType
                                                + "\" messageSelector=\"" + _strMessageSelector + "\""
                                                + (strOriginationTimestamp == null ? "" : " originationTimestamp=\"" + strOriginationTimestamp + "\"")
                                                + " receiveTime=\"" + _dateFormatter.format(dateReceiveTime) + "\""
                                                + (strISOTimestamp == null ? "" : " jmsServerTimestamp=\"" + strISOTimestamp + "\"")
                                                + (strISOExpiration == null ? "" : " jmsMsgExpiration=\"" + strISOExpiration + "\"")
                                                + ">");

                strHeader = "<header ";
                if (strJMSMessageID != null && !strJMSMessageID.equals("")) strHeader += "JMSMessageID=\"" + strJMSMessageID + "\" ";
                if (strJMSDestination != null && !strJMSDestination.equals("")) strHeader += "JMSDestination=\"" + strJMSDestination + "\" ";
                if (strJMSDestination != null && !strJMSDestination.equals("")) strHeader += "JMSDestinationType=\"" + _conHelper.getFactoryDescription() + "\" ";
                if (strJMSReplyTo != null && !strJMSReplyTo.equals("")) strHeader += "JMSReplyTo=\"" + strJMSReplyTo + "\" ";
                if (strJMSCorrelationID != null && !strJMSCorrelationID.equals("")) strHeader += "JMSCorrelationID=\"" + strJMSCorrelationID + "\" ";
                if (strJMSDeliveryMode != null && !strJMSDeliveryMode.equals("")) strHeader += "JMSDeliveryMode=\"" + strJMSDeliveryMode + "\" ";
                if (strJMSPriority != null && !strJMSPriority.equals("")) strHeader += "JMSPriority=\"" + strJMSPriority + "\" ";
                if (strJMSType != null && !strJMSType.equals("")) strHeader += "JMSType=\"" + strJMSType + "\" ";
                if (strJMSExpiration != null && !strJMSExpiration.equals("")) strHeader += "JMSExpiration=\"" + strJMSExpiration + "\" ";
                if (strJMSTimestamp != null && !strJMSTimestamp.equals("")) strHeader += "JMSTimestamp=\"" + strJMSTimestamp + "\"";
                strHeader += "/>";
                strXmlBuffer += appendString (2, strHeader);

                // PROPERTIES section of jmsMessage
                strXmlBuffer += appendString(2, "<properties>");
                while (enuMap.hasMoreElements()) {
                    String name = enuMap.nextElement().toString();
                    Object element = jmsMessage.getObjectProperty(name);
                    String className = element.getClass().getName().replaceFirst("java.lang.", "");
                    strXmlBuffer += appendString(3, "<property name=\"" + FormatHelper.translateXML(name) + "\" type=\"" + className + "\">" + FormatHelper.translateXML(element.toString()) + "</property>");
                }
                if (_strRcvTimestamp != null) { // VERY IMPORTANT DO NOT OMIT
                    strXmlBuffer += appendString(3, "<property name=\"" + FormatHelper.translateXML(_strRcvTimestamp) + "\" type=\"String\">" + _dateFormatter.format(dateReceiveTime) + "</property>");
                }
                strXmlBuffer += appendString(2, "</properties>");
                strXmlBuffer += appendString(1, "</message>");

                if (_blnVerbose || _blnFileText) {
                    strTextBuffer += "\n#---------- #" + _intTotalCount
                                     + " : " + strJMSMessageID
                                     + " ----------#\n";
                    strTextBuffer += "<MSG_INFO>" + "\n";
                    // If we record with timed statistics (-time), put the wait time in the XML file
                    if (_env.get("timed").equals(Boolean.TRUE))
                        strTextBuffer += appendString(1, "<wait>" + (waitTime) + "</wait>");
                    if (blnCommitTrans)
                        strTextBuffer += appendString(1, "<commitTrans>true</commitTrans>");
                    strTextBuffer += strXmlBuffer;
                    strTextBuffer += "</MSG_INFO>";
                }

                // Message handlers
                if (MapMessage.class.isInstance(jmsMessage)) {
                    MapMessage msg = (MapMessage)jmsMessage;
                    String strMapMsgXML = mapMessageToXml(1, msg, null);
                    if (_blnEchoXML) {
                        strXmlBuffer += strMapMsgXML;
                    }
                    if (_blnVerbose || _blnFileText) {
                        // Must use getBytes().length to correctly size UTF-8 content.
                        strTextBuffer += "\nBodyLength=" + strMapMsgXML.getBytes(_strStringEncoding).length + "\n" + strMapMsgXML;
                    }
                }
                else if (TextMessage.class.isInstance(jmsMessage)) {
                    TextMessage msg = (TextMessage)jmsMessage;
                    strMsg = msg.getText();
                    if (_blnEchoXML) {
                        strXmlBuffer += appendString(3, "<node name=\"Text\" type=\"String\">" + FormatHelper.translateXML(msg.getText()) + "</node>");
                    }
                    if (_blnVerbose || _blnFileText) {
                        // Must use getBytes().length to correctly size UTF-8 content.
                        strTextBuffer += "\nBodyLength=" + strMsg.getBytes(_strStringEncoding).length + "\n" + strMsg;
                    }
                }
                else if (ObjectMessage.class.isInstance(jmsMessage)) {
                    ObjectMessage msg = (ObjectMessage)jmsMessage;
                    strMsg = msg.getObject().toString();
                    if (_blnVerbose || _blnFileText) {
                        // Must use getBytes().length to correctly size UTF-8 content.
                        strTextBuffer += "\nBodyLength=" + strMsg.getBytes(_strStringEncoding).length + "\n" + strMsg;
                    }
                }
                else if (BytesMessage.class.isInstance(jmsMessage)) {
                    BytesMessage msg = (BytesMessage)jmsMessage;
                    Base64 base64encoder = new Base64();
                    byte[] byteBuffer = new byte[(int)msg.getBodyLength()];
                    msg.readBytes(byteBuffer);
                    strMsg = base64encoder.encode(byteBuffer);
                    if (_blnEchoXML) {
                        strXmlBuffer += appendString(3, "<node name=\"base64\" type=\"String\">" + FormatHelper.translateXML(strMsg) + "</node>");
                    }
                    if (_blnVerbose || _blnFileText) {
                        // Use .length() because there is no UTF-8 content in Base64.
                        strTextBuffer += "\nBodyLength=" + strMsg.length() + "\n" + strMsg;
                    }
                }
                strXmlBuffer += appendString(2, "</body>");
                strXmlBuffer += appendString(1, "</jmsMessage>");
            }

            if (_blnEchoCVS || _blnFileCVS) {
                if (_intThreadNumber == 1) {
                    // Get the Message Header Names only if this is the first thread.
                    strCsvFields = "JMSMessageID,JMSDestination,JMSReplyTo,JMSCorrelationID,JMSDeliveryMode,"
                                   + "JMSPriority,JMSType,JMSExpiration,JMSTimestamp";
                    while (enuMap.hasMoreElements()) {
                        strCsvFields += ("," + enuMap.nextElement().toString());
                    }

                    strCsvFields += (_originationTimestamp == null ? "" : ",originationTimestamp");
                    strCsvFields += (_strRcvTimestamp == null ? "" : "," + _strRcvTimestamp);
                }

                 // Get the Message Header Values
                strCsvBuffer = (strJMSMessageID == null ? "" : strJMSMessageID) + ","
                               + (strJMSDestination == null ? "" : strJMSDestination) + ","
                               + (strJMSReplyTo == null ? "" : strJMSReplyTo) + ","
                               + (strJMSCorrelationID == null ? "" : strJMSCorrelationID) + ","
                               + (strJMSDeliveryMode == null ? "" : strJMSDeliveryMode) + ","
                               + (strJMSPriority == null ? "" : strJMSPriority) + ","
                               + (strJMSType == null ? "" : strJMSType) + ","
                               + (strJMSExpiration == null ? "" : strISOExpiration) + ","
                               + (jmsMessage.getJMSTimestamp() == 0 ? "" : strISOTimestamp);
                enuMap = jmsMessage.getPropertyNames(); // Reset the Enumerator to get the values.
                while (enuMap.hasMoreElements()) {
                    String name = enuMap.nextElement().toString();
                    Object element = jmsMessage.getObjectProperty(name);
                    strCsvBuffer += ("," + element.toString());
                }
                strCsvBuffer += (strOriginationTimestamp == null ? "" : "," + strOriginationTimestamp);
                strCsvBuffer += (_strRcvTimestamp == null ? "" : "," + _dateFormatter.format(dateReceiveTime));
            }

            try {  // Print jmsMessage to screen
                // Lock section to prevent race condition.
                if (_blnVerbose) {
                    _locScreen.lock();
                    System.out.println(strTextBuffer);
                    _locScreen.unlock();
                }
                else if (_blnEchoCVS) {
                    _locScreen.lock();
                    System.out.println();
                    if (!strCsvFields.equals(_strCsvFields) && _intThreadNumber == 1) System.out.println(strCsvFields);
                    System.out.print(strCsvBuffer); // no change return necessary
                    _locScreen.unlock();
                }
                else if (_blnEchoXML) { // Print jmsMessage on stdout if "echoxml" is true
                    _locScreen.lock();
                    System.out.println(" " + _dateFormatter.format(new Date()) + ":");
                    System.out.println(strXmlBuffer);
                    _locScreen.unlock();
                }
                else if (!_blnNoEcho) {
                    _locScreen.lock();
                    System.out.println(jmsMessage.toString());
                    _locScreen.unlock();
                }

                if (_outCsvStream != null) {
                    if (!strCsvFields.equals(_strCsvFields) && _intThreadNumber == 1) writeCsv(strCsvFields);
                    writeCsv(strCsvBuffer);
                }

                // The string buffer now holds the complete jmsMessage; write it to disk NOW
                if (_outFileStream != null) {
                    if(_blnFileText) {
                        writeText(strTextBuffer);
                    }
                    /* // This else gives errors when everything is OK.
                    else {
                        throw new Exception("ERROR: Wrong file type.");
                    }
                    */
                }

                // If the CSV fields have changed then update the global CSV fields variable
                if ((_blnEchoCVS || _outCsvStream != null) && !strCsvFields.equals(_strCsvFields)) {
                    _locScreen.lock();
                    _strCsvFields = strCsvFields;
                    _locScreen.unlock();
                }
            }
            catch (Exception ex) {
                _locScreen.unlock();
                ex.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String appendString(int tab, String strIn)
    {
        for (int i = 0; i < tab * 4; i++) strIn = " " + strIn;
        strIn += "\n";
        return strIn;
    }

    public void writeText(String strOut)
    {
        if (_outFileStream != null) {
            strOut = strOut.concat("\n");
            if (_zippedStream != null) { // ZIP file
                try {
                    _locMsgWrite.lock();
                    if (_intMsgPerZip > 0) { // Break up messages into separate ZIP entries for large messages captures.
                        if (_intZipMsgCount == _intMsgPerZip) { // Create new ZIP entry
                            _intZipEntry++;
                            _intZipMsgCount = 0;
                            _zippedStream.putNextEntry(new ZipEntry(_intZipEntry + ".jmss"));
                        }
                        _intZipMsgCount++;
                    }
                    _zippedStream.write(strOut.getBytes(_strStringEncoding));
                    _zippedStream.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    _locMsgWrite.unlock();
                }
            }
            else { // Not a ZIP file
                try {
                    _locMsgWrite.lock();
                    _outFileStream.write(strOut.getBytes(_strStringEncoding));
                    _outFileStream.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    _locMsgWrite.unlock();
                }
            }
        }
    }

    private String mapMessageToXml(int intTabSpace, MapMessage msg, String strName)
    {
        String strXmlMap;
        if (strName == null || strName.equals(""))
            strXmlMap = appendString(intTabSpace, "<MapMessage>");
        else
            strXmlMap = appendString(intTabSpace, "<MapMessage name=\"" + strName + "\">");

        try {
            Enumeration enuMap =  msg.getMapNames();
            while (enuMap.hasMoreElements()) {
                String name = enuMap.nextElement().toString();
                Object objElement = msg.getObject(name);
                String strClassName = objElement.getClass().getName().replaceFirst("java.lang.", "");
                if (strClassName.equals("com.tibco.tibjms.TibjmsMapMessage")) {
                    strXmlMap += mapMessageToXml(intTabSpace + 1, (MapMessage)objElement, FormatHelper.translateXML(name));
                }
                else {
                    strXmlMap += appendString(intTabSpace + 1, "<node name=\"" + FormatHelper.translateXML(name) + "\" type=\"" + strClassName + "\">" + FormatHelper.translateXML(objElement.toString()) + "</node>");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        strXmlMap += appendString(intTabSpace, "</MapMessage>");

        return strXmlMap;
    }

    public void writeCsv(String strOut)
    {
        if (strOut.length() > 200) {
            System.out.println("Line corruption.");
        }
        if (_outCsvStream != null) {
            try {
                strOut += "\n";
                _locCsvWrite.lock();
                _outCsvStream.write(strOut.getBytes());
                _outCsvStream.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                _locCsvWrite.unlock();
            }
        }
    }

//    public static synchronized void writeCsv(String strOut, FileOutputStream fosStream)
//    {
//        try {
//            strOut += "\n";
//            fosStream.write(strOut.getBytes());
//            fosStream.flush();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void setOriginationTimestamp(Date dateTime)
    {
        _originationTimestamp = dateTime;
    }
}