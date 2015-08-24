/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimerTask;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.6.2
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "UnusedDeclaration", "unused"})
public final class JmsStreamTimerTask extends TimerTask
{
    private JmsStreamListener _listener = null;
    private JmsStreamPublisher _publisher = null;

    public JmsStreamTimerTask(JmsStreamListener listener)
    {
        _listener = listener;
    }

    public JmsStreamTimerTask(JmsStreamPublisher publisher)
    {
        _publisher = publisher;
    }

    public void run()
    {        
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
        if (_listener != null) {
            if (JmsStreamListener._msgTimerCount > 0)
                System.out.println(formatter.format(new Date()) + ": New messages received (within " + JmsStreamListener._statsDelay/1000 + " sec): " + JmsStreamListener._msgTimerCount +
                        " *** Average message rate per second: " + JmsStreamListener._msgTimerCount * 1000 / JmsStreamListener._statsDelay + " *** total count: " + JmsStreamListener._intTotalCount);
            JmsStreamListener._msgTimerCount = 0;
        }
        if (_publisher != null) {
            if (_publisher._msgTimerCount > 0)
                System.out.println(formatter.format(new Date()) + ": New messages published (within " + _publisher._statsDelay/1000 + " sec): " + _publisher._msgTimerCount +
                        " *** Average message rate per second: " + _publisher._msgTimerCount * 1000 / _publisher._statsDelay + " *** total count: " + _publisher._intTotalSentMessages);
            _publisher._msgTimerCount = 0;
        }
    }
}
