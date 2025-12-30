package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.SQLiteConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        if (roleCombo != null) {
            roleCombo.getItems().setAll("Customer");
            roleCombo.setValue("Customer");
        }
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    @FXML
    private void onRegisterClick(ActionEvent event) {
        if (errorLabel != null) errorLabel.setText("");

        String fullName = text(fullNameField);
        String ageText = text(ageField);
        String phone = text(phoneField);
        String address = text(addressField);
        String email = text(emailField);
        String password = text(passwordField);
        String confirmPassword = text(confirmPasswordField);
        String role = "Customer";

        if (fullName.isBlank() || ageText.isBlank() || phone.isBlank()
                || address.isBlank() || email.isBlank()
                || password.isBlank() || confirmPassword.isBlank()) {
            setError("All fields are required.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0) {
                setError("Age must be positive.");
                return;
            }
        } catch (NumberFormatException ex) {
            setError("Age must be a number.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            setError("Passwords do not match.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            setError("Enter a valid email address.");
            return;
        }

        String sql = "INSERT INTO users(fullName, age, phone, address, email, password, role) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setInt(2, age);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, email);
            ps.setString(6, password);
            ps.setString(7, role);
            ps.executeUpdate();

            goToLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
            setError("Could not register. Email might already be registered.");
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Inventro - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            setError("Cannot open login page.");
        }
    }

    private static String text(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim();
    }

    private static String text(PasswordField pf) {
        return pf == null || pf.getText() == null ? "" : pf.getText().trim();
    }

    private void setError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
        }
    }
}
