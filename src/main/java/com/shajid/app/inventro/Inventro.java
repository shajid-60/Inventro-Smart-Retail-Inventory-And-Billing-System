package com.shajid.app.inventro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Inventro extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load login.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        // Set the scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Inventro - Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
