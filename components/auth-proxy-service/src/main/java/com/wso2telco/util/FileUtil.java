/*
 * FileUtil.java
 * Apr 2, 2013  11:20:38 AM
 * Tharanga Ranaweera
 *
 * Copyright (C) Dialog Axiata PLC. All Rights Reserved.
 */

package com.wso2telco.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * <TO-DO>
 * <code>FileUtil</code>
 *
 * @version $Id: FileUtil.java,v 1.00.000
 */
public class FileUtil {

    private static Log log = LogFactory.getLog(FileUtil.class);

    private static Properties props = new Properties();

    public static boolean createDirectory(String directoryName, String directoryPath) {
        if ((new File(directoryPath + "\\" + directoryName)).exists()) {
            return true;
        } else {
            // File or directory does not exist
            return new File(directoryPath + "\\" + directoryName).mkdirs();
        }
    }

    /**
     * public static boolean fileUpload(FileItem item,String tempFolderName) {
     * try { //HttpServletRequest request,FileItem item,String db,String userID
     * String uploadedFileFullPath = item.getName(); String realFileName =
     * uploadedFileFullPath.substring(uploadedFileFullPath.lastIndexOf("\\") +
     * 1); File uploadedFile = new File(tempFolderName + "\\" + realFileName);
     * item.write(uploadedFile);
     *
     * } catch (Exception ex) {
     * Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex); }
     *
     * return true; }
     */
    /*public static String createTempFolder(HttpServletRequest request, String db, String userID) {
     String serverFolder = request.getRealPath("\\_TEMP");
     String currentDate = DateUtil.getDateTime("yyyy-mm-dd");
     String tempFolderName = "";

     if (FileUtil.createDirectory(currentDate, serverFolder)) {
     tempFolderName = serverFolder + "\\" + currentDate;
     String uniqueFolder = db + "_" + userID + "_" + UUID.randomUUID().toString();
     if (FileUtil.createDirectory(uniqueFolder, tempFolderName)) {
     tempFolderName = tempFolderName + "\\" + uniqueFolder;
     }
     }

     return tempFolderName;

     } */
    public static void deleteFile(String filename) {

        try {
            File f1 = new File(filename);
            boolean success = f1.delete();
            if (!success) {
                //System.out.println("Deletion failed.");
                //System.exit(0);
            } else {
                //System.out.println("File deleted.");
            }
        } catch (Exception e) {
        }
    }

    public static String getCorrectFileName(String fileName) {

        // REPLACING ILLEGAL CHARACTERS, Replacing characters with an underscore '_'
        fileName = fileName.replaceAll(" ", "_");


        return fileName;
    }

    public static void fileWrite(String filePath, String data) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(filePath));
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }

    }

    static {
        try {
            props.load(FileUtil.class.getResourceAsStream("application.properties"));
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
            System.err.println(
                    "Check your Property file, it should be in application home dir, Error:"
                    + e.getCause() + "Cant load APPLICATION.properties");

            //System.exit(-1);
        } catch (IOException e) {
            System.err.println(
                    "Check your Property file, it should be in application home dir, Error:"
                    + e.getCause() + "Cant load APPLICATION.properties");
            //System.exit(-1);
        }
    }

    /**
     * This method return value from property file of corresponding key passed.
     *
     * @return String
     */
    public static String getApplicationProperty(String key) {
        return props.getProperty(key);
    }

    public static String ReadFullyIntoVar(String fullpath) {

        String result = "";

        try {

            log.info(fullpath + " file path");
            FileInputStream file = new FileInputStream(fullpath);
            DataInputStream in = new DataInputStream(file);
            byte[] b = new byte[in.available()];
            in.readFully(b);
            in.close();
            result = new String(b, 0, b.length, "Cp850");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void copy(String src, String dst) throws IOException {

        String fileName = src.substring(src.lastIndexOf("/") + 1);

        File fsrc = new File(src);
        File fdst = new File(dst + "/" + fileName);

        InputStream in = new FileInputStream(fsrc);
        OutputStream out = new FileOutputStream(fdst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
