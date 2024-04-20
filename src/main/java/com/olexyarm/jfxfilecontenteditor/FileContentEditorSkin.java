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

import java.util.concurrent.ExecutionException;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileContentEditorSkin extends SkinBase<FileContentEditor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileContentEditorSkin.class);

    private final FileContentEditor control;

    private final String strId;
    private final String strFileName;

    private final VBox vbox;
    private final TextArea textArea;
    private final StringProperty spFileContent;
    private final InvalidationListener invalidationListenerFileContent;

    private final ReadOnlyIntegerProperty caretPositionProperty;
    private final ChangeListener<Number> caretChangeListener;

    private final IntegerProperty objIntPropCaretPosition = new SimpleIntegerProperty();
    private final ChangeListener<Number> objIntPropCaretPositionChangeListener;

    private final ReadOnlyBooleanProperty focusedProperty;
    private final ChangeListener<Boolean> focusedPropertyChangeListener;

    private final HBox hboxState;
    private final BooleanProperty bpHboxStateVisibility;
    private final Label lblFileState;
    private final Label lblFileName;
    private final ProgressBar progressBar;

    private final ObjectProperty objectPropTaskFileLoad = new SimpleObjectProperty();
    private final ChangeListener<Task> changeListenerTaskFileLoad;
    private Task taskFileLoad;

    private final ObjectProperty objectPropServiceFileSave = new SimpleObjectProperty();
    private final ChangeListener<Service> changeListenerService;
    private Service<String> serviceFileSave;
    private int intSaveCount = 0;
    private final StringProperty objStrPropFileState = new SimpleStringProperty();
    private final ChangeListener<String> changeListenerFileStatus;

    private Font font;
    private final ObjectProperty opFont = new SimpleObjectProperty();
    private final ChangeListener<Font> changeListenerObjPropFont;

    // -------------------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------------------
    public FileContentEditorSkin(final FileContentEditor control) {

        super(control);

        this.control = control;

        this.strId = control.getId();
        this.strFileName = control.getFileName();
        this.spFileContent = control.getFileContent();

        LOGGER.debug("FileContentEditorSkin constructor start."
                + " Id=\"" + this.strId + "\""
                + " FileName=\"" + this.strFileName + "\"");

        // ---------- initGraphics - begin -----------------------------------------------------
        this.vbox = new VBox();

        this.textArea = new TextArea();
        this.textArea.setPromptText("Enter Text here.");
        this.textArea.textProperty().bindBidirectional(this.spFileContent);
        this.font = control.getFont();
        if (this.font == null) {
            LOGGER.debug("Font is not set, use default."
                    + " Id=\"" + strId + "\""
                    + " font=\"" + this.font + "\"");
            this.font = Font.getDefault();
        }
        this.textArea.setFont(font);

        VBox.setVgrow(this.textArea, Priority.ALWAYS);

        this.hboxState = new HBox();
        Insets insHboxPadd = new Insets(5, 5, 5, 20);
        this.hboxState.setPadding(insHboxPadd);
        this.hboxState.setSpacing(5);
        this.hboxState.setVisible(true);
        this.hboxState.managedProperty().bind(this.hboxState.visibleProperty());
        this.bpHboxStateVisibility = this.hboxState.visibleProperty();
        this.bpHboxStateVisibility.bindBidirectional(control.getHboxStateVisibility());

        this.lblFileState = new Label("");
        this.lblFileName = new Label(this.strFileName);
        this.progressBar = new ProgressBar(0);
        this.objStrPropFileState.bind(control.getSpFileState());

        this.hboxState.getChildren().addAll(this.lblFileState, this.lblFileName, this.progressBar);

        this.vbox.getChildren().addAll(this.textArea, this.hboxState);

        this.getChildren().addAll(this.vbox);
        this.textArea.requestFocus();

        // ---------- initGraphics - end -------------------------------------------------------
        // ---------- registerListeners - begin ------------------------------------------------
        ChangeListener HboxStateVisibilityChangeListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                LOGGER.debug("HboxStateVisibilityChangeListener."
                        + " Id=\"" + strId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " observable=" + observable
                        + " oldValue=" + oldValue + " newValue=" + newValue);
            }
        };
        this.bpHboxStateVisibility.addListener(HboxStateVisibilityChangeListener);

        this.invalidationListenerFileContent = new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                control.getFileModified().set(true);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The FileContent binding is invalid."
                            + " Id=\"" + strId + "\""
                            + " FileName=\"" + strFileName + "\""
                            + " Observable=\"" + o + "\"");
                }
            }
        };

        // -------------------------------------------------------------------------------------
        this.caretPositionProperty = this.textArea.caretPositionProperty();
        this.caretChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldNumberValue, Number newNumberValue) {
                bpHboxStateVisibility.set(false);
                objIntPropCaretPosition.set(newNumberValue.intValue());
                LOGGER.debug("caretChangeListener."
                        + " Id=\"" + strId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " observable=" + observable
                        + " oldNumberValue=" + oldNumberValue + " newNumberValue=" + newNumberValue);
            }
        };
        this.caretPositionProperty.addListener(this.caretChangeListener);

        this.objIntPropCaretPositionChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldNumberValue, Number newNumberValue) {
                int intCaretPositionNew = newNumberValue.intValue();
                textArea.positionCaret(intCaretPositionNew);
                textArea.requestFocus();
                LOGGER.debug("objIntPropCaretPositionChangeListener."
                        + " Id=\"" + strId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " observable=" + observable
                        + " oldNumberValue=" + oldNumberValue + " newNumberValue=" + newNumberValue);
            }
        };
        this.objIntPropCaretPosition.addListener(objIntPropCaretPositionChangeListener);
        control.getIntCaretPosition().bindBidirectional(this.objIntPropCaretPosition);

        // -------------------------------------------------------------------------------------
        this.focusedPropertyChangeListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                bpHboxStateVisibility.set(false);
                int intCaretPosition = caretPositionProperty.getValue();
                LOGGER.debug("focusedPropertyChangeListener."
                        + " Id=\"" + strId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " CaretPosition=\"" + intCaretPosition + "\""
                        + " observable=" + observable
                        + " oldValue=" + oldValue + " newValue=" + newValue);
            }
        };
        this.focusedProperty = this.textArea.focusedProperty();
        this.focusedProperty.addListener(this.focusedPropertyChangeListener);

        this.textArea.textProperty().addListener(this.invalidationListenerFileContent);

        // -------------------------------------------------------------------------------------
        this.objectPropTaskFileLoad.bind(control.getOpTaskFileLoad());
        this.changeListenerTaskFileLoad = new ChangeListener<Task>() {
            @Override
            public void changed(ObservableValue<? extends Task> observable, Task oldValue, Task newValue) {
                taskFileLoad = newValue;
                LOGGER.debug("changeListenerTaskFileLoad Got Task."
                        + " Id=\"" + strId + "\""
                        + " taskFileLoad=\"" + taskFileLoad + "\""
                        + " oldValue=\"" + oldValue + "\"" + " newValue=\"" + newValue + "\"");
                processTask();
            }
        };
        this.objectPropTaskFileLoad.addListener(changeListenerTaskFileLoad);

        this.taskFileLoad = (Task) this.objectPropTaskFileLoad.getValue();

        this.processTask();

        // -------------------------------------------------------------------------------------
        this.objectPropServiceFileSave.bindBidirectional(control.getOpServiceFileSave());
        this.changeListenerService = new ChangeListener<Service>() {
            @Override
            public void changed(ObservableValue<? extends Service> observable, Service oldValue, Service newValue) {
                intSaveCount++;
                if (newValue == null) {
                    LOGGER.error("changeListenerService. newPropertyValue is null."
                            + " Id=\"" + strId + "\""
                            + " intSaveCount=" + intSaveCount);
                    return;
                }
                serviceFileSave = (Service) objectPropServiceFileSave.getValue();
                LOGGER.debug("changeListenerService."
                        + " Id=\"" + strId + "\""
                        + " intSaveCount=" + intSaveCount
                        + " serviceFileSave=\"" + serviceFileSave + "\""
                        + " observable=\"" + observable + "\""
                        + " oldValue=\"" + oldValue + "\""
                        + " newValue=\"" + newValue + "\"");
                ReadOnlyObjectProperty<Worker.State> stateProperty = serviceFileSave.stateProperty();
                Worker.State state = stateProperty.getValue();
                String stateName = state.name();
                LOGGER.debug("changeListenerService."
                        + " Id=\"" + strId + "\""
                        + " intSaveCount=" + intSaveCount
                        + " serviceFileSave=\"" + serviceFileSave + "\""
                        + "\nstateProperty=\"" + stateProperty + "\""
                        + "\nstate=\"" + state + "\""
                        + " stateName=\"" + stateName + "\"");

                serviceFileSave.onScheduledProperty().set(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        textArea.textProperty().removeListener(invalidationListenerFileContent);
                        caretPositionProperty.removeListener(caretChangeListener);
                        focusedProperty.removeListener(focusedPropertyChangeListener);

                        progressBar.progressProperty().unbind();
                        lblFileState.textProperty().unbind();
                        bpHboxStateVisibility.set(true);
                        progressBar.progressProperty().bind(serviceFileSave.progressProperty());
                        lblFileState.textProperty().bind(serviceFileSave.messageProperty());

                        EventType eventType = event.getEventType();
                        LOGGER.debug("onScheduledProperty serviceFileSave."
                                + " Id=\"" + strId + "\""
                                + " intSaveCount=" + intSaveCount
                                + " eventType=\"" + eventType + "\""
                                + " event=\"" + event + "\""
                                + " bpHboxStateVisibility=\"" + bpHboxStateVisibility.getValue() + "\"");
                    }
                });

                serviceFileSave.onRunningProperty().set(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        EventType eventType = event.getEventType();
                        LOGGER.debug("onRunningProperty serviceFileSave."
                                + " Id=\"" + strId + "\""
                                + " eventType=\"" + eventType + "\""
                                + " event=\"" + event + "\""
                                + " bpHboxStateVisibility=\"" + bpHboxStateVisibility.getValue() + "\"");
                    }
                });

                serviceFileSave.onFailedProperty().set(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        textArea.textProperty().addListener(invalidationListenerFileContent);
                        caretPositionProperty.addListener(caretChangeListener);
                        focusedProperty.addListener(focusedPropertyChangeListener);

                        control.getFileModified().set(false);
                        EventType eventType = event.getEventType();
                        LOGGER.debug("onFailedProperty serviceFileSave."
                                + " Id=\"" + strId + "\""
                                + " eventType=\"" + eventType + "\""
                                + " event=\"" + event + "\"");
                    }
                });

                serviceFileSave.onSucceededProperty().set(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        textArea.textProperty().addListener(invalidationListenerFileContent);
                        caretPositionProperty.addListener(caretChangeListener);
                        focusedProperty.addListener(focusedPropertyChangeListener);

                        control.getFileModified().set(false);
                        ReadOnlyObjectProperty<Worker.State> stateProperty = serviceFileSave.stateProperty();
                        Worker.State state = stateProperty.getValue();
                        String stateName = state.name();
                        String strResult = (String) serviceFileSave.getValue();
                        String strMsg = serviceFileSave.getMessage();

                        EventType eventType = event.getEventType();
                        LOGGER.debug("onSucceededProperty serviceFileSave."
                                + " Id=\"" + strId + "\""
                                + " intSaveCount=" + intSaveCount
                                + " eventType=\"" + eventType + "\""
                                + " event=\"" + event + "\""
                                + "\nstateProperty=\"" + stateProperty + "\""
                                + "\nstate=\"" + state + "\""
                                + " stateName=\"" + stateName + "\""
                                + "\nstrMsg=\"" + strMsg + "\""
                                + "\nstrResult=\"" + strResult + "\"");
                    }
                });
            }
        };
        this.objectPropServiceFileSave.addListener(this.changeListenerService);

        this.changeListenerFileStatus = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                LOGGER.debug("changeListenerFileStatus."
                        + " Id=\"" + strId + "\""
                        + " observable=\"" + observable + "\""
                        + " oldValue=\"" + oldValue + "\"" + " newValue=\"" + newValue + "\"");
                lblFileState.textProperty().setValue(newValue);
            }
        };
        this.objStrPropFileState.addListener(changeListenerFileStatus);

        // -------------------------------------------------------------------------------------
        // Font
        this.opFont.bind(control.getOpFont());
        this.changeListenerObjPropFont = new ChangeListener<Font>() {
            @Override
            public void changed(ObservableValue<? extends Font> observable, Font oldValue, Font newValue) {
                if (newValue == null) {
                    LOGGER.error("changeListenerFont. newValue is null."
                            + " Id=\"" + strId + "\"");
                    return;
                }
                LOGGER.debug("changeListenerFont."
                        + " Id=\"" + strId + "\""
                        + " observable=\"" + observable + "\""
                        + " oldValue=\"" + oldValue + "\""
                        + " newValue=\"" + newValue + "\""
                );
                font = newValue;
                textArea.setFont(font);
            }
        };
        this.opFont.addListener(this.changeListenerObjPropFont);

        // -------------------------------------------------------------------------------------
        LOGGER.debug("FileContentEditorSkin constructor finish."
                + " Id=\"" + strId + "\""
                + " FileName=\"" + strFileName + "\"");
        /* 
        // For test only
        this.textArea.setOnKeyTyped(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                EventType eventType = event.getEventType();
                //control.setFileModified(true);
                //control.getFileModified().set(true);
            }
        });
         */
        // TODO: add this: this.textArea.contextMenuProperty()
    }

    // -------------------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------------------
    private void processTask() {

        if (this.taskFileLoad == null) {
        } else {
            this.progressBar.progressProperty().bind(this.taskFileLoad.progressProperty());
            this.lblFileState.textProperty().bind(this.taskFileLoad.messageProperty());

            ReadOnlyObjectProperty<Worker.State> stateProperty = this.taskFileLoad.stateProperty();
            Worker.State state = stateProperty.getValue();
            String stateName = state.name();

            LOGGER.debug("Got Task."
                    + " Id=\"" + this.strId + "\""
                    + " taskFileLoad=\"" + this.taskFileLoad + "\""
                    + " stateProperty=\"" + stateProperty + "\""
                    + " state=\"" + state + "\""
                    + " stateName=\"" + stateName + "\""
            );

            this.taskFileLoad.onScheduledProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    textArea.textProperty().removeListener(invalidationListenerFileContent);
                    LOGGER.debug("onScheduledProperty."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\"");
                }
            });

            this.taskFileLoad.onRunningProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    LOGGER.debug("onRunningProperty."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\"");
                }
            });

            this.taskFileLoad.onFailedProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    //textArea.textProperty().addListener(invalidationListenerFileContent);
                    String strErrMsg = lblFileState.textProperty().getValue()
                            + "\nFile=" + lblFileName.getText()
                            + "\nTry to Open File with different Charset or Open File Binary.";
                    textArea.setText(strErrMsg);
                    textArea.setEditable(false);
                    lblFileState.textProperty().unbind();
                    control.getFileModified().set(false);
                    EventType eventType = event.getEventType();
                    LOGGER.debug("onFailedProperty."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\""
                            + " ErrMsg=\"" + strErrMsg + "\"");
                }
            });

            this.taskFileLoad.onSucceededProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ReadOnlyObjectProperty<Worker.State> stateProperty = taskFileLoad.stateProperty();
                    Worker.State state = stateProperty.getValue();
                    String stateName = state.name();
                    EventType eventType = event.getEventType();
                    String strText;
                    int intTextLen = 0;
                    try {
                        strText = (String) taskFileLoad.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        LOGGER.error("onSucceededProperty."
                                + " Id=\"" + strId + "\""
                                + " Exception=\"" + ex.toString() + "\"");
                        strText = "";
                    }
                    // TODO: filter text!!!
                    int intTextLogLimit = 500;
                    String strTextPart = "";
                    if (strText != null) {
                        intTextLen = strText.length();
                        int intTrim = Math.min(intTextLen, intTextLogLimit);
                        strTextPart = strText.substring(0, intTrim) + "\n...";
                    }
                    LOGGER.debug("onSucceededProperty got File content."
                            + " Id=\"" + strId + "\""
                            + " event=\"" + event + "\""
                            + " stateProperty=\"" + stateProperty + "\""
                            + " state=\"" + state + "\""
                            + " stateName=\"" + stateName + "\""
                            + "\nintTextLen=\"" + intTextLen + "\""
                            + "\nstrText=\"" + strTextPart + "\""
                    );
                    event.consume();
                    textArea.setText(strText);
                    textArea.textProperty().addListener(invalidationListenerFileContent);
                    String strMsg = taskFileLoad.getMessage();
                    lblFileState.textProperty().unbind();
                    control.getFileModified().set(false);
                    LOGGER.debug("onSucceededProperty set text to textArea."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\""
                            + "\nstrMsg=\"" + strMsg + "\""
                            + "\nintTextLen=\"" + intTextLen + "\""
                    + "\nstrText=\"" + strTextPart + "\""
                    );
                }
            });
        }
    }

    // -------------------------------------------------------------------------------------
    @Override
    public void dispose() {

        this.textArea.textProperty().unbind();
        this.bpHboxStateVisibility.unbind();
        this.hboxState.managedProperty().unbind();
        this.control.getIntCaretPosition().unbind();
        this.objectPropTaskFileLoad.unbind();
        this.progressBar.progressProperty().unbind();
        this.lblFileState.textProperty().unbind();
        this.objectPropServiceFileSave.unbind();
        this.opFont.unbind();

        this.caretPositionProperty.removeListener(this.caretChangeListener);
        this.objIntPropCaretPosition.removeListener(objIntPropCaretPositionChangeListener);
        this.focusedProperty.removeListener(this.focusedPropertyChangeListener);
        this.textArea.textProperty().removeListener(this.invalidationListenerFileContent);
        this.objectPropServiceFileSave.removeListener(this.changeListenerService);
        this.opFont.removeListener(this.changeListenerObjPropFont);

        super.dispose();

        this.getChildren().removeAll();
    }

    // -------------------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
}
