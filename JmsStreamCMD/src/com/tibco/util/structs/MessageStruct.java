/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.structs;

import javax.jms.Message;

/**
 * This class is used to information about the sequence of publication JMS Messages.<p>
 * The sleepTime is the milliseconds to wait until publication.
 * The commitTrans indicate the transaction should be committed after the message is published.
 * The jmsMessage is the actual JMS Message to be published.<p>
 */
public final class MessageStruct
{
    public long sleepTime;
    public boolean commitTrans;
    public Message jmsMessage;
}
