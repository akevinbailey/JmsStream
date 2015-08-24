/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Thu Jul 06 16:20:26 CEST 2006
 */

package com.tibco.util.gui.forms;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
@SuppressWarnings({"FieldCanBeLocal", "deprecation"})
public class JmsStreamLicense extends JDialog {
	public JmsStreamLicense(Frame owner) {
		super(owner);
		initComponents();
	}

	public JmsStreamLicense(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void okButtonActionPerformed() {
		this.dispose();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        lblWarning = new JLabel();
        scrollPane1 = new JScrollPane();
        txtLicense = new JTextPane();
        buttonBar = new JPanel();
        okButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("JmsStream License");
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                    "default, $lcgap, 310dlu, $lcgap, default",
                    "default, $ugap, 2*(default, $lgap), [300dlu,min]:grow, $lgap, default"));

                //---- lblWarning ----
                lblWarning.setText("By using this software you agree to the following license agreement:");
                lblWarning.setFont(new Font("Tahoma", Font.BOLD, 15));
                contentPanel.add(lblWarning, cc.xy(3, 3));

                //======== scrollPane1 ========
                {
                    scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                    //---- txtLicense ----
                    txtLicense.setText("text");
                    txtLicense.setEditable(false);
                    txtLicense.setFont(new Font("Courier New", Font.PLAIN, 11));
                    scrollPane1.setViewportView(txtLicense);
                }
                contentPanel.add(scrollPane1, cc.xy(3, 7));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    "$glue, $button",
                    "pref"));

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okButtonActionPerformed();
                    }
                });
                buttonBar.add(okButton, cc.xy(2, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
        txtLicense.setText(com.tibco.util.jmshelper.License.getLicenseAgreement());
        txtLicense.setCaretPosition(0); // Set the cursor to the top of the JTextPane
    }

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel lblWarning;
    private JScrollPane scrollPane1;
    private JTextPane txtLicense;
    private JPanel buttonBar;
    private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
