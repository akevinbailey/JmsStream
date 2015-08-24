/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util;

import com.tibco.util.xa.TransactionManagerWrapper;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.transaction.Transaction;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * Title:        JmsStreamClose<p>
 * Description:  This is class hooks into the JVM and is run before the JVM exits.<p>
 * @author A. Kevin Bailey
 * @version 2.7.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "SameParameterValue", "WeakerAccess"})
public final class JmsStreamClose extends Thread
{
    public final static short XML_TYPE = 0;
    public final static short TEXT_TYPE = 1;

    private final FileOutputStream _os;
    private final ZipOutputStream _zipos;
    private final FileOutputStream _cbos;
    private final short _intType;

    private Connection _jmsConnection = null;
    private Session _jmsSession = null;
    private TransactionManagerWrapper _tmwTrans = null;
    private Transaction _xaTrans = null;

    private boolean _blnCommitTrans = false;

    /**
     * JmsStreamClose Class.
     * @param osFile      The JmsStream save file FileOutputStream.
     * @param intType     The type of file XML_TYPE or TEXT_TYPE.  Default is TEXT_TYPE
     * @param osZipFile   The ZipOutputStream.
     * @param osCsvFile   The CSV FileOutputStream.
     */
    public JmsStreamClose(FileOutputStream osFile, short intType, ZipOutputStream osZipFile, FileOutputStream osCsvFile)
    {
        _os = osFile;
        _zipos = osZipFile;
        _cbos = osCsvFile;
        _intType = intType;
    }

    public void setTransactionManagerWrapper(TransactionManagerWrapper tmwTrans)
    {
        _tmwTrans = tmwTrans;
    }

    public void setXaTransaction(Transaction xaTrans, boolean blnCommitTrans)
    {
        _xaTrans = xaTrans;
        _blnCommitTrans = blnCommitTrans;
    }

    public void setJmsSession(Session jmsSession, boolean blnCommitTrans)
    {
        _jmsSession = jmsSession;
        _blnCommitTrans = blnCommitTrans;
    }

    public void setJmsConnection(Connection jmsConnection)
    {
        _jmsConnection = jmsConnection;
    }

    public void run()
    {
        // If there is an XA transaction commit or rollback.
        if (_xaTrans != null){
            try {
                if (_xaTrans.getStatus() == javax.transaction.Status.STATUS_ACTIVE) {
                    if (_tmwTrans == null) {
                        // If for some reason the thread kills the TransactionManager we can still commit the Transaction.
                        if (_blnCommitTrans) _xaTrans.commit();
                        else _xaTrans.rollback();
                    }
                    else {
                        _tmwTrans.resume(_xaTrans);
                        if (_blnCommitTrans) _tmwTrans.commit();
                        else _tmwTrans.rollback();
                    }
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        // If there is a JMS transaction commit or rollback.
        if (_jmsSession != null){
            try {
                if (_jmsSession.getTransacted()) {
                    if (_blnCommitTrans) _jmsSession.commit();
                    else _jmsSession.rollback();
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        // Close the active JMS Connection
        try {
            if (_jmsConnection != null) {
                _jmsConnection.close();
            }
        }
        catch (JMSException je) {
            je.printStackTrace();
        }

        this.isInterrupted();

        // If there is an open CSV stream then close it.
        if (_cbos != null && _cbos.getChannel().isOpen()) {
            try {
                _cbos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        // If there is an open JmsStream save file stream, close it.
        if (_os != null && _os.getChannel().isOpen()) {
            // And the open output stream is a ZIP stream then close it!
            if (_zipos != null) {
                try {
                    if (_intType == XML_TYPE) _zipos.write("</messages>".getBytes());
                    _zipos.closeEntry();
                    _zipos.close();
                    _os.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                // Or close a normal file stream
                try {
                    if (_intType == XML_TYPE) _os.write("</messages>".getBytes());
                    _os.close();
                }
                catch (IOException e) {
                   // ignore write exceptions
                   // e.printStackTrace();
                }
                try {
                    _os.close();
                }
                catch (IOException e) {
                   e.printStackTrace();
                }
            }
        }
    }
}
