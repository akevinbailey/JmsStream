/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Thu Jun 08 17:41:57 CEST 2006
 */

package com.tibco.util.gui;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.util.JmsStream;
import com.tibco.util.JmsStreamListener;
import com.tibco.util.gui.forms.JmsStreamAbout;
import com.tibco.util.gui.forms.JmsStreamConfig;
import com.tibco.util.gui.forms.JmsStreamFileJndiConfig;
import com.tibco.util.gui.forms.JmsStreamLicense;
import com.tibco.util.gui.forms.msgedit.JmsStreamMsgEdit;
import com.tibco.util.gui.helper.*;
import com.tibco.util.jmshelper.ConnectionHelper;
import com.tibco.util.jmshelper.FormatHelper;

import javax.naming.Context;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Title:        JmsStreamForm<p>
 * Description:  This is the main form for the JmsStreamGUI application.<p>
 * @author A. Kevin Bailey
 * @version 2.5.3
 */
@SuppressWarnings({"FieldCanBeLocal", "unchecked", "WeakerAccess", "CanBeFinal"})
public class JmsStreamForm extends JFrame {
    private boolean _blnConfigFileDirty = false;
    private TextAreaOutputStream _taosTextArea;
    private Hashtable _env;
    private Thread _thrJmsStream = null;
    private JmsStream _runJmsStream = null;
    private Timer _timWatchThread = null;
    private Timer _timWatchMem;
    private Timer _timUpdateText;
    private String _strSaveConfigURI = "";
    private String _strMsgFileURI = "";
    private String _strEncoding = "UTF-8";

    public JmsStreamForm()
    {
        _env = new Hashtable();
        initComponents();

        _timWatchThread = new Timer(2000, new AliveCheckListener(this)); // Repeat Timer every 2 sec.
        _timWatchThread.setRepeats(true);

        _timUpdateText = new Timer(1000, new TextUpdateListener(_taosTextArea)); // Repeat Timer every 1 sec.
        _timUpdateText.setRepeats(true);
        _timUpdateText.start();

        _timWatchMem = new Timer(5000, new MemCheckListener(this)); // Repeat Timer every 5 sec.
        _timWatchMem.setRepeats(true);
        _timWatchMem.start();
        updateMemDisplay();
    }

    // Check if file is dirty.
    // If so get user to make a "Save? yes/no/cancel" decision.
    // **** Not used in this version ****
    private boolean okToAbandon()
    {
        if (!_blnConfigFileDirty) {
            return true;
        }
        int value =  JOptionPane.showConfirmDialog(this, "Save changes?", "JmsStream Configuration",
                                                   JOptionPane.YES_NO_CANCEL_OPTION) ;
        switch (value) {
            case JOptionPane.YES_OPTION:
                // yes, please save changes
                btnConfigSaveActionPerformed();
                return true;
            case JOptionPane.NO_OPTION:
                // no, abandon edits
                // i.e. return true without saving
                return true;
            case JOptionPane.CANCEL_OPTION:
            default:
                // cancel
                return false;
        }
    }

    private void showConfigButtons()
    {
        mnuShowConfig.setEnabled(true);
        mnuConfiguration.setEnabled(true);
        mnuSaveConfigFile.setEnabled(true);
        mnuSaveConfigAs.setEnabled(true);
        mnuGenCommandLine.setEnabled(true);
        btnShowConf.setEnabled(true);
        btnConfigEdit.setEnabled(true);
        btnStart.setEnabled(true);
        btnGenCommandLine.setEnabled(true);
        btnSaveConfig.setEnabled(false);
    }

    private void showMsgButtons()
    {
        mnuMsgEdit.setEnabled(true);
    }

    public void updateMemDisplay()
    {
        long intMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        lblMemory.setText((intMem < 1048576 ? Math.round(intMem / 1024) + "KB" : Math.round(intMem / 1040512)
                          + "MB") + " of " + Math.round(Runtime.getRuntime().totalMemory()/1040512) + "MB");
    }

    public void stopThread()
    {
        _timWatchThread.stop();
        refreshText();
        btnStopActionPerformed();
    }

    public boolean isThreadAlive()
    {
        return _thrJmsStream != null && _thrJmsStream.isAlive();
    }

    private void btnNewConfigActionPerformed() {
        JmsStreamConfig dlg = new JmsStreamConfig(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setVisible(true);
        if (dlg.isOK()) {
            _env = dlg.getValues();
            // Change the Form title
            this.setTitle(JmsStream.APP_NAME);
            showConfigButtons();
            btnSaveConfig.setEnabled(true);
        }
    }

    private void btnOpenConfigActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CONFIG));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            // Call openFile to attempt to load the JmsStream configuration
            try {
                _env = FormatHelper.getPropertiesFile(dlgFileChooser.getSelectedFile().getPath());
                _strSaveConfigURI = dlgFileChooser.getSelectedFile().toString();

                if (_env.containsKey("guiDisplayRefresh") && !_env.get("guiDisplayRefresh").equals("")) {
                    _env.put("guiDisplayRefresh", new Integer(_env.get("guiDisplayRefresh").toString()));
                    _timUpdateText.stop();
                    _timUpdateText.setInitialDelay((Integer)_env.get("guiDisplayRefresh"));
                    _timUpdateText.setDelay((Integer)_env.get("guiDisplayRefresh"));
                    _timUpdateText.start();
                }
                if (_env.containsKey("guiDisplayBuffer") && !_env.get("guiDisplayBuffer").equals("")) {
                    _env.put("guiDisplayBuffer", new Integer(_env.get("guiDisplayBuffer").toString()));
                    _taosTextArea.setMaxCharLength((Integer)_env.get("guiDisplayBuffer"));
                }

                if (_env.containsKey("encoding")) _strEncoding = _env.get("encoding").toString();

                showConfigButtons();
                if (_env.containsKey("file") && !_env.get("file").toString().equals("")) {
                    showMsgButtons();
                }
                // Change the Form title.
                this.setTitle(JmsStream.APP_NAME + " - " + dlgFileChooser.getSelectedFile().getName());
                // Alert user the configuration loaded.
                System.out.println("Configuration file \"" + dlgFileChooser.getSelectedFile().getName() + "\" is now loaded.");
            }
            catch (java.io.IOException ioe) {
                JOptionPane.showMessageDialog(this, ioe.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                ioe.printStackTrace();
            }
        }
        this.repaint();
    }

    private void mnuNewConfigActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        btnNewConfigActionPerformed();
    }

    private void mnuOpenConfigActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        btnOpenConfigActionPerformed();
    }

    private void mnuNewMessagesActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        JFileChooser dlgFileChooser = new JFileChooser();
        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.MSG));
        // Use the custom version of the dialog, test return for Approve/Cancel
        dlgFileChooser.setDialogTitle("New Message File");
        dlgFileChooser.setApproveButtonText("Create File");
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showDialog(this, null)) {
            _strMsgFileURI = dlgFileChooser.getSelectedFile().getPath();
            try {
                java.io.File fileNew = new java.io.File(_strMsgFileURI);
                //noinspection StatementWithEmptyBody
                if (fileNew.createNewFile()) {/* Do nothing. */}
            }
            catch (IOException ioe){
                JOptionPane.showMessageDialog(this,
                        "Error:  " + ioe.getMessage(),
                        "JmsStream Message File Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            mnuMsgEditActionPerformed();
            //showMsgButtons();
        }
        else { // Need to set the default cursor because the Message Edit Dialog set the cursor wait.
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void mnuOpenMessagesActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        JFileChooser dlgFileChooser = new JFileChooser();
        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.MSG));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showOpenDialog(this)) {
            _strMsgFileURI = dlgFileChooser.getSelectedFile().getPath();
            mnuMsgEditActionPerformed();
        }
        else { // Need to set the default cursor because the Message Edit Dialog set the cursor wait.
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void mnuSaveConfigFileActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        btnConfigSaveActionPerformed();
    }

    private void mnuExitActionPerformed() {
        if (okToAbandon()) {
           System.exit(0);
        }
    }

    private void btnConfigSaveActionPerformed() {
        // If the file as not be saved previously then prompt for a new file.
        if (_strSaveConfigURI.equals("")) mnuSaveConfigAsActionPerformed();
        if (_strSaveConfigURI.equals("")) return;

        java.io.FileOutputStream outFileStream;
        String strOut;
        java.util.Iterator iteEnv = _env.entrySet().iterator();
        java.util.Map.Entry entryEnv;

        try {
            outFileStream = new java.io.FileOutputStream(_strSaveConfigURI);
            strOut = "########################################################################\n" +
                     "#       Copyright (c) 2010 TIBCO Software Inc.\n" +
                     "#       All Rights Reserved.\n" +
                     "#       For more information, please contact:\n" +
                     "#       TIBCO Software Inc.\n" +
                     "#       Palo Alto, California, USA\n" +
                     "#\n" +
                     "#       JmsStream Configuration File version B\n" +
                     "########################################################################\n";
            outFileStream.write(strOut.getBytes("UTF-8"));

            // Write all of the properties in _env to the text file
            while (iteEnv.hasNext()) {
                entryEnv = (Map.Entry)iteEnv.next();
                strOut = "\n" + entryEnv.getKey() + " = " + FormatHelper.fixEscapeChars(entryEnv.getValue().toString());
                outFileStream.write(strOut.getBytes("UTF-8"));
            }
            outFileStream.close();
            btnSaveConfig.setEnabled(false);
        }
        catch (java.io.IOException ioe) {
            JOptionPane.showMessageDialog(this, ioe.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            ioe.printStackTrace();
        }
    }

    private void mnuSaveConfigAsActionPerformed() {
        JFileChooser dlgFileChooser = new JFileChooser();

        dlgFileChooser.setCurrentDirectory(FileNameFilter.getUserDir());
        dlgFileChooser.setFileFilter(new FileNameFilter(FileNameFilter.CONFIG));
        // Use the OPEN version of the dialog, test return for Approve/Cancel
        if (JFileChooser.APPROVE_OPTION == dlgFileChooser.showSaveDialog(this)) {
            _strSaveConfigURI = dlgFileChooser.getSelectedFile().toString();
            // Change the Form title
            this.setTitle(JmsStream.APP_NAME + " - " + dlgFileChooser.getSelectedFile().getName());
            // Call the save file function.
            btnConfigSaveActionPerformed();
        }

        btnSaveConfig.setEnabled(false);
        this.repaint();
    }

    private void mnuLicenseActionPerformed() {
        JmsStreamLicense dlg = new JmsStreamLicense(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    private void mnuAboutActionPerformed() {
        JmsStreamAbout dlg = new JmsStreamAbout(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    private void btnConfigEditActionPerformed() {
        JmsStreamConfig dlg = new JmsStreamConfig(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setValues(_env);
        dlg.setVisible(true);
        if (dlg.isOK()) {
            _env = dlg.getValues();
            _taosTextArea.setMaxCharLength((Integer)_env.get("guiDisplayBuffer"));
            _timUpdateText.stop();
            _timUpdateText.setInitialDelay((Integer)_env.get("guiDisplayRefresh"));
            _timUpdateText.setDelay((Integer)_env.get("guiDisplayRefresh"));
            _timUpdateText.start();
            btnSaveConfig.setEnabled(true);
        }
    }

    private void mnuConfigurationActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        btnConfigEditActionPerformed();
    }

    private void btnShowConfActionPerformed() {
        _env.put("showconfig", Boolean.TRUE);
        JmsStream jmsApp = new JmsStream(_env, true);
        // Start Thread
        Thread thread = new Thread(jmsApp);
        thread.start();
        try {
            // Wait for the thread to finish.
            thread.join();
        }
        catch (InterruptedException ie) {
            System.err.println(" JmsStream GUI interrupted.");
        }

        // Remove the setting so it will not run the show config again
        _env.remove("showconfig");

        refreshText();
        btnStopActionPerformed();
    }

    private void mnuShowConfigActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        btnShowConfActionPerformed();
    }

    private void brnClearScreenActionPerformed() {
        try {
            txtOutput.getDocument().remove(0, txtOutput.getDocument().getLength());
            txtOutput.setText("");
            refreshText();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void btnStartActionPerformed() {
        _runJmsStream = new JmsStream(_env, false);
        // Clear static counter data.
        JmsStreamListener.clearCount();

        // Start Thread
        _thrJmsStream = new Thread(_runJmsStream, "JmsSteam_Main");
        _thrJmsStream.start();

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);

        // Check every 2 sec to see if thread is still active.
        _timWatchThread.start();
    }

    private void btnStopActionPerformed() {
        if (_runJmsStream != null && _thrJmsStream != null) {
            _runJmsStream.stopThread();
            _thrJmsStream.interrupt();
            if (_thrJmsStream.isAlive()) {
                try {
                    _thrJmsStream.join();
                }
                catch (InterruptedException ie) {
                    System.err.println("JmsStream thread terminated.");
                }
            }
        }

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    private void refreshText()
    {
        // Scroll to end of txtOutput
        //Rectangle recView = txtOutput.getVisibleRect();
        //recView.y = txtOutput.getHeight() - recView.height;
        //recView.x = 0;
        //txtOutput.scrollRectToVisible(recView);

        // Find the max line width and set the JTextArea Columns to get the horizontal scroll-bars to display properly.
        int intMaxWidth = 0;
        int intWidth;
        Element eleTemp = txtOutput.getDocument().getDefaultRootElement();
        // Loop through each line to get its length.
        for (int i=0; i < eleTemp.getElementCount(); i++) {
            intWidth = eleTemp.getElement(i).getEndOffset() - eleTemp.getElement(i).getStartOffset();
            if (intMaxWidth < intWidth) intMaxWidth = intWidth;
        }

        txtOutput.setColumns(intMaxWidth);
    }

    @SuppressWarnings({"CallToSystemGC"})
    private void btnGcActionPerformed() {
        // Garbage collector
        Runtime.getRuntime().gc();
        JOptionPane.showMessageDialog(this, "Virtual machine has made its best effort to recycle all discarded objects.", "JmsStream GUI",
                                      JOptionPane.INFORMATION_MESSAGE);
        updateMemDisplay();
    }

    private void mnuMsgEditActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        JmsStreamMsgEdit dlg = null;
        String strFileURI;

        if (_env.containsKey("zip") && _env.get("zip").equals(Boolean.TRUE)) {
            JOptionPane.showMessageDialog(
                    this,
                    "JmsStream cannot edit messages in a ZIP file.",
                    "JmsStream Message File",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (_strMsgFileURI != null && !_strMsgFileURI.equals("")) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.txtOutput.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Must set the txtOutput cursor separately.
            dlg = new JmsStreamMsgEdit(this, _strMsgFileURI, _strEncoding);
        }
        else {
            if (_env.containsKey("file") && _env.get("file").toString() != null && !_env.get("file").equals("")) {
                strFileURI = _env.get("file").toString();
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                this.txtOutput.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Must set the txtOutput cursor separately.
                dlg = new JmsStreamMsgEdit(this, strFileURI, _strEncoding);
            }
            else {
                JOptionPane.showMessageDialog(this, "File could not be found.", "JmsStream GUI", JOptionPane.ERROR_MESSAGE);

            }
        }

        if (dlg != null) {
            if (dlg.isCanceled()) {
                dlg.dispose();
            }
            else {
                dlg.setModal(true);
                dlg.setVisible(true);
            }
        }
        _strMsgFileURI = ""; // Clear message file name, because we don't need it anymore.
        this.txtOutput.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)); // Must set the txtOutput cursor separately.
        this.setCursor(Cursor.getDefaultCursor());
    }

    private void btnHelpActionPerformed() {
        JmsStream.usage();
    }

    private void mnuShowHelpActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        btnHelpActionPerformed();
    }

    private void btnGenCommandLineActionPerformed() {
        System.out.println("\n" + genCommandLine(_env) + "\n");
        refreshText();
    }

    private void mnuGenCommandLineActionPerformed() {
        menuBar.doLayout();
        this.repaint();
        btnGenCommandLineActionPerformed();
    }

    public static String genCommandLine(Hashtable env)
    {
        String strCommand = "java -cp JmsStream.jar com.tibco.util.JmsStream";
        boolean blnJndi = false;
        ConnectionHelper conHelper = new ConnectionHelper();

        try {
            // ---- Connection Options ----
            if (env.containsKey("jmsclient")) {
                strCommand += " -jmsclient " + env.get("jmsclient");
                conHelper.setJmsClientType(env.get("jmsclient").toString());
            }
            if (env.containsKey(Context.INITIAL_CONTEXT_FACTORY) && !env.get(Context.INITIAL_CONTEXT_FACTORY).equals(conHelper.getInitialContextFactory())) {
                strCommand += " -jndicontextfactory " + env.get(Context.INITIAL_CONTEXT_FACTORY).toString();
            }
            if (env.containsKey("connectionfactory")) {
                if (!env.get("connectionfactory").toString().equals(conHelper.getDefaultQueueFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultTopicFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultGenericFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultSslQueueFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultSslTopicFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultSslGenericFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultXATopicFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultXATopicFactory())
                        && !env.get("connectionfactory").toString().equals(conHelper.getDefaultXAGenericFactory()))
                    strCommand += " -connectionfactory " + env.get("connectionfactory").toString();
            }
            if (env.containsKey(Context.PROVIDER_URL) && !env.get(Context.PROVIDER_URL).equals("tibjmsnaming://localhost:7222")) {
                strCommand += " -providerurl " + env.get(Context.PROVIDER_URL).toString();
            }
            if (env.containsKey("isSeparateUsrPwd") && env.get("isSeparateUsrPwd").equals(Boolean.TRUE)) {
                blnJndi = true;
            }
            if (env.containsKey("user")) {
                if (blnJndi)
                    strCommand += " -jmsuser " + env.get("user").toString();
                else
                    strCommand += " -user " + env.get(Context.SECURITY_PRINCIPAL).toString();
            }
            if (env.containsKey(Context.SECURITY_PRINCIPAL) && blnJndi) {
                strCommand += " -jndiuser " + env.get(Context.SECURITY_PRINCIPAL).toString();
            }
            if (env.containsKey("password")) {
                if (blnJndi)
                    strCommand += " -jmspassword " + env.get("password").toString();
                else
                    strCommand += " -password " + env.get(Context.SECURITY_CREDENTIALS).toString();
            }
            if (env.containsKey(Context.SECURITY_CREDENTIALS) && blnJndi) {
                strCommand += " -jndipassword " + env.get(Context.SECURITY_PRINCIPAL).toString();
            }

            if (env.containsKey("isListener") && env.get("isListener").equals(Boolean.TRUE)) {
                strCommand += " -listen " + env.get("listendest");
            }
            else if (env.containsKey("isListener") && env.get("isListener").equals(Boolean.FALSE)) {
                if (env.containsKey("senddest") && !env.get("senddest").equals(""))
                    strCommand += " -send " + env.get("senddest");
                else
                    strCommand += " -send";
            }
            else if ((env.containsKey("isListener") && env.get("isListener").equals(Boolean.FALSE)) &&
                     (env.containsKey("requestreply") && env.get("requestreply").equals(Boolean.TRUE))) {
                if (env.containsKey("senddest") && !env.get("senddest").equals(""))
                    strCommand += " -requestreply " + env.get("senddest");
                else
                    strCommand += " -requestreply";
            }
            if (env.containsKey("replytimeout")) {
                strCommand += " -replytimeout " + env.get("replytimeout");
            }
            if (env.containsKey("asyncreply") && env.get("asyncreply").equals(Boolean.TRUE)) {
                strCommand += " -asyncreply";
            }
            if (env.containsKey("type") && env.get("type").equals("queue")) {
                strCommand += " -queue";
            }
            else if (env.containsKey("type") && env.get("type").equals("topic")) {
                strCommand += " -topic";
            }
            else if (env.containsKey("type") && env.get("type").equals("generic")) {
                strCommand += " -generic";
            }
            if (env.containsKey("clientid")) {
                strCommand += " -clientid " + env.get("clientid");
            }

            // ---- Input/Output Options ----
            if (env.containsKey("file")) {
                strCommand += " -file \"" + env.get("file") + "\"";
            }
            if (env.containsKey("zip") && env.get("zip").equals(Boolean.TRUE)) {
                if (env.containsKey("zipentries")) {
                    Vector vec = (Vector)env.get("zipentries");
                    strCommand += " -zip ";
                    for (Object aVec : vec) strCommand += " \"" + aVec + "\"";
                }
                if (env.containsKey("zipmsgperentry"))
                    strCommand += " -zipmsgperentry " + env.get("zipmsgperentry");
                else
                    strCommand += " -zip";
            }
            if (env.containsKey("fileappend") && env.get("fileappend").equals(Boolean.TRUE)) {
                strCommand += " -fileappend";
            }
            if (env.containsKey("replyfile")) {
                strCommand += " -replyfile \"" + env.get("replyfile") + "\"";
            }
            if (env.containsKey("stats")) {
                strCommand += " -stats " + env.get("stats");
            }
            if (env.containsKey("verbose") && env.get("verbose").equals(Boolean.TRUE)) {
                strCommand += " -verbose";
            }
            if (env.containsKey("noecho") && env.get("noecho").equals(Boolean.TRUE)) {
                strCommand += " -noecho";
            }
            if (env.containsKey("stopafter")) {
                strCommand += " -stopafter " + env.get("stopafter");
            }

            // ---- Custom JNDI Properties ----
            // TODO: Generate custom JNDI Properties output
            
            if ((env.containsKey("isListener") && env.get("isListener").equals(Boolean.TRUE))
                    || (env.containsKey("requestreply") && env.get("requestreply").equals(Boolean.TRUE))) {
                // ---- Listener Options ----
                if (env.containsKey("ackmode") && !env.get("ackmode").equals("AUTO_ACKNOWLEDGE")) {
                    strCommand += " -ackmode " + env.get("ackmode");
                }
                if (env.containsKey("noconfirm") && env.get("noconfirm").equals(Boolean.TRUE)) {
                    strCommand += " -noconfirm";
                }
                if (env.containsKey("selector")) {
                    strCommand += " -selector " + env.get("selector");
                }
                if (env.containsKey("durablename")) {
                    strCommand += " -durable " + env.get("durablename");
                    if (env.containsKey("unsubscribe") && env.get("unsubscribe").equals(Boolean.TRUE))
                        strCommand += " -unsubscribe";
                }
                if (env.containsKey("browse") && env.get("browse").equals(Boolean.TRUE)) {
                    strCommand += " -browse";
                }
                if (env.containsKey("timed") && env.get("timed").equals(Boolean.TRUE)) {
                    strCommand += " -timed";
                }
                if (env.containsKey("rcvtimestamp")) {
                    strCommand += " -rcvtimestamp \"" + env.get("rcvtimestamp") + "\"";
                }
                if (env.containsKey("echoxml") && env.get("echoxml").equals(Boolean.TRUE)) {
                    strCommand += " -echoxml";
                }
                if (env.containsKey("echocsv") && env.get("echocsv").equals(Boolean.TRUE)) {
                    strCommand += " -echocsv";
                }
                if (env.containsKey("raw") && env.get("raw").equals(Boolean.TRUE)) {
                    strCommand += " -raw";
                }
                if (env.containsKey("csvfile")) {
                    strCommand += " -csvfile \"" + env.get("csvfile") + "\"";
                }
            }
            if ((env.containsKey("isListener") && env.get("isListener").equals(Boolean.FALSE))
                    || (env.containsKey("requestreply") && env.get("requestreply").equals(Boolean.TRUE))) {
                // ---- Sender Options ----
                if (env.containsKey("deliverymode")) {
                    strCommand += " -deliverymode " + env.get("deliverymode");
                }
                if (env.containsKey("compress") && env.get("compress").equals(Boolean.TRUE)) {
                    strCommand += " -compress";
                }
                if (env.containsKey("speed")) {
                    strCommand += " -speed " + env.get("speed");
                }
                if (env.containsKey("fileloop")) {
                    strCommand += " -fileloop " + env.get("fileloop");
                }
                if (env.containsKey("sndtimestamp")) {
                    strCommand += " -sndtimestamp " + env.get("sndtimestamp");
                }
                if (env.containsKey("sequence")) {
                    strCommand += " -sequence " + env.get("sequence");
                }
                if (env.containsKey("rate"))
                    strCommand += " -rate " + env.get("rate");
                if (env.containsKey("maxrate") && env.containsKey("rate") && (Float)env.get("rate") > 0)
                    strCommand += " -startrate " + env.get("rate");
                if (env.containsKey("maxrate"))
                    strCommand += " -maxrate " + env.get("maxrate");
                if (env.containsKey("numberofintervals"))
                    strCommand += " -numberofintervals " + env.get("numberofintervals");
                if (env.containsKey("intervalsize"))
                    strCommand += " -intervalsize " + env.get("intervalsize");
                if (env.containsKey("ratestamp"))
                    strCommand += " -ratestamp " + env.get("ratestamp");
            }

            // ---- SSL Options ----
            if (env.containsKey("ssl") && env.get("ssl").equals(Boolean.TRUE)) {
                strCommand += " -ssl";
            }
            if (env.containsKey(conHelper.getSecurityProtocol()) && env.get(conHelper.getSecurityProtocol()).equals("ssl")) {
                // Specify ssl as the security protocol to use by the Initial Context
                strCommand += " -ssl_jndi";
            }
            if (env.containsKey(conHelper.getSslAuthOnly()) && env.get(conHelper.getSslAuthOnly()).equals(Boolean.TRUE)) {
                strCommand += " -ssl_auth_only";
            }
            if (env.containsKey(conHelper.getSslVendor()) && !env.get(conHelper.getSslVendor()).equals("j2se")) {
                strCommand += " -ssl_vendor " + env.get(conHelper.getSslVendor());
            }
            if (env.containsKey(conHelper.getSslCipherSuites())) {
                strCommand += " -ssl_ciphers " + env.get(conHelper.getSslCipherSuites());
            }
            if (env.containsKey(conHelper.getSslTrace()) && env.get(conHelper.getSslTrace()).equals(Boolean.TRUE)) {
                strCommand += " -ssl_trace";
            }
            if (env.containsKey(conHelper.getSslDebugTrace()) && env.get(conHelper.getSslDebugTrace()).equals(Boolean.TRUE)) {
                strCommand += " -ssl_debug_trace";
            }
            if (env.containsKey(conHelper.getSslTrustedCertificates())) {
                Vector vec = (Vector)env.get(conHelper.getSslTrustedCertificates());
                strCommand += " -ssl_trusted ";
                for (Object aVec : vec) strCommand += " \"" + aVec + "\"";
            }
            if (env.containsKey(conHelper.getSslExpectedHostName())) {
                strCommand += " -ssl_hostname " + env.get(conHelper.getSslExpectedHostName());
            }
            if (env.containsKey(conHelper.getSslIdentity())) {
                strCommand += " -ssl_identity " + env.get(conHelper.getSslIdentity());
            }
            if (env.containsKey(conHelper.getSslPassword())) {
                strCommand += " -ssl_password " + env.get(conHelper.getSslPassword());
            }
            if (env.containsKey(conHelper.getSslPrivateKey())) {
                strCommand += " -ssl_key " + env.get(conHelper.getSslPrivateKey());
            }
            if (env.containsKey(conHelper.getSslEnableVerifyHostName()) && env.get(conHelper.getSslEnableVerifyHostName()).equals(Boolean.TRUE)) {
                strCommand += " -verify_host_name";
            }
            if (env.containsKey(conHelper.getSslEnableVerifyHost()) && env.get(conHelper.getSslEnableVerifyHost()).equals(Boolean.TRUE)) {
                strCommand += " -verify_host";
            }

            // ---- Transaction Options ----
            if (env.containsKey("trans")) {
                strCommand += " -trans " + env.get("trans");
            }
            if (env.containsKey("commitonexit") && env.get("commitonexit").equals(Boolean.TRUE)) {
                strCommand += " -commitonexit";
            }
            if (env.containsKey("transmsgnum")) {
                strCommand += " -transmsgnum " + env.get("transmsgnum");
            }
            if (env.containsKey("transmgrtype")) {
                strCommand += " -transmgrtype " + env.get("transmgrtype");
            }
            if (env.containsKey("transjndiname")) {
                strCommand += " -transjndiname " + env.get("transjndiname");
            }
            if (env.containsKey("transtimeout")) {
                strCommand += " -transtimeout " + env.get("transtimeout");
            }

            // ----Other Options ----
            if (env.containsKey("xmlreaderclass") && !env.get("xmlreaderclass").equals("org.apache.crimson.parser.XMLReaderImpl")) {
                strCommand += " -xmlreaderclass " + env.get("xmlreaderclass");
            }
            if (env.containsKey("encoding") && !env.get("encoding").equals("UTF-8")) {
                strCommand += " -encoding " + env.get("encoding");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return strCommand;
    }

    private void btnPauseScreenItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            // Stop the screen refreshText.
            if (_timUpdateText.isRunning()) _timUpdateText.stop();
            refreshText();
        }
        else if (e.getStateChange() == ItemEvent.DESELECTED) {
            // Restart the screen refreshText.
            if (!_timUpdateText.isRunning()) _timUpdateText.start();
        }
    }

    private void mnuCreateFileJndiActionPerformed() {
        JmsStreamFileJndiConfig dlg = new JmsStreamFileJndiConfig(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    private void btnCreateFileJndiActionPerformed() {
        mnuCreateFileJndiActionPerformed();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar = new JMenuBar();
        menuFile = new JMenu();
        mnuNewConfig = new JMenuItem();
        mnuOpenConfig = new JMenuItem();
        mnuSaveConfigFile = new JMenuItem();
        mnuSaveConfigAs = new JMenuItem();
        mnuExit = new JMenuItem();
        menuTools = new JMenu();
        mnuConfiguration = new JMenuItem();
        mnuCreateFileJndi = new JMenuItem();
        mnuMessages = new JMenu();
        mnuNewMessages = new JMenuItem();
        mnuOpenMessages = new JMenuItem();
        mnuMsgEdit = new JMenuItem();
        mnuShowConfig = new JMenuItem();
        mnuGenCommandLine = new JMenuItem();
        menuHelp = new JMenu();
        mnuShowHelp = new JMenuItem();
        mnuLicense = new JMenuItem();
        mnuAbout = new JMenuItem();
        toolBarParent = new JToolBar();
        toolBar = new JToolBar();
        btnNewConfig = new JButton();
        btnOpenConfig = new JButton();
        btnSaveConfig = new JButton();
        btnConfigEdit = new JButton();
        btnShowConf = new JButton();
        btnGenCommandLine = new JButton();
        btnCreateFileJndi = new JButton();
        btnHelp = new JButton();
        dialogPane = new JPanel();
        buttonBar = new JPanel();
        btnStart = new JButton();
        btnStop = new JButton();
        btnPauseScreen = new JToggleButton();
        brnClearScreen = new JButton();
        panelMemory = new JPanel();
        lblMemory = new JLabel();
        btnGc = new JButton();
        panelMain = new JScrollPane();
        panelOutput = new JPanel();
        txtOutput = new JTextArea();
        hSpacer = new JPanel(null);
        vSpacer1 = new JPanel(null);
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setIconImage(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/tibrv_ico.gif")).getImage());
        setTitle("JmsStream");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setFont(new Font("Dialog", Font.PLAIN, 12));
        setForeground(Color.white);
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "default:grow",
            "fill:default, fill:default:grow"));

        //======== menuBar ========
        {

            //======== menuFile ========
            {
                menuFile.setText("File");
                menuFile.setMnemonic('F');

                //---- mnuNewConfig ----
                mnuNewConfig.setText("New Configuration...");
                mnuNewConfig.setMnemonic('C');
                mnuNewConfig.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuNewConfigActionPerformed();
                    }
                });
                menuFile.add(mnuNewConfig);

                //---- mnuOpenConfig ----
                mnuOpenConfig.setText("Open Configuration...");
                mnuOpenConfig.setMnemonic('C');
                mnuOpenConfig.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuOpenConfigActionPerformed();
                    }
                });
                menuFile.add(mnuOpenConfig);

                //---- mnuSaveConfigFile ----
                mnuSaveConfigFile.setText("Save Configuration");
                mnuSaveConfigFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
                mnuSaveConfigFile.setMnemonic('S');
                mnuSaveConfigFile.setEnabled(false);
                mnuSaveConfigFile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuSaveConfigFileActionPerformed();
                    }
                });
                menuFile.add(mnuSaveConfigFile);

                //---- mnuSaveConfigAs ----
                mnuSaveConfigAs.setText("Save Configuration As...");
                mnuSaveConfigAs.setMnemonic('A');
                mnuSaveConfigAs.setEnabled(false);
                mnuSaveConfigAs.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuSaveConfigAsActionPerformed();
                    }
                });
                menuFile.add(mnuSaveConfigAs);
                menuFile.addSeparator();

                //---- mnuExit ----
                mnuExit.setText("Exit");
                mnuExit.setMnemonic('E');
                mnuExit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuExitActionPerformed();
                    }
                });
                menuFile.add(mnuExit);
            }
            menuBar.add(menuFile);

            //======== menuTools ========
            {
                menuTools.setText("Tools");
                menuTools.setMnemonic('T');

                //---- mnuConfiguration ----
                mnuConfiguration.setText("Edit Configuration...");
                mnuConfiguration.setMnemonic('E');
                mnuConfiguration.setEnabled(false);
                mnuConfiguration.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuConfigurationActionPerformed();
                    }
                });
                menuTools.add(mnuConfiguration);
                menuTools.addSeparator();

                //---- mnuCreateFileJndi ----
                mnuCreateFileJndi.setText("Create a File Based JNDI...");
                mnuCreateFileJndi.setMnemonic('C');
                mnuCreateFileJndi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuCreateFileJndiActionPerformed();
                    }
                });
                menuTools.add(mnuCreateFileJndi);

                //======== mnuMessages ========
                {
                    mnuMessages.setText("Messages");
                    mnuMessages.setSelectedIcon(null);
                    mnuMessages.setMnemonic('M');

                    //---- mnuNewMessages ----
                    mnuNewMessages.setText("New Message File...");
                    mnuNewMessages.setMnemonic('N');
                    mnuNewMessages.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            mnuNewMessagesActionPerformed();
                        }
                    });
                    mnuMessages.add(mnuNewMessages);

                    //---- mnuOpenMessages ----
                    mnuOpenMessages.setText("Edit Message File...");
                    mnuOpenMessages.setMnemonic('E');
                    mnuOpenMessages.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            mnuOpenMessagesActionPerformed();
                        }
                    });
                    mnuMessages.add(mnuOpenMessages);

                    //---- mnuMsgEdit ----
                    mnuMsgEdit.setText("Edit Current Messages...");
                    mnuMsgEdit.setMnemonic('M');
                    mnuMsgEdit.setEnabled(false);
                    mnuMsgEdit.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            mnuMsgEditActionPerformed();
                        }
                    });
                    mnuMessages.add(mnuMsgEdit);
                }
                menuTools.add(mnuMessages);

                //---- mnuShowConfig ----
                mnuShowConfig.setText("Show Configuration");
                mnuShowConfig.setMnemonic('S');
                mnuShowConfig.setEnabled(false);
                mnuShowConfig.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuShowConfigActionPerformed();
                    }
                });
                menuTools.add(mnuShowConfig);

                //---- mnuGenCommandLine ----
                mnuGenCommandLine.setText("Display Command Line");
                mnuGenCommandLine.setEnabled(false);
                mnuGenCommandLine.setMnemonic('D');
                mnuGenCommandLine.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuGenCommandLineActionPerformed();
                    }
                });
                menuTools.add(mnuGenCommandLine);
            }
            menuBar.add(menuTools);

            //======== menuHelp ========
            {
                menuHelp.setText("Help");
                menuHelp.setMnemonic('H');

                //---- mnuShowHelp ----
                mnuShowHelp.setText("Help Topics...");
                mnuShowHelp.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/menu-help.png")));
                mnuShowHelp.setMnemonic('H');
                mnuShowHelp.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuShowHelpActionPerformed();
                    }
                });
                menuHelp.add(mnuShowHelp);

                //---- mnuLicense ----
                mnuLicense.setText("License...");
                mnuLicense.setMnemonic('L');
                mnuLicense.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuLicenseActionPerformed();
                    }
                });
                menuHelp.add(mnuLicense);
                menuHelp.addSeparator();

                //---- mnuAbout ----
                mnuAbout.setText("About");
                mnuAbout.setMnemonic('A');
                mnuAbout.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mnuAboutActionPerformed();
                    }
                });
                menuHelp.add(mnuAbout);
            }
            menuBar.add(menuHelp);
        }
        setJMenuBar(menuBar);

        //======== toolBarParent ========
        {
            toolBarParent.setFloatable(false);
            toolBarParent.setBorderPainted(false);
            toolBarParent.setBorder(null);

            //======== toolBar ========
            {
                toolBar.setForeground(SystemColor.control);

                //---- btnNewConfig ----
                btnNewConfig.setToolTipText("New Configuration");
                btnNewConfig.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/fileNew.png")));
                btnNewConfig.setMargin(new Insets(2, 2, 1, 1));
                btnNewConfig.setMaximumSize(new Dimension(25, 25));
                btnNewConfig.setForeground(SystemColor.menu);
                btnNewConfig.setMinimumSize(new Dimension(25, 25));
                btnNewConfig.setPreferredSize(new Dimension(25, 25));
                btnNewConfig.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnNewConfigActionPerformed();
                    }
                });
                toolBar.add(btnNewConfig);

                //---- btnOpenConfig ----
                btnOpenConfig.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/fileOpen.png")));
                btnOpenConfig.setToolTipText("Open Configuration");
                btnOpenConfig.setMargin(new Insets(5, 2, 1, 1));
                btnOpenConfig.setForeground(SystemColor.menu);
                btnOpenConfig.setMaximumSize(new Dimension(25, 25));
                btnOpenConfig.setMinimumSize(new Dimension(25, 25));
                btnOpenConfig.setPreferredSize(new Dimension(25, 25));
                btnOpenConfig.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnOpenConfigActionPerformed();
                    }
                });
                toolBar.add(btnOpenConfig);

                //---- btnSaveConfig ----
                btnSaveConfig.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/fileSave2.png")));
                btnSaveConfig.setToolTipText("Save Configuration");
                btnSaveConfig.setMargin(new Insets(1, 2, 1, 1));
                btnSaveConfig.setEnabled(false);
                btnSaveConfig.setMaximumSize(new Dimension(25, 25));
                btnSaveConfig.setMinimumSize(new Dimension(25, 25));
                btnSaveConfig.setPreferredSize(new Dimension(25, 25));
                btnSaveConfig.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnConfigSaveActionPerformed();
                    }
                });
                toolBar.add(btnSaveConfig);

                //---- btnConfigEdit ----
                btnConfigEdit.setToolTipText("Edit Current Configuration");
                btnConfigEdit.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/fileEdit.png")));
                btnConfigEdit.setEnabled(false);
                btnConfigEdit.setMargin(new Insets(2, 2, 1, 1));
                btnConfigEdit.setMaximumSize(new Dimension(25, 25));
                btnConfigEdit.setMinimumSize(new Dimension(25, 25));
                btnConfigEdit.setPreferredSize(new Dimension(25, 25));
                btnConfigEdit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnConfigEditActionPerformed();
                    }
                });
                toolBar.add(btnConfigEdit);
                toolBar.addSeparator();

                //---- btnShowConf ----
                btnShowConf.setToolTipText("Display Configuration");
                btnShowConf.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/information.png")));
                btnShowConf.setEnabled(false);
                btnShowConf.setMargin(new Insets(2, 2, 1, 1));
                btnShowConf.setMaximumSize(new Dimension(25, 25));
                btnShowConf.setMinimumSize(new Dimension(25, 25));
                btnShowConf.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnShowConfActionPerformed();
                    }
                });
                toolBar.add(btnShowConf);

                //---- btnGenCommandLine ----
                btnGenCommandLine.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/console.png")));
                btnGenCommandLine.setMargin(new Insets(2, 2, 1, 1));
                btnGenCommandLine.setEnabled(false);
                btnGenCommandLine.setToolTipText("Display Command Line Arguments");
                btnGenCommandLine.setMaximumSize(new Dimension(25, 25));
                btnGenCommandLine.setMinimumSize(new Dimension(25, 25));
                btnGenCommandLine.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnGenCommandLineActionPerformed();
                    }
                });
                toolBar.add(btnGenCommandLine);

                //---- btnCreateFileJndi ----
                btnCreateFileJndi.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/jndiCreate.png")));
                btnCreateFileJndi.setMargin(new Insets(2, 2, 1, 1));
                btnCreateFileJndi.setMaximumSize(new Dimension(25, 25));
                btnCreateFileJndi.setMinimumSize(new Dimension(25, 25));
                btnCreateFileJndi.setToolTipText("Create File Based JNDI");
                btnCreateFileJndi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnCreateFileJndiActionPerformed();
                    }
                });
                toolBar.add(btnCreateFileJndi);
                toolBar.addSeparator();

                //---- btnHelp ----
                btnHelp.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/menu-help.png")));
                btnHelp.setMargin(new Insets(1, 2, 1, 1));
                btnHelp.setToolTipText("Output JmsStream Help");
                btnHelp.setMaximumSize(new Dimension(25, 25));
                btnHelp.setMinimumSize(new Dimension(25, 25));
                btnHelp.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnHelpActionPerformed();
                    }
                });
                toolBar.add(btnHelp);
            }
            toolBarParent.add(toolBar);
        }
        contentPane.add(toolBarParent, cc.xy(1, 1));

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.createEmptyBorder("1dlu, 4dlu, 4dlu, 4dlu"));
            dialogPane.setLayout(new FormLayout(
                "default:grow",
                "fill:default:grow, fill:default"));

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.createEmptyBorder("5dlu, 1dlu, 1dlu, 1dlu"));
                buttonBar.setLayout(new FormLayout(
                    "$button, $rgap, $button, $glue, [50dlu,default], $lcgap, $button, $rgap, default",
                    "pref"));

                //---- btnStart ----
                btnStart.setText("Start");
                btnStart.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/start.png")));
                btnStart.setEnabled(false);
                btnStart.setMaximumSize(new Dimension(80, 30));
                btnStart.setMinimumSize(new Dimension(80, 30));
                btnStart.setPreferredSize(new Dimension(80, 30));
                btnStart.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnStartActionPerformed();
                    }
                });
                buttonBar.add(btnStart, cc.xy(1, 1));

                //---- btnStop ----
                btnStop.setText("Stop");
                btnStop.setEnabled(false);
                btnStop.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/stop.png")));
                btnStop.setPreferredSize(new Dimension(80, 30));
                btnStop.setMaximumSize(new Dimension(80, 30));
                btnStop.setMinimumSize(new Dimension(80, 30));
                btnStop.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        btnStopActionPerformed();
                    }
                });
                buttonBar.add(btnStop, cc.xy(3, 1));

                //---- btnPauseScreen ----
                btnPauseScreen.setText("Pause Screen");
                btnPauseScreen.setMaximumSize(new Dimension(260, 30));
                btnPauseScreen.setMinimumSize(new Dimension(130, 30));
                btnPauseScreen.setPreferredSize(new Dimension(130, 30));
                btnPauseScreen.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/pause.png")));
                btnPauseScreen.setToolTipText("Pause the JmsStream GUI display screen.");
                btnPauseScreen.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        btnPauseScreenItemStateChanged(e);
                    }
                });
                buttonBar.add(btnPauseScreen, cc.xy(5, 1));

                //---- brnClearScreen ----
                brnClearScreen.setMaximumSize(new Dimension(260, 30));
                brnClearScreen.setMinimumSize(new Dimension(130, 30));
                brnClearScreen.setPreferredSize(new Dimension(130, 30));
                brnClearScreen.setText("Clear Screen");
                brnClearScreen.setToolTipText("Clear the JmsStream GUI display screen.");
                brnClearScreen.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/clear.png")));
                brnClearScreen.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        brnClearScreenActionPerformed();
                    }
                });
                buttonBar.add(brnClearScreen, cc.xy(7, 1));

                //======== panelMemory ========
                {
                    panelMemory.setBorder(new EtchedBorder(EtchedBorder.RAISED));
                    panelMemory.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                    panelMemory.setLayout(new BoxLayout(panelMemory, BoxLayout.X_AXIS));

                    //---- lblMemory ----
                    lblMemory.setText("100MB of 200MB");
                    lblMemory.setMaximumSize(new Dimension(90, 20));
                    lblMemory.setMinimumSize(new Dimension(90, 20));
                    lblMemory.setPreferredSize(new Dimension(90, 20));
                    lblMemory.setHorizontalAlignment(SwingConstants.TRAILING);
                    lblMemory.setForeground(Color.darkGray);
                    lblMemory.setRequestFocusEnabled(false);
                    lblMemory.setAlignmentY(0.0F);
                    lblMemory.setVerticalAlignment(SwingConstants.BOTTOM);
                    panelMemory.add(lblMemory);

                    //---- btnGc ----
                    btnGc.setMargin(new Insets(2, 2, 2, 2));
                    btnGc.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/gc.png")));
                    btnGc.setToolTipText("Run JVM Garbage Collector");
                    btnGc.setBorderPainted(false);
                    btnGc.setOpaque(false);
                    btnGc.setContentAreaFilled(false);
                    btnGc.setFocusPainted(false);
                    btnGc.setRolloverIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/gc_pushed.png")));
                    btnGc.setAlignmentY(0.0F);
                    btnGc.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            btnGcActionPerformed();
                        }
                    });
                    panelMemory.add(btnGc);
                }
                buttonBar.add(panelMemory, cc.xy(9, 1));
            }
            dialogPane.add(buttonBar, cc.xy(1, 2, CellConstraints.FILL, CellConstraints.DEFAULT));

            //======== panelMain ========
            {
                panelMain.setBorder(new BevelBorder(BevelBorder.LOWERED));
                panelMain.setViewportBorder(new BevelBorder(BevelBorder.RAISED));
                panelMain.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                panelMain.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

                //======== panelOutput ========
                {
                    panelOutput.setLayout(new FormLayout(
                        "default:grow, default",
                        "fill:default:grow, fill:default"));

                    //---- txtOutput ----
                    txtOutput.setBackground(Color.black);
                    txtOutput.setForeground(Color.lightGray);
                    txtOutput.setBorder(new BevelBorder(BevelBorder.LOWERED));
                    txtOutput.setAutoscrolls(false);
                    txtOutput.setDoubleBuffered(true);
                    txtOutput.setCaretColor(Color.white);
                    txtOutput.setFocusCycleRoot(true);
                    txtOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    panelOutput.add(txtOutput, cc.xy(1, 1));
                    panelOutput.add(hSpacer, cc.xy(2, 1));
                    panelOutput.add(vSpacer1, cc.xy(1, 2));
                }
                panelMain.setViewportView(panelOutput);
            }
            dialogPane.add(panelMain, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.FILL));
        }
        contentPane.add(dialogPane, cc.xy(1, 2, CellConstraints.FILL, CellConstraints.FILL));
        setSize(990, 600);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        // Now create a new TextAreaOutputStream to write to our JTextArea control and wrap a
        // PrintStream around it to support the println/printf methods.
        _taosTextArea = new TextAreaOutputStream(txtOutput);
        try {
            PrintStream psOut = new PrintStream(_taosTextArea, true, "UTF-8");

            // Redirect standard output stream to the TextAreaOutputStream
            System.setOut(psOut);
            // Redirect standard error stream to the TextAreaOutputStream
            System.setErr(psOut);
        }
        catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        // Set the Form Title
        this.setTitle(JmsStream.APP_NAME);
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem mnuNewConfig;
    private JMenuItem mnuOpenConfig;
    private JMenuItem mnuSaveConfigFile;
    private JMenuItem mnuSaveConfigAs;
    private JMenuItem mnuExit;
    private JMenu menuTools;
    private JMenuItem mnuConfiguration;
    private JMenuItem mnuCreateFileJndi;
    private JMenu mnuMessages;
    private JMenuItem mnuNewMessages;
    private JMenuItem mnuOpenMessages;
    private JMenuItem mnuMsgEdit;
    private JMenuItem mnuShowConfig;
    private JMenuItem mnuGenCommandLine;
    private JMenu menuHelp;
    private JMenuItem mnuShowHelp;
    private JMenuItem mnuLicense;
    private JMenuItem mnuAbout;
    private JToolBar toolBarParent;
    private JToolBar toolBar;
    private JButton btnNewConfig;
    private JButton btnOpenConfig;
    private JButton btnSaveConfig;
    private JButton btnConfigEdit;
    private JButton btnShowConf;
    private JButton btnGenCommandLine;
    private JButton btnCreateFileJndi;
    private JButton btnHelp;
    private JPanel dialogPane;
    private JPanel buttonBar;
    private JButton btnStart;
    private JButton btnStop;
    private JToggleButton btnPauseScreen;
    private JButton brnClearScreen;
    private JPanel panelMemory;
    private JLabel lblMemory;
    private JButton btnGc;
    private JScrollPane panelMain;
    private JPanel panelOutput;
    private JTextArea txtOutput;
    private JPanel hSpacer;
    private JPanel vSpacer1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
