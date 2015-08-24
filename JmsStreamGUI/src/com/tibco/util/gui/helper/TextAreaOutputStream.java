/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui.helper;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Title:        TextAreaOutputStream<p>
 * Description:  This class is used to buffer and redirect the System.out and
 *               System.err to a JTextArea.  It also updates the JTextArea
 *               using a timer to call the updateTextArea() method.<p>
 * @author A. Kevin Bailey
 * @version 2.4.0
 */
public class TextAreaOutputStream extends OutputStream
{
    private final JTextArea _textControl;
    private final ByteArrayOutputStream _baoBuffer;
    private int _intMaxLength;
    private int _intCutLength;
    private boolean _blnNewLine;
    /**
     * Creates a new instance of TextAreaOutputStream which writes
     * to the specified instance of javax.swing.JTextArea control.
     *
     * @param control   A reference to the javax.swing.JTextArea
     *                  control to which the output must be redirected
     *                  to.
     */
    public TextAreaOutputStream(JTextArea control)
    {
        _intMaxLength = 10000000; // Default value
        _intCutLength = _intMaxLength + Math.round(_intMaxLength * 0.2F); // Allow 20% over the max length before cutting the buffer
        _blnNewLine = false;
        _textControl = control;
        _baoBuffer = new ByteArrayOutputStream();
    }

    /**
     * Writes the specified byte as a character to the
     * javax.swing.JTextArea.<p>
     *
     * @param b    The byte to be written as character to the
     *             StringBuffer.
     */
    public void write(int b) throws IOException
    {
        // Append the data as characters to the string buffer
        synchronized (_baoBuffer) {
            _baoBuffer.write(b);
            // To avoid memory overflows cut the buffer too.
//            if (_baoBuffer.size() > _intCutLength) {
//                try {
//                    //_baoBuffer.delete(0, _baoBuffer.size - _intMaxLength);
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }

        if ((char)b == '\r') _blnNewLine = true;
    }

    /**
     * Sets the maximum buffer length for the
     * javax.swing.JTextArea control.
     *
     * @param maxLength    The maximum buffer length in characters.
     */
    public void setMaxCharLength(int maxLength)
    {
        _intCutLength = maxLength + Math.round(maxLength * 0.2F);
        _intMaxLength = maxLength;
    }

    /**
     * Sets the maximum buffer length for the
     * javax.swing.JTextArea control.
     *
     * @throws UnsupportedEncodingException     Will never be thrown because UTF-8 is always supported.
     *
     */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void updateTextArea() throws UnsupportedEncodingException
    {
        //TODO:  Fix nested synchronize statements
        synchronized (_textControl) {
            synchronized (_baoBuffer) {
                if (_baoBuffer.size() > 0) {
                    // Append the buffer data to the JTextArea control
                    _textControl.append(_baoBuffer.toString("UTF-8"));
                    _baoBuffer.reset();
                }
            }
            // If there is a carriage return check buffer and scroll to bottom of screen.
            if (_blnNewLine) {
                // Cut the characters that exceed the _inMaxLength but only if it exceeds it by more than 20% characters.
                if (_textControl.getDocument().getLength() > _intCutLength) {
                    try {
                        _textControl.getDocument().remove(0, _textControl.getDocument().getLength()-_intMaxLength);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Set the Columns to get the horizontal scroll bars to display properly.
                //_textControl.setColumns(_textControl.getWidth());

                //Rectangle recView = _textControl.getVisibleRect();
                //recView.y = _textControl.getHeight() - recView.height;
                //recView.x = 0;
                //_textControl.scrollRectToVisible(recView);
               _blnNewLine = false;
            }
        }
    }
}
