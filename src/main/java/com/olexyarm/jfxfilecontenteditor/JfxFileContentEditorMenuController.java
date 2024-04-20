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

import static com.olexyarm.jfxfilecontenteditor.Utils.showMessage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JfxFileContentEditorMenuController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxFileContentEditorMenuController.class);

    // -------------------------------------------------------------------------------------
    private JfxFileContentEditorController jfxEditorController;
    private BorderPane borderPaneEditor;

    // -------------------------------------------------------------------------------------
    @FXML
    private HBox hboxMenu;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuFile;

    @FXML
    private MenuItem miSaveFile;

    @FXML
    private MenuItem miSaveFileAs;

    @FXML
    private MenuItem miSaveFilesAll;

    @FXML
    private MenuItem miPrint;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuEdit;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuFont;

    // -------------------------------------------------------------------------------------
    @FXML
    private CustomMenuItem menuFontFamily;

    // -------------------------------------------------------------------------------------
    @FXML
    private ListView menuListViewFontFamily;

    // -------------------------------------------------------------------------------------
    @FXML
    private CustomMenuItem menuFontSize;

    // -------------------------------------------------------------------------------------
    @FXML
    private TextField menuTextFielsFontSize;

    // -------------------------------------------------------------------------------------
    @FXML
    private CustomMenuItem menuCharset;

    // -------------------------------------------------------------------------------------
    @FXML
    private ListView menuListViewCharset;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuFavorites;

    //ObservableList<MenuItem> menuItems;
    // -------------------------------------------------------------------------------------
    private TabPane tabPane;
    private ObservableList<Tab> lstTabs;

    // -------------------------------------------------------------------------------------
    // JFX constructor
    // -------------------------------------------------------------------------------------
    public void initialize() {

        LOGGER.debug("### Initialize JfxFileContentEditorMenuController."
                + " this=\"" + this + "\""
                + " hboxMenu=\"" + hboxMenu + "\""
                + " menuFavorites=\"" + menuFavorites + "\"");
    }

    // -------------------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------------------
    public void setParentController(JfxFileContentEditorController jfxEditorController) {

        this.jfxEditorController = jfxEditorController;
        this.borderPaneEditor = this.jfxEditorController.borderPaneEditor;

        this.tabPane = this.jfxEditorController.tabPaneEditor;
        this.lstTabs = this.tabPane.getTabs();

        VBox jfxEditorBottom = this.jfxEditorController.jfxEditorBottom;

        LOGGER.debug("### Setting Parent Controller in JfxFileContentEditorMenuController."
                + " this=\"" + this + "\""
                + " jfxEditorController=\"" + jfxEditorController + "\""
                + " hboxMenu=\"" + hboxMenu + "\""
                + " menuFavorites=\"" + menuFavorites + "\""
                + " borderPaneEditor=\"" + this.borderPaneEditor + "\""
                + " tabPane=\"" + this.tabPane + "\""
                + " lstTabs=\"" + this.lstTabs + "\""
                + " jfxEditorBottom=\"" + jfxEditorBottom + "\""
        );

        Settings.load();
        this.addMenuFontFamily();
        this.addMenuCharset();
        this.addFavorites();
    }

    // -------------------------------------------------------------------------------------
    private void addMenuFontFamily() {

        ObservableList<String> menuItemsFontFamily = this.menuListViewFontFamily.getItems();
        menuItemsFontFamily.addAll(Settings.getObsLstFontFamilies());
    }

    // -------------------------------------------------------------------------------------
    private void addMenuCharset() {

        ObservableList<String> menuItemsCharsets = this.menuListViewCharset.getItems();
        menuItemsCharsets.addAll(Settings.getObsLstFontCharsets());
    }

    // -------------------------------------------------------------------------------------
    private void addFavorites() {

        ObservableList<MenuItem> menuItemsFavorites = this.menuFavorites.getItems();
        if (menuItemsFavorites == null) {
            // Should never happend.
            LOGGER.error("Menu Favorites is empty.");
            return;
        }

        int intMenuItemsCount = menuItemsFavorites.size();
        menuItemsFavorites.remove(3, intMenuItemsCount);

        String strFileFavoritesPath = Settings.caclulateFavoritesPath();
        Path pathFileFavorites = FileSystems.getDefault().getPath(strFileFavoritesPath);
        if (Utils.checkFileExist("addFavorites", pathFileFavorites)) {
            Charset charset = Charset.forName(Settings.STR_CHARSET_CURRENT);
            try (BufferedReader br = Files.newBufferedReader(pathFileFavorites, charset)) {
                int intLineCount = 0;
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    intLineCount++;
                    if (!strLine.isEmpty()) {
                        boolean booFound = false;
                        for (MenuItem item : menuItemsFavorites) {
                            String strMenuFilePath = item.getText();
                            if (strLine.equalsIgnoreCase(strMenuFilePath)) {
                                LOGGER.debug("Favorites FilePath found in Menu"
                                        + " LineNumber=\"" + intLineCount + "\""
                                        + " FileFavoritesPath=\"" + strLine + "\"");
                                booFound = true;
                                break;
                            }
                        }
                        if (booFound) {
                            continue;
                        }
                        final ObservableList<Tab> lstTabsFinal = lstTabs;
                        TabPane tabPaneFinal = tabPane;
                        final int intLineCountFinal = intLineCount;
                        final String strLineFinal = strLine;
                        MenuItem menuItemNew = new MenuItem(strLine);
                        menuItemNew.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                event.consume();
                                if (!Utils.checkNewTabsAllowed(lstTabsFinal)) {
                                    return;
                                }
                                //File file = new File(strLineFinal);
                                Path pathFile = FileSystems.getDefault().getPath(strLineFinal);
                                Tab tab = createNewTab(pathFile);
                                if (tab == null) {
                                    LOGGER.debug("Could not create Tab from Favorites menu."
                                            + " LineNumber=\"" + intLineCountFinal + "\""
                                            + " FileFavoritesPath=\"" + strLineFinal + "\"");
                                    return;
                                }
                                FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
                                fileEditor.openFile();
                                fileEditor.setFont(Settings.getFontDefault());
                                lstTabsFinal.add(tab);
                                tabPaneFinal.getSelectionModel().select(tab);

                                changeMenuVisibility(true);

                                LOGGER.debug("Opened FilePath from Favorites Menu."
                                        + " LineNumber=\"" + intLineCountFinal + "\""
                                        + " FileFavoritesPath=\"" + strLineFinal + "\"");
                            }
                        });
                        menuItemsFavorites.add(menuItemNew);
                        LOGGER.debug("Added FilePath to Favorites Menu."
                                + " LineNumber=\"" + intLineCount + "\""
                                + " FileFavoritesPath=\"" + strLine + "\"");
                    }
                }
            } catch (Throwable t) {
                LOGGER.debug("Could not read Favorites File"
                        + " FileFavoritesPath=\"" + pathFileFavorites + "\""
                        + " Throwable=\"" + t.toString() + "\"");
            }
        } else {
            LOGGER.debug("Creating Favorites File"
                    + " FileFavoritesPath=\"" + pathFileFavorites + "\"");
            Utils.createNewFile(pathFileFavorites);
        }
    }

    // -------------------------------------------------------------------------------------
    // FXML Action Methods
    // -------------------------------------------------------------------------------------
    @FXML
    private void newFile(ActionEvent actionEvent) throws IOException {

        if (!Utils.checkNewTabsAllowed(this.lstTabs)) {
            return;
        }
        LOGGER.debug("New File."
                + " FILES_OPEN_COUNT=\"" + Settings.INT_FILES_OPEN_COUNT_TOTAL);
        Tab tab = this.createNewTab(null);
        if (tab == null) {
            return;
        }
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = Settings.getFontDefault();
        fileEditor.setFont(font);
        fileEditor.newFile();

        this.lstTabs.add(tab);
        this.tabPane.getSelectionModel().select(tab);

        this.changeMenuVisibility(true);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void openFile(ActionEvent actionEvent) throws IOException {

        if (!Utils.checkNewTabsAllowed(this.lstTabs)) {
            return;
        }
        MenuItem menuitemSource = (MenuItem) actionEvent.getSource();
        String strMenuItemId = menuitemSource.getId();

        LOGGER.debug("openFile."
                + " FILES_OPEN_COUNT=" + Settings.INT_FILES_OPEN_COUNT_TOTAL
                + " actionEvent.getSource()=\"" + actionEvent.getSource() + "\""
                + " strMenuItemId=\"" + strMenuItemId + "\""
        );
        actionEvent.consume();

        FileChooser fileChooser = new FileChooser();
        // TODO: Use last open directory and keep it in Settings.
        fileChooser.setInitialDirectory(new File(Settings.STR_DIRECTORY_USER_HOME_PATH));
        fileChooser.setTitle("Select a file to open");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.*", "*.*"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.txt", "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.log", "*.log"));
        //fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.encrypted","*.encrypted"));

        File file = fileChooser.showOpenDialog(this.borderPaneEditor.getScene().getWindow());
        if (file == null) {
            LOGGER.info("Opening File. File is not selected.");
            Utils.showMessage(AlertType.WARNING, "Opening File", "", "File is not selected.", null, null);
            return;
        }
        Path pathFile = file.toPath();
        LOGGER.debug("Opening selected File."
                + " FILES_OPEN_COUNT=" + Settings.INT_FILES_OPEN_COUNT_TOTAL
                + " file=\"" + file + "\""
                + " pathFile=\"" + pathFile + "\"");
        Tab tab = this.createNewTab(pathFile);
        if (tab == null) {
            return;
        }
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        fileEditor.openFile();
        fileEditor.setFont(Settings.getFontDefault());
        this.lstTabs.add(tab);
        this.tabPane.getSelectionModel().select(tab);

        this.changeMenuVisibility(true);

        Settings.INT_FILES_OPEN_COUNT_TOTAL++;
        LOGGER.info("Opened selected File."
                + " FILES_OPEN_COUNT=\"" + Settings.INT_FILES_OPEN_COUNT_TOTAL
                + " pathFile=\"" + pathFile + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void saveFile(ActionEvent actionEvent) throws IOException {

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Can't save File from Tab null.");
            return;
        }
        this.saveFileFromTab(tab);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void saveFileAs(ActionEvent actionEvent) throws IOException {

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Saving file AS before any tab created.");
            return;
        }
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        String strFileName = fileEditor.getFileName();
        String strFilePath = fileEditor.getFilePath();
        String strFileNameExt = fileEditor.getFileExt();
        String strFileDir = fileEditor.getFileDir();

        FileChooser fileChooser = new FileChooser();
        if (strFileNameExt != null && !strFileNameExt.isEmpty()) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Files (*." + strFileNameExt + ")", "*." + strFileNameExt));
        }

        if (strFileDir != null && !strFileDir.isEmpty()) {
            fileChooser.setInitialDirectory(new File(strFileDir));
        } else {
            fileChooser.setInitialDirectory(new File(Settings.STR_DIRECTORY_USER_HOME_PATH));
        }

        File fileSaveAs = fileChooser.showSaveDialog(null);
        String strFileNameAs = fileSaveAs.getName();
        String strFilePathAs = fileSaveAs.getAbsolutePath();
        Path pathFileSaveAs = fileSaveAs.toPath();

        LOGGER.info("Saving file As."
                + " TabId=\"" + strTabId + "\""
                + " FileNameOld=\"" + strFileName + "\""
                + " FilePathOld=\"" + strFilePath + "\""
                + " FileNameAs=\"" + strFileNameAs + "\""
                + " FilePathAs=\"" + strFilePathAs + "\""
                + " pathFileSaveAs=\"" + pathFileSaveAs + "\"");
        if (fileEditor.saveFile(pathFileSaveAs)) {
            strFileNameAs = fileEditor.getFileName();
            tab.setText(strFileNameAs);
            strFilePathAs = fileEditor.getFilePath();
            Tooltip tltp = tab.getTooltip();
            tltp.setText(strFilePathAs);
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void saveFilesAll(ActionEvent actionEvent) throws IOException {

        ObservableList<Tab> tabs = this.tabPane.getTabs();
        if (tabs == null || tabs.isEmpty()) {
            LOGGER.error("Saving file before any tab created.");
            return;
        }
        for (Tab tab : tabs) {
            this.saveFileFromTab(tab);
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void printFile(ActionEvent actionEvent) throws IOException {

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            return;
        }
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        String strFileName = fileEditor.getFileName();
        String strFilePath = fileEditor.getFilePath();

// TODO: move print to Custom Control, or find better way to call PrinterJob.
        StringProperty sspFileContent = fileEditor.getFileContent();
        String strFileContent = sspFileContent.getValue();

        Font font = fileEditor.getFont();
        Text text = new Text(strFileContent);
        text.setFont(font);

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {
            Window window = this.borderPaneEditor.getScene().getWindow();
            boolean booPrinterSelected = printerJob.showPrintDialog(window);
            if (!booPrinterSelected) {
                LOGGER.info("Printing canceled."
                        + " TabId=\"" + strTabId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
                return;
            }
            Printer printer = printerJob.getPrinter();
            String strPrinterName = printer.getName();
            boolean booPageSetup = printerJob.showPageSetupDialog(window);
            if (!booPageSetup) {
                LOGGER.info("Printing canceled."
                        + " TabId=\"" + strTabId + "\""
                        + " PrinterName=\"" + strPrinterName + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
                return;
            }

            boolean booPrinted = printerJob.printPage(text);
            if (booPrinted) {
                printerJob.endJob();
                LOGGER.info("Printed File."
                        + " TabId=\"" + strTabId + "\""
                        + " PrinterName=\"" + strPrinterName + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
            } else {
                LOGGER.error("Printing failed."
                        + " TabId=\"" + strTabId + "\""
                        + " PrinterName=\"" + strPrinterName + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
            }
        } else {
            LOGGER.error("Printing failed, could not create PrinterJob."
                    + " TabId=\"" + strTabId + "\""
                    + " FileName=\"" + strFileName + "\""
                    + " FilePath=\"" + strFilePath + "\"");
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void find(ActionEvent actionEvent) throws IOException {

        if (lstTabs.isEmpty()) {
            Utils.showMessage(AlertType.INFORMATION, "Find", "", "No one file open for editing.", null, null);
            return;
        }

        HBox node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomSearchResult.toString());
        Utils.changeNodeVisibility(node, true);

        node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomFind.toString());
        Utils.changeNodeVisibility(node, true);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void replace(ActionEvent actionEvent) throws IOException {

        if (lstTabs.isEmpty()) {
            Utils.showMessage(AlertType.INFORMATION, "Replace", "", "No one file open for editing.", null, null);
            return;
        }

        HBox node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomSearchResult.toString());
        Utils.changeNodeVisibility(node, true);

        node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomFind.toString());
        Utils.changeNodeVisibility(node, true);

        node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomReplace.toString());
        Utils.changeNodeVisibility(node, true);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void exit(ActionEvent actionEvent) throws IOException {

        boolean booReturn;
        boolean booFileModyfied = Utils.checkAnyoneFileModified(this.lstTabs);
        if (booFileModyfied) {
            booReturn = Utils.showMessage(AlertType.CONFIRMATION,
                    "Exit Confirmation", "There is File modified and not saved. Do you want to exit?",
                    "Click Yes to exit", "Yes", "No");
            if (!booReturn) {
                // do not exit app
                return;
            }
        }
        Platform.exit();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontSelect(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        String strFontFamily;
        double dblFontSize;

        Object source = actionEvent.getSource();
        MenuItem menuItem = (MenuItem) source;
        String strMenuItemId = menuItem.getId();
        switch (strMenuItemId) {
            case "osDefault":
                strFontFamily = Settings.STR_FONT_FAMILY_OS_DEFAULT;
                dblFontSize = Settings.DOUBLE_FONT_SIZE_OS_DEFAULT;
                break;
            case "default":
            default:
                strFontFamily = Settings.STR_FONT_FAMILY_CURRENT;
                dblFontSize = Settings.DOUBLE_FONT_SIZE_CURRENT;
        }

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        String strTabId = tab.getId();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = new Font(strFontFamily, dblFontSize);
        fileEditor.setFont(font);
        LOGGER.info("Changed Font."
                + " TabId=\"" + strTabId + "\""
                + " MenuId=\"" + strMenuItemId + "\""
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    public void charsetSelect(MouseEvent arg0) {

        String strCharset = (String) this.menuListViewCharset.getSelectionModel().getSelectedItem();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        String strTabId = tab.getId();
        LOGGER.debug("Changed Charset."
                + " TabId=\"" + strTabId + "\""
                + " Charset=\"" + strCharset + "\"");
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        fileEditor.setCharsetNameRead(strCharset);
        fileEditor.openFile();
        Path pathFile = fileEditor.getPathFile();
        LOGGER.info("Opened File with new charset."
                + " FILES_OPEN_COUNT=\"" + Settings.INT_FILES_OPEN_COUNT_TOTAL
                + " pathFile=\"" + pathFile + "\""
                + " Charset=\"" + strCharset + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    public void fontFamilySelect(MouseEvent arg0) {

        String strFontFamily = (String) this.menuListViewFontFamily.getSelectionModel().getSelectedItem();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        String strTabId = tab.getId();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = fileEditor.getFont();
        double dblFontSize;
        if (font == null) {
            dblFontSize = Settings.DOUBLE_FONT_SIZE_CURRENT;
        } else {
            dblFontSize = font.getSize();
        }
        font = new Font(strFontFamily, dblFontSize);
        fileEditor.setFont(font);
        LOGGER.info("Changed Font Family."
                + " TabId=\"" + strTabId + "\""
                + " FontFamily=\"" + strFontFamily + "\""
                + " FontSize=" + dblFontSize
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontSizeSelect(ActionEvent actionEvent) {//throws IOException {

        String strFontSize = menuTextFielsFontSize.getText();
        if (strFontSize == null || strFontSize.isEmpty()) {
            return;
        }

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        String strTabId = tab.getId();

        double dblFontSize;
        try {
            dblFontSize = Double.parseDouble(strFontSize);
        } catch (Exception e) {
            LOGGER.error("Incorrect Font size."
                    + " TabId=\"" + strTabId + "\""
                    + " FontSize=\"" + strFontSize + "\""
                    + " Exception=\"" + e.toString() + "\"");
            return;
        }
        if (dblFontSize < Settings.DOUBLE_FONT_SIZE_MIN) {
            dblFontSize = Settings.DOUBLE_FONT_SIZE_MIN;
        } else if (dblFontSize > Settings.DOUBLE_FONT_SIZE_MAX) {
            dblFontSize = Settings.DOUBLE_FONT_SIZE_MAX;
        }
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = fileEditor.getFont();
        String strFontFamily;
        if (font == null) {
            strFontFamily = Settings.STR_FONT_FAMILY_CURRENT;
        } else {
            strFontFamily = font.getFamily();
        }
        font = new Font(strFontFamily, dblFontSize);
        fileEditor.setFont(font);
        LOGGER.info("Changed Font size."
                + " TabId=\"" + strTabId + "\""
                + " FontFamily=\"" + strFontFamily + "\""
                + " FontSize=" + dblFontSize
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void favoritesEdit(ActionEvent actionEvent) throws IOException {

        if (!Utils.checkNewTabsAllowed(this.lstTabs)) {
            return;
        }

        String strFileFavoritesPathCalc = Settings.caclulateFavoritesPath();
        //File fileFavorites = new File(strFileFavoritesPathCalc);
        Path pathFileFavorites = FileSystems.getDefault().getPath(strFileFavoritesPathCalc);
        Tab tab = this.createNewTab(pathFileFavorites);
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        String strFileFavoritesPath = fileEditor.getFilePath();

        if (Utils.checkFileExist(strTabId, pathFileFavorites)) {
            LOGGER.info("Opening Favorites File for Editing."
                    + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
// TODO: check for not-text characters

//TODO: set current font.
            fileEditor.openFile();
            fileEditor.setFont(Settings.getFontDefault());
            this.addFavorites();
        } else {
            //TODO: create file.
            LOGGER.info("Favorites File does not exist."
                    + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
            if (Utils.createFile(pathFileFavorites) > 0) {
                LOGGER.info("Favorites File created."
                        + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
            }
        }

        this.lstTabs.add(tab);
        this.tabPane.getSelectionModel().select(tab);

        this.changeMenuVisibility(true);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void favoritesAdd(ActionEvent actionEvent) throws IOException {

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Could not add File to Favorites before any tab created.");
            return;
        }
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        boolean booFileModified = fileEditor.isFileModified();
        if (booFileModified) {
            fileEditor.saveFile(null);
        }

        String strFileFavoritesPath = Settings.caclulateFavoritesPath();
        Path pathFileFavorites = FileSystems.getDefault().getPath(strFileFavoritesPath);
        String strFilePathToAdd = fileEditor.getFilePath();

        if (!Utils.checkFileExist(strTabId, pathFileFavorites)) {
            LOGGER.info("Create Favorites File."
                    + " TabId=\"" + strTabId + "\""
                    + " strFileFavoritesPath=\"" + strFileFavoritesPath + "\""
                    + " pathFileFavorites=\"" + pathFileFavorites + "\"");
            if (Utils.createFile(pathFileFavorites) > 0) {
                if (!Utils.checkFileExist(strTabId, pathFileFavorites)) {
                    LOGGER.error("Could not create Favorites File."
                            + " TabId=\"" + strTabId + "\""
                            + " strFileFavoritesPath=\"" + strFileFavoritesPath + "\""
                            + " pathFileFavorites=\"" + pathFileFavorites + "\"");
                    return;
                }
            }
        }

        // Check if File Path is in Favorites list already.
        //try (BufferedReader br = new BufferedReader(new FileReader(strFileFavoritesPath))) {
        Charset charset = Charset.forName(Settings.STR_CHARSET_CURRENT);
        try (BufferedReader br = Files.newBufferedReader(pathFileFavorites, charset)) {
            int intLineCount = 0;
            String strLine;
            while ((strLine = br.readLine()) != null) {
                intLineCount++;
                if (!strLine.isEmpty()) {
                    if (strLine.equals(strFilePathToAdd)) {
                        LOGGER.info("FilePath found in Favorites File."
                                + " TabId=\"" + strTabId + "\""
                                + " LineNumber=\"" + intLineCount + "\""
                                + " FilePath=\"" + strFilePathToAdd + "\""
                                + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
                        return;
                    }
                }
            }
        }
        LOGGER.debug("Adding FilePath to Favorites File."
                + " TabId=\"" + strTabId + "\""
                + " FilePath=\"" + strFilePathToAdd + "\""
                + " FileFavoritesPathCalc=\"" + strFileFavoritesPath + "\""
                + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");

        try (BufferedWriter bw = Files.newBufferedWriter(pathFileFavorites, charset, StandardOpenOption.APPEND)) {
            bw.append(strFilePathToAdd);
            bw.append(System.lineSeparator()); //"\n");
        } catch (Throwable t) {
            LOGGER.error("Could not add FilePath to Favorites File."
                    + " TabId=\"" + strTabId + "\""
                    + " FilePath=\"" + strFilePathToAdd + "\""
                    + " FileFavoritesPath=\"" + strFileFavoritesPath + "\""
                    + " Throwable=\"" + t.toString() + "\"");
        }
        LOGGER.info("Added FilePath to Favorites File."
                + " TabId=\"" + strTabId + "\""
                + " FilePath=\"" + strFilePathToAdd + "\""
                + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
        this.addFavorites();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void settingsEdit(ActionEvent actionEvent) throws IOException {

        Settings.load();

        Scene scene = new Scene(Utils.loadFXML("jfxEditorSettings"), Settings.INT_WINDOW_SETTINGS_WIDTH, Settings.INT_WINDOW_ABOUT_HIGH);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Settings");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void about(ActionEvent actionEvent) throws IOException {

        Scene scene = new Scene(Utils.loadFXML("jfxEditorAbout"), Settings.INT_WINDOW_ABOUT_WIDTH, Settings.INT_WINDOW_SETTINGS_HIGH);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("About");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    // -------------------------------------------------------------------------------------
    // Helpers 
    // -------------------------------------------------------------------------------------
    private Tab createNewTab(Path pathFile) {

        Settings.INT_TABS_COUNT_OPENED_TOTAL++;
        Settings.INT_TABS_COUNT_OPEN++;
        String strTabId = "" + Settings.INT_TABS_COUNT_OPENED_TOTAL;

        if (pathFile == null) {
            Settings.INT_FILES_NEW_COUNT++;
            pathFile = Paths.get(Settings.STR_DIRECTORY_USER_HOME_PATH, Settings.STR_JFX_EDITOR_SETTINGS_DIRECTORY,
                    Settings.STR_NEW_FILENAME_DEFAULT + Settings.INT_FILES_NEW_COUNT + "." + Settings.STR_FILENAME_EXT_DEFAULT);
        }

        FileContentEditor fileEditor = new FileContentEditor(strTabId, pathFile);
        String strFilePath = fileEditor.getFilePath();
        String strFileName = fileEditor.getFileName();
        String strFileNameExt = fileEditor.getFileExt();

        Tab tab = new Tab(strFileName, fileEditor);
        tab.setId(strTabId);

        Tooltip tltp = new Tooltip(strFilePath);
        tab.setTooltip(tltp);

        tab.setClosable(true);
        tab.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {

                // Don't do that here:event.consume();
                EventType eventType = event.getEventType();
                LOGGER.debug("Closing Tab."
                        + " TabId=\"" + strTabId + "\""
                        + " TabsOpenedTotal=" + Settings.INT_TABS_COUNT_OPENED_TOTAL
                        + " TabsOpen=" + Settings.INT_TABS_COUNT_OPEN
                        + " TabsClosed=" + Settings.INT_TABS_COUNT_CLOSED
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\""
                        + " FileNameExt=\"" + strFileNameExt + "\""
                        + " eventType=\"" + eventType + "\"");
                Settings.INT_TABS_COUNT_OPEN--;
                Settings.INT_TABS_COUNT_CLOSED++;
                if (Settings.INT_TABS_COUNT_OPEN == 0) {
                    changeMenuVisibility(false);

                    HBox node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomSearchResult.toString());
                    Utils.changeNodeVisibility(node, false);

                    node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomFind.toString());
                    Utils.changeNodeVisibility(node, false);

                    node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomReplace.toString());
                    Utils.changeNodeVisibility(node, false);
                }
                boolean booModified = fileEditor.isFileModified();
                if (!booModified) {
                    return;
                }
                if (!showMessage(Alert.AlertType.WARNING, "Saving File on Close",
                        "The File was modified and not saved.\nFilePath=" + strFilePath,
                        "Do you want to save file?", "Yes", "No")) {
                    LOGGER.info("Saving modified File while closing Tab was denied."
                            + " TabId=\"" + strTabId + "\""
                            + " FileName=\"" + strFileName + "\""
                            + " FilePath=\"" + strFilePath + "\""
                            + " FileNameExt=\"" + strFileNameExt + "\""
                            + " eventType=\"" + eventType + "\"");
                    return;
                }
                LOGGER.debug("Saving File while closing Tab."
                        + " TabId=\"" + strTabId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\""
                        + " FileNameExt=\"" + strFileNameExt + "\""
                        + " eventType=\"" + eventType + "\"");
                fileEditor.saveFile(null);
            }
        });

        LOGGER.info("Created Tab."
                + " TabId=\"" + strTabId + "\""
                + " FileName=\"" + strFileName + "\""
                + " FilePath=\"" + strFilePath + "\""
                + " FileNameExt=\"" + strFileNameExt + "\"");
        return tab;
    }

    // ---------------------------------------------------------------------------
    private void saveFileFromTab(Tab tab) {

        if (tab == null) {
            LOGGER.error("Can't save File from Tab null.");
            return;
        }
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        boolean booFileModified = fileEditor.isFileModified();
        if (!booFileModified) {
            String strTabId = tab.getId();
            String strFilePath = fileEditor.getFilePath();
            LOGGER.debug("No needs to save existing File if it was not modified."
                    + " TabId=\"" + strTabId + "\""
                    + " FilePath=\"" + strFilePath + "\"");
            return;
        }
        fileEditor.saveFile(null);
    }

    // -------------------------------------------------------------------------------------
    private void changeMenuVisibility(boolean booVisible) {

        this.miSaveFile.setVisible(booVisible);
        this.miSaveFileAs.setVisible(booVisible);
        this.miSaveFilesAll.setVisible(booVisible);
        this.menuEdit.setVisible(booVisible);
        this.menuFont.setVisible(booVisible);
        this.miPrint.setVisible(booVisible);
    }
    // -------------------------------------------------------------------------------------
}
