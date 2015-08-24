/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui.helper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Title:        TextUpdateListener<p>
 * Description:  A Timer Class calls this class to Call the text area update
 *               function on TextAreaOutputStream to display the output text.<p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
public class TextUpdateListener implements ActionListener
{
    private TextAreaOutputStream _app = null;

    public TextUpdateListener(TextAreaOutputStream app)
    {
        _app = app;
    }

    public void actionPerformed(ActionEvent ae)
    {
        try {
            _app.updateTextArea();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}