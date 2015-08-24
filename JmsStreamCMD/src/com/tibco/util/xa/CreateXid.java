/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.xa;

import javax.transaction.xa.Xid;
import java.util.Arrays;

/**
 * Title:        CreateXid<p>
 * Description:  This class is used to artificially generate a Xid for XA transactions.<p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "UnusedDeclaration", "unused"})
public class CreateXid implements Xid
{

    private int _formatId;
    private byte _branchQual[];
    private int _branchQualLength;
    private byte _globalTxId[];
    private int _globalTxIdLength;
    public static final int NULLXID = -1;

    public CreateXid()
    {
        create(-1, null, 0, null, 0);
    }

    public CreateXid(int i, byte abyte0[], int j, byte abyte1[], int k)
    {
        create(i, abyte0, j, abyte1, k);
    }

    public CreateXid(int i, String s, String s1)
    {
        byte abyte0[] = s.getBytes();
        int j = abyte0.length;
        byte abyte1[] = s1.getBytes();
        int k = abyte1.length;
        create(i, abyte0, j, abyte1, k);
    }

    public CreateXid(Xid xid)
    {
        byte abyte0[] = xid.getGlobalTransactionId();
        int i = abyte0.length;
        byte abyte1[] = xid.getBranchQualifier();
        int j = abyte1.length;
        create(xid.getFormatId(), abyte0, i, abyte1, j);
    }

    private void create(int i, byte abyte0[], int j, byte abyte1[], int k)
    {
        _globalTxId = new byte[64];
        _branchQual = new byte[64];
        _formatId = i;
        if(j > 64)
        {
            j = 64;
        }
        if(abyte0 != null)
        {
            System.arraycopy(abyte0, 0, _globalTxId, 0, j);
        }
        _globalTxIdLength = j;
        if(k > 64)
        {
            k = 64;
        }
        if(abyte1 != null)
        {
            System.arraycopy(abyte1, 0, _branchQual, 0, k);
        }
        _branchQualLength = k;
    }

    public byte[] getBranchQualifier()
    {
        byte abyte0[] = new byte[_branchQualLength];
        System.arraycopy(_branchQual, 0, abyte0, 0, _branchQualLength);
        return abyte0;
    }

    public int getFormatId()
    {
        return _formatId;
    }

    public byte[] getGlobalTransactionId()
    {
        byte abyte0[] = new byte[_globalTxIdLength];
        System.arraycopy(_globalTxId, 0, abyte0, 0, _globalTxIdLength);
        return abyte0;
    }

    public static boolean isSame(Xid xid, Xid xid1)
    {
        if(xid == null || xid1 == null)
        {
            return false;
        }
        byte abyte0[] = xid.getGlobalTransactionId();
        byte abyte1[] = xid.getBranchQualifier();
        byte abyte2[] = xid1.getGlobalTransactionId();
        byte abyte3[] = xid1.getBranchQualifier();
        return xid.getFormatId() == xid1.getFormatId() && abyte0.length == abyte2.length && abyte1.length == abyte3.length && Arrays.equals(abyte0, abyte2) && Arrays.equals(abyte1, abyte1);
    }

    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer(256);
        if(_formatId == -1)
        {
            return "NULLXID";
        }
        stringbuffer.append("{formatID=").append(_formatId);
        stringbuffer.append(" gtrid_length=").append(_globalTxIdLength);
        stringbuffer.append(" bqual_length=").append(_branchQualLength).append(" data=");
        for(int i = 0; i < _globalTxIdLength; i++)
        {
            if(_globalTxId[i] >= 32 && _globalTxId[i] <= 126 && _globalTxId[i] != 37)
            {
                stringbuffer.append((char)_globalTxId[i]);
            } else
            {
                stringbuffer.append("%").append(Byte.toString(_globalTxId[i]));
            }
        }

        for(int j = 0; j < _branchQualLength; j++)
        {
            if(_branchQual[j] >= 32 && _branchQual[j] <= 126 && _branchQual[j] != 37)
            {
                stringbuffer.append((char)_branchQual[j]);
            } else
            {
                stringbuffer.append("%").append(Byte.toString(_branchQual[j]));
            }
        }

        stringbuffer.append("}");
        return new String(stringbuffer);
    }
}
