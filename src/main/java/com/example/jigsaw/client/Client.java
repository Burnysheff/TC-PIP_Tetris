package com.example.jigsaw.client;

import com.example.jigsaw.Jigsaw;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Jigsaw.class.getResource("client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 700);
        ClientController clientController = (ClientController) fxmlLoader.getController();
        clientController.setStage(stage);
        stage.setTitle("Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}