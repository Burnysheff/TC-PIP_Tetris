package com.example.jigsaw;

import javafx.scene.Node;

/**
 * A class for dragging the elements
 * */
public class Draggable {
    private double placeX;
    private double placeY;

    public void setDraggable(Node node) {

        node.setOnMousePressed(mouseEvent -> {
            placeX = mouseEvent.getSceneX() - node.getTranslateX();
            placeY = mouseEvent.getSceneY() - node.getTranslateY();
        });

        node.setOnMouseDragged(mouseEvent -> {
            node.setTranslateX(mouseEvent.getSceneX() - placeX);
            node.setTranslateY(mouseEvent.getSceneY() - placeY);
        });
    }
}
