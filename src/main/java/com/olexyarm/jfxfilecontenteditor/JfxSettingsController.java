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
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JfxSettingsController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxSettingsController.class);

    @FXML
    public GridPane gridPaneSettings;

    @FXML
    public RadioButton settingsBackupEnable;

    @FXML
    public RadioButton settingsBackupDisable;

    @FXML
    public TextField settingsBackupNum;

    @FXML
    public TextField settingsTabsNum;

    @FXML
    public TextField settingsFontSize;

    @FXML
    public ListView settingsFontFamily;

    // -------------------------------------------------------------------------------------
    @Override
    @FXML
    public void initialize(URL url, ResourceBundle rb) {

        LOGGER.debug("### JfxSettingsController initialize.");

        // -------------------------------------------------------------------------------------
        // Backup Files Enable/Disable
        final ToggleGroup grpBackupFiles = new ToggleGroup();
        settingsBackupEnable.setToggleGroup(grpBackupFiles);
        settingsBackupDisable.setToggleGroup(grpBackupFiles);

        if (Settings.BOO_BACKUP_FILES_EABLED) {
            settingsBackupEnable.setToggleGroup(grpBackupFiles);
            settingsBackupEnable.setSelected(true);
            settingsBackupEnable.setFocusTraversable(true);
        } else {
            settingsBackupDisable.setToggleGroup(grpBackupFiles);
            settingsBackupDisable.setSelected(true);
            settingsBackupDisable.setFocusTraversable(true);
        }

        grpBackupFiles.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                LOGGER.debug("Changed settings Backup-Files."
                        + " ov=" + ov
                        + " old_toggle=" + old_toggle
                        + " new_toggle=" + new_toggle);
                if (new_toggle != null) {
                    RadioButton rb = (RadioButton) new_toggle;
                    String strID = rb.getId();
                    if (strID == null) {
                        LOGGER.info("Could not get RadioButton ID of settings Backup-Files."
                                + " ov=" + ov
                                + " old_toggle=" + old_toggle
                                + " new_toggle=" + new_toggle);
                    } else {
                        if (strID.equalsIgnoreCase("settingsBackupEnable")) {
                            settingsBackupEnable.setSelected(true);
                            settingsBackupEnable.setFocusTraversable(true);
                            Settings.BOO_BACKUP_FILES_EABLED = true;
                            settingsBackupNum.setDisable(false);
                            LOGGER.info("Changed settings Backup-Files Enable."
                                    + " ov=" + ov
                                    + " old_toggle=" + old_toggle
                                    + " new_toggle=" + new_toggle
                                    + " ID=" + strID);
                        } else if (strID.equalsIgnoreCase("settingsBackupDisable")) {
                            settingsBackupDisable.setSelected(true);
                            settingsBackupDisable.setFocusTraversable(true);
                            Settings.BOO_BACKUP_FILES_EABLED = false;
                            settingsBackupNum.setDisable(true);
                            LOGGER.info("Changed settings Backup-Files Disable."
                                    + " ov=" + ov
                                    + " old_toggle=" + old_toggle
                                    + " new_toggle=" + new_toggle
                                    + " ID=" + strID);
                        }
                    }
                }
            }
        });

        // -------------------------------------------------------------------------------------
        // Backup Files Nunber
        settingsBackupNum.setText("" + Settings.INT_BACKUP_FILES_MAX);
        if (Settings.BOO_BACKUP_FILES_EABLED == false) {
            settingsBackupNum.setDisable(true);
        }
        settingsBackupNum.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    if (newValue != null && !newValue.isEmpty()) {
                        int intNewValue = Integer.parseInt(newValue);
                        Settings.INT_BACKUP_FILES_MAX = intNewValue;
                        LOGGER.info("Changed settings Backup-Files Number."
                                + " observable=" + observable
                                + " oldValue=\"" + oldValue + "\""
                                + " newValue=\"" + newValue + "\"");
                    }
                } catch (Exception e) {
                    //observable.setValue(oldValue);
                    LOGGER.error("Couls not changed settings Backup-Files Number."
                            + " observable=" + observable
                            + " oldValue=\"" + oldValue + "\""
                            + " newValue=\"" + newValue + "\""
                            + "Exception=\"" + e.toString() + "\"");
                    settingsBackupNum.setText(oldValue);
                }
            }
        });

        // -------------------------------------------------------------------------------------
        // Tabs Number
        settingsTabsNum.setText("" + Settings.INT_TABS_COUNT_MAX);
        settingsTabsNum.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    if (newValue != null && !newValue.isEmpty()) {
                        int intNewValue = Integer.parseInt(newValue);
                        Settings.INT_TABS_COUNT_MAX = intNewValue;
                        LOGGER.info("Changed settings Tabs Number."
                                + " observable=" + observable
                                + " oldValue=\"" + oldValue + "\""
                                + " newValue=\"" + newValue + "\"");
                    }
                } catch (Exception e) {
                    //observable.setValue(oldValue);
                    LOGGER.error("Could not changed settings Tabss Number."
                            + " observable=" + observable
                            + " oldValue=\"" + oldValue + "\""
                            + " newValue=\"" + newValue + "\""
                            + "Exception=\"" + e.toString() + "\"");
                    settingsTabsNum.setText(oldValue);
                }
            }
        });

        // -------------------------------------------------------------------------------------
        settingsFontSize.setText("" + Settings.DOUBLE_FONT_SIZE_CURRENT);
        /*
        settingsFontSize.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!Settings.changeFontSize(newValue)) {
                    settingsFontSize.setText(oldValue);
                }
            }
        });
         */
        // -------------------------------------------------------------------------------------
        settingsFontFamily.setItems(Settings.getObsLstFontFamilies());
        settingsFontFamily.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Settings.STR_FONT_FAMILY_CURRENT = newValue;
                LOGGER.info("Salected Font Family."
                        + " FontFamily=" + newValue);
            }
        });
        // -------------------------------------------------------------------------------------
    }

    // -------------------------------------------------------------------------------------
    // Methodss
    // -------------------------------------------------------------------------------------
    @FXML
    private void settingsSave(ActionEvent actionEvent) throws IOException {

        fontSizeSelect(null);
        Settings.save();
        LOGGER.info("Saved settings.");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontSizeSelect(ActionEvent actionEvent) {//throws IOException {

        String strFontSize = settingsFontSize.getText();
        if (strFontSize == null || strFontSize.isEmpty()) {
            return;
        }
        double dblFontSize;
        try {
            dblFontSize = Double.parseDouble(strFontSize);
        } catch (Exception e) {
            LOGGER.error("Incorrect Font size."
                    + " strFontSize=\"" + strFontSize + "\""
                    + " Exception=\"" + e.toString() + "\"");
            return;
        }
        if (dblFontSize < Settings.DOUBLE_FONT_SIZE_MIN) {
            dblFontSize = Settings.DOUBLE_FONT_SIZE_MIN;
        } else if (dblFontSize > Settings.DOUBLE_FONT_SIZE_MAX) {
            dblFontSize = Settings.DOUBLE_FONT_SIZE_MAX;
        }
        Settings.DOUBLE_FONT_SIZE_CURRENT = dblFontSize;

        settingsFontSize.setText("" + dblFontSize);
        LOGGER.info("Changed Font size."
                + " FontSize=" + dblFontSize);
    }

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
}
