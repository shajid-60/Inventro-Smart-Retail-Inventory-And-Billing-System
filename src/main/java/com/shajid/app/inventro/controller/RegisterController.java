package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.SQLiteConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField ageField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        // Only Customer registration allowed
        if (roleCombo != null) {
            roleCombo.getItems().setAll("Customer");
            roleCombo.setValue("Customer");
        }
    }

    @FXML
    void onRegisterClick(ActionEvent event) {
        errorLabel.setText("");

        String fullName = fullNameField.getText().trim();
        String age = ageField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        String role = "Customer"; // enforce Customer

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Please fill in all required fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        try (Connection conn = SQLiteConnection.connect()) {
            String sql = "INSERT INTO users(fullName, age, phone, address, email, password, role) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fullName);
            // optional numeric fields
            if (age.isEmpty()) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, Integer.parseInt(age));
            }
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setString(5, email);
            stmt.setString(6, password);
            stmt.setString(7, role);

            stmt.executeUpdate();

            // After successful registration, go back to login
            goToLogin(event);

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                errorLabel.setText("Email already registered.");
            } else {
                errorLabel.setText("Database error.");
            }
            e.printStackTrace();
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Inventro - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (errorLabel != null) {
                errorLabel.setText("Cannot open login page.");
            }
        }
    }
}
