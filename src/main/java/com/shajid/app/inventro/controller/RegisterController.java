package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.SQLiteConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private ComboBox<String> roleCombo;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        // roleCombo may not be present in FXML; guard against NPE
        if (roleCombo != null) {
            roleCombo.getItems().addAll("Customer"); // only customer allowed
            roleCombo.setValue("Customer");
        }
    }

    @FXML
    void onRegisterClick(ActionEvent event) {
        String fullName = fullNameField != null ? fullNameField.getText() : "";
        String age = ageField != null ? ageField.getText() : "";
        String phone = phoneField != null ? phoneField.getText() : "";
        String address = addressField != null ? addressField.getText() : "";
        String email = emailField != null ? emailField.getText() : "";
        String pass = passwordField != null ? passwordField.getText() : "";
        String role = "Customer";

        if (fullName.isBlank() || email.isBlank() || pass.isBlank()) {
            if (errorLabel != null) errorLabel.setText("Required fields missing.");
            return;
        }

        try (Connection conn = SQLiteConnection.connect()) {
            String sql = """
                    INSERT INTO users(fullName, email, password, role)
                    VALUES(?, ?, ?, ?)
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, pass);
            stmt.setString(4, role);

            stmt.executeUpdate();

            if (errorLabel != null) errorLabel.setText("Registration Successful!");

            // navigate back to login
            goToLogin(event);

        } catch (Exception e) {
            if (errorLabel != null) errorLabel.setText("User already exists!");
            e.printStackTrace();
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            // pick a non-null node to obtain the current stage
            Node anyNode = (fullNameField != null) ? fullNameField : (emailField != null ? emailField : null);
            Stage stage;
            if (anyNode != null) {
                stage = (Stage) anyNode.getScene().getWindow();
            } else if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                // last-resort: create a new stage (shouldn't happen in normal usage)
                stage = new Stage();
            }
            stage.setScene(new Scene(loader.load(), 1200, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
