/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.jmshelper;

import org.hornetq.api.jms.JMSFactoryType;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Hashtable;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.8
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "unchecked", "UnusedDeclaration", "ProhibitedExceptionDeclared", "ProhibitedExceptionThrown", "NestedTryStatement", "ConstantConditions", "unused"})
public final class ConnectionHelper {
    private Hashtable<String,String> _propValues = new Hashtable<String,String>();
    private Hashtable _env;
    private ConnectionFactory _cFactory;
    private XAConnectionFactory _cFactoryXA;
    private short _intConFactoryType;
    private short _intClientType;
    private String _strClientType;
    private static boolean _blnSslWasConnected = false;

    public final static short GENERIC = 1;
    public final static short TOPIC = 2;
    public final static short QUEUE = 3;
    public final static short XA_GENERIC = 4;
    public final static short XA_TOPIC = 5;
    public final static short XA_QUEUE = 6;

    public final static String JMS_SERVER_TIBCO_EMS = "TIBCO_EMS";
    public final static String JMS_SERVER_APACHE_AMQ = "APACHE_AMQ";
    public final static String JMS_SERVER_HORNETQ = "HORNETQ";

    public final static short JMS_SERVER_TIBCO_EMS_ID = 0;
    public final static short JMS_SERVER_APACHE_AMQ_ID = 1;
    public final static short JMS_SERVER_HORNETQ_ID = 2;

    public final static String JNDI_FILE_URL= "file:./lib/jndi";

    /**
     *   Creates a ConnectionHelper object without setting the env Hashtable
     *   Default to TIBCO EMS Client.
     */
    public ConnectionHelper()
    {
        _strClientType = JMS_SERVER_TIBCO_EMS;
        _intClientType = JMS_SERVER_TIBCO_EMS_ID;
        initProps();
    }

    /**
     *   Creates a ConnectionHelper object without setting the env Hashtable
     *   Default to strClientType Client.
     */
    public ConnectionHelper(String strClientType)
    {
        _strClientType = strClientType;

        if (strClientType.equals(JMS_SERVER_APACHE_AMQ)) _intClientType = JMS_SERVER_APACHE_AMQ_ID;
        else if (strClientType.equals(JMS_SERVER_HORNETQ)) _intClientType = JMS_SERVER_HORNETQ_ID;
        else if (strClientType.equals(JMS_SERVER_TIBCO_EMS)) _intClientType = JMS_SERVER_TIBCO_EMS_ID;

        initProps();
    }

    /**
     *   The _env Hashtable must have the following entries:
     *   _env.put(Context.INITIAL_CONTEXT_FACTORY, String x);
     *   _env.put("connectionfactory", String x);
     *   _env.put(Context.PROVIDER_URL, String x);
     *   .
     * @param env  The env Hashtable
     * @throws Exception when env does not have the right properties
     */

    public ConnectionHelper(Hashtable env) throws Exception
    {
        initProps();
        setEnv(env);
    }

    private void initProps()
    {
        _propValues.put("LocalJndiStore", "file:./lib/jndi");

        /* TIBCO SSL Context variables for JNDI
        TibjmsContext.PROPERTY_URL_LIST = "com.tibco.tibjms.naming.url.list";
        TibjmsContext.SECURITY_PROTOCOL = "com.tibco.tibjms.naming.security_protocol";
        TibjmsContext.SSL_VENDOR = "com.tibco.tibjms.naming.ssl_vendor";
        TibjmsContext.SSL_TRACE = "com.tibco.tibjms.naming.ssl_trace";
        TibjmsContext.SSL_DEBUG_TRACE = "com.tibco.tibjms.naming.ssl_debug_trace";
        TibjmsContext.SSL_ENABLE_VERIFY_HOST = "com.tibco.tibjms.naming.ssl_enable_verify_host";
        TibjmsContext.SSL_ENABLE_VERIFY_HOST_NAME = "com.tibco.tibjms.naming.ssl_enable_verify_hostname";
        TibjmsContext.SSL_EXPECTED_HOST_NAME = "com.tibco.tibjms.naming.ssl_expected_hostname";
        TibjmsContext.SSL_AUTH_ONLY = "com.tibco.tibjms.naming.ssl_auth_only";
        TibjmsContext.SSL_TRUSTED_CERTIFICATES = "com.tibco.tibjms.naming.ssl_trusted_certs";
        TibjmsContext.SSL_HOST_NAME_VERIFIER = "com.tibco.tibjms.naming.ssl_hostname_verifier";
        TibjmsContext.SSL_IDENTITY = "com.tibco.tibjms.naming.ssl_identity";
        TibjmsContext.SSL_IDENTITY_ENCODING = "com.tibco.tibjms.naming.ssl_identity_encoding";
        TibjmsContext.SSL_ISSUER_CERTIFICATES = "com.tibco.tibjms.naming.ssl_issuer_certs";
        TibjmsContext.SSL_PRIVATE_KEY = "com.tibco.tibjms.naming.ssl_private_key";
        TibjmsContext.SSL_PRIVATE_KEY_ENCODING = "com.tibco.tibjms.naming.ssl_private_key_encoding";
        TibjmsContext.SSL_PASSWORD = "com.tibco.tibjms.naming.ssl_password";
        TibjmsContext.SSL_CIPHER_SUITES = "com.tibco.tibjms.naming.ssl_cipher_suites";
        TibjmsContext._NAMING_SSL_PREFIX = "com.tibco.tibjms.naming.ssl_";
        TibjmsContext._TIBJMS_SSL_PREFIX = "com.tibco.tibjms.ssl.";

        /* TIBCO SSL Context variables for connection factory
        public static final String PROP_SSL_TRACE = "tibco.tibjms.ssl.trace";
        public static final String PROP_SSL_DEBUG_TRACE = "tibco.tibjms.ssl.debug.trace";
        public static final String VENDOR = "com.tibco.tibjms.ssl.vendor";
        public static final String TRACE = "com.tibco.tibjms.ssl.trace";
        public static final String AUTH_ONLY = "com.tibco.tibjms.ssl.auth_only";
        public static final String DEBUG_TRACE = "com.tibco.tibjms.ssl.debug_trace";
        public static final String TRUSTED_CERTIFICATES = "com.tibco.tibjms.ssl.trusted_certs";
        public static final String ENABLE_VERIFY_HOST = "com.tibco.tibjms.ssl.enable_verify_host";
        public static final String ENABLE_VERIFY_HOST_NAME = "com.tibco.tibjms.ssl.enable_verify_hostname";
        public static final String EXPECTED_HOST_NAME = "com.tibco.tibjms.ssl.expected_hostname";
        public static final String HOST_NAME_VERIFIER = "com.tibco.tibjms.ssl.hostname_verifier";
        public static final String IDENTITY = "com.tibco.tibjms.ssl.identity";
        public static final String IDENTITY_ENCODING = "com.tibco.tibjms.ssl.identity_encoding";
        public static final String ISSUER_CERTIFICATES = "com.tibco.tibjms.ssl.issuer_certs";
        public static final String PRIVATE_KEY = "com.tibco.tibjms.ssl.private_key";
        public static final String PRIVATE_KEY_ENCODING = "com.tibco.tibjms.ssl.private_key_encoding";
        public static final String PASSWORD = "com.tibco.tibjms.ssl.password";
        public static final String CIPHER_SUITES = "com.tibco.tibjms.ssl.cipher_suites";
         */

        _propValues.put("TIBCO_EMS.InitialContextFactory", "com.tibco.tibjms.naming.TibjmsInitialContextFactory");
        _propValues.put("TIBCO_EMS.ProviderUrl", "tibjmsnaming://localhost:7222");
        _propValues.put("TIBCO_EMS.UrlPkgPrefixes", "com.tibco.tibjms.naming");
        _propValues.put("TIBCO_EMS.DefaultGenericFactory", "GenericConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultTopicFactory", "TopicConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultQueueFactory", "QueueConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultXAGenericFactory", "XAGenericConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultXATopicFactory", "XATopicConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultXAQueueFactory", "XAQueueConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultSslGenericFactory", "SSLGenericConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultSslTopicFactory", "SSLTopicConnectionFactory");
        _propValues.put("TIBCO_EMS.DefaultSslQueueFactory", "SSLQueueConnectionFactory");
        // TIBCO SSL
        _propValues.put("TIBCO_EMS.PropertyUrlList", "com.tibco.tibjms.naming.url.list");
        _propValues.put("TIBCO_EMS.SecurityProtocol", "com.tibco.tibjms.naming.security_protocol");
        _propValues.put("TIBCO_EMS.SslNamingPrefix", "com.tibco.tibjms.naming.ssl_");
        _propValues.put("TIBCO_EMS.SslPrefix", "com.tibco.tibjms.ssl.");
        // The following TIBCO SSL properties must have a TIBCO_EMS.SslNamingPrefix or TIBCO_EMS.SslPrefix
        _propValues.put("TIBCO_EMS.SslVendor", "vendor");
        _propValues.put("TIBCO_EMS.SslTrace", "trace");
        _propValues.put("TIBCO_EMS.SslDebugTrace", "debug_trace");
        _propValues.put("TIBCO_EMS.SslEnableVerifyHost", "enable_verify_host");
        _propValues.put("TIBCO_EMS.SslEnableVerifyHostName", "enable_verify_hostname");
        _propValues.put("TIBCO_EMS.SslExpectedHostName", "expected_hostname");
        _propValues.put("TIBCO_EMS.SslAuthOnly", "auth_only");
        _propValues.put("TIBCO_EMS.SslTrustedCertificates", "trusted_certs");
        _propValues.put("TIBCO_EMS.SslHostNameVerifier", "hostname_verifier");
        _propValues.put("TIBCO_EMS.SslIdentity", "identity");
        _propValues.put("TIBCO_EMS.SslIdentityEncoding", "identity_encoding");
        _propValues.put("TIBCO_EMS.SslIssuerCertificates", "issuer_certs");
        _propValues.put("TIBCO_EMS.SslPrivateKey", "private_key");
        _propValues.put("TIBCO_EMS.SslPrivateKeyEncoding", "private_key_encoding");
        _propValues.put("TIBCO_EMS.SslPassword", "password");
        _propValues.put("TIBCO_EMS.SslCipherSuites", "cipher_suites");
        // Java SSL
        _propValues.put("TIBCO_EMS.SslVendorDefault", "j2se-default");
        _propValues.put("TIBCO_EMS.keyStoreType", "javax.net.ssl.keyStoreType");
        _propValues.put("TIBCO_EMS.keyStore", "javax.net.ssl.keyStore");
        _propValues.put("TIBCO_EMS.keyStorePassword", "javax.net.ssl.keyStorePassword");
        _propValues.put("TIBCO_EMS.trustStoreType", "javax.net.ssl.trustStoreType");
        _propValues.put("TIBCO_EMS.trustStore", "javax.net.ssl.trustStore");
        _propValues.put("TIBCO_EMS.trustStorePassword", "javax.net.ssl.trustStorePassword");
        _propValues.put("TIBCO_EMS.net.debug", "javax.net.debug");

        /* Setting for Apache ActiveMQ
        properties.put("sendAcksAsync", Boolean.FALSE);
        properties.put("prefetchPolicy.all", "32");
        properties.put("prefetchPolicy.durableTopicPrefetch", "32");
        properties.put("prefetchPolicy.optimizeDurableTopicPrefetch", "32");
        properties.put("prefetchPolicy.queuePrefetch", "777");
        properties.put("redeliveryPolicy.maximumRedeliveries", "15");
        properties.put("redeliveryPolicy.backOffMultiplier", "32");
        */

        _propValues.put("APACHE_AMQ.InitialContextFactory", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        _propValues.put("APACHE_AMQ.ProviderUrl", "tcp://localhost:61616"); // or "ssl://localhost:61616"
        _propValues.put("APACHE_AMQ.UrlPkgPrefixes", "org.apache.activemq.jndi");
        _propValues.put("APACHE_AMQ.DefaultGenericFactory", "ConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultTopicFactory", "TopicConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultQueueFactory", "QueueConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultXAGenericFactory", "XAConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultXATopicFactory", "XATopicConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultXAQueueFactory", "XAQueueConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultSslGenericFactory", "ConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultSslTopicFactory", "TopicConnectionFactory");
        _propValues.put("APACHE_AMQ.DefaultSslQueueFactory", "QueueConnectionFactory");

        // Apache SSL
        /*
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/broker.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStoreType", "jks");
        System.setProperty("javax.net.ssl.keyStore", "src/test/resources/client.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.keyStoreType", "jks");
        System.setProperty("javax.net.debug", "ssl,handshake,data,trustmanager");
         */
        _propValues.put("APACHE_AMQ.SecurityProtocol", "java.protocol.handler.pkgs");
        _propValues.put("APACHE_AMQ.SslVendor", "security.provider.n=");
        _propValues.put("APACHE_AMQ.keyStoreType", "javax.net.ssl.keyStoreType");
        _propValues.put("APACHE_AMQ.keyStore", "javax.net.ssl.keyStore");
        _propValues.put("APACHE_AMQ.keyStorePassword", "javax.net.ssl.keyStorePassword");
        _propValues.put("APACHE_AMQ.trustStoreType", "javax.net.ssl.trustStoreType");
        _propValues.put("APACHE_AMQ.trustStore", "javax.net.ssl.trustStore");
        _propValues.put("APACHE_AMQ.trustStorePassword", "javax.net.ssl.trustStorePassword");
        _propValues.put("APACHE_AMQ.net.debug", "javax.net.debug");

        // Apache More properties
        /*
        connection.setPrefetchPolicy(getPrefetchPolicy());
        connection.setDisableTimeStampsByDefault(isDisableTimeStampsByDefault());
        connection.setOptimizedMessageDispatch(isOptimizedMessageDispatch());
        connection.setCopyMessageOnSend(isCopyMessageOnSend());
        connection.setUseComuipression(isUseCompression());
        connection.setObjectMessageSerializationDefered(isObjectMessageSerializationDefered());
        connection.setDispatchAsync(isDispatchAsync());
        connection.setUseAsyncSend(isUseAsyncSend());
        connection.setAlwaysSyncSend(isAlwaysSyncSend());
        connection.setAlwaysSessionAsync(isAlwaysSessionAsync());
        connection.setOptimizeAcknowledge(isOptimizeAcknowledge());
        connection.setUseRetroactiveConsumer(isUseRetroactiveConsumer());
        connection.setExclusiveConsumer(isExclusiveConsumer());
        connection.setRedeliveryPolicy(getRedeliveryPolicy());
        connection.setTransformer(getTransformer());
        connection.setBlobTransferPolicy(getBlobTransferPolicy().copy());
        connection.setWatchTopicAdvisories(isWatchTopicAdvisories());
        connection.setProducerWindowSize(getProducerWindowSize());
        connection.setWarnAboutUnstartedConnectionTimeout(getWarnAboutUnstartedConnectionTimeout());
        connection.setSendTimeout(getSendTimeout());
        connection.setSendAcksAsync(isSendAcksAsync());
        connection.setAuditDepth(getAuditDepth());
        connection.setAuditMaximumProducerNumber(getAuditMaximumProducerNumber());
         */
        
        // HornetQ
        _propValues.put("HORNETQ.InitialContextFactory", "org.jnp.interfaces.NamingContextFactory");
        _propValues.put("HORNETQ.ProviderUrl", "jnp://localhost:1099");
        _propValues.put("HORNETQ.UrlPkgPrefixes", "org.jboss.naming:org.jnp.interfaces");
        _propValues.put("HORNETQ.DefaultGenericFactory", "ConnectionFactory");
        _propValues.put("HORNETQ.DefaultTopicFactory", "ConnectionFactory");
        _propValues.put("HORNETQ.DefaultQueueFactory", "ConnectionFactory");
        _propValues.put("HORNETQ.DefaultXAGenericFactory", "XAConnectionFactory");
        _propValues.put("HORNETQ.DefaultXATopicFactory", "XAConnectionFactory");
        _propValues.put("HORNETQ.DefaultXAQueueFactory", "XAConnectionFactory");
        _propValues.put("HORNETQ.DefaultSslGenericFactory", "SSLConnectionFactory");
        _propValues.put("HORNETQ.DefaultSslTopicFactory", "SSLConnectionFactory");
        _propValues.put("HORNETQ.DefaultSslQueueFactory", "SSLConnectionFactory");

        _propValues.put("HORNETQ.SecurityProtocol", "java.protocol.handler.pkgs");
        _propValues.put("HORNETQ.SslVendor", "security.provider.n=");
        _propValues.put("HORNETQ.keyStoreType", "javax.net.ssl.keyStoreType");
        _propValues.put("HORNETQ.keyStore", "javax.net.ssl.keyStore");
        _propValues.put("HORNETQ.keyStorePassword", "javax.net.ssl.keyStorePassword");
        _propValues.put("HORNETQ.trustStoreType", "javax.net.ssl.trustStoreType");
        _propValues.put("HORNETQ.trustStore", "javax.net.ssl.trustStore");
        _propValues.put("HORNETQ.trustStorePassword", "javax.net.ssl.trustStorePassword");
        _propValues.put("HORNETQ.net.debug", "javax.net.debug");
    }

    /**
     *   The _env Hashtable must have the following entries:
     *   _env.put(Context.INITIAL_CONTEXT_FACTORY, String x);
     *   _env.put("connectionfactory", String x);
     *   _env.put(Context.PROVIDER_URL, String x);
     *   .
     * @param env  The env Hashtable
     * @throws Exception when env does not have the right properties
     */
    public void setEnv(Hashtable env) throws Exception
    {
        if (env.containsKey(Context.INITIAL_CONTEXT_FACTORY)
                && env.containsKey("connectionfactory")
                && env.containsKey(Context.PROVIDER_URL)) {
            _env = env;
            _cFactory = null;
            _cFactoryXA = null;
            _intConFactoryType = 0;
 
            if (_env.containsKey("jmsclient"))
                setJmsClientType(_env.get("jmsclient").toString());
        }
        else
            throw new Exception("Error:  env does not contain the required entries.");
    }

    public void setJmsClientType(String JmsClientType) throws Exception
    {
        if (JmsClientType.equals(JMS_SERVER_TIBCO_EMS))  {
            com.tibco.tibjms.TibjmsSSL.initialize();
            _intClientType = JMS_SERVER_TIBCO_EMS_ID;
            // Set constants
        }
        else if (JmsClientType.equals(JMS_SERVER_APACHE_AMQ))  {
            _intClientType = JMS_SERVER_APACHE_AMQ_ID;
            // Set constants
        }
        else if (JmsClientType.equals(JMS_SERVER_HORNETQ))  {
            _intClientType = JMS_SERVER_HORNETQ_ID;
            // Set constants
        }
        else
            throw new Exception("ERROR:  SSL not supported for vendor " + JmsClientType + ".");

        _strClientType = JmsClientType;
    }

    public ConnectionFactory createConnectionFactory() throws Exception
    {
        // Set the SSL system properties if using Java SSL
        if (_intClientType == JMS_SERVER_APACHE_AMQ_ID || _intClientType == JMS_SERVER_HORNETQ_ID) {

            if (_env.containsKey(getKeyStoreType())) {
                System.setProperty(getKeyStoreType(), _env.get(getKeyStoreType()).toString());
            }
            if (_env.containsKey(getKeyStore())) {
                System.setProperty(getKeyStore(), _env.get(getKeyStore()).toString());
            }
            if (_env.containsKey(getKeyStorePassword())) {
                System.setProperty(getKeyStorePassword(), _env.get(getKeyStorePassword()).toString());
            }
            if (_env.containsKey(getTrustStoreType())) {
                System.setProperty(getTrustStoreType(), _env.get(getTrustStoreType()).toString());
            }
            if (_env.containsKey(getTrustStore())) {
                System.setProperty(getTrustStore(), _env.get(getTrustStore()).toString());
            }
            if (_env.containsKey(getTrustStorePassword())) {
                System.setProperty(getTrustStorePassword(), _env.get(getTrustStorePassword()).toString());
            }
            if (_env.containsKey(getNetDebug())) {
                System.setProperty(getNetDebug(), _env.get(getNetDebug()).toString());
            }

            // Test the Cipher Suites
            //javax.net.ssl.SSLContext sslCon = javax.net.ssl.SSLContext.getDefault();
            //javax.net.ssl.SSLParameters sslPar = sslCon.getDefaultSSLParameters();
            //sslPar.getCipherSuites();
        }

        // Get the connection factories using JNDI lookup
        Context context;
        // Get the connection factory context as an object
        Object objConnectionFactory;

        context = new InitialContext(_env);
        objConnectionFactory = context.lookup(_env.get("connectionfactory").toString());

        if (context == null) throw new RuntimeException("Failed to get the JNDI root context");

        if (objConnectionFactory != null) {
            // If connection type is not given, try to derive the type from the connection factory.
            if (_intConFactoryType == 0) {
                if (XATopicConnectionFactory.class.isInstance(objConnectionFactory)) {
                    _intConFactoryType = XA_TOPIC;
                }
                else if (XAQueueConnectionFactory.class.isInstance(objConnectionFactory)) {
                    _intConFactoryType = XA_QUEUE;
                }
                else if (XAConnectionFactory.class.isInstance(objConnectionFactory)) {
                    _intConFactoryType = XA_GENERIC;
                }
                else if (TopicConnectionFactory.class.isInstance(objConnectionFactory)) {
                    _intConFactoryType = TOPIC;
                }
                else if (QueueConnectionFactory.class.isInstance(objConnectionFactory)) {
                    _intConFactoryType = QUEUE;
                }
                else if (ConnectionFactory.class.isInstance(objConnectionFactory)) {
                    _intConFactoryType = GENERIC;
                }
                //TIBCO Specific connection factories
                // TibjmsUFOConnectionFactory
            }
            switch (_intConFactoryType) {
                case GENERIC:
                    _cFactory = (ConnectionFactory)objConnectionFactory;
                    break;
                case TOPIC:
                    _cFactory = (TopicConnectionFactory)objConnectionFactory;
                    break;
                case QUEUE:
                    _cFactory = (QueueConnectionFactory)objConnectionFactory;
                    break;
                case XA_GENERIC:
                    _cFactoryXA = (XAConnectionFactory)objConnectionFactory;
                    break;
                case XA_TOPIC:
                    _cFactoryXA = (XATopicConnectionFactory)objConnectionFactory;
                    break;
                case XA_QUEUE:
                    _cFactoryXA = (XAQueueConnectionFactory)objConnectionFactory;
                    break;
                default:
                    throw new Exception("ERROR: Unrecognized connection factory type.");
            }
        }

        // If the SSL was ever initiated in the VM the set this variable to true.
        if (!_blnSslWasConnected && _env.containsKey("ssl") && _env.get("ssl").equals(Boolean.TRUE)) _blnSslWasConnected = true;

        // Must create new default SSLContext to accept all certificates if there is no Trust Store.
        if ((_intClientType == JMS_SERVER_HORNETQ_ID || _intClientType == JMS_SERVER_APACHE_AMQ_ID) && (_env.containsKey("ssl") && _env.get("ssl").equals(Boolean.TRUE))
                && ((_env.containsKey("trustAllCerts") && _env.get("trustAllCerts").equals(Boolean.TRUE)) || System.getProperty(getTrustStore()) == null)) {
            setTrustAllCerts();
        }

        if ((_env.containsKey("trans") && _env.get("trans").equals("xa"))
            && (_intConFactoryType != XA_GENERIC && _intConFactoryType != XA_TOPIC && _intConFactoryType != XA_QUEUE)) {
            throw new Exception("ERROR: Expected transactional connection factory, but returned connection factory is not transactional.");
        }
        else if ((_intConFactoryType == XA_GENERIC || _intConFactoryType == XA_TOPIC || _intConFactoryType == XA_QUEUE) && (!_env.containsKey("trans") && !_env.get("trans").equals("xa"))) {
            throw new Exception("ERROR: Unexpected connection factory.  Connection Factory is transactional.");
        }

        return (ConnectionFactory)objConnectionFactory;
    }

    public short getFactoryType()
    {
        return _intConFactoryType;
    }

    public void setFactoryType(short factoryType) {
        _intConFactoryType = factoryType;
    }

    /**
     *
     * @param env  Then env hashtable.
     */
    public void resetConnectionFactory(Hashtable env)
    {
        _env = env;
        _cFactory = null;
        _intConFactoryType = 0;
    }

    public ConnectionFactory getConnectionFactory() throws Exception
    {
        if (_cFactory == null) createConnectionFactory();
        return _cFactory;
    }

    public XAConnectionFactory getXAConnectionFactory() throws Exception
    {
        if (_cFactoryXA == null) createConnectionFactory();
        return _cFactoryXA;
    }

    public String getFactoryName()
    {
        return _env.get("connectionfactory").toString();
    }

    public String getFactoryDescription()
    {
        String strDesc;

        switch (_intConFactoryType) {
            case GENERIC:
                strDesc = "Generic";
                break;
            case TOPIC:
                strDesc = "Topic";
                break;
            case QUEUE:
                strDesc = "Queue";
                break;
            case XA_GENERIC:
                strDesc = "XA_Generic";
                break;
            case XA_TOPIC:
                strDesc = "XA_Topic";
                break;
            case XA_QUEUE:
                strDesc = "XA_Queue";
                break;
            default:
                strDesc = "Unknown_Factory";
        }
        return strDesc;
    }

    /**
     * Returns the string name of the session acknowledgement type.
     *
     * @param ackType  The EMS acknowledgement type.
     * @return String
     * @throws Exception    Acknowledgement Mode not supported.
     */
    public static String ackTypeToString(int ackType) throws Exception
    {
        String strName;
        if (ackType == Session.AUTO_ACKNOWLEDGE) strName = "AUTO_ACKNOWLEDGE";
        else if (ackType == Session.CLIENT_ACKNOWLEDGE) strName = "CLIENT_ACKNOWLEDGE";
        else if (ackType == Session.DUPS_OK_ACKNOWLEDGE) strName = "DUPS_OK_ACKNOWLEDGE";
        else if (ackType == com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE) strName = "EXPLICIT_CLIENT_ACKNOWLEDGE";
        else if (ackType == com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE) strName = "EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE";
        else if (ackType == com.tibco.tibjms.Tibjms.NO_ACKNOWLEDGE) strName = "NO_ACKNOWLEDGE";
        else if (ackType == org.apache.activemq.ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE) strName = "INDIVIDUAL_ACKNOWLEDGE";
        else throw new Exception(ackType + " Acknowledgement Mode not supported.");

        return strName;
    }

    /**
     * Returns the string name of the session acknowledgement type.
     *
     * @param ackName       The acknowledgement type.
     * @param JmsClientType The client type.
     * @return int
     * @throws Exception    Acknowledgement Mode not supported.
     */
    public static int stringToAckType(String ackName, String JmsClientType) throws Exception
    {
        int ackType = 0;

        if (ackName.equalsIgnoreCase("AUTO_ACKNOWLEDGE")) ackType = Session.AUTO_ACKNOWLEDGE;
        else if (ackName.equalsIgnoreCase("CLIENT_ACKNOWLEDGE")) ackType = Session.CLIENT_ACKNOWLEDGE;
        else if (ackName.equalsIgnoreCase("DUPS_OK_ACKNOWLEDGE")) ackType = Session.DUPS_OK_ACKNOWLEDGE;
        else if (ackName.equalsIgnoreCase("EXPLICIT_CLIENT_ACKNOWLEDGE")) ackType = com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE;
        else if (ackName.equalsIgnoreCase("EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE")) ackType = com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE;
        else if (ackName.equalsIgnoreCase("NO_ACKNOWLEDGE")) ackType = com.tibco.tibjms.Tibjms.NO_ACKNOWLEDGE;
        else if (ackName.equalsIgnoreCase("INDIVIDUAL_ACKNOWLEDGE")) {
            if (JmsClientType.equals(ConnectionHelper.JMS_SERVER_TIBCO_EMS))
                ackType = com.tibco.tibjms.Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE;
            else if (JmsClientType.equals(ConnectionHelper.JMS_SERVER_APACHE_AMQ))
                ackType = org.apache.activemq.ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE;
        }
        else throw new Exception(ackName + " Acknowledgement Mode not supported.");

        return ackType;
    }

    /**
     * Returns the string name of the DeliveryMode.
     *
     * @param deliveryMode  The JMS delivery mode.
     * @return String
     */
    public static String deliveryModeToString(int deliveryMode)
    {
        String strName = "";
        if (deliveryMode == DeliveryMode.NON_PERSISTENT) strName = "NON_PERSISTENT";
        else if (deliveryMode == DeliveryMode.PERSISTENT) strName = "PERSISTENT";
        else if (deliveryMode == com.tibco.tibjms.Tibjms.RELIABLE_DELIVERY) strName = "RELIABLE_DELIVERY";

        return strName;
    }

    // Connection Helpers
    public String getInitialContextFactory()
    {
        return _propValues.get(_strClientType + ".InitialContextFactory");
    }

    public String getProviderUrl()
    {
        return _propValues.get(_strClientType + ".ProviderUrl");
    }

    public String getUrlPkgPrefixes()
    {
        return _propValues.get(_strClientType + ".UrlPkgPrefixes");
    }

    public String getJmsClientType()
    {
        return _strClientType;
    }

    public int getJmsClientTypeId()
    {
        return _intClientType;
    }

    public String getTibPropertyUrlList()
    {
        return _propValues.get("TIBCO_EMS.PropertyUrlList");
    }

    // SSL TIBCO Helpers
    public String getSecurityProtocol()
    {
        return _propValues.get(_strClientType + ".SecurityProtocol");
    }

    public String getSslVendor()
    {
        return _propValues.get(_strClientType + ".SslPrefix") + _propValues.get(_strClientType + ".SslVendor");
    }

    public String getSslVendorNaming()
    {
        return _propValues.get(_strClientType + ".SslNamingPrefix") + _propValues.get(_strClientType + ".SslVendor");
    }

    public String getSslTrace()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslTrace");
    }

    public String getSslTraceNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") +  _propValues.get("TIBCO_EMS.SslTrace");
    }

    public String getSslDebugTrace()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslDebugTrace");
    }

    public String getSslDebugTraceNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslDebugTrace");
    }

    public String getSslEnableVerifyHost()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslEnableVerifyHost");
    }

    public String getSslEnableVerifyHostNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslEnableVerifyHost");
    }

    public String getSslEnableVerifyHostName()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslEnableVerifyHostName");
    }

    public String getSslEnableVerifyHostNameNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslEnableVerifyHostName");
    }

    public String getSslExpectedHostName()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslExpectedHostName");
    }

    public String getSslExpectedHostNameNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslExpectedHostName");
    }

    public String getSslAuthOnly()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslAuthOnly");
    }

    public String getSslAuthOnlyNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslAuthOnly");
    }

    public String getSslTrustedCertificates()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslTrustedCertificates");
    }

    public String getSslTrustedCertificatesNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslTrustedCertificates");
    }

    public String getSslHostNameVerifier()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslHostNameVerifier");
    }

    public String getSslHostNameVerifierNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslHostNameVerifier");
    }

    public String getSslIdentity()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslIdentity");
    }

    public String getSslIdentityNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslIdentity");
    }

    public String getSslIdentityEncoding()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslIdentityEncoding");
    }

    public String getSslIdentityEncodingNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslIdentityEncoding");
    }

    public String getSslIssuerCertificates()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslIssuerCertificates");
    }

    public String getSslIssuerCertificatesNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslIssuerCertificates");
    }

    public String getSslPrivateKey()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslPrivateKey");
    }

    public String getSslPrivateKeyNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslPrivateKey");
    }

    public String getSslPrivateKeyEncoding()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslPrivateKeyEncoding");
    }

    public String getSslPrivateKeyEncodingNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslPrivateKeyEncoding");
    }

    public String getSslPassword()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslPassword");
    }

    public String getSslPasswordNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslPassword");
    }

    public String getSslCipherSuites()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix") + _propValues.get("TIBCO_EMS.SslCipherSuites");
    }

    public String getSslCipherSuitesNaming()
    {
        return _propValues.get("TIBCO_EMS.SslNamingPrefix") + _propValues.get("TIBCO_EMS.SslCipherSuites");
    }

    public String getNamingSslPrefix()
    {
        return _propValues.get("TIBCO_EMS.NamingSslPrefix");
    }

    public String getSslPrefix()
    {
        return _propValues.get("TIBCO_EMS.SslPrefix");
    }

    public String getSslVendorDefault()
    {
        return _propValues.get("TIBCO_EMS.SslVendorDefault");
    }

    public String getTrustStore()
    {
        return _propValues.get(_strClientType + ".trustStore");
    }

    public String getTrustStorePassword()
    {
        return _propValues.get(_strClientType + ".trustStorePassword");
    }

    public String getTrustStoreType()
    {
        return _propValues.get(_strClientType + ".trustStoreType");
    }

    public String getKeyStore()
    {
        return _propValues.get(_strClientType + ".keyStore");
    }

    public String getKeyStorePassword()
    {
        return _propValues.get(_strClientType + ".keyStorePassword");
    }

    public String getKeyStoreType()
    {
        return _propValues.get(_strClientType + ".keyStoreType");
    }

    public String getNetDebug()
    {
        return _propValues.get(_strClientType + ".net.debug");
    }

    // Connection Factory settings
    public String getDefaultGenericFactory()
    {
        return _propValues.get(_strClientType + ".DefaultGenericFactory");
    }

    public String getDefaultTopicFactory()
    {
        return _propValues.get(_strClientType + ".DefaultTopicFactory");
    }

    public String getDefaultQueueFactory()
    {
        return _propValues.get(_strClientType + ".DefaultQueueFactory");
    }

    public String getDefaultXAGenericFactory()
    {
        return _propValues.get(_strClientType + ".DefaultXAGenericFactory");
    }

    public String getDefaultXATopicFactory()
    {
        return _propValues.get(_strClientType + ".DefaultXATopicFactory");
    }

    public String getDefaultXAQueueFactory()
    {
        return _propValues.get(_strClientType + ".DefaultXAQueueFactory");
    }

    public String getDefaultSslGenericFactory()
    {
        return _propValues.get(_strClientType + ".DefaultSslGenericFactory");
    }

    public String getDefaultSslTopicFactory()
    {
        return _propValues.get(_strClientType + ".DefaultSslTopicFactory");
    }

    public String getDefaultSslQueueFactory()
    {
        return _propValues.get(_strClientType + ".DefaultSslQueueFactory");
    }

    /*
    public String getCompressMsgProperty()
    {
        return _propValues.get(_strClientType + ".CompressMessage");
    }
    */

    public boolean isDefaultSetting(String strParameter)
    {
        if (getDefaultGenericFactory().equals(strParameter)) return true;
        else if (getDefaultQueueFactory().equals(strParameter)) return true;
        else if (getDefaultSslGenericFactory().equals(strParameter)) return true;
        else if (getDefaultSslQueueFactory().equals(strParameter)) return true;
        else if (getDefaultSslTopicFactory().equals(strParameter)) return true;
        else if (getDefaultTopicFactory().equals(strParameter)) return true;
        else if (getDefaultXAGenericFactory().equals(strParameter)) return true;
        else if (getDefaultXAQueueFactory().equals(strParameter)) return true;
        else if (getDefaultXATopicFactory().equals(strParameter)) return true;
        else if (getFactoryDescription().equals(strParameter)) return true;
        else if (getInitialContextFactory().equals(strParameter)) return true;
        else if (getKeyStore().equals(strParameter)) return true;
        else if (getKeyStorePassword().equals(strParameter)) return true;
        else if (getKeyStoreType().equals(strParameter)) return true;
        else if (getNetDebug().equals(strParameter)) return true;
        else if (getProviderUrl().equals(strParameter)) return true;
        else if (getSecurityProtocol().equals(strParameter)) return true;
        else if (getSslAuthOnly().equals(strParameter)) return true;
        else if (getSslAuthOnlyNaming().equals(strParameter)) return true;
        else if (getSslCipherSuites().equals(strParameter)) return true;
        else if (getSslCipherSuitesNaming().equals(strParameter)) return true;
        else if (getSslDebugTrace().equals(strParameter)) return true;
        else if (getSslDebugTraceNaming().equals(strParameter)) return true;
        else if (getSslEnableVerifyHost().equals(strParameter)) return true;
        else if (getSslEnableVerifyHostName().equals(strParameter)) return true;
        else if (getSslEnableVerifyHostNameNaming().equals(strParameter)) return true;
        else if (getSslEnableVerifyHostNaming().equals(strParameter)) return true;
        else if (getSslExpectedHostName().equals(strParameter)) return true;
        else if (getSslExpectedHostNameNaming().equals(strParameter)) return true;
        else if (getSslHostNameVerifier().equals(strParameter)) return true;
        else if (getSslHostNameVerifierNaming().equals(strParameter)) return true;
        else if (getSslIdentity().equals(strParameter)) return true;
        else if (getSslIdentityEncoding().equals(strParameter)) return true;
        else if (getSslIdentityEncodingNaming().equals(strParameter)) return true;
        else if (getSslIdentityNaming().equals(strParameter)) return true;
        else if (getSslIssuerCertificates().equals(strParameter)) return true;
        else if (getSslIssuerCertificatesNaming().equals(strParameter)) return true;
        else if (getSslPassword().equals(strParameter)) return true;
        else if (getSslPasswordNaming().equals(strParameter)) return true;
        else if (getSslPrivateKey().equals(strParameter)) return true;
        else if (getSslPrivateKeyEncoding().equals(strParameter)) return true;
        else if (getSslPrivateKeyEncodingNaming().equals(strParameter)) return true;
        else if (getSslPrivateKeyNaming().equals(strParameter)) return true;
        else if (getSslTrace().equals(strParameter)) return true;
        else if (getSslTraceNaming().equals(strParameter)) return true;
        else if (getSslTrustedCertificates().equals(strParameter)) return true;
        else if (getSslTrustedCertificatesNaming().equals(strParameter)) return true;
        else if (getSslVendor().equals(strParameter)) return true;
        else if (getSslVendorDefault().equals(strParameter)) return true;
        else if (getSslVendorNaming().equals(strParameter)) return true;
        else if (getTibPropertyUrlList().equals(strParameter)) return true;
        else if (getTrustStore().equals(strParameter)) return true;
        else if (getTrustStorePassword().equals(strParameter)) return true;
        else if (getTrustStoreType().equals(strParameter)) return true;
        else if (getUrlPkgPrefixes().equals(strParameter)) return true;

        return false;
    }

    /**
     * This method is to check if the Java SSL Connection was ever instantiated.
     *
     * Due to limitations with the Java SSL implementation the SSL parameters can only be set once.
     * After the SSL connection is initiated you must re-start JmsStream to change these settings.
     * Disable the Java SSL fields to prevent the user from editing them.
     *
     * @return  boolean
     */
    public static boolean wasSslConnected()
    {
        return _blnSslWasConnected;
    }

    /**
     * This method creates a java.Security.SecureRandom object seeded with
     * the current time.  It allows the JmsStream to use SSL to initialize
     * the SSL environment much faster than if it had to generate a truly
     * random seed.
     *
     * NOTE: THIS SHOULD NOT BE USED IN A PRODUCTION ENVIRONMENT AS IT IS
     *       NOT SECURE.
     *
     * @return Secure Random
     */
    public static SecureRandom createUnsecureRandom()
    {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(System.currentTimeMillis());
            return sr;
        }
        catch(NoSuchAlgorithmException e) {
            System.err.println("SHA1PRNG secure random (SSL) not available for this JRE.");
            return null;
        }
    }

    /**
     * The default javax.net.ssl.* libraries only except connections with trusted
     * certificates.  This method will replace the default SSLContext with one
     * which will will not validate the server certificate.
     */
    public static void setTrustAllCerts()
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    // Do nothing
                }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    // Do nothing
                }
            }
        };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, createUnsecureRandom());
            SSLContext.setDefault(sc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This classed is used to create a file based JNDI reference for and object.
     * The .binding file is located in the ./lib/jndi directory relative to the
     * execution path.
     *
     * @param strRefName    the JNDI lookup name to create for the object
     * @param refObject     a refreshable object
     * @throws NamingException  throws a naming exception
     */
    public static void jndiCreateRefObject(String strRefName, Object refObject) throws NamingException
    {
        Hashtable<String, String> env = new Hashtable<String, String>(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        env.put(Context.PROVIDER_URL, JNDI_FILE_URL);

        Context initCtx = new InitialContext(env);
        initCtx.bind(strRefName, refObject);
    }

    public static void createJndiFileContext(String strClassName, String strContextName, String strJmsURL) throws NamingException, JMSException
    {
        Object cfObject = null;

        if (com.tibco.tibjms.TibjmsConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new com.tibco.tibjms.TibjmsConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((com.tibco.tibjms.TibjmsConnectionFactory)cfObject).setServerUrl(strJmsURL);
        }
        else if (com.tibco.tibjms.TibjmsQueueConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new com.tibco.tibjms.TibjmsQueueConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((com.tibco.tibjms.TibjmsQueueConnectionFactory)cfObject).setServerUrl(strJmsURL);
        }
        else if (com.tibco.tibjms.TibjmsTopicConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new com.tibco.tibjms.TibjmsTopicConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((com.tibco.tibjms.TibjmsTopicConnectionFactory)cfObject).setServerUrl(strJmsURL);
        }
        else if (com.tibco.tibjms.TibjmsXAConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new com.tibco.tibjms.TibjmsXAConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((com.tibco.tibjms.TibjmsXAConnectionFactory)cfObject).setServerUrl(strJmsURL);
        }
        else if (com.tibco.tibjms.TibjmsXAQueueConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new com.tibco.tibjms.TibjmsXAQueueConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((com.tibco.tibjms.TibjmsXAQueueConnectionFactory)cfObject).setServerUrl(strJmsURL);
        }
        else if (com.tibco.tibjms.TibjmsXATopicConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new com.tibco.tibjms.TibjmsXATopicConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((com.tibco.tibjms.TibjmsXATopicConnectionFactory)cfObject).setServerUrl(strJmsURL);
        }
        else if (org.apache.activemq.ActiveMQConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new org.apache.activemq.ActiveMQConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((org.apache.activemq.ActiveMQConnectionFactory)cfObject).setBrokerURL(strJmsURL);
        }
        else if (org.apache.activemq.ActiveMQXAConnectionFactory.class.getName().equals(strClassName)) {
            cfObject = new org.apache.activemq.ActiveMQXAConnectionFactory();
            if (strJmsURL != null && !strJmsURL.equals("")) ((org.apache.activemq.ActiveMQXAConnectionFactory)cfObject).setBrokerURL(strJmsURL);
        }
        else if (org.hornetq.jms.client.HornetQConnectionFactory.class.getName().equals(strClassName)) {

            if (strJmsURL != null && !strJmsURL.equals("")) {
                // Get the address and port
                java.util.Map<String, Object> connectionParams = new java.util.HashMap<String, Object>();
                java.net.URI uri;

                try {uri = new java.net.URI(strJmsURL);} catch (Exception exc) { throw new JMSException("Invalid URL.");}

                connectionParams.put(org.hornetq.core.remoting.impl.netty.TransportConstants.HOST_PROP_NAME, uri.getHost());
                connectionParams.put(org.hornetq.core.remoting.impl.netty.TransportConstants.PORT_PROP_NAME, uri.getPort());

                org.hornetq.api.core.TransportConfiguration transportConfiguration =
                     new org.hornetq.api.core.TransportConfiguration(org.hornetq.core.remoting.impl.netty.NettyConnectorFactory.class.getName(), connectionParams);

                cfObject = org.hornetq.api.jms.HornetQJMSClient.createConnectionFactoryWithHA(JMSFactoryType.CF, transportConfiguration);
            }
            else {
                // default host: localhost
                // default port: 5445
                org.hornetq.api.core.TransportConfiguration transportConfiguration =
                     new org.hornetq.api.core.TransportConfiguration(org.hornetq.core.remoting.impl.netty.NettyConnectorFactory.class.getName());

                cfObject = org.hornetq.api.jms.HornetQJMSClient.createConnectionFactoryWithHA(JMSFactoryType.CF, transportConfiguration);
            }
        }
        else if (org.hornetq.jms.client.HornetQXAConnectionFactory.class.getName().equals(strClassName)) {

            if (strJmsURL != null && !strJmsURL.equals("")) {
                // Get the address and port
                java.util.Map<String, Object> connectionParams = new java.util.HashMap<String, Object>();
                java.net.URI uri;

                try {uri = new java.net.URI(strJmsURL);} catch (Exception exc) { throw new JMSException("Invalid URL.");}

                connectionParams.put(org.hornetq.core.remoting.impl.netty.TransportConstants.HOST_PROP_NAME, uri.getHost());
                connectionParams.put(org.hornetq.core.remoting.impl.netty.TransportConstants.PORT_PROP_NAME, uri.getPort());

                org.hornetq.api.core.TransportConfiguration transportConfiguration =
                     new org.hornetq.api.core.TransportConfiguration(org.hornetq.core.remoting.impl.netty.NettyConnectorFactory.class.getName(), connectionParams);

                cfObject = org.hornetq.api.jms.HornetQJMSClient.createConnectionFactoryWithHA(JMSFactoryType.XA_CF, transportConfiguration);
            }
            else {
                // default host: localhost
                // default port: 5445
                org.hornetq.api.core.TransportConfiguration transportConfiguration =
                     new org.hornetq.api.core.TransportConfiguration(org.hornetq.core.remoting.impl.netty.NettyConnectorFactory.class.getName());

                cfObject = org.hornetq.api.jms.HornetQJMSClient.createConnectionFactoryWithHA(JMSFactoryType.XA_CF, transportConfiguration);
            }
        }

        ConnectionHelper.jndiCreateRefObject(strContextName, cfObject);
    }
}
