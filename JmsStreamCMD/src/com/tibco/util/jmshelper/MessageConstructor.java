/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.jmshelper;

import com.tibco.util.structs.MessageStruct;

import javax.jms.Session;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "unchecked", "UnusedDeclaration", "ProhibitedExceptionDeclared", "ProhibitedExceptionThrown", "NestedTryStatement", "unused"})
public final class MessageConstructor
{
    private MessageFile _mfMsgFile;
    private final Session _jmsSession;

    // Global variables for XML parsing
    private String _strBuffer = "";


    public MessageConstructor(Session jmsSession)
    {
        _jmsSession = jmsSession;
    }

    public final void characters(char ch[], int start, int length)
    {
        for (int i = 0; i < length ; i++) {
            _strBuffer += ch[i + start];
        }
    }

    /**
     * Construct the message array for an InputStream.
     *
     * @param rafMsgFile    File RandomAccessFile.
     * @param strEncoding   Text encoding.
     * @return              An array of com.tibco.util.structs.MessageStruct objects
     * @exception Exception An exception.
     */
    public final MessageStruct[] getMessages(RandomAccessFile rafMsgFile, String strEncoding) throws Exception
    {
        _mfMsgFile = new MessageFile(rafMsgFile, strEncoding);
        return createJmsMsgArray();
    }

    /**
     * Construct the message array for an InputStream.
     *
     * @param isMsgFile     File InputStream.
     * @param strEncoding   Text encoding.
     * @return              An array of com.tibco.util.structs.MessageStruct objects
     * @exception Exception An exception.
     */
    public final MessageStruct[] getMessages(InputStream isMsgFile, String strEncoding) throws Exception
    {
        _mfMsgFile = new MessageFile(isMsgFile, strEncoding);

        return createJmsMsgArray();
    }

    /**
     * Create the messages array.
     *
     * @return An array of com.tibco.util.structs.MessageStruct objects
     * @exception Exception An exception.
     */
    private MessageStruct[] createJmsMsgArray() throws Exception
    {
        ArrayList<MessageStruct> alMessages;

        alMessages = _mfMsgFile.getMessageArray(_jmsSession);

        // Convert ArrayList to MessageStruct[]
        MessageStruct[] msgStructs = new MessageStruct[alMessages.size()];
        for (int i=0; i < msgStructs.length; i++) msgStructs[i] = alMessages.get(i);

        return msgStructs;
    }
}