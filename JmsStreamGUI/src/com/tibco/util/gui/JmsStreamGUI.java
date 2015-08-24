/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui;

import javax.swing.*;

/**
 * Title:        JmsStreamGUI<p>
 * Description:  Main class that starts the JmsStreamGUI form.<p>
 * @author A. Kevin Bailey
 * @version 2.7.8
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration", "CanBeFinal", "unused"})
public class JmsStreamGUI
{
    public final static String APP_NAME = "JmsStream GUI";
    public final static String APP_VERSION = "2.7.9";
    public final static String APP_DATE = "2015-03-11";

    private boolean packFrame = false;

    // Construct the application
    public JmsStreamGUI()
    {
        JmsStreamForm frame = new JmsStreamForm();
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack();
        }
        else {
            frame.validate();
        }

        frame.setVisible(true);
    }

    // Main method
    public static void main(String[] args)
    {
        com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.licenseCode", "u#Andrew_Bailey#1t7fl9r#essnb0");
        com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.isLookAndFeelFrameDecoration", "true");
        com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.isToolbarEffectsEnabled", "false");
        com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.theme", "default");
        try {
            LookAndFeel alloyLnf = new com.incors.plaf.alloy.AlloyLookAndFeel();
            UIManager.setLookAndFeel(alloyLnf);
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }
        catch (UnsupportedLookAndFeelException ue) {
            ue.printStackTrace();
        }

        new JmsStreamGUI();
    }
}
