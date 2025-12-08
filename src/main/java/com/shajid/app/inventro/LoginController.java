package com.shajid.app.inventro;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class LoginController {

    @FXML
    private TextField loginUserId;

    @FXML
    private PasswordField loginPassword;

    @FXML
    private RadioButton adminRadio;

    @FXML
    private RadioButton customerRadio;

    @FXML
    private ToggleGroup roleGroup; // Automatically links to RadioButtons

    @FXML
    private void handleLogin() {
        String id = loginUserId.getText();
        String pass = loginPassword.getText();

        RadioButton selectedRole = (RadioButton) roleGroup.getSelectedToggle();
        if (selectedRole == null) {
            System.out.println("Please select a role!");
            return;
        }
        String role = selectedRole.getText();

        System.out.println("Login ID: " + id + " | Password: " + pass + " | Role: " + role);

        // TODO: Add database validation based on role
        if (role.equals("Admin")) {
            // Open admin dashboard
        } else if (role.equals("Customer")) {
            // Open customer dashboard
        }
    }

    @FXML
    private void goToRegister(javafx.event.ActionEvent event) throws Exception {
        javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(root, 800, 600));
        stage.setTitle("Inventro - Register");
    }
}
