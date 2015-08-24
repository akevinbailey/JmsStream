/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Tue Feb 03 22:38:12 CET 2009
 */

package com.tibco.util.gui.forms.msgedit;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "WeakerAccess"})
public class PanelJmsTextMsg extends JPanel {
    public PanelJmsTextMsg() {
        initComponents();
    }

    public String getTextData() {
        return txtText.getText();
    }

    public void setTextData(String strData) {
        txtText.setText(strData);
    }

    private void chkLineWrapItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            txtText.setLineWrap(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            txtText.setLineWrap(false);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblTextPanel = new JLabel();
        lblText = new JLabel();
        chkLineWrap = new JCheckBox();
        scrText = new JScrollPane();
        txtText = new JTextArea();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "2dlu, $lcgap, default:grow, $lcgap, default, $lcgap, 2dlu",
            "2dlu, $lgap, default, $ugap, default, $lgap, fill:default:grow, $lgap, 2dlu"));

        //---- lblTextPanel ----
        lblTextPanel.setText("Text Message Type");
        lblTextPanel.setFont(new Font("Tahoma", Font.BOLD, 11));
        add(lblTextPanel, cc.xy(3, 3));

        //---- lblText ----
        lblText.setText("Message Data in Text");
        lblText.setLabelFor(txtText);
        add(lblText, cc.xy(3, 5));

        //---- chkLineWrap ----
        chkLineWrap.setText("Line Wrap");
        chkLineWrap.setMargin(new Insets(0, 0, 0, 0));
        chkLineWrap.setSelected(true);
        chkLineWrap.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                chkLineWrapItemStateChanged(e);
            }
        });
        add(chkLineWrap, cc.xy(5, 5));

        //======== scrText ========
        {

            //---- txtText ----
            txtText.setLineWrap(true);
            scrText.setViewportView(txtText);
        }
        add(scrText, cc.xywh(3, 7, 3, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblTextPanel;
    private JLabel lblText;
    private JCheckBox chkLineWrap;
    private JScrollPane scrText;
    private JTextArea txtText;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
