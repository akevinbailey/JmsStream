/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Tue Jul 11 18:28:51 CEST 2006
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
 * @version 2.7.0
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "FieldCanBeLocal", "CanBeFinal", "UnusedDeclaration", "deprecation", "unused"})
public class JmsStreamGetLength extends JDialog {
    public JmsStreamGetLength(Frame owner) {
        super(owner);
        initComponents();
    }

    public JmsStreamGetLength(Dialog owner) {
        super(owner);
        initComponents();
    }

    private void cancelButtonActionPerformed() {
        this.dispose();
    }

    private void btnCalculateActionPerformed() {
        int intBodyLength = 0;
        char [] charTemp;

        try {
            charTemp = textArea.getText().toCharArray();
            for (int i=0; i < charTemp.length; i++) {
                // If reading a Windows file do not count the line feed.
                if (charTemp[i] != '\r') intBodyLength++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            txtLength.setText("");
        }

        txtLength.setText(Long.toString(intBodyLength));
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        lblTextArea = new JLabel();
        scrollPane = new JScrollPane();
        textArea = new JTextArea();
        buttonBar = new JPanel();
        btnCalculate = new JButton();
        lblLength = new JLabel();
        txtLength = new JTextField();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Calculate Message Length");
        setModal(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                    "default:grow",
                    "default, $lgap, fill:default:grow"));

                //---- lblTextArea ----
                lblTextArea.setText("Paste message body in to text area:");
                lblTextArea.setLabelFor(textArea);
                contentPanel.add(lblTextArea, cc.xy(1, 1));

                //======== scrollPane ========
                {

                    //---- textArea ----
                    textArea.setRows(10);
                    textArea.setTabSize(10);
                    scrollPane.setViewportView(textArea);
                }
                contentPanel.add(scrollPane, cc.xy(1, 3));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    "default, $ugap, default, $lcgap, 50dlu, 10px:grow, $button",
                    "pref"));

                //---- btnCalculate ----
                btnCalculate.setText("Calculate Length");
                btnCalculate.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/calculate.png")));
                btnCalculate.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnCalculateActionPerformed();
                    }
                });
                buttonBar.add(btnCalculate, cc.xy(1, 1));

                //---- lblLength ----
                lblLength.setText("Message Length");
                buttonBar.add(lblLength, cc.xy(3, 1));

                //---- txtLength ----
                txtLength.setEditable(false);
                txtLength.setToolTipText("The message length in number of characters.");
                buttonBar.add(txtLength, cc.xy(5, 1));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.setMaximumSize(new Dimension(69, 29));
                cancelButton.setMinimumSize(new Dimension(69, 29));
                cancelButton.setPreferredSize(new Dimension(69, 29));
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButtonActionPerformed();
                    }
                });
                buttonBar.add(cancelButton, cc.xy(7, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(520, 350);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel lblTextArea;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JPanel buttonBar;
    private JButton btnCalculate;
    private JLabel lblLength;
    private JTextField txtLength;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
