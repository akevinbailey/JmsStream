/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.jmshelper;

import com.tibco.util.structs.MessageStruct;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.3
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "FieldCanBeLocal", "CanBeFinal", "unchecked", "UnusedDeclaration", "ProhibitedExceptionDeclared", "ProhibitedExceptionThrown", "NestedTryStatement", "ConstantConditions", "unused"})
public final class MessageFile
{
    //TODO:  This class need to be rewritten to use a custom Exception class and to get rid of the nested tries.
    @SuppressWarnings("unused")
    public enum MsgType { TextMessage, MapMessage, BytesMessage, ObjectMessage, StreamMessage }
    @SuppressWarnings("unused")
    public enum DestType { Queue, Topic, Generic, XA_Queue, XA_Topic, XA_Generic }
    @SuppressWarnings("unused")
    public enum DeliveryType {
        NON_PERSISTENT(1), PERSISTENT(2);
        private int intCode;
        public int getCode() { return intCode; }
        DeliveryType(int code) { intCode = code; }

        // For reverse look up.
        private static final Map<Integer,DeliveryType> lookup = new HashMap<Integer,DeliveryType>();
        static {for(DeliveryType dt : EnumSet.allOf(DeliveryType.class)) lookup.put(dt.getCode(), dt);}
        public static DeliveryType valueOf(int code) {return lookup.get(code);}
    }

    private RandomAccessFile _rafMsgFile = null;
    private InputStream _istMsgData = null;
    // And ArrayList that contains the individual messages for reading and writing.
    private ArrayList<ByteBuffer> _alMessages = new ArrayList<ByteBuffer>();
    private int _intCurrentMsgIndex = 0;

    // JMS Properties
    private Integer _cintJmsPriority = null; // Must be Integer Class because the value could be null.
    private long   _intJmsExpiration = 0L; // Expiration in milliseconds
    private long   _intSleepTime = 0L; // Time to wait between messages (if -timed parameter was enabled)
    private long   _intJmsTimestamp = 0L;
    private boolean _blnCommitTrans = false;
    private DeliveryType _enmDeliveryMode = DeliveryType.NON_PERSISTENT;
    private MsgType _enmMsgType = null; // Message Type
    private DestType _enmDestType = null; // Destination Type
    private String _strJmsMsgId = null;
    private String _strJmsServerTimestamp = null;
    private String _strOriginationTime = null;
    private String _strJmsMsgExpiration = null;
    private String _strMessageSelector = null;
    private String _strReceiveTime = null;
    private String _strJmsDest = null; // JMS Destination
    private String _strJmsReplyTo = null;
    private String _strJmsCorrId = null;
    private String _strJmsType = null;
    private String _strBody = null;
    private final String _strEncoding;
    private ArrayList<MsgPropStruct> _alMsgProp = null;
    private ArrayList<MsgPropStruct> _alMapMsgRoot = null;

    /**
     * Parse the messages from the file.
     *
     * @param fileURI   FQN file name of the JmsStream Message File.
     * @param strEncoding   The file type encoding:  US-ASCII, ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16
     * @throws IOException    Throws and exception.
     */
    public MessageFile(String fileURI, String strEncoding) throws Exception
    {
        if (strEncoding == null) throw new Exception("NULL pointer exception (strEncoding is null).");
        _strEncoding = strEncoding;

        if (fileURI == null) throw new Exception("NULL pointer exception (fileURI is null).");
        _rafMsgFile = new RandomAccessFile(fileURI, "rwd");

        processByteData(null);
    }

    /**
     * Parse the messages from the file.
     *
     * @param rafMsgFile   RandomAccessFile pointer of the JmsStream Message File.
     * @param strEncoding   The file type encoding:  US-ASCII, ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16
     * @throws IOException    Throws and exception.
     */
    public MessageFile(RandomAccessFile rafMsgFile, String strEncoding) throws Exception
    {
        if (strEncoding == null) throw new Exception("NULL pointer exception (strEncoding is null).");
        _strEncoding = strEncoding;

        if (rafMsgFile == null) throw new Exception("NULL pointer exception (fileURI is null).");
        _rafMsgFile = rafMsgFile;
    }

    /**
     * Parse the messages from an InputStream.
     *
     * @param istMessageData   InputStream containing JmsStream Message data.
     * @param strEncoding   The file type encoding:  US-ASCII, ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16
     * @throws IOException    Throws and exception.
     */
    public MessageFile(InputStream istMessageData, String strEncoding) throws Exception
    {
        if (strEncoding == null) throw new Exception("NULL pointer exception (strEncoding is null).");
        _strEncoding = strEncoding;

        if (istMessageData == null) throw new Exception("NULL pointer exception (fileURI is null).");
        _istMsgData = istMessageData;
    }

    public ArrayList<MessageStruct> getMessageArray(Session jmsSession) throws Exception
    {
        int intByteHolder;
        byte byteHolder;
        boolean blnIsFirst = true;
        boolean blnEOF = false;
        boolean blnEOL = false;
        ByteArrayOutputStream baReadLine = new ByteArrayOutputStream();
        ByteArrayOutputStream baMessage = new ByteArrayOutputStream(500); // Start with 500 bytes for a message size.
        ArrayList<MessageStruct> alMsgStruct = new ArrayList<MessageStruct>();
        MessageStruct msgStruct;
        Message jmsMessage;

        try {
            // Read the header
            do {
                while (!blnEOF && !blnEOL) {
                    baReadLine.reset();
                    try {
                        if (_rafMsgFile != null) {
                            // Must use readByte, because readLine does not support Unicode.
                            do { // Read line.
                                byteHolder = _rafMsgFile.readByte();
                                baReadLine.write(byteHolder);
                            }  while (byteHolder != '\n');
                        }
                        else if (_istMsgData != null) {
                            // Must use readByte, because readLine does not support Unicode.
                            do { // Read line.
                                intByteHolder = _istMsgData.read();

                                // .read() will not return a value greater than 255, but -1 when EOF
                                if (intByteHolder == -1) throw new EOFException();
                                else byteHolder = (byte)intByteHolder;

                                baReadLine.write(byteHolder);
                            }  while (byteHolder != '\n');
                        }
                    }
                    catch (EOFException eof) {
                        blnEOF = true;
                    }
                    finally {
                        blnEOL = baReadLine.toString(_strEncoding).startsWith("#---------- ");
                        if (blnIsFirst || !blnEOL) baReadLine.writeTo(baMessage);
                    }
                }
                blnEOL = false;
                if (blnIsFirst) {
                    blnIsFirst = false;
                }
                else {
                    _alMessages.add(ByteBuffer.wrap(baMessage.toByteArray()));
                    readMessage();
                    _alMessages = new ArrayList<ByteBuffer>(); // ArrayList no longer needed.

                    // Create the MessageStruct and JmsMessage
                    msgStruct = new MessageStruct();
                    msgStruct.commitTrans = _blnCommitTrans;
                    msgStruct.sleepTime = _intSleepTime;

                    // Instantiate the Message Class and add the message body
                    if (_enmMsgType == MsgType.BytesMessage) {
                        jmsMessage = jmsSession.createBytesMessage();
                        ((BytesMessage)jmsMessage).writeBytes(Base64.decode(_strBody));
                    }
                    else if (_enmMsgType == MsgType.MapMessage) {
                        jmsMessage = this.createMapMessage(jmsSession, _alMapMsgRoot);
                    }
                    else if (_enmMsgType == MsgType.ObjectMessage) {
                        jmsMessage = jmsSession.createObjectMessage(true);
                    }
                    else if (_enmMsgType == MsgType.StreamMessage) {
                        jmsMessage = jmsSession.createStreamMessage();
                    }
                    else if (_enmMsgType == MsgType.TextMessage) {
                        jmsMessage = jmsSession.createTextMessage();
                        ((TextMessage)jmsMessage).setText(_strBody);
                    }
                    else
                        throw new Exception("Unsupported JMS message type");

                    // Fill in the JMS message properties
                    if (!_strJmsCorrId.equals("")) jmsMessage.setJMSCorrelationID(_strJmsCorrId);
                    jmsMessage.setJMSDeliveryMode(_enmDeliveryMode.intCode);
                    if (!_strJmsDest.equals("")) {
                        if (TopicSession.class.isInstance(jmsSession))
                            jmsMessage.setJMSDestination(jmsSession.createTopic(_strJmsDest));
                        else // Default to Queue session
                            jmsMessage.setJMSDestination(jmsSession.createQueue(_strJmsDest));
                    }
                    if (_intJmsExpiration != 0) jmsMessage.setJMSExpiration(_intJmsExpiration);
                    if (!_strJmsMsgId.equals("")) jmsMessage.setJMSMessageID(_strJmsMsgId);
                    if (_cintJmsPriority != null) jmsMessage.setJMSPriority(_cintJmsPriority);
                    if (!_strJmsReplyTo.equals("")) {
                        if (TopicSession.class.isInstance(jmsSession))
                            jmsMessage.setJMSReplyTo(jmsSession.createTopic(_strJmsReplyTo));
                        else // Default to Queue session
                            jmsMessage.setJMSReplyTo(jmsSession.createQueue(_strJmsReplyTo));
                    }
                    if (_intJmsTimestamp != 0) jmsMessage.setJMSTimestamp(_intJmsTimestamp);
                    if (!_strJmsType.equals("")) jmsMessage.setJMSType(_strJmsType);

                    // Fill in the JMS custom properties
                    for (int i = 0; i < _alMsgProp.size(); i++) {
                        if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.Boolean) {
                            jmsMessage.setBooleanProperty(_alMsgProp.get(i).getName(), Boolean.getBoolean(_alMsgProp.get(i).getValue()));
                        }
                        else if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.Byte) {
                            jmsMessage.setByteProperty(_alMsgProp.get(i).getName(), (Base64.decode(_alMsgProp.get(i).getValue())[0]));
                        }
                        else if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.Double) {
                            jmsMessage.setDoubleProperty(_alMsgProp.get(i).getName(), Double.parseDouble(_alMsgProp.get(i).getValue()));
                        }
                        else if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.Float) {
                            jmsMessage.setFloatProperty(_alMsgProp.get(i).getName(), Float.parseFloat(_alMsgProp.get(i).getValue()));
                        }
                        else if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.Integer) {
                            jmsMessage.setIntProperty(_alMsgProp.get(i).getName(), Integer.parseInt(_alMsgProp.get(i).getValue()));
                        }
                        else if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.Long) {
                            jmsMessage.setLongProperty(_alMsgProp.get(i).getName(), Long.parseLong(_alMsgProp.get(i).getValue()));
                        }
                        else if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.Short) {
                            jmsMessage.setShortProperty(_alMsgProp.get(i).getName(), Short.parseShort(_alMsgProp.get(i).getValue()));
                        }
                        else if (_alMsgProp.get(i).getType() == MsgPropStruct.ValueType.String) {
                            jmsMessage.setStringProperty(_alMsgProp.get(i).getName(), _alMsgProp.get(i).getValue());
                        }
                    }

                    msgStruct.jmsMessage = jmsMessage;
                    alMsgStruct.add(msgStruct);

                    // Reset the byte buffer for next message
                    baMessage.reset();
                    baReadLine.writeTo(baMessage);
                }
                baReadLine.reset();
            } while (!blnEOF);

            // Close file
            if (_rafMsgFile != null) _rafMsgFile.close();
        }
        catch (IOException ioe) {
            if (_rafMsgFile != null) _rafMsgFile.close();
            throw ioe;
        }

        return alMsgStruct;
    }

    public MapMessage createMapMessage(Session jmsSession, ArrayList<MsgPropStruct> alMapMsg)
    {
        MapMessage jmsMapMsg = null;

        try {
            jmsMapMsg = jmsSession.createMapMessage();

            for (int i = 0; i < alMapMsg.size(); i++) {
                if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Boolean) {
                    jmsMapMsg.setBoolean(alMapMsg.get(i).getName(), Boolean.getBoolean(alMapMsg.get(i).getValue()));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Byte) {
                    jmsMapMsg.setByte(alMapMsg.get(i).getName(), (Base64.decode(alMapMsg.get(i).getValue())[0]));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Bytes) {
                    jmsMapMsg.setBytes(alMapMsg.get(i).getName(), Base64.decode(alMapMsg.get(i).getValue()));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Char) {
                    jmsMapMsg.setChar(alMapMsg.get(i).getName(), alMapMsg.get(i).getValue().charAt(0));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Double) {
                    jmsMapMsg.setDouble(alMapMsg.get(i).getName(), Double.parseDouble(alMapMsg.get(i).getValue()));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Float) {
                    jmsMapMsg.setFloat(alMapMsg.get(i).getName(), Float.parseFloat(alMapMsg.get(i).getValue()));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Integer) {
                    jmsMapMsg.setInt(alMapMsg.get(i).getName(), Integer.parseInt(alMapMsg.get(i).getValue()));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Long) {
                    jmsMapMsg.setLong(alMapMsg.get(i).getName(), Long.parseLong(alMapMsg.get(i).getValue()));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.Short) {
                    jmsMapMsg.setShort(alMapMsg.get(i).getName(), Short.parseShort(alMapMsg.get(i).getValue()));
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.String) {
                    jmsMapMsg.setString(alMapMsg.get(i).getName(), alMapMsg.get(i).getValue());
                }
                else if (alMapMsg.get(i).getType() == MsgPropStruct.ValueType.ArrayList) {
                    jmsMapMsg.setBooleanProperty("JMS_TIBCO_MSG_EXT",true); // This is a proprietary TibjmsMapMessage
                    jmsMapMsg.setObject(alMapMsg.get(i).getName(), createMapMessage(jmsSession, alMapMsg.get(i).getArray()));
                }
                else
                    throw new JMSException("Invalid MapMessage type.");
            }
        }
        catch (JMSException jex) {
            jex.printStackTrace();
        }

        return jmsMapMsg;
    }

    private void processByteData(InputStream istMsgData) throws Exception
    {
        int intByteHolder;
        byte byteHolder;
        boolean blnIsFirst = true;
        boolean blnEOF = false;
        boolean blnEOL = false;
        ByteArrayOutputStream baReadLine = new ByteArrayOutputStream();
        ByteArrayOutputStream baMessage = new ByteArrayOutputStream(500); // Start with 500 bytes for a message size.

        try {
            // Read the header
            do {
                while (!blnEOF && !blnEOL) {
                    baReadLine.reset();
                    try {
                        if (_rafMsgFile != null) {
                            // Must use readByte, because readLine does not support Unicode.
                            do { // Read line.
                                byteHolder = _rafMsgFile.readByte();
                                baReadLine.write(byteHolder);
                            }  while (byteHolder != '\n');
                        }
                        else if (istMsgData != null) {
                            // Must use readByte, because readLine does not support Unicode.
                            do { // Read line.
                                intByteHolder = istMsgData.read();

                                // .read() will not return a value greater than 255, but -1 when EOF
                                if (intByteHolder == -1) throw new EOFException();
                                else byteHolder = (byte)intByteHolder;

                                baReadLine.write(byteHolder);
                            }  while (byteHolder != '\n');
                        }
                    }
                    catch (EOFException eof) {
                        blnEOF = true;
                    }
                    finally {
                        blnEOL = baReadLine.toString(_strEncoding).startsWith("#---------- ");
                        if (blnIsFirst || !blnEOL) baReadLine.writeTo(baMessage);
                    }
                }
                blnEOL = false;
                if (blnIsFirst) {
                    blnIsFirst = false;
                }
                else {
                    _alMessages.add(ByteBuffer.wrap(baMessage.toByteArray()));
                    baMessage.reset();
                    baReadLine.writeTo(baMessage);
                }
                baReadLine.reset();
            } while (!blnEOF);

            // Go back to the first of the file.
            _rafMsgFile.seek(0L);

            // Read First Message
            readMessage();
        }
        catch (IOException ioe) {
            _rafMsgFile.close();
            throw ioe;
        }

    }

    public void closeFile() throws IOException
    {
        _rafMsgFile.close();
    }

    private static String appendString(int tab, String strIn)
    {
        for (int i = 0; i < tab * 4; i++) strIn = " " + strIn;
        strIn += "\n";
        return strIn;
    }

    private void parseMsgInfo(byte[] byteXML) throws Exception
    {
        DocumentBuilderFactory dbfxFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbxBuilder = dbfxFactory.newDocumentBuilder();
        Document docXml = dbxBuilder.parse(new ByteArrayInputStream(byteXML));
        NodeList nlElements;
        Element eleWait;
        Element eleCommitTrans;
        Element eleMessage;
        Element eleHeader;
        Element eleProperties;
        Element eleProperty;

        nlElements = docXml.getElementsByTagName("wait");
        if (nlElements.getLength() == 1) {
            eleWait = (Element)nlElements.item(0);
            _intSleepTime = Long.parseLong(eleWait.getTextContent());

        }
        else _intSleepTime = 0;

        nlElements = docXml.getElementsByTagName("commitTrans");
        if (nlElements.getLength() == 1) {
            eleCommitTrans = (Element)nlElements.item(0);
            _blnCommitTrans = Boolean.parseBoolean(eleCommitTrans.getTextContent());
        }
        else _blnCommitTrans = false;

        nlElements = docXml.getElementsByTagName("message");
        if (nlElements.getLength() == 1) {
            eleMessage = (Element)nlElements.item(0);
            if (eleMessage.getAttribute("type").equals(""))
                throw new Exception("Missing \"Message Type\" in message file.");
            else
                _enmMsgType = MsgType.valueOf(eleMessage.getAttribute("type").trim());
            _strMessageSelector = eleMessage.getAttribute("messageSelector").trim();
            _strOriginationTime = eleMessage.getAttribute("originationTimestamp").trim();
            _strReceiveTime = eleMessage.getAttribute("receiveTime").trim();
            _strJmsServerTimestamp = eleMessage.getAttribute("jmsServerTimestamp").trim();
            _strJmsMsgExpiration = eleMessage.getAttribute("jmsMsgExpiration").trim();
        }
        else {
            throw new SAXException("Parse Error:  One <message> element is required.");
        }
        nlElements = eleMessage.getElementsByTagName("header");
        if (nlElements.getLength() == 1) {
            eleHeader = (Element)nlElements.item(0);
            _strJmsMsgId = eleHeader.getAttribute("JMSMessageID").trim();
            _strJmsDest = eleHeader.getAttribute("JMSDestination").trim();
            _enmDestType = eleHeader.getAttribute("JMSDestinationType").equals("") ? DestType.Generic : DestType.valueOf(eleHeader.getAttribute("JMSDestinationType").trim());
            _strJmsReplyTo = eleHeader.getAttribute("JMSReplyTo").trim();
            _strJmsCorrId = eleHeader.getAttribute("JMSCorrelationID").trim();
            _enmDeliveryMode = eleHeader.getAttribute("JMSDeliveryMode").equals("") ? DeliveryType.NON_PERSISTENT : DeliveryType.valueOf(Integer.parseInt(eleHeader.getAttribute("JMSDeliveryMode").trim()));
            _cintJmsPriority = eleHeader.getAttribute("JMSPriority").equals("") ? null : Integer.parseInt(eleHeader.getAttribute("JMSPriority").trim());
            _strJmsType = eleHeader.getAttribute("JMSType").trim();
            _intJmsExpiration = eleHeader.getAttribute("JMSExpiration").equals("") ? 0 : Long.parseLong(eleHeader.getAttribute("JMSExpiration").trim());
            _intJmsTimestamp = eleHeader.getAttribute("JMSTimestamp").equals("") ? 0 : Long.parseLong(eleHeader.getAttribute("JMSTimestamp").trim());
        }
        else {
            throw new SAXException("Parse Error:  One <header> element is required.");
        }
        nlElements = eleMessage.getElementsByTagName("properties");
        if (nlElements.getLength() == 1) {
            _alMsgProp = new ArrayList<MsgPropStruct>();
            eleProperties = (Element)nlElements.item(0);
            nlElements = eleProperties.getElementsByTagName("property");
            for (int i = 0; i < nlElements.getLength(); i++) {
                eleProperty = (Element)nlElements.item(i);
                _alMsgProp.add(new MsgPropStruct(eleProperty.getAttribute("name").trim()
                                                , eleProperty.getAttribute("type").trim()
                                                , eleProperty.getTextContent().trim()));
            }
        }
    }

    /**
     * The parseMapMsg method does not support the TibMapMessage multi-level map messages.
     *
     * @param byteXML       parse MapMessage XML
     * @throws Exception    throws an exception
     */
    private void parseMapMsg(byte[] byteXML) throws Exception
    {
        DocumentBuilderFactory dbfxFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbxBuilder = dbfxFactory.newDocumentBuilder();
        Document docXml = dbxBuilder.parse(new ByteArrayInputStream(byteXML));
        NodeList nlElements;

        nlElements = docXml.getChildNodes();
        _alMapMsgRoot = parseElements(nlElements);
    }

    /**
     * The parse Elements method creates a nested ArrayList<MsgPropStruct> from a XML NodeList
     *
     * @param nlElements    a XML NodeList
     * @return              an ArrayList<MsgPropStruct>
     */
    private ArrayList<MsgPropStruct> parseElements(NodeList nlElements)
    {
        Element eleNode;
        ArrayList<MsgPropStruct> alMapMsg = new ArrayList<MsgPropStruct>();

        for (int i = 0; i < nlElements.getLength(); i++) {
            if (nlElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                eleNode = (Element)nlElements.item(i);
                if (eleNode.getNodeName().equals("node")) {
                    alMapMsg.add(new MsgPropStruct(eleNode.getAttribute("name").trim(), eleNode.getAttribute("type").trim(),
                            eleNode.getTextContent()));
                }
                else if (eleNode.getNodeName().equals("MapMessage") && eleNode.getParentNode().getNodeType() != Node.ELEMENT_NODE) { // Root Node
                    alMapMsg = parseElements(eleNode.getChildNodes());
                }
                else if (eleNode.getNodeName().equals("MapMessage") && !eleNode.getAttribute("name").equals("")) {
                    alMapMsg.add(new MsgPropStruct(eleNode.getAttribute("name").trim(), parseElements(eleNode.getChildNodes())));
                }
            }
        }

        return alMapMsg;
    }

    private static String renderMapMsg(int intTabSpace, ArrayList<MsgPropStruct> alMapMsg, String strName)
    {
        String strXmlMap;
        if (strName == null || strName.equals(""))
            strXmlMap = appendString(intTabSpace, "<MapMessage>");
        else
            strXmlMap = appendString(intTabSpace, "<MapMessage name=\"" + strName + "\">");

        try {
            for (MsgPropStruct a_alMapMsg : alMapMsg) {
                if (a_alMapMsg.getType() == MsgPropStruct.ValueType.ArrayList) {
                    strXmlMap += renderMapMsg(intTabSpace + 1, a_alMapMsg.getArray(), FormatHelper.translateXML(a_alMapMsg.getName()));
                }
                else {
                    strXmlMap += appendString(intTabSpace + 1, "<node name=\"" + FormatHelper.translateXML(a_alMapMsg.getName()) + "\" type=\"" + a_alMapMsg.getType() + "\">" + FormatHelper.translateXML(a_alMapMsg.getValue()) + "</node>");
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

    private void readMessage() throws Exception
    {
        byte byteHolder;
        ByteArrayOutputStream baReadLine = new ByteArrayOutputStream();
        ByteArrayOutputStream baXmlHeader = new ByteArrayOutputStream();

        // Read Just the the BodyLength of a single message.
        if (_alMessages.size() == 0) return;
        while (!(baReadLine.toString(_strEncoding).startsWith("#---------- "))) {
            baReadLine.reset();
            // Must use readByte, because readLine does not support Unicode.
             do{ // Read line.
                byteHolder = _alMessages.get(_intCurrentMsgIndex).get();
                if (byteHolder != '\r') baReadLine.write(byteHolder);
            }  while (byteHolder != '\n');
        }
        if (baReadLine.size() < 1) throw new Exception("MessageFile.class Error:  JmsStream Message File is not in the correct format.");

        baReadLine.reset(); // Clear line information.

        // Read the Message Info
        while (!(baReadLine.toString(_strEncoding).startsWith("BodyLength"))) {
            baXmlHeader.write(baReadLine.toByteArray());
            baReadLine.reset(); // Clear line information.
            // Must use readByte, because readLine does not support Unicode.
            do{ // Read line.
                byteHolder = _alMessages.get(_intCurrentMsgIndex).get();
                if (byteHolder != '\r' && byteHolder != '\n') baReadLine.write(byteHolder);
            }  while (byteHolder != '\n');
        }
        if (baReadLine.size() < 11) throw new Exception("MessageFile.class Error:  JmsStream Message File is not in the correct format.");

        // Read the Message Body
        int intByteSize = Integer.parseInt(baReadLine.toString(_strEncoding).substring(11));
        byte[] byteText = new byte[intByteSize];
        int i = 0;
        try {
            while (i < intByteSize)
                byteText[i++] = _alMessages.get(_intCurrentMsgIndex).get();
        }
        catch (Exception e) {
            throw new Exception("File message data has inconsistencies.  Message of length " + intByteSize + " stopped at index " + i + ".");
        }
        // Parse MSG_INFO
        this.parseMsgInfo(baXmlHeader.toByteArray());

        switch (_enmMsgType) {
            case BytesMessage:  // Set Base64 for BytesMessage
                _strBody = new String(byteText); // No encoding necessary because Base64 in ANSI.
                break;
            case MapMessage: // Parse MapMessage file structure (XML) and add them into message
                this.parseMapMsg(byteText);
                break;
            case ObjectMessage:
                //  TODO: Add ObjectMessage handler
                break;
            case StreamMessage:
                // TODO: Add StreamMessage handler
                break;
            case TextMessage:
                _strBody = new String(byteText, _strEncoding);
                break;
            default:
                // do nothing
        }
    }

    public void updateMessage() // Save any updated message content.
    {
        try {
            _alMessages.set(_intCurrentMsgIndex, ByteBuffer.wrap(renderXml().getBytes(_strEncoding)));
        }
        catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

    }

    public String renderXml()
    {
        String strTextBuffer;
        String strXmlBuffer;
        String strHeader;

        strTextBuffer = "#---------- #" + (_intCurrentMsgIndex + 1)
                         + " : " + _strJmsMsgId
                         + " ----------#\n";
        strTextBuffer += "<MSG_INFO>" + "\n";
        // If we record with timed statistics (-time), put the wait time in the XML file
        if (_intSleepTime > 0)
            strTextBuffer += appendString(1, "<wait>" + (_intSleepTime) + "</wait>");
        if (_blnCommitTrans)
            strTextBuffer += appendString(1, "<commitTrans>true</commitTrans>");

        strXmlBuffer = appendString(1, "<message type=\"" + _enmMsgType.toString()
                                        + "\" messageSelector=\"" + _strMessageSelector + "\""
                                        + (_strOriginationTime == null ? "" : " originationTimestamp=\"" + _strOriginationTime +"\"")
                                        + (_strReceiveTime == null ? "" : " receiveTime=\"" + _strReceiveTime + "\"")
                                        + (_strJmsServerTimestamp == null ? "" : " jmsServerTimestamp=\"" + _strJmsServerTimestamp + "\"")
                                        + (_strJmsMsgExpiration == null ? "" : " jmsMsgExpiration=\"" + _strJmsMsgExpiration + "\"")
                                        + ">");

        strHeader = "<header ";
        if (_strJmsMsgId != null && !_strJmsMsgId.equals("")) strHeader += "JMSMessageID=\"" + _strJmsMsgId + "\" ";
        if (_strJmsDest != null && !_strJmsDest.equals("")) strHeader += "JMSDestination=\"" + _strJmsDest + "\" ";
        if (_enmDestType != null) strHeader += "JMSDestinationType=\"" + _enmDestType.toString() + "\" ";
        if (_strJmsReplyTo != null && !_strJmsReplyTo.equals("")) strHeader += "JMSReplyTo=\"" + _strJmsReplyTo + "\" ";
        if (_strJmsCorrId != null && !_strJmsCorrId.equals("")) strHeader += "JMSCorrelationID=\"" + _strJmsCorrId + "\" ";
        if (_enmDeliveryMode != null) strHeader += "JMSDeliveryMode=\"" + _enmDeliveryMode.getCode() + "\" ";
        if (_cintJmsPriority != null) strHeader += "JMSPriority=\"" + _cintJmsPriority + "\" ";
        if (_strJmsType != null && !_strJmsType.equals("")) strHeader += "JMSType=\"" + _strJmsType + "\" ";
        if (_intJmsExpiration > 0) strHeader += "JMSExpiration=\"" + _intJmsExpiration + "\" ";
        if (_intJmsTimestamp > 0) strHeader += "JMSTimestamp=\"" + _intJmsTimestamp + "\"";
        strHeader += "/>";
        strXmlBuffer += appendString (2, strHeader);

        // PROPERTIES section of jmsMessage
        strXmlBuffer += appendString(2, "<properties>");
        if (_alMsgProp != null) {
            for (MsgPropStruct a_alMsgProp : _alMsgProp) {
                strXmlBuffer += appendString(3, "<property name=\"" + FormatHelper.translateXML(a_alMsgProp.getName()) + "\" type=\"" + a_alMsgProp.getType() + "\">" + FormatHelper.translateXML(a_alMsgProp.getValue()) + "</property>");
            }
        }
        strXmlBuffer += appendString(2, "</properties>");
        strXmlBuffer += appendString(1, "</message>");

        strTextBuffer += strXmlBuffer;
        strTextBuffer += "</MSG_INFO>";

        try {
            switch (_enmMsgType) {
                case BytesMessage:
                    if (_strBody != null)
                        strTextBuffer += "\nBodyLength=" + _strBody.trim().getBytes(_strEncoding).length + "\n" + _strBody.trim();
                    break;
                case MapMessage:
                    if (_alMapMsgRoot != null) {
                        String strMapMsgXml = renderMapMsg(1, _alMapMsgRoot, null);
                        strTextBuffer += "\nBodyLength=" + strMapMsgXml.getBytes(_strEncoding).length + "\n" + strMapMsgXml;
                    }
                    break;
                case ObjectMessage:
                    //  TODO: Add ObjectMessage handler
                    break;
                case StreamMessage:
                    // TODO: Add StreamMessage handler
                    break;
                case TextMessage:
                    if (_strBody != null)
                        strTextBuffer += "\nBodyLength=" + _strBody.getBytes(_strEncoding).length + "\n" + _strBody;
                    break;
                default:
                    // do nothing
            }
        }
        catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        return strTextBuffer;
    }

    public Integer getJmsPriority() { return _cintJmsPriority; }
    public long getJmsExpiration() { return  _intJmsExpiration; } // Expiration in milliseconds
    public long getSleepTime() { return _intSleepTime; } // Time to wait between messages (if -timed parameter was enabled)
    public long getJmsTimestamp() { return _intJmsTimestamp; }
    public boolean getCommitTrans() { return _blnCommitTrans; }
    public DeliveryType getDeliveryMode() { return _enmDeliveryMode; }
    public MsgType getMessageType() { return _enmMsgType; } // Message Type
    public DestType getDestinationType() { return _enmDestType; } // Destination Type
    public String getJmsMessageID() { return _strJmsMsgId; }
    public String getJmsServerTimestamp() { return _strJmsServerTimestamp; }
    public String getOriginationTime() { return _strOriginationTime; }
    public String getJmsMsgExpiration() { return _strJmsMsgExpiration; }
    public String getMessageSelector() { return _strMessageSelector; }
    public String getReceiveTime() { return _strReceiveTime; }
    public String getJmsDestination() { return _strJmsDest; } // JMS Destination
    public String getJmsReplyToDest() { return _strJmsReplyTo; }
    public String getJmsCorrelationId() { return _strJmsCorrId; }
    public String getJmsType() { return _strJmsType; }
    public String getTextBody() { return _enmMsgType == MsgType.TextMessage ? _strBody : null; }
    public String getBase64Body() { return _enmMsgType == MsgType.BytesMessage ? _strBody : null; }
    public ArrayList<MsgPropStruct> getMsgProperties() { return _alMsgProp; }
    public ArrayList<MsgPropStruct> getMapMessageBody() { return _enmMsgType == MsgType.MapMessage ? _alMapMsgRoot : null; }
    public int getMessageCount() { return _alMessages.size(); }
    public int getCurrentMsgIndex() { return _intCurrentMsgIndex; }

    public void setJmsPriority(Integer JmsPriority) { _cintJmsPriority = JmsPriority; }
    public void setJmsExpiration(long JmsExpiration) { _intJmsExpiration = JmsExpiration; } // Expiration in milliseconds
    public void setSleepTime(long SleepTime) { _intSleepTime = SleepTime; } // Time to wait between messages (if -timed parameter was enabled)
    public void setCommitTrans(boolean CommitTrans) { _blnCommitTrans = CommitTrans; }
    public void setDeliveryMode(DeliveryType enmDeliveryMode) { _enmDeliveryMode = enmDeliveryMode; }
    public void setMessageType(MsgType enumMsgType) { _enmMsgType = enumMsgType; } // Message Type
    public void setDestinationType(DestType enmDestType) { _enmDestType = enmDestType; } // Destination Type
    public void setJmsDestination(String JmsDestination) { _strJmsDest = JmsDestination; } // JMS Destination
    public void setJmsReplyToDest(String JmsReplyToDest) { _strJmsReplyTo = JmsReplyToDest; }
    public void setJmsCorrelationId(String JmsCorrelationId) { _strJmsCorrId = JmsCorrelationId; }
    public void setJmsType(String JmsType) { _strJmsType = JmsType; }
    public void setTextBody(String TextBody) { _strBody = _enmMsgType == MsgType.TextMessage ? TextBody : null; }
    public void setBase64Body(String Base64Body) { _strBody = _enmMsgType == MsgType.BytesMessage ? Base64Body : null; }
    public void setMsgProperties(ArrayList<MsgPropStruct> alMsgProp) { _alMsgProp = alMsgProp; }
    public void setMapMessageBody(ArrayList<MsgPropStruct> alMapMsg) { _alMapMsgRoot = _enmMsgType == MsgType.MapMessage ? alMapMsg : null; }
    
    public void nextMsg() throws Exception
    {
        if (_intCurrentMsgIndex + 1 < getMessageCount()) {
            updateMessage(); // Save any updated message content.
            _alMessages.get(++_intCurrentMsgIndex).position(0);
            readMessage();
        }
    }

    public void previousMsg() throws Exception
    {
        if (_intCurrentMsgIndex - 1 >= 0) {
            updateMessage(); // Save any updated message content.
            _alMessages.get(--_intCurrentMsgIndex).position(0);
            readMessage();
        }
    }

    public void addNewMsg() throws Exception
    {
        _strJmsMsgId = "";
        _strJmsServerTimestamp = "";
        _strOriginationTime = "";
        _strJmsMsgExpiration = "";
        _strMessageSelector = "";

        setJmsPriority(null);
        setJmsExpiration(0);
        setSleepTime(0);
        setCommitTrans(false);
        setDeliveryMode(DeliveryType.NON_PERSISTENT);
        setMessageType(MsgType.TextMessage);
        setDestinationType(DestType.Generic);
        setJmsDestination("");
        setJmsReplyToDest("");
        setJmsCorrelationId("");
        setJmsType("");
        setTextBody("");
        setBase64Body(null);
        setMsgProperties(null);
        setMapMessageBody(null);

        if (_alMessages.isEmpty())
            _alMessages.add(ByteBuffer.wrap(renderXml().getBytes(_strEncoding)));
        else
            _alMessages.add(++_intCurrentMsgIndex, ByteBuffer.wrap(renderXml().getBytes(_strEncoding)));
    }

    public void deleteMsg() throws Exception
    {
        if (_intCurrentMsgIndex > -1) {
            _alMessages.remove(_intCurrentMsgIndex);
            if (_intCurrentMsgIndex == _alMessages.size()) _intCurrentMsgIndex--;
        }
    }

    public void saveMsg() throws IOException
    {
        updateMessage();
        _rafMsgFile.seek(0);
        _rafMsgFile.setLength(0);
        for (ByteBuffer alMessage : _alMessages) {
            _rafMsgFile.write(alMessage.array());
            _rafMsgFile.write("\n".getBytes(_strEncoding));
        }

        _rafMsgFile.close();
    }
}
