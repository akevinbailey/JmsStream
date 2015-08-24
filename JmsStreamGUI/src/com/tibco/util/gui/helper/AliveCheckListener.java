 /*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui.helper;

 import com.tibco.util.gui.JmsStreamForm;

 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;

/**
 * Title:        AliveCheckListener<p>
 * Description:  A Timer Class calls this class to check if the
 *               JmsStream thread is still alive.  If it is not
 *               running this class calls the stopThread method
 *               of the JmsStreamForm Class.<p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class AliveCheckListener implements ActionListener
{
    private JmsStreamForm _app = null;

    public AliveCheckListener(JmsStreamForm app)
    {
        _app = app;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (!_app.isThreadAlive()) {
            _app.stopThread();
        }
    }
}
