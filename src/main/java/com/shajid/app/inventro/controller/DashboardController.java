package com.shajid.app.inventro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // can set dynamic welcome text in future
        if (welcomeLabel != null) welcomeLabel.setText("Welcome, Admin");
    }

    @FXML
    void onLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
