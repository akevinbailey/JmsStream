/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util;

import com.tibco.util.jmshelper.ConnectionHelper;
import com.tibco.util.jmshelper.License;

import javax.naming.Context;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import javax.transaction.Status;
//import javax.jms.JMSSecurityException;

/**
 * Title:        JmsStream<p>
 * Description:  This is the main class for the JmsStream command line application.<p>
 * @author A. Kevin Bailey
 * @version 2.7.9.1
 */
@SuppressWarnings({"FieldCanBeLocal", "UnnecessaryBoxing", "unchecked", "UnusedDeclaration", "ProhibitedExceptionThrown", "NestedTryStatement", "unused"})
public final class JmsStream implements Runnable
{
    public final static String APP_NAME = "JmsStream";
    public final static String APP_VERSION = "2.7.9.1";
    public final static String APP_DATE = "2015-03-17";

    public final static String APP_COMPANY = "TIBCO Software Inc.";
    public final static String APP_AUTHOR = "A. Kevin Bailey";
    public final static String APP_AUTHOR_EMAIL = "abailey@tibco.com";

    public final static String DEFAULT_OJB_STORE = "./DefaultStore";

    private Hashtable _env = null; // Holds settings, etc.
    private int _intNumOfArgs = 0;

    private JmsStreamListener _runListener =  null;    // -- test public --
    private JmsStreamPublisher _runPublisher = null;
    private Thread _thrListener = null;
    private Thread _thrPublisher = null;

    private ConnectionHelper _conHelper;

    public static void main(String args[])
    {
        new JmsStream(args);
    }

    /**
     *
     * @param env  The env hashtable.
     * @param run  True if running as a thread.
     */
    public JmsStream(Hashtable env, boolean run)
    {
        if (run) _intNumOfArgs = 1;
        else _intNumOfArgs = env.size();

        // Initialize default properties
        initProperties();
        // Copy all of the properties from env to _env
        _env.putAll(env);
        // Set the connection helper
        try {_conHelper = new ConnectionHelper(_env);} catch (Exception exc) {exc.printStackTrace();}
    }

    private JmsStream(String args [])
    {
        _intNumOfArgs = args.length;

        // Initialize default properties
        initProperties();
        // Read command line arguments
        parseArgs(args);
        // Set the connection helper
        try {_conHelper = new ConnectionHelper(_env);} catch (Exception exc) {exc.printStackTrace();}
        // Execute application with properties
        run();
    }

    private void initProperties()
    {
        _env = new Hashtable();

        // set defaults
        _env.put(Context.AUTHORITATIVE, "");
        _env.put(Context.BATCHSIZE, "");
        _env.put(Context.SECURITY_AUTHENTICATION, "");

        _env.put("connectionfactory", "");
        _env.put("requestreply", Boolean.FALSE);
        _env.put("timed", Boolean.FALSE);
        _env.put("speed", new Float(1));
        _env.put("isListener", Boolean.FALSE);
        _env.put("unsubscribe", Boolean.FALSE);
        _env.put("noconfirm", Boolean.FALSE);
        _env.put("browse", Boolean.FALSE);
        _env.put("zip", Boolean.FALSE);
        //_env.put("xmlreaderclass", null);  //"org.apache.crimson.parser.XMLReaderImpl" for crimson parser
        _env.put("stats", new Integer(0));
        _env.put("raw", Boolean.FALSE);
        _env.put("echoxml", Boolean.FALSE);
        _env.put("echocsv", Boolean.FALSE);
        _env.put("verbose", Boolean.FALSE);
        _env.put("fileappend", Boolean.FALSE);
        _env.put("filetype", "text");
        _env.put("fileloop", new Integer(1));
        _env.put("asyncreply", Boolean.FALSE);
        _env.put("usetibcolib", Boolean.TRUE);
        //_env.put("commitonexit", Boolean.FALSE);
    }

    public void stopThread()
    {
        if (_runPublisher != null && _thrPublisher != null) {
            _runPublisher.stopPublish();
            _runPublisher.stopReplyThread(); // In case it is a Request/Reply

            // Must interrupt the thread before the closeConnection so the thread can TransactionManager.suspend()
            _thrPublisher.interrupt();
            if (_thrPublisher.isAlive()) {
                try {
                    _thrPublisher.join();
                }
                catch (InterruptedException ie) {
                    System.err.println(" JmsStream thread interrupted.");
                }
            }
            _runPublisher.closeConnections();
            _runPublisher = null;
        }
        if (_runListener != null && _thrListener != null) {
            _runListener.stopListening();

            // Must interrupt the thread before the closeConnection so the thread can TransactionManager.suspend()
            _thrListener.interrupt();
            if (_thrListener.isAlive()) {
                try {
                    _thrListener.join(3000); // If it takes more than 3sec then move on.
                }
                catch (InterruptedException ie) {
                    System.err.println(" JmsStream thread interrupted.");
                }
            }
            _runListener.closeConnections();
            _runListener = null;
        }
    }

    /**
     * The parseArgs method parses the command line arguments and set the
     * application variables.
     *
     * @param args  The command line arguments.
     */
    public void parseArgs(String args[])
    {
        Vector entries = new Vector();
        Vector ssl_trusted = new Vector();

        if (args.length == 0 || args[0].charAt(0) != '-') {
            System.err.println();
            System.err.println(" -------------------------------------------");
            System.err.println(" " + APP_NAME + " " + APP_VERSION);
            System.err.println();
            System.err.println(" No arguments specified (use -? for options)");
            System.err.println(" -------------------------------------------");
            System.err.println();
            System.exit(1);
        }

        try {
            // Initialize ConnectionHelper
            _conHelper = new ConnectionHelper();

            // read arguments
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-configfile") && args[i+1].charAt(0) != '-') {
                    _env.put("configfile", args[i+1]);
                    break;
                }
                else if (args[i].equalsIgnoreCase("-jmsclient") && args[i+1].charAt(0) != '-') {
                    _env.put("jmsclient", args[i+1]);
                    _conHelper.setJmsClientType(args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-jndicontextfactory") && args[i+1].charAt(0) != '-') {
                    _env.put(Context.INITIAL_CONTEXT_FACTORY, args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-connectionfactory") && args[i+1].charAt(0) != '-') {
                    _env.put("connectionfactory", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-providerurl") && args[i+1].charAt(0) != '-') {
                    _env.put(Context.PROVIDER_URL, args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-user") && args[i+1].charAt(0) != '-') {
                    _env.put("user", args[i+1]);
                    _env.put(Context.SECURITY_PRINCIPAL, args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-password") && args[i+1].charAt(0) != '-') {
                    _env.put("password", args[i+1]);
                    _env.put(Context.SECURITY_CREDENTIALS, args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-jndiuser") && args[i+1].charAt(0) != '-') {
                    _env.put(Context.SECURITY_PRINCIPAL, args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-jndipassword") && args[i+1].charAt(0) != '-') {
                    _env.put(Context.SECURITY_CREDENTIALS, args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-jmsuser") && args[i+1].charAt(0) != '-') {
                    _env.put("user", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-jmspassword") && args[i+1].charAt(0) != '-') {
                    _env.put("password", args[i+1]);
                    i++;
                }
                // Transaction settings
                else if (args[i].equalsIgnoreCase("-trans") && args[i+1].charAt(0) != '-') {
                    if (args[i+1].equalsIgnoreCase("jms")) _env.put("trans", "jms");
                    else if (args[i+1].equalsIgnoreCase("xa")) _env.put("trans", "xa");
                    else {
                        System.err.println("ERROR: -trans can only be \"jms\", or \"xa\".");
                        System.exit(1);
                    }
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-transmsgnum") && args[i+1].charAt(0) != '-') {
                    _env.put("transmsgnum", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-commitonexit")) {
                    _env.put("commitonexit", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-transmgrtype") && args[i+1].charAt(0) != '-') {
                    if (args[i+1].equalsIgnoreCase("local")) _env.put("transmgrtype", "local");
                    else if (args[i+1].equalsIgnoreCase("nomgr")) _env.put("transmgrtype", "nomgr");
                    else if (args[i+1].equalsIgnoreCase("remote")) _env.put("transmgrtype", "remote");
                    else {
                        System.err.println("ERROR: -transmgrtype can only be \"local\", \"nomgr\", or \"remote\".");
                        System.exit(1);
                    }
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-transjndiname") && args[i+1].charAt(0) != '-') {
                    _env.put("transjndiname", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-transtimeout") && args[i+1].charAt(0) != '-') {
                    _env.put("transtimeout", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-jndiprop") && args[i+1].charAt(0) != '-') {
                    int intIndex =  args[i+1].indexOf('=');
                    String strProp = (args[i+1].substring(0, intIndex)).trim();
                    String strValue = (args[i+1].substring(intIndex + 1)).trim();
                    _env.put(strProp, strValue);
                    i++;
                    // TODO:  Test!
                }

                // SSL settings
                else if (args[i].equalsIgnoreCase("-ssl") || args[i].equalsIgnoreCase("-ssl_jndi")) {
                    _env.put("ssl", Boolean.TRUE);
                    // Install our own random number generator which is fast but not secure!
                    // com.tibco.tibjms.TibjmsSSL.setSecureRandom(com.tibco.util.jmshelper.ConnectionHelper.createUnsecureRandom());
                    // Set the initial SSL config
                    if (!_env.containsKey(_conHelper.getSslTrace())) {
                        _env.put(_conHelper.getSslTrace(), Boolean.FALSE);
                        _env.put(_conHelper.getSslTraceNaming(), Boolean.FALSE);
                    }
                    if (!_env.containsKey(_conHelper.getSslDebugTrace())) {
                        _env.put(_conHelper.getSslDebugTrace(), Boolean.FALSE);
                        _env.put(_conHelper.getSslDebugTraceNaming(), Boolean.FALSE);
                    }
                    if (!_env.containsKey(_conHelper.getSslEnableVerifyHost())) {
                        _env.put(_conHelper.getSslEnableVerifyHost(), Boolean.FALSE);
                        _env.put(_conHelper.getSslEnableVerifyHostNaming(), Boolean.FALSE);
                    }
                    if (!_env.containsKey(_conHelper.getSslEnableVerifyHostName())) {
                        _env.put(_conHelper.getSslEnableVerifyHostName(), Boolean.FALSE);
                        _env.put(_conHelper.getSslEnableVerifyHostNameNaming(), Boolean.FALSE);
                    }
                    if (!_env.containsKey(_conHelper.getSslAuthOnly())) {
                        _env.put(_conHelper.getSslAuthOnly(), Boolean.FALSE);
                        _env.put(_conHelper.getSslAuthOnlyNaming(), Boolean.FALSE);
                    }
                    if (!_env.containsKey(_conHelper.getSslVendor())) {
                        _env.put(_conHelper.getSslVendor(), _conHelper.getSslVendorDefault());
                        _env.put(_conHelper.getSslVendorNaming(), _conHelper.getSslVendorDefault());
                    }
                    if (args[i].equalsIgnoreCase("-ssl_jndi")) {
                        // Specify SSL as the security protocol to use by the Initial Context
                        _env.put(_conHelper.getSecurityProtocol(), "ssl");
                    }
                }
                else if (args[i].equalsIgnoreCase("-ssl_auth_only")) {
                    _env.put(_conHelper.getSslAuthOnly(), Boolean.TRUE);
                    //_env.put(_conHelper.getSslAuthOnlyNaming(), Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-ssl_vendor") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getSslVendor(), args[i+1]);
                    _env.put(_conHelper.getSslVendorNaming(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_ciphers") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getSslCipherSuites(), args[i+1]);
                    _env.put(_conHelper.getSslCipherSuitesNaming(), args[i+1]);
                    i++;
                }
                // Set trace for client-side operations, loading of certificates and other
                else if (args[i].equalsIgnoreCase("-ssl_trace")) {
                    _env.put(_conHelper.getSslTrace(), Boolean.TRUE);
                    _env.put(_conHelper.getSslTraceNaming(), Boolean.TRUE);
                    //com.tibco.tibjms.TibjmsSSL.setClientTracer(System.out);
                }
                // Set vendor trace. Has no effect for "j2se", "entrust61" uses
                // This to trace SSL handshake
                else if (args[i].equalsIgnoreCase("-ssl_debug_trace")) {
                    _env.put(_conHelper.getSslDebugTrace(), Boolean.TRUE);
                    _env.put(_conHelper.getSslDebugTraceNaming(), Boolean.TRUE);
                }
                // Set trusted certificates if specified
                else if (args[i].equalsIgnoreCase("-ssl_trusted")) {
                    ssl_trusted.clear();
                    while (i+1 < args.length && !args[i+1].startsWith(("-"))) {
                        i++;
                        //TibjmsSSL.addTrustedCerts(args[i]);
                        ssl_trusted.add(args[i]);
                    }
                    _env.put(_conHelper.getSslTrustedCertificates(), ssl_trusted);
                    _env.put(_conHelper.getSslTrustedCertificatesNaming(), ssl_trusted);
                }
                // Set trusted certificates if specified
                else if (args[i].equalsIgnoreCase("-ssl_hostname") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getSslExpectedHostName(), args[i+1]);
                    _env.put(_conHelper.getSslExpectedHostNameNaming(), args[i+1]);
                    i++;
                }
                // Set client identity if specified. ssl_key may be null
                // if identity is PKCS12, JKS or EPF. 'j2se' only supports
                // PKCS12 and JKS. 'entrust61' also supports PEM and PKCS8.
                else if (args[i].equalsIgnoreCase("-ssl_identity") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getSslIdentity(), args[i+1]);
                    _env.put(_conHelper.getSslIdentityNaming(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_password") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getSslPassword(), args[i+1]);
                    _env.put(_conHelper.getSslPasswordNaming(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_key") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getSslPrivateKey(), args[i+1]);
                    _env.put(_conHelper.getSslPrivateKeyNaming(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-verify_host_name")) {
                    _env.put(_conHelper.getSslEnableVerifyHostName(), Boolean.TRUE);
                    _env.put(_conHelper.getSslEnableVerifyHostNameNaming(), Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-verify_host")) {
                    _env.put(_conHelper.getSslEnableVerifyHost(), Boolean.TRUE);
                    _env.put(_conHelper.getSslEnableVerifyHostNaming(), Boolean.TRUE);
                }
                // Java based SSL Params
                else if (args[i].equalsIgnoreCase("-ssl_keystore_type") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getKeyStoreType(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_keystore") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getKeyStore(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_keystore_pwd") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getKeyStorePassword(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_truststore_type") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getTrustStoreType(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_truststore") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getTrustStore(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_truststore_pwd") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getTrustStorePassword(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ssl_debug") && args[i+1].charAt(0) != '-') {
                    _env.put(_conHelper.getNetDebug(), args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-listen") && args[i+1].charAt(0) != '-') {
                    if (!_env.get("requestreply").equals(Boolean.TRUE)) _env.put("isListener", Boolean.TRUE);
                    _env.put("listendest", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-send") && args[i+1].charAt(0) == '-') {
                    _env.put("isListener", Boolean.FALSE);
                }
                else if (args[i].equalsIgnoreCase("-send") && args[i+1].charAt(0) != '-') {
                    _env.put("isListener", Boolean.FALSE);
                    _env.put("senddest", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-requestreply") && args[i+1].charAt(0) == '-') {
                    _env.put("isListener", Boolean.FALSE);
                    _env.put("requestreply", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-replytimeout") && args[i+1].charAt(0) != '-') {
                    _env.put("replytimeout", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-asyncreply")) {
                    _env.put("asyncreply", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-queue")) {
                    if (_env.get("connectionfactory").toString().equals(""))
                        _env.put("connectionfactory", _conHelper.getDefaultQueueFactory());
                    _env.put("type", "queue");
                }
                else if (args[i].equalsIgnoreCase("-topic")) {
                    if (_env.get("connectionfactory").toString().equals(""))
                        _env.put("connectionfactory", _conHelper.getDefaultTopicFactory());
                    _env.put("type", "topic");
                }
                else if (args[i].equalsIgnoreCase("-generic")) {
                    if (_env.get("connectionfactory").toString().equals(""))
                        _env.put("connectionfactory", _conHelper.getDefaultGenericFactory());
                    _env.put("type", "generic");
                }
                else if (args[i].equalsIgnoreCase("-durable") && args[i+1].charAt(0) != '-') {
                    _env.put("durablename", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-unsubscribe")) {
                    if (_env.get("durablename") == null) {
                        System.err.println("The -unsubscribe argument must be proceeded by the -durable argument.");
                        System.exit(1);
                    }
                    else
                        _env.put("unsubscribe", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-file") && args[i+1].charAt(0) != '-') {
                    _env.put("file", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-filetype") && args[i+1].charAt(0) != '-') {
                    if (args[i+1].equalsIgnoreCase("text")) {
                        _env.put("filetype", "text");
                    }
                    else if (args[i+1].equalsIgnoreCase("xml")) {
                        System.err.println("WARNING:  XML file type has been depricted.");
                        _env.put("filetype", "xml");
                    }
                    else {
                        System.err.println("ERROR: -filetype can only be \"xml\" or \"text\".");
                        System.exit(1);
                    }
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-deliverymode")  && args[i+1].charAt(0) != '-') {
                    if (args[i+1].equalsIgnoreCase("NON_PERSISTENT")) _env.put("deliverymode", "NON_PERSISTENT");
                    else if (args[i+1].equalsIgnoreCase("PERSISTENT")) _env.put("deliverymode", "PERSISTENT");
                    else if (args[i+1].equalsIgnoreCase("RELIABLE_DELIVERY")) _env.put("deliverymode", "RELIABLE_DELIVERY");
                    else {
                        System.err.println("ERROR: -deliverymode can only be \"NON_PERSISTENT\", \"PERSISTENT\", or \"RELIABLE_DELIVERY\".");
                        System.exit(1);
                    }
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ackmode")  && args[i+1].charAt(0) != '-') {
                    if (args[i+1].equalsIgnoreCase("AUTO_ACKNOWLEDGE")) _env.put("ackmode", "AUTO_ACKNOWLEDGE");
                    else if (args[i+1].equalsIgnoreCase("CLIENT_ACKNOWLEDGE")) _env.put("ackmode", "CLIENT_ACKNOWLEDGE");
                    else if (args[i+1].equalsIgnoreCase("DUPS_OK_ACKNOWLEDGE")) _env.put("ackmode", "DUPS_OK_ACKNOWLEDGE");
                    else if (args[i+1].equalsIgnoreCase("TIBCO_ACKNOWLEDGE")) _env.put("ackmode", "EXPLICIT_CLIENT_ACKNOWLEDGE");
                    else if (args[i+1].equalsIgnoreCase("EXPLICIT_CLIENT_ACKNOWLEDGE")) _env.put("ackmode", "EXPLICIT_CLIENT_ACKNOWLEDGE");
                    else if (args[i+1].equalsIgnoreCase("INDIVIDUAL_ACKNOWLEDGE")) _env.put("ackmode", "INDIVIDUAL_ACKNOWLEDGE");
                    else if (args[i+1].equalsIgnoreCase("NO_ACKNOWLEDGE")) _env.put("ackmode", "NO_ACKNOWLEDGE");
                    else {
                        System.err.println("ERROR: -ackmode can only be \"AUTO_ACKNOWLEDGE\", \"CLIENT_ACKNOWLEDGE\"," +
                                            " \"DUPS_OK_ACKNOWLEDGE\", \"TIBCO_ACKNOWLEDGE\", \"EXPLICIT_CLIENT_ACKNOWLEDGE\"," +
                                            " \"INDIVIDUAL_ACKNOWLEDGE\", or \"NO_ACKNOWLEDGE\".");
                        System.exit(1);
                    }
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-noconfirm")) {
                    _env.put("noconfirm", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-replyfile") && args[i+1].charAt(0) != '-') {
                    _env.put("replyfile", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-zip")) {
                    entries.clear();
                    while ( i+1 < args.length && !args[i+1].startsWith(("-"))) {
                        i++;
                        entries.add(args[i]);
                    }
                    _env.put("zip", Boolean.TRUE);
                    _env.put("zipentries", entries);
                }
                else if (args[i].equalsIgnoreCase("-zipmsgperentry") && args[i+1].charAt(0) != '-') {
                    _env.put("zip", Boolean.TRUE);
                    _env.put("zipmsgperentry", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-echoxml")) {
                    _env.put("echoxml", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-timed")) {
                    _env.put("timed", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-speed")) {
                    _env.put("timed", Boolean.TRUE);
                    _env.put("speed", new Float(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-rate") && args[i+1].charAt(0) != '-') {
                    _env.put("rate", new Float(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-startrate") && args[i+1].charAt(0) != '-') {
                    _env.put("rate", new Float(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-maxrate") && args[i+1].charAt(0) != '-') {
                    _env.put("maxrate", new Float(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-numberofintervals") && args[i+1].charAt(0) != '-') {
                    _env.put("numberofintervals", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-intervalsize") && args[i+1].charAt(0) != '-') {
                    _env.put("intervalsize", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-ratestamp") && args[i+1].charAt(0) != '-') {
                    _env.put("ratestamp", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-sndtimestamp") && args[i+1].charAt(0) != '-') {
                    _env.put("sndtimestamp", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-rcvtimestamp") && args[i+1].charAt(0) != '-') {
                    _env.put("rcvtimestamp", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-sequence") && args[i+1].charAt(0) != '-') {
                    _env.put("sequence", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-browse")) {
                    _env.put("browse", Boolean.TRUE);
                }
                //else if (args[i].equalsIgnoreCase("-xmlreaderclass") && args[i+1].charAt(0) != '-') {
                //    _env.put("xmlreaderclass", args[i+1]);
                //    i++;
                //}
                else if (args[i].equalsIgnoreCase("-encoding") && args[i+1].charAt(0) != '-') {
                    _env.put("encoding", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-selector") && args[i+1].charAt(0) != '-') {
                    _env.put("selector", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-clientid") && args[i+1].charAt(0) != '-') {
                    _env.put("clientid", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-compress")) {
                    _env.put("compress", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-version")) {
                    System.out.println(APP_NAME);
                    System.out.println("Version:    " + APP_VERSION);
                    System.out.println("Build Date: " + APP_DATE);
                    System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-stats") && args[i+1].charAt(0) != '-') {
                    if (!_env.containsKey("verbose")) _env.put("verbose", Boolean.FALSE);
                    _env.put("noecho", Boolean.TRUE);
                    _env.put("stats", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-showconfig")) {
                    _env.put("showconfig", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-getbodylength") && args[i+1].charAt(0) != '-') {
                    _env.put("getbodylength", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-author")) {
                    System.out.println("Company: " + APP_COMPANY);
                    System.out.println("Author:  " + APP_AUTHOR);
                    System.out.println("Email:   " + APP_AUTHOR_EMAIL);
                    System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-verbose")) {
                    _env.put("verbose", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-noecho")) {
                    _env.put("verbose", Boolean.FALSE);
                    _env.put("echoxml", Boolean.FALSE);
                    _env.put("echocsv", Boolean.FALSE);
                    _env.put("noecho", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-raw")) {
                    _env.put("raw", Boolean.TRUE);
                    _env.put("verbose", Boolean.FALSE);
                    _env.put("echoxml", Boolean.FALSE);
                    _env.put("echocsv", Boolean.FALSE);
                }
                else if (args[i].equalsIgnoreCase("-echocsv")) {
                    _env.put("echocsv", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-fileappend")) {
                    _env.put("fileappend", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-fileloop") && args[i+1].charAt(0) != '-') {
                    _env.put("fileloop", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-csvfile") && args[i+1].charAt(0) != '-') {
                    _env.put("csvfile", args[i+1]);
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-extractmonmsg")) {
                    _env.put("extractmonmsg", Boolean.TRUE);
                }
                else if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-?")) {
                    usage(); System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-notes")) {
                    notes(); System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-license")) {
                    license(); System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-stopafter") && args[i+1].charAt(0) != '-') {
                    _env.put("stopafter", new Integer(args[i+1]));
                    i++;
                }
                else if (args[i].charAt(0) == '-') {  // used to prevent misspelling and errors
                    System.err.println(" " + APP_NAME + " " + APP_VERSION);
                    System.err.println();
                    System.err.println(" ERROR: Unexpected argument near: " + args[i]);
                    System.err.println(" (use -? for options for the appropriate arguments)");
                    System.err.println();
                    System.exit(1);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Get the BodyLength and nothing else
        if (_env.containsKey("getbodylength")) {
            if (_env.containsKey("encoding"))
                System.out.println("BodyLength=" + getBodyLength(_env.get("getbodylength").toString(), _env.get("encoding").toString()));
            else
                System.out.println("BodyLength=" + getBodyLength(_env.get("getbodylength").toString(), "UTF-8"));
            System.exit(0);
        }

        // Set default Jms Client if one is not set.
        if (!_env.containsKey("jmsclient")) {
            _env.put("jmsclient", _conHelper.getJmsClientType());
        }

        // Set default INITIAL_CONTEXT_FACTORY if one is not set.
        if (!_env.containsKey(Context.INITIAL_CONTEXT_FACTORY)) {
            _env.put(Context.INITIAL_CONTEXT_FACTORY, _conHelper.getInitialContextFactory());
        }
        // Set default PROVIDER_URL if one is not set.
        if (!_env.containsKey(Context.PROVIDER_URL)) {
            _env.put(Context.PROVIDER_URL, _conHelper.getProviderUrl());
        }
        // Set default URL_PKG_PREFIXES if one is not set.
        if (!_env.containsKey(Context.URL_PKG_PREFIXES)) {
            _env.put(Context.URL_PKG_PREFIXES, _conHelper.getUrlPkgPrefixes());
        }

        if (_env.containsKey("configfile")) {
            try {
                _env.putAll(com.tibco.util.jmshelper.FormatHelper.getPropertiesFile(_env.get("configfile").toString()));
            }
            catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        if (_env.get("isListener").equals(Boolean.FALSE) && _env.get("requestreply").equals(Boolean.FALSE) && _env.containsKey("csvfile")) {
            System.err.println("ERROR: Cannot use -csvfile in send mode.");
            System.exit(1);
        }
        if (_env.get("isListener").equals(Boolean.FALSE) && _env.get("requestreply").equals(Boolean.FALSE) && (_env.get("echocsv").equals(Boolean.TRUE) || _env.get("echoxml").equals(Boolean.TRUE))) {
            System.err.println("ERROR: Cannot use -echocsv or -echoxml options in send mode.");
            System.exit(1);
        }
        if (_env.get("echocsv").equals(Boolean.TRUE) || _env.get("verbose").equals(Boolean.TRUE)) {  // statement is put outside for loop in case user puts -echo
            _env.put("echoxml", Boolean.FALSE);
        }
        if (_env.get("echocsv").equals(Boolean.TRUE) || _env.get("echoxml").equals(Boolean.TRUE)) {
            _env.put("verbose", Boolean.FALSE);
        }
        if (_env.containsKey("replyfile") && !_env.containsKey("file")) { // if the user mistakenly put -replyfile for -file
            _env.put("file", _env.get("replyfile").toString());
        }
        if ((_env.containsKey("maxrate") && !_env.containsKey("numberofintervals") && !_env.containsKey("intervalsize"))
            || (!_env.containsKey("maxrate") && _env.containsKey("numberofintervals") && !_env.containsKey("intervalsize"))
            || (!_env.containsKey("maxrate") && !_env.containsKey("numberofintervals") && _env.containsKey("intervalsize"))) {
            System.err.println("ERROR: Must contain -maxrate, -numberofintervals and" +
                               "\n        -intervalsize, or none at all.");
            System.exit(1);
        }
        if (_env.containsKey("ratestamp") && !_env.containsKey("maxrate")) {
            System.err.println("ERROR: Must contain -maxrate, -numberofintervals, and" +
                               "\n        -intervalsize for -ratestamp.");
            System.exit(1);
        }
        if (_env.get("raw").equals(Boolean.TRUE) && (_env.containsKey("file") || _env.containsKey("csvfile"))) {
            System.err.println("ERROR: Cannot use -raw with the -file or -csvfile options.");
            System.exit(1);
        }
        if (_env.containsKey("zipmsgperentry") && _env.containsKey("zipentries")) {
            System.err.println("ERROR: Cannot use ZIP entries with the -zipmsgperentry.");
            System.exit(1);
        }
        if (_env.get("fileappend").equals(Boolean.TRUE) && _env.get("zip").equals(Boolean.TRUE)) {
            System.err.println("ERROR: Cannot use -fileappend with ZIP files.");
            System.exit(1);
        }

        if (_env.containsKey("ssl") && _env.get("connectionfactory").toString().equals(_conHelper.getDefaultQueueFactory())) {
                _env.put("connectionfactory", _conHelper.getDefaultSslQueueFactory());
        }
        if (_env.containsKey("ssl") && _env.get("connectionfactory").toString().equals(_conHelper.getDefaultTopicFactory())) {
                _env.put("connectionfactory", _conHelper.getDefaultSslTopicFactory());
        }
        if (_env.containsKey("ssl") && _env.get("connectionfactory").toString().equals(_conHelper.getDefaultGenericFactory())) {
                _env.put("connectionfactory", _conHelper.getDefaultSslGenericFactory());
        }
    }

    /**
     * The usage method prints the application help to the screen.
     */
    public static void usage()
    {
        System.out.println(" Usage of JmsStream:  (The <> brackets mean a mandatory entry.  The [] brackets mean an optional entry.)");

        System.out.println();
        System.out.println(" java -jar JmsStream.jar -configfile <file-name>");
        System.out.println(" or");
        System.out.println(" java -jar JmsStream.jar -<send | listen | requestreply> [destination] -<[queue | topic | generic] | connectionfactory> [options]");

        System.out.println();
        System.out.println(" ---- Configuration File ----");
        System.out.println("   -configfile <file-name>    : Read the configuration from the JmsStream configuration file.");
        System.out.println("                                (No other args are necessary.)");
        System.out.println();
        System.out.println(" ---- Connection Options ----");
        System.out.println("   -jmsclient < TIBCO_EMS     : (optional) Sets the JMS client libraries to a specific vendor's");
        System.out.println("              | APACHE_AMQ >     product. (when the arg is absent the default is TIBCO_EMS)");
        System.out.println("              | HORNETQ >");
        System.out.println("   -send [destination]        : Send messages to queue or topic. The [destination] is optional. If");
        System.out.println("                                [destination] is not given the send destination is read from the");
        System.out.println("                                messages in the file. (must supply the -file <file-name> argument)");
        System.out.println("   -listen <destination>      : Listens for messages from a queue or topic <destination>.");
        System.out.println("   -requestreply              : Send messages to topic or queue and listen for reply. The sending");
        System.out.println("                                destination is taken form the -send arg and the listening destination is");
        System.out.println("                                taken from the -listen arg.  If either of these arguments are not present");
        System.out.println("                                then the sending and/or listening destinations are taken form the Message");
        System.out.println("                                file. (Must supply the -file <file-name> argument with -requestreply)");
        System.out.println("   -queue                     : Force the destination to be a queue regardless of connection factory.");
        System.out.println("   -topic                     : Force the destination to be a topic regardless of connection factory.");
        System.out.println("   -generic                   : Force the destination to be generic regardless of connection factory.");
        System.out.println("   -connectionfactory <name>  : Use a connection factory (default: QueueConnectionFactory for queues,");
        System.out.println("                                TopicConnectionFactory for topic, and GenericConnectionFactory");
        System.out.println("                                for generic destinations)");
        System.out.println("   -replytimeout <ms>         : The reply timeout value in <ms> milliseconds.");
        System.out.println("   -asyncreply                : The request will publish all messages in the file and set");
        System.out.println("                                up a listener for each reply. (default is to block");
        System.out.println("                                the request thread until the reply is received)");
        System.out.println("   -jndicontextfactory <name> : Initial context factory for the JNDI.");
        System.out.println("                                (default com.tibco.tibjms.naming.TibjmsInitialContextFactory)");
        System.out.println("   -providerurl <name>        : Context URL for JNDI lookup.");
        System.out.println("                                (default: tibjmsnaming://localhost:7222)");
        System.out.println("   -user <name>               : User name used for both JNDI and JMS.");
        System.out.println("   -password <string>         : Password used for both JNDI and JMS.");
        System.out.println("   -jndiuser <name>           : JNDI security principal.");
        System.out.println("   -jndipassword <string>     : JNDI security credentials.");
        System.out.println("   -jmsuser <name>            : JMS user name.");
        System.out.println("   -jmspassword <string>      : JMS user password.");
        System.out.println("   -clientid <name>           : JMS client ID.");
        System.out.println("   -jndiprop <propname>=<value> : a JNDI custom property, where <propname> is the JNDI property,");
        System.out.println("                                  <value> is the string representation of the property value.");
        System.out.println("                                  (this argument can be repeated for entering of multiple custom");
        System.out.println("                                  JNDI properties");

        // set client identity if specified. ssl_key may be null if identity is PKCS12, JKS or EPF.
        // 'j2se' only supports PKCS12 and JKS. 'entrust61' also supports PEM and PKCS8.
        System.out.println();
        System.out.println(" ---- SSL Options ----");
        System.out.println("   -ssl         : Start SSL transport. (required for all SSL communication)");
        System.out.println("   OpenSSL Options (TIBCO_EMS):");
        System.out.println("      -ssl_jndi                 : Use SSL for JNDI lookup.");
        System.out.println("      -ssl_auth_only            : Use SSL encryption for authentication only.");
        System.out.println("      -ssl_vendor <name>        : SSL vendor: 'j2se-default', 'j2se', 'entrust61', or 'ibm'.");
        System.out.println("      -ssl_ciphers <name>       : OpenSSL names for the cipher suites used for encryption.");
        System.out.println("      -ssl_trace                : Trace SSL initialization.");
        System.out.println("      -ssl_debug_trace          : Trace SSL handshake and related.");
        System.out.println("      -ssl_trusted <file1 ...>  : File(s) with trusted certificate(s).");
        System.out.println("      -ssl_hostname <host-name> : Name expected in the server certificate.");
        System.out.println("      -ssl_identity <file-name> : Client identity file.");
        System.out.println("      -ssl_password <string>    : Password for the client identity file.");
        System.out.println("      -ssl_key <file-name>      : Client key file or private key file.");
        System.out.println("                                : (only valid for 'entrust' and 'ibm')");
        System.out.println("      -verify_host_name         : Host name verification.");
        System.out.println("      -verify_host              : Host verification.");
        System.out.println("   Netty SSL Options (APACHE_AMQ, HORNETQ):");
        System.out.println("      -ssl_keystore_type <JKS | PKCS12>   : Client keystore type.");
        System.out.println("      -ssl_keystore <file-name>           : FQN of the client keystore file.");
        System.out.println("      -ssl_keystore_pwd <string>          : Password for the client keystore file.");
        System.out.println("      -ssl_truststore_type <JKS | PKCS12> : Trust keystore type.");
        System.out.println("      -ssl_truststore <file-name>         : FQN of the trust keystore file.");
        System.out.println("      -ssl_truststore_pwd <string>        : Password for the trust keystore file.");
        System.out.println("      -ssl_debug <string>                 : A Java SSL debug string.");
        System.out.println();
        System.out.println(" ---- Input/Output Options ----");
        System.out.println("   -file <file-name>      : Read/write captured messages from/to JmsStream file <file-name> for replay.");
        System.out.println("   -zip [entry1 ...]      : Read/write JmsStream file (entry) from/to a ZIP compressed file.");
        System.out.println("                            (default: read all entries in zip)");
        System.out.println("   -filetype <text | xml> : JmsStream text or XML file to read from / write to for replay.");
        System.out.println("                            (xml file is deprecated. Only used for backwards compatibility)");
        System.out.println("                            (object type messages cannot be re-published) (default is text)");
        System.out.println("   -fileappend            : Append output to -file <file-name>. (default is to create a new file)");
        System.out.println("   -replyfile <file-name> : Write capture reply messages to <file-name> for -requestreply.");
        System.out.println("                            (requestreply mode only)");
        System.out.println("   -stats <sec>            : Will collect and print statistical information every");
        System.out.println("                            <sec> seconds.");
        System.out.println("   -verbose               : Print detailed message text type body, binary body, map message, or an");
        System.out.println("                            object.toString() to stdout when sent/received.");
        System.out.println("                            (not setting -verbose will print only basic message info to screen)");
        System.out.println("   -noecho                : Do not print messages to the screen.");
        System.out.println("   -showconfig            : Displays configuration and exits.");

        System.out.println();
        System.out.println(" ---- Listener Options ----");
        System.out.println("   -ackmode < AUTO_ACKNOWLEDGE            : default acknowledge mode");
        System.out.println("            | CLIENT_ACKNOWLEDGE          : standard behavior");
        System.out.println("            | DUPS_OK_ACKNOWLEDGE         : standard behavior");
        System.out.println("            | EXPLICIT_CLIENT_ACKNOWLEDGE : com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE");
        System.out.println("            | INDIVIDUAL_ACKNOWLEDGE      : org.apache.activemq.ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE");
        System.out.println("            | NO_ACKNOWLEDGE >            : com.tibco.tibjms.Tibjms.NO_ACKNOWLEDGE");
        System.out.println("   -noconfirm               : JmsStream will not send client acknowledgments.");
        System.out.println("   -selector <string>       : Set JMS message selector.");
        System.out.println("   -durable <name>          : Use a durable topic subscriber with name <name>.");
        System.out.println("   -unsubscribe             : Unsubscribe from the durable topic on exit. (requires -durable <name>)");
        System.out.println("   -browse                  : Browse the queue. (will not remove the messages from queue)");
        System.out.println("   -stopafter <number>      : Stop listening and exit after <number> of received messages.");
        System.out.println("   -timed                   : Record timed statistics for real-life replay.");
        System.out.println("   -rcvtimestamp <property> : Add receiving timestamp to <property> message property.");
        System.out.println("   -echoxml                 : Print messages text type body and map message in XML format");
        System.out.println("                              to stdout when received. (listen mode only)");
        System.out.println("   -echocsv                 : Print message header and property fields in CSV format");
        System.out.println("                              to stdout when received. (listen mode only)");
        System.out.println("   -raw                     : Print message.toString() to the screen and do not write to file.");
        System.out.println("                              (listen mode only)");
        System.out.println("   -zipmsgperentry <number> : Maximum messages per ZIP entry. (breaks up large file ");
        System.out.println("                              captures into separate ZIP entries in the ZIP file)");
        System.out.println("                              (automatically puts -zip)");
        System.out.println("   -csvfile <file-name>     : Write captured message headers and properties to CVS file");
        System.out.println("                              used for gathering performance info. (listen mode only)");
        System.out.println("   -extractmonmsg           : Extracts embedded client messages from TIBCO EMS System Monitor");
        System.out.println("                              messages. (EMS and listen mode only)");

        System.out.println();
        System.out.println(" ---- Sender Options ----");
        System.out.println("   -deliverymode < NON_PERSISTENT  : Overrides the delivermode in the message.");
        System.out.println("                 | PERSISTENT");
        System.out.println("                 | RELIABLE_DELIVERY >");
        System.out.println("   -compress                       : Compress the messages when sending. (TIBCO EMS only)");
        System.out.println("   -speed <number>                 : Speed-up or slow-down replay if using timed play back. (default 1)");
        System.out.println("                                     (requires the messages were captured using the -timed arg)");
        System.out.println("   -fileloop <int>                 : Loop over the read file and re-send the messages");
        System.out.println("                                     <int> times. <int> = 0 will continue indefinitely");
        System.out.println("   -sndtimestamp <property>        : Add sending timestamp to message property <property>.");
        System.out.println("   -sequence <property>            : Add sending message sequence number to message");
        System.out.println("                                     property <property>.");
        System.out.println("   -rate <number>                  : Number of messages to send per second.");
        System.out.println("                                     (default 0 = fast as possible)");
        System.out.println("   Following params must be used together:");
        System.out.println("      -startrate <number>         : (optional) The starting message rate to send per second.");
        System.out.println("                                    (if -startrate is not given the message rate starts at");
        System.out.println("                                    maxrate/numberofintervals)");
        System.out.println("      -maxrate <number>           : Maximum messages to send per second.");
        System.out.println("                                    (program will exit after achieving maxrate)");
        System.out.println("      -numberofintervals <number> : Number of message intervals between -startrate and -maxrate.");
        System.out.println("                                    (maxrate/numberofintervals = message rate step size)");
        System.out.println("      -intervalsize <number>      : Number of messages per interval. (overrides -fileloop)");
        System.out.println("      -ratestamp <property>       : (optional) Add current msg/sec rate to message");
        System.out.println("                                    property <property>.");

        System.out.println();
        System.out.println(" ---- Transaction Options ----");
        System.out.println("   -trans <jms | xa>                : 'jms' is a simple JMS transaction session, 'xa' uses");
        System.out.println("                                      a XA Connection Factory.");
        System.out.println("   -commitonexit                    : Commit pending message transaction when program exits.");
        System.out.println("   -transmsgnum <int>               : (optional) The number of messages in a transaction.");
        System.out.println("   Following params are for XA Transactions only:");
        System.out.println("      -transmgrtype <local | nomgr> : (optional) XA Transaction Type. 'local' uses a local");
        System.out.println("                                      JBossTS transaction manager. 'nomgr' use a basic XA");
        System.out.println("                                      without a transaction manager. (default: local)");
        System.out.println("      -transjndiname <name>         : (optional) JTA Transaction Manager name in the JNDI");
        System.out.println("                                       server. (default: TransactionManager)");
        System.out.println("      -transtimeout <int>           : (optional) XA Transaction timeout in seconds.");
        System.out.println("                                      (default: 0)");

        System.out.println();
        System.out.println(" ---- Other JmsStream Options ----");
        System.out.println("   -encoding <name>            : Set message encoding. (US-ASCII, ISO-8859-1, UTF-8, UTF-16BE,");
        System.out.println("                                 UTF-16LE, UTF-16) (default UTF-8)");
        //System.out.println("   -xmlreaderclass <name>      : Use an external XMLReader class.");
        //System.out.println("                                 (default is the standard Java parser)");
        System.out.println("   -getbodylength <file-name>  : Get the length of the message body in the file.");
        System.out.println("                                 (used to determine the BodyLength value when modifying or");
        System.out.println("                                 creating a message body in the send file)");
        System.out.println("                                 (this should be the only argument)");

        System.out.println();
        System.out.println(" ---- Help Option ----");
        System.out.println("   -help -?  : Usage information");
        System.out.println("   -author   : Contact information");
        System.out.println("   -version  : Displays version information");
        System.out.println("   -license  : JmsStream usage license (by using this software you agree to the license)");
        System.out.println("   -notes    : Application comments and examples");
    }

    /**
     * The notes method print the usage notes to the screen.
     */
    public static void notes()
    {
        System.out.println(" Notes for JmsStream: ");

        System.out.println();
        System.out.println(" * Must use Java 1.5 or greater.");
        System.out.println(" * The -filetype xml has been deprecated. Used only for backwards compatibility.");
        System.out.println("     The -filetype text is actually better for capturing XML message types.");
        System.out.println(" * When replaying messages, all of the messages in a save file are loaded into memory before");
        System.out.println("     publication, except when using a ZIP file which loads one ZIP entry into memory at a time.");
        System.out.println(" * This version of JmsStream only supports the included local JBossTS transaction manager");
        System.out.println("     for XA transactions.");
        System.out.println(" * Cannot append to ZIP files because it is not supported in the Java libraries.");
        System.out.println(" * Writing to a ZIP file is much slower than the standard text file.");
        System.out.println(" * For SSL, set client identity if specified. ssl_key may be null if identity is PKCS12,");
        System.out.println("     JKS or EPF.  'j2se' only supports PKCS12 and JKS. 'entrust61' supports PEM and PKCS8.");
        System.out.println(" * Tested only on Windows Vista SP1 and Red Hat Enterprise Linux 5.");
        System.out.println(" * JmsStream captures the following message types: MapMessage, TextMessage, BytesMessage,");
        System.out.println("     and ObjectMessage.");
        System.out.println(" * JmsStream sends the following message types: MapMessage, TextMessage, and BytesMessage");
        System.out.println(" * Supports only one (1) destination for listening per instance; however, wildcards (*, >)");
        System.out.println("     are supported. (request reply doses support multiple reply destinations)");
        System.out.println(" * The JmsStreamGUI will only display ISO8859-1 and UTF-8 characters. UTF-16 will not display");
        System.out.println("     in the JmsStreamGUI.");
        System.out.println(" * JmsStream screen output is limited to the console default character set of the machine.");
        System.out.println("     Message capture and replay are not affected by this limitation.");
        System.out.println(" * Once the SSL trace or SSL debug options are set in JmsStreamGUI and the start button is");
        System.out.println("     pressed, you must stop and restart JmsStreamGUI for changes to SSL trace and SSL debug");
        System.out.println("     to take affect. This is due to the SSL libraries not allowing the SSL trace or SSL debug");
        System.out.println("     to be changed once a connection is initialized.");
        System.out.println();
        System.out.println(" Examples:");
        System.out.println();
        System.out.println(" java -jar JmsStream.jar -topic -listen topic.sample");
        System.out.println("\n java -jar JmsStream.jar -getbodylength \"c:\\msgbody.txt\"");
        System.out.println("\n java -jar JmsStream.jar -queue -listen queue.sample -user admin -password admin -echocsv -verbose -file \"c:\\testjmss.jmss\" -csvfile \"c:\\csvfile.csv\"");
        System.out.println("\n java -jar JmsStream.jar -topic -send -providerurl tibjmsnaming://localhost:7222 -file /export/home/tibco/testjmss.xml -filetype xml");
        System.out.println("\n java -jar JmsStream.jar -topic -send topic.sample -providerurl tibjmsnaming://localhost:7222 -file /export/home/tibco/testjmss.jmss");
        System.out.println("\n java -jar JmsStream.jar -queue -requestreply -providerurl tibjmsnaming://localhost:7222 -user admin -password admin -sndtimestamp SndTimestamp -file \"c:\\testjmss.jmss\" -csvfile \"c:\\csvfile.csv\" -asyncreply -echocsv");
        System.out.println("\n java -jra JmsStream.jar -queue -requestreply -user admin -password admin -file \"c:\\testjmss_xml.jmss\" -replyfile \"c:\\replyjmss.jmss\"");
        System.out.println("\n java -jar JmsStream.jar -topic -send -deliverymode RELIABLE_DELIVERY -sndtimestamp SndTimestamp -ratestamp RateStamp -startrate 50 -maxrate 100 -numberofintervals 500 -intervalsize 5 -file \"c:\\test.jmss\"");
        System.out.println("\n java -jar JmsStream.jar -queue -listen queue.sample -user admin -password admin -noecho -csvfile \"c:\\csvfile.csv\"");
        System.out.println("\n java -jar JmsStream.jar -queue -listen queue.sample -user admin -password admin -file \"c:\\testjmss.zip\" -zip save1.jmss");
        System.out.println("\n java -jar JmsStream.jar -queue -listen queue.sample -user admin -password admin -file \"c:\\testjmss.zip\" -zipmsgperentry 1000");
        System.out.println("\n java -jar JmsStream.jar -topic -send topic.sample -user admin -password admin -file \"c:\\testjmss.zip\" -zip -noecho");
        System.out.println("\n java -jar JmsStream.jar -ssl_jndi -connectionfactory SSLQueueConnectionFactory -providerurl tibjmsnaming://svr-esb.tibco.com:7243 -listen topic.sample -user admin -password admin -clientid JmsStream -raw -ssl_trace");
        System.out.println("\n java -jar JmsStream.jar -providerurl tibjmsnaming://localhost:7222 -connectionfactory XAQueueConnectionFactory -user admin -password tibco123 -queue -listen queue.sample -trans xa -transmgrtype local -transmsgnum 3");
        System.out.println("\n java -jar JmsStream.jar -jmsclient APACHE_AMQ -providerurl tcp://localhost:61616 -send TEST.Q -queue -file \"c:\\TestMessage(UTF-8).jmss\" -stats 2 -noecho -fileloop 5");
        System.out.println("\n java -jar JmsStream.jar -jmsclient HORNETQ -providerurl jnp://localhost:1099 -listen ExampleQueue -queue -verbose");
        System.out.println("\n java com.tibco.util.JmsStream -topic -connectionfactory UIL2ConnectionFactory -jndicontextfactory org.jnp.interfaces.NamingContextFactory -providerurl jnp://localhost:1099 -listen testTopic -verbose -file /home/tibco/test.jmss");
        System.out.println("\n java com.tibco.util.JmsStream -topic -connectionfactory UIL2ConnectionFactory -jndicontextfactory org.jnp.interfaces.NamingContextFactory -providerurl jnp://localhost:1099 -send TestTopic -rate 1.5 -file /home/tibco/test.jmss");
        System.out.println();

        // TODO: create more examples and usage notes
    }

    /**
     * The license method calls the static method getLicenseAgreement, and prints the return value to the screen.
     */
    public static void license()
    {
        System.out.println(" ---------------------------------------------------------------------");
        System.out.println(" " + APP_NAME + " " + APP_VERSION);
        System.out.println();
        System.out.println(" By using this software you agree the the following license agreement.");
        System.out.println(" ---------------------------------------------------------------------");
        System.out.println(License.getLicenseAgreement());
    }

    /**
     * The getBodyLength method calculates the body length of a message in a text file.  Used
     * to calculate the length of messages that were changed by a text editor.
     *
     * @param strFileName  The file name.
     * @param strEncoding  The file type encoding:  US-ASCII, ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16
     * @return the body length.
     */
    public static long getBodyLength(String strFileName, String strEncoding)
    {
        java.io.RandomAccessFile raFile = null;
        java.io.File fileInfo;
        long intBodyLength =0;
        byte bBit;
        String strTemp = "";

        try {
            fileInfo = new java.io.File(strFileName);
            raFile = new java.io.RandomAccessFile(strFileName, "r");
            if (!fileInfo.isFile()) {
                throw new Exception("ERROR: File \"" + strFileName + "\" does not exist.");
            }
            if (fileInfo.length() > Integer.MAX_VALUE){
                throw new Exception("ERROR: Message body is too large. Max size is " + Integer.MAX_VALUE + ".");
            }
            try {
                bBit = raFile.readByte();
                //noinspection InfiniteLoopStatement
                while (true) {
                    // If reading a Windows file do not count the line feed.
                    if (bBit != '\r') {
                        intBodyLength++;
                        strTemp += bBit;
                    }
                    bBit = raFile.readByte();
                }
            }
            catch (java.io.EOFException eof) {
                // End of file.
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        finally {
            if (raFile != null ) {
                try {
                    raFile.close();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return intBodyLength;
    }

    /**
     * The setupListenerFile method opens a file to store the messages.
     *
     * @param strFileName  The file name.
     */
    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "ProhibitedExceptionCaught"})
    private void setupListenerFile(String strFileName)
    {
        // Create file writers now for future multi-threaded listeners.
        FileOutputStream outFileStream = null;
        ZipOutputStream zippedStream;
        FileOutputStream outCsvStream = null;
        boolean blnAppend = _env.get("fileappend").equals(Boolean.TRUE);

        try {
            if (_env.get("filetype").toString().equals("text") && strFileName != null && !strFileName.equals("")) {
                outFileStream = new FileOutputStream(strFileName, blnAppend);
                _env.put("osfile", outFileStream);

                // Setup zipped output stream
                if (_env.get("zip").equals(Boolean.TRUE)) {
                    Vector entries;
                    zippedStream = new ZipOutputStream(outFileStream);
                    try {
                        if (_env.containsKey("zipentries")) {
                            entries = (Vector)_env.get("zipentries");
                        }
                        else {
                            entries = new Vector();
                            // If creating a ZIP file and there are no entries use "1.jmss".
                            entries.add("1.jmss");
                        }

                        for (int i=0; i < entries.toArray().length; i++) {
                            ZipEntry zipE = new ZipEntry(entries.elementAt(i).toString());
                            zippedStream.putNextEntry(zipE);
                        }
                        zippedStream.setComment(APP_NAME + " " + APP_VERSION);
                        _env.put("oszip", zippedStream);
                    }
                    catch (IOException e) {
                        zippedStream.close();
                        e.printStackTrace();
                    }
                }
            }
            else if (_env.get("filetype").toString().equals("xml") && strFileName != null && !strFileName.equals("")) {
                // XML save file is deprecated for performance reasons
                if (_env.get("filetype").toString().equals("xml")) {
                    System.out.println("XML files have been deprecated for performance reasons." +
                                       "\n Please use text file type.");
                    return;
                }
            }

            if (_env.containsKey("csvfile")) {
                try {
                    outCsvStream = new FileOutputStream(_env.get("csvfile").toString(), blnAppend);
                    _env.put("oscsvfile", outCsvStream);
                    /*
                    JmsStreamListener.writeCsv("JMSMessageID,JMSDestination,JMSReplyTo,JMSCorrelationID,JMSDeliveryMode,"
                                               + "JMSPriority,JMSType,JMSExpiration,JMSTimestamp,UserProperties--->", outCsvStream);
                    */
                }
                catch (Exception e) {
                    assert outCsvStream != null;
                    outCsvStream.close();
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println();
            System.err.println("No valid file set - no output sent to file!");
            System.err.println();
            try {
                if (outFileStream != null) outFileStream.close();
                if (outCsvStream != null) outCsvStream.close();
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        catch (NullPointerException e) {
            System.err.println();
            try {
                if (outFileStream != null) outFileStream.close();
                if (outCsvStream != null) outCsvStream.close();
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @SuppressWarnings({"deprecation"})
    public void run()
    {
        // Print configuration to stdout
        if (_env.containsKey("showconfig")) {
            Enumeration keys = _env.keys();
            System.out.println("********************   " + APP_NAME + " " + APP_VERSION + " configuration:   ********************");
            System.out.println("VM Vendor.........................................: " + System.getProperty("java.vendor"));
            System.out.println("VM Version........................................: " + System.getProperty("java.version"));
            System.out.println("Max VM Memory.....................................: " + Math.round(Runtime.getRuntime().maxMemory())/1040512 + " MB");
            System.out.println("Total Allocated VM Memory.........................: " + Math.round(Runtime.getRuntime().totalMemory()/1040512) + " MB");
            long intMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            System.out.println("Used VM Memory....................................: " +
                               (intMem < 1048576 ? Math.round(intMem / 1024) + " KB\n" : Math.round(intMem / 1040512) + " MB\n"));
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                // Indent entries so they appear nicely on the screen
                System.out.println((key + "..................................................").substring(0,50) + ": " + _env.get(key));
            }
            System.out.println("******************************************************************************\n");

            // Exit if -showconfig is the only argument
            //if (_intNumOfArgs == 1) return;
            return;
        }
        else {
            // Print line
            System.out.println();
        }

        // Check to see if the arguments are compatible
        if ((_env.get("isListener").equals(Boolean.FALSE) && _env.get("durablename") != null)) {
            System.err.println("ERROR: The -durable and -unsubscribe arguments are only for topic subscribers.");
            return;
        }

        if (_env.containsKey("extractmonmsg") && _env.get("extractmonmsg").equals(Boolean.TRUE) && !(!_env.containsKey("jmsclient") || _env.get("jmsclient").equals("TIBCO_EMS"))) {
            System.err.println("ERROR:  Extracting client messages from TIBCO EMS System Monitor messages only works with TIBCO EMS.");
        }

        if (_env.get("connectionfactory").equals("")) {
            System.err.println("ERROR: The connection factory cannot be null or \"\".");
            System.err.println("        Must set -topic, -queue, -generic, and/or -connectionfactory.");
            return;
        }

        if (_env.containsKey("trans") && _env.get("requestreply").equals(Boolean.TRUE)) {
            System.err.println("ERROR: Request/Reply transaction messages are not supported in JMS.");
        }

        // Put default XA connection factory if using EMS and user did not specify a custom factory
        if (_env.containsKey("trans") && _env.get("trans").equals("xa")) {
            if (_env.get("connectionfactory").equals(_conHelper.getDefaultTopicFactory())) {
                _env.put("connectionfactory", _conHelper.getDefaultXATopicFactory());
            }
            else if (_env.get("connectionfactory").equals(_conHelper.getDefaultQueueFactory())) {
                _env.put("connectionfactory", _conHelper.getDefaultXAQueueFactory());
            }
            else if (_env.get("connectionfactory").equals(_conHelper.getDefaultGenericFactory())) {
                _env.put("connectionfactory", _conHelper.getDefaultXAGenericFactory());
            }

            // Check to see if user put -transmgrtype.  If not set, put the default to local transaction manager.
            if (!_env.containsKey("transmgrtype")) _env.put("transmgrtype", "local");
            // Check to see if user put -transjndiname.  If not set the default to TransactionManager.
            if (!_env.containsKey("transjndiname")) _env.put("transjndiname", "TransactionManager");
            // Put a default timeout of 0.
            if (!_env.containsKey("transtimeout")) _env.put("transtimeout", new Integer(0));

            // Set the system properties for the JBOSS Transaction Manager
            System.setProperty("com.arjuna.ats.arjuna.objectstore.objectStoreDir", DEFAULT_OJB_STORE);
            if (_env.get("transmgrtype").toString().equals("local")) {
                if (_env.get("isListener").equals(Boolean.TRUE))
                    System.setProperty("com.arjuna.ats.arjuna.xa.nodeIdentifier", APP_NAME + "_Listener");
                else
                    System.setProperty("com.arjuna.ats.arjuna.xa.nodeIdentifier", APP_NAME + "_Sender");
                System.setProperty("com.arjuna.ats.jta.jtaTMImplementation", "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple");
                System.setProperty("com.arjuna.ats.jta.jtaUTImplementation", "com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple");
            }
            else if (_env.get("transmgrtype").toString().equals("remote")) {
                System.setProperty("com.arjuna.ats.jta.jtaTMImplementation", "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
                System.setProperty("com.arjuna.ats.jta.jtaUTImplementation", "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");
            }
        }

        if (_env.get("isListener").equals(Boolean.TRUE)) {
            if (_env.containsKey("file"))
                setupListenerFile(_env.get("file").toString());
            else
                setupListenerFile("");

            // Start Thread
            try {
                _runListener = new JmsStreamListener(_env);
                // Clear Listener static variables
                JmsStreamListener.clearCount();
                _thrListener = new Thread(_runListener, "JmsStream Listener");
                _thrListener.start();
                // TODO: Allow multiple listening subjects by calling several Listener threads.
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
            try {
                _thrListener.join();
            }
            catch (InterruptedException ie) {
                System.err.println(" JmsStream thread interrupted.");
            }
        }
        else {
            if (_env.get("requestreply").equals(Boolean.TRUE)) {
                if (_env.containsKey("replyfile"))
                    setupListenerFile(_env.get("replyfile").toString());
                else
                    setupListenerFile("");
            }

            // Publisher is single threaded
            if (_env.get("filetype").toString().equals("text")) {
                try {
                    _runPublisher = new JmsStreamPublisher(_env);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                _thrPublisher = new Thread(_runPublisher, "JmsStream Publisher");
                _thrPublisher.start();
                try {
                    _thrPublisher.join();
                }
                catch (InterruptedException ie) {
                    System.err.println(" JmsStream thread interrupted.");
                }
            }
            // JmsStreamXmlPublisher is deprecated only supplied of backward compatibility
            else if (_env.get("filetype").toString().equals("xml")) {
                JmsStreamXmlPublisher publisher = new JmsStreamXmlPublisher(_env);
                Thread thread = new Thread(publisher);
                thread.start();
            }
            else {
                System.err.println("ERROR: No input file.  Use -file <file-name> command line argument.");
            }
        }
    }
}