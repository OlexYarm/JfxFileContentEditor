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

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

public class ExploreSceneGraph {

    public static void exploreScene(int intLevel, String strWhere, Scene scene) {

        if (intLevel == 0) {
            System.out.printf("### " + strWhere + " Scene Level %d ###%n", intLevel);
        }
        String strOffset = " ".repeat(intLevel);
        String strFormat = "%s %-20s | %-40s | %-40s |%n";
        System.out.printf(strFormat, strOffset, "Node ID", "Class Name", "Parent Class Name");

        Parent parent = scene.getRoot();
        Node nodeParent = parent;
        printNodeInfo(nodeParent, strOffset, strFormat);

        intLevel++;
        System.out.printf("--- " + strWhere + " Scene Level %d ###%n", intLevel);
        ObservableList<Node> lstChildren = parent.getChildrenUnmodifiable();
        for (Node node : lstChildren) {
            printNodeInfo(node, strOffset, strFormat);
            //String strNodeClassNameLC = strNodeClassName.toLowerCase();

        }

    }

    private static void printNodeInfo(Node node, String strOffset, String strFormat) {

        String strNodeClassName = node.getClass().getName();
        String strNodeID = node.getId();
        Parent parentNode = node.getParent();
        String strNodeParentClassName;
        if (parentNode == null) {
            strNodeParentClassName = null;
        } else {
            strNodeParentClassName = parentNode.getClass().getName();
        }
        ObservableMap<Object, Object> mapNodeProperties = node.getProperties();
        StringProperty idPropertyNode = node.idProperty();
        System.out.printf(strFormat, strOffset, strNodeID, strNodeClassName, strNodeParentClassName);

    }

    public static void explore(int intLevel, String strWhere, Pane pane) {

        if (intLevel == 0) {
            System.out.printf("### " + strWhere + " Level%3d ###%n", intLevel);
        }
        String strOffset = " ".repeat(intLevel);
        String strFormat = "%s %-20s | %-40s | %-40s |%n";
        System.out.printf(strFormat, strOffset, "Node ID", "Class Name", "Parent Class Name");

        printNodeInfo(pane, strOffset, strFormat);

        intLevel++;
        System.out.printf("--- " + strWhere + " Level%3d ###%n", intLevel);
        ObservableList<Node> lstChildren = pane.getChildren();
        for (Node node : lstChildren) {
            String strNodeClassName = node.getClass().getName();
            String strNodeID = node.getId();
            Parent parentNode = node.getParent();
            String strNodeParentClassName = parentNode.getClass().getName();
            ObservableMap<Object, Object> mapNodeProperties = node.getProperties();
            StringProperty idPropertyNode = node.idProperty();
            System.out.printf(strFormat, strOffset, strNodeID, strNodeClassName, strNodeParentClassName);
            String strNodeClassNameLC = strNodeClassName.toLowerCase();
            //switch(strNodeClassNameLC){
            //    case :
            //}
            if (strNodeClassNameLC.contains("tabpane")) {
                System.out.println("tabpane");
                TabPane paneChild = (TabPane) node;
                paneChild.getChildrenUnmodifiable();
                //Pane paneChild = (Pane)node.getClass();
                //explore(intLevel + 1, paneChild);
                //explore(intLevel + 1, node.getClass());
            }
        }
    }

}
