package com.example.jigsaw;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

/**
 * A controller for the elements
 * */
public class ServerStart {
    @FXML
    private Label time = new Label();

    @FXML
    private Label seconds = new Label();

    @FXML
    private Label minutes = new Label();

    @FXML
    private TextField secs = new TextField();

    @FXML
    private TextField mins = new TextField();

    @FXML
    private Button start = new Button();

    @FXML
    private Button friend = new Button();

    @FXML
    private Label started = new Label();

    @FXML
    /**
     * Initializing the main this, like the grid and showing welcome labels and buttons
     * */
    public void initialize() {
        start.setDisable(true);
        friend.setDisable(true);
        time.setText("Enter the maximum time of the game:");
        seconds.setText("Seconds:");
        minutes.setText("Minutes:");
        started.setVisible(false);
        start.setText("Single game");
        friend.setText("Play with friend");
        secs.setOnKeyReleased(keyEvent -> disabling());
        mins.setOnKeyReleased(keyEvent -> disabling());
    }

    private void disabling() {
        if (secs.getText().length() == 0 || mins.getText().length() == 0) {
            start.setDisable(true);
            friend.setDisable(true);
            return;
        } else {
            if (!checkInt(secs.getText()) || !checkInt(mins.getText())) {
                start.setDisable(true);
                friend.setDisable(true);
                return;
            }
        }
        start.setDisable(false);
        friend.setDisable(false);
    }

    private boolean checkInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    public void init() {
        Server server = new Server(1, this, Integer.parseInt(secs.getText()), Integer.parseInt(mins.getText()));
    }
    @FXML
    public void friend() {
        Server server = new Server(2, this, Integer.parseInt(secs.getText()), Integer.parseInt(mins.getText()));
    }

    public void started() {
        start.setVisible(false);
        friend.setVisible(false);
        minutes.setVisible(false);
        seconds.setVisible(false);
        time.setVisible(false);
        secs.setVisible(false);
        mins.setVisible(false);
        started.setVisible(true);
        started.setText("Server started successfully!");
        started.setFont(Font.font(30));
    }
}