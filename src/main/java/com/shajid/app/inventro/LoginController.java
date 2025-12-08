package com.shajid.app.inventro;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class MainController {

    // --- Login Page Fields ---
    @FXML
    private TextField loginUserId;

    @FXML
    private PasswordField loginPassword;

    @FXML
    private void handleLogin() {
        String id = loginUserId.getText();
        String pass = loginPassword.getText();
        System.out.println("Login ID: " + id + " | Password: " + pass);

        // TODO: Add database validation here
    }

    // --- Register Page Fields ---
    @FXML
    private TextField regUserId;

    @FXML
    private PasswordField regPassword;

    @FXML
    private void handleRegister() {
        String id = regUserId.getText();
        String pass = regPassword.getText();
        System.out.println("Register ID: " + id + " | Password: " + pass);

        // TODO: Save new user to database here
    }

    // --- Switch from Login to Register ---
    @FXML
    private void goToRegister(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Inventro - Register");
    }

    // --- Switch from Register to Login ---
    @FXML
    private void goToLogin(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Inventro - Login");
    }
}
