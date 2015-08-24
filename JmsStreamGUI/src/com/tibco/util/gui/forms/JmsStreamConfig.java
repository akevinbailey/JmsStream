/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui.forms;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.util.gui.forms.msgedit.JmsStreamMsgEdit;
import com.tibco.util.gui.helper.FileNameFilter;
import com.tibco.util.gui.helper.MultiLineToolTip;
import com.tibco.util.jmshelper.ConnectionHelper;
import com.tibco.util.jmshelper.FormatHelper;

import javax.naming.Context;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.8
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "FieldCanBeLocal", "UnnecessaryBoxing", "unchecked", "unused", "deprecation", "WeakerAccess", "CanBeFinal"})
public class JmsStreamConfig extends JDialog {
    public boolean isOK() {return _blnOK;}

    public JmsStreamConfig(Frame owner) {
        super(owner);
        initComponents();
    }

    public JmsStreamConfig(Dialog owner) {
        super(owner);
        initComponents();
    }

    /**
     *
     * @return a Hashtable with the JmsStream properties
     */
    public Hashtable getValues()
    {
        return _env;
    }

    /**
     * Set the JmsStreamConfig values for editing an existing configuration.
     *
     * @param env   the JmsStream properties
     */
    public void setValues(Hashtable env)
    {
        /**
         * Read the JmsStream properties in the Hashtable and set the JmsStreamConfig values
         */
        try {
            if (env.containsKey("guiDisplayRefresh")) {
                if ((Integer)env.get("guiDisplayRefresh") >= 50)
                spDisplayRefresh.setValue(env.get("guiDisplayRefresh"));
            }

            if (env.containsKey("guiDisplayBuffer")) {
                if ((Integer)env.get("guiDisplayBuffer") >= 100)
                spDisplayBuffer.setValue(env.get("guiDisplayBuffer"));
            }

            if (env.containsKey("jmsclient")) {
                _conHelper.setJmsClientType(env.get("jmsclient").toString());
                cboJmsClientType.setSelectedItem(env.get("jmsclient").toString());
                cboJmsClientTypeItemStateChanged();
            }

            if (env.containsKey(Context.INITIAL_CONTEXT_FACTORY)) {
                txtJndiContextFactory.setText(env.get(Context.INITIAL_CONTEXT_FACTORY).toString());
            }
            if (env.containsKey("connectionfactory")) {
                if (!_conHelper.isDefaultSetting(env.get("connectionfactory").toString()))
                    txtConFactory.setText(env.get("connectionfactory").toString());

                /*
                if (env.containsKey("jmsclient") && env.get("jmsclient").equals(ConnectionHelper.JMS_SERVER_TIBCO_EMS)) {
                    if (!env.get("connectionfactory").toString().equals("QueueConnectionFactory")
                    && !env.get("connectionfactory").toString().equals("TopicConnectionFactory")
                    && !env.get("connectionfactory").toString().equals("GenericConnectionFactory"))
                        txtConFactory.setText(env.get("connectionfactory").toString());
                }
                else
                    txtConFactory.setText(env.get("connectionfactory").toString());
                */
            }
            if (env.containsKey(Context.PROVIDER_URL)) {
                txtProviderUrl.setText(env.get(Context.PROVIDER_URL).toString());
            }
            if (env.containsKey("isSeparateUsrPwd") && env.get("isSeparateUsrPwd").equals(Boolean.TRUE)) {
                chkJndiUserPwd.setSelected(true);
                chkJndiUserPwdItemStateChanged(new ItemEvent(chkJndiUserPwd, ItemEvent.ITEM_STATE_CHANGED, chkJndiUserPwd, ItemEvent.SELECTED));
            }
            if (env.containsKey("user")) {
                if (chkJndiUserPwd.isSelected())
                    txtJmsUser.setText(env.get("user").toString());
                else
                    txtUser.setText(env.get(Context.SECURITY_PRINCIPAL).toString());
            }
            if (env.containsKey(Context.SECURITY_PRINCIPAL) && chkJndiUserPwd.isSelected()) {
                    txtJndiUser.setText(env.get(Context.SECURITY_PRINCIPAL).toString());
            }
            if (env.containsKey("password")) {
                if (chkJndiUserPwd.isSelected())
                    txtJmsPassword.setText(env.get("password").toString());
                else
                    txtPassword.setText(env.get(Context.SECURITY_CREDENTIALS).toString());
            }
            if (env.containsKey(Context.SECURITY_CREDENTIALS) && chkJndiUserPwd.isSelected()) {
                txtJndiPassword.setText(env.get(Context.SECURITY_CREDENTIALS).toString());
            }
            if (env.containsKey("extractmonmsg") && env.get("extractmonmsg").equals(Boolean.TRUE)) {
                chkExtractMonMsg.setSelected(true);
            }

            // Transaction Properties
            if (env.containsKey("trans")) {
                chkTransactional.setSelected(true);
                chkTransactionalItemStateChanged(new ItemEvent(chkTransactional, 0, chkTransactional, ItemEvent.SELECTED));
                if (env.get("trans").equals("jms")) {
                    rdoTransJms.setSelected(true);
                    rdoTransXa.setSelected(false);
                }
                else if (env.get("trans").equals("xa")) {
                    rdoTransXa.setSelected(true);
                    rdoTransJms.setSelected(false);
                }
                if (env.containsKey("transmsgnum"))
                    spTransMsgNum.setValue(env.get("transmsgnum"));
                if (env.containsKey("commitonexit") && env.get("commitonexit").equals(Boolean.TRUE))
                    chkCommitOnExit.setSelected(true);
                else
                    chkCommitOnExit.setSelected(false);
                if (env.containsKey("transmgrtype")) {
                    if (env.get("transmgrtype").equals("local")) {
                        rdoTransMgrLocal.setSelected(true);
                        rdoTransMgrNoMgr.setSelected(false);
                    }
                    else if (env.get("transmgrtype").equals("nomgr")) {
                        rdoTransMgrNoMgr.setSelected(true);
                        rdoTransMgrLocal.setSelected(false);
                    }
                }
                if (env.containsKey("transjndiname"))
                    txtTransJndi.setText(env.get("transjndiname").toString());
                if (env.containsKey("transtimeout"))
                    spTransTimeout.setValue(env.get("transtimeout"));
            }
            // SSL settings
            if (env.containsKey("ssl") && env.get("ssl").equals(Boolean.TRUE)) {
                chkSsl.setSelected(true);
                chkSslItemStateChanged(new ItemEvent(chkSsl, 0, chkSsl, ItemEvent.SELECTED));
            }
            if (env.containsKey("trustAllCerts") && env.get("trustAllCerts").equals(Boolean.TRUE)) {
                chkTrustAllCerts.setSelected(true);
            }
            else {
                chkTrustAllCerts.setSelected(false);
            }

            if (env.containsKey("useFileJNDI") && env.get("useFileJNDI").equals(Boolean.TRUE)) {
                chkUseFileJNDI.setSelected(true);
            }
            // TODO: Add separate Naming and Connection parameters.
            if (env.containsKey(_conHelper.getSecurityProtocol()) && env.get(_conHelper.getSecurityProtocol()).equals("ssl")) {
                // Specify ssl as the security protocol to use by the Initial Context
                chkSslJndi.setSelected(true);
                chkSslItemStateChanged(new ItemEvent(chkSsl, 0, chkSsl, ItemEvent.SELECTED));
            }
            if (env.containsKey(_conHelper.getSslAuthOnlyNaming()) && env.get(_conHelper.getSslAuthOnlyNaming()).equals(Boolean.TRUE)) {
                chkSslAuthOnly.setSelected(true);
            }
            if (env.containsKey(_conHelper.getSslVendorNaming())) {
                cboSslVendor.setSelectedItem(env.get(_conHelper.getSslVendorNaming()));
            }
            if (env.containsKey(_conHelper.getSslCipherSuitesNaming())) {
                txtSslCiphers.setText(env.get(_conHelper.getSslCipherSuitesNaming()).toString());
            }
            if (env.containsKey(_conHelper.getSslTraceNaming()) && env.get(_conHelper.getSslTraceNaming()).equals(Boolean.TRUE)) {
                chkSslTrace.setSelected(true);
            }
            if (env.containsKey(_conHelper.getSslDebugTraceNaming()) && env.get(_conHelper.getSslDebugTraceNaming()).equals(Boolean.TRUE)) {
                chkSslDebugTrace.setSelected(true);
            }
            if (env.containsKey(_conHelper.getSslTrustedCertificatesNaming())) {
                Vector vec = (Vector)env.get(_conHelper.getSslTrustedCertificatesNaming());
                for (Object aVec : vec) dlmSslTrusted.addElement(aVec);
            }
            if (env.containsKey(_conHelper.getSslExpectedHostNameNaming())) {
                txtSslHostName.setText(env.get(_conHelper.getSslExpectedHostNameNaming()).toString());
            }
            if (env.containsKey(_conHelper.getSslIdentityNaming())) {
                txtSslIdentity.setText(env.get(_conHelper.getSslIdentityNaming()).toString());
            }
            if (env.containsKey(_conHelper.getSslPasswordNaming())) {
                txtSslPassword.setText(env.get(_conHelper.getSslPasswordNaming()).toString());
            }
            if (env.containsKey(_conHelper.getSslPrivateKeyNaming())) {
                txtSslKey.setText(env.get(_conHelper.getSslPrivateKeyNaming()).toString());
            }
            if (env.containsKey(_conHelper.getSslEnableVerifyHostNameNaming()) && env.get(_conHelper.getSslEnableVerifyHostNameNaming()).equals(Boolean.TRUE)) {
                chkVerifyHostName.setSelected(true);
            }
            if (env.containsKey(_conHelper.getSslEnableVerifyHostNaming()) && env.get(_conHelper.getSslEnableVerifyHostNaming()).equals(Boolean.TRUE)) {
                chkVerifyHost.setSelected(true);
            }
            if (env.containsKey(_conHelper.getKeyStoreType())) {
                cboKeyStoreType.setSelectedItem(env.get(_conHelper.getKeyStoreType()));
            }
            if (env.containsKey(_conHelper.getKeyStore())) {
                txtKeyStore.setText(env.get(_conHelper.getKeyStore()).toString());
            }
            if (env.containsKey(_conHelper.getKeyStorePassword())) {
                txtKeyStorePassword.setText(env.get(_conHelper.getKeyStorePassword()).toString());
            }
            if (env.containsKey(_conHelper.getTrustStoreType())) {
                cboTrustStoreType.setSelectedItem(env.get(_conHelper.getTrustStoreType()));
            }
            if (env.containsKey(_conHelper.getTrustStore())) {
                txtTrustStore.setText(env.get(_conHelper.getTrustStore()).toString());
            }
            if (env.containsKey(_conHelper.getTrustStorePassword())) {
                txtTrustStorePassword.setText(env.get(_conHelper.getTrustStorePassword()).toString());
            }
            if (env.containsKey(_conHelper.getNetDebug())) {
                txtSslDebugParams.setText(env.get(_conHelper.getNetDebug()).toString());
            }
            // End of SSL Settings

            if (env.containsKey("isListener") && env.get("isListener").equals(Boolean.TRUE)) {
                rdoSend.setSelected(false);
                rdoReqRep.setSelected(false);
                rdoListen.setSelected(true);
            }
            else if (env.containsKey("isListener") && env.get("isListener").equals(Boolean.FALSE)) {
                if (env.containsKey("requestreply") && env.get("requestreply").equals(Boolean.TRUE)) {
                    rdoListen.setSelected(false);
                    rdoSend.setSelected(false);
                    rdoReqRep.setSelected(true);
                }
                else {
                    rdoListen.setSelected(false);
                    rdoReqRep.setSelected(false);
                    rdoSend.setSelected(true);
                }
            }
            if (env.containsKey("senddest")) {
                txtSendDest.setText(env.get("senddest").toString());
            }
            if (env.containsKey("listendest")) {
                txtListenDest.setText(env.get("listendest").toString());
            }
            if (env.containsKey("replytimeout")) {
                spReplyTimeout.setValue(env.get("replytimeout"));
            }
            if (env.containsKey("asyncreply") && env.get("asyncreply").equals(Boolean.TRUE)) {
                chkAsyncReply.setSelected(true);
            }
            if (env.containsKey("type") && env.get("type").equals("queue")) {
                rdoTopic.setSelected(false);
                rdoGeneric.setSelected(false);
                rdoQueue.setSelected(true);
                rdoQueueItemStateChanged(new ItemEvent(rdoQueue, 0, rdoQueue, ItemEvent.SELECTED));
                if (env.containsKey("browse") && env.get("browse").equals(Boolean.TRUE))
                    chkBrowseQueue.setSelected(true);
                //rdoQueueItemStateChanged(new ItemEvent(rdoQueue, ItemEvent.ITEM_STATE_CHANGED, rdoQueue, ItemEvent.SELECTED));
            }
            else if (env.containsKey("type") && env.get("type").equals("topic")) {
                rdoQueue.setSelected(false);
                rdoGeneric.setSelected(false);
                rdoTopic.setSelected(true);
                rdoTopicItemStateChanged(new ItemEvent(rdoTopic, 0, rdoTopic, ItemEvent.SELECTED));
            }
            else if (env.containsKey("type") && env.get("type").equals("generic")) {
                rdoTopic.setSelected(false);
                rdoQueue.setSelected(false);
                rdoGeneric.setSelected(true);
                rdoGenericItemStateChanged(new ItemEvent(rdoGeneric, 0, rdoGeneric, ItemEvent.SELECTED));
            }
            if (env.containsKey("durablename")) {
                txtDurableName.setText(env.get("durablename").toString());
                chkDurable.setSelected(true);
                if (env.containsKey("unsubscribe") && env.get("unsubscribe").equals(Boolean.TRUE))
                    chkUnsubscribe.setSelected(true);
            }
            if (env.containsKey("file")) {
                txtJmsStreamFile.setText(env.get("file").toString());
            }
            if (env.containsKey("deliverymode")) {
                cboDeliveryMode.setSelectedItem(env.get("deliverymode"));
            }
            if (env.containsKey("ackmode")) {
                cboAckMode.setSelectedItem(env.get("ackmode"));
            }
            if (env.containsKey("noconfirm") && env.get("noconfirm").equals(Boolean.TRUE)) {
                chkNoConfirm.setSelected(true);
            }
            if (env.containsKey("replyfile")) {
                txtReplyFile.setText(env.get("replyfile").toString());
            }
            if (env.containsKey("zip") && env.get("zip").equals(Boolean.TRUE)) {
                chkZip.setSelected(true);
                if (env.containsKey("zipentries")) {
                    Vector vec = (Vector)env.get("zipentries");
                    for (Object aVec : vec) dlmZipEntry.addElement(aVec);
                }
                if (env.containsKey("zipmsgperentry"))
                    spZipMessageNum.setValue(env.get("zipmsgperentry"));
            }
            if (env.containsKey("echoxml") && env.get("echoxml").equals(Boolean.TRUE)) {
                chkEchoXml.setSelected(true);
            }
            if (env.containsKey("timed") && env.get("timed").equals(Boolean.TRUE)) {
                chkTimed.setSelected(true);
                if (env.containsKey("speed"))
                    spSpeed.setValue(env.get("speed"));
            }
            if (env.containsKey("ratestamp")) {
                txtRatestamp.setText(env.get("ratestamp").toString());
            }
            if (env.containsKey("sndtimestamp")) {
                txtSndTimestamp.setText(env.get("sndtimestamp").toString());
            }
            if (env.containsKey("rcvtimestamp")) {
                txtRcvTimestamp.setText(env.get("rcvtimestamp").toString());
            }
            if (env.containsKey("sequence")) {
                txtSequence.setText(env.get("sequence").toString());
            }
            if (env.containsKey("encoding")) {
                cboEncoding.setSelectedItem(env.get("encoding").toString());
            }
            else {
                cboEncoding.setSelectedItem("Default");
            }
            if (env.containsKey("selector")) {
                txtSelector.setText(env.get("selector").toString());
            }
            if (env.containsKey("clientid")) {
                txtClientId.setText(env.get("clientid").toString());
            }
            if (env.containsKey("compress") && env.get("compress").equals(Boolean.TRUE)) {
                chkCompress.setSelected(true);
            }
            if (env.containsKey("stats")) {
                spStats.setValue(env.get("stats"));
            }
            if (env.containsKey("verbose") && env.get("verbose").equals(Boolean.TRUE)) {
                chkVerbose.setSelected(true);
            }
            else if (env.containsKey("verbose") && env.get("verbose").equals(Boolean.FALSE)) {
                chkVerbose.setSelected(false);
            }
            if (env.containsKey("noecho") && env.get("noecho").equals(Boolean.TRUE)) {
                chkNoEcho.setSelected(true);
                chkEchoRaw.setSelected(false);
                chkVerbose.setSelected(false);
                chkEchoXml.setSelected(false);
                chkEchoCsv.setSelected(false);
            }
            if (env.containsKey("raw") && env.get("raw").equals(Boolean.TRUE)) {
                chkEchoRaw.setSelected(true);
                chkNoEcho.setSelected(false);
                chkVerbose.setSelected(false);
                chkEchoXml.setSelected(false);
                chkEchoCsv.setSelected(false);
            }
            if (env.containsKey("echocsv") && env.get("echocsv").equals(Boolean.TRUE)) {
                chkEchoCsv.setSelected(true);
            }
            if (env.containsKey("fileappend") && env.get("fileappend").equals(Boolean.TRUE)) {
                chkFileAppend.setSelected(true);
            }
            if (env.containsKey("fileloop")) {
                if ((Integer)env.get("fileloop") > 1)
                    spFileLoop.setValue(env.get("fileloop"));
            }
            if (env.containsKey("csvfile")) {
                txtCsvSaveFile.setText(env.get("csvfile").toString());
            }
            if (env.containsKey("stopafter") && (Integer)env.get("stopafter") > 0) {
                spStopAfter.setValue(env.get("stopafter"));
            }
            if (env.containsKey("variablerate") && env.get("variablerate").equals(Boolean.TRUE)) {
                chkVariableMsgRate.setEnabled(true);
                chkVariableMsgRate.setSelected(true);
                //chkVariableMsgRateItemStateChanged(new ItemEvent(chkVariableMsgRate,ItemEvent.ITEM_STATE_CHANGED,chkVariableMsgRate,ItemEvent.SELECTED)); // For some reason setting selected to true does not fire this event.
                if (env.containsKey("rate")) {
                    chkStartingRate.setEnabled(true);
                    chkStartingRate.setSelected(true);
                    spStartRate.setEnabled(true);
                    spStartRate.setValue(env.get("rate"));
                }
                if (env.containsKey("maxrate"))
                    spMaxRate.setValue(env.get("maxrate"));
                if (env.containsKey("numberofintervals"))
                    spNumberOfIntervals.setValue(env.get("numberofintervals"));
                if (env.containsKey("intervalsize"))
                    spIntervalSize.setValue(env.get("intervalsize"));
            }
            else if (env.containsKey("rate")) {
                lblRate.setEnabled(true);
                spRate.setEnabled(true);
                lblMsgSec.setEnabled(true);
                spRate.setValue(env.get("rate"));
            }
            /**
            * Read the JmsStream properties in the Hashtable and set the JmsStreamConfig values.  We must
            * iterate over the env values so we can determine if it is a known or custom JNDI property.
            */
            Object objEnvSet[] = env.entrySet().toArray();
            Map.Entry mapRow;
            String strRow[] = new String[2];
            _dtmJndiProps.setRowCount(0);
            for (Object anObjEnvSet : objEnvSet) {
                mapRow = (Map.Entry)anObjEnvSet;
                if (!FormatHelper.isConfigProp(mapRow.getKey().toString(), _conHelper.getJmsClientType())) {
                    strRow[0] = mapRow.getKey().toString();
                    strRow[1] = mapRow.getValue().toString();
                    _dtmJndiProps.addRow(strRow);
                }
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Due to limitations with the the SSL implementation the SSL parameters can only be set once.
     * After the SSL connection is initiated you must re-start JmsStream to change these settings.
     * Disable the Java SSL fields to prevent the user from editing them.
     */
    public void disableJavaSslInput()
    {
        // TIBCO SSL
        lblSslVendor.setEnabled(false);
        lblSslCiphers.setEnabled(false);
        lblSslHostName.setEnabled(false);
        lblSslIdentity.setEnabled(false);
        lblSslPassword.setEnabled(false);
        lblSslKey.setEnabled(false);
        lblSslTrusted.setEnabled(false);

        cboSslVendor.setEnabled(false);
        chkSslJndi.setEnabled(false);
        chkSslAuthOnly.setEnabled(false);
        chkSslTrace.setEnabled(false);
        chkSslDebugTrace.setEnabled(false);
        chkVerifyHost.setEnabled(false);
        chkVerifyHostName.setEnabled(false);
        txtSslCiphers.setEnabled(false);
        txtSslHostName.setEnabled(false);
        txtSslIdentity.setEnabled(false);
        txtSslPassword.setEnabled(false);
        txtSslKey.setEnabled(false);
        lstSslTrusted.setEnabled(false);
        btnSslKeyFile.setEnabled(false);
        btnSslIdentityFile.setEnabled(false);
        btnAddTrustedCert.setEnabled(false);
        btnRemoveTrustedCert.setEnabled(false);

        // Java SSL
        lblSslDebugParam.setEnabled(false);
        lblKeyStoreType.setEnabled(false);
        lblKeyStore.setEnabled(false);
        lblKeyStorePassword.setEnabled(false);
        lblTrustStoreType.setEnabled(false);
        lblTrustStore.setEnabled(false);
        lblTrustStorePassword.setEnabled(false);

        txtSslDebugParams.setEnabled(false);
        chkTrustAllCerts.setEnabled(false);
        cboKeyStoreType.setEnabled(false);
        txtKeyStore.setEnabled(false);
        txtKeyStorePassword.setEnabled(false);
        cboTrustStoreType.setEnabled(false);
        txtTrustStore.setEnabled(false);
        txtTrustStorePassword.setEnabled(false);

        btnKeyStoreFile.setEnabled(false);
        btnTrustStoreFile.setEnabled(false);
    }

    private void rdoListenItemStateChanged(ItemEvent e) {
        adjustMsgButtons();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            chkFileAppend.setEnabled(true);
            tabbedPane.setEnabledAt(3, true); // Listener tab
            tabbedPane.setEnabledAt(4, false); // Sender tab
            lblListenDest.setFont(_fntRequired);
            lblListenDest.setText("* Listen Destination");
            if (chkZip.isSelected()) {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "ZIP Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntNormal));
            }
            else {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "JmsStream Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntNormal));
            }
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            lblListenDest.setFont(_fntNormal);
            lblListenDest.setText("Override Reply Destination");
            if (chkZip.isSelected()) {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "* ZIP Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntRequired));
            }
            else {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "* JmsStream Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntRequired));
            }
        }
    }

    private void rdoSendItemStateChanged(ItemEvent e) {
        adjustMsgButtons();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            // No Listening
            tabbedPane.setEnabledAt(3, false); // Listener tab
            tabbedPane.setEnabledAt(4, true); // Sender tab
            chkFileAppend.setEnabled(false);
            chkFileAppend.setSelected(false);
            chkTimed.setSelected(false);
        }
    }

    private void rdoReqRepItemStateChanged(ItemEvent e) {
        adjustMsgButtons();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            // Is Request/Reply
            lblListenDest.setEnabled(true);
            txtListenDest.setEnabled(true);
            tabbedPane.setEnabledAt(3, true); // Listener tab
            tabbedPane.setEnabledAt(4, true); // Sender tab
            chkFileAppend.setEnabled(false);
            lblStopAfter.setEnabled(false);
            spStopAfter.setEnabled(false);
            chkAsyncReply.setEnabled(true);
            lblReplyTimeout.setEnabled(true);
            spReplyTimeout.setEnabled(true);
            btnReplyFile.setEnabled(true);
            lblReplyFile.setEnabled(true);
            txtReplyFile.setEnabled(true);
            lblMiliSec.setEnabled(true);
            lblStopAfter.setEnabled(false);
            spStopAfter.setEnabled(false);
       }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            lblListenDest.setEnabled(true);
            txtListenDest.setEnabled(true);
            lblStopAfter.setEnabled(true);
            spStopAfter.setEnabled(true);
            chkAsyncReply.setEnabled(false);
            chkAsyncReply.setSelected(false);
            lblMiliSec.setEnabled(false);
            lblReplyTimeout.setEnabled(false);
            spReplyTimeout.setEnabled(false);
            spReplyTimeout.setValue(0);
            btnReplyFile.setEnabled(false);
            lblReplyFile.setEnabled(false);
            txtReplyFile.setEnabled(false);
            txtReplyFile.setText("");
        }
    }

    private void chkJndiUserPwdItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            lblUser.setEnabled(false);
            txtUser.setEnabled(false);
            txtUser.setText("");
            lblPassword.setEnabled(false);
            txtPassword.setEnabled(false);
            txtPassword.setText("");
            lblJndiUser.setEnabled(true);
            txtJndiUser.setEnabled(true);
            lblJndiPassword.setEnabled(true);
            txtJndiPassword.setEnabled(true);
            lblJmsUser.setEnabled(true);
            txtJmsUser.setEnabled(true);
            lblJmsPassword.setEnabled(true);
            txtJmsPassword.setEnabled(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            lblUser.setEnabled(true);
            txtUser.setEnabled(true);
            lblPassword.setEnabled(true);
            txtPassword.setEnabled(true);
            lblJndiUser.setEnabled(false);
            txtJndiUser.setEnabled(false);
            txtJndiUser.setText("");
            lblJndiPassword.setEnabled(false);
            txtJndiPassword.setEnabled(false);
            txtJndiPassword.setText("");
            lblJmsUser.setEnabled(false);
            txtJmsUser.setEnabled(false);
            txtJmsUser.setText("");
            lblJmsPassword.setEnabled(false);
            txtJmsPassword.setEnabled(false);
            txtJmsPassword.setText("");
        }
    }

    private void cancelButtonActionPerformed() {
        _blnOK = false;
        this.dispose();
    }

    private void okButtonActionPerformed() {
        _blnOK = true;

        if (_env == null) _env = new Hashtable();
        else _env.clear();

        /**
         * Put all of for values in a Hashtable to pass to JmsStream
         */
        try {
            if (!createEnv()) {// If the config is invalid then try again.
                return;
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        this.dispose();
    }

    private void chkNoEchoItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            chkVerbose.setEnabled(false);
            chkVerbose.setSelected(false);
            chkEchoCsv.setEnabled(false);
            chkEchoCsv.setSelected(false);
            chkEchoXml.setEnabled(false);
            chkEchoXml.setSelected(false);
            chkEchoRaw.setEnabled(false);
            chkEchoRaw.setSelected(false);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            chkVerbose.setEnabled(true);
            chkEchoCsv.setEnabled(true);
            chkEchoXml.setEnabled(true);
            chkEchoRaw.setEnabled(true);
        }
    }

    private void chkVerboseItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            chkNoEcho.setEnabled(false);
            chkNoEcho.setSelected(false);
            chkEchoCsv.setEnabled(false);
            chkEchoCsv.setSelected(false);
            chkEchoXml.setEnabled(false);
            chkEchoXml.setSelected(false);
            chkEchoRaw.setEnabled(false);
            chkEchoRaw.setSelected(false);
            spStats.setValue(0);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            chkNoEcho.setEnabled(true);
            chkEchoCsv.setEnabled(true);
            chkEchoXml.setEnabled(true);
            chkEchoRaw.setEnabled(true);
        }
    }

    private void chkEchoRawItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            chkVerbose.setEnabled(false);
            chkVerbose.setSelected(false);
            chkEchoCsv.setEnabled(false);
            chkEchoCsv.setSelected(false);
            chkEchoXml.setEnabled(false);
            chkEchoXml.setSelected(false);
            chkNoEcho.setEnabled(false);
            chkNoEcho.setSelected(false);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            chkVerbose.setEnabled(true);
            chkVerbose.setSelected(true);
            chkEchoCsv.setEnabled(true);
            chkEchoXml.setEnabled(true);
            chkNoEcho.setEnabled(true);
        }
    }

    private void rdoQueueItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            chkBrowseQueue.setEnabled(true);
            if (chkDurable.isSelected()) {
                chkDurable.setSelected(false);
            }
            chkDurable.setEnabled(false);
        }
    }

    private void rdoTopicItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            chkBrowseQueue.setSelected(false);
            chkBrowseQueue.setEnabled(false);
            chkDurable.setEnabled(true);
        }
    }

    private void rdoGenericItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            chkBrowseQueue.setEnabled(true);
            chkDurable.setEnabled(true);
        }
    }

    private void chkZipItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (rdoListen.isSelected()) {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "ZIP Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntNormal));
            }
            else {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "* ZIP Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntRequired));
            }
            chkFileAppend.setEnabled(false);
            chkFileAppend.setSelected(false);
            lblZipEntry.setEnabled(true);
            btnAddZipEntry.setEnabled(true);
            btnRemoveZipEntry.setEnabled(true);
            lstZipEntry.setEnabled(true);
            lblZipMessageNum.setEnabled(true);
            spZipMessageNum.setEnabled(true);
            adjustMsgButtons();
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            if (rdoListen.isSelected()) {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "JmsStream Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntNormal));
            }
            else {
                pnlJmsStreamFile.setBorder(new TitledBorder(null, "* JmsStream Save File", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, _fntRequired));
            }
            chkFileAppend.setEnabled(true);
            lblZipEntry.setEnabled(false);
            btnAddZipEntry.setEnabled(false);
            btnRemoveZipEntry.setEnabled(false);
            lstZipEntry.setEnabled(false);
            lstZipEntry.removeAll();
            lblZipMessageNum.setEnabled(false);
            spZipMessageNum.setEnabled(false);
            spZipMessageNum.setValue(new Integer(0));
            adjustMsgButtons();
        }
    }

    private void btnOpenMsgFileActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        if (chkZip.isSelected())
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.ZIP));
        else
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.MSG));

        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            txtJmsStreamFile.setText(dlgFileChooser.getSelectedFile().toString());
        }
        if (!chkZip.isSelected()) adjustMsgButtons();
        this.repaint();
    }

    private void btnReplyFileActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.MSG));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            txtReplyFile.setText(dlgFileChooser.getSelectedFile().toString());
        }
        this.repaint();
    }

    private void btnCsvSaveFileActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CSV));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            txtCsvSaveFile.setText(dlgFileChooser.getSelectedFile().toString());
        }
        this.repaint();
    }

    private void btnSslIdentityFileActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        if (cboSslVendor.getSelectedItem().equals("j2se-default"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_ID_J));
        else if (cboSslVendor.getSelectedItem().equals("j2se"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_ID_J));
        else if (cboSslVendor.getSelectedItem().equals("entrust61"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_ID_E));
        else if (cboSslVendor.getSelectedItem().equals("ibm"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_ID_I));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            txtSslIdentity.setText(dlgFileChooser.getSelectedFile().toString());
        }
        this.repaint();
    }

    private void btnSslKeyFileActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            txtSslKey.setText(dlgFileChooser.getSelectedFile().toString());
        }
        this.repaint();
    }

    private void chkVariableMsgRateItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            lblRate.setEnabled(false);
            spRate.setEnabled(false);
            spRate.setValue(new Float(0));
            lblMsgSec.setEnabled(false);

            lblFileLoop.setEnabled(false);
            spFileLoop.setValue(new Integer(0));
            spFileLoop.setEnabled(false);

            chkStartingRate.setEnabled(true);
            lblMaxRate.setEnabled(true);
            spMaxRate.setEnabled(true);
            lblMsgSec3.setEnabled(true);
            lblMsgSec2.setEnabled(true);
            lblNumberOfIntervals.setEnabled(true);
            spNumberOfIntervals.setEnabled(true);
            lblIntervalSize.setEnabled(true);
            spIntervalSize.setEnabled(true);
            lblRatestamp.setEnabled(true);
            txtRatestamp.setEnabled(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            lblRate.setEnabled(true);
            spRate.setEnabled(true);
            lblMsgSec.setEnabled(true);

            lblFileLoop.setEnabled(true);
            spFileLoop.setEnabled(true);
            
            chkStartingRate.setEnabled(false);
            chkStartingRate.setSelected(false);
            lblMaxRate.setEnabled(false);
            spMaxRate.setEnabled(false);
            spMaxRate.setValue(new Float(0));
            lblMsgSec3.setEnabled(false);
            lblNumberOfIntervals.setEnabled(false);
            spNumberOfIntervals.setEnabled(false);
            spNumberOfIntervals.setValue(new Integer(0));
            lblIntervalSize.setEnabled(false);
            spIntervalSize.setEnabled(false);
            spIntervalSize.setValue(new Integer(0));
            lblRatestamp.setEnabled(false);
            txtRatestamp.setEnabled(false);
            txtRatestamp.setText("");
        }
    }

    private void chkStartingRateItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            lblStartRate.setEnabled(true);
            spStartRate.setEnabled(true);
            lblMsgSec2.setEnabled(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            lblStartRate.setEnabled(false);
            spStartRate.setEnabled(false);
            spStartRate.setValue(new Float(0));
            lblMsgSec2.setEnabled(false);
        }
    }

    private void chkDurableItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            lblDurableName.setEnabled(true);
            txtDurableName.setEnabled(true);
            chkUnsubscribe.setEnabled(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            lblDurableName.setEnabled(false);
            txtDurableName.setEnabled(false);
            txtDurableName.setText("");
            chkUnsubscribe.setEnabled(false);
            chkUnsubscribe.setSelected(false);
        }
    }

    private void btnAddTrustedCertActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            dlmSslTrusted.add(dlmSslTrusted.getSize(), dlgFileChooser.getSelectedFile().toString());
            lstSslTrusted.ensureIndexIsVisible(dlmSslTrusted.getSize()-1);
        }
        this.repaint();

    }

    private void btnRemoveTrustedCertActionPerformed() {
        if (lstSslTrusted.isSelectionEmpty())
            JOptionPane.showMessageDialog(this, "No SSL Trusted file selected.", "JmsStream Configuration",
                                          JOptionPane.ERROR_MESSAGE);
        else
            dlmSslTrusted.removeElementAt(lstSslTrusted.getSelectedIndex());
    }

    private void btnAddZipEntryActionPerformed() {
    	String strEntry = JOptionPane.showInputDialog(this, "Enter ZIP Entry:", "JmsStream Configuration", JOptionPane.QUESTION_MESSAGE);
        if (strEntry != null && !strEntry.equals("")) {
            dlmZipEntry.add(dlmZipEntry.getSize(), strEntry);
            lstZipEntry.ensureIndexIsVisible(dlmZipEntry.getSize()-1);
        }
    }

    private void btnRemoveZipEntryActionPerformed() {
        if (lstZipEntry.isSelectionEmpty())
            JOptionPane.showMessageDialog(this, "No ZIP Entry selected.", "JmsStream Configuration",
                                          JOptionPane.ERROR_MESSAGE);
        else
            dlmZipEntry.removeElementAt(lstZipEntry.getSelectedIndex());
    }

    private void chkSslItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            tabbedPane.setEnabledAt(5, true); // SSL tab
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            tabbedPane.setEnabledAt(5, false); // SSL tab
            chkSslJndi.setSelected(false);
            chkSslAuthOnly.setSelected(false);
            chkSslTrace.setSelected(false);
            chkSslDebugTrace.setSelected(false);
            chkVerifyHostName.setSelected(false);
            chkVerifyHost.setSelected(false);
            cboSslVendor.setSelectedIndex(0);
            txtSslCiphers.setText("");
            txtSslHostName.setText("");
            txtSslIdentity.setText("");
            txtSslPassword.setText("");
            txtSslKey.setText("");
            lstSslTrusted.removeAll();
        }
    }
    // Transactional
    private void chkTransactionalItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            tabbedPane.setEnabledAt(6, true); // Transaction tab
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            tabbedPane.setEnabledAt(6, false); // Transaction tab
            rdoTransJms.setSelected(true);
            rdoTransXa.setSelected(false);
            spTransMsgNum.setValue(new Integer(0));
            chkCommitOnExit.setSelected(false);
            txtTransJndi.setText("");
            rdoTransMgrLocal.setSelected(true);
            rdoTransMgrNoMgr.setSelected(false);
            spTransTimeout.setValue(new Integer(0));

            lblTransMgr.setEnabled(false);
            lblTransJndi.setEnabled(false);
            lblTransTimeout.setEnabled(false);
            lblSec3.setEnabled(false);
            txtTransJndi.setEnabled(false);
            rdoTransMgrLocal.setEnabled(false);
            rdoTransMgrNoMgr.setEnabled(false);
            spTransTimeout.setEnabled(false);
        }
    }

    private void rdoTransXaItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            lblXaGroup.setEnabled(true);
            lblTransMgr.setEnabled(true);
            lblTransJndi.setEnabled(true);
            lblTransTimeout.setEnabled(true);
            lblSec3.setEnabled(true);
            txtTransJndi.setEnabled(true);
            rdoTransMgrLocal.setEnabled(true);
            rdoTransMgrNoMgr.setEnabled(true);
            spTransTimeout.setEnabled(true);
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            lblXaGroup.setEnabled(false);
            lblTransMgr.setEnabled(false);
            lblTransJndi.setEnabled(false);
            lblTransTimeout.setEnabled(false);
            lblSec3.setEnabled(false);
            txtTransJndi.setEnabled(false);
            rdoTransMgrLocal.setEnabled(false);
            rdoTransMgrNoMgr.setEnabled(false);
            spTransTimeout.setEnabled(false);
        }
    }

    private void btnAddRowActionPerformed() {
        if (tblJndiProperties.getSelectedRow() == -1) {
            _dtmJndiProps.addRow(new Vector());
        }
        else {
            _dtmJndiProps.insertRow(tblJndiProperties.getSelectedRow() + 1, new Vector());
        }
    }

    private void btnRemoveRowActionPerformed() {
        if (tblJndiProperties.getSelectedRow() > -1) {
            _dtmJndiProps.removeRow(tblJndiProperties.getSelectedRow());
        }
    }

    private void panelPropsComponentHidden() {
        if (tblJndiProperties.isEditing())
            tblJndiProperties.getCellEditor().stopCellEditing();
        tblJndiProperties.clearSelection();
    }

    private void btnNewMsgFileActionPerformed() {
        String strMsgFileURI;
        JFileChooser dlgFileChooser = new JFileChooser();
        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.MSG));
        // Use the custom version of the dialog, test return for Approve/Cancel
        dlgFileChooser.setDialogTitle("New Message File");
        dlgFileChooser.setApproveButtonText("Create File");
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showDialog(this, null)) {
            strMsgFileURI = dlgFileChooser.getSelectedFile().getPath();
            try {
                java.io.File fileNew = new java.io.File(strMsgFileURI);
                //noinspection StatementWithEmptyBody
                if (fileNew.createNewFile()) {/* Do nothing. */}
                txtJmsStreamFile.setText(strMsgFileURI);
            }
            catch (IOException ioe){
                JOptionPane.showMessageDialog(this,
                        "Error:  " + ioe.getMessage(),
                        "JmsStream Message File Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            //btnEditMsgFileActionPerformed();
            adjustMsgButtons();
        }
        else { // Need to set the default cursor because the Message Edit Dialog set the cursor wait.
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void btnEditMsgFileActionPerformed() {
        JmsStreamMsgEdit dlg = null;
        String strTxtFileURI = txtJmsStreamFile.getText();
        String strEncoding = cboEncoding.getSelectedItem().toString();

        strEncoding = strEncoding.equals("Default") ? "UTF-8" : strEncoding;  // Set the default to UTF-8

        if (chkZip.isSelected()) {
            JOptionPane.showMessageDialog(
                    this,
                    "JmsStream cannot edit messages in a ZIP file.",
                    "JmsStream Message File",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        if (strTxtFileURI == null || strTxtFileURI.equals("")) {
            JFileChooser dlgFileChooser = new JFileChooser();
            dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.MSG));

            // Use the OPEN version of the dialog, test return for Approve/Cancel
            if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
                txtJmsStreamFile.setText(dlgFileChooser.getSelectedFile().toString());
                dlg = new JmsStreamMsgEdit(this, txtJmsStreamFile.getText(), strEncoding);
            }
        }
        else dlg = new JmsStreamMsgEdit(this, strTxtFileURI, strEncoding);

        if (dlg != null) {
            if (dlg.isCanceled()) {
                dlg.dispose();
            }
            else {
                dlg.setModal(true);
                dlg.setVisible(true);
                txtJmsStreamFile.setText(dlg.getFileURI()); // Set the edited file as the JmsStream file in the configuration.
            }
        }
    }

    private void adjustMsgButtons()
    {
        if (chkZip.isSelected()) {
            if (rdoListen.isSelected()) {
                btnNewMsgFile.setEnabled(true);
                btnEditMsgFile.setEnabled(false);
                btnOpenMsgFile.setEnabled(false);
            }
            else {
                btnNewMsgFile.setEnabled(false);
                btnEditMsgFile.setEnabled(false);
                btnOpenMsgFile.setEnabled(true);
            }
        }
        else {
            btnNewMsgFile.setEnabled(true);
            btnEditMsgFile.setEnabled(true);
            btnOpenMsgFile.setEnabled(true);
        }
    }

    /**
     * Description:         This class creates the env values to send to the JmsStream class.<p>
     * @return boolean      Returns true if createEnv was successful and false otherwise.
     */
    private boolean createEnv()
    {
        Vector entries = new Vector();
        Vector ssl_trusted = new Vector();

        _env.put("resetthreads", Boolean.TRUE);

        if ((Integer)spDisplayRefresh.getValue() >= 50) {
            _env.put("guiDisplayRefresh", spDisplayRefresh.getValue());
        }
        if ((Integer)spDisplayBuffer.getValue() >= 100) {
            _env.put("guiDisplayBuffer", spDisplayBuffer.getValue());
        }
        if (chkJndiUserPwd.isSelected())
            _env.put("isSeparateUsrPwd", Boolean.TRUE);
        else
            _env.put("isSeparateUsrPwd", Boolean.FALSE);

        if (cboJmsClientType.getSelectedItem().toString().length() > 0) {
            _env.put("jmsclient", cboJmsClientType.getSelectedItem().toString());
        }
        if (chkUseFileJNDI.isSelected()) {
            _env.put("useFileJNDI", Boolean.TRUE);
        }
        if (txtJndiContextFactory.getText().trim().length() > 0) {
            _env.put(Context.INITIAL_CONTEXT_FACTORY, txtJndiContextFactory.getText());
        }
        if (txtConFactory.getText().trim().length() > 0) {
            _env.put("connectionfactory", txtConFactory.getText());
        }
        if (txtProviderUrl.getText().trim().length() > 0) {
            _env.put(Context.PROVIDER_URL, txtProviderUrl.getText());
        }
        if (txtUser.getText().trim().length() > 0) {
            _env.put("user", txtUser.getText());
            _env.put(Context.SECURITY_PRINCIPAL, txtUser.getText());
        }
        if (txtPassword.getPassword().length > 0) {
            _env.put("password", new String(txtPassword.getPassword()));
            _env.put(Context.SECURITY_CREDENTIALS, new String(txtPassword.getPassword()));
        }
        if (txtJndiUser.getText().trim().length() > 0) {
            _env.put(Context.SECURITY_PRINCIPAL, txtJndiUser.getText());
        }
        if (txtJndiPassword.getPassword().length > 0) {
            _env.put(Context.SECURITY_CREDENTIALS, new String (txtJndiPassword.getPassword()));
        }
        if (txtJmsUser.getText().trim().length() > 0) {
            _env.put("user", txtJmsUser.getText());
        }
        if (txtJmsPassword.getPassword().length > 0) {
            _env.put("password", new String(txtJmsPassword.getPassword()));
        }
        if (cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_TIBCO_EMS) && rdoTopic.isSelected() && chkExtractMonMsg.isSelected()) {
            _env.put("extractmonmsg", Boolean.TRUE);
        }
        // Transaction Properties
        if (chkTransactional.isSelected()) {
            if (rdoTransJms.isSelected())  _env.put("trans", "jms");
            else if (rdoTransXa.isSelected()) _env.put("trans", "xa");

            if (rdoTransMgrLocal.isSelected()) _env.put("transmgrtype", "local");
            else if (rdoTransMgrNoMgr.isSelected()) _env.put("transmgrtype", "nomgr");

            if (chkCommitOnExit.isSelected()) _env.put("commitonexit", Boolean.TRUE);
            else _env.put("commitonexit", Boolean.FALSE);

            if ((Integer)spTransMsgNum.getValue() > 0)
                _env.put("transmsgnum", spTransMsgNum.getValue());
            if ((Integer)spTransTimeout.getValue() > 0)
                _env.put("transtimeout", spTransTimeout.getValue());
            if (txtTransJndi.getText().length() > 0)
                _env.put("transjndiname", txtTransJndi.getText());
        }
        // SSL settings
        if (chkSsl.isSelected()) {
            _env.put("ssl", Boolean.TRUE);

            if (cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_TIBCO_EMS)) {
                if (chkSslJndi.isSelected()) {
                    // Specify ssl as the security protocol to use by the Initial Context
                    _env.put(_conHelper.getSecurityProtocol(), "ssl");
                }
                if (chkSslAuthOnly.isSelected()) {
                    _env.put(_conHelper.getSslAuthOnly(), Boolean.TRUE);
                    _env.put(_conHelper.getSslAuthOnlyNaming(), Boolean.TRUE);
                    //TibjmsSSL.setAuthOnly(true);
                }
                else {
                    _env.put(_conHelper.getSslAuthOnly(), Boolean.FALSE);
                    _env.put(_conHelper.getSslAuthOnlyNaming(), Boolean.FALSE);
                }

                if (cboSslVendor.getSelectedItem().toString().length() > 0) {
                    _env.put(_conHelper.getSslVendor(), cboSslVendor.getSelectedItem().toString());
                    _env.put(_conHelper.getSslVendorNaming(), cboSslVendor.getSelectedItem().toString());
                    //TibjmsSSL.setVendor(cboSslVendor.getSelectedItem().toString());
                }
                if (txtSslCiphers.getText().trim().length() > 0) {
                    _env.put(_conHelper.getSslCipherSuites(), txtSslCiphers.getText());
                    _env.put(_conHelper.getSslCipherSuitesNaming(), txtSslCiphers.getText());
                    //TibjmsSSL.setCipherSuites(txtSslCiphers.getText());
                }
                // Set trace for client-side operations, loading of certificates and other
                if (chkSslTrace.isSelected()) {
                    _env.put(_conHelper.getSslTrace(), Boolean.TRUE);
                    _env.put(_conHelper.getSslTraceNaming(), Boolean.TRUE);
                    //TibjmsSSL.setClientTracer(System.out);
                }
                else {
                    _env.put(_conHelper.getSslTrace(), Boolean.FALSE);
                    _env.put(_conHelper.getSslTraceNaming(), Boolean.FALSE);
                }
                // Set vendor trace. Has no effect for "j2se", "entrust61" uses this to trace SSL handshake
                if (chkSslDebugTrace.isSelected()) {
                    _env.put(_conHelper.getSslDebugTrace(), Boolean.TRUE);
                    _env.put(_conHelper.getSslDebugTraceNaming(), Boolean.TRUE);
                    //TibjmsSSL.setDebugTraceEnabled(true);
                }
                else {
                    _env.put(_conHelper.getSslDebugTrace(), Boolean.FALSE);
                    _env.put(_conHelper.getSslDebugTraceNaming(), Boolean.FALSE);
                }
                // Set trusted certificates if specified
                if (lstSslTrusted.getModel().getSize() > 0) {
                    ssl_trusted.clear();
                    for (int i = 0; i < lstSslTrusted.getModel().getSize(); i++) {
                        ssl_trusted.add(lstSslTrusted.getModel().getElementAt(i));
                        //TibjmsSSL.addTrustedCerts(lstSslTrusted.getModel().getElementAt(i));
                    }
                    _env.put(_conHelper.getSslTrustedCertificates(), ssl_trusted);
                    _env.put(_conHelper.getSslTrustedCertificatesNaming(), ssl_trusted);
                }
                // Set trusted certificates if specified
                if (txtSslHostName.getText().trim().length() > 0) {
                    _env.put(_conHelper.getSslExpectedHostName(), txtSslHostName.getText());
                    _env.put(_conHelper.getSslExpectedHostNameNaming(), txtSslHostName.getText());
                    //TibjmsSSL.setExpectedHostName(txtSslHostName.getText());
                }
                // Set client identity if specified. ssl_key may be null if identity is PKCS12, JKS or EPF.
                // 'j2se' only supports PKCS12 and JKS. 'entrust61' also supports PEM and PKCS8.
                if (txtSslIdentity.getText().trim().length() > 0) {
                    _env.put(_conHelper.getSslIdentity(), txtSslIdentity.getText());
                    _env.put(_conHelper.getSslIdentityNaming(), txtSslIdentity.getText());
                }
                if (txtSslPassword.getPassword().length > 0) {
                    _env.put(_conHelper.getSslPassword(), new String(txtSslPassword.getPassword()));
                    _env.put(_conHelper.getSslPasswordNaming(), new String(txtSslPassword.getPassword()));
                }
                if (txtSslKey.getText().trim().length() > 0) {
                    _env.put(_conHelper.getSslPrivateKey(), txtSslKey.getText());
                    _env.put(_conHelper.getSslPrivateKeyNaming(), txtSslKey.getText());
                }
                if (chkVerifyHostName.isSelected()) {
                    _env.put(_conHelper.getSslEnableVerifyHostName(), Boolean.TRUE);
                    _env.put(_conHelper.getSslEnableVerifyHostNameNaming(), Boolean.TRUE);
                    //TibjmsSSL.setVerifyHostName(true);
                }
                else {
                    _env.put(_conHelper.getSslEnableVerifyHostName(), Boolean.FALSE);
                    _env.put(_conHelper.getSslEnableVerifyHostNameNaming(), Boolean.FALSE);
                }

                if (chkVerifyHost.isSelected()) {
                    _env.put(_conHelper.getSslEnableVerifyHost(), Boolean.TRUE);
                    _env.put(_conHelper.getSslEnableVerifyHostNaming(), Boolean.TRUE);
                    //TibjmsSSL.setVerifyHost(true);
                }
                else {
                    _env.put(_conHelper.getSslEnableVerifyHost(), Boolean.FALSE);
                    _env.put(_conHelper.getSslEnableVerifyHostNaming(), Boolean.FALSE);
                }
            }
            else if (cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_APACHE_AMQ)
                    || cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_HORNETQ)) {
                if (txtSslDebugParams.getText().trim().length() > 0) {
                    _env.put(_conHelper.getNetDebug(), txtSslDebugParams.getText());
                }
                if (chkTrustAllCerts.isSelected()) {
                    _env.put("trustAllCerts", Boolean.TRUE);
                }
                else {
                    _env.put("trustAllCerts", Boolean.FALSE);

                    if (cboKeyStoreType.getSelectedItem().toString().trim().length() > 0) {
                        _env.put(_conHelper.getKeyStoreType(), cboKeyStoreType.getSelectedItem().toString());
                    }
                    if (txtKeyStore.getText().trim().length() > 0) {
                        _env.put(_conHelper.getKeyStore(), txtKeyStore.getText());
                    }
                    if (txtKeyStorePassword.getPassword().length > 0) {
                        _env.put(_conHelper.getKeyStorePassword(), new String(txtKeyStorePassword.getPassword()));
                    }
                    if (cboTrustStoreType.getSelectedItem().toString().trim().length() > 0) {
                        _env.put(_conHelper.getTrustStoreType(), cboTrustStoreType.getSelectedItem().toString());
                    }
                    if (txtTrustStore.getText().trim().length() > 0) {
                        _env.put(_conHelper.getTrustStore(), txtTrustStore.getText());
                    }
                    if (txtTrustStorePassword.getPassword().length > 0) {
                        _env.put(_conHelper.getTrustStorePassword(), new String(txtTrustStorePassword.getPassword()));
                    }
                }
            }
        }

        if (rdoListen.isSelected()) {
            _env.put("isListener", Boolean.TRUE);
            _env.put("listendest", txtListenDest.getText());
        }
        if (rdoSend.isSelected()) {
            _env.put("isListener", Boolean.FALSE);
            if (txtSendDest.getText().trim().length() > 0) _env.put("senddest", txtSendDest.getText());
        }
        if (rdoReqRep.isSelected()) {
            _env.put("isListener", Boolean.FALSE);
            _env.put("requestreply", Boolean.TRUE);
            if (txtSendDest.getText().trim().length() > 0) _env.put("senddest", txtSendDest.getText());
            if (txtListenDest.getText().trim().length() > 0) _env.put("listendest", txtListenDest.getText());
        }
        if ((Integer)spReplyTimeout.getValue() > 0) {
            _env.put("replytimeout", spReplyTimeout.getValue());
        }
        if (chkAsyncReply.isSelected()) {
            _env.put("asyncreply", Boolean.TRUE);
        }
        if (rdoQueue.isSelected()) {
            if (!_env.containsKey("connectionfactory") || _env.get("connectionfactory").toString().equals(""))
                if (chkSsl.isSelected())
                    _env.put("connectionfactory", _conHelper.getDefaultSslQueueFactory());
                else if (chkTransactional.isSelected())
                    _env.put("connectionfactory", _conHelper.getDefaultXAQueueFactory());
                else
                    _env.put("connectionfactory", _conHelper.getDefaultQueueFactory());
            _env.put("type", "queue");
        }
        if (rdoTopic.isSelected()) {
            if (!_env.containsKey("connectionfactory") || _env.get("connectionfactory").toString().equals(""))
                if (chkSsl.isSelected())
                    _env.put("connectionfactory", _conHelper.getDefaultSslTopicFactory());
                else if (chkTransactional.isSelected())
                    _env.put("connectionfactory", _conHelper.getDefaultXATopicFactory());
                else
                    _env.put("connectionfactory", _conHelper.getDefaultTopicFactory());
            _env.put("type", "topic");
        }
        if (rdoGeneric.isSelected()) {
            if (!_env.containsKey("connectionfactory") || _env.get("connectionfactory").toString().equals(""))
                if (chkSsl.isSelected())
                    _env.put("connectionfactory", _conHelper.getDefaultSslGenericFactory());
                else if (chkTransactional.isSelected())
                    _env.put("connectionfactory", _conHelper.getDefaultXAGenericFactory());
                else
                    _env.put("connectionfactory", _conHelper.getDefaultGenericFactory());
            _env.put("type", "generic");
        }
        if (txtDurableName.getText().trim().length() > 0) {
            _env.put("durablename", txtDurableName.getText());
        }
        if (chkUnsubscribe.isSelected()) {
            if (_env.get("durablename") == null) {
                JOptionPane.showMessageDialog(this, "The Durable Name must no be blank when using Unsubscribe.", "JmsStream Configuration",
                                              JOptionPane.ERROR_MESSAGE);
                return false;
            }
            _env.put("unsubscribe", Boolean.TRUE);
        }
        if (txtJmsStreamFile.getText().trim().length() > 0) {
            _env.put("file", txtJmsStreamFile.getText());
        }
        if (cboDeliveryMode.getSelectedIndex() > 0) {
            _env.put("deliverymode", cboDeliveryMode.getSelectedItem().toString());
        }
        if (cboAckMode.getSelectedItem().toString().length() > 0) {
            _env.put("ackmode", cboAckMode.getSelectedItem().toString());
        }
        if (chkNoConfirm.isSelected()) {
            _env.put("noconfirm", Boolean.TRUE);
        }
        if (txtReplyFile.getText().trim().length() > 0) {
            _env.put("replyfile", txtReplyFile.getText());
        }
        if (chkZip.isSelected()) {
            entries.clear();
            if (lstZipEntry.getModel().getSize() > 0) {
                for (int i = 0; i < lstZipEntry.getModel().getSize(); i++) {
                    entries.add(lstZipEntry.getModel().getElementAt(i));
                }
                _env.put("zipentries", entries);
            }
            _env.put("zip", Boolean.TRUE);
        }
        if ((Integer)spZipMessageNum.getValue() > 0) {
             if (entries.size() > 0) {
                 JOptionPane.showMessageDialog(this, "Cannot use ZIP entries with the Messages Per ZIP Entry.", "JmsStream Configuration",
                                               JOptionPane.ERROR_MESSAGE);
                 return false;
             }
            _env.put("zip", Boolean.TRUE);
            _env.put("zipmsgperentry", spZipMessageNum.getValue());
        }
        if (chkEchoXml.isSelected()) {
            _env.put("echoxml", Boolean.TRUE);
        }
        if (chkTimed.isSelected()) {
            _env.put("timed", Boolean.TRUE);
        }
        if ((Float)spSpeed.getValue() > 0) {
            _env.put("timed", Boolean.TRUE);
            _env.put("speed", spSpeed.getValue());
        }
        if ((Float)spRate.getValue() > 0) {
            _env.put("rate", spRate.getValue());
        }
        if (chkVariableMsgRate.isSelected()) {
            _env.put("variablerate", Boolean.TRUE);
        }
        if ((Float)spStartRate.getValue() > 0) {
            _env.put("rate", spStartRate.getValue());
        }
        if ((Float)spMaxRate.getValue() > 0) {
            _env.put("maxrate", spMaxRate.getValue());
        }
        if ((Integer)spNumberOfIntervals.getValue() > 0) {
            _env.put("numberofintervals", spNumberOfIntervals.getValue());
        }
        if ((Integer)spIntervalSize.getValue() > 0) {
            _env.put("intervalsize", spIntervalSize.getValue());
        }
        if (txtRatestamp.getText().trim().length() > 0) {
            _env.put("ratestamp", txtRatestamp.getText());
        }
        if (txtSndTimestamp.getText().trim().length() > 0) {
            _env.put("sndtimestamp", txtSndTimestamp.getText());
        }
        if (txtRcvTimestamp.getText().trim().length() >0) {
            _env.put("rcvtimestamp", txtRcvTimestamp.getText());
        }
        if (txtSequence.getText().trim().length() > 0) {
            _env.put("sequence", txtSequence.getText());
        }
        if (chkBrowseQueue.isSelected()) {
            _env.put("browse", Boolean.TRUE);
        }
        if (!cboEncoding.getSelectedItem().toString().equals("Default")) {
            _env.put("encoding", cboEncoding.getSelectedItem().toString());
        }
        if (txtSelector.getText().trim().length() > 0) {
            _env.put("selector", txtSelector.getText());
        }
        if (txtClientId.getText().trim().length() > 0) {
            _env.put("clientid", txtClientId.getText());
        }
        if (chkCompress.isSelected()) {
            _env.put("compress", Boolean.TRUE);
        }
        if ((Integer)spStats.getValue() > 0) {
            _env.put("stats", spStats.getValue());
        }

        if (chkVerbose.isSelected()) {
            _env.put("verbose", Boolean.TRUE);
            _env.put("noecho", Boolean.FALSE);
            _env.put("echoxml", Boolean.FALSE);
            _env.put("echocsv", Boolean.FALSE);
        }
        else if (chkNoEcho.isSelected()) {
            _env.put("noecho", Boolean.TRUE);
            _env.put("verbose", Boolean.FALSE);
            _env.put("echoxml", Boolean.FALSE);
            _env.put("echocsv", Boolean.FALSE);
        }
        else if (chkEchoRaw.isSelected()) {
            _env.put("raw", Boolean.TRUE);
            _env.put("verbose", Boolean.FALSE);
            _env.put("echoxml", Boolean.FALSE);
            _env.put("echocsv", Boolean.FALSE);
        }
        else if (chkEchoCsv.isSelected()) {
            _env.put("echocsv", Boolean.TRUE);
        }
        else {
            _env.put("verbose", Boolean.FALSE);
        }

        if (chkFileAppend.isSelected()) {
            _env.put("fileappend", Boolean.TRUE);
        }
        if ((Integer)spFileLoop.getValue() > 0) {
            _env.put("fileloop", spFileLoop.getValue());
        }
        if (txtCsvSaveFile.getText().trim().length() > 0) {
            _env.put("csvfile", txtCsvSaveFile.getText());
        }
        if ((Integer)spStopAfter.getValue() > 0) {
            _env.put("stopafter", spStopAfter.getValue());
        }

        /**
        * Set custom JNDI properties. Row 0 is the key and row 1 is the value.
        */
        for (int i = 0; i < _dtmJndiProps.getRowCount(); i++) {
            _env.put(_dtmJndiProps.getValueAt(i,0), _dtmJndiProps.getValueAt(i,1));
        }

        return true;
    }
            
    private void chkTrustAllCertsItemStateChanged() {
        if (!ConnectionHelper.wasSslConnected()) {
            if (chkTrustAllCerts.isSelected()) {
                lblKeyStoreType.setEnabled(false);
                lblKeyStore.setEnabled(false);
                lblKeyStorePassword.setEnabled(false);
                lblTrustStoreType.setEnabled(false);
                lblTrustStore.setEnabled(false);
                lblTrustStorePassword.setEnabled(false);
                cboKeyStoreType.setEnabled(false);
                txtKeyStore.setEnabled(false);
                txtKeyStorePassword.setEnabled(false);
                cboTrustStoreType.setEnabled(false);
                txtTrustStore.setEnabled(false);
                txtTrustStorePassword.setEnabled(false);
                btnKeyStoreFile.setEnabled(false);
                btnTrustStoreFile.setEnabled(false);
                cboKeyStoreType.setSelectedIndex(0);
                txtKeyStore.setText("");
                txtKeyStorePassword.setText("");
                cboTrustStoreType.setSelectedIndex(0);
                txtTrustStore.setText("");
                txtTrustStorePassword.setText("");
            }
            else {
                lblKeyStoreType.setEnabled(true);
                lblKeyStore.setEnabled(true);
                lblKeyStorePassword.setEnabled(true);
                lblTrustStoreType.setEnabled(true);
                lblTrustStore.setEnabled(true);
                lblTrustStorePassword.setEnabled(true);
                cboKeyStoreType.setEnabled(true);
                txtKeyStore.setEnabled(true);
                txtKeyStorePassword.setEnabled(true);
                cboTrustStoreType.setEnabled(true);
                txtTrustStore.setEnabled(true);
                txtTrustStorePassword.setEnabled(true);
                btnKeyStoreFile.setEnabled(true);
                btnTrustStoreFile.setEnabled(true);
            }
        }
    }

    private void cboSslVendorActionPerformed() {
        if (cboSslVendor.getSelectedItem().equals("j2se-default")) {
            lblSslKey.setEnabled(false);
            txtSslKey.setEnabled(false);
            txtSslKey.setText("");
            btnSslKeyFile.setEnabled(false);
        }
        else if (cboSslVendor.getSelectedItem().equals("j2se")) {
            lblSslKey.setEnabled(false);
            txtSslKey.setEnabled(false);
            txtSslKey.setText("");
            btnSslKeyFile.setEnabled(false);
        }
        else if (cboSslVendor.getSelectedItem().equals("entrust61")) {
            lblSslKey.setEnabled(true);
            txtSslKey.setEnabled(true);
            btnSslKeyFile.setEnabled(true);
        }
        else if (cboSslVendor.getSelectedItem().equals("ibm")) {
            lblSslKey.setEnabled(true);
            txtSslKey.setEnabled(true);
            btnSslKeyFile.setEnabled(true);
        }
    }

    private void tblJndiPropertiesFocusLost(FocusEvent e) {
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
                                TableCellEditor ed = tblJndiProperties.getCellEditor();
                                if (ed != null) {
                                    ed.stopCellEditing();
                                }
                            }
                        }
                );
            }
        }
    }

    private void cboJmsClientTypeItemStateChanged() {
        if (cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_TIBCO_EMS)) {
            chkCompress.setEnabled(true);
            cboAckMode.setModel(new DefaultComboBoxModel(new String[] {
                            "AUTO_ACKNOWLEDGE",
                            "CLIENT_ACKNOWLEDGE",
                            "DUPS_OK_ACKNOWLEDGE",
                            "EXPLICIT_CLIENT_ACKNOWLEDGE",
                            "NO_ACKNOWLEDGE"
                        }));
            tabbedPaneSsl.setEnabledAt(0, true);  // TIBCO SSL
            tabbedPaneSsl.setEnabledAt(1, false); // Apache SSL
            tabbedPaneSsl.setSelectedIndex(0);
            try {_conHelper.setJmsClientType(ConnectionHelper.JMS_SERVER_TIBCO_EMS);} catch (Exception exc) {exc.printStackTrace();}
        }
        else if (cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_APACHE_AMQ)) {
            chkCompress.setEnabled(true);
            cboAckMode.setModel(new DefaultComboBoxModel(new String[] {
                            "AUTO_ACKNOWLEDGE",
                            "CLIENT_ACKNOWLEDGE",
                            "DUPS_OK_ACKNOWLEDGE",
                            "INDIVIDUAL_ACKNOWLEDGE"
                        }));
            tabbedPaneSsl.setEnabledAt(0, false);  // TIBCO SSL
            tabbedPaneSsl.setEnabledAt(1, true); // Java SSL
            tabbedPaneSsl.setSelectedIndex(1);
            try {_conHelper.setJmsClientType(ConnectionHelper.JMS_SERVER_APACHE_AMQ);} catch (Exception exc) {exc.printStackTrace();}
        }
        else if (cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_HORNETQ)) {
            chkCompress.setSelected(false);
            chkCompress.setEnabled(false);
            cboAckMode.setModel(new DefaultComboBoxModel(new String[] {
                            "AUTO_ACKNOWLEDGE",
                            "CLIENT_ACKNOWLEDGE",
                            "DUPS_OK_ACKNOWLEDGE"
                        }));
            tabbedPaneSsl.setEnabledAt(0, false);  // TIBCO SSL
            tabbedPaneSsl.setEnabledAt(1, true); // Java SSL
            tabbedPaneSsl.setSelectedIndex(1);
            try {_conHelper.setJmsClientType(ConnectionHelper.JMS_SERVER_HORNETQ);} catch (Exception exc) {exc.printStackTrace();}
        }

        if (!chkUseFileJNDI.isSelected()) {
            if (txtJndiContextFactory.getText().trim().equals("") || _conHelper.isDefaultSetting(txtJndiContextFactory.getText()))
                txtJndiContextFactory.setText(_conHelper.getInitialContextFactory());
            if (txtProviderUrl.getText().trim().equals("") || _conHelper.isDefaultSetting(txtProviderUrl.getText()))
                txtProviderUrl.setText(_conHelper.getProviderUrl());
        }
    }

    private void btnTrustStoreFileActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        if (cboTrustStoreType.getSelectedItem().equals("JKS"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_JKS));
        else if (cboSslVendor.getSelectedItem().equals("PKCS12"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_PKCS12));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            txtTrustStore.setText(dlgFileChooser.getSelectedFile().toString());
        }
        this.repaint();
    }

    private void btnKeyStoreFileActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        if (cboTrustStoreType.getSelectedItem().equals("JKS"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_JKS));
        else if (cboSslVendor.getSelectedItem().equals("PKCS12"))
            dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CRYPT_PKCS12));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            txtKeyStore.setText(dlgFileChooser.getSelectedFile().toString());
        }
        this.repaint();
    }

    private void chkUseFileJNDIItemStateChanged() {
        if (chkUseFileJNDI.isSelected()) {
            txtJndiContextFactory.setEnabled(false);
            txtProviderUrl.setEnabled(false);
            txtJndiContextFactory.setText("com.sun.jndi.fscontext.RefFSContextFactory");
            txtProviderUrl.setText("file:./lib/jndi");
            lblConFactory.setFont(_fntRequired);
            lblConFactory.setText("* Connection Factory");
        }
        else {
            txtJndiContextFactory.setEnabled(true);
            txtProviderUrl.setEnabled(true);
            txtJndiContextFactory.setText("");
            txtProviderUrl.setText("");
            lblConFactory.setFont(_fntNormal);
            lblConFactory.setText("Connection Factory");
            cboJmsClientTypeItemStateChanged();
        }
    }

    private void chkVerifyHostNameItemStateChanged() {
        if(chkVerifyHostName.isSelected()) {
            lblSslHostName.setFont(_fntRequired);
            lblSslHostName.setText("* SSL Host Name");
        }
        else {
            lblSslHostName.setFont(_fntNormal);
            lblSslHostName.setText("SSL Host Name");
        }
    }

    private void btnTestConnectActionPerformed() {
        javax.jms.ConnectionFactory conFactory;
        javax.jms.Connection conConnection;

        if (_env == null) _env = new Hashtable();
        else _env.clear();

        if (createEnv()) {
            try {
                ConnectionHelper conHelper = new ConnectionHelper(_env);
                conFactory = conHelper.createConnectionFactory();
                if (_env.containsKey("user") && _env.containsKey("password"))
                    conConnection = conFactory.createConnection(_env.get("user").toString(), _env.get("password").toString());
                else if (_env.containsKey("user") && !_env.containsKey("password"))
                    conConnection = conFactory.createConnection(_env.get("user").toString(), "");
                else
                    conConnection = conFactory.createConnection();
                JOptionPane.showMessageDialog(this, "Connection Success.", "JmsStream Configuration",
                                              JOptionPane.INFORMATION_MESSAGE);
                conConnection.close();
            }
            catch (Exception exc) {
                JOptionPane.showMessageDialog(this, exc.getMessage(), "JmsStream Configuration",
                                              JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void tabbedPaneStateChanged() {
        if (tabbedPane.getSelectedIndex() == 3) { // Listen tab
            if (cboJmsClientType.getSelectedItem().toString().equals(ConnectionHelper.JMS_SERVER_TIBCO_EMS) && rdoTopic.isSelected()) {
                chkExtractMonMsg.setVisible(true);
            }
            else {
                chkExtractMonMsg.setVisible(false);
                chkExtractMonMsg.setSelected(false);
            }
        }
    }

    private void chkExtractMonMsgStateChanged() {
        if (chkExtractMonMsg.isSelected()) {
            if (!txtListenDest.getText().startsWith("$sys.monitor.")) {
                txtListenDest.setText("$sys.monitor.");
            }
        }
    }

    private void initComponents() {
        this.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        tabbedPane = new JTabbedPane();
        panelConnection = new JPanel();
        panelCon1 = new JPanel();
        lblJmsServerType = new JLabel();
        cboJmsClientType = new JComboBox() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        btnTestConnect = new JButton();
        lblConFactory = new JLabel();
        txtConFactory = new JTextField() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        lblDestinationType = new JLabel();
        rdoQueue = new JRadioButton();
        rdoTopic = new JRadioButton();
        rdoGeneric = new JRadioButton();
        chkSsl = new JCheckBox();
        chkTransactional = new JCheckBox();
        lblOperation = new JLabel();
        rdoListen = new JRadioButton();
        rdoSend = new JRadioButton() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        rdoReqRep = new JRadioButton() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        chkAsyncReply = new JCheckBox() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        lblReplyTimeout = new JLabel();
        spReplyTimeout = new JSpinner();
        lblMiliSec = new JLabel();
        lblJndiConFactory = new JLabel();
        txtJndiContextFactory = new JTextField() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        chkUseFileJNDI = new JCheckBox();
        lblProviderUrl = new JLabel();
        txtProviderUrl = new JTextField();
        paneCon2 = new JPanel();
        chkJndiUserPwd = new JCheckBox();
        lblClientId = new JLabel();
        txtClientId = new JTextField();
        lblUser = new JLabel();
        txtUser = new JTextField();
        lblPassword = new JLabel();
        txtPassword = new JPasswordField();
        lblJndiUser = new JLabel();
        txtJndiUser = new JTextField();
        lblJndiPassword = new JLabel();
        txtJndiPassword = new JPasswordField();
        lblJmsUser = new JLabel();
        txtJmsUser = new JTextField();
        lblJmsPassword = new JLabel();
        txtJmsPassword = new JPasswordField();
        panelJNDI = new JPanel();
        panelProps = new JPanel();
        pnlTableButtons = new JPanel();
        btnAddRow = new JButton();
        btnRemoveRow = new JButton();
        scrJndiProperties = new JScrollPane();
        tblJndiProperties = new JTable();
        panelInputOutput = new JPanel();
        panelIO1 = new JPanel();
        pnlJmsStreamFile = new JPanel();
        btnNewMsgFile = new JButton();
        btnEditMsgFile = new JButton();
        btnOpenMsgFile = new JButton();
        txtJmsStreamFile = new JTextField();
        chkFileAppend = new JCheckBox();
        lblReplyFile = new JLabel();
        txtReplyFile = new JTextField();
        btnReplyFile = new JButton();
        chkVerbose = new JCheckBox() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        chkZip = new JCheckBox();
        chkNoEcho = new JCheckBox();
        lblZipEntry = new JLabel();
        scrZipEntry = new JScrollPane();
        dlmZipEntry = new DefaultListModel();
        lstZipEntry = new JList(dlmZipEntry);
        btnAddZipEntry = new JButton();
        panelStats = new JPanel();
        lblStats = new JLabel();
        spStats = new JSpinner();
        lblSec2 = new JLabel();
        btnRemoveZipEntry = new JButton();
        panelListener = new JPanel();
        panelListener1 = new JPanel();
        lblListenDest = new JLabel();
        txtListenDest = new JTextField();
        lblSelector = new JLabel();
        txtSelector = new JTextField();
        lblStopAfter = new JLabel();
        spStopAfter = new JSpinner();
        lblAckMode = new JLabel();
        cboAckMode = new JComboBox();
        chkBrowseQueue = new JCheckBox();
        lblRcvTimestamp = new JLabel();
        txtRcvTimestamp = new JTextField();
        chkNoConfirm = new JCheckBox();
        lblCsvSaveFile = new JLabel();
        txtCsvSaveFile = new JTextField();
        btnCsvSaveFile = new JButton();
        chkTimed = new JCheckBox();
        lblZipMessageNum = new JLabel();
        spZipMessageNum = new JSpinner();
        chkEchoCsv = new JCheckBox();
        chkDurable = new JCheckBox();
        chkEchoXml = new JCheckBox();
        lblDurableName = new JLabel();
        txtDurableName = new JTextField();
        chkEchoRaw = new JCheckBox();
        chkUnsubscribe = new JCheckBox();
        chkExtractMonMsg = new JCheckBox();
        panelSend = new JPanel();
        panelSend1 = new JPanel();
        lblSendDest = new JLabel();
        txtSendDest = new JTextField();
        chkCompress = new JCheckBox();
        lblDeliveryMode = new JLabel();
        cboDeliveryMode = new JComboBox();
        lblSpeed = new JLabel();
        spSpeed = new JSpinner() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        lblFileLoop = new JLabel();
        spFileLoop = new JSpinner();
        lblSndTimestamp = new JLabel();
        txtSndTimestamp = new JTextField();
        lblSequence = new JLabel();
        txtSequence = new JTextField();
        lblRate = new JLabel();
        spRate = new JSpinner();
        lblMsgSec = new JLabel();
        chkVariableMsgRate = new JCheckBox();
        chkStartingRate = new JCheckBox();
        lblMaxRate = new JLabel();
        spMaxRate = new JSpinner();
        lblMsgSec3 = new JLabel();
        lblStartRate = new JLabel();
        spStartRate = new JSpinner();
        lblMsgSec2 = new JLabel();
        lblNumberOfIntervals = new JLabel();
        spNumberOfIntervals = new JSpinner() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        lblIntervalSize = new JLabel();
        spIntervalSize = new JSpinner();
        lblRatestamp = new JLabel();
        txtRatestamp = new JTextField();
        panelSsl = new JPanel();
        tabbedPaneSsl = new JTabbedPane();
        panelTibcoSsl = new JPanel();
        panel1 = new JPanel();
        lblSslVendor = new JLabel();
        cboSslVendor = new JComboBox();
        txtSslLimitaionWarning2 = new JTextPane();
        lblSslCiphers = new JLabel();
        txtSslCiphers = new JTextField();
        chkSslJndi = new JCheckBox();
        lblSslHostName = new JLabel();
        txtSslHostName = new JTextField();
        chkSslAuthOnly = new JCheckBox();
        lblSslIdentity = new JLabel();
        txtSslIdentity = new JTextField();
        btnSslIdentityFile = new JButton();
        chkVerifyHostName = new JCheckBox();
        lblSslPassword = new JLabel();
        txtSslPassword = new JPasswordField();
        chkVerifyHost = new JCheckBox();
        lblSslKey = new JLabel();
        txtSslKey = new JTextField();
        btnSslKeyFile = new JButton();
        lblSslTrusted = new JLabel();
        scrSslTrusted = new JScrollPane();
        dlmSslTrusted = new DefaultListModel();
        lstSslTrusted = new JList(dlmSslTrusted);
        chkSslTrace = new JCheckBox();
        btnAddTrustedCert = new JButton();
        chkSslDebugTrace = new JCheckBox();
        btnRemoveTrustedCert = new JButton();
        panelJavaSsl = new JPanel();
        chkTrustAllCerts = new JCheckBox();
        txtSslLimitaionWarning = new JTextPane();
        lblKeyStoreType = new JLabel();
        cboKeyStoreType = new JComboBox();
        lblKeyStore = new JLabel();
        txtKeyStore = new JTextField();
        btnKeyStoreFile = new JButton();
        lblKeyStorePassword = new JLabel();
        txtKeyStorePassword = new JPasswordField();
        lblTrustStoreType = new JLabel();
        cboTrustStoreType = new JComboBox();
        lblTrustStore = new JLabel();
        txtTrustStore = new JTextField();
        btnTrustStoreFile = new JButton();
        lblTrustStorePassword = new JLabel();
        txtTrustStorePassword = new JPasswordField();
        lblSslDebugParam = new JLabel();
        txtSslDebugParams = new JTextField() {
            public JToolTip createToolTip() {
                MultiLineToolTip mltTip = new MultiLineToolTip();
                mltTip.setComponent(this);
                return mltTip;
            }
        };
        panelTrans = new JPanel();
        panelTrans1 = new JPanel();
        lblTransType = new JLabel();
        rdoTransJms = new JRadioButton();
        lblTransMsgNum = new JLabel();
        spTransMsgNum = new JSpinner();
        rdoTransXa = new JRadioButton();
        chkCommitOnExit = new JCheckBox();
        lblXaGroup = new JLabel();
        lblTransMgr = new JLabel();
        lblTransJndi = new JLabel();
        txtTransJndi = new JTextField();
        rdoTransMgrLocal = new JRadioButton();
        lblTransTimeout = new JLabel();
        spTransTimeout = new JSpinner();
        lblSec3 = new JLabel();
        rdoTransMgrNoMgr = new JRadioButton();
        panelOther = new JPanel();
        panelOther1 = new JPanel();
        lblEncoding = new JLabel();
        cboEncoding = new JComboBox();
        lblDisplayBuffer = new JLabel();
        spDisplayBuffer = new JSpinner();
        lblKb = new JLabel();
        lblDisplayRefresh = new JLabel();
        spDisplayRefresh = new JSpinner();
        lblMilli = new JLabel();
        buttonBar = new JPanel();
        lblInfo = new JLabel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle("JmsStream Configuration");
        setResizable(false);
        setModal(true);
        setForeground(Color.white);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG);
            dialogPane.setMinimumSize(new Dimension(900, 400));
            dialogPane.setPreferredSize(new Dimension(900, 400));
            dialogPane.setLayout(new BoxLayout(dialogPane, BoxLayout.Y_AXIS));

            //======== tabbedPane ========
            {
                tabbedPane.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        tabbedPaneStateChanged();
                    }
                });

                //======== panelConnection ========
                {
                    panelConnection.setLayout(new FormLayout(
                        "center:default:grow",
                        "1dlu, $ugap, top:default, $pgap, default"));

                    //======== panelCon1 ========
                    {
                        panelCon1.setLayout(new FormLayout(
                            "right:default, 3*($lcgap, default), $ugap, default, $rgap, default, $lcgap, $button, $lcgap, default",
                            "5*(default, $lgap), default"));

                        //---- lblJmsServerType ----
                        lblJmsServerType.setText("JMS Server Type");
                        lblJmsServerType.setLabelFor(cboJmsClientType);
                        panelCon1.add(lblJmsServerType, CC.xy(1, 1));

                        //---- cboJmsClientType ----
                        cboJmsClientType.setModel(new DefaultComboBoxModel(new String[] {
                            "TIBCO_EMS",
                            "APACHE_AMQ",
                            "HORNETQ"
                        }));
                        cboJmsClientType.setToolTipText("(optional) Sets the JMS client libraries to a specific vendor's product.\n(when the arg is absent the default is TIBCO_EMS)");
                        cboJmsClientType.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                cboJmsClientTypeItemStateChanged();
                            }
                        });
                        panelCon1.add(cboJmsClientType, CC.xywh(3, 1, 4, 1));

                        //---- btnTestConnect ----
                        btnTestConnect.setText("Test Connection");
                        btnTestConnect.setSelectedIcon(null);
                        btnTestConnect.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                btnTestConnectActionPerformed();
                            }
                        });
                        panelCon1.add(btnTestConnect, CC.xywh(12, 1, 4, 1, CC.DEFAULT, CC.CENTER));

                        //---- lblConFactory ----
                        lblConFactory.setText("Connection Factory");
                        lblConFactory.setLabelFor(txtConFactory);
                        panelCon1.add(lblConFactory, CC.xy(1, 3));

                        //---- txtConFactory ----
                        txtConFactory.setToolTipText("Use a connection factory (default: QueueConnectionFactory for queues,\nTopicConnectionFactory for topic, and GenericConnectionFactory for generic destinations)");
                        panelCon1.add(txtConFactory, CC.xywh(3, 3, 13, 1));

                        //---- lblDestinationType ----
                        lblDestinationType.setText("* Destination Type");
                        lblDestinationType.setFont(new Font("Tahoma", Font.BOLD, 11));
                        panelCon1.add(lblDestinationType, CC.xy(1, 5));

                        //---- rdoQueue ----
                        rdoQueue.setText("Queue");
                        rdoQueue.setSelected(true);
                        rdoQueue.setToolTipText("Force the destination to be a queue regardless of connection factory.");
                        rdoQueue.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                rdoQueueItemStateChanged(e);
                            }
                        });
                        panelCon1.add(rdoQueue, CC.xy(3, 5));

                        //---- rdoTopic ----
                        rdoTopic.setText("Topic");
                        rdoTopic.setToolTipText("Force the destination to be a topic regardless of connection factory.");
                        rdoTopic.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                rdoTopicItemStateChanged(e);
                            }
                        });
                        panelCon1.add(rdoTopic, CC.xy(5, 5));

                        //---- rdoGeneric ----
                        rdoGeneric.setText("Generic");
                        rdoGeneric.setToolTipText("Force the destination to be generic regardless of connection factory.");
                        rdoGeneric.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                rdoGenericItemStateChanged(e);
                            }
                        });
                        panelCon1.add(rdoGeneric, CC.xy(7, 5));

                        //---- chkSsl ----
                        chkSsl.setText("SSL Transport");
                        chkSsl.setToolTipText("Enables SSL transport.");
                        chkSsl.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkSslItemStateChanged(e);
                            }
                        });
                        panelCon1.add(chkSsl, CC.xy(9, 5));

                        //---- chkTransactional ----
                        chkTransactional.setText("Transactional");
                        chkTransactional.setToolTipText("Enables Transactions.");
                        chkTransactional.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkTransactionalItemStateChanged(e);
                            }
                        });
                        panelCon1.add(chkTransactional, CC.xywh(11, 5, 3, 1));

                        //---- lblOperation ----
                        lblOperation.setText("* Operation");
                        lblOperation.setFont(new Font("Tahoma", Font.BOLD, 11));
                        panelCon1.add(lblOperation, CC.xy(1, 7));

                        //---- rdoListen ----
                        rdoListen.setText("Listen");
                        rdoListen.setSelected(true);
                        rdoListen.setToolTipText("Listens for messages from a queue or topic destination");
                        rdoListen.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                rdoListenItemStateChanged(e);
                            }
                        });
                        panelCon1.add(rdoListen, CC.xy(3, 7));

                        //---- rdoSend ----
                        rdoSend.setText("Send");
                        rdoSend.setToolTipText("Send messages to queue or topic. The destination is optional. If the destination\nis not given the send destination is read from the messages in the file.\n(must supply the JmsStream save file)");
                        rdoSend.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                rdoSendItemStateChanged(e);
                            }
                        });
                        panelCon1.add(rdoSend, CC.xy(5, 7));

                        //---- rdoReqRep ----
                        rdoReqRep.setText("Request/Reply");
                        rdoReqRep.setToolTipText("Send messages to topic or queue and listen for reply. The sending destination\nis taken form the JmsStream message file and the listening destination is taken\nfrom the destination value. The either of these values are not present then the\nsending and/or listening destinations are taken form the Message file.");
                        rdoReqRep.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                rdoReqRepItemStateChanged(e);
                            }
                        });
                        panelCon1.add(rdoReqRep, CC.xy(7, 7));

                        //---- chkAsyncReply ----
                        chkAsyncReply.setText("Asynch. Reply");
                        chkAsyncReply.setEnabled(false);
                        chkAsyncReply.setToolTipText("The request will publish all messages in the file and set up a listener for each reply.\n(default is to block the request thread until the reply is received)");
                        panelCon1.add(chkAsyncReply, CC.xy(9, 7));

                        //---- lblReplyTimeout ----
                        lblReplyTimeout.setText("Reply Timeout");
                        lblReplyTimeout.setLabelFor(spReplyTimeout);
                        lblReplyTimeout.setEnabled(false);
                        lblReplyTimeout.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelCon1.add(lblReplyTimeout, CC.xy(11, 7));

                        //---- spReplyTimeout ----
                        spReplyTimeout.setEnabled(false);
                        spReplyTimeout.setModel(new SpinnerNumberModel(0, 0, null, 500));
                        spReplyTimeout.setToolTipText("The reply timeout value in milliseconds.");
                        panelCon1.add(spReplyTimeout, CC.xy(13, 7));

                        //---- lblMiliSec ----
                        lblMiliSec.setText("msec");
                        lblMiliSec.setEnabled(false);
                        panelCon1.add(lblMiliSec, CC.xy(15, 7));

                        //---- lblJndiConFactory ----
                        lblJndiConFactory.setText("* JNDI Context Factory");
                        lblJndiConFactory.setLabelFor(txtJndiContextFactory);
                        lblJndiConFactory.setFont(new Font("Tahoma", Font.BOLD, 11));
                        panelCon1.add(lblJndiConFactory, CC.xy(1, 9));

                        //---- txtJndiContextFactory ----
                        txtJndiContextFactory.setText(_conHelper.getInitialContextFactory());
                        txtJndiContextFactory.setToolTipText("Initial context factory for the JNDI.\n(default " + _conHelper.getInitialContextFactory() + ")");
                        panelCon1.add(txtJndiContextFactory, CC.xywh(3, 9, 7, 1));

                        //---- chkUseFileJNDI ----
                        chkUseFileJNDI.setText("Use local file based JNDI");
                        chkUseFileJNDI.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkUseFileJNDIItemStateChanged();
                            }
                        });
                        panelCon1.add(chkUseFileJNDI, CC.xywh(11, 9, 5, 1));

                        //---- lblProviderUrl ----
                        lblProviderUrl.setText("* Provider URL");
                        lblProviderUrl.setLabelFor(txtProviderUrl);
                        lblProviderUrl.setFont(new Font("Tahoma", Font.BOLD, 11));
                        panelCon1.add(lblProviderUrl, CC.xy(1, 11));

                        //---- txtProviderUrl ----
                        txtProviderUrl.setText(_conHelper.getProviderUrl());
                        txtProviderUrl.setToolTipText("Context URL for JNDI lookup. (default: " + _conHelper.getProviderUrl() + ")");
                        panelCon1.add(txtProviderUrl, CC.xywh(3, 11, 13, 1));
                    }
                    panelConnection.add(panelCon1, CC.xy(1, 3));

                    //======== paneCon2 ========
                    {
                        paneCon2.setLayout(new FormLayout(
                            "right:default, $lcgap, 135dlu, $rgap, right:default, $lcgap, 135dlu",
                            "default, $nlgap, 2*(default, $lgap), default"));

                        //---- chkJndiUserPwd ----
                        chkJndiUserPwd.setText("Use Separate JNDI/JMS Credentials ");
                        chkJndiUserPwd.setBorderPaintedFlat(true);
                        chkJndiUserPwd.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkJndiUserPwdItemStateChanged(e);
                            }
                        });
                        paneCon2.add(chkJndiUserPwd, CC.xy(3, 1));

                        //---- lblClientId ----
                        lblClientId.setText("Client ID");
                        lblClientId.setLabelFor(txtClientId);
                        lblClientId.setHorizontalAlignment(SwingConstants.TRAILING);
                        paneCon2.add(lblClientId, CC.xy(5, 1));

                        //---- txtClientId ----
                        txtClientId.setToolTipText("JMS client ID");
                        paneCon2.add(txtClientId, CC.xy(7, 1));

                        //---- lblUser ----
                        lblUser.setText("User");
                        lblUser.setLabelFor(txtUser);
                        paneCon2.add(lblUser, CC.xy(1, 3));

                        //---- txtUser ----
                        txtUser.setToolTipText("User name used for both JNDI and JMS.");
                        paneCon2.add(txtUser, CC.xy(3, 3));

                        //---- lblPassword ----
                        lblPassword.setText("Password");
                        lblPassword.setHorizontalAlignment(SwingConstants.TRAILING);
                        paneCon2.add(lblPassword, CC.xy(5, 3));

                        //---- txtPassword ----
                        txtPassword.setToolTipText("Password used for both JNDI and JMS");
                        paneCon2.add(txtPassword, CC.xy(7, 3));

                        //---- lblJndiUser ----
                        lblJndiUser.setText("JNDI User");
                        lblJndiUser.setLabelFor(txtJndiUser);
                        lblJndiUser.setEnabled(false);
                        paneCon2.add(lblJndiUser, CC.xy(1, 5));

                        //---- txtJndiUser ----
                        txtJndiUser.setEnabled(false);
                        txtJndiUser.setToolTipText("JNDI security principal");
                        paneCon2.add(txtJndiUser, CC.xy(3, 5));

                        //---- lblJndiPassword ----
                        lblJndiPassword.setText("JNDI Password");
                        lblJndiPassword.setLabelFor(txtJndiPassword);
                        lblJndiPassword.setEnabled(false);
                        lblJndiPassword.setHorizontalAlignment(SwingConstants.TRAILING);
                        paneCon2.add(lblJndiPassword, CC.xy(5, 5));

                        //---- txtJndiPassword ----
                        txtJndiPassword.setEnabled(false);
                        txtJndiPassword.setToolTipText("JNDI security credentials");
                        paneCon2.add(txtJndiPassword, CC.xy(7, 5));

                        //---- lblJmsUser ----
                        lblJmsUser.setText("JMS User");
                        lblJmsUser.setEnabled(false);
                        lblJmsUser.setLabelFor(txtJmsUser);
                        paneCon2.add(lblJmsUser, CC.xy(1, 7));

                        //---- txtJmsUser ----
                        txtJmsUser.setEnabled(false);
                        txtJmsUser.setToolTipText("JMS user name");
                        paneCon2.add(txtJmsUser, CC.xy(3, 7));

                        //---- lblJmsPassword ----
                        lblJmsPassword.setText("JMS Password");
                        lblJmsPassword.setLabelFor(txtJmsPassword);
                        lblJmsPassword.setEnabled(false);
                        lblJmsPassword.setHorizontalAlignment(SwingConstants.TRAILING);
                        paneCon2.add(lblJmsPassword, CC.xy(5, 7));

                        //---- txtJmsPassword ----
                        txtJmsPassword.setEnabled(false);
                        txtJmsPassword.setToolTipText("JMS user password");
                        paneCon2.add(txtJmsPassword, CC.xy(7, 7));
                    }
                    panelConnection.add(paneCon2, CC.xy(1, 5));
                }
                tabbedPane.addTab("Connection", panelConnection);

                //======== panelJNDI ========
                {
                    panelJNDI.setLayout(new FormLayout(
                        "$rgap, default:grow, $rgap",
                        "$nlgap, $lgap, top:default:grow"));

                    //======== panelProps ========
                    {
                        panelProps.setBorder(new TitledBorder("JNDI Custom Properties"));
                        panelProps.addComponentListener(new ComponentAdapter() {
                            @Override
                            public void componentHidden(ComponentEvent e) {
                                panelPropsComponentHidden();
                            }
                        });
                        panelProps.setLayout(new FormLayout(
                            "2dlu, default, 2dlu, 415dlu:grow, $lcgap, 2dlu",
                            "2dlu, fill:default:grow, $ugap"));

                        //======== pnlTableButtons ========
                        {
                            pnlTableButtons.setLayout(new FormLayout(
                                "14dlu",
                                "10dlu, bottom:14dlu, top:14dlu, $ugap, bottom:14dlu, top:14dlu"));

                            //---- btnAddRow ----
                            btnAddRow.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordAdd1.png")));
                            btnAddRow.setFont(UIManager.getFont("Button.font"));
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
                            pnlTableButtons.add(btnAddRow, CC.xy(1, 2));

                            //---- btnRemoveRow ----
                            btnRemoveRow.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordDelete1.png")));
                            btnRemoveRow.setFont(UIManager.getFont("Button.font"));
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
                            pnlTableButtons.add(btnRemoveRow, CC.xy(1, 3));
                        }
                        panelProps.add(pnlTableButtons, CC.xy(2, 2));

                        //======== scrJndiProperties ========
                        {
                            scrJndiProperties.setAutoscrolls(true);

                            //---- tblJndiProperties ----
                            tblJndiProperties.setCellSelectionEnabled(true);
                            tblJndiProperties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            tblJndiProperties.setModel(new DefaultTableModel(
                                new Object[][] {
                                },
                                new String[] {
                                    "Name", "Value"
                                }
                            ) {
                                Class<?>[] columnTypes = new Class<?>[] {
                                    String.class, String.class
                                };
                                @Override
                                public Class<?> getColumnClass(int columnIndex) {
                                    return columnTypes[columnIndex];
                                }
                            });
                            tblJndiProperties.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                            tblJndiProperties.setPreferredScrollableViewportSize(new Dimension(400, 225));
                            tblJndiProperties.addFocusListener(new FocusAdapter() {
                                @Override
                                public void focusLost(FocusEvent e) {
                                    tblJndiPropertiesFocusLost(e);
                                }
                            });
                            scrJndiProperties.setViewportView(tblJndiProperties);
                        }
                        panelProps.add(scrJndiProperties, CC.xy(4, 2));
                    }
                    panelJNDI.add(panelProps, CC.xy(2, 3));
                }
                tabbedPane.addTab("JNDI Options", panelJNDI);

                //======== panelInputOutput ========
                {
                    panelInputOutput.setLayout(new FormLayout(
                        "center:default:grow",
                        "15dlu, top:default"));

                    //======== panelIO1 ========
                    {
                        panelIO1.setLayout(new FormLayout(
                            "150dlu, 1dlu, 30dlu, $button, $lcgap, 200dlu, $glue, 11dlu",
                            "top:default, $lgap, default, $rgap, 2*(default, $lgap), default, $rgap, default, $nlgap, default"));

                        //======== pnlJmsStreamFile ========
                        {
                            pnlJmsStreamFile.setBorder(new TitledBorder("JmsStream Message File"));
                            pnlJmsStreamFile.setLayout(new FormLayout(
                                "3*(default, $lcgap), default:grow, 1dlu",
                                "default, $lgap, default"));

                            //---- btnNewMsgFile ----
                            btnNewMsgFile.setText("New");
                            btnNewMsgFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgNew.png")));
                            btnNewMsgFile.setToolTipText("Create and add a new JmsStream message file.");
                            btnNewMsgFile.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnNewMsgFileActionPerformed();
                                }
                            });
                            pnlJmsStreamFile.add(btnNewMsgFile, CC.xy(1, 1));

                            //---- btnEditMsgFile ----
                            btnEditMsgFile.setText("Edit");
                            btnEditMsgFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgEdit.png")));
                            btnEditMsgFile.setToolTipText("Add and edit an exsisting JmsStream message file.");
                            btnEditMsgFile.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnEditMsgFileActionPerformed();
                                }
                            });
                            pnlJmsStreamFile.add(btnEditMsgFile, CC.xy(3, 1));

                            //---- btnOpenMsgFile ----
                            btnOpenMsgFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/msgOpen.png")));
                            btnOpenMsgFile.setFont(UIManager.getFont("Button.font"));
                            btnOpenMsgFile.setToolTipText("Add an exsisting JmsStrem message file.");
                            btnOpenMsgFile.setText("Open");
                            btnOpenMsgFile.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnOpenMsgFileActionPerformed();
                                }
                            });
                            pnlJmsStreamFile.add(btnOpenMsgFile, CC.xy(5, 1));

                            //---- txtJmsStreamFile ----
                            txtJmsStreamFile.setToolTipText("Read/write captured messages from/to JmsStream message file.");
                            pnlJmsStreamFile.add(txtJmsStreamFile, CC.xywh(1, 3, 8, 1));
                        }
                        panelIO1.add(pnlJmsStreamFile, CC.xywh(1, 1, 8, 1));

                        //---- chkFileAppend ----
                        chkFileAppend.setText("Append to Existing JmsStream File");
                        chkFileAppend.setToolTipText("Append output to JmsStream message file. (default is to create a new file)");
                        panelIO1.add(chkFileAppend, CC.xy(1, 3, CC.LEFT, CC.DEFAULT));

                        //---- lblReplyFile ----
                        lblReplyFile.setText("Reply Save File");
                        lblReplyFile.setLabelFor(txtReplyFile);
                        lblReplyFile.setEnabled(false);
                        lblReplyFile.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelIO1.add(lblReplyFile, CC.xy(4, 3));

                        //---- txtReplyFile ----
                        txtReplyFile.setEnabled(false);
                        txtReplyFile.setToolTipText("Write capture reply messages to a JmsStream message file  for request/reply.");
                        panelIO1.add(txtReplyFile, CC.xy(6, 3));

                        //---- btnReplyFile ----
                        btnReplyFile.setEnabled(false);
                        btnReplyFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/ellipsis.png")));
                        btnReplyFile.setFont(UIManager.getFont("Button.font"));
                        btnReplyFile.setToolTipText("Open file dialog box.");
                        btnReplyFile.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                btnReplyFileActionPerformed();
                            }
                        });
                        panelIO1.add(btnReplyFile, CC.xy(8, 3));

                        //---- chkVerbose ----
                        chkVerbose.setText("Verbose Message Output to Screen");
                        chkVerbose.setToolTipText("Print detailed message text type body, binary body, map message, or an object.toString()\n to stdout when sent/received. (not setting -verbose will print only basic message info to screen)");
                        chkVerbose.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkVerboseItemStateChanged(e);
                            }
                        });
                        panelIO1.add(chkVerbose, CC.xy(1, 5, CC.LEFT, CC.DEFAULT));

                        //---- chkZip ----
                        chkZip.setText("Read/Write From ZIP File");
                        chkZip.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkZipItemStateChanged(e);
                            }
                        });
                        panelIO1.add(chkZip, CC.xywh(4, 5, 5, 1));

                        //---- chkNoEcho ----
                        chkNoEcho.setText("No Echo Output to Screen");
                        chkNoEcho.setToolTipText("Do not print messages to the screen.");
                        chkNoEcho.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkNoEchoItemStateChanged(e);
                            }
                        });
                        panelIO1.add(chkNoEcho, CC.xy(1, 7, CC.LEFT, CC.DEFAULT));

                        //---- lblZipEntry ----
                        lblZipEntry.setText("ZIP Entries");
                        lblZipEntry.setEnabled(false);
                        lblZipEntry.setLabelFor(lstZipEntry);
                        lblZipEntry.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelIO1.add(lblZipEntry, CC.xy(4, 7));

                        //======== scrZipEntry ========
                        {

                            //---- lstZipEntry ----
                            lstZipEntry.setVisibleRowCount(5);
                            lstZipEntry.setToolTipText("Read/write JmsStream file (entry) from/to a ZIP compressed file. (default: read all entries in zip)");
                            lstZipEntry.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            lstZipEntry.setEnabled(false);
                            scrZipEntry.setViewportView(lstZipEntry);
                        }
                        panelIO1.add(scrZipEntry, CC.xywh(6, 7, 1, 5));

                        //---- btnAddZipEntry ----
                        btnAddZipEntry.setText("Add");
                        btnAddZipEntry.setToolTipText("Add ZIP entry");
                        btnAddZipEntry.setEnabled(false);
                        btnAddZipEntry.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordAdd1.png")));
                        btnAddZipEntry.setHorizontalAlignment(SwingConstants.LEFT);
                        btnAddZipEntry.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                btnAddZipEntryActionPerformed();
                            }
                        });
                        panelIO1.add(btnAddZipEntry, CC.xy(4, 9));

                        //======== panelStats ========
                        {
                            panelStats.setLayout(new FormLayout(
                                "default, $lcgap, 30dlu, 2dlu, left:default",
                                "default"));

                            //---- lblStats ----
                            lblStats.setText("Show Statistical Info");
                            panelStats.add(lblStats, CC.xy(1, 1));

                            //---- spStats ----
                            spStats.setModel(new SpinnerNumberModel(0, 0, null, 1));
                            spStats.setToolTipText("Will collect and print statistical information every seconds.");
                            panelStats.add(spStats, CC.xy(3, 1));

                            //---- lblSec2 ----
                            lblSec2.setText("sec");
                            panelStats.add(lblSec2, CC.xy(5, 1, CC.DEFAULT, CC.CENTER));
                        }
                        panelIO1.add(panelStats, CC.xy(1, 9));

                        //---- btnRemoveZipEntry ----
                        btnRemoveZipEntry.setText("Remove");
                        btnRemoveZipEntry.setToolTipText("Remove ZIP entry");
                        btnRemoveZipEntry.setEnabled(false);
                        btnRemoveZipEntry.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordDelete1.png")));
                        btnRemoveZipEntry.setHorizontalAlignment(SwingConstants.LEFT);
                        btnRemoveZipEntry.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                btnRemoveZipEntryActionPerformed();
                            }
                        });
                        panelIO1.add(btnRemoveZipEntry, CC.xy(4, 11));
                    }
                    panelInputOutput.add(panelIO1, CC.xy(1, 2));
                }
                tabbedPane.addTab("Input/Output", panelInputOutput);

                //======== panelListener ========
                {
                    panelListener.setLayout(new FormLayout(
                        "center:default:grow",
                        "$ugap, top:default"));

                    //======== panelListener1 ========
                    {
                        panelListener1.setLayout(new FormLayout(
                            "75dlu, $lcgap, 47dlu, $rgap, right:88dlu, $lcgap, 50dlu, $lcgap, 140dlu, $glue, 11dlu:grow",
                            "10*(default, $lgap), default"));

                        //---- lblListenDest ----
                        lblListenDest.setText("* Listen Destination");
                        lblListenDest.setLabelFor(txtListenDest);
                        lblListenDest.setFont(new Font("Tahoma", Font.BOLD, 11));
                        panelListener1.add(lblListenDest, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                        //---- txtListenDest ----
                        txtListenDest.setToolTipText("The name of the Queue or Topic to listen to. ");
                        panelListener1.add(txtListenDest, CC.xywh(3, 1, 9, 1));

                        //---- lblSelector ----
                        lblSelector.setText("JMS Selector");
                        lblSelector.setLabelFor(txtSelector);
                        lblSelector.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelListener1.add(lblSelector, CC.xy(1, 3));

                        //---- txtSelector ----
                        txtSelector.setToolTipText("Set JMS message selector");
                        panelListener1.add(txtSelector, CC.xywh(3, 3, 9, 1));

                        //---- lblStopAfter ----
                        lblStopAfter.setText("Stop After Message");
                        lblStopAfter.setLabelFor(spStopAfter);
                        lblStopAfter.setHorizontalAlignment(SwingConstants.RIGHT);
                        panelListener1.add(lblStopAfter, CC.xy(1, 5));

                        //---- spStopAfter ----
                        spStopAfter.setToolTipText("Stop listening and exit after this number of received messages.");
                        spStopAfter.setModel(new SpinnerNumberModel(0, 0, null, 1));
                        panelListener1.add(spStopAfter, CC.xy(3, 5));

                        //---- lblAckMode ----
                        lblAckMode.setText("Acknowledgement Mode");
                        lblAckMode.setLabelFor(cboAckMode);
                        panelListener1.add(lblAckMode, CC.xy(5, 5, CC.RIGHT, CC.DEFAULT));

                        //---- cboAckMode ----
                        cboAckMode.setMaximumRowCount(5);
                        cboAckMode.setModel(new DefaultComboBoxModel(new String[] {
                            "AUTO_ACKNOWLEDGE",
                            "CLIENT_ACKNOWLEDGE",
                            "DUPS_OK_ACKNOWLEDGE",
                            "EXPLICIT_CLIENT_ACKNOWLEDGE",
                            "NO_ACKNOWLEDGE"
                        }));
                        cboAckMode.setToolTipText("Set the JMS acknowledgement mode.");
                        panelListener1.add(cboAckMode, CC.xywh(7, 5, 3, 1, CC.LEFT, CC.DEFAULT));

                        //---- chkBrowseQueue ----
                        chkBrowseQueue.setText("Browse Queue");
                        chkBrowseQueue.setToolTipText("Browse the queue. (will not remove the messages from queue)");
                        panelListener1.add(chkBrowseQueue, CC.xywh(1, 7, 2, 1));

                        //---- lblRcvTimestamp ----
                        lblRcvTimestamp.setText("Receiving Timestamp Property");
                        lblRcvTimestamp.setLabelFor(txtSelector);
                        panelListener1.add(lblRcvTimestamp, CC.xywh(3, 7, 3, 1, CC.RIGHT, CC.DEFAULT));

                        //---- txtRcvTimestamp ----
                        txtRcvTimestamp.setToolTipText("Add receiving timestamp to this property message property.");
                        panelListener1.add(txtRcvTimestamp, CC.xywh(7, 7, 3, 1));

                        //---- chkNoConfirm ----
                        chkNoConfirm.setText("No Confirmation");
                        chkNoConfirm.setToolTipText("JmsStream will not send client acknowledgments.");
                        panelListener1.add(chkNoConfirm, CC.xywh(1, 9, 3, 1, CC.LEFT, CC.DEFAULT));

                        //---- lblCsvSaveFile ----
                        lblCsvSaveFile.setText("CSV Save File");
                        lblCsvSaveFile.setLabelFor(txtCsvSaveFile);
                        lblCsvSaveFile.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelListener1.add(lblCsvSaveFile, CC.xy(5, 9, CC.RIGHT, CC.DEFAULT));

                        //---- txtCsvSaveFile ----
                        txtCsvSaveFile.setToolTipText("Write captured message headers and properties to CVS file used for gathering performance info.");
                        panelListener1.add(txtCsvSaveFile, CC.xywh(7, 9, 3, 1));

                        //---- btnCsvSaveFile ----
                        btnCsvSaveFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/ellipsis.png")));
                        btnCsvSaveFile.setFont(UIManager.getFont("Button.font"));
                        btnCsvSaveFile.setToolTipText("Open file dialog box.");
                        btnCsvSaveFile.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                btnCsvSaveFileActionPerformed();
                            }
                        });
                        panelListener1.add(btnCsvSaveFile, CC.xy(11, 9));

                        //---- chkTimed ----
                        chkTimed.setText("Record Time Between Messages");
                        chkTimed.setToolTipText("Record timed statistics for real-life replay.");
                        panelListener1.add(chkTimed, CC.xywh(1, 11, 3, 1));

                        //---- lblZipMessageNum ----
                        lblZipMessageNum.setText("Messages Per ZIP Entry");
                        lblZipMessageNum.setLabelFor(spZipMessageNum);
                        lblZipMessageNum.setEnabled(false);
                        panelListener1.add(lblZipMessageNum, CC.xy(5, 11, CC.RIGHT, CC.DEFAULT));

                        //---- spZipMessageNum ----
                        spZipMessageNum.setModel(new SpinnerNumberModel(0, 0, null, 1));
                        spZipMessageNum.setEnabled(false);
                        spZipMessageNum.setToolTipText("Maximum messages per ZIP entry. (breaks up large file captures into separate ZIP entries in the ZIP file)");
                        panelListener1.add(spZipMessageNum, CC.xy(7, 11));

                        //---- chkEchoCsv ----
                        chkEchoCsv.setText("Echo CSV Output to Screen");
                        chkEchoCsv.setToolTipText("Print message header and property fields in CSV format to stdout when received.");
                        panelListener1.add(chkEchoCsv, CC.xywh(1, 13, 3, 1));

                        //---- chkDurable ----
                        chkDurable.setText("Enable Durable Topic Subscriptions");
                        chkDurable.setEnabled(false);
                        chkDurable.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkDurableItemStateChanged(e);
                                chkDurableItemStateChanged(e);
                            }
                        });
                        panelListener1.add(chkDurable, CC.xywh(7, 13, 3, 1));

                        //---- chkEchoXml ----
                        chkEchoXml.setText("Echo XML Formated Output");
                        chkEchoXml.setToolTipText("Print messages text type body and map message in XML format to stdout when received.");
                        panelListener1.add(chkEchoXml, CC.xywh(1, 15, 3, 1));

                        //---- lblDurableName ----
                        lblDurableName.setText("* Durable Name");
                        lblDurableName.setHorizontalAlignment(SwingConstants.TRAILING);
                        lblDurableName.setLabelFor(txtDurableName);
                        lblDurableName.setEnabled(false);
                        lblDurableName.setFont(new Font("Tahoma", Font.BOLD, 11));
                        panelListener1.add(lblDurableName, CC.xy(5, 15));

                        //---- txtDurableName ----
                        txtDurableName.setEnabled(false);
                        txtDurableName.setToolTipText("Use a durable topic subscriber with this name.");
                        panelListener1.add(txtDurableName, CC.xywh(7, 15, 5, 1));

                        //---- chkEchoRaw ----
                        chkEchoRaw.setText("Only Echo Raw Message Data");
                        chkEchoRaw.setToolTipText("Print message.toString() to the screen and do not write to file.");
                        chkEchoRaw.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkEchoRawItemStateChanged(e);
                            }
                        });
                        panelListener1.add(chkEchoRaw, CC.xywh(1, 17, 4, 1));

                        //---- chkUnsubscribe ----
                        chkUnsubscribe.setText("Unsubscribe on Exit");
                        chkUnsubscribe.setEnabled(false);
                        chkUnsubscribe.setToolTipText("Unsubscribe from the durable topic on exit.");
                        panelListener1.add(chkUnsubscribe, CC.xywh(7, 17, 3, 1));

                        //---- chkExtractMonMsg ----
                        chkExtractMonMsg.setText("Extract TIBCO EMS Monitor Message");
                        chkExtractMonMsg.setToolTipText("Extracts embedded client messages from TIBCO EMS System Monitor");
                        chkExtractMonMsg.addChangeListener(new ChangeListener() {
                            public void stateChanged(ChangeEvent e) {
                                chkExtractMonMsgStateChanged();
                            }
                        });
                        panelListener1.add(chkExtractMonMsg, CC.xywh(1, 19, 5, 1));
                    }
                    panelListener.add(panelListener1, CC.xy(1, 2));
                }
                tabbedPane.addTab("Listener Options", panelListener);

                //======== panelSend ========
                {
                    panelSend.setLayout(new FormLayout(
                        "center:default:grow",
                        "$ugap, top:default"));

                    //======== panelSend1 ========
                    {
                        panelSend1.setLayout(new FormLayout(
                            "right:default, $lcgap, 50dlu, $lcgap, default, $rgap, left:default:grow, $rgap, default, $lcgap, 50dlu, $lcgap, default, $lcgap, left:50px",
                            "default, $lgap, default, $rgap, 3*(default, $lgap), default, $rgap, 3*(default, $lgap), default"));

                        //---- lblSendDest ----
                        lblSendDest.setText("Override Send Destination");
                        lblSendDest.setLabelFor(txtSendDest);
                        panelSend1.add(lblSendDest, CC.xy(1, 1));

                        //---- txtSendDest ----
                        txtSendDest.setToolTipText("Override the Sending Destinations in the Message file with this destination.");
                        panelSend1.add(txtSendDest, CC.xywh(3, 1, 13, 1));

                        //---- chkCompress ----
                        chkCompress.setText("Compress Messages");
                        chkCompress.setToolTipText("Compress the messages when sending. (TIBCO EMS only)");
                        panelSend1.add(chkCompress, CC.xywh(2, 3, 4, 1, CC.LEFT, CC.DEFAULT));

                        //---- lblDeliveryMode ----
                        lblDeliveryMode.setText("Override Delivery Mode");
                        lblDeliveryMode.setLabelFor(cboDeliveryMode);
                        lblDeliveryMode.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblDeliveryMode, CC.xy(9, 3));

                        //---- cboDeliveryMode ----
                        cboDeliveryMode.setMaximumRowCount(4);
                        cboDeliveryMode.setModel(new DefaultComboBoxModel(new String[] {
                            " ",
                            "NON_PERSISTENT",
                            "PERSISTENT",
                            "RELIABLE_DELIVERY"
                        }));
                        cboDeliveryMode.setToolTipText("Overrides the delivermode in the message.");
                        panelSend1.add(cboDeliveryMode, CC.xywh(11, 3, 3, 1, CC.LEFT, CC.DEFAULT));

                        //---- lblSpeed ----
                        lblSpeed.setText("Timed Replay Speed");
                        lblSpeed.setLabelFor(spSpeed);
                        lblSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblSpeed, CC.xy(1, 5));

                        //---- spSpeed ----
                        spSpeed.setModel(new SpinnerNumberModel(0.0F, 0.0F, null, 0.1F));
                        spSpeed.setToolTipText("Speed-up or slow-down replay if using timed play back. (default 1) \n(requires the messages were captured using \"timed\")");
                        panelSend1.add(spSpeed, CC.xy(3, 5));

                        //---- lblFileLoop ----
                        lblFileLoop.setText("Repeat Send File Publication");
                        lblFileLoop.setLabelFor(spFileLoop);
                        lblFileLoop.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblFileLoop, CC.xywh(5, 5, 5, 1));

                        //---- spFileLoop ----
                        spFileLoop.setModel(new SpinnerNumberModel(1, 1, null, 1));
                        spFileLoop.setToolTipText("Loop over the read file and re-send the messages this number of times.  (0 will loop indefinitely)");
                        panelSend1.add(spFileLoop, CC.xy(11, 5));

                        //---- lblSndTimestamp ----
                        lblSndTimestamp.setText("Timestamp Property Field");
                        lblSndTimestamp.setLabelFor(txtSndTimestamp);
                        lblSndTimestamp.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblSndTimestamp, CC.xy(1, 7));

                        //---- txtSndTimestamp ----
                        txtSndTimestamp.setToolTipText("Add sending timestamp to this message property.");
                        panelSend1.add(txtSndTimestamp, CC.xywh(3, 7, 13, 1));

                        //---- lblSequence ----
                        lblSequence.setText("Sequence Property Field");
                        lblSequence.setLabelFor(txtSequence);
                        lblSequence.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblSequence, CC.xy(1, 9));

                        //---- txtSequence ----
                        txtSequence.setToolTipText("Add sending message sequence number to this message property.");
                        panelSend1.add(txtSequence, CC.xywh(3, 9, 13, 1));

                        //---- lblRate ----
                        lblRate.setText("Message Rate");
                        lblRate.setLabelFor(spRate);
                        lblRate.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblRate, CC.xy(1, 11));

                        //---- spRate ----
                        spRate.setModel(new SpinnerNumberModel(0.0F, 0.0F, null, 0.1F));
                        spRate.setToolTipText("Number of messages to send per second. (0 = fast as possible)");
                        panelSend1.add(spRate, CC.xy(3, 11));

                        //---- lblMsgSec ----
                        lblMsgSec.setText("msg/sec");
                        panelSend1.add(lblMsgSec, CC.xy(5, 11));

                        //---- chkVariableMsgRate ----
                        chkVariableMsgRate.setText("Use a Variable Msg Rate");
                        chkVariableMsgRate.setHorizontalAlignment(SwingConstants.LEFT);
                        chkVariableMsgRate.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkVariableMsgRateItemStateChanged(e);
                            }
                        });
                        panelSend1.add(chkVariableMsgRate, CC.xywh(1, 13, 3, 1));

                        //---- chkStartingRate ----
                        chkStartingRate.setText("Override Default Starting Rate");
                        chkStartingRate.setHorizontalAlignment(SwingConstants.LEFT);
                        chkStartingRate.setEnabled(false);
                        chkStartingRate.setToolTipText("Override default starting rate. (if this is disabled the message rate starts at maxrate/numberofintervals)");
                        chkStartingRate.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                chkStartingRateItemStateChanged(e);
                            }
                        });
                        panelSend1.add(chkStartingRate, CC.xywh(5, 13, 5, 1));

                        //---- lblMaxRate ----
                        lblMaxRate.setText("* Max Message Rate");
                        lblMaxRate.setLabelFor(spMaxRate);
                        lblMaxRate.setEnabled(false);
                        lblMaxRate.setFont(new Font("Tahoma", Font.BOLD, 11));
                        lblMaxRate.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblMaxRate, CC.xy(1, 15));

                        //---- spMaxRate ----
                        spMaxRate.setModel(new SpinnerNumberModel(0.0F, 0.0F, null, 0.1F));
                        spMaxRate.setEnabled(false);
                        spMaxRate.setToolTipText("Maximum messages to send per second. (program will exit after achieving maxrate)");
                        panelSend1.add(spMaxRate, CC.xy(3, 15));

                        //---- lblMsgSec3 ----
                        lblMsgSec3.setText("msg/sec");
                        lblMsgSec3.setEnabled(false);
                        lblMsgSec3.setToolTipText("JmsStream Option: -maxrate");
                        panelSend1.add(lblMsgSec3, CC.xy(5, 15));

                        //---- lblStartRate ----
                        lblStartRate.setText("Starting Message Rate");
                        lblStartRate.setLabelFor(spStartRate);
                        lblStartRate.setEnabled(false);
                        lblStartRate.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblStartRate, CC.xy(9, 15));

                        //---- spStartRate ----
                        spStartRate.setModel(new SpinnerNumberModel(0.0F, 0.0F, null, 0.1F));
                        spStartRate.setEnabled(false);
                        spStartRate.setToolTipText("(optional) The starting message rate to send per second.");
                        panelSend1.add(spStartRate, CC.xy(11, 15));

                        //---- lblMsgSec2 ----
                        lblMsgSec2.setText("msg/sec");
                        lblMsgSec2.setEnabled(false);
                        panelSend1.add(lblMsgSec2, CC.xy(13, 15));

                        //---- lblNumberOfIntervals ----
                        lblNumberOfIntervals.setText("* Number of Intervals");
                        lblNumberOfIntervals.setLabelFor(spNumberOfIntervals);
                        lblNumberOfIntervals.setEnabled(false);
                        lblNumberOfIntervals.setFont(new Font("Tahoma", Font.BOLD, 11));
                        lblNumberOfIntervals.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblNumberOfIntervals, CC.xy(1, 17));

                        //---- spNumberOfIntervals ----
                        spNumberOfIntervals.setModel(new SpinnerNumberModel(0, 0, null, 1));
                        spNumberOfIntervals.setEnabled(false);
                        spNumberOfIntervals.setToolTipText("Number of message intervals between \"Starting Message Rate\" and \"Max Message Rate\".\n(maxrate/numberofintervals = message rate step size)");
                        panelSend1.add(spNumberOfIntervals, CC.xy(3, 17));

                        //---- lblIntervalSize ----
                        lblIntervalSize.setText("* Interval Size");
                        lblIntervalSize.setLabelFor(spIntervalSize);
                        lblIntervalSize.setEnabled(false);
                        lblIntervalSize.setFont(new Font("Tahoma", Font.BOLD, 11));
                        lblIntervalSize.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelSend1.add(lblIntervalSize, CC.xy(9, 17));

                        //---- spIntervalSize ----
                        spIntervalSize.setEnabled(false);
                        spIntervalSize.setToolTipText("Number of messages per interval. (overrides \"Repeat Send File Publication\")");
                        panelSend1.add(spIntervalSize, CC.xy(11, 17));

                        //---- lblRatestamp ----
                        lblRatestamp.setText("Rate Stamp Property Field");
                        lblRatestamp.setHorizontalAlignment(SwingConstants.TRAILING);
                        lblRatestamp.setLabelFor(txtRatestamp);
                        lblRatestamp.setEnabled(false);
                        panelSend1.add(lblRatestamp, CC.xy(1, 19));

                        //---- txtRatestamp ----
                        txtRatestamp.setEnabled(false);
                        txtRatestamp.setToolTipText("(optional) Add current msg/sec rate to this message property.");
                        panelSend1.add(txtRatestamp, CC.xywh(3, 19, 13, 1));
                    }
                    panelSend.add(panelSend1, CC.xy(1, 2));
                }
                tabbedPane.addTab("Send Options", panelSend);
                tabbedPane.setEnabledAt(4, false);

                //======== panelSsl ========
                {
                    panelSsl.setLayout(new FormLayout(
                        "center:440dlu:grow",
                        "5dlu, default"));

                    //======== tabbedPaneSsl ========
                    {
                        tabbedPaneSsl.setTabPlacement(SwingConstants.LEFT);

                        //======== panelTibcoSsl ========
                        {
                            panelTibcoSsl.setFont(UIManager.getFont("Label.font"));
                            panelTibcoSsl.setLayout(new FormLayout(
                                "5dlu, default, $rgap, $button, $lcgap, 220dlu, 11dlu, $lcgap, default, $glue, 5dlu",
                                "$rgap, 8*($lgap, default), $nlgap, default, 5dlu"));

                            //======== panel1 ========
                            {
                                panel1.setLayout(new FormLayout(
                                    "default, $lcgap, default",
                                    "default"));

                                //---- lblSslVendor ----
                                lblSslVendor.setText("SSL Vendor");
                                lblSslVendor.setLabelFor(cboSslVendor);
                                lblSslVendor.setHorizontalAlignment(SwingConstants.TRAILING);
                                panel1.add(lblSslVendor, CC.xy(1, 1));

                                //---- cboSslVendor ----
                                cboSslVendor.setMaximumRowCount(5);
                                cboSslVendor.setModel(new DefaultComboBoxModel(new String[] {
                                    "j2se-default",
                                    "j2se",
                                    "entrust61",
                                    "ibm"
                                }));
                                cboSslVendor.setToolTipText("SSL vendor: 'j2se-default', 'j2se', 'entrust61', or 'ibm'.");
                                cboSslVendor.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        cboSslVendorActionPerformed();
                                    }
                                });
                                panel1.add(cboSslVendor, CC.xy(3, 1, CC.LEFT, CC.DEFAULT));
                            }
                            panelTibcoSsl.add(panel1, CC.xy(2, 5));

                            //---- txtSslLimitaionWarning2 ----
                            txtSslLimitaionWarning2.setFont(new Font("Tahoma", Font.BOLD, 12));
                            txtSslLimitaionWarning2.setBackground(UIManager.getColor("Label.background"));
                            txtSslLimitaionWarning2.setEditable(false);
                            txtSslLimitaionWarning2.setText("Due to limitations with the SSL libraries the SSL parameters can only be set once.  After the SSL connection is initiated you must re-start JmsStream to change these settings.");
                            txtSslLimitaionWarning2.setFocusable(false);
                            txtSslLimitaionWarning2.setFocusCycleRoot(false);
                            txtSslLimitaionWarning2.setAutoscrolls(false);
                            panelTibcoSsl.add(txtSslLimitaionWarning2, CC.xywh(2, 3, 10, 1));

                            //---- lblSslCiphers ----
                            lblSslCiphers.setText("SSL Ciphers");
                            lblSslCiphers.setLabelFor(txtSslCiphers);
                            lblSslCiphers.setHorizontalAlignment(SwingConstants.TRAILING);
                            panelTibcoSsl.add(lblSslCiphers, CC.xy(4, 5));

                            //---- txtSslCiphers ----
                            txtSslCiphers.setToolTipText("OpenSSL names for the cipher suites used for encryption.");
                            panelTibcoSsl.add(txtSslCiphers, CC.xywh(6, 5, 2, 1));

                            //---- chkSslJndi ----
                            chkSslJndi.setText("Use SSL for JNDI Lookup");
                            chkSslJndi.setToolTipText("Use SSL for JNDI lookup.");
                            chkSslJndi.setSelected(true);
                            panelTibcoSsl.add(chkSslJndi, CC.xy(2, 7));

                            //---- lblSslHostName ----
                            lblSslHostName.setText("SSL Host Name");
                            lblSslHostName.setLabelFor(txtSslHostName);
                            lblSslHostName.setHorizontalAlignment(SwingConstants.TRAILING);
                            panelTibcoSsl.add(lblSslHostName, CC.xywh(3, 7, 2, 1));

                            //---- txtSslHostName ----
                            txtSslHostName.setToolTipText("Name expected in the server certificate.");
                            panelTibcoSsl.add(txtSslHostName, CC.xywh(6, 7, 2, 1));

                            //---- chkSslAuthOnly ----
                            chkSslAuthOnly.setText("SSL Authentication Only");
                            chkSslAuthOnly.setToolTipText("Use SSL encryption for authentication only. (TIBCO EMS only)");
                            panelTibcoSsl.add(chkSslAuthOnly, CC.xy(2, 9));

                            //---- lblSslIdentity ----
                            lblSslIdentity.setText("SSL Identity");
                            lblSslIdentity.setLabelFor(txtSslIdentity);
                            lblSslIdentity.setHorizontalAlignment(SwingConstants.TRAILING);
                            panelTibcoSsl.add(lblSslIdentity, CC.xy(4, 9));

                            //---- txtSslIdentity ----
                            txtSslIdentity.setToolTipText("Client identity file.");
                            panelTibcoSsl.add(txtSslIdentity, CC.xy(6, 9));

                            //---- btnSslIdentityFile ----
                            btnSslIdentityFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/ellipsis.png")));
                            btnSslIdentityFile.setFont(UIManager.getFont("Button.font"));
                            btnSslIdentityFile.setToolTipText("Open file dialog box.");
                            btnSslIdentityFile.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnSslIdentityFileActionPerformed();
                                }
                            });
                            panelTibcoSsl.add(btnSslIdentityFile, CC.xy(7, 9));

                            //---- chkVerifyHostName ----
                            chkVerifyHostName.setText("Verify Host Name");
                            chkVerifyHostName.setToolTipText("Host name verification");
                            chkVerifyHostName.addItemListener(new ItemListener() {
                                public void itemStateChanged(ItemEvent e) {
                                    chkVerifyHostNameItemStateChanged();
                                }
                            });
                            panelTibcoSsl.add(chkVerifyHostName, CC.xy(2, 11));

                            //---- lblSslPassword ----
                            lblSslPassword.setText("SSL ID Password");
                            lblSslPassword.setLabelFor(txtSslPassword);
                            lblSslPassword.setHorizontalAlignment(SwingConstants.TRAILING);
                            panelTibcoSsl.add(lblSslPassword, CC.xywh(3, 11, 2, 1));

                            //---- txtSslPassword ----
                            txtSslPassword.setToolTipText("Password to decrypt client identity file.");
                            panelTibcoSsl.add(txtSslPassword, CC.xywh(6, 11, 2, 1));

                            //---- chkVerifyHost ----
                            chkVerifyHost.setText("Verify Host");
                            chkVerifyHost.setToolTipText("Host verification");
                            panelTibcoSsl.add(chkVerifyHost, CC.xy(2, 13));

                            //---- lblSslKey ----
                            lblSslKey.setText("SSL Key File");
                            lblSslKey.setLabelFor(txtSslKey);
                            lblSslKey.setToolTipText("JmsStream Option: -ssl_key");
                            lblSslKey.setHorizontalAlignment(SwingConstants.TRAILING);
                            lblSslKey.setEnabled(false);
                            panelTibcoSsl.add(lblSslKey, CC.xy(4, 13));

                            //---- txtSslKey ----
                            txtSslKey.setEnabled(false);
                            txtSslKey.setToolTipText("Client key file or private key file. (only valid for 'entrust' and 'ibm')");
                            panelTibcoSsl.add(txtSslKey, CC.xy(6, 13));

                            //---- btnSslKeyFile ----
                            btnSslKeyFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/ellipsis.png")));
                            btnSslKeyFile.setFont(UIManager.getFont("Button.font"));
                            btnSslKeyFile.setToolTipText("Open file dialog box.");
                            btnSslKeyFile.setEnabled(false);
                            btnSslKeyFile.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnSslKeyFileActionPerformed();
                                }
                            });
                            panelTibcoSsl.add(btnSslKeyFile, CC.xy(7, 13));

                            //---- lblSslTrusted ----
                            lblSslTrusted.setText("SSL Trusted Certs");
                            lblSslTrusted.setHorizontalAlignment(SwingConstants.TRAILING);
                            panelTibcoSsl.add(lblSslTrusted, CC.xywh(2, 15, 3, 1));

                            //======== scrSslTrusted ========
                            {

                                //---- lstSslTrusted ----
                                lstSslTrusted.setToolTipText("File(s) with trusted certificate(s).");
                                lstSslTrusted.setVisibleRowCount(4);
                                lstSslTrusted.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                                scrSslTrusted.setViewportView(lstSslTrusted);
                            }
                            panelTibcoSsl.add(scrSslTrusted, CC.xywh(6, 15, 2, 5));

                            //---- chkSslTrace ----
                            chkSslTrace.setText("Show SSL Trace");
                            chkSslTrace.setToolTipText("Trace SSL initialization.");
                            panelTibcoSsl.add(chkSslTrace, CC.xy(2, 17));

                            //---- btnAddTrustedCert ----
                            btnAddTrustedCert.setText("Add");
                            btnAddTrustedCert.setToolTipText("Add SSL Trusted Certificate");
                            btnAddTrustedCert.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordAdd1.png")));
                            btnAddTrustedCert.setHorizontalAlignment(SwingConstants.LEFT);
                            btnAddTrustedCert.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnAddTrustedCertActionPerformed();
                                }
                            });
                            panelTibcoSsl.add(btnAddTrustedCert, CC.xy(4, 17));

                            //---- chkSslDebugTrace ----
                            chkSslDebugTrace.setText("Show SSL Debug Trace");
                            chkSslDebugTrace.setToolTipText("Trace SSL handshake and related.");
                            panelTibcoSsl.add(chkSslDebugTrace, CC.xy(2, 19));

                            //---- btnRemoveTrustedCert ----
                            btnRemoveTrustedCert.setText("Remove");
                            btnRemoveTrustedCert.setToolTipText("Remove SSL Trusted Certificate");
                            btnRemoveTrustedCert.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordDelete1.png")));
                            btnRemoveTrustedCert.setHorizontalAlignment(SwingConstants.LEFT);
                            btnRemoveTrustedCert.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnRemoveTrustedCertActionPerformed();
                                }
                            });
                            panelTibcoSsl.add(btnRemoveTrustedCert, CC.xy(4, 19));
                        }
                        tabbedPaneSsl.addTab("TIBCO SSL", panelTibcoSsl);

                        //======== panelJavaSsl ========
                        {
                            panelJavaSsl.setLayout(new FormLayout(
                                "5dlu, right:default, $lcgap, 50dlu, $lcgap, default:grow, 11dlu, 5dlu",
                                "5dlu, default, $rgap, 2*(default, $lgap), default, $rgap, 2*(default, $lgap), default, $ugap, default, 5dlu"));

                            //---- chkTrustAllCerts ----
                            chkTrustAllCerts.setText("Trust All Servers Certificates");
                            chkTrustAllCerts.setHorizontalAlignment(SwingConstants.CENTER);
                            chkTrustAllCerts.setSelected(true);
                            chkTrustAllCerts.addItemListener(new ItemListener() {
                                public void itemStateChanged(ItemEvent e) {
                                    chkTrustAllCertsItemStateChanged();
                                }
                            });
                            panelJavaSsl.add(chkTrustAllCerts, CC.xywh(2, 2, 3, 1));

                            //---- txtSslLimitaionWarning ----
                            txtSslLimitaionWarning.setFont(new Font("Tahoma", Font.BOLD, 12));
                            txtSslLimitaionWarning.setBackground(UIManager.getColor("Label.background"));
                            txtSslLimitaionWarning.setEditable(false);
                            txtSslLimitaionWarning.setText("Due to limitations with the SSL libraries the SSL parameters can only be set once.  After the SSL connection is initiated you must re-start JmsStream to change these settings.");
                            txtSslLimitaionWarning.setFocusable(false);
                            txtSslLimitaionWarning.setFocusCycleRoot(false);
                            txtSslLimitaionWarning.setAutoscrolls(false);
                            panelJavaSsl.add(txtSslLimitaionWarning, CC.xywh(6, 2, 2, 1));

                            //---- lblKeyStoreType ----
                            lblKeyStoreType.setText("Key Store Type");
                            lblKeyStoreType.setLabelFor(cboKeyStoreType);
                            lblKeyStoreType.setEnabled(false);
                            panelJavaSsl.add(lblKeyStoreType, CC.xy(2, 4));

                            //---- cboKeyStoreType ----
                            cboKeyStoreType.setModel(new DefaultComboBoxModel(new String[] {
                                "JKS",
                                "PKCS12"
                            }));
                            cboKeyStoreType.setMaximumRowCount(3);
                            cboKeyStoreType.setEnabled(false);
                            panelJavaSsl.add(cboKeyStoreType, CC.xy(4, 4));

                            //---- lblKeyStore ----
                            lblKeyStore.setText("Key Store");
                            lblKeyStore.setEnabled(false);
                            panelJavaSsl.add(lblKeyStore, CC.xy(2, 6));

                            //---- txtKeyStore ----
                            txtKeyStore.setEnabled(false);
                            txtKeyStore.setToolTipText("Key Store File Name");
                            panelJavaSsl.add(txtKeyStore, CC.xywh(4, 6, 3, 1));

                            //---- btnKeyStoreFile ----
                            btnKeyStoreFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/ellipsis.png")));
                            btnKeyStoreFile.setFont(UIManager.getFont("Button.font"));
                            btnKeyStoreFile.setToolTipText("Open file dialog box.");
                            btnKeyStoreFile.setEnabled(false);
                            btnKeyStoreFile.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnKeyStoreFileActionPerformed();
                                }
                            });
                            panelJavaSsl.add(btnKeyStoreFile, CC.xy(7, 6));

                            //---- lblKeyStorePassword ----
                            lblKeyStorePassword.setText("Key Store Password");
                            lblKeyStorePassword.setLabelFor(txtKeyStorePassword);
                            lblKeyStorePassword.setEnabled(false);
                            panelJavaSsl.add(lblKeyStorePassword, CC.xy(2, 8));

                            //---- txtKeyStorePassword ----
                            txtKeyStorePassword.setEnabled(false);
                            txtKeyStorePassword.setToolTipText("Key Store Password");
                            panelJavaSsl.add(txtKeyStorePassword, CC.xywh(4, 8, 4, 1));

                            //---- lblTrustStoreType ----
                            lblTrustStoreType.setText("Trust Store Type");
                            lblTrustStoreType.setLabelFor(cboTrustStoreType);
                            lblTrustStoreType.setEnabled(false);
                            panelJavaSsl.add(lblTrustStoreType, CC.xy(2, 10));

                            //---- cboTrustStoreType ----
                            cboTrustStoreType.setModel(new DefaultComboBoxModel(new String[] {
                                "JKS",
                                "PKCS12"
                            }));
                            cboTrustStoreType.setMaximumRowCount(3);
                            cboTrustStoreType.setEnabled(false);
                            panelJavaSsl.add(cboTrustStoreType, CC.xy(4, 10));

                            //---- lblTrustStore ----
                            lblTrustStore.setText("* Trust Store");
                            lblTrustStore.setLabelFor(txtTrustStore);
                            lblTrustStore.setFont(new Font("Tahoma", Font.BOLD, 11));
                            lblTrustStore.setEnabled(false);
                            panelJavaSsl.add(lblTrustStore, CC.xy(2, 12));

                            //---- txtTrustStore ----
                            txtTrustStore.setEnabled(false);
                            txtTrustStore.setText("Trust Store File Name");
                            panelJavaSsl.add(txtTrustStore, CC.xywh(4, 12, 3, 1));

                            //---- btnTrustStoreFile ----
                            btnTrustStoreFile.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/ellipsis.png")));
                            btnTrustStoreFile.setFont(UIManager.getFont("Button.font"));
                            btnTrustStoreFile.setToolTipText("Open file dialog box.");
                            btnTrustStoreFile.setEnabled(false);
                            btnTrustStoreFile.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    btnTrustStoreFileActionPerformed();
                                }
                            });
                            panelJavaSsl.add(btnTrustStoreFile, CC.xy(7, 12));

                            //---- lblTrustStorePassword ----
                            lblTrustStorePassword.setText("Trust Store Password");
                            lblTrustStorePassword.setLabelFor(txtTrustStorePassword);
                            lblTrustStorePassword.setEnabled(false);
                            panelJavaSsl.add(lblTrustStorePassword, CC.xy(2, 14));

                            //---- txtTrustStorePassword ----
                            txtTrustStorePassword.setEnabled(false);
                            txtTrustStorePassword.setToolTipText("Trust Store Password");
                            panelJavaSsl.add(txtTrustStorePassword, CC.xywh(4, 14, 4, 1));

                            //---- lblSslDebugParam ----
                            lblSslDebugParam.setText("SSL Debug Parameters");
                            lblSslDebugParam.setLabelFor(txtSslDebugParams);
                            panelJavaSsl.add(lblSslDebugParam, CC.xy(2, 16));

                            //---- txtSslDebugParams ----
                            txtSslDebugParams.setToolTipText("One or more of the following strings seperated by commas:\nssl, handshake, data, trustmanager");
                            panelJavaSsl.add(txtSslDebugParams, CC.xywh(4, 16, 4, 1));
                        }
                        tabbedPaneSsl.addTab("Java SSL", panelJavaSsl);
                        tabbedPaneSsl.setEnabledAt(1, false);
                    }
                    panelSsl.add(tabbedPaneSsl, CC.xy(1, 2));
                }
                tabbedPane.addTab("SSL Options", panelSsl);
                tabbedPane.setEnabledAt(5, false);

                //======== panelTrans ========
                {
                    panelTrans.setLayout(new FormLayout(
                        "$ugap, center:default:grow, $ugap",
                        "$ugap, top:default"));

                    //======== panelTrans1 ========
                    {
                        panelTrans1.setLayout(new FormLayout(
                            "95dlu, $lcgap, 86dlu, 3dlu, 41dlu, $lcgap, 100dlu",
                            "default, $nlgap, default, 1dlu, default, $ugap, 2*(default, $nlgap), default, 1dlu, default, $lgap, default"));

                        //---- lblTransType ----
                        lblTransType.setText("Transaction Type");
                        lblTransType.setFont(new Font("Tahoma", Font.BOLD, 11));
                        panelTrans1.add(lblTransType, CC.xy(1, 1, CC.LEFT, CC.BOTTOM));

                        //---- rdoTransJms ----
                        rdoTransJms.setText("JMS Transaction");
                        rdoTransJms.setSelected(true);
                        rdoTransJms.setToolTipText("Simple JMS transaction session.");
                        panelTrans1.add(rdoTransJms, CC.xy(1, 3));

                        //---- lblTransMsgNum ----
                        lblTransMsgNum.setText("Messages in a Transaction");
                        lblTransMsgNum.setLabelFor(spTransMsgNum);
                        panelTrans1.add(lblTransMsgNum, CC.xy(3, 3, CC.RIGHT, CC.DEFAULT));

                        //---- spTransMsgNum ----
                        spTransMsgNum.setModel(new SpinnerNumberModel(0, 0, null, 1));
                        spTransMsgNum.setToolTipText("(optional) The number of messages in a transaction.");
                        panelTrans1.add(spTransMsgNum, CC.xy(5, 3));

                        //---- rdoTransXa ----
                        rdoTransXa.setText("XA Transaction");
                        rdoTransXa.setToolTipText("XA Transaction which uses a XA Connection Factory.");
                        rdoTransXa.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                rdoTransXaItemStateChanged(e);
                            }
                        });
                        panelTrans1.add(rdoTransXa, CC.xy(1, 5));

                        //---- chkCommitOnExit ----
                        chkCommitOnExit.setText("Commit on Exit");
                        chkCommitOnExit.setToolTipText("Commit pending message transaction when program exits.");
                        panelTrans1.add(chkCommitOnExit, CC.xywh(2, 5, 2, 1, CC.LEFT, CC.DEFAULT));

                        //---- lblXaGroup ----
                        lblXaGroup.setText("XA Transaction Properties");
                        lblXaGroup.setFont(new Font("Tahoma", Font.BOLD, 11));
                        lblXaGroup.setHorizontalAlignment(SwingConstants.LEFT);
                        lblXaGroup.setEnabled(false);
                        panelTrans1.add(lblXaGroup, CC.xywh(1, 7, 3, 1, CC.LEFT, CC.DEFAULT));

                        //---- lblTransMgr ----
                        lblTransMgr.setText("Transaction Manager");
                        lblTransMgr.setEnabled(false);
                        panelTrans1.add(lblTransMgr, CC.xy(1, 9, CC.LEFT, CC.CENTER));

                        //---- lblTransJndi ----
                        lblTransJndi.setText("Transaction JNDI Name");
                        lblTransJndi.setLabelFor(txtTransJndi);
                        lblTransJndi.setEnabled(false);
                        panelTrans1.add(lblTransJndi, CC.xy(3, 9, CC.RIGHT, CC.DEFAULT));

                        //---- txtTransJndi ----
                        txtTransJndi.setEnabled(false);
                        txtTransJndi.setToolTipText("(optional) JTA Transaction Manager name in the JNDI server. (default: TransactionManager)");
                        panelTrans1.add(txtTransJndi, CC.xywh(5, 9, 3, 1));

                        //---- rdoTransMgrLocal ----
                        rdoTransMgrLocal.setText("Local");
                        rdoTransMgrLocal.setSelected(true);
                        rdoTransMgrLocal.setEnabled(false);
                        rdoTransMgrLocal.setToolTipText("XA Transaction Type 'Local' uses a local JBossTS transaction manager.");
                        panelTrans1.add(rdoTransMgrLocal, CC.xy(1, 11));

                        //---- lblTransTimeout ----
                        lblTransTimeout.setText("Transaction Timeout");
                        lblTransTimeout.setLabelFor(spTransTimeout);
                        lblTransTimeout.setEnabled(false);
                        panelTrans1.add(lblTransTimeout, CC.xy(3, 11, CC.RIGHT, CC.DEFAULT));

                        //---- spTransTimeout ----
                        spTransTimeout.setEnabled(false);
                        spTransTimeout.setModel(new SpinnerNumberModel(0, 0, null, 1));
                        spTransTimeout.setToolTipText("XA Transaction timeout in seconds. (0 indicates no timeout)");
                        panelTrans1.add(spTransTimeout, CC.xy(5, 11));

                        //---- lblSec3 ----
                        lblSec3.setText("sec");
                        lblSec3.setEnabled(false);
                        panelTrans1.add(lblSec3, CC.xy(7, 11));

                        //---- rdoTransMgrNoMgr ----
                        rdoTransMgrNoMgr.setText("No Manager");
                        rdoTransMgrNoMgr.setEnabled(false);
                        rdoTransMgrNoMgr.setToolTipText("XA Transaction Type 'No Manager' use a basic XA without a transaction manager.");
                        panelTrans1.add(rdoTransMgrNoMgr, CC.xy(1, 13));
                    }
                    panelTrans.add(panelTrans1, CC.xy(2, 2));
                }
                tabbedPane.addTab("Transaction Options", panelTrans);
                tabbedPane.setEnabledAt(6, false);

                //======== panelOther ========
                {
                    panelOther.setLayout(new FormLayout(
                        "$ugap, center:default:grow, $ugap",
                        "$ugap, top:default"));

                    //======== panelOther1 ========
                    {
                        panelOther1.setBorder(new TitledBorder("JmsStream Display Properties"));
                        panelOther1.setLayout(new FormLayout(
                            "default, $lcgap, right:default, $lcgap, 100dlu, $lcgap, 60dlu",
                            "$ugap, 3*($lgap, default), $rgap, $lgap, default, $ugap"));

                        //---- lblEncoding ----
                        lblEncoding.setText("Message Encoding");
                        lblEncoding.setLabelFor(cboEncoding);
                        lblEncoding.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelOther1.add(lblEncoding, CC.xy(3, 3));

                        //---- cboEncoding ----
                        cboEncoding.setToolTipText("Set message encoding.");
                        cboEncoding.setModel(new DefaultComboBoxModel(new String[] {
                            "Default",
                            "US-ASCII",
                            "ISO-8859-1",
                            "UTF-8",
                            "UTF-16BE",
                            "UTF-16LE",
                            "UTF-16"
                        }));
                        cboEncoding.setSelectedIndex(0);
                        cboEncoding.setMaximumRowCount(7);
                        panelOther1.add(cboEncoding, CC.xy(5, 3));

                        //---- lblDisplayBuffer ----
                        lblDisplayBuffer.setText("Display Buffer");
                        panelOther1.add(lblDisplayBuffer, CC.xy(3, 5));

                        //---- spDisplayBuffer ----
                        spDisplayBuffer.setModel(new SpinnerNumberModel(10000000, 500, null, 10000));
                        spDisplayBuffer.setToolTipText("JmsStream GUI Output Display Buffer Size");
                        panelOther1.add(spDisplayBuffer, CC.xy(5, 5));

                        //---- lblKb ----
                        lblKb.setText("Characters");
                        lblKb.setLabelFor(spDisplayBuffer);
                        panelOther1.add(lblKb, CC.xy(7, 5));

                        //---- lblDisplayRefresh ----
                        lblDisplayRefresh.setText("Display Refresh Rate");
                        lblDisplayRefresh.setLabelFor(spDisplayRefresh);
                        lblDisplayRefresh.setHorizontalAlignment(SwingConstants.TRAILING);
                        panelOther1.add(lblDisplayRefresh, CC.xy(3, 7));

                        //---- spDisplayRefresh ----
                        spDisplayRefresh.setModel(new SpinnerNumberModel(250, 50, null, 50));
                        spDisplayRefresh.setToolTipText("The display refresh rate for the output display window.");
                        panelOther1.add(spDisplayRefresh, CC.xy(5, 7));

                        //---- lblMilli ----
                        lblMilli.setText("msec");
                        lblMilli.setLabelFor(spDisplayRefresh);
                        panelOther1.add(lblMilli, CC.xy(7, 7));
                    }
                    panelOther.add(panelOther1, CC.xy(2, 2));
                }
                tabbedPane.addTab("Other Options", panelOther);
            }
            dialogPane.add(tabbedPane);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
                buttonBar.setLayout(new FormLayout(
                    "$lcgap, default, $glue, $button, $rgap, $button",
                    "pref"));

                //---- lblInfo ----
                lblInfo.setText("* Required Field");
                lblInfo.setFont(new Font("Tahoma", Font.BOLD, 11));
                lblInfo.setHorizontalAlignment(SwingConstants.LEFT);
                buttonBar.add(lblInfo, CC.xy(2, 1, CC.LEFT, CC.DEFAULT));

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okButtonActionPerformed();
                    }
                });
                buttonBar.add(okButton, CC.xy(4, 1));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButtonActionPerformed();
                    }
                });
                buttonBar.add(cancelButton, CC.xy(6, 1));
            }
            dialogPane.add(buttonBar);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        //---- btngrpType ----
        ButtonGroup btngrpType = new ButtonGroup();
        btngrpType.add(rdoQueue);
        btngrpType.add(rdoTopic);
        btngrpType.add(rdoGeneric);

        //---- btngrpDest ----
        ButtonGroup btngrpDest = new ButtonGroup();
        btngrpDest.add(rdoListen);
        btngrpDest.add(rdoSend);
        btngrpDest.add(rdoReqRep);

        //---- btngrpTransType ----
        ButtonGroup btngrpTransType = new ButtonGroup();
        btngrpTransType.add(rdoTransJms);
        btngrpTransType.add(rdoTransXa);

        //---- btngrpTransMgr ----
        ButtonGroup btngrpTransMgr = new ButtonGroup();
        btngrpTransMgr.add(rdoTransMgrLocal);
        btngrpTransMgr.add(rdoTransMgrNoMgr);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        _dtmJndiProps = (DefaultTableModel)(tblJndiProperties.getModel());

        this.getParent().setCursor(Cursor.getDefaultCursor());

        if (ConnectionHelper.wasSslConnected()) disableJavaSslInput();
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JTabbedPane tabbedPane;
    private JPanel panelConnection;
    private JPanel panelCon1;
    private JLabel lblJmsServerType;
    private JComboBox cboJmsClientType;
    private JButton btnTestConnect;
    private JLabel lblConFactory;
    private JTextField txtConFactory;
    private JLabel lblDestinationType;
    private JRadioButton rdoQueue;
    private JRadioButton rdoTopic;
    private JRadioButton rdoGeneric;
    private JCheckBox chkSsl;
    private JCheckBox chkTransactional;
    private JLabel lblOperation;
    private JRadioButton rdoListen;
    private JRadioButton rdoSend;
    private JRadioButton rdoReqRep;
    private JCheckBox chkAsyncReply;
    private JLabel lblReplyTimeout;
    private JSpinner spReplyTimeout;
    private JLabel lblMiliSec;
    private JLabel lblJndiConFactory;
    private JTextField txtJndiContextFactory;
    private JCheckBox chkUseFileJNDI;
    private JLabel lblProviderUrl;
    private JTextField txtProviderUrl;
    private JPanel paneCon2;
    private JCheckBox chkJndiUserPwd;
    private JLabel lblClientId;
    private JTextField txtClientId;
    private JLabel lblUser;
    private JTextField txtUser;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JLabel lblJndiUser;
    private JTextField txtJndiUser;
    private JLabel lblJndiPassword;
    private JPasswordField txtJndiPassword;
    private JLabel lblJmsUser;
    private JTextField txtJmsUser;
    private JLabel lblJmsPassword;
    private JPasswordField txtJmsPassword;
    private JPanel panelJNDI;
    private JPanel panelProps;
    private JPanel pnlTableButtons;
    private JButton btnAddRow;
    private JButton btnRemoveRow;
    private JScrollPane scrJndiProperties;
    private JTable tblJndiProperties;
    private JPanel panelInputOutput;
    private JPanel panelIO1;
    private JPanel pnlJmsStreamFile;
    private JButton btnNewMsgFile;
    private JButton btnEditMsgFile;
    private JButton btnOpenMsgFile;
    private JTextField txtJmsStreamFile;
    private JCheckBox chkFileAppend;
    private JLabel lblReplyFile;
    private JTextField txtReplyFile;
    private JButton btnReplyFile;
    private JCheckBox chkVerbose;
    private JCheckBox chkZip;
    private JCheckBox chkNoEcho;
    private JLabel lblZipEntry;
    private JScrollPane scrZipEntry;
    private JList lstZipEntry;
    private JButton btnAddZipEntry;
    private JPanel panelStats;
    private JLabel lblStats;
    private JSpinner spStats;
    private JLabel lblSec2;
    private JButton btnRemoveZipEntry;
    private JPanel panelListener;
    private JPanel panelListener1;
    private JLabel lblListenDest;
    private JTextField txtListenDest;
    private JLabel lblSelector;
    private JTextField txtSelector;
    private JLabel lblStopAfter;
    private JSpinner spStopAfter;
    private JLabel lblAckMode;
    private JComboBox cboAckMode;
    private JCheckBox chkBrowseQueue;
    private JLabel lblRcvTimestamp;
    private JTextField txtRcvTimestamp;
    private JCheckBox chkNoConfirm;
    private JLabel lblCsvSaveFile;
    private JTextField txtCsvSaveFile;
    private JButton btnCsvSaveFile;
    private JCheckBox chkTimed;
    private JLabel lblZipMessageNum;
    private JSpinner spZipMessageNum;
    private JCheckBox chkEchoCsv;
    private JCheckBox chkDurable;
    private JCheckBox chkEchoXml;
    private JLabel lblDurableName;
    private JTextField txtDurableName;
    private JCheckBox chkEchoRaw;
    private JCheckBox chkUnsubscribe;
    private JCheckBox chkExtractMonMsg;
    private JPanel panelSend;
    private JPanel panelSend1;
    private JLabel lblSendDest;
    private JTextField txtSendDest;
    private JCheckBox chkCompress;
    private JLabel lblDeliveryMode;
    private JComboBox cboDeliveryMode;
    private JLabel lblSpeed;
    private JSpinner spSpeed;
    private JLabel lblFileLoop;
    private JSpinner spFileLoop;
    private JLabel lblSndTimestamp;
    private JTextField txtSndTimestamp;
    private JLabel lblSequence;
    private JTextField txtSequence;
    private JLabel lblRate;
    private JSpinner spRate;
    private JLabel lblMsgSec;
    private JCheckBox chkVariableMsgRate;
    private JCheckBox chkStartingRate;
    private JLabel lblMaxRate;
    private JSpinner spMaxRate;
    private JLabel lblMsgSec3;
    private JLabel lblStartRate;
    private JSpinner spStartRate;
    private JLabel lblMsgSec2;
    private JLabel lblNumberOfIntervals;
    private JSpinner spNumberOfIntervals;
    private JLabel lblIntervalSize;
    private JSpinner spIntervalSize;
    private JLabel lblRatestamp;
    private JTextField txtRatestamp;
    private JPanel panelSsl;
    private JTabbedPane tabbedPaneSsl;
    private JPanel panelTibcoSsl;
    private JPanel panel1;
    private JLabel lblSslVendor;
    private JComboBox cboSslVendor;
    private JTextPane txtSslLimitaionWarning2;
    private JLabel lblSslCiphers;
    private JTextField txtSslCiphers;
    private JCheckBox chkSslJndi;
    private JLabel lblSslHostName;
    private JTextField txtSslHostName;
    private JCheckBox chkSslAuthOnly;
    private JLabel lblSslIdentity;
    private JTextField txtSslIdentity;
    private JButton btnSslIdentityFile;
    private JCheckBox chkVerifyHostName;
    private JLabel lblSslPassword;
    private JPasswordField txtSslPassword;
    private JCheckBox chkVerifyHost;
    private JLabel lblSslKey;
    private JTextField txtSslKey;
    private JButton btnSslKeyFile;
    private JLabel lblSslTrusted;
    private JScrollPane scrSslTrusted;
    private JList lstSslTrusted;
    private JCheckBox chkSslTrace;
    private JButton btnAddTrustedCert;
    private JCheckBox chkSslDebugTrace;
    private JButton btnRemoveTrustedCert;
    private JPanel panelJavaSsl;
    private JCheckBox chkTrustAllCerts;
    private JTextPane txtSslLimitaionWarning;
    private JLabel lblKeyStoreType;
    private JComboBox cboKeyStoreType;
    private JLabel lblKeyStore;
    private JTextField txtKeyStore;
    private JButton btnKeyStoreFile;
    private JLabel lblKeyStorePassword;
    private JPasswordField txtKeyStorePassword;
    private JLabel lblTrustStoreType;
    private JComboBox cboTrustStoreType;
    private JLabel lblTrustStore;
    private JTextField txtTrustStore;
    private JButton btnTrustStoreFile;
    private JLabel lblTrustStorePassword;
    private JPasswordField txtTrustStorePassword;
    private JLabel lblSslDebugParam;
    private JTextField txtSslDebugParams;
    private JPanel panelTrans;
    private JPanel panelTrans1;
    private JLabel lblTransType;
    private JRadioButton rdoTransJms;
    private JLabel lblTransMsgNum;
    private JSpinner spTransMsgNum;
    private JRadioButton rdoTransXa;
    private JCheckBox chkCommitOnExit;
    private JLabel lblXaGroup;
    private JLabel lblTransMgr;
    private JLabel lblTransJndi;
    private JTextField txtTransJndi;
    private JRadioButton rdoTransMgrLocal;
    private JLabel lblTransTimeout;
    private JSpinner spTransTimeout;
    private JLabel lblSec3;
    private JRadioButton rdoTransMgrNoMgr;
    private JPanel panelOther;
    private JPanel panelOther1;
    private JLabel lblEncoding;
    private JComboBox cboEncoding;
    private JLabel lblDisplayBuffer;
    private JSpinner spDisplayBuffer;
    private JLabel lblKb;
    private JLabel lblDisplayRefresh;
    private JSpinner spDisplayRefresh;
    private JLabel lblMilli;
    private JPanel buttonBar;
    private JLabel lblInfo;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private boolean _blnOK = false;
    private Hashtable _env = null;
    private final ConnectionHelper _conHelper = new ConnectionHelper();
    private final Font _fntNormal= new Font("Tahoma", Font.PLAIN, 11);
    private final Font _fntRequired = new Font("Tahoma", Font.BOLD, 11);
    private DefaultTableModel _dtmJndiProps;
    private DefaultListModel dlmZipEntry;
    private DefaultListModel dlmSslTrusted;
}
