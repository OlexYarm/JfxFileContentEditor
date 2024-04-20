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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {

    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

    public static String STR_VERSION;
    public static String STR_BUILD_TIME;
    public static String STR_BUILD_JAVA_HOME;
    public static String STR_BUILD_OS;

    private static final String STR_VERSION_FILENAME = "version.txt";
    private static final String STR_TAB_NAME_ABOUT = "about";

    private static final List<String> LST_FONT_FAMILIES = Font.getFamilies();
    private static final ObservableList<String> OBS_LST_VIEW_FONT_FAMILIES = FXCollections.observableArrayList(LST_FONT_FAMILIES);

    public static final Font FONT_OS_DEFAULT = Font.getDefault();
    public static final String STR_FONT_FAMILY_OS_DEFAULT = FONT_OS_DEFAULT.getFamily();
    public static final double DOUBLE_FONT_SIZE_OS_DEFAULT = FONT_OS_DEFAULT.getSize();

    public static SortedMap<String, Charset> MAP_CHARSETS_AVAILABLE = Charset.availableCharsets();
    public static final Charset CHARSET_OS_DEFAULT = Charset.defaultCharset();
    public static final String STR_CHARSET_OS_DEFAULT = CHARSET_OS_DEFAULT.name();
    public static final String STR_CHARSET_DEFAULT = "UTF-8";

    static {
        STR_VERSION = "unknown";
        STR_BUILD_TIME = "unknown";
        URL urlResourceVersionFile = Settings.class.getResource(STR_VERSION_FILENAME);
        if (urlResourceVersionFile == null) {
            LOGGER.error("Could not find Version file URL."
                    + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\"");
        } else {
            URI uriFileVersion;
            try {
                uriFileVersion = urlResourceVersionFile.toURI();
                try {
                    Path pathFileVersion = Paths.get(uriFileVersion);
                    if (!Utils.checkFileExist(STR_TAB_NAME_ABOUT, pathFileVersion)) {
                        LOGGER.error("Version file does not exist."
                                + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\""
                                + " uriFileVersion=\"" + uriFileVersion + "\"");
                    } else {
                        String strVersionFileContent = Utils.readTextFileToString(pathFileVersion);
                        if (strVersionFileContent == null) {
                            LOGGER.error("Version file is empty."
                                    + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\""
                                    + " uriFileVersion=\"" + uriFileVersion + "\""
                                    + " pathFileVersion=\"" + pathFileVersion + "\"");
                        } else {
                            int intPosVersion = strVersionFileContent.indexOf("Build.version");
                            int intPosBuildDate = strVersionFileContent.indexOf("Build.date");
                            int intPosJavaHome = strVersionFileContent.indexOf("Build.JavaHome");
                            int intPosOs = strVersionFileContent.indexOf("Build.OS");

                            if (intPosVersion >= 0 && intPosBuildDate >= 0 && intPosJavaHome > 0 && intPosOs > 0) {
                                STR_VERSION = strVersionFileContent.substring(intPosVersion, intPosBuildDate - 1);
                                STR_BUILD_TIME = strVersionFileContent.substring(intPosBuildDate, intPosJavaHome - 1);
                                STR_BUILD_JAVA_HOME = strVersionFileContent.substring(intPosJavaHome, intPosOs - 1);
                                STR_BUILD_OS = strVersionFileContent.substring(intPosOs);
                                LOGGER.info("Parsed Version file."
                                        + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\""
                                        + " uriFileVersion=\"" + uriFileVersion + "\""
                                        + " pathFileVersion=\"" + pathFileVersion + "\""
                                        + " strVersionFileContent=\"" + strVersionFileContent + "\""
                                        + " STR_VERSION=\"" + STR_VERSION + "\""
                                        + " STR_BUILD_TIME=\"" + STR_BUILD_TIME + "\""
                                        + " STR_BUILD_JAVA_HOME=\"" + STR_BUILD_JAVA_HOME + "\""
                                        + " STR_BUILD_OS=\"" + STR_BUILD_OS + "\"");
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("Could not get Path for Version file."
                            + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\""
                            + " uriFileVersion=\"" + uriFileVersion + "\""
                            + " Throwable=\"" + t.toString() + "\"");
                }
            } catch (Throwable t) {
                LOGGER.error("Could not get URI for Version file."
                        + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\""
                        + " urlResourceVersionFile=\"" + urlResourceVersionFile + "\""
                        + " Throwable=\"" + t.toString() + "\"");
            }
        }
    }

// -------------------------------------------------------------------------------------
// Unmodifiable settings
// -------------------------------------------------------------------------------------
    public static final String STR_DIRECTORY_USER_HOME = "user.home";
    public static String STR_DIRECTORY_USER_HOME_PATH = System.getProperty(STR_DIRECTORY_USER_HOME);

    public static final String STR_DIRECTORY_USER_DIR = "user.dir";
    public static String STR_DIRECTORY_USER_HOME_DIR = System.getProperty(STR_DIRECTORY_USER_DIR);

    public static final String STR_APP_TITLE = "Open JFX File Content Editor";
    public static final String STR_JFX_EDITOR_SETTINGS_DIRECTORY = "JfxEditor";

    // -------------------------------------------------------------------------------------
    public static int INT_WINDOW_ABOUT_WIDTH = 350;
    public static int INT_WINDOW_ABOUT_HIGH = 300;

    // -------------------------------------------------------------------------------------
    public static int INT_WINDOW_SETTINGS_WIDTH = 300;
    public static int INT_WINDOW_SETTINGS_HIGH = 300;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_WINDOW_WIDTH = "window_widh";
    public static int INT_WINDOW_WIDTH = 800;
    public static int INT_WINDOW_WIDTH_DEFAULT = 800;
    public static int INT_WINDOW_WIDTH_MAX = 8000;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_WINDOW_HIGH = "window_high";
    public static int INT_WINDOW_HIGH = 600;
    public static int INT_WINDOW_HIGH_DEFAULT = 600;
    public static int INT_WINDOW_HIGH_MAX = 4000;

    // -------------------------------------------------------------------------------------
    public static int INT_TABS_COUNT_OPENED_TOTAL = 0;
    public static int INT_TABS_COUNT_OPEN = 0;
    public static int INT_TABS_COUNT_CLOSED = 0;
    public static int INT_FILES_NEW_COUNT = 0;
    public static int INT_FILES_OPEN_COUNT_TOTAL = 0;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_BACKUP_FILES_EXT = "BackupFiles_ext";
    public static String STR_BACKUP_FILES_EXT = "bak";

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FILE_FAVORITES_NAME = "Favorites";
    public static final String STR_FILE_FAVORITES_NAME = "Favorites";
    public static final String STR_FILE_FAVORITES_EXT = "txt";

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FILE_FAVORITES_DIR = "Favorites_path";
    public static final String STR_FILE_FAVORITES_DIR = "";

    // -------------------------------------------------------------------------------------
    public static final String STR_NEW_FILENAME_DEFAULT = "new file";

    // -------------------------------------------------------------------------------------
    public static final String STR_FILENAME_EXT_DEFAULT = "txt";

    // -------------------------------------------------------------------------------------
    private static final String STR_SETTINGS_FILE_NAME = "Settings.properties";
    private static final String STR_SETTINGS_FILE_PATH = Settings.STR_DIRECTORY_USER_HOME_PATH
            + File.separator + STR_JFX_EDITOR_SETTINGS_DIRECTORY
            + File.separator + STR_SETTINGS_FILE_NAME;

    // -------------------------------------------------------------------------------------
    // Modifiable settings
    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_BACKUP_FILES_ENABLED = "BakupFiles_enabled";
    public static boolean BOO_BACKUP_FILES_EABLED = true;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FILEBACKUP_MAX = "BackupFiles_max";
    private static final int INT_BACKUP_FILES_MAX_MAX = 500;
    public static int INT_BACKUP_FILES_MAX = 3;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_TABS_MAX = "Tabs_max";
    private static final int INT_TABS_COUNT_MAX_MAX = 50;
    public static int INT_TABS_COUNT_MAX = 3;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_LOG_LEVEL = "Log_level";
    public static String STR_LOG_LEVEL = "D";
// TODO: add implementation.

    // -------------------------------------------------------------------------------------
    private static Font FONT_CURRENT;
    // -------------------------------------------------------------------------------------
    public static final double DOUBLE_FONT_SIZE_MAX = 36;
    public static final double DOUBLE_FONT_SIZE_MIN = 8;
    public static final double DOUBLE_FONT_SIZE_DEFAULT = 22;
    private static final String STR_PROP_NAME_FONT_SIZE_CURRENT = "Font_size_current";
    public static double DOUBLE_FONT_SIZE_CURRENT;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FONT_FAMILY_CURRENT = "Font_family_current";
    public static String STR_FONT_FAMILY_CURRENT;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_CHARSET_CURRENT = "Charset_current";
    public static String STR_CHARSET_CURRENT;

    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
    private static Properties prop = new Properties();

    // -------------------------------------------------------------------------------------
    public static void load() {

        Path pathFileSettings = FileSystems.getDefault().getPath(STR_SETTINGS_FILE_PATH);

        if (!Utils.checkFileExist("SettingsLoad", pathFileSettings)) {
            LOGGER.info("File Settings does not exist.");
            Utils.createFile(pathFileSettings);
            return;
        }

        File fileSettings = new File(STR_SETTINGS_FILE_PATH);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileSettings));) {
            prop.load(bis);
        } catch (Exception ex) {
            LOGGER.error("Could not open Settings file."
                    + " pathFileSettings=\"" + pathFileSettings + "\""
                    + " Exception=\"" + ex.toString() + "\"");
            return;
        }

        String strPropValue;
        try {
            INT_WINDOW_WIDTH = getPropValueInt(STR_PROP_NAME_WINDOW_WIDTH, "" + INT_WINDOW_WIDTH_DEFAULT, INT_WINDOW_WIDTH_MAX);
            INT_WINDOW_HIGH = getPropValueInt(STR_PROP_NAME_WINDOW_HIGH, "" + INT_WINDOW_HIGH_DEFAULT, INT_WINDOW_HIGH_MAX);

            INT_TABS_COUNT_MAX = getPropValueInt(STR_PROP_NAME_TABS_MAX, "" + INT_TABS_COUNT_MAX, INT_TABS_COUNT_MAX_MAX);

            strPropValue = prop.getProperty(STR_PROP_NAME_BACKUP_FILES_EXT);
            if (strPropValue == null) {
                LOGGER.debug("Could not find property \"" + STR_PROP_NAME_BACKUP_FILES_EXT + "\"");
            } else {
                STR_BACKUP_FILES_EXT = strPropValue;
            }

            BOO_BACKUP_FILES_EABLED = getPropValueBoolean(STR_PROP_NAME_BACKUP_FILES_ENABLED, BOO_BACKUP_FILES_EABLED ? "Y" : "N");
            if (strPropValue == null) {
                LOGGER.debug("Could not find property \"" + STR_PROP_NAME_BACKUP_FILES_ENABLED + "\"");
            } else {
                STR_BACKUP_FILES_EXT = strPropValue;
            }

            INT_BACKUP_FILES_MAX = getPropValueInt(STR_PROP_NAME_FILEBACKUP_MAX, "" + INT_BACKUP_FILES_MAX, INT_BACKUP_FILES_MAX_MAX);

            DOUBLE_FONT_SIZE_CURRENT = getPropValueDouble(STR_PROP_NAME_FONT_SIZE_CURRENT, "" + DOUBLE_FONT_SIZE_OS_DEFAULT, DOUBLE_FONT_SIZE_MAX);

            strPropValue = prop.getProperty(STR_PROP_NAME_FONT_FAMILY_CURRENT);
            if (strPropValue == null) {
                LOGGER.debug("Could not find property \"" + STR_PROP_NAME_FONT_FAMILY_CURRENT + "\"");
                STR_FONT_FAMILY_CURRENT = STR_FONT_FAMILY_OS_DEFAULT;
            } else {
                STR_FONT_FAMILY_CURRENT = strPropValue;
            }

            FONT_CURRENT = new Font(STR_FONT_FAMILY_CURRENT, DOUBLE_FONT_SIZE_CURRENT);

            strPropValue = prop.getProperty(STR_PROP_NAME_CHARSET_CURRENT);
            if (strPropValue == null) {
                LOGGER.debug("Could not find property \"" + STR_PROP_NAME_CHARSET_CURRENT + "\"");
                STR_CHARSET_CURRENT = STR_CHARSET_DEFAULT;
            } else {
                STR_CHARSET_CURRENT = strPropValue;
            }

        } catch (Exception ex) {
            LOGGER.error("Could not process property."
                    + " IOException=\"" + ex.toString() + "\"");
        }

    }

    // -------------------------------------------------------------------------------------
    public static void save() {

        prop.setProperty(STR_PROP_NAME_WINDOW_WIDTH, "" + INT_WINDOW_WIDTH);
        prop.setProperty(STR_PROP_NAME_WINDOW_HIGH, "" + INT_WINDOW_HIGH);

        prop.setProperty(STR_PROP_NAME_BACKUP_FILES_EXT, STR_BACKUP_FILES_EXT);

        prop.setProperty(STR_PROP_NAME_BACKUP_FILES_ENABLED, BOO_BACKUP_FILES_EABLED ? "Y" : "N");
        if (INT_BACKUP_FILES_MAX <= 0) {
            INT_BACKUP_FILES_MAX = 1;
        }
        if (INT_BACKUP_FILES_MAX > INT_BACKUP_FILES_MAX_MAX) {
            INT_BACKUP_FILES_MAX = INT_BACKUP_FILES_MAX_MAX;
        }
        prop.setProperty(STR_PROP_NAME_FILEBACKUP_MAX, "" + INT_BACKUP_FILES_MAX);

        if (INT_TABS_COUNT_MAX <= 0) {
            INT_TABS_COUNT_MAX = 1;
        }
        if (INT_TABS_COUNT_MAX > INT_TABS_COUNT_MAX_MAX) {
            INT_TABS_COUNT_MAX = INT_TABS_COUNT_MAX_MAX;
        }
        prop.setProperty(STR_PROP_NAME_TABS_MAX, "" + INT_TABS_COUNT_MAX);

        FONT_CURRENT = new Font(STR_FONT_FAMILY_CURRENT, DOUBLE_FONT_SIZE_CURRENT);
        prop.setProperty(STR_PROP_NAME_FONT_SIZE_CURRENT, "" + DOUBLE_FONT_SIZE_CURRENT);
        prop.setProperty(STR_PROP_NAME_FONT_FAMILY_CURRENT, STR_FONT_FAMILY_CURRENT);

        prop.setProperty(STR_PROP_NAME_CHARSET_CURRENT, STR_CHARSET_CURRENT);

        try {
            OutputStream os = new FileOutputStream(STR_SETTINGS_FILE_PATH);
            prop.store(os, "");
        } catch (IOException ex) {
            LOGGER.error("Could not save Properties file."
                    + " IOException=\"" + ex.toString() + "\"");
        }

    }

    // -------------------------------------------------------------------------------------
    public static String caclulateFavoritesPath() {

        String strFileFavoritesPath;
        if (Settings.STR_FILE_FAVORITES_DIR == null || Settings.STR_FILE_FAVORITES_DIR.isEmpty()) {
            strFileFavoritesPath = Settings.STR_DIRECTORY_USER_HOME_PATH
                    + File.separator + Settings.STR_JFX_EDITOR_SETTINGS_DIRECTORY
                    + File.separator + Settings.STR_FILE_FAVORITES_NAME;
        } else {
            strFileFavoritesPath = Settings.STR_FILE_FAVORITES_DIR + File.separator + Settings.STR_FILE_FAVORITES_NAME;
        }
        strFileFavoritesPath += "." + Settings.STR_FILE_FAVORITES_EXT;

        return strFileFavoritesPath;
    }

    // -------------------------------------------------------------------------------------
    public static String osName() {
        return System.getProperty("os.name");
    }

    // -------------------------------------------------------------------------------------
    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    // -------------------------------------------------------------------------------------
    public static String javafxVersion() {
        return System.getProperty("javafx.version");
    }

    // -------------------------------------------------------------------------------------
    private static boolean getPropValueBoolean(String strPropName, String strPropValueDefault) throws Exception {

        String strPropValue = prop.getProperty(strPropName, strPropValueDefault);
        boolean booValue;
        if (strPropValue == null) {
            strPropValue = strPropValueDefault;
        }
        switch (strPropValue) {
            case "Y":
            case "YES":
                booValue = true;
                break;
            case "N":
            case "NO":
                booValue = false;
                break;
            default:
                booValue = true;
        }
        return booValue;
    }

    // -------------------------------------------------------------------------------------
    private static int getPropValueInt(String strPropName, String strPropValueDefault, int IntPropValueMax) throws Exception {

        String strPropValue = prop.getProperty(strPropName, strPropValueDefault);
        if (strPropValue == null) {
            LOGGER.debug("Could not find property \"" + strPropName + "\"");
            strPropValue = strPropValueDefault;
        }
        int intPropValue = Integer.parseInt(strPropValue);
        if (intPropValue <= 0 || intPropValue > IntPropValueMax) {
            LOGGER.error("Properties value is incorrrect."
                    + " PropName=\"" + strPropName + "\""
                    + " IntPropValueMax=\"" + IntPropValueMax + "\""
                    + " strPropValue=\"" + strPropValue + "\""
                    + " intPropValue=\"" + intPropValue + "\"");
            throw new Exception("Properties value is incorrrect.");
        }
        LOGGER.debug("Got int Properties value."
                + " PropName=\"" + strPropName + "\""
                + " IntPropValueMax=\"" + IntPropValueMax + "\""
                + " strPropValue=\"" + strPropValue + "\""
                + " intPropValue=\"" + intPropValue + "\"");
        return intPropValue;
    }

    // -------------------------------------------------------------------------------------
    private static double getPropValueDouble(String strPropName, String strPropValueDefault, double dblPropValueMax) throws Exception {

        String strPropValue = prop.getProperty(strPropName, strPropValueDefault);
        if (strPropValue == null) {
            LOGGER.debug("Could not find property \"" + strPropName + "\"");
            strPropValue = strPropValueDefault;
        }
        double dblPropValue = Double.parseDouble(strPropValue);
        if (dblPropValue <= 0 || dblPropValue > dblPropValueMax) {
            LOGGER.error("Properties value is incorrrect."
                    + " PropName=\"" + strPropName + "\""
                    + " dblPropValueMax=\"" + dblPropValueMax + "\""
                    + " strPropValue=\"" + strPropValue + "\""
                    + " dblPropValue=\"" + dblPropValue + "\"");
            throw new Exception("Properties value is incorrrect.");
        }
        LOGGER.debug("Got double Properties value."
                + " PropName=\"" + strPropName + "\""
                + " dblPropValueMax=\"" + dblPropValueMax + "\""
                + " strPropValue=\"" + strPropValue + "\""
                + " dblPropValue=\"" + dblPropValue + "\"");
        return dblPropValue;
    }

    // -------------------------------------------------------------------------------------
    public static boolean changeFontSize(String strFontSize) {

        double dblFontSize;
        try {
            dblFontSize = Double.parseDouble(strFontSize);
            if (dblFontSize <= DOUBLE_FONT_SIZE_MIN) {
                LOGGER.debug("Font size is too small."
                        + " dblFontSize=" + dblFontSize
                        + " DOUBLE_FONT_SIZE_MIN=" + DOUBLE_FONT_SIZE_MIN);
                return false;
            } else if (dblFontSize > Settings.DOUBLE_FONT_SIZE_MAX) {
                LOGGER.debug("Font size is too large."
                        + " dblFontSize=" + dblFontSize
                        + " DOUBLE_FONT_SIZE_MAX=" + DOUBLE_FONT_SIZE_MAX);
                return false;
            } else {
                DOUBLE_FONT_SIZE_CURRENT = dblFontSize;
                save();
                LOGGER.info("Font size changed."
                        + " dblFontSize=" + dblFontSize);
                return true;
            }

        } catch (NumberFormatException e) {
            LOGGER.error("Could not changed settings Font Size Default."
                    + " strFontSize=\"" + strFontSize + "\""
                    + " NumberFormatException=\"" + e.toString() + "\"");
            return false;
        }

    }

    // -------------------------------------------------------------------------------------
    public static Font getFontDefault() {

        return FONT_CURRENT;
    }

    // -------------------------------------------------------------------------------------
    public static ObservableList<String> getObsLstFontFamilies() {
        return OBS_LST_VIEW_FONT_FAMILIES;
    }

    public static ObservableList<String> getObsLstFontCharsets() {

        Set<String> keyset = MAP_CHARSETS_AVAILABLE.keySet();
        List<String> lst = new ArrayList<>(keyset);
        ObservableList<String> ol = FXCollections.observableList(lst);
        return ol;
    }

    // -------------------------------------------------------------------------------------
}
