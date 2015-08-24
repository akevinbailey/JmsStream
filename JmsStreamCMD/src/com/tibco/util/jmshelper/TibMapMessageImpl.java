/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.jmshelper;

import com.tibco.tibjms.TibjmsMapMessage;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Enumeration;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "UnusedDeclaration", "unused"})
public class TibMapMessageImpl extends TibjmsMapMessage
{
    private String _strName = null;
    private TibMapMessageImpl _parent = null;

    TibMapMessageImpl() throws JMSException
    {
        setBooleanProperty("JMS_TIBCO_MSG_EXT", true);
    }

    TibMapMessageImpl(String name, TibMapMessageImpl parent) throws JMSException
    {
        setBooleanProperty("JMS_TIBCO_MSG_EXT", true);
        _strName = name;
        _parent = parent;
    }

    public TibMapMessageImpl(MapMessage msg) throws JMSException
    {
        Enumeration enuProp = msg.getPropertyNames();
        Enumeration enuMap = msg.getMapNames();
        String strPropName;
        String strMapName;

        setJMSCorrelationID(msg.getJMSCorrelationID());
        setJMSDeliveryMode(msg.getJMSDeliveryMode());
        setJMSDestination(msg.getJMSDestination());
        setJMSExpiration(msg.getJMSExpiration());
        setJMSMessageID(msg.getJMSMessageID());
        setJMSPriority(msg.getJMSPriority());
        setJMSRedelivered(msg.getJMSRedelivered());
        setJMSReplyTo(msg.getJMSReplyTo());
        setJMSTimestamp(msg.getJMSTimestamp());
        setJMSType(msg.getJMSType());

        while (enuProp.hasMoreElements()) {
            strPropName = enuProp.nextElement().toString();
            setObjectProperty(strPropName, msg.getObjectProperty(strPropName));
        }

        while (enuMap.hasMoreElements()) {
            strMapName = enuMap.nextElement().toString();
            setObject(strMapName, msg.getObject(strMapName));
        }

        setBooleanProperty("JMS_TIBCO_MSG_EXT", true);
    }

    public void setName(String name)
    {
        _strName = name;
    }

    public String getName()
    {
        return _strName;
    }

    public TibMapMessageImpl getParent()
    {
        return _parent;
    }
}


