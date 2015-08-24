/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui.helper;

import com.tibco.util.gui.JmsStreamForm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Title:        MemCheckListener<p>
 * Description:  A Timer Class calls this class to Call the memory update
 *               function on JmsStreamForm to display the memory usage.<p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
public class MemCheckListener implements ActionListener
{
    private JmsStreamForm _app = null;

    public MemCheckListener(JmsStreamForm app)
    {
        _app = app;
    }

    public void actionPerformed(ActionEvent e)
    {
        _app.updateMemDisplay();
    }
}