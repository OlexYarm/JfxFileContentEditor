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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileContentEditor extends Control {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileContentEditor.class);
    private static final int INT_PROGRESS_BAR_STEPS = 10;

    private Path pathFile;
    private String strFilePath;
    private String strFileName;
    private String strFileExt;
    private String strFileDir;
    private int intPosFoundFromCursor;
    private boolean booBinary;
    private String strCharsetNameRead;
    private String strCharsetNameWrite;

    private final BooleanProperty bpHboxStateVisibility = new SimpleBooleanProperty();
    private final BooleanProperty bpFileModified = new SimpleBooleanProperty(false);
    private final StringProperty objStrPropFileState = new SimpleStringProperty();

    private final StringProperty spFileContent = new SimpleStringProperty();
    private final ObjectProperty<Cursor> cursorProperty = new SimpleObjectProperty<>(Cursor.DEFAULT);
    private final IntegerProperty objIntPropCaretPosition = new SimpleIntegerProperty();

    private final ObjectProperty objectPropTaskFileLoad = new SimpleObjectProperty();
    private Task<String> taskFileLoad;

    private final ObjectProperty objectPropServiceFileSave = new SimpleObjectProperty();
    private Service<String> serviceFileSave;

    private Font font;
    private final ObjectProperty opFont = new SimpleObjectProperty();

    // -------------------------------------------------------------------------------------
    // Construstors
    // -------------------------------------------------------------------------------------
    public FileContentEditor(final String strId, final Path pathFile) {

        getStyleClass().add("fileContentEditor.css");

        setId(strId);
        this.pathFile = pathFile;
        this.parseFilePath(strId, pathFile);
        this.intPosFoundFromCursor = 0;
        this.objIntPropCaretPosition.set(this.intPosFoundFromCursor);
        this.booBinary = false;
        this.font = Font.getDefault();

        this.strCharsetNameRead = "utf-8";
        this.strCharsetNameWrite = "utf-8";
        if (pathFile != null) {
            this.requestFocus();
            this.setFocusTraversable(true);
            getStyleClass().add("fileContentEditor");
            LOGGER.debug("## Created FileContentEditor."
                    + " Id=\"" + strId + "\""
                    + " FilePath=\"" + strFilePath + "\""
            );
        }
    }

    // -------------------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------------------
    @Override
    protected Skin<?> createDefaultSkin() {
        LOGGER.debug("## Create createDefaultSkin."
                + " Id=\"" + this.getId() + "\""
                + " FilePath=\"" + strFilePath + "\""
        );
        return new FileContentEditorSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return FileContentEditor.class.getResource("fileContentEditor.css").toExternalForm();
    }

    // -------------------------------------------------------------------------------------
    public void newFile() {

        if (this.pathFile == null) {
            LOGGER.error("Could not open file null.");
            return;
        }
        this.bpHboxStateVisibility.set(false);
        LOGGER.debug("# New File.");
    }

    // -------------------------------------------------------------------------------------
    public void openFile() {

        LOGGER.debug("# openFile."
                + " pathFile=\"" + this.pathFile + "\""
                + " Binary=" + this.booBinary
                + " strCharsetNameRead=\"" + this.strCharsetNameRead + "\"");
        if (this.pathFile == null) {
            LOGGER.error("Could not open file null.");
            return;
        }

        this.bpHboxStateVisibility.set(true);

        this.taskFileLoad = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Read text file.
                updateMessage("File loading started.");
                int intLinesTotal = 0;
                StringBuilder sb = new StringBuilder();
// TODO: change charset to read bad files.
                Charset charset = Charset.forName(strCharsetNameRead);
                CharsetDecoder cd = charset.newDecoder();
                cd.onMalformedInput(CodingErrorAction.REPLACE);
                cd.onUnmappableCharacter(CodingErrorAction.REPLACE);
                cd.replaceWith("?");

                LOGGER.debug("Using charset."
                        + " pathFile=\"" + pathFile + "\""
                        + " Charset=\"" + charset + "\""
                        + " CharsetDecoder=\"" + cd + "\""
                        + " cd.malformedInputAction()=\"" + cd.malformedInputAction() + "\""
                        + " cd.unmappableCharacterAction()=\"" + cd.unmappableCharacterAction() + "\""
                        + " cd.replacement()=\"" + cd.replacement() + "\""
                );

                try (BufferedReader reader = Files.newBufferedReader(FileContentEditor.this.pathFile, charset)) {
                    String strLine;
                    while ((strLine = reader.readLine()) != null) {
                        intLinesTotal++;
                    }
                    LOGGER.debug("Read File to calculate number of lines."
                            + " pathFile=\"" + pathFile + "\""
                            + " intLinesTotal=" + intLinesTotal);
                } catch (Throwable t) {
                    updateMessage("File loading failed with Charset \"" + charset + "\""
                            + " (" + t.toString() + ").");
                    LOGGER.error("Could not Open or Read File to calculate number of lines."
                            + " pathFile=\"" + pathFile + "\""
                            + " Throwable=\"" + t.toString() + "\"");
                    throw new Exception("Read File failed." + " Throwable=\"" + t.toString() + "\"");
                }
                /*
                BufferedReader reader = new BufferedReader(new FileReader(FileContentEditor.this.file));
                //Use Files.lines() to calculate total lines - used for progress
                try (Stream<String> stream = Files.lines(FileContentEditor.this.pathFile)) {
                    intLinesTotal = stream.count();
                }
                 */
                updateProgress(0, intLinesTotal);

                int intUpdateProgressLinesStep = intLinesTotal / INT_PROGRESS_BAR_STEPS;
                LOGGER.debug("Loading file."
                        + " Id=\"" + getId() + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " LinesTotal=" + intLinesTotal
                        + " UpdateProgressLinesStep=" + intUpdateProgressLinesStep);

                //Load all lines one by one into a StringBuilder separated by System.lineSeparator() ("\n") - compatible with TextArea
                String strLine;
                StringBuilder sbFileContent = new StringBuilder();
                long lngLinesLoaded = 0;
                long lngBytesReadTotal = 0;
                int intLinesCounter = 0;
                int intUpdateProgressCounter = 0;
                try (BufferedReader reader = Files.newBufferedReader(pathFile, charset)) {
                    while ((strLine = reader.readLine()) != null) {
                        sbFileContent.append(strLine);
                        sbFileContent.append(System.lineSeparator()); //"\n");
                        int intBytesRead = strLine.length();
                        lngBytesReadTotal += intBytesRead;
                        ++lngLinesLoaded;
                        ++intLinesCounter;
                        if (intUpdateProgressLinesStep == 0 || intLinesCounter >= intUpdateProgressLinesStep) {
                            intLinesCounter = 0;
                            ++intUpdateProgressCounter;
                            updateProgress(lngLinesLoaded, intLinesTotal);
                            updateMessage("File Loading " + " steps=" + intUpdateProgressCounter * INT_PROGRESS_BAR_STEPS + " bytes=" + lngBytesReadTotal);
                            // updateValue(sb.toString()); for testing - it works.
                            LOGGER.debug("# Loading file progress."
                                    + " Id=\"" + getId() + "\""
                                    + " FileName=\"" + FileContentEditor.this.strFileName + "\""
                                    + " UpdateProgressCounter=" + intUpdateProgressCounter
                                    + " LinesLoaded=" + lngLinesLoaded
                                    + " LinesTotal=" + intLinesTotal
                                    + " BytesRead=" + intBytesRead
                                    + " BytesReadTotal=" + lngBytesReadTotal);
                        }
                        /*
                        // For testing only !
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException interrupted) {
                            if (isCancelled()) {
                                updateMessage("Cancelled");
                                break;
                            }
                        }
                         */
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            break;
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("Could not Read File."
                            + " pathFile=\"" + pathFile + "\""
                            + " Throwable=\"" + t.toString() + "\"");
                }
                updateMessage("File Loaded (" + lngBytesReadTotal + " bytes).");
                LOGGER.debug("Loaded file."
                        + " Id=\"" + getId() + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " intLinesTotal=" + intLinesTotal
                        + " lngLinesLoaded=" + lngLinesLoaded
                        + " lngBytesReadTotal=" + lngBytesReadTotal);

                return sbFileContent.toString();
            }
        };
        this.objectPropTaskFileLoad.set(this.taskFileLoad);

        Skin<?> skin = this.getSkin();
        if (skin != null) {
            LOGGER.debug("# openFile-Task starting."
                    + " Id=\"" + getId() + "\""
                    + " task=\"" + taskFileLoad + "\"");
            new Thread(taskFileLoad).start();
            LOGGER.debug("# openFile-Task started."
                    + " Id=\"" + getId() + "\""
                    + " task=\"" + taskFileLoad + "\"");
        } else {
            LOGGER.debug("# FileContentEditor Skin is null.");
            this.skinProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable o) {
                    skinProperty().removeListener(this);
                    LOGGER.debug("# openFile-Task starting."
                            + " Id=\"" + getId() + "\""
                            + " Observable=\"" + o + "\""
                            + " task=\"" + taskFileLoad + "\"");
                    new Thread(taskFileLoad).start();
                    LOGGER.debug("# openFile-Task started."
                            + " Id=\"" + getId() + "\""
                            + " task=\"" + taskFileLoad + "\"");
                }
            });
        }

//        //Platform.runLater(this.taskFileLoad);
//        Platform.runLater(new Thread(this.taskFileLoad));
//        //new Thread(this.taskFileLoad).start();
    }

    // -------------------------------------------------------------------------------------
    public void openFileBinary() {

        this.booBinary = true;
        this.openFile();
    }

    // -------------------------------------------------------------------------------------
    public boolean saveFile(Path pathFileSaveAs) {
        // Parameters:
        // pathFileSaveAs == null for saving new of existing file.
        // pathFileSaveAs != null for saving File As.

        LOGGER.debug("Saving File.");
        this.bpHboxStateVisibility.set(true);

        if (pathFileSaveAs == null) {
            String strReason = canSaveFile(this.getId(), this.pathFile);
            if (strReason != null) {
                objStrPropFileState.setValue(strReason);
                LOGGER.debug("Could not save File."
                        + " pathFile=\"" + pathFile + "\""
                        + " strReason=\"" + strReason + "\"");
                return false;
            }
        } else {
            this.pathFile = pathFileSaveAs;
            this.parseFilePath(this.getId(), this.pathFile);
        }

        if (Settings.BOO_BACKUP_FILES_EABLED) {
//Old            renameFileToBackupReverted(strTabID, fileToSave);
            renameFileToBackup(this.getId(), this.pathFile);
        }

        this.serviceFileSave = new Service<>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws InterruptedException {
                        LOGGER.debug("File Save started."
                                + " pathFile=\"" + pathFile + "\"");
                        updateMessage("File Save started.");
                        String strText = spFileContent.getValue();
                        if (strText == null) {
                            LOGGER.info("Saving null string."
                                    + " pathFile=\"" + pathFile + "\"");
                            strText = "";
                        }
                        int intLen = strText.length();
                        updateProgress(0, intLen);
                        LOGGER.info("File saving."
                                + " Id=\"" + getId() + "\""
                                + " pathFile=\"" + pathFile + "\""
                                + " intLen=" + intLen);
                        //int intSteps = intLen / INT_PROGRESS_BAR_STEPS + 1;
                        int intStep = intLen / INT_PROGRESS_BAR_STEPS;
                        int intFrom = 0;
                        LOGGER.debug("Saving file."
                                + " Id=\"" + getId() + "\""
                                + " pathFile=\"" + pathFile + "\""
                                + " intLen=" + intLen
                                + " intStep=" + intStep);
                        Charset charset = Charset.forName(Settings.STR_CHARSET_CURRENT);
                        try (BufferedWriter writer = Files.newBufferedWriter(pathFile, charset)) {
                            //writer.write(s, 0, s.length());
                            if (intStep == 0) {
                                writer.write(strText, 0, intLen);
                            } else {
                                while (intFrom <= intLen - intStep) {
                                    writer.write(strText, intFrom, intStep);
                                    intFrom += intStep;
                                    updateProgress(intFrom, intLen);
                                    LOGGER.debug("Saving file."
                                            + " Id=\"" + getId() + "\""
                                            + " pathFile=\"" + pathFile + "\""
                                            + " intLen=" + intLen
                                            + " intFrom=" + intFrom);
                                    /*
                                    // For testing only !
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException interrupted) {
                                        if (isCancelled()) {
                                            updateMessage("Cancelled");
                                            break;
                                        }
                                    }
                                     */
                                }
                                int intRest = intLen - intFrom;
                                if (intRest != 0) {
                                    writer.write(strText, intFrom, intRest);
                                }
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Could not save file."
                                    + " Id=\"" + getId() + "\""
                                    + " pathFile=\"" + pathFile + "\""
                                    + " Throwable=\"" + t.toString() + "\"");
                        }
                        /*
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(strFilePath))) {
                            writer.write(strText);
                        } catch (Exception e) {
                            LOGGER.error("Could not save file."
                                    + " Id=\"" + getId() + "\""
                                    + " pathFile=\"" + pathFile + "\""
                                    + " Exception=\"" + e.toString() + "\"");
                            return null;
                        }
                         */
                        updateProgress(intLen, intLen);
                        updateMessage("File Save finished (" + intLen + " bytes).");
                        LOGGER.debug("File Save finished(" + intLen + " bytes)."
                                + " pathFile=\"" + pathFile + "\"");
                        return "OK";
                    }
                };
            }
        };

        this.objectPropServiceFileSave.set(this.serviceFileSave);

        this.serviceFileSave.reset();
        LOGGER.info("Saving file."
                + " Id=\"" + this.getId() + "\""
                + " pathFile=\"" + this.pathFile + "\"");
        this.serviceFileSave.start();

        if (pathFileSaveAs != null) {
            // Update Tab after File Save AS.
/*
            userData = new UserData(strTabID, fileSaveAs); //, false);
            String strFileName = userData.getFileName();
            tab.setUserData(userData);
            tab.setText(strFileName);
            Tooltip tltp = new Tooltip(strFilePath);
            tab.setTooltip(tltp);
             */
        }
        this.bpFileModified.set(false);
        return true;
    }

    // -------------------------------------------------------------------------------------
    public int find(String strTextFind) {

        if (strTextFind == null || strTextFind.isEmpty()) {
            return -1;
        }
        if (this.spFileContent == null) {
            return -1;
        }
        String strText = this.spFileContent.getValue();
        if (strText == null || strText.isEmpty()) {
            return -1;
        }
        // Count all findings.
        int intTextFindLen = strTextFind.length();
        int intPosLast = 0;
        int intCount = 0;
        int intPosFirst = -1;
        while (intPosLast != -1) {
            intPosLast = strText.indexOf(strTextFind, intPosLast);
            if (intPosLast != -1) {
                if (intPosFirst == -1) {
                    // Save position of first text found.
                    intPosFirst = intPosLast;
                }
                intCount++;
                intPosLast += intTextFindLen;
            }
        }
        if (intCount == 0) {
            return 0;
        }
        if (intCount == 1) {
            this.intPosFoundFromCursor = intPosFirst;
        } else {
            // Find text from cursor position.
            int intTextLen = strText.length();
            int intPosCursor = this.objIntPropCaretPosition.getValue();
            int intPosFindStart;
            if (intPosCursor < intTextLen - intTextFindLen) {
                intPosFindStart = intPosCursor + 1;
            } else {
                intPosFindStart = 0;
            }
            this.intPosFoundFromCursor = strText.indexOf(strTextFind, intPosFindStart);
            if (this.intPosFoundFromCursor < 0) {
                intPosFindStart = 0;
                this.intPosFoundFromCursor = strText.indexOf(strTextFind, intPosFindStart);
            }
            if (this.intPosFoundFromCursor < 0) {
                this.intPosFoundFromCursor = 0;
            }
        }
        LOGGER.debug("Text findings."
                + " Id=\"" + this.getId() + "\""
                + " TextFind=\"" + strTextFind + "\""
                + " Count=\"" + intCount + "\""
                + " PosFoundFromCursor=\"" + this.intPosFoundFromCursor + "\"");

        this.objIntPropCaretPosition.set(this.intPosFoundFromCursor);
        return intCount;
    }

    // -------------------------------------------------------------------------------------
    public int replace(String strTextFind, String strTextReplace) {

        if (strTextFind == null || strTextFind.isEmpty()) {
            return -1;
        }
        if (strTextReplace == null || strTextReplace.isEmpty()) {
            return -1;
        }
        if (this.spFileContent == null) {
            return -1;
        }
        String strFileContent = this.spFileContent.getValue();
        if (strFileContent == null || strFileContent.isEmpty()) {
            return -1;
        }
        int intFileContentLen = strFileContent.length();

        int intTextFindLen = strTextFind.length();
        int intPos = strFileContent.indexOf(strTextFind, this.intPosFoundFromCursor);
        if (intPos < 0) {

            return 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(strFileContent.substring(0, intPos));
        sb.append(strTextReplace);
        if (intFileContentLen > intPos + intTextFindLen) {
            sb.append(strFileContent.substring(intPos + intTextFindLen));
        }
        String strTextUpdated = sb.toString();
        this.spFileContent.set(strTextUpdated);
        this.objIntPropCaretPosition.set(this.intPosFoundFromCursor);
        return 1;
    }

    // -------------------------------------------------------------------------------------
    public long getFileSize() {

        if (this.pathFile == null) {
            return 0;
        } else {
            long lngFileSize;
            try {
                lngFileSize = Files.size(pathFile);
            } catch (IOException ex) {
                LOGGER.error("Could not get File size."
                        + " Id=\"" + this.getId() + "\""
                        + " pathFile=\"" + this.pathFile + "\""
                        + " IOException=\"" + ex.toString() + "\"");
                return 0;
            }
            return lngFileSize;
            // TODO: test why it does not work for some files.
            //return this.file.length();
/*
            Path pathFile = Paths.get(this.getFilePath());
            FileChannel fileChannel;
            try {
                fileChannel = FileChannel.open(pathFile);
                long lngFileSize = fileChannel.size();
                return lngFileSize;
            } catch (IOException ex) {
            }
             */
        }
    }

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------
    private void parseFilePath(final String strId, Path pathFile) {

        if (pathFile == null) {
            this.strFilePath = "";
            this.strFileName = "";
            this.strFileExt = "";
            this.strFileDir = "";
            LOGGER.error("File Path is null."
                    + " Id=\"" + strId + "\"");
        } else {
            //this.pathFileName = pathFile.getFileName();
            this.strFilePath = pathFile.toString();
            final int intFileNamePos = this.strFilePath.lastIndexOf(File.separator);
            if (intFileNamePos < 0) {
                this.strFileDir = "";
                this.strFileName = strFilePath;
            } else {
                this.strFileDir = this.strFilePath.substring(0, intFileNamePos);
                this.strFileName = this.strFilePath.substring(intFileNamePos + 1);
            }

            final int intFileNameExtPos = strFileName.lastIndexOf(".");
            final String strFileNameExt;
            if (intFileNameExtPos < 0 || intFileNameExtPos == 0 || intFileNameExtPos == this.strFileName.length()) {
                strFileNameExt = "";
            } else {
                strFileNameExt = this.strFileName.substring(intFileNameExtPos + 1);
            }
            this.strFileExt = strFileNameExt;
            LOGGER.info("Parsed File."
                    + " Id=\"" + strId + "\""
                    + " FilePath=\"" + this.pathFile + "(" + this.strFilePath + ")\""
                    + " FileNamePos=\"" + intFileNamePos + "\""
                    + " FileName=\"" + this.strFileName + "\""
                    + " FileDir=\"" + this.strFileDir + "\""
                    + " FileNameExtPos=\"" + intFileNameExtPos + "\""
                    + " FileNameExt=\"" + this.strFileExt + "\"");
        }
    }

    // -------------------------------------------------------------------------------------
    private static String canSaveFile(String strId, Path pathFile) {

        if (pathFile == null) {
            LOGGER.error("Cannot save null File."
                    + " TabId=\"" + strId + "\"");
            return "File is null";
        }

        if (!Files.exists(pathFile)) {
            try {
                Path pathNewFile = Files.createFile(pathFile);
                LOGGER.info("Create New File."
                        + " TabId=\"" + strId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " pathNewFile=\"" + pathNewFile + "\"");
            } catch (IOException ex) {
                LOGGER.error("Could not create File."
                        + " TabId=\"" + strId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " IOException=\"" + ex.toString() + "\"");
                return "Create File exception:" + ex.toString();
            }
        }
        try {
            if (Files.isDirectory(pathFile)) {
                // It should never happen, but ...
                LOGGER.error("File is directory."
                        + " TabId=\"" + strId + "\""
                        + " pathFile=\"" + pathFile + "\"");
                return "File is directory";
            }
            if (Files.isWritable(pathFile)) {
                return null;
            } else {
                if (Files.isReadable(pathFile)) {
                    return "Could not save Read-only File";
                }
                return "Could not save not writable File";
            }
        } catch (Throwable t) {
            LOGGER.error("Could not analize File because of security violation."
                    + " TabId=\"" + strId + "\""
                    + " pathFile=\"" + pathFile + "\""
                    + " Throwable=\"" + t.toString() + "\"");
            return "File save exception:" + t.toString();
        }
    }

    // -------------------------------------------------------------------------------------
    private static void renameFileToBackup(String strTabId, Path pathFile) {

        if (pathFile == null) {
            LOGGER.error("Could not create *bak File for null File."
                    + " TabId=\"" + strTabId + "\"");
            return;
        }

        if (Files.isDirectory(pathFile)) {
            LOGGER.error("Could not create *bak File for directory."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\"");
            return;
        }
        if (!Files.exists(pathFile)) {
            LOGGER.error("Could not create *bak File because File does not exist."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\"");
            return;
        }
        if (!Files.isRegularFile(pathFile)) {
            LOGGER.error("Could not create *bak File because it's not a Regular File."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\"");
            return;
        }
        if (Settings.BOO_BACKUP_FILES_DAILY_ONLY) {
            FileTime ft;
            try {
                ft = Files.getLastModifiedTime(pathFile);
            } catch (IOException ex) {
                LOGGER.error("Could not get getLastModifiedTime."
                        + " TabId=\"" + strTabId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " IOException=\"" + ex.toString() + "\"");
                return;
            }
            long lngFileModifiedDays = ft.to(TimeUnit.DAYS);

            LocalDate localDate = LocalDate.now();
            long lngLocalDateEpochDay = localDate.toEpochDay();

            LOGGER.debug("Compare File Modified Days and Current Day."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\""
                    + " FileTime=\"" + ft + "\""
                    + " lngFileModifiedDays=\"" + lngFileModifiedDays + "\""
                    + " localDate=\"" + localDate + "\""
                    + " lngLocalDateEpochDay=\"" + lngLocalDateEpochDay + "\"");
            if (lngFileModifiedDays - lngLocalDateEpochDay >= 0) {
                LOGGER.debug("Skip updating backup files."
                        + " TabId=\"" + strTabId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " lngFileModifiedDays=\"" + lngFileModifiedDays + "\""
                        + " lngLocalDateEpochDay=\"" + lngLocalDateEpochDay + "\"");
                return;
            }
        }

        // Compute FilePath string without file extension.
        String strFilePath = pathFile.toString();
        String strFilePathNoExt;
        int intPos = strFilePath.lastIndexOf(".");
        if (intPos <= 0) {
            strFilePathNoExt = strFilePath;
        } else {
            strFilePathNoExt = strFilePath.substring(0, intPos);
        }

// TODO:  Always backup Favorites file after editing.
        boolean booFileBackupOldest = true;
        //String strFilenameBackupOld = null;
        //File fileBackupOld = null;
        //File fileBackup = null;
        Path pathFileBackupOld = null;
        Path pathFileBackup = null;
        for (int i = Settings.INT_BACKUP_FILES_MAX - 1; i >= 0; i--) {
            String strFileNameBackupCount;
            if (i == 0) {
                strFileNameBackupCount = "";
            } else {
                strFileNameBackupCount = "(" + i + ")";
            }
            String strFilenameBackup = strFilePathNoExt + strFileNameBackupCount + "." + Settings.STR_BACKUP_FILES_EXT;
            pathFileBackup = FileSystems.getDefault().getPath(strFilenameBackup);
            //fileBackup = new File(strFilenameBackup);
            //if (fileBackup.exists()) {
            if (Files.exists(pathFileBackup)) {
                if (booFileBackupOldest) {
                    try {
                        //if (!fileBackup.delete()) {
                        Files.deleteIfExists(pathFileBackup);
                    } catch (Throwable t) {
                        LOGGER.error("Could not delete oldest *.bak File."
                                + " TabId=\"" + strTabId + "\""
                                + " FilePathBak=\"" + pathFileBackup + "\""
                                + " Throwable=\"" + t.toString() + "\"");
                        return;
                    }
                    //}
                } else {
                    try {
                        Files.move(pathFileBackup, pathFileBackupOld);
                    } catch (Throwable t) {
                        LOGGER.error("Could not rename *.bak File."
                                + " TabId=\"" + strTabId + "\""
                                + " pathFile=\"" + pathFile + "\""
                                + " pathFileBackup=\"" + pathFileBackup + "\""
                                + " pathFileBackupOld=\"" + pathFileBackupOld + "\""
                                + " Throwable=\"" + t.toString() + "\"");
                        return;
                    }
                    //fileBackup.renameTo(fileBackupOld);
                    LOGGER.debug("Renamed *.bak File."
                            + " TabId=\"" + strTabId + "\""
                            + " pathFile=\"" + pathFile + "\""
                            + " pathFileBackup=\"" + pathFileBackup + "\""
                            + " pathFileBackupOld=\"" + pathFileBackupOld + "\"");
                }
            }
            booFileBackupOldest = false;
            //strFilenameBackupOld = strFilenameBackup;
            //fileBackupOld = fileBackup;
            pathFileBackupOld = pathFileBackup;
        }
        try {
            //file.renameTo(fileBackup);
            Files.move(pathFile, pathFileBackup);
        } catch (Throwable t) {
            LOGGER.error("Could not rename File to *.bak File."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\""
                    + " pathFileBackup=\"" + pathFileBackup + "\""
                    + " Throwable=\"" + t.toString() + "\"");
            return;
        }
        LOGGER.debug("Renamed *.bak File."
                + " TabId=\"" + strTabId + "\""
                + " pathFile=\"" + pathFile + "\""
                + " pathFileBackup=\"" + pathFileBackup + "\"");
    }

    // -------------------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------------------
    public final Path getPathFile() {
        return this.pathFile;
    }

    public final String getFilePath() {
        return this.strFilePath;
    }

    public final String getFileName() {
        return this.strFileName;
    }

    public String getFileExt() {
        return this.strFileExt;
    }

    public final String getFileDir() {
        return this.strFileDir;
    }

    public final boolean isFileModified() {
        if (this.pathFile == null) {
            return false;
        } else {
            return this.bpFileModified.getValue();
        }
    }

    public BooleanProperty getFileModified() {
        return this.bpFileModified;
    }

    public StringProperty getFileContent() {
        return this.spFileContent;
    }

    // -------------------------------------------------------------------------------------
    public String getCharsetNameRead() {
        return this.strCharsetNameRead;
    }

    public void setCharsetNameRead(String strCharsetNameRead) {
        this.strCharsetNameRead = strCharsetNameRead;
    }

    public String getCharsetNameWrite() {
        return this.strCharsetNameWrite;
    }

    public void setCharsetNameWrite(String strCharsetWrite) {
        this.strCharsetNameWrite = strCharsetWrite;
    }

    // -------------------------------------------------------------------------------------
    public Font getFont() {
        return (Font) this.opFont.getValue();
    }

    public void setFont(Font font) {
        this.opFont.set(font);
    }

    public ObjectProperty getOpFont() {
        return this.opFont;
    }

    // -------------------------------------------------------------------------------------
    public ObjectProperty<Cursor> getCursorProperty() {
        return this.cursorProperty;
    }

    public IntegerProperty getIntCaretPosition() {
        return this.objIntPropCaretPosition;
    }

    public ObjectProperty getOpTaskFileLoad() {
        return this.objectPropTaskFileLoad;
    }

    public ObjectProperty getOpServiceFileSave() {
        return this.objectPropServiceFileSave;
    }

    public Task getTask() {
        return this.taskFileLoad;
    }

    public BooleanProperty getHboxStateVisibility() {

        LOGGER.debug("# Call getHboxStateVisibility."
                + " Id=\"" + this.getId() + "\"");
        return this.bpHboxStateVisibility;
    }

    public StringProperty getSpFileState() {

        LOGGER.debug("# Call getSpFileState."
                + " Id=\"" + this.getId() + "\"");
        return this.objStrPropFileState;
    }

    // -------------------------------------------------------------------------------------
}
