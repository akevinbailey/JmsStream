/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Thu Mar 25 15:52:48 CET 2010
 */

package com.tibco.util.gui.forms;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.util.jmshelper.ConnectionHelper;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.6.0
 */
@SuppressWarnings({"FieldCanBeLocal", "deprecation", "unused"})
public class JmsStreamFileJndiConfig extends JDialog
{
    public JmsStreamFileJndiConfig(Frame owner)
    {
        super(owner);
        initComponents();
    }

    public JmsStreamFileJndiConfig(Dialog owner)
    {
        super(owner);
        initComponents();
    }

    private void chkCreateTibcoItemStateChanged() {
        if (chkCreateTibco.isSelected()) {
            lblTibJndiName.setEnabled(true);
            lblTibJmsConnectionURL.setEnabled(true);
            txtTibjmsGenericConnectionFactory.setEnabled(true);
            txtTibjmsGenericConnectionFactoryURL.setEnabled(true);
            txtTibjmsQueueConnectionFactory.setEnabled(true);
            txtTibjmsQueueConnectionFactoryURL.setEnabled(true);
            txtTibjmsTopicConnectionFactory.setEnabled(true);
            txtTibjmsTopicConnectionFactoryURL.setEnabled(true);
            txtTibjmsXAGenericConnectionFactory.setEnabled(true);
            txtTibjmsXAGenericConnectionFactoryURL.setEnabled(true);
            txtTibjmsXAQueueConnectionFactory.setEnabled(true);
            txtTibjmsXAQueueConnectionFactoryURL.setEnabled(true);
            txtTibjmsXATopicConnectionFactory.setEnabled(true);
            txtTibjmsXATopicConnectionFactoryURL.setEnabled(true);
        }
        else {
            lblTibJndiName.setEnabled(false);
            lblTibJmsConnectionURL.setEnabled(false);
            txtTibjmsGenericConnectionFactory.setEnabled(false);
            txtTibjmsGenericConnectionFactoryURL.setEnabled(false);
            txtTibjmsQueueConnectionFactory.setEnabled(false);
            txtTibjmsQueueConnectionFactoryURL.setEnabled(false);
            txtTibjmsTopicConnectionFactory.setEnabled(false);
            txtTibjmsTopicConnectionFactoryURL.setEnabled(false);
            txtTibjmsXAGenericConnectionFactory.setEnabled(false);
            txtTibjmsXAGenericConnectionFactoryURL.setEnabled(false);
            txtTibjmsXAQueueConnectionFactory.setEnabled(false);
            txtTibjmsXAQueueConnectionFactoryURL.setEnabled(false);
            txtTibjmsXATopicConnectionFactory.setEnabled(false);
            txtTibjmsXATopicConnectionFactoryURL.setEnabled(false);
        }
    }

    private void chkCreateActiveMQItemStateChanged() {
        if (chkCreateActiveMQ.isSelected()) {
            lblAmqJndiName.setEnabled(true);
            lblAmqJmsConnectionURL.setEnabled(true);
            txtActiveMQConnectionFactory.setEnabled(true);
            txtActiveMQConnectionFactoryURL.setEnabled(true);
            txtActiveMQXAConnectionFactory.setEnabled(true);
            txtActiveMQXAConnectionFactoryURL.setEnabled(true);
        }
        else {
            lblAmqJndiName.setEnabled(false);
            lblAmqJmsConnectionURL.setEnabled(false);
            txtActiveMQConnectionFactory.setEnabled(false);
            txtActiveMQConnectionFactoryURL.setEnabled(false);
            txtActiveMQXAConnectionFactory.setEnabled(false);
            txtActiveMQXAConnectionFactoryURL.setEnabled(false);
        }
    }

    private void chkCreateHornetQItemStateChanged() {
        if (chkCreateHornetQ.isSelected()) {
            lblHornetQJndiName.setEnabled(true);
            lblHornetQJmsConnectionURL.setEnabled(true);
            txtHornetQConnectionFactory.setEnabled(true);
            txtHornetQConnectionFactoryURL.setEnabled(true);
            txtHornetQXAConnectionFactory.setEnabled(true);
            txtHornetQXAConnectionFactoryURL.setEnabled(true);
        }
        else {
            lblHornetQJndiName.setEnabled(false);
            lblHornetQJmsConnectionURL.setEnabled(false);
            txtHornetQConnectionFactory.setEnabled(false);
            txtHornetQConnectionFactoryURL.setEnabled(false);
            txtHornetQXAConnectionFactory.setEnabled(false);
            txtHornetQXAConnectionFactoryURL.setEnabled(false);
        }
    }
                 
    private void okButtonActionPerformed() {
        try {
            // TIBCO EMS
            if (chkCreateTibco.isSelected()) {
                ConnectionHelper.createJndiFileContext("com.tibco.tibjms.TibjmsConnectionFactory", txtTibjmsGenericConnectionFactory.getText(), txtTibjmsGenericConnectionFactoryURL.getText());
                ConnectionHelper.createJndiFileContext("com.tibco.tibjms.TibjmsQueueConnectionFactory", txtTibjmsQueueConnectionFactory.getText(), txtTibjmsQueueConnectionFactoryURL.getText());
                ConnectionHelper.createJndiFileContext("com.tibco.tibjms.TibjmsTopicConnectionFactory", txtTibjmsTopicConnectionFactory.getText(), txtTibjmsTopicConnectionFactoryURL.getText());
                ConnectionHelper.createJndiFileContext("com.tibco.tibjms.TibjmsXAConnectionFactory", txtTibjmsXAGenericConnectionFactory.getText(), txtTibjmsXAGenericConnectionFactoryURL.getText());
                ConnectionHelper.createJndiFileContext("com.tibco.tibjms.TibjmsXAQueueConnectionFactory", txtTibjmsXAQueueConnectionFactory.getText(), txtTibjmsXAQueueConnectionFactoryURL.getText());
                ConnectionHelper.createJndiFileContext("com.tibco.tibjms.TibjmsXATopicConnectionFactory", txtTibjmsXATopicConnectionFactory.getText(), txtTibjmsXATopicConnectionFactoryURL.getText());
            }
            // Apache ActiveMQ
            if (chkCreateActiveMQ.isSelected()) {
                ConnectionHelper.createJndiFileContext("org.apache.activemq.ActiveMQConnectionFactory", txtActiveMQConnectionFactory.getText(), txtActiveMQConnectionFactoryURL.getText());
                ConnectionHelper.createJndiFileContext("org.apache.activemq.ActiveMQXAConnectionFactory", txtActiveMQXAConnectionFactory.getText(), txtActiveMQXAConnectionFactoryURL.getText());
            }
            // HornetQ
            if (chkCreateHornetQ.isSelected()) {
                ConnectionHelper.createJndiFileContext("org.hornetq.jms.client.HornetQConnectionFactory", txtHornetQConnectionFactory.getText(), txtHornetQConnectionFactoryURL.getText());
                ConnectionHelper.createJndiFileContext("org.hornetq.jms.client.HornetQXAConnectionFactory", txtHornetQXAConnectionFactory.getText(), txtHornetQXAConnectionFactoryURL.getText());
            }
        }
        catch (NamingException ne)
        {
            if (javax.naming.NameAlreadyBoundException.class.isInstance(ne))
            {
                JOptionPane.showMessageDialog(
                        this,
                        "The JNDI ConnectionFactory name is already in the JNDI location:\n" +
                                ConnectionHelper.JNDI_FILE_URL + "/.binding\n\n" +
                                "Please edit the file directly using a text editor, or delete the file and try again.",
                        "JNDI Name Already Bound",
                        JOptionPane.WARNING_MESSAGE);

            }
            else ne.printStackTrace();
        }
        catch (JMSException je) {
            je.printStackTrace();
        }
        this.dispose();
    }

    private void cancelButtonActionPerformed() {
        this.dispose();
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        scrollPane = new JScrollPane();
        contentPanel = new JPanel();
        chkCreateTibco = new JCheckBox();
        lblTibJndiName = new JLabel();
        lblTibJmsConnectionURL = new JLabel();
        txtTibjmsGenericConnectionFactory = new JTextField();
        txtTibjmsGenericConnectionFactoryURL = new JTextField();
        txtTibjmsQueueConnectionFactory = new JTextField();
        txtTibjmsQueueConnectionFactoryURL = new JTextField();
        chkCreateActiveMQ = new JCheckBox();
        txtTibjmsTopicConnectionFactory = new JTextField();
        txtTibjmsTopicConnectionFactoryURL = new JTextField();
        txtTibjmsXAGenericConnectionFactory = new JTextField();
        txtTibjmsXAGenericConnectionFactoryURL = new JTextField();
        txtTibjmsXAQueueConnectionFactory = new JTextField();
        txtTibjmsXAQueueConnectionFactoryURL = new JTextField();
        txtTibjmsXATopicConnectionFactory = new JTextField();
        txtTibjmsXATopicConnectionFactoryURL = new JTextField();
        lblAmqJndiName = new JLabel();
        lblAmqJmsConnectionURL = new JLabel();
        txtActiveMQConnectionFactory = new JTextField();
        txtActiveMQConnectionFactoryURL = new JTextField();
        txtActiveMQXAConnectionFactory = new JTextField();
        txtActiveMQXAConnectionFactoryURL = new JTextField();
        chkCreateHornetQ = new JCheckBox();
        lblHornetQJndiName = new JLabel();
        lblHornetQJmsConnectionURL = new JLabel();
        txtHornetQConnectionFactory = new JTextField();
        txtHornetQConnectionFactoryURL = new JTextField();
        txtHornetQXAConnectionFactory = new JTextField();
        txtHornetQXAConnectionFactoryURL = new JTextField();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Create Local File JNDI");
        setResizable(false);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setLayout(new BorderLayout());

            //======== scrollPane ========
            {
                scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                //======== contentPanel ========
                {
                    contentPanel.setLayout(new FormLayout(
                        "2dlu, $lcgap, default, $lcgap, 250dlu, $lcgap, 2dlu",
                        "2dlu, 8*($lgap, default), $ugap, 3*(default, $lgap), default, $ugap, 4*(default, $lgap), 5dlu"));

                    //---- chkCreateTibco ----
                    chkCreateTibco.setText("Create local JNDI entry for TIBCO EMS");
                    chkCreateTibco.setSelected(true);
                    chkCreateTibco.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            chkCreateTibcoItemStateChanged();
                        }
                    });
                    contentPanel.add(chkCreateTibco, cc.xywh(3, 3, 3, 1));

                    //---- lblTibJndiName ----
                    lblTibJndiName.setText("JNDI Conneciton Factory Name");
                    lblTibJndiName.setFont(new Font("Tahoma", Font.BOLD, 11));
                    contentPanel.add(lblTibJndiName, cc.xy(3, 5, CellConstraints.CENTER, CellConstraints.DEFAULT));

                    //---- lblTibJmsConnectionURL ----
                    lblTibJmsConnectionURL.setText("JMS Connection URL");
                    lblTibJmsConnectionURL.setFont(new Font("Tahoma", Font.BOLD, 11));
                    contentPanel.add(lblTibJmsConnectionURL, cc.xy(5, 5, CellConstraints.CENTER, CellConstraints.DEFAULT));

                    //---- txtTibjmsGenericConnectionFactory ----
                    txtTibjmsGenericConnectionFactory.setText("TibjmsGenericConnectionFactory");
                    txtTibjmsGenericConnectionFactory.setEditable(false);
                    contentPanel.add(txtTibjmsGenericConnectionFactory, cc.xy(3, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtTibjmsGenericConnectionFactoryURL, cc.xy(5, 7));

                    //---- txtTibjmsQueueConnectionFactory ----
                    txtTibjmsQueueConnectionFactory.setText("TibjmsQueueConnectionFactory");
                    txtTibjmsQueueConnectionFactory.setEditable(false);
                    contentPanel.add(txtTibjmsQueueConnectionFactory, cc.xy(3, 9, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtTibjmsQueueConnectionFactoryURL, cc.xy(5, 9));

                    //---- chkCreateActiveMQ ----
                    chkCreateActiveMQ.setText("Create local JNDI entry for Apache ActiveMQ");
                    chkCreateActiveMQ.setSelected(true);
                    chkCreateActiveMQ.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            chkCreateActiveMQItemStateChanged();
                        }
                    });
                    contentPanel.add(chkCreateActiveMQ, cc.xywh(3, 19, 3, 1));

                    //---- txtTibjmsTopicConnectionFactory ----
                    txtTibjmsTopicConnectionFactory.setText("TibjmsTopicConnectionFactory");
                    txtTibjmsTopicConnectionFactory.setEditable(false);
                    contentPanel.add(txtTibjmsTopicConnectionFactory, cc.xy(3, 11, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtTibjmsTopicConnectionFactoryURL, cc.xy(5, 11));

                    //---- txtTibjmsXAGenericConnectionFactory ----
                    txtTibjmsXAGenericConnectionFactory.setText("TibjmsXAGenericConnectionFactory");
                    txtTibjmsXAGenericConnectionFactory.setEditable(false);
                    contentPanel.add(txtTibjmsXAGenericConnectionFactory, cc.xy(3, 13, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtTibjmsXAGenericConnectionFactoryURL, cc.xy(5, 13));

                    //---- txtTibjmsXAQueueConnectionFactory ----
                    txtTibjmsXAQueueConnectionFactory.setText("TibjmsXAQueueConnectionFactory");
                    txtTibjmsXAQueueConnectionFactory.setEditable(false);
                    contentPanel.add(txtTibjmsXAQueueConnectionFactory, cc.xy(3, 15, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtTibjmsXAQueueConnectionFactoryURL, cc.xy(5, 15));

                    //---- txtTibjmsXATopicConnectionFactory ----
                    txtTibjmsXATopicConnectionFactory.setText("TibjmsXATopicConnectionFactory");
                    txtTibjmsXATopicConnectionFactory.setEditable(false);
                    contentPanel.add(txtTibjmsXATopicConnectionFactory, cc.xy(3, 17, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtTibjmsXATopicConnectionFactoryURL, cc.xy(5, 17));

                    //---- lblAmqJndiName ----
                    lblAmqJndiName.setText("JNDI Conneciton Factory Name");
                    lblAmqJndiName.setFont(new Font("Tahoma", Font.BOLD, 11));
                    contentPanel.add(lblAmqJndiName, cc.xy(3, 21, CellConstraints.CENTER, CellConstraints.DEFAULT));

                    //---- lblAmqJmsConnectionURL ----
                    lblAmqJmsConnectionURL.setText("JMS Connection URL");
                    lblAmqJmsConnectionURL.setFont(new Font("Tahoma", Font.BOLD, 11));
                    contentPanel.add(lblAmqJmsConnectionURL, cc.xy(5, 21, CellConstraints.CENTER, CellConstraints.DEFAULT));

                    //---- txtActiveMQConnectionFactory ----
                    txtActiveMQConnectionFactory.setText("ActiveMQConnectionFactory");
                    txtActiveMQConnectionFactory.setEditable(false);
                    contentPanel.add(txtActiveMQConnectionFactory, cc.xy(3, 23, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtActiveMQConnectionFactoryURL, cc.xy(5, 23));

                    //---- txtActiveMQXAConnectionFactory ----
                    txtActiveMQXAConnectionFactory.setText("ActiveMQXAConnectionFactory");
                    txtActiveMQXAConnectionFactory.setEditable(false);
                    contentPanel.add(txtActiveMQXAConnectionFactory, cc.xy(3, 25, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtActiveMQXAConnectionFactoryURL, cc.xy(5, 25));

                    //---- chkCreateHornetQ ----
                    chkCreateHornetQ.setText("Create local JNDI entry for HornetQ");
                    chkCreateHornetQ.setSelected(true);
                    chkCreateHornetQ.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            chkCreateHornetQItemStateChanged();
                        }
                    });
                    contentPanel.add(chkCreateHornetQ, cc.xywh(3, 27, 3, 1));

                    //---- lblHornetQJndiName ----
                    lblHornetQJndiName.setText("JNDI Conneciton Factory Name");
                    lblHornetQJndiName.setFont(new Font("Tahoma", Font.BOLD, 11));
                    contentPanel.add(lblHornetQJndiName, cc.xy(3, 29, CellConstraints.CENTER, CellConstraints.DEFAULT));

                    //---- lblHornetQJmsConnectionURL ----
                    lblHornetQJmsConnectionURL.setText("JMS Connection URL");
                    lblHornetQJmsConnectionURL.setFont(new Font("Tahoma", Font.BOLD, 11));
                    contentPanel.add(lblHornetQJmsConnectionURL, cc.xy(5, 29, CellConstraints.CENTER, CellConstraints.DEFAULT));

                    //---- txtHornetQConnectionFactory ----
                    txtHornetQConnectionFactory.setText("HornetQConnectionFactory");
                    txtHornetQConnectionFactory.setEditable(false);
                    contentPanel.add(txtHornetQConnectionFactory, cc.xy(3, 31, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtHornetQConnectionFactoryURL, cc.xy(5, 31));

                    //---- txtHornetQXAConnectionFactory ----
                    txtHornetQXAConnectionFactory.setText("HornetQXAConnectionFactory");
                    txtHornetQXAConnectionFactory.setEditable(false);
                    contentPanel.add(txtHornetQXAConnectionFactory, cc.xy(3, 33, CellConstraints.FILL, CellConstraints.DEFAULT));
                    contentPanel.add(txtHornetQXAConnectionFactoryURL, cc.xy(5, 33));
                }
                scrollPane.setViewportView(contentPanel);
            }
            dialogPane.add(scrollPane, BorderLayout.NORTH);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    "$glue, $button, $rgap, $button",
                    "pref"));

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okButtonActionPerformed();
                    }
                });
                buttonBar.add(okButton, cc.xy(2, 1));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButtonActionPerformed();
                    }
                });
                buttonBar.add(cancelButton, cc.xy(4, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JScrollPane scrollPane;
    private JPanel contentPanel;
    private JCheckBox chkCreateTibco;
    private JLabel lblTibJndiName;
    private JLabel lblTibJmsConnectionURL;
    private JTextField txtTibjmsGenericConnectionFactory;
    private JTextField txtTibjmsGenericConnectionFactoryURL;
    private JTextField txtTibjmsQueueConnectionFactory;
    private JTextField txtTibjmsQueueConnectionFactoryURL;
    private JCheckBox chkCreateActiveMQ;
    private JTextField txtTibjmsTopicConnectionFactory;
    private JTextField txtTibjmsTopicConnectionFactoryURL;
    private JTextField txtTibjmsXAGenericConnectionFactory;
    private JTextField txtTibjmsXAGenericConnectionFactoryURL;
    private JTextField txtTibjmsXAQueueConnectionFactory;
    private JTextField txtTibjmsXAQueueConnectionFactoryURL;
    private JTextField txtTibjmsXATopicConnectionFactory;
    private JTextField txtTibjmsXATopicConnectionFactoryURL;
    private JLabel lblAmqJndiName;
    private JLabel lblAmqJmsConnectionURL;
    private JTextField txtActiveMQConnectionFactory;
    private JTextField txtActiveMQConnectionFactoryURL;
    private JTextField txtActiveMQXAConnectionFactory;
    private JTextField txtActiveMQXAConnectionFactoryURL;
    private JCheckBox chkCreateHornetQ;
    private JLabel lblHornetQJndiName;
    private JLabel lblHornetQJmsConnectionURL;
    private JTextField txtHornetQConnectionFactory;
    private JTextField txtHornetQConnectionFactoryURL;
    private JTextField txtHornetQXAConnectionFactory;
    private JTextField txtHornetQXAConnectionFactoryURL;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
