package com.shajid.app.inventro;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField regName;

    @FXML
    private TextField regAge;

    @FXML
    private TextField regPhone;

    @FXML
    private TextField regAddress;

    @FXML
    private TextField regUserId;

    @FXML
    private PasswordField regPassword;

    @FXML
    private void handleRegister() {
        String name = regName.getText();
        String age = regAge.getText();
        String phone = regPhone.getText();
        String address = regAddress.getText();
        String id = regUserId.getText();
        String pass = regPassword.getText();

        if (name.isEmpty() || age.isEmpty() || phone.isEmpty() || address.isEmpty() || id.isEmpty() || pass.isEmpty()) {
            System.out.println("Please fill all fields!");
            return;
        }

        System.out.println("Registered Customer:");
        System.out.println("Name: " + name + ", Age: " + age + ", Phone: " + phone + ", Address: " + address);
        System.out.println("UserID: " + id + " | Password: " + pass);

        // TODO: Save these details into database
    }

    @FXML
    private void goToLogin(javafx.event.ActionEvent event) throws Exception {
        javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(root, 800, 600));
        stage.setTitle("Inventro - Login");
    }
}
