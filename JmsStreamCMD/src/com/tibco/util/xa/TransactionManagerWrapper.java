/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.xa;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.*;
import java.lang.reflect.Method;
import java.util.Hashtable;

/*  Transaction Status from javax.transaction.Status
    STATUS_ACTIVE = 0
    STATUS_MARKED_ROLLBACK = 1
    STATUS_PREPARED = 2
    STATUS_COMMITTED = 3
    STATUS_ROLLEDBACK = 4
    STATUS_UNKNOWN = 5                                                                                                                                                                                  h
    STATUS_NO_TRANSACTION = 6
    STATUS_PREPARING = 7
    STATUS_COMMITTING = 8
    STATUS_ROLLING_BACK = 9
*/

/**
 * Title:        TransactionManagerWrapper<p>
 * Description:  This class raps the functionality of the XA transaction manager to allow different types of XA managers.<p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "unchecked", "WeakerAccess"})
public class TransactionManagerWrapper implements TransactionManager
{
    public static final int NO_MGR = 500;
    public static final int LOCAL_MGR = 501;
    public static final int REMOTE_MGR = 502;

    private TransactionManager _txManager = null;
    private Transaction _transCurrent = null;
    private int _intTransType = 0;
    private int _intTransTimeout = 0;

    /**
     *   The _env Hashtable must have the following entries:
     *   _env.put("trans", "xa");
     *   _env.put("transjndiname", String x);
     *   _env.put("transmgrtype", String x);
     *   _env.put("transtimeout", Integer x);
     *
     * @param env               The env Hashtable
     * @throws SystemException  System exception.
     */
    public TransactionManagerWrapper(Hashtable env) throws SystemException
    {
        String strTransJndiName;
        String strTransType;

        // Make sure we have the appropriate env variables.
        if (env.containsKey("transjndiname")) strTransJndiName = env.get("transjndiname").toString();
        else throw new SystemException("ERROR:  -transjndiname missing.");

        if (env.containsKey("transmgrtype")) strTransType = env.get("transmgrtype").toString();
        else throw new SystemException("ERROR:  -transmgrtype missing.");

        if (env.containsKey("transtimeout")) _intTransTimeout = (Integer)env.get("transtimeout");

        if (!(env.containsKey("trans") && env.get("trans").equals("xa"))) throw new SystemException("XA Transaction code mismatch.");

        if (strTransType.equals("nomgr")) _intTransType = NO_MGR;
        else if (strTransType.equals("local")) _intTransType = LOCAL_MGR;
        else if (strTransType.equals("remote")) _intTransType = REMOTE_MGR;
        else throw new SystemException("Unsupported XA transaction type " + strTransType);
        //

        switch (_intTransType) {
            case NO_MGR:
                // Not using a transaction manager, so return.
                return;
            case LOCAL_MGR:
                // Create local JBoss transaction manager
                try {
                    // creates a local instance of JBossTS transaction manager
                    Class class1 = Class.forName("com.arjuna.ats.jta.TransactionManager");
                    Method method1 = class1.getMethod("transactionManager");
                    _txManager = (TransactionManager)method1.invoke(null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    e.getCause();
                }
                break;
            case REMOTE_MGR:
                // TODO:  Connect to a remote JNDI server to lookup transaction manager connection factory.

                // Look up the Transaction Manager using the JNDI context.
                try {
                     Context ctx = new InitialContext(/*env2*/);
                    _txManager = (TransactionManager)ctx.lookup(strTransJndiName);
                }
                catch (Exception e) {throw new SystemException(e.getMessage());}

                // Or try this:
                // Create remote JBoss transaction manager
                try {
                    Context ctx = new InitialContext(/*env2*/);
                    // creates a local instance of JBossTS transaction manager
                    Class class1 = Class.forName("com.arjuna.ats.jta.TransactionManager");
                    Method method1 = class1.getMethod("transactionManager");
                    _txManager = (TransactionManager)method1.invoke(ctx);
                }
                catch (Exception e) {throw new SystemException(e.getMessage());}

                break;
            default:
                throw new SystemException("ERROR:  Unknown transaction manager type.");
        }
    }

    public Transaction getTransaction() throws SystemException
    {
        return _transCurrent;
    }

    public void begin() throws SystemException
    {
        switch (_intTransType) {
            case NO_MGR:
                NoMgrTransaction nmtLocal = new NoMgrTransaction();
                nmtLocal.init();
                _transCurrent = nmtLocal;
                break;
            case LOCAL_MGR:
            case REMOTE_MGR:
                try {
                    _txManager.begin();
                    _txManager.setTransactionTimeout(_intTransTimeout);
                } catch (NotSupportedException nse) {throw new SystemException(nse.getMessage());}
                _transCurrent = _txManager.getTransaction();
                break;
            default:
                throw new SystemException("ERROR:  Unknown transaction manager type.");
        }
    }

    public void commit() throws SystemException
    {
        try {
            switch (_intTransType) {
                case NO_MGR:
                    _transCurrent.commit();
                    _transCurrent = null;
                    break;
                case LOCAL_MGR:
                case REMOTE_MGR:
                    _txManager.commit();
                    break;
                default:
                    throw new SystemException("ERROR:  Unknown transaction manager type.");
           }
        }
        catch (Exception e) {
            throw new SystemException(e.getMessage());
        }
    }

    public void rollback() throws SystemException
    {
        switch (_intTransType) {
            case NO_MGR:
                _transCurrent.rollback();
                break;
            case LOCAL_MGR:
            case REMOTE_MGR:
                _txManager.rollback();
                break;
            default:
                throw new SystemException("ERROR:  Unknown transaction manager type.");
       }
    }

    public Transaction suspend() throws SystemException
    {
        Transaction tObj = null;

        if (_txManager != null) tObj = _txManager.suspend();

        return tObj;
    }

    public void resume(Transaction tObj) throws SystemException
    {
        if (_txManager != null) {
            try {
                _txManager.resume(tObj);
            }
            catch (InvalidTransactionException itx) {
                throw new SystemException(itx.getMessage());
            }
        }
    }

    public int getStatus() throws SystemException
    {
        int intStatus;

        switch (_intTransType) {
            case NO_MGR:
                if (_transCurrent == null) intStatus = javax.transaction.Status.STATUS_NO_TRANSACTION;
                else intStatus = _transCurrent.getStatus();
                break;
            case LOCAL_MGR:
            case REMOTE_MGR:
                intStatus = _txManager.getStatus();
                break;
            default:
                throw new SystemException("ERROR:  Unknown transaction manager type.");
       }

        return intStatus;
    }

     public void setTransactionTimeout(int seconds) throws SystemException
    {
        if (_txManager != null) _txManager.setTransactionTimeout(seconds);
    }

    public void setRollbackOnly() throws SystemException
    {
        if (_txManager != null) _txManager.setRollbackOnly();
    }
}