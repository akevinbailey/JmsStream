/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Tue Jul 04 19:53:10 CEST 2007
 */

package com.tibco.util.gui.forms;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.util.JmsStream;
import com.tibco.util.gui.JmsStreamGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "deprecation", "unused"})
public final class JmsStreamAbout extends JDialog {
    public JmsStreamAbout(Frame owner) {
        super(owner);
        initComponents();
    }

    public JmsStreamAbout(Dialog owner) {
        super(owner);
        initComponents();
    }

    private void okButtonActionPerformed() {
        this.dispose();
    }

    private void btnLicenseActionPerformed() {
        JmsStreamLicense dlg = new JmsStreamLicense(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        lblTibco = new JLabel();
        panel1 = new JPanel();
        lblGuiName = new JLabel();
        lblGuiVersion = new JLabel();
        lblGuiDate = new JLabel();
        lblAppName = new JLabel();
        lblAppVersion = new JLabel();
        lblAppDate = new JLabel();
        lblAppAuthor = new JLabel();
        lblInfo1 = new JLabel();
        lblInfo2 = new JLabel();
        buttonBar = new JPanel();
        btnLicense = new JButton();
        okButton = new JButton();

        //======== this ========
        setTitle("JmsStream About");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                    "110dlu, $rgap, default",
                    "2*(default, $lgap), 20dlu, 2*($lgap, default)"));

                //---- lblTibco ----
                lblTibco.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/TIBCOLogo_144wide.png")));
                contentPanel.add(lblTibco, CC.xy(1, 1, CC.CENTER, CC.DEFAULT));

                //======== panel1 ========
                {
                    panel1.setLayout(new FormLayout(
                        "3dlu, 7dlu, default",
                        "3*(default, $lgap), default, $ugap, 2*(default, $lgap), default"));

                    //---- lblGuiName ----
                    lblGuiName.setText("JmsStream GUI");
                    lblGuiName.setFont(new Font("Tahoma", Font.BOLD, 18));
                    panel1.add(lblGuiName, CC.xy(3, 3));

                    //---- lblGuiVersion ----
                    lblGuiVersion.setText("Version 0.0.0");
                    lblGuiVersion.setFont(new Font("Tahoma", Font.BOLD, 14));
                    panel1.add(lblGuiVersion, CC.xy(3, 5));

                    //---- lblGuiDate ----
                    lblGuiDate.setText("Build Date: 2000-01-01");
                    panel1.add(lblGuiDate, CC.xy(3, 7));

                    //---- lblAppName ----
                    lblAppName.setText("JmsStream Engine");
                    lblAppName.setFont(new Font("Tahoma", Font.BOLD, 18));
                    panel1.add(lblAppName, CC.xy(3, 9));

                    //---- lblAppVersion ----
                    lblAppVersion.setText("Version 0.0.0");
                    lblAppVersion.setFont(new Font("Tahoma", Font.BOLD, 14));
                    panel1.add(lblAppVersion, CC.xy(3, 11));

                    //---- lblAppDate ----
                    lblAppDate.setText("Build Date: 2000-01-01");
                    panel1.add(lblAppDate, CC.xy(3, 13));
                }
                contentPanel.add(panel1, CC.xy(3, 1));

                //---- lblAppAuthor ----
                lblAppAuthor.setText("Author:  A. Kevin Bailey");
                contentPanel.add(lblAppAuthor, CC.xywh(1, 3, 3, 1));

                //---- lblInfo1 ----
                lblInfo1.setText("This software comes with no warranties of any kind.");
                contentPanel.add(lblInfo1, CC.xywh(1, 7, 3, 1));

                //---- lblInfo2 ----
                lblInfo2.setText("Read the license for more details.");
                contentPanel.add(lblInfo2, CC.xywh(1, 9, 3, 1));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    "default, $glue, $button",
                    "pref"));

                //---- btnLicense ----
                btnLicense.setText("License...");
                btnLicense.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnLicenseActionPerformed();
                    }
                });
                buttonBar.add(btnLicense, CC.xy(1, 1, CC.LEFT, CC.DEFAULT));

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okButtonActionPerformed();
                    }
                });
                buttonBar.add(okButton, CC.xy(3, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        lblGuiVersion.setText("Version " + JmsStreamGUI.APP_VERSION);
        lblGuiDate.setText("Build Date: " + JmsStreamGUI.APP_DATE);
        lblAppVersion.setText("Version " + JmsStream.APP_VERSION);
        lblAppDate.setText("Build Date: "+ JmsStream.APP_DATE);
    }

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel lblTibco;
    private JPanel panel1;
    private JLabel lblGuiName;
    private JLabel lblGuiVersion;
    private JLabel lblGuiDate;
    private JLabel lblAppName;
    private JLabel lblAppVersion;
    private JLabel lblAppDate;
    private JLabel lblAppAuthor;
    private JLabel lblInfo1;
    private JLabel lblInfo2;
    private JPanel buttonBar;
    private JButton btnLicense;
    private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
