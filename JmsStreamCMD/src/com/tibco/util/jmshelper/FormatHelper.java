/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.jmshelper;

import javax.naming.Context;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.7.7
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "WeakerAccess"})
public final class FormatHelper
{
    /**
     * Replaces the XML symbols with their codes.  Used for
     * putting string inside XML.
     *
     * @param   strIn    the input string.
     * @return  the string with the XML symbols replaced with codes.
     */
    public static String translateXML(String strIn)
    {
        strIn = strIn.replaceAll("&", "&amp;");
        strIn = strIn.replaceAll("\"", "&quot;");
        strIn = strIn.replaceAll("'", "&apos;");
        strIn = strIn.replaceAll("<", "&lt;");
        strIn = strIn.replaceAll(">", "&gt;");
        return strIn;
    }

    /**
     * Replaces a single backslash "\"  in a string with double
     * backslash "\\".  Method not modify preexisting "//".
     *
     * @param   strIn    the input string.
     * @return  the string with "\" replaced with "\\"
     */
    public static String fixEscapeChars(String strIn)
    {
        char [] chrInstr = strIn.toCharArray();
        String strOut = "";
        for (char aChrInstr : chrInstr) {
            switch (aChrInstr) {
                case '\\':
                    strOut += "\\\\";
                    break;
                case '\b':
                    strOut += "\\b";
                    break;
                case '\t':
                    strOut += "\\t";
                    break;
                case '\n':
                    strOut += "\\n";
                    break;
                case '\r':
                    strOut += "\\r";
                    break;
                case '\"':
                    strOut += "\\\"";
                    break;
                case '\'':
                    strOut += "\\\'";
                    break;
                default:
                    strOut += aChrInstr;
            }
        }
        return strOut;
    }

    /**
     * Tokenizes a CSV string and returns a Vector containing the string values.
     *
     * @param   strIn    a string containing a values in a csv format.
     * @return  a Vector that holds the tokenized String.
     */
    public static Vector<String> stringToVector(String strIn)
    {
        Vector<String> vecOut = new Vector<String>();
        String [] strs;

        // Remove [ and ] from start of string
        if (strIn.startsWith("[")) strIn = strIn.substring(1);
        if (strIn.endsWith("]")) strIn = strIn.substring(0, strIn.length() - 1);
        // Tokenize the string
        strs = strIn.split(",");

        // Add the strings to the Vector
        for (String str : strs) vecOut.add(str.trim());  // This is a for - each

        return vecOut;
    }

    /**
     * This static method get the JmsStream configuration properties from
     * a file.
     *
     * @param   strFileURL  fully qualified path to the JmsStream Properties file.
     * @return              a Hashtable with the JmsStream configuration properties.
     * @throws java.io.IOException  File Exception
     *
     */
    @SuppressWarnings({"FieldCanBeLocal", "unchecked"})
    public static Hashtable getPropertiesFile(String strFileURL) throws java.io.IOException
    {
        Hashtable env;
        java.io.FileInputStream inFileStream;
        Properties propsJmsStream = new Properties();
        java.util.Iterator iteEnv;
        java.util.Map.Entry entryEnv;

        inFileStream = new java.io.FileInputStream(strFileURL);
        propsJmsStream.load(inFileStream);
        inFileStream.close();
        env = new Hashtable(propsJmsStream);
        iteEnv = env.entrySet().iterator();

        while (iteEnv.hasNext()) {
            entryEnv = (java.util.Map.Entry)iteEnv.next();
            // Change the Boolean string values to Boolean Class and numbers to a Integer or Float Class
            if (entryEnv.getValue().equals("true")) entryEnv.setValue(Boolean.TRUE);
            else if (entryEnv.getValue().equals("false")) entryEnv.setValue(Boolean.FALSE);
            else if (entryEnv.getKey().equals("zipmsgperentry")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("rate")) entryEnv.setValue(new Float(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("maxrate")) entryEnv.setValue(new Float(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("numberofintervals")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("intervalsize")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("stats")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("fileloop")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("stopafter")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("transmsgnum")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("replytimeout")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("transtimeout")) entryEnv.setValue(new Integer(entryEnv.getValue().toString()));
            else if (entryEnv.getKey().equals("com.tibco.tibjms.naming.ssl_trusted_certs")) {
                entryEnv.setValue(FormatHelper.stringToVector(entryEnv.getValue().toString()));
            }
            else if (entryEnv.getKey().equals("com.tibco.tibjms.ssl.trusted_certs")) {
                entryEnv.setValue(FormatHelper.stringToVector(entryEnv.getValue().toString()));
            }
            else if (entryEnv.getKey().equals("zipentries")) {
                entryEnv.setValue(FormatHelper.stringToVector(entryEnv.getValue().toString()));
            }
        }
        return env;
    }

    /**
     * This static method checks the strConfigVar string against all the JmsStream configuration
     * properties and reruns true if matches one.
     *
     * @param   strConfigProp   string to test.
     * @param   strClientType   the client type constant from ConnectionHelper
     * @return                  true if strConfigVar is a JmsStream configuration properties, false otherwise.
     *
     */
    public static boolean isConfigProp(String strConfigProp, String strClientType)
    {
        ConnectionHelper conHelper = new ConnectionHelper(strClientType);

        if (strConfigProp.equals(Context.INITIAL_CONTEXT_FACTORY)) return true;
        else if (strConfigProp.equals(Context.PROVIDER_URL)) return true;
        else if (strConfigProp.equals(Context.URL_PKG_PREFIXES)) return true;
        else if (strConfigProp.equals(Context.AUTHORITATIVE)) return true;
        else if (strConfigProp.equals(Context.BATCHSIZE)) return true;
        else if (strConfigProp.equals(Context.SECURITY_AUTHENTICATION)) return true;
        else if (strConfigProp.equals(Context.SECURITY_PRINCIPAL)) return true;
        else if (strConfigProp.equals(Context.SECURITY_CREDENTIALS)) return true;
        else if (strConfigProp.equals("guiDisplayRefresh")) return true;
        else if (strConfigProp.equals("guiDisplayBuffer")) return true;
        else if (strConfigProp.equals("jmsclient")) return true;
        else if (strConfigProp.equals("connectionfactory")) return true;
        else if (strConfigProp.equals("requestreply")) return true;
        else if (strConfigProp.equals("replytimeout")) return true;
        else if (strConfigProp.equals("type")) return true;
        else if (strConfigProp.equals("timed")) return true;
        else if (strConfigProp.equals("speed")) return true;
        else if (strConfigProp.equals("isListener")) return true;
        else if (strConfigProp.equals("listendest")) return true;
        else if (strConfigProp.equals("senddest")) return true;
        else if (strConfigProp.equals("asyncreply")) return true;
        else if (strConfigProp.equals("unsubscribe")) return true;
        else if (strConfigProp.equals("noconfirm")) return true;
        else if (strConfigProp.equals("browse")) return true;
        else if (strConfigProp.equals("zip")) return true;
        else if (strConfigProp.equals("zipmsgperentry")) return true;
        else if (strConfigProp.equals("xmlreaderclass")) return true;
        else if (strConfigProp.equals("stats")) return true;
        else if (strConfigProp.equals("raw")) return true;
        else if (strConfigProp.equals("echoxml")) return true;
        else if (strConfigProp.equals("echocsv")) return true;
        else if (strConfigProp.equals("verbose")) return true;
        else if (strConfigProp.equals("noecho")) return true;
        else if (strConfigProp.equals("fileappend")) return true;
        else if (strConfigProp.equals("filetype")) return true;
        else if (strConfigProp.equals("fileloop")) return true;
        else if (strConfigProp.equals("csvfile")) return true;
        else if (strConfigProp.equals("usetibcolib")) return true;
        else if (strConfigProp.equals("commitonexit")) return true;
        else if (strConfigProp.equals("durablename")) return true;
        else if (strConfigProp.equals("trans")) return true;
        else if (strConfigProp.equals("transmsgnum")) return true;
        else if (strConfigProp.equals("transmgrtype")) return true;
        else if (strConfigProp.equals("transjndiname")) return true;
        else if (strConfigProp.equals("transtimeout")) return true;
        else if (strConfigProp.equals("commitonexit")) return true;
        else if (strConfigProp.equals("file")) return true;
        else if (strConfigProp.equals("fileappend")) return true;
        else if (strConfigProp.equals("fileloop")) return true;
        else if (strConfigProp.equals("replyfile")) return true;
        else if (strConfigProp.equals("configfile")) return true;
        else if (strConfigProp.equals("isSeparateUsrPwd")) return true;
        else if (strConfigProp.equals("user")) return true;
        else if (strConfigProp.equals("password")) return true;
        else if (strConfigProp.equals("ssl")) return true;
        else if (strConfigProp.equals("deliverymode")) return true;
        else if (strConfigProp.equals("ackmode")) return true;
        else if (strConfigProp.equals("rate")) return true;
        else if (strConfigProp.equals("maxrate")) return true;
        else if (strConfigProp.equals("numberofintervals")) return true;
        else if (strConfigProp.equals("intervalsize")) return true;
        else if (strConfigProp.equals("ratestamp")) return true;
        else if (strConfigProp.equals("variablerate")) return true;
        else if (strConfigProp.equals("sndtimestamp")) return true;
        else if (strConfigProp.equals("rcvtimestamp")) return true;
        else if (strConfigProp.equals("sequence")) return true;
        else if (strConfigProp.equals("encoding")) return true;
        else if (strConfigProp.equals("selector")) return true;
        else if (strConfigProp.equals("clientid")) return true;
        else if (strConfigProp.equals("compress")) return true;
        else if (strConfigProp.equals("showconfig")) return true;
        else if (strConfigProp.equals("getbodylength")) return true;
        else if (strConfigProp.equals("zipentries")) return true;
        else if (strConfigProp.equals("stopafter")) return true;
        else if (strConfigProp.equals("trustAllCerts")) return true;
        else if (strConfigProp.equals("useFileJNDI")) return true;
        else if (strConfigProp.equals("extractmonmsg")) return true;
        else if (conHelper.isDefaultSetting(strConfigProp)) return true;

        return false;
    }
}