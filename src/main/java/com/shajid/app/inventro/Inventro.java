// Java
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
        // Initialize DB and tables
        DatabaseSetup.initialize();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setTitle("Inventro - Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
