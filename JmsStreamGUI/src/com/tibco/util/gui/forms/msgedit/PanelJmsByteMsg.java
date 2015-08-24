/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Sun Feb 08 21:01:53 CET 2009
 */

package com.tibco.util.gui.forms.msgedit;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.util.jmshelper.Base64;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.0
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "unchecked", "unused", "FieldCanBeLocal", "CanBeFinal"})
public class PanelJmsByteMsg extends JPanel
{
    public PanelJmsByteMsg()
    {
        initComponents();
    }

    public String getBase64() {
        if (chkBoxTextToBinary.isSelected()) refreshText();
        return txtBinary.getText();
    }

    public void setBase64(String strBase64) {
        txtBinary.setText(strBase64);
        refreshText();
    }

    public void refreshText()
    {
        try {
            if (chkBoxTextToBinary.isSelected() && txtText.getText() != null) {
                if (cboNewLine.getSelectedIndex() == 0) _clsBase64.setLineSeparator("\n"); // Unix/Linux
                else if (cboNewLine.getSelectedIndex() == 1) _clsBase64.setLineSeparator("\r\n"); // Windows
                txtBinary.setText(_clsBase64.encode(txtText.getText().getBytes(cboCharacterSet.getSelectedItem().toString())));
            }
            else if (txtBinary.getText() != null) {
                txtText.setText(Base64.decodeToString(txtBinary.getText(), cboCharacterSet.getSelectedItem().toString()));
            }
            else {
                txtBinary.setText(null);
                txtText.setText(null);
            }
        }
        catch (UnsupportedEncodingException uee) {
            JOptionPane.showMessageDialog(this, "Unsupported Encoding Type.", "Character Set Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chkBoxTextToBinaryItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            txtBinary.setEditable(false);
            txtText.setEditable(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            txtBinary.setEditable(true);
            txtText.setEditable(false);
        }
    }

    private void chkLineWrapBinaryItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            txtBinary.setLineWrap(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            txtBinary.setLineWrap(false);
        }
    }

    private void chkLineWrapTextItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            txtText.setLineWrap(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            txtText.setLineWrap(false);
        }
    }

    private void txtBinaryKeyTyped() {
        btnUpdate.setEnabled(true);
    }

    private void txtTextKeyTyped() {
        btnUpdate.setEnabled(true);
    }

    private void btnUpdateActionPerformed() {
        refreshText();
        btnUpdate.setEnabled(false);
    }

    private void cboCharacterSetItemStateChanged() {
        btnUpdateActionPerformed();
    }

    private void cboNewLineItemStateChanged() {
        btnUpdateActionPerformed();
    }

    private void initComponents() {
        _clsBase64 = new Base64();
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblBinaryPanel = new JLabel();
        chkBoxTextToBinary = new JCheckBox();
        lblNewLine = new JLabel();
        cboNewLine = new JComboBox();
        lblBinary = new JLabel();
        chkLineWrapBinary = new JCheckBox();
        chkLineWrapText = new JCheckBox();
        scrBinary = new JScrollPane();
        txtBinary = new JTextArea();
        btnUpdate = new JButton();
        lblCharacterSet = new JLabel();
        cboCharacterSet = new JComboBox();
        lblText = new JLabel();
        scrText = new JScrollPane();
        txtText = new JTextArea();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "2dlu, $lcgap, default, $lcgap, default:grow, $lcgap, default, $lcgap, 2dlu",
            "2dlu, $lgap, default, $ugap, default, $rgap, default, $lgap, fill:default:grow, $ugap, default, $rgap, default, $lgap, fill:default:grow, $lgap, default, $lgap, 2dlu"));

        //---- lblBinaryPanel ----
        lblBinaryPanel.setText("Binary Message Type");
        lblBinaryPanel.setFont(new Font("Tahoma", Font.BOLD, 11));
        add(lblBinaryPanel, cc.xy(3, 3));

        //---- chkBoxTextToBinary ----
        chkBoxTextToBinary.setText("Translate Text to Binary");
        chkBoxTextToBinary.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                chkBoxTextToBinaryItemStateChanged(e);
            }
        });
        add(chkBoxTextToBinary, cc.xy(3, 5));

        //---- lblNewLine ----
        lblNewLine.setText("New Line Type");
        lblNewLine.setLabelFor(cboNewLine);
        add(lblNewLine, cc.xywh(5, 5, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

        //---- cboNewLine ----
        cboNewLine.setModel(new DefaultComboBoxModel(new String[] {
            "Linux/Unix",
            "Windows"
        }));
        cboNewLine.setMaximumRowCount(2);
        cboNewLine.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                cboNewLineItemStateChanged();
            }
        });
        add(cboNewLine, cc.xy(7, 5));

        //---- lblBinary ----
        lblBinary.setText("Message Data in Base64:");
        lblBinary.setLabelFor(txtBinary);
        add(lblBinary, cc.xy(3, 7));

        //---- chkLineWrapBinary ----
        chkLineWrapBinary.setText("Line Wrap Base64");
        chkLineWrapBinary.setMargin(new Insets(0, 0, 0, 0));
        chkLineWrapBinary.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                chkLineWrapBinaryItemStateChanged(e);
            }
        });
        add(chkLineWrapBinary, cc.xy(7, 7));

        //---- chkLineWrapText ----
        chkLineWrapText.setText("Line Wrap Text");
        chkLineWrapText.setMargin(new Insets(0, 0, 0, 0));
        chkLineWrapText.setSelected(true);
        chkLineWrapText.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                chkLineWrapTextItemStateChanged(e);
            }
        });
        add(chkLineWrapText, cc.xy(7, 13));

        //======== scrBinary ========
        {

            //---- txtBinary ----
            txtBinary.setRows(6);
            txtBinary.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    txtBinaryKeyTyped();
                }
            });
            scrBinary.setViewportView(txtBinary);
        }
        add(scrBinary, cc.xywh(3, 9, 5, 1));

        //---- btnUpdate ----
        btnUpdate.setText("Update Text");
        btnUpdate.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/sync.png")));
        btnUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnUpdateActionPerformed();
            }
        });
        add(btnUpdate, cc.xywh(3, 11, 1, 1, CellConstraints.LEFT, CellConstraints.CENTER));

        //---- lblCharacterSet ----
        lblCharacterSet.setText("Character Set");
        lblCharacterSet.setLabelFor(cboCharacterSet);
        add(lblCharacterSet, cc.xywh(5, 11, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

        //---- cboCharacterSet ----
        cboCharacterSet.setModel(new DefaultComboBoxModel(new String[] {
            "ASCII",
            "Cp1252",
            "ISO8859_1",
            "UnicodeBig",
            "UnicodeBigUnmarked",
            "UnicodeLittle",
            "UnicodeLittleUnmarked",
            "UTF8",
            "UTF-16"
        }));
        cboCharacterSet.setMaximumRowCount(9);
        cboCharacterSet.setSelectedIndex(2);
        cboCharacterSet.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                cboCharacterSetItemStateChanged();
            }
        });
        add(cboCharacterSet, cc.xy(7, 11));

        //---- lblText ----
        lblText.setText("Message Data in Text:");
        lblText.setLabelFor(txtText);
        add(lblText, cc.xy(3, 13));

        //======== scrText ========
        {

            //---- txtText ----
            txtText.setEditable(false);
            txtText.setLineWrap(true);
            txtText.setRows(6);
            txtText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    txtTextKeyTyped();
                }
            });
            scrText.setViewportView(txtText);
        }
        add(scrText, cc.xywh(3, 15, 5, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private Base64 _clsBase64;
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblBinaryPanel;
    private JCheckBox chkBoxTextToBinary;
    private JLabel lblNewLine;
    private JComboBox cboNewLine;
    private JLabel lblBinary;
    private JCheckBox chkLineWrapBinary;
    private JCheckBox chkLineWrapText;
    private JScrollPane scrBinary;
    private JTextArea txtBinary;
    private JButton btnUpdate;
    private JLabel lblCharacterSet;
    private JComboBox cboCharacterSet;
    private JLabel lblText;
    private JScrollPane scrText;
    private JTextArea txtText;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
