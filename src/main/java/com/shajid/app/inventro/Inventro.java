// Java
package com.shajid.app.inventro;

import com.shajid.app.inventro.database.DatabaseSetup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Inventro extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseSetup.initialize(); // or call once at app startup if you want a fresh DB

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Inventro - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
