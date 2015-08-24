/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.gui.helper;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Title:        File Name Filter<p>
 * Description:  File chooser dialog box helper class.<p>
 * @author A. Kevin Bailey
 * @version 2.7.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class FileNameFilter extends FileFilter
{
    public final static short MSG = 0;
    public final static short CONFIG = 1;
    public final static short CSV = 2;
    public final static short ZIP = 3;
    // set client identity if specified. ssl_key may be null if identity is PKCS12, JKS or EPF.
    // 'j2se' only supports PKCS12 and JKS. 'entrust61' also supports PEM and PKCS8.
    public final static short CRYPT = 4;
    public final static short CRYPT_ID_J = 5; // j2se
    public final static short CRYPT_ID_E = 6; // entrust61
    public final static short CRYPT_ID_I = 7; // ibm
    public final static short CRYPT_JKS = 8;
    public final static short CRYPT_PKCS12 = 9;

    private short _intType;

    public FileNameFilter(short type) {
        _intType = type;
    }

    // Accept all directories and all sav and jmss files.
    public boolean accept(File f)
    {
        String extension = "";

        if (f.isDirectory()) {
            return true;
        }

        try {
            extension = Utils.getExtension(f);
        }
        catch (Exception e) {
            // do nothing
        }

        if (extension != null) {
            switch (_intType) {
                case MSG:
                    return extension.equals(Utils.msg) || extension.equals(Utils.sav) || extension.equals(Utils.jmss);
                case CONFIG:
                    return extension.equals(Utils.conf);
                case CSV:
                    return extension.equals(Utils.csv);
                case ZIP:
                    return extension.equals(Utils.zip);
                case CRYPT:
                    return extension.equals(Utils.pem) || extension.equals(Utils.der) || extension.equals(Utils.p8) || extension.equals(Utils.p7b);
                case CRYPT_ID_J:
                    // set client identity if specified. ssl_key may be null if identity is PKCS12, JKS or EPF.
                    // 'j2se' only supports PKCS12 and JKS. 'entrust6' also supports PEM and PKCS8.
                    return extension.equals(Utils.p12) || extension.equals(Utils.jks) || extension.equals(Utils.ks);
                case CRYPT_ID_E:
                    return extension.equals(Utils.pem) || extension.equals(Utils.p8) || extension.equals(Utils.epf);
                case CRYPT_JKS:
                    return extension.equals(Utils.jks) || extension.equals(Utils.ks) || extension.equals(Utils.ts);
                case CRYPT_PKCS12:
                    return extension.equals(Utils.p12) || extension.equals(Utils.ts);
            }
        }

        return false;
    }

    // The description of this filter
    public String getDescription() {
        String strReturn = "";

        switch (_intType) {
            case MSG:
                strReturn = "JMS Message Files (*.msg, *.sav, *.jmss)";
                break;
            case CONFIG:
                strReturn = "JmsStream Config Files (*.conf)";
                break;
            case CSV:
                strReturn = "CSV Files (*.csv)";
                break;
            case ZIP:
                strReturn = "ZIP Files (*.zip)";
                break;
            case CRYPT:
                /** .pem  PEM encoded certificates and keys (allows the certificate and private key to be stored together in the same file)
                 *  .der  DER encoded certificates
                 *  .p8  PKCS#8 file
                 *  .p7b  PKCS#7 file
                 *  .jks  Java KeyStore file
                 *  .epf  Entrust store file
                 */
                strReturn = "SSL Certificates (*.pem, *.der, *.p8, *.p7b)";
                break;
            case CRYPT_ID_J:
                // set client identity if specified. ssl_key may be null if identity is PKCS12, JKS or EPF.
                // 'j2se' only supports PKCS12 and JKS. 'entrust6' also supports PEM and PKCS8.
                strReturn = "SSL ID Certificates (*.p12, *.jks, *ks)";
                break;
            case CRYPT_ID_E:
                strReturn = "SSL ID Certificates (*.pem, *.p8, *.epf)";
                break;
            case CRYPT_ID_I:
                strReturn = "SSL ID Certificates (*.pem, *.p8, *.epf)";
                break;
            case CRYPT_JKS:
                strReturn = "SSL ID Certificates (*.jks, *.ks, *.ts)";
                break;
            case CRYPT_PKCS12:
                strReturn = "SSL ID Certificates (*.p12, *.ts)";
                break;
        }

        return strReturn;
    }

    public static File getUserDir()
    {
        String strUserDir = System.getProperty("user.dir");
        return new File(strUserDir);
    }
 }

final class Utils
{
    public final static String msg = "msg";
    public final static String sav = "sav";
    public final static String jmss = "jmss";
    public final static String conf = "conf";
    public final static String csv = "csv";
    public final static String zip = "zip";
    public final static String pem = "pem";
    public final static String der = "der";
    public final static String p8 = "p8";
    public final static String p7b = "p7b";
    public final static String jks = "jks";
    public final static String ks = "ks";
    public final static String epf = "epf";
    public final static String p12 = "p12";
    public final static String ts = "ts";

    /*
    * Get the extension of a file.
    */
    public static String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}