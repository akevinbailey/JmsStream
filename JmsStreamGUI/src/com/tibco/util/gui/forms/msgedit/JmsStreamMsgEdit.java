/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Thu Jan 29 14:10:57 CET 2009
 */

package com.tibco.util.gui.forms.msgedit;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.util.gui.helper.FileNameFilter;
import com.tibco.util.jmshelper.MessageFile;
import com.tibco.util.jmshelper.MsgPropStruct;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.1
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "unchecked", "unused", "FieldCanBeLocal", "CanBeFinal", "deprecation"})
public class JmsStreamMsgEdit extends JDialog
{
    public JmsStreamMsgEdit(Frame owner, String strFileURI, String strEncoding) {
        super(owner);
        _strEncoding = strEncoding;
        _strMsgFileURI = strFileURI;
        initComponents();
    }

    public JmsStreamMsgEdit(Dialog owner, String strFileURI, String strEncoding) {
        super(owner);
        _strEncoding = strEncoding;
        _strMsgFileURI = strFileURI;
        initComponents();
    }

    public JmsStreamMsgEdit(Frame owner, String strFileURI[], String strEncoding) {
        super(owner);
        _strEncoding = strEncoding;
        _aryMsgFileURI = strFileURI;
        initComponents();
    }

    public JmsStreamMsgEdit(Dialog owner, String strFileURI[], String strEncoding) {
        super(owner);
        _strEncoding = strEncoding;
        _aryMsgFileURI = strFileURI;
        initComponents();
    }

    public boolean isCanceled() {
        return _blnCanceled;
    }

    public String getFileURI()
    {
        return _strMsgFileURI;
    }

    private void newData() {
        lblMsgNum.setText("Message " + (_msgFile.getCurrentMsgIndex() + 1) + " of " +
                ((_msgFile.getMessageCount() < (_msgFile.getCurrentMsgIndex() + 1)) ? (_msgFile.getCurrentMsgIndex() + 1) :  _msgFile.getMessageCount()));
        txtJmsMsgId.setText("");
        txtReceiveTime.setText("");
        txtJmsServerTime.setText("");
        txtOriginationTime.setText("");
        txtMsgSelector.setText("");
        cboMessageType.setSelectedIndex(0);
        chkCommitTrans.setSelected(false);
        rdoQueue.setSelected(true);
        txtJmsDest.setText("");
        txtJmsReplyTo.setText("");
        txtJmsCorrId.setText("");
        cboJmsDeliveryMode.setSelectedIndex(0);
        txtJmsType.setText("");
        spnJmsExpiration.setValue(0);
        spnSleepTime.setValue(0);
        spnJmsExpiration.setValue(0);
        chkJmsPriority.setSelected(false);
        spnJmsPriority.setEnabled(false);
        spnJmsPriority.setValue(0);
        cboMessageType.setSelectedIndex(0);
        pnlJmsTextMsg.setTextData("");
        ((DefaultTableModel)tblMsgProperties.getModel()).setRowCount(0);
    }

    private void refreshData() {
        if (_msgFile != null) {
            lblMsgNum.setText("Message " + (_msgFile.getCurrentMsgIndex() + 1) + " of " +
                    ((_msgFile.getMessageCount() < (_msgFile.getCurrentMsgIndex() + 1)) ? (_msgFile.getCurrentMsgIndex() + 1) :  _msgFile.getMessageCount()));

            txtJmsMsgId.setText(_msgFile.getJmsMessageID());
            txtReceiveTime.setText(_msgFile.getReceiveTime());
            txtJmsServerTime.setText(_msgFile.getJmsServerTimestamp());
            txtOriginationTime.setText(_msgFile.getOriginationTime());
            txtMsgSelector.setText(_msgFile.getMessageSelector());
            cboMessageType.setSelectedItem(_msgFile.getMessageType().toString());
            chkCommitTrans.setSelected(_msgFile.getCommitTrans());
            switch (_msgFile.getDestinationType()) {
                case Queue:
                    rdoQueue.setSelected(true);
                    break;
                case Generic:
                    rdoGeneric.setSelected(true);
                    break;
                case Topic:
                    rdoTopic.setSelected(true);
                    break;
                case XA_Queue:
                    rdoXAQueue.setSelected(true);
                    break;
                case XA_Generic:
                    rdoXAGeneric.setSelected(true);
                    break;
                case XA_Topic:
                    rdoXATopic.setSelected(true);
                    break;
                default:
                    // Do nothing.
            }
            txtJmsDest.setText(_msgFile.getJmsDestination());
            txtJmsReplyTo.setText(_msgFile.getJmsReplyToDest());
            txtJmsCorrId.setText(_msgFile.getJmsCorrelationId());
            cboJmsDeliveryMode.setSelectedItem(_msgFile.getDeliveryMode().toString());
            txtJmsType.setText(_msgFile.getJmsType());
            spnJmsExpiration.setValue(_msgFile.getJmsExpiration());
            spnSleepTime.setValue(_msgFile.getSleepTime());

            if (_msgFile.getJmsPriority() == null) {
                chkJmsPriority.setSelected(false);
                spnJmsPriority.setEnabled(false);
                spnJmsPriority.setValue(0);
            }
            else {
                chkJmsPriority.setSelected(true);
                spnJmsPriority.setEnabled(true);
                spnJmsPriority.setValue(_msgFile.getJmsPriority());
            }

            cboMessageType.setSelectedItem(_msgFile.getMessageType());
            switch (_msgFile.getMessageType()) {
                case BytesMessage:
                    tabbedPane.setComponentAt(2, pnlJmsByteMsg);
                    pnlJmsByteMsg.setBase64(_msgFile.getBase64Body());
                    break;
                case MapMessage:
                    tabbedPane.setComponentAt(2, pnlJmsMapMsg);
                    pnlJmsMapMsg.setMapMsg(_msgFile.getMapMessageBody());
                    break;
                case ObjectMessage:
                    // TODO:  Create ObjectMessage Viewer
                    break;
                case StreamMessage:
                    // TODO:  Create StreamMessage Viewer
                    break;
                case TextMessage:
                    tabbedPane.setComponentAt(2, pnlJmsTextMsg);
                    pnlJmsTextMsg.setTextData(_msgFile.getTextBody());
                    break;
                default:
                    // Do nothing
            }

            // Remove previous table data
            ((DefaultTableModel)tblMsgProperties.getModel()).setRowCount(0);
            // Add user properties to tblMsgProperties table
            if (_msgFile.getMsgProperties() != null) {
                for (int i=0; i < _msgFile.getMsgProperties().size(); i++) {
                    Vector<String> vecMsgProp = new Vector<String>(3); // Number of columns
                    vecMsgProp.add(_msgFile.getMsgProperties().get(i).getName());           // Name
                    vecMsgProp.add(_msgFile.getMsgProperties().get(i).getType().toString());// Type
                    vecMsgProp.add(_msgFile.getMsgProperties().get(i).getValue());          // Value
                    ((DefaultTableModel)tblMsgProperties.getModel()).addRow(vecMsgProp);
                }
            }
        }
    }

    private void update() {
        _msgFile.setCommitTrans(chkCommitTrans.isSelected());
        _msgFile.setDeliveryMode(MessageFile.DeliveryType.valueOf(cboJmsDeliveryMode.getSelectedItem().toString()));
        _msgFile.setJmsCorrelationId(txtJmsCorrId.getText());
        _msgFile.setJmsDestination(txtJmsDest.getText());
        _msgFile.setJmsExpiration(Long.valueOf(spnJmsExpiration.getValue().toString()));
        _msgFile.setJmsReplyToDest(txtJmsReplyTo.getText());
        _msgFile.setJmsType(txtJmsType.getText());
        _msgFile.setSleepTime(Long.valueOf(spnSleepTime.getValue().toString()));

        if (chkJmsPriority.isSelected())
            _msgFile.setJmsPriority(Integer.parseInt(spnJmsPriority.getValue().toString()));
        else
            _msgFile.setJmsPriority(null);

        if (rdoGeneric.isSelected())
            _msgFile.setDestinationType(MessageFile.DestType.Generic);
        else if (rdoQueue.isSelected())
            _msgFile.setDestinationType(MessageFile.DestType.Queue);
        else if (rdoTopic.isSelected())
            _msgFile.setDestinationType(MessageFile.DestType.Topic);
        else if (rdoXAGeneric.isSelected())
            _msgFile.setDestinationType(MessageFile.DestType.XA_Generic);
        else if (rdoXAQueue.isSelected())
            _msgFile.setDestinationType(MessageFile.DestType.XA_Queue);
        else if (rdoXATopic.isSelected())
            _msgFile.setDestinationType(MessageFile.DestType.XA_Topic);
        else
            _msgFile.setDestinationType(MessageFile.DestType.Generic);

        switch (MessageFile.MsgType.valueOf(cboMessageType.getSelectedItem().toString())) {
            case BytesMessage:
                _msgFile.setMessageType(MessageFile.MsgType.BytesMessage);
                _msgFile.setBase64Body(pnlJmsByteMsg.getBase64());
                break;
            case MapMessage:
                _msgFile.setMessageType(MessageFile.MsgType.MapMessage);
                _msgFile.setMapMessageBody(pnlJmsMapMsg.getMapMsg());
                break;
            case ObjectMessage:
                // TODO:  Create ObjectMessage Writer
                break;
            case StreamMessage:
                // TODO:  Create StreamMessage Writer
                break;
            case TextMessage:
                _msgFile.setMessageType(MessageFile.MsgType.TextMessage);
                _msgFile.setTextBody(pnlJmsTextMsg.getTextData());
                break;
            default:
                // Do nothing
        }

        Vector vecData = ((DefaultTableModel)tblMsgProperties.getModel()).getDataVector();
        ArrayList<MsgPropStruct> alMsgProp = new ArrayList<MsgPropStruct>();
        for (int i=0; i < vecData.size(); i++) {
            alMsgProp.add(new MsgPropStruct(((Vector)vecData.elementAt(i)).elementAt(0).toString()      // Name
                                            , ((Vector)vecData.elementAt(i)).elementAt(1).toString()    // Type
                                            , ((Vector)vecData.elementAt(i)).elementAt(2).toString())); // Value
        }
        _msgFile.setMsgProperties(alMsgProp);
        _msgFile.updateMessage();
    }

    private void cboMessageTypeItemStateChanged(ItemEvent e) {
        if (((JComboBox)e.getItemSelectable()).getSelectedItem().toString().equals("TextMessage")) {
            tabbedPane.setComponentAt(2, pnlJmsTextMsg);
        }
        if (((JComboBox)e.getItemSelectable()).getSelectedItem().toString().equals("MapMessage")) {
            tabbedPane.setComponentAt(2, pnlJmsMapMsg);
        }
        if (((JComboBox)e.getItemSelectable()).getSelectedItem().toString().equals("BytesMessage")) {
            //tabbedPane.remove(2);
            tabbedPane.setComponentAt(2, pnlJmsByteMsg);
         }
    }

    private void btnAddRowActionPerformed() {
        if (tblMsgProperties.getSelectedRow() == -1) {
            _tmodMsgProps.addRow(new Vector());
        }
        else {
            _tmodMsgProps.insertRow(tblMsgProperties.getSelectedRow() + 1, new Vector());
        }
    }

    private void btnRemoveRowActionPerformed() {
        if (tblMsgProperties.getSelectedRow() > -1) {
            _tmodMsgProps.removeRow(tblMsgProperties.getSelectedRow());
        }
    }

    private void btnUpActionPerformed() {
        int intIndex = tblMsgProperties.getSelectedRow();
        if (intIndex > -1 && (intIndex - 1) > -1 ) {
            _tmodMsgProps.moveRow(intIndex, intIndex, intIndex - 1);
            tblMsgProperties.setRowSelectionInterval(intIndex - 1, intIndex - 1);
        }
    }

    private void btnDownActionPerformed() {
        int intIndex = tblMsgProperties.getSelectedRow();
        if (intIndex > -1 && (intIndex + 1) < tblMsgProperties.getRowCount()) {
            _tmodMsgProps.moveRow(intIndex, intIndex, intIndex + 1);    
            tblMsgProperties.setRowSelectionInterval(intIndex + 1, intIndex + 1);
        }
    }

    private void panelPropsComponentHidden() {
        if (tblMsgProperties.isEditing())
            tblMsgProperties.getCellEditor().stopCellEditing();
        tblMsgProperties.clearSelection();
    }

    private void cancelButtonActionPerformed() {
        _blnCanceled = true;
        this.dispose();
    }

    private void btnNextActionPerformed() {
        try {
            update();
            _msgFile.nextMsg();
            refreshData();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.toString(), "Message Editor Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnPreviousActionPerformed() {
        try {
            update();
            _msgFile.previousMsg();
            refreshData();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.toString(), "Message Editor Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnNewMsgActionPerformed() {
        try {
            if (_msgFile.getMessageCount() != 0)
                update();
            _msgFile.addNewMsg();
            tabbedPane.setSelectedIndex(0);
            refreshData();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.toString(), "Message Editor Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnDeleteMsgActionPerformed() {
        try {
            _msgFile.deleteMsg();
            tabbedPane.setSelectedIndex(0);
            if (_msgFile.getMessageCount() == 0)
                newData();
            else
                refreshData();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.toString(), "Message Editor Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void okButtonActionPerformed() { // Save
        try {
            update();
            _msgFile.saveMsg();
            _blnCanceled = false;
            this.dispose();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.toString(), "Message Editor Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chkJmsPriorityItemStateChanged(ItemEvent e) {
        switch (e.getStateChange()) {
            case ItemEvent.SELECTED :
                spnJmsPriority.setEnabled(true);
                break;
            case ItemEvent.DESELECTED :
                spnJmsPriority.setEnabled(false);
                spnJmsPriority.setValue(0);
                break;
            default:
                // do nothing
        }
    }

    private void showChooseDialog()
    {
        Object objChoice;

        //**** Get JmsStream Message File ****
        objChoice = JOptionPane.showInputDialog(
                this.getParent(),
                "Would you like to do?",
                "JmsStream Message File",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[] {"Edit the Message File in JmsStream Config.",
                "Edit the current Message File."},
                "Edit the Message File in JmsStream Config.");

        if (objChoice != null && objChoice.toString().equals("Edit the Message File in JmsStream Config.")) {
            // The Message File form the JmsStream Config is fist in the array.
            _strMsgFileURI = _aryMsgFileURI[0];
        }
        else if (objChoice != null && objChoice.toString().equals("Edit the current Message File.")){
            // The Message File form the JmsStream Config is second in the array.
            _strMsgFileURI = _aryMsgFileURI[1];
        }
    }

    private void tblMsgPropertiesFocusLost(FocusEvent e) {
        // Add focus listener to the table cells in order to stop editing when the cell looses focus
        JTable theTable = (JTable)e.getSource();
        Component comp = theTable.getEditorComponent();
        if (comp != null) {
            boolean alreadySet = false;
            FocusListener listeners[] = comp.getFocusListeners();
            for (int i = 0, m = listeners.length; i < m; i++) {
                FocusListener l = listeners[i];
                if (l instanceof FocusAdapter) {
                    alreadySet = true;
                    break;
                }
            }
            if (!alreadySet) {
                comp.addFocusListener(
                        new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                TableCellEditor ed = tblMsgProperties.getCellEditor();
                                if (ed != null) {
                                    ed.stopCellEditing();
                                }
                            }
                        }
                );
            }
        }
    }

    private void initComponents() {
        pnlJmsTextMsg = new PanelJmsTextMsg();
        pnlJmsMapMsg = new PanelJmsMapMsg();
        pnlJmsByteMsg = new PanelJmsByteMsg();

        this.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        tabbedPane = new JTabbedPane();
        panelJMS = new JPanel();
        lblJmsMsgId = new JLabel();
        txtJmsMsgId = new JTextField();
        lblReceiveTime = new JLabel();
        txtReceiveTime = new JTextField();
        lblJmsServerTime = new JLabel();
        txtJmsServerTime = new JTextField();
        lblOriginationTime = new JLabel();
        txtOriginationTime = new JTextField();
        lblMsgSelector = new JLabel();
        txtMsgSelector = new JTextField();
        lblMsgType = new JLabel();
        cboMessageType = new JComboBox();
        chkJmsPriority = new JCheckBox();
        spnJmsPriority = new JSpinner();
        lblDestType = new JLabel();
        rdoQueue = new JRadioButton();
        rdoTopic = new JRadioButton();
        rdoGeneric = new JRadioButton();
        chkCommitTrans = new JCheckBox();
        rdoXAQueue = new JRadioButton();
        rdoXATopic = new JRadioButton();
        rdoXAGeneric = new JRadioButton();
        lblJmsDest = new JLabel();
        txtJmsDest = new JTextField();
        lblJmsReplyTo = new JLabel();
        txtJmsReplyTo = new JTextField();
        lblJmsCorrId = new JLabel();
        txtJmsCorrId = new JTextField();
        lblJmsDeliveryMode = new JLabel();
        cboJmsDeliveryMode = new JComboBox();
        lblJmsType = new JLabel();
        txtJmsType = new JTextField();
        lblJmsExpiration = new JLabel();
        spnJmsExpiration = new JSpinner();
        lblmsec1 = new JLabel();
        lblSleep = new JLabel();
        spnSleepTime = new JSpinner();
        lblmsec2 = new JLabel();
        panelProps = new JPanel();
        pnlTableButtons = new JPanel();
        btnAddRow = new JButton();
        btnRemoveRow = new JButton();
        btnUp = new JButton();
        btnDown = new JButton();
        scrMsgProperties = new JScrollPane();
        tblMsgProperties = new JTable();
        panelBody = new JPanel();
        buttonBar = new JPanel();
        btnNewMsg = new JButton();
        btnDeleteMsg = new JButton();
        btnPrevious = new JButton();
        btnNext = new JButton();
        lblMsgNum = new JLabel();
        okButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 510));
        setTitle("JMS Message Editor");
        setName("MessageEditor");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridLayout());

                //======== tabbedPane ========
                {
                    tabbedPane.setFont(UIManager.getFont("TabbedPane.font"));

                    //======== panelJMS ========
                    {
                        panelJMS.setLayout(new FormLayout(
                            "2dlu, $lcgap, right:default, $lcgap, 2*(default, 1dlu), default, 50dlu, 1dlu, default, 1dlu, 20dlu, $lcgap, 2dlu",
                            "3dlu, 14*($lgap, default), $lgap, 2dlu"));

                        //---- lblJmsMsgId ----
                        lblJmsMsgId.setText("JMS Message ID");
                        lblJmsMsgId.setLabelFor(txtJmsMsgId);
                        panelJMS.add(lblJmsMsgId, cc.xy(3, 3));

                        //---- txtJmsMsgId ----
                        txtJmsMsgId.setEditable(false);
                        panelJMS.add(txtJmsMsgId, cc.xywh(5, 3, 10, 1));

                        //---- lblReceiveTime ----
                        lblReceiveTime.setText("Receive Timestamp");
                        lblReceiveTime.setLabelFor(txtReceiveTime);
                        panelJMS.add(lblReceiveTime, cc.xy(3, 5));

                        //---- txtReceiveTime ----
                        txtReceiveTime.setEditable(false);
                        panelJMS.add(txtReceiveTime, cc.xywh(5, 5, 10, 1));

                        //---- lblJmsServerTime ----
                        lblJmsServerTime.setText("JMS Server Timestamp");
                        lblJmsServerTime.setLabelFor(txtJmsServerTime);
                        panelJMS.add(lblJmsServerTime, cc.xy(3, 7));

                        //---- txtJmsServerTime ----
                        txtJmsServerTime.setEditable(false);
                        panelJMS.add(txtJmsServerTime, cc.xywh(5, 7, 10, 1));

                        //---- lblOriginationTime ----
                        lblOriginationTime.setText("Origination Timestamp");
                        lblOriginationTime.setLabelFor(txtOriginationTime);
                        panelJMS.add(lblOriginationTime, cc.xy(3, 9));

                        //---- txtOriginationTime ----
                        txtOriginationTime.setEditable(false);
                        panelJMS.add(txtOriginationTime, cc.xywh(5, 9, 10, 1));

                        //---- lblMsgSelector ----
                        lblMsgSelector.setText("Message Selector");
                        lblMsgSelector.setLabelFor(txtMsgSelector);
                        panelJMS.add(lblMsgSelector, cc.xy(3, 11));

                        //---- txtMsgSelector ----
                        txtMsgSelector.setEditable(false);
                        panelJMS.add(txtMsgSelector, cc.xywh(5, 11, 10, 1));

                        //---- lblMsgType ----
                        lblMsgType.setText("Message Type");
                        lblMsgType.setLabelFor(cboMessageType);
                        panelJMS.add(lblMsgType, cc.xy(3, 13));

                        //---- cboMessageType ----
                        cboMessageType.setModel(new DefaultComboBoxModel(new String[] {
                            "TextMessage",
                            "MapMessage",
                            "BytesMessage"
                        }));
                        cboMessageType.setMaximumRowCount(3);
                        cboMessageType.setBackground(SystemColor.controlLtHighlight);
                        cboMessageType.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                cboMessageTypeItemStateChanged(e);
                            }
                        });
                        panelJMS.add(cboMessageType, cc.xywh(5, 13, 4, 1));

                        //---- chkJmsPriority ----
                        chkJmsPriority.setText("Priority");
                        chkJmsPriority.setHorizontalAlignment(SwingConstants.RIGHT);
                        chkJmsPriority.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkJmsPriorityItemStateChanged(e);
                            }
                        });
                        panelJMS.add(chkJmsPriority, cc.xy(10, 13));

                        //---- spnJmsPriority ----
                        spnJmsPriority.setModel(new SpinnerNumberModel(0, 0, 9, 1));
                        spnJmsPriority.setEnabled(false);
                        panelJMS.add(spnJmsPriority, cc.xy(12, 13));

                        //---- lblDestType ----
                        lblDestType.setText("Destination Type");
                        panelJMS.add(lblDestType, cc.xy(3, 15));

                        //---- rdoQueue ----
                        rdoQueue.setText("Queue");
                        rdoQueue.setSelected(true);
                        panelJMS.add(rdoQueue, cc.xy(5, 15));

                        //---- rdoTopic ----
                        rdoTopic.setText("Topic");
                        panelJMS.add(rdoTopic, cc.xy(7, 15));

                        //---- rdoGeneric ----
                        rdoGeneric.setText("Generic");
                        panelJMS.add(rdoGeneric, cc.xywh(9, 15, 2, 1));

                        //---- chkCommitTrans ----
                        chkCommitTrans.setText("Commit Transaction");
                        chkCommitTrans.setToolTipText("If transactional, commit the transaction after this message.");
                        panelJMS.add(chkCommitTrans, cc.xy(12, 15));

                        //---- rdoXAQueue ----
                        rdoXAQueue.setText("XA Queue");
                        panelJMS.add(rdoXAQueue, cc.xy(5, 17));

                        //---- rdoXATopic ----
                        rdoXATopic.setText("XA Topic");
                        panelJMS.add(rdoXATopic, cc.xy(7, 17));

                        //---- rdoXAGeneric ----
                        rdoXAGeneric.setText("XA Generic");
                        panelJMS.add(rdoXAGeneric, cc.xywh(9, 17, 2, 1));

                        //---- lblJmsDest ----
                        lblJmsDest.setText("Destination");
                        lblJmsDest.setLabelFor(txtJmsDest);
                        panelJMS.add(lblJmsDest, cc.xy(3, 19));
                        panelJMS.add(txtJmsDest, cc.xywh(5, 19, 10, 1));

                        //---- lblJmsReplyTo ----
                        lblJmsReplyTo.setText("Reply To");
                        lblJmsReplyTo.setLabelFor(txtJmsReplyTo);
                        panelJMS.add(lblJmsReplyTo, cc.xy(3, 21));
                        panelJMS.add(txtJmsReplyTo, cc.xywh(5, 21, 10, 1));

                        //---- lblJmsCorrId ----
                        lblJmsCorrId.setText("Correlation ID");
                        lblJmsCorrId.setLabelFor(txtJmsCorrId);
                        panelJMS.add(lblJmsCorrId, cc.xy(3, 23));
                        panelJMS.add(txtJmsCorrId, cc.xywh(5, 23, 10, 1));

                        //---- lblJmsDeliveryMode ----
                        lblJmsDeliveryMode.setText("Delivery Mode");
                        lblJmsDeliveryMode.setLabelFor(cboJmsDeliveryMode);
                        panelJMS.add(lblJmsDeliveryMode, cc.xy(3, 25));

                        //---- cboJmsDeliveryMode ----
                        cboJmsDeliveryMode.setModel(new DefaultComboBoxModel(new String[] {
                            "NON_PERSISTENT",
                            "PERSISTENT"
                        }));
                        cboJmsDeliveryMode.setMaximumRowCount(2);
                        cboJmsDeliveryMode.setBackground(SystemColor.controlLtHighlight);
                        panelJMS.add(cboJmsDeliveryMode, cc.xywh(5, 25, 6, 1));

                        //---- lblJmsType ----
                        lblJmsType.setText("JMS Type");
                        lblJmsType.setLabelFor(txtJmsType);
                        panelJMS.add(lblJmsType, cc.xy(3, 27));
                        panelJMS.add(txtJmsType, cc.xywh(5, 27, 10, 1));

                        //---- lblJmsExpiration ----
                        lblJmsExpiration.setText("Expiration");
                        lblJmsExpiration.setLabelFor(spnJmsExpiration);
                        panelJMS.add(lblJmsExpiration, cc.xy(3, 29));
                        panelJMS.add(spnJmsExpiration, cc.xywh(5, 29, 3, 1));

                        //---- lblmsec1 ----
                        lblmsec1.setText("msec");
                        panelJMS.add(lblmsec1, cc.xy(9, 29));

                        //---- lblSleep ----
                        lblSleep.setText("Sleep Time");
                        lblSleep.setToolTipText("The number of msec to\nsleep after this message");
                        lblSleep.setLabelFor(spnSleepTime);
                        panelJMS.add(lblSleep, cc.xy(10, 29, CellConstraints.RIGHT, CellConstraints.DEFAULT));

                        //---- spnSleepTime ----
                        spnSleepTime.setModel(new SpinnerNumberModel(0, 0, null, 500));
                        panelJMS.add(spnSleepTime, cc.xy(12, 29));

                        //---- lblmsec2 ----
                        lblmsec2.setText("msec");
                        panelJMS.add(lblmsec2, cc.xy(14, 29));
                    }
                    tabbedPane.addTab("JMS Properties", panelJMS);


                    //======== panelProps ========
                    {
                        panelProps.addComponentListener(new ComponentAdapter() {
                            @Override
                            public void componentHidden(ComponentEvent e) {
                                panelPropsComponentHidden();
                            }
                        });
                        panelProps.setLayout(new FormLayout(
                            "2dlu, default, 2dlu, default:grow, $lcgap, 2dlu",
                            "2dlu, $lgap, fill:default:grow, $lgap, 2dlu"));

                        //======== pnlTableButtons ========
                        {
                            pnlTableButtons.setLayout(new FormLayout(
                                "14dlu",
                                "10dlu, bottom:14dlu, top:14dlu, $ugap, bottom:14dlu, top:14dlu"));

                            //---- btnAddRow ----
                            btnAddRow.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordAdd1.png")));
                            btnAddRow.setFocusPainted(false);
                            btnAddRow.setMargin(new Insets(3, 13, 3, 14));
                            btnAddRow.setMaximumSize(new Dimension(41, 41));
                            btnAddRow.setMinimumSize(new Dimension(41, 41));
                            btnAddRow.setPreferredSize(new Dimension(41, 41));
                            btnAddRow.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnAddRowActionPerformed();
                                }
                            });
                            pnlTableButtons.add(btnAddRow, cc.xy(1, 2));

                            //---- btnRemoveRow ----
                            btnRemoveRow.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordDelete1.png")));
                            btnRemoveRow.setFocusPainted(false);
                            btnRemoveRow.setMargin(new Insets(3, 12, 3, 15));
                            btnRemoveRow.setMaximumSize(new Dimension(41, 41));
                            btnRemoveRow.setMinimumSize(new Dimension(41, 41));
                            btnRemoveRow.setPreferredSize(new Dimension(41, 41));
                            btnRemoveRow.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnRemoveRowActionPerformed();
                                }
                            });
                            pnlTableButtons.add(btnRemoveRow, cc.xy(1, 3));

                            //---- btnUp ----
                            btnUp.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/moveUp1.png")));
                            btnUp.setFocusPainted(false);
                            btnUp.setMargin(new Insets(3, 13, 3, 14));
                            btnUp.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnUpActionPerformed();
                                }
                            });
                            pnlTableButtons.add(btnUp, cc.xy(1, 5));

                            //---- btnDown ----
                            btnDown.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/moveDown1.png")));
                            btnDown.setFocusPainted(false);
                            btnDown.setMargin(new Insets(3, 13, 3, 14));
                            btnDown.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnDownActionPerformed();
                                }
                            });
                            pnlTableButtons.add(btnDown, cc.xy(1, 6));
                        }
                        panelProps.add(pnlTableButtons, cc.xy(2, 3));

                        //======== scrMsgProperties ========
                        {
                            scrMsgProperties.setAutoscrolls(true);

                            //---- tblMsgProperties ----
                            tblMsgProperties.setCellSelectionEnabled(true);
                            tblMsgProperties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            tblMsgProperties.setModel(new DefaultTableModel(
                                new Object[][] {
                                },
                                new String[] {
                                    "Name", "Type", "Value"
                                }
                            ) {
                                Class<?>[] columnTypes = new Class<?>[] {
                                    String.class, String.class, String.class
                                };
                                @Override
                                public Class<?> getColumnClass(int columnIndex) {
                                    return columnTypes[columnIndex];
                                }
                            });
                            {
                                TableColumnModel cm = tblMsgProperties.getColumnModel();
                                cm.getColumn(1).setResizable(false);
                                cm.getColumn(1).setCellEditor(new DefaultCellEditor(
                                    new JComboBox(new DefaultComboBoxModel(new String[] {
                                        "Boolean",
                                        "Character",
                                        "Double",
                                        "Float",
                                        "Integer",
                                        "Long",
                                        "Number",
                                        "String"
                                    }))));
                            }
                            tblMsgProperties.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                            tblMsgProperties.setAutoscrolls(false);
                            tblMsgProperties.addFocusListener(new FocusAdapter() {
                                @Override
                                public void focusLost(FocusEvent e) {
                                    tblMsgPropertiesFocusLost(e);
                                }
                            });
                            scrMsgProperties.setViewportView(tblMsgProperties);
                        }
                        panelProps.add(scrMsgProperties, cc.xy(4, 3));
                    }
                    tabbedPane.addTab("Message Properties", panelProps);


                    //======== panelBody ========
                    {
                        panelBody.setLayout(null);

                        { // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < panelBody.getComponentCount(); i++) {
                                Rectangle bounds = panelBody.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panelBody.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panelBody.setMinimumSize(preferredSize);
                            panelBody.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane.addTab("Message Body", panelBody);

                }
                contentPanel.add(tabbedPane);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    "$lcgap, default, 1dlu, default, $lcgap, $ugap, $lcgap, default, 1dlu, default, $lcgap, default:grow, $button, $rgap, $button",
                    "pref"));

                //---- btnNewMsg ----
                btnNewMsg.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgAdd.png")));
                btnNewMsg.setToolTipText("Create New Message");
                btnNewMsg.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnNewMsgActionPerformed();
                    }
                });
                buttonBar.add(btnNewMsg, cc.xy(2, 1));

                //---- btnDeleteMsg ----
                btnDeleteMsg.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgDelete.png")));
                btnDeleteMsg.setToolTipText("Delete Current Message");
                btnDeleteMsg.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnDeleteMsgActionPerformed();
                    }
                });
                buttonBar.add(btnDeleteMsg, cc.xy(4, 1));

                //---- btnPrevious ----
                btnPrevious.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgPrevious.png")));
                btnPrevious.setToolTipText("Previous Message");
                btnPrevious.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnPreviousActionPerformed();
                    }
                });
                buttonBar.add(btnPrevious, cc.xy(8, 1));

                //---- btnNext ----
                btnNext.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgNext.png")));
                btnNext.setToolTipText("Next Message");
                btnNext.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnNextActionPerformed();
                    }
                });
                buttonBar.add(btnNext, cc.xy(10, 1));

                //---- lblMsgNum ----
                lblMsgNum.setText("Message 0 of 0");
                buttonBar.add(lblMsgNum, cc.xy(12, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));

                //---- okButton ----
                okButton.setText("Save");
                okButton.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgSave.png")));
                okButton.setToolTipText("Save Changes");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okButtonActionPerformed();
                    }
                });
                buttonBar.add(okButton, cc.xy(13, 1));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/cancel.png")));
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButtonActionPerformed();
                    }
                });
                buttonBar.add(cancelButton, cc.xy(15, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        //---- btngrpDestType ----
        ButtonGroup btngrpDestType = new ButtonGroup();
        btngrpDestType.add(rdoQueue);
        btngrpDestType.add(rdoTopic);
        btngrpDestType.add(rdoGeneric);
        btngrpDestType.add(rdoXAQueue);
        btngrpDestType.add(rdoXATopic);
        btngrpDestType.add(rdoXAGeneric);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        //
        // Set the Message Body default to Text Message type.
        tabbedPane.setComponentAt(2, pnlJmsTextMsg);

        if (_aryMsgFileURI != null) {
            if (_aryMsgFileURI.length == 1) _strMsgFileURI = _aryMsgFileURI[0];
            if (_aryMsgFileURI.length > 1) showChooseDialog();
        }

        // Test to see if file exists.
        try {
            java.io.RandomAccessFile rafFileTest = new java.io.RandomAccessFile(_strMsgFileURI, "r");
            rafFileTest.close();
        }
        catch (Exception fe) {  // Give user to opportunity to choose another file.
            JOptionPane.showMessageDialog(this, "File not Found.", "Error", JOptionPane.ERROR_MESSAGE);

            JFileChooser dlgFileChooser = new JFileChooser();
            dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.MSG));
            // Use the OPEN version of the dialog, test return for Approve/Cancel
            if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
                _strMsgFileURI = dlgFileChooser.getSelectedFile().getPath();
            }
            else {
                cancelButtonActionPerformed();
                return;
            }
        }

        try {
            _msgFile = new MessageFile(_strMsgFileURI, _strEncoding);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this.getParent(), ex.toString(), "Message Editor Error", JOptionPane.ERROR_MESSAGE);
            cancelButtonActionPerformed();
            return;
        }

        // Get file name
        this.setTitle(this.getTitle() + " - " + _strMsgFileURI.substring(_strMsgFileURI.lastIndexOf(java.io.File.pathSeparatorChar) + 1));

        // This is for a new message file
        if (_msgFile.getMessageCount() == 0) {
            btnNewMsgActionPerformed();
        }
        else
            refreshData();

        // Get the DefaultTableModel from tblMsgProperties
        _tmodMsgProps = (DefaultTableModel)(tblMsgProperties.getModel());

        this.getParent().setCursor(Cursor.getDefaultCursor());
    }

    private String _strMsgFileURI;
    private String _aryMsgFileURI[];
    private String _strEncoding;
    private PanelJmsTextMsg pnlJmsTextMsg;
    private PanelJmsMapMsg pnlJmsMapMsg;
    private PanelJmsByteMsg pnlJmsByteMsg;
    private DefaultTableModel _tmodMsgProps;
    private MessageFile _msgFile;
    private boolean _blnCanceled = false;

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane;
    private JPanel panelJMS;
    private JLabel lblJmsMsgId;
    private JTextField txtJmsMsgId;
    private JLabel lblReceiveTime;
    private JTextField txtReceiveTime;
    private JLabel lblJmsServerTime;
    private JTextField txtJmsServerTime;
    private JLabel lblOriginationTime;
    private JTextField txtOriginationTime;
    private JLabel lblMsgSelector;
    private JTextField txtMsgSelector;
    private JLabel lblMsgType;
    private JComboBox cboMessageType;
    private JCheckBox chkJmsPriority;
    private JSpinner spnJmsPriority;
    private JLabel lblDestType;
    private JRadioButton rdoQueue;
    private JRadioButton rdoTopic;
    private JRadioButton rdoGeneric;
    private JCheckBox chkCommitTrans;
    private JRadioButton rdoXAQueue;
    private JRadioButton rdoXATopic;
    private JRadioButton rdoXAGeneric;
    private JLabel lblJmsDest;
    private JTextField txtJmsDest;
    private JLabel lblJmsReplyTo;
    private JTextField txtJmsReplyTo;
    private JLabel lblJmsCorrId;
    private JTextField txtJmsCorrId;
    private JLabel lblJmsDeliveryMode;
    private JComboBox cboJmsDeliveryMode;
    private JLabel lblJmsType;
    private JTextField txtJmsType;
    private JLabel lblJmsExpiration;
    private JSpinner spnJmsExpiration;
    private JLabel lblmsec1;
    private JLabel lblSleep;
    private JSpinner spnSleepTime;
    private JLabel lblmsec2;
    private JPanel panelProps;
    private JPanel pnlTableButtons;
    private JButton btnAddRow;
    private JButton btnRemoveRow;
    private JButton btnUp;
    private JButton btnDown;
    private JScrollPane scrMsgProperties;
    private JTable tblMsgProperties;
    private JPanel panelBody;
    private JPanel buttonBar;
    private JButton btnNewMsg;
    private JButton btnDeleteMsg;
    private JButton btnPrevious;
    private JButton btnNext;
    private JLabel lblMsgNum;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
