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

import java.io.IOException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JfxFileContentEditorBottomController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxFileContentEditorBottomController.class);

    // -------------------------------------------------------------------------------------
    private JfxFileContentEditorController jfxEditorController;

    // -------------------------------------------------------------------------------------
    @FXML
    private VBox vboxBottom;

    @FXML
    private HBox hboxBottomSearchResult;

    @FXML
    private HBox hboxBottomFind;

    @FXML
    private HBox hboxBottomReplace;

    @FXML
    private HBox hboxBottomVersion;

    @FXML
    private Label hboxBottomLabelVersion;

    // -------------------------------------------------------------------------------------
    private BorderPane borderPaneEditor;
    private TabPane tabPane;
    private ObservableList<Tab> lstTabs;

    // -------------------------------------------------------------------------------------
    private Label lblBottomFindResult = null;
    private TextField tfBottonFind = null;

    // -------------------------------------------------------------------------------------
    // JFX constructor
    // -------------------------------------------------------------------------------------
    public void initialize() {

        Utils.MAP_NODE_REFS.put(
                Utils.NODE_NAMES.hboxBottomSearchResult.toString(), hboxBottomSearchResult);
        Utils.MAP_NODE_REFS.put(
                Utils.NODE_NAMES.hboxBottomFind.toString(), hboxBottomFind);
        Utils.MAP_NODE_REFS.put(
                Utils.NODE_NAMES.hboxBottomReplace.toString(), hboxBottomReplace);

        hboxBottomSearchResult.managedProperty().bind(hboxBottomSearchResult.visibleProperty());
        hboxBottomFind.managedProperty().bind(hboxBottomFind.visibleProperty());
        hboxBottomReplace.managedProperty().bind(hboxBottomReplace.visibleProperty());

        LOGGER.debug("### Initialize JfxFileContentEditorBottomController."
                + " this=\"" + this + "\""
                + " vboxBottom=\"" + vboxBottom + "\""
                + " hboxBottomSearchResult=\"" + hboxBottomSearchResult + "\""
                + " hboxBottomFind=\"" + hboxBottomFind + "\""
                + " hboxBottomReplace=\"" + hboxBottomReplace + "\""
                + " hboxBottomVersion=\"" + hboxBottomVersion + "\""
                + " hboxBottomLabelVersion=\"" + hboxBottomLabelVersion + "\"");
    }

    // -------------------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------------------
    public void setParentController(JfxFileContentEditorController jfxEditorController) {

        this.jfxEditorController = jfxEditorController;

        this.borderPaneEditor = this.jfxEditorController.borderPaneEditor;

        this.tabPane = this.jfxEditorController.tabPaneEditor;
        this.lstTabs = this.tabPane.getTabs();

        this.hboxBottomLabelVersion.setText(Settings.STR_APP_TITLE + " " + Settings.STR_VERSION);

        LOGGER.debug("### Setting Parent Controller in JfxFileContentEditorBottomController."
                + " this=\"" + this + "\""
                + " jfxEditorController=\"" + this.jfxEditorController + "\""
                + " vboxBottom=\"" + vboxBottom + "\""
                + " hboxBottomSearchResult=\"" + hboxBottomSearchResult + "\""
                + " hboxBottomFind=\"" + hboxBottomFind + "\""
                + " hboxBottomReplace=\"" + hboxBottomReplace + "\""
                + " hboxBottomVersion=\"" + hboxBottomVersion + "\""
                + " hboxBottomLabelVersion=\"" + hboxBottomLabelVersion + "\"");
    }

    // -------------------------------------------------------------------------------------
    // FXML Action Methods
    // -------------------------------------------------------------------------------------
    @FXML
    private void find(ActionEvent actionEvent) throws IOException {

        if (lstTabs.isEmpty()) {
            LOGGER.error("No one file open for editing.");
//            Utils.showMessage(Alert.AlertType.INFORMATION, "Find", "", "No one file open for editing.", null, null);
            return;
        }

        if (!this.findLabelBottomSearchResult()) {
            return;
        }

        String strTextFind = this.findTextFieldFindValue();
        if (strTextFind == null) {
            return;
        }

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        int intFoundCount = fileEditor.find(strTextFind);
        if (intFoundCount < 1) {
            String strErrMsg = "No result found for " + fileEditor.getFilePath();
            LOGGER.info(strErrMsg
                    + " actionEvent=\"" + actionEvent + "\""
                    + " TextFind=\"" + strTextFind + "\"");
            this.lblBottomFindResult.setText(strErrMsg);
            return;
        }
        String strResult;
        if (intFoundCount == 1) {
            strResult = "";
        } else {
            strResult = "s";
        }
        String strFindResult = intFoundCount + " substring" + strResult + " found for " + fileEditor.getFilePath();
        this.lblBottomFindResult.setText(strFindResult);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void replace(ActionEvent actionEvent) throws IOException {

        if (this.lstTabs.isEmpty()) {
            LOGGER.error("No one file open for editing.");
//            Utils.showMessage(Alert.AlertType.INFORMATION, "Find", "", "No one file open for editing.", null, null);
            return;
        }

        if (!this.findLabelBottomSearchResult()) {
            return;
        }

        String strTextFind = this.findTextFieldFindValue();
        if (strTextFind == null) {
            return;
        }

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        if (fileEditor.find(strTextFind) < 1) {
            String strErrMsg = "No result found for " + fileEditor.getFilePath();
            LOGGER.info(strErrMsg
                    + " actionEvent=\"" + actionEvent + "\""
                    + " TextFind=\"" + strTextFind + "\"");
            this.lblBottomFindResult.setText(strErrMsg);
            return;
        }

        String strNodeID = "tfBottomReplace";
        TextField tfTextField = (TextField) Utils.lookupNodeByID(this.borderPaneEditor, TextField.class, strNodeID);
        String strTextReplace = tfTextField.getText();
        LOGGER.debug("Found Replace string."
                + " actionEvent=\"" + actionEvent + "\""
                + " TextReplace=\"" + strTextReplace + "\"");
        if (strTextReplace == null) {
            strTextReplace = "";
        }
        int intFoundCount = fileEditor.replace(strTextFind, strTextReplace);
        if (intFoundCount < 1) {
            String strErrMsg = "No result found for " + fileEditor.getFilePath();
            LOGGER.info(strErrMsg
                    + " actionEvent=\"" + actionEvent + "\""
                    + " TextFind=\"" + strTextFind + "\""
                    + " TextReplace=\"" + strTextReplace + "\"");
            this.lblBottomFindResult.setText(strErrMsg);
            return;
        }
        String strResult;
        if (intFoundCount == 1) {
            strResult = "";
        } else {
            strResult = "s";
        }
        String strFindResult = intFoundCount + " substring" + strResult + " updated in " + fileEditor.getFilePath();
        this.lblBottomFindResult.setText(strFindResult);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void bottomHide(ActionEvent actionEvent) throws IOException {

        Utils.changeNodeVisibility(this.hboxBottomSearchResult, false);
        Utils.changeNodeVisibility(this.hboxBottomFind, false);
        Utils.changeNodeVisibility(this.hboxBottomReplace, false);
    }

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------
    private boolean findLabelBottomSearchResult() {

        if (this.lblBottomFindResult == null) {
            String strNodeID = "lblBottomSearchResult";
            String strNodeClassName = Label.class.getName();
            this.lblBottomFindResult = (Label) Utils.lookupNodeByID(this.borderPaneEditor, Label.class, strNodeID);
            if (this.lblBottomFindResult == null) {
                String strErrMsg = "Could not find Node."
                        + " NodeID=\"" + strNodeID + "\" "
                        + " NodeClassName=\"" + strNodeClassName + "\"";
                LOGGER.info(strErrMsg);
                // TODO: add error message on screen
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------------------
    private String findTextFieldFindValue() {

        if (this.tfBottonFind == null) {
            String strNodeID = "tfBottomFind";
            String strNodeClassName = TextField.class.getName();
            this.tfBottonFind = (TextField) Utils.lookupNodeByID(this.borderPaneEditor, TextField.class, strNodeID);
            if (this.tfBottonFind == null) {
                String strErrMsg = "Could not find Node."
                        + " NodeID=\"" + strNodeID + "\" "
                        + " NodeClassName=\"" + strNodeClassName + "\"";
                LOGGER.info(strErrMsg);
                // TODO: add error message on screen
                return null;
            }
        }
        String strTextFind = this.tfBottonFind.getText();
        if (strTextFind == null || strTextFind.isEmpty()) {
            String strErrMsg = "Find string not set (is null or empty).";
            LOGGER.error(strErrMsg
                    + " TextFind=\"" + strTextFind + "\"");
            if (this.findLabelBottomSearchResult()) {
                this.lblBottomFindResult.setText(strErrMsg);
            }
            return null;
        }
        return strTextFind;
    }
    // -------------------------------------------------------------------------------------

}
