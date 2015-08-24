/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.xa;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.rmi.server.UID;

/*  Transaction Status from javax.transaction.Status
    STATUS_ACTIVE = 0
    STATUS_MARKED_ROLLBACK = 1
    STATUS_PREPARED = 2
    STATUS_COMMITTED = 3
    STATUS_ROLLEDBACK = 4
    STATUS_UNKNOWN = 5
    STATUS_NO_TRANSACTION = 6
    STATUS_PREPARING = 7
    STATUS_COMMITTING = 8
    STATUS_ROLLING_BACK = 9
*/

/**
 * Title:        NoMgrTransaction<p>
 * Description:  This class simulates an XA transaction manager.<p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "WeakerAccess"})
public class NoMgrTransaction implements Transaction
{
    private Xid _xid = null;
    private XAResource _xaResource = null;
    private int _intStatus = 0;

    /**
     * Because NoMgrTransaction implements Transaction a constructor is not called;
     * therefore, you must call the init() method after instantiation of NoMgrTransaction.
     */
    public void init()
    {
        int intFormatID = 0;
        UID uid = new java.rmi.server.UID();
        _xid = new CreateXid(intFormatID, uid.toString(), com.tibco.util.JmsStream.APP_NAME);
    }

    public void commit() throws SystemException
    {
        try {
            _xaResource.end(_xid, XAResource.TMSUCCESS);
            _intStatus = javax.transaction.Status.STATUS_PREPARING;
            
            _xaResource.prepare(_xid);
            _intStatus = javax.transaction.Status.STATUS_PREPARED;

            _xaResource.commit(_xid, false);
            _intStatus = javax.transaction.Status.STATUS_COMMITTED;
        }
        catch (XAException xe) {throw new SystemException(xe.getMessage());}
    }

    public void rollback() throws SystemException
    {
        try {
            _xaResource.end(_xid, XAResource.TMSUCCESS);
            _intStatus = javax.transaction.Status.STATUS_PREPARING;

            _xaResource.prepare(_xid);
            _intStatus = javax.transaction.Status.STATUS_PREPARED;

            _xaResource.rollback(_xid);
            _intStatus = javax.transaction.Status.STATUS_ROLLEDBACK;            
        }
        catch (XAException xe) {throw new SystemException(xe.getMessage());}
    }

    public boolean enlistResource(XAResource xaResource) throws SystemException
    {
        _xaResource = xaResource;
        try {
            _xaResource.start(_xid, XAResource.TMNOFLAGS);
            _xaResource.setTransactionTimeout(0);
        }
        catch (XAException xe) {throw new SystemException(xe.getMessage());}
        _intStatus = javax.transaction.Status.STATUS_ACTIVE;
        return true;
    }

    public boolean delistResource(XAResource xaResource, int x) throws SystemException
    {
        try {_xaResource.end(_xid, XAResource.TMSUCCESS);}
        catch (XAException xe) {throw new SystemException(xe.getMessage());}
        _xaResource = null;
        _intStatus = javax.transaction.Status.STATUS_NO_TRANSACTION;
        return true;
    }

    public void registerSynchronization(Synchronization sync)
    {
      // Do nothing
    }

    public int getStatus()
    {
        return _intStatus;
    }

    public void setRollbackOnly()
    {
       // Do nothing
    }
}
