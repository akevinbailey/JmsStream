/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui.helper;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToolTipUI;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Title:        MultiLineToolTip<p>
 * Description:  Allows a component tooltip to have multiple lines.<p>
 * Example to replace the default tooltip for a component with MultiLineToolTip:
        rdoReqRep = new JRadioButton() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
 *
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
public class MultiLineToolTip extends JToolTip
{
  public MultiLineToolTip() {
    setUI(new MultiLineToolTipUI());
  }
}

/**
 * Title:        MultiLineToolTipUI<p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration", "UnusedAssignment", "ConstantConditions", "unused"})
class MultiLineToolTipUI extends BasicToolTipUI
{
    private String[] strStrings;

    private int maxWidth = 0;

    public void paint(Graphics graphics, JComponent jComponent)
    {
        FontMetrics fmMetrics = graphics.getFontMetrics();
        Dimension size = jComponent.getSize();

        graphics.setColor(jComponent.getBackground());
        graphics.fillRect(0, 0, size.width, size.height);
        graphics.setColor(jComponent.getForeground());
        if (strStrings != null) {
            for (int i = 0; i < strStrings.length; i++) {
                graphics.drawString(strStrings[i], 3, (fmMetrics.getHeight()) * (i + 1));
            }
        }
    }

    public Dimension getPreferredSize(JComponent jComponent)
    {
        String strLine;
        int intMaxWidth = 0;
        int intHeight;
        int intLines;
        FontMetrics fmMetrics = jComponent.getFontMetrics(jComponent.getFont());
        String strTipText = ((JToolTip) jComponent).getTipText();
        BufferedReader bufferedReader = new BufferedReader(new StringReader(strTipText));
        Vector<String> vStrings = new Vector<String>();

        if (strTipText == null) {
            strTipText = "";
        }
        try {
            while ((strLine = bufferedReader.readLine()) != null) {
                int width = SwingUtilities.computeStringWidth(fmMetrics, strLine);
                intMaxWidth = (intMaxWidth < width) ? width : intMaxWidth;
                vStrings.addElement(strLine);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        intLines = vStrings.size();
        if (intLines < 1) {
            strStrings = null;
            intLines = 1;
        }
        else {
            strStrings = new String[intLines];
            int i = 0;
            for (Enumeration e = vStrings.elements(); e.hasMoreElements(); i++) {
                strStrings[i] = (String) e.nextElement();
            }
        }
        intHeight = fmMetrics.getHeight() * intLines;
        this.maxWidth = intMaxWidth;

        return new Dimension(intMaxWidth + 6, intHeight + 4);
    }
}
