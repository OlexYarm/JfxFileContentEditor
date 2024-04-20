/*
 * Copyright (c) 2024, Oleksandr Yarmolenko. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 */
package com.olexyarm.jfxfilecontenteditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static final Map<String, Node> MAP_NODE_REFS = new HashMap<>();

    public enum NODE_NAMES {
        hboxBottomSearchResult, hboxBottomFind, hboxBottomReplace
    };

    // ---------------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------------
    public static Parent loadFXML(String fxml) throws IOException {
        LOGGER.debug("### Loading fxml. fxml=\"" + fxml + "\"");
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    // ---------------------------------------------------------------------------
    public static boolean checkNewTabsAllowed(ObservableList<Tab> lstTabs) {

        if (lstTabs == null) {
            // it should never happen, but ...
            LOGGER.error("List of tabs is null.");
            return false;
        }
        int int_tabs_count = lstTabs.size();
        if (int_tabs_count >= Settings.INT_TABS_COUNT_MAX) {
            LOGGER.error("# Too many files opened."
                    + " TABS_COUNT=" + int_tabs_count
                    + " TABS_COUNT_MAX=" + Settings.INT_TABS_COUNT_MAX);
            showMessage(Alert.AlertType.ERROR, "Open File", "Too many files opened (" + int_tabs_count + " files)",
                    "Please close any tab and try again, or change settings for TABS MAX.", null, null);
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------------------
    public static String readTextFileToString(Path pathFile) {

        if (pathFile == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        Charset charset = Charset.forName(Settings.STR_CHARSET_OS_DEFAULT);
        try (BufferedReader reader = Files.newBufferedReader(pathFile, charset)) {
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                sb.append(strLine);
            }
        } catch (Throwable t) {
            LOGGER.error("Could not Open or Read File."
                    + " pathFile=\"" + pathFile + "\""
                    + " Throwable=\"" + t.toString() + "\"");
        }
        String strFileContent = sb.toString();
        return strFileContent;
    }

    // -------------------------------------------------------------------------------------
    public static boolean checkAnyoneFileModified(ObservableList<Tab> lstTabs) {

        if (lstTabs == null || lstTabs.isEmpty()) {
            return false;
        }
        for (Tab tab : lstTabs) {
            FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
            boolean booFileModified = fileEditor.isFileModified();
            if (booFileModified) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------------------
    public static boolean createNewFile(Path pathFile) {

        switch (createFile(pathFile)) {
            case 0:
                return false;
            case 1:
                return true;
            case -1:
                // Could be no Parent Directory.
                if (createParentDirectories(pathFile)) {
                    if (createFile(pathFile) == 1) {
                        return true;
                    }
                }
            default:
                return false;
        }
    }

    // -------------------------------------------------------------------------------------
    public static boolean checkFileExist(String strTabID, Path pathFile) {

        if (pathFile == null) {
            LOGGER.error("pathFile is null."
                    + " TabId=\"" + strTabID + "\"");
            return false;
        }

        try {
            if (Files.isDirectory(pathFile)) {
                LOGGER.error("File is directory."
                        + " TabId=\"" + strTabID + "\""
                        + " pathFile=\"" + pathFile + "\"");
                return false;
            }
            if (Files.isReadable(pathFile)) {
                return true;
            }
        } catch (Throwable t) {
            LOGGER.error("Could analize File because of security violation."
                    + " TabId=\"" + strTabID + "\""
                    + " pathFile=\"" + pathFile + "\""
                    + " Throwable=\"" + t.toString() + "\"");
            return false;
        }
        return false;
    }

    // -------------------------------------------------------------------------------------
    public static int createFile(Path pathFile) {

        if (pathFile == null) {
            LOGGER.error("Could not create null File.");
            return 0;
        }

        if (Files.exists(pathFile) && !Files.isDirectory(pathFile) && Files.isReadable(pathFile)) {
            if (Files.isWritable(pathFile)) {
                LOGGER.info("File already exist and writeable."
                        + " FilePath=\"" + pathFile + "\"");
                return 1;
            } else {
                try {
                    Files.setAttribute(pathFile, "readonly", "false");
                } catch (IOException ex) {
                    LOGGER.error("File already exist, could not set writable."
                            + " FilePath=\"" + pathFile + "\""
                            + " IOException=\"" + ex.toString() + "\"");
                    return 0;
                }
            }
            if (!Files.isWritable(pathFile)) {
                LOGGER.info("File already exist and is read-only."
                        + " FilePath=\"" + pathFile + "\"");
                return 0;
            }
        }

        try {
            Path pathFileNew = Files.createFile(pathFile);
            LOGGER.info("Created new File."
                    + " FilePath=\"" + pathFileNew + "\"");
            return 1;
        } catch (IOException e) {
            Path pathParent = pathFile.getParent();
            if (Files.exists(pathParent)) {
                LOGGER.error("Could not create new File."
                        + " FilePath=\"" + pathFile + "\""
                        + " IOException=\"" + e.toString() + "\"");
                return -1;
            }
            try {
                Files.createDirectories(pathParent);
                LOGGER.info("Created new Directories."
                        + " FilePath=\"" + pathFile + "\""
                        + " pathParent=\"" + pathParent + "\"");
            } catch (IOException ex) {
                LOGGER.error("Could not create parent directory."
                        + " FilePath=\"" + pathFile + "\""
                        + " pathParent=\"" + pathParent + "\""
                        + " IOException=\"" + ex.toString() + "\"");
                return -1;
            }
            try {
                Path pathFileNew = Files.createFile(pathFile);
                LOGGER.info("Created new File."
                        + " FilePath=\"" + pathFileNew + "\"");
                return 1;
            } catch (IOException ex) {
                LOGGER.error("Could not create new File."
                        + " FilePath=\"" + pathFile + "\""
                        + " pathParent=\"" + pathParent + "\""
                        + " IOException=\"" + e.toString() + "\"");
                return -1;
            }
        } catch (Throwable t) {
            String strErrMsg = t.toString();
            LOGGER.error("Could not create new File."
                    + " FilePath=\"" + pathFile + "\""
                    + " Throwable=\"" + strErrMsg + "\"");
            return 0;
        }
    }

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------
    private static boolean createParentDirectories(Path pathFile) {

        if (pathFile == null) {
            LOGGER.error("Could not create Parent Directory for null File.");
            return false;
        }

        Path pathFileAbsolute = pathFile.toAbsolutePath();
        Path pathParent = pathFileAbsolute.getParent();
        if (pathParent == null) {
            LOGGER.error("Could not create null Parent Directory."
                    + " FilePath=\"" + pathFile + "\""
                    + " FilePathAbsolute=\"" + pathFileAbsolute + "\"");
            return false;
        }
        Path pathParentDir;
        if (!Files.exists(pathParent)) {
            LOGGER.info("Parent directory does not exist."
                    + " FilePath=\"" + pathFile + "\""
                    + " FilePathAbsolute=\"" + pathFileAbsolute + "\""
                    + " pathParent=\"" + pathParent + "\"");
            try {
                pathParentDir = Files.createDirectories(pathFile);
            } catch (Throwable ex) {
                LOGGER.error("Cannot create Parent directories."
                        + " FilePath=\"" + pathFile + "\""
                        + " FilePathAbsolute=\"" + pathFileAbsolute + "\""
                        + " pathParent=\"" + pathParent + "\""
                        + " Throwable=\"" + ex.toString() + "\"");
                return false;
            }
            if (!Files.exists(pathParentDir)) {
                // It should never happend, but ...
                LOGGER.error("Cannot create Parent directories."
                        + " FilePath=\"" + pathFile + "\""
                        + " FilePathAbsolute=\"" + pathFileAbsolute + "\""
                        + " pathParent=\"" + pathParent + "\""
                        + " pathParentDir=\"" + pathParentDir + "\"");
                return false;
            }
            if (!Files.isDirectory(pathParentDir)) {
                // It should never happend, but ...
                LOGGER.error("Parent is not Directories."
                        + " FilePath=\"" + pathFile + "\""
                        + " FilePathAbsolute=\"" + pathFileAbsolute + "\""
                        + " pathParent=\"" + pathParent + "\""
                        + " pathParentDir=\"" + pathParentDir + "\"");
                return false;
            }
            if (!Files.isWritable(pathParentDir)) {
                // It should never happend, but ...
                LOGGER.error("Parent Directories is not writable."
                        + " FilePath=\"" + pathFile + "\""
                        + " FilePathAbsolute=\"" + pathFileAbsolute + "\""
                        + " pathParent=\"" + pathParent + "\""
                        + " pathParentDir=\"" + pathParentDir + "\"");
                return false;
            }
        }
        LOGGER.info("Created Parent directory."
                + " FilePath=\"" + pathFile + "\""
                + " FilePathAbsolute=\"" + pathFileAbsolute + "\""
                + " pathParent=\"" + pathParent + "\"");
        return true;

    }

    // -------------------------------------------------------------------------------------
    // Old implementation - keep for future use
    private static void renameFileToBackupReverted(String strTabID, File file) {

        if (file == null) {
            LOGGER.error("Could not create *bak File for null File."
                    + " TabSelectedID=\"" + strTabID + "\"");
            return;
        }
        String strFilePath = file.getAbsolutePath();
        if (file.isDirectory()) {
            LOGGER.error("Could not create *bak File for directory."
                    + " TabSelectedID=\"" + strTabID + "\""
                    + " FilePath=\"" + strFilePath + "\"");
            return;
        }
        if (!file.exists()) {
            LOGGER.error("Could not create *bak File because File does not exist."
                    + " TabSelectedID=\"" + strTabID + "\""
                    + " FilePath=\"" + strFilePath + "\"");
            return;
        }
        if (!file.isFile()) {
            LOGGER.error("Could not create *bak File because it's not a File."
                    + " TabSelectedID=\"" + strTabID + "\""
                    + " FilePath=\"" + strFilePath + "\"");
            return;
        }

        // Compute FilePath without file extension.
        String strFilePathNoExt;
        int intPos = strFilePath.lastIndexOf(".");
        if (intPos <= 0) {
            strFilePathNoExt = strFilePath;
        } else {
            strFilePathNoExt = strFilePath.substring(0, intPos);
        }

        String strFileNameBackupCount = "";
        int i = 0;
        do {
            String strFilenameBackup = strFilePathNoExt + strFileNameBackupCount + "." + Settings.STR_BACKUP_FILES_EXT;
            File fileBak = new File(strFilenameBackup);
            String strFileFavoritesPathCalc = Settings.caclulateFavoritesPath();
            if (strFilePath.equals(strFileFavoritesPathCalc)) {
                // Always backup Favorites file after editing.
                if (fileBak.exists()) {
                    if (!fileBak.delete()) {
                        LOGGER.error("Could not delete *.bak File Favorites."
                                + " TabSelectedID=\"" + strTabID + "\""
                                + " FilePath=\"" + strFilenameBackup + "\"");
                    }
                }
            }
            if (!fileBak.exists()) {
                if (file.renameTo(fileBak)) {
                    LOGGER.info("Renamed File to *.bak File."
                            + " TabSelectedID=\"" + strTabID + "\""
                            + " FilePath=\"" + strFilePath + "\""
                            + " FilePathBak=\"" + fileBak.getAbsolutePath() + "\"");
                } else {
                    LOGGER.error("Could not rename File to *.bak File."
                            + " TabSelectedID=\"" + strTabID + "\""
                            + " FilePath=\"" + strFilePath + "\""
                            + " FilePathBak=\"" + fileBak.getAbsolutePath() + "\"");
                }
                return;
            }
            i++;
            strFileNameBackupCount = "(" + i + ")";
        } while (i < Settings.INT_BACKUP_FILES_MAX);
        LOGGER.debug("Too many *bak Files exists (" + i + ")."
                + " TabSelectedID=\"" + strTabID + "\""
                + " FilePath=\"" + strFilePath + "\"");
        // Delete oldest backup file.
        String strFilenameBackup = strFilePathNoExt + "." + Settings.STR_BACKUP_FILES_EXT;
        File fileBak = new File(strFilenameBackup);
        if (!fileBak.delete()) {
            LOGGER.error("Could not delete *.bak File."
                    + " TabSelectedID=\"" + strTabID + "\""
                    + " FilePath=\"" + strFilenameBackup + "\"");
        } else {
            // Rename bak files.
            i = 0;
            do {
                i++;
                strFileNameBackupCount = "(" + i + ")";
                String strFilenameBackupNext = strFilePathNoExt + strFileNameBackupCount + "." + Settings.STR_BACKUP_FILES_EXT;
                File fileBakNext = new File(strFilenameBackupNext);

                if (fileBakNext.renameTo(fileBak)) {
                    LOGGER.info("Renamed File to *.bak File."
                            + " TabSelectedID=\"" + strTabID + "\""
                            + " FilePath=\"" + strFilenameBackupNext + "\""
                            + " FilePathBak=\"" + fileBak.getAbsolutePath() + "\"");
                } else {
                    LOGGER.error("Could not rename File to *.bak File."
                            + " TabSelectedID=\"" + strTabID + "\""
                            + " FilePath=\"" + strFilenameBackupNext + "\""
                            + " FilePathBak=\"" + fileBak.getAbsolutePath() + "\"");
                    return;
                }
                fileBak = fileBakNext;
            } while (i < Settings.INT_BACKUP_FILES_MAX - 1);

            // Rename original file to bak file with version number.
            if (file.renameTo(fileBak)) {
                LOGGER.info("Renamed File to *.bak File."
                        + " TabSelectedID=\"" + strTabID + "\""
                        + " FilePath=\"" + file.getAbsolutePath() + "\""
                        + " FilePathBak=\"" + fileBak.getAbsolutePath() + "\"");
                return;
            } else {
                LOGGER.error("Could not rename File to *.bak File."
                        + " TabSelectedID=\"" + strTabID + "\""
                        + " FilePath=\"" + file.getAbsolutePath() + "\""
                        + " FilePathBak=\"" + fileBak.getAbsolutePath() + "\"");
            }
        }
        showMessage(Alert.AlertType.WARNING,
                "Create bak File", "Could not create *.bak file for File \"" + strFilePath + "\"",
                "Too many *bak Files exists (" + i + ").", null, null);
    }

    // -------------------------------------------------------------------------------------
    public static boolean showMessage(Alert.AlertType alertType, String strTitle, String strHeader, String strMessage,
            String strButtonTextYes, String strButtonTextNo) {

        ButtonType btYes = new ButtonType(strButtonTextYes, ButtonBar.ButtonData.OK_DONE);
        ButtonType btNo = new ButtonType(strButtonTextNo, ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(alertType);
        alert.setTitle(strTitle);
        alert.setHeaderText(strHeader);
        alert.setContentText(strMessage);
        //int intBtCount=alert.getButtonTypes().size();
        if ((strButtonTextYes != null && !strButtonTextYes.isEmpty()) || (strButtonTextNo != null && !strButtonTextNo.isEmpty())) {
            alert.getButtonTypes().clear();
        }
        if (strButtonTextYes != null && !strButtonTextYes.isEmpty()) {
            alert.getButtonTypes().add(btYes);
        }
        if (strButtonTextNo != null && !strButtonTextNo.isEmpty()) {
            alert.getButtonTypes().add(btNo);
        }
        LOGGER.debug("Show Alert."
                + " Title=\"" + strTitle + "\""
                + " Header=\"" + strHeader + "\""
                + " Message=\"" + strMessage + "\"");

        boolean booReturn = false;
        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElse(btYes) == btYes) {
            LOGGER.debug("Alert response Yes."
                    + " Title=\"" + strTitle + "\""
                    + " Header=\"" + strHeader + "\""
                    + " Message=\"" + strMessage + "\"");
            booReturn = true;
        }
        return booReturn;
    }

    // -------------------------------------------------------------------------------------
    public static <T extends Node> T lookupNodeByID(Node nodeParent, Class clazzNode, String strNodeID) {

        Node nodeFound = nodeParent.lookup("#" + strNodeID);
        if (nodeFound == null) {
            LOGGER.error("Could not find Node."
                    + " NodeID=\"" + strNodeID + "\""
                    + " ClassNode=\"" + clazzNode + "\"");
        } else {
            String strNodeIdFound = nodeFound.getId();
            Class clazzNodeFound = nodeFound.getClass();
            String strClazzNodeFoundName = clazzNodeFound.getName();

            String strClazzNodeName = clazzNode.getName();

            LOGGER.debug("Found Node by ID."
                    + " NodeID=\"" + strNodeID + "\""
                    + " ClassNode=\"" + clazzNode + "\""
                    + " NodeIdFound=\"" + strNodeIdFound + "\""
                    + " NodeFound=\"" + nodeFound + "\""
                    + " ClassNodeFound=\"" + clazzNodeFound + "\"");
            if (strNodeIdFound != null && !strNodeIdFound.isEmpty()) {
                if (strNodeIdFound.equalsIgnoreCase(strNodeID)
                        && strClazzNodeFoundName.equalsIgnoreCase(strClazzNodeName)
                        && clazzNodeFound.equals(clazzNode)) {
                    return (T) nodeFound;
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------------------
    public static <T extends Node> void changeNodeVisibility(T node, boolean booEnable) {

        node.setVisible(booEnable);
    }

    // -------------------------------------------------------------------------------------
    public static <T extends Node> boolean changeNodeVisibility2(Node nodeParent, Class clazzNode, String strNodeID, boolean booEnable) {

        T node = lookupNodeByID(nodeParent, clazzNode, strNodeID);
        if (node == null) {
            LOGGER.error("Could not find Node."
                    + " NodeID=\"" + strNodeID + "\"");
        } else {
            String strNodeIdFound = node.getId();
            Class clazzNodeFind = node.getClass();
            LOGGER.debug("Change Node Visibility."
                    + " NodeID=\"" + strNodeID + "\""
                    + " ClassNode=\"" + clazzNode + "\""
                    + " NodeIdFound=\"" + strNodeIdFound + "\""
                    + " ClassNodeFind=\"" + clazzNodeFind + "\"");
            if (booEnable) {
                node.setVisible(true);
            } else {
                node.setVisible(false);
            }
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------------------
}
