package com.shajid.app.inventro;

import com.shajid.app.inventro.database.DatabaseSetup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Inventro extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ensure DB and `users` table exist
        DatabaseSetup.initialize();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Inventro - Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
