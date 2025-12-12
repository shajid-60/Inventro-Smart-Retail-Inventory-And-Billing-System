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
import java.sql.ResultSet;

public class LoginController {

    private static final String ADMIN_EMAIL = "majharulshajid585@gmail.com";
    private static final String ADMIN_PASSWORD = "shajid585";

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
        roleCombo.getItems().setAll("Admin", "Customer");
        roleCombo.setValue("Customer");
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        errorLabel.setText("");

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            errorLabel.setText("Please enter email, password, and select a role.");
            return;
        }

        try {
            if ("Admin".equals(role)) {
                // Hardcoded single admin
                if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
                    loadScene(event, "/fxml/dashboard.fxml", "Inventro - Admin Dashboard");
                } else {
                    errorLabel.setText("Incorrect admin email or password.");
                }
            } else {
                // Customer from DB
                try (Connection conn = SQLiteConnection.connect()) {
                    String sql = "SELECT * FROM users WHERE email=? AND password=? AND role=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, email);
                    stmt.setString(2, password);
                    stmt.setString(3, "Customer");

                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        loadScene(event, "/fxml/customer_dashboard.fxml", "Inventro - Customer Dashboard");
                    } else {
                        errorLabel.setText("Incorrect email or password for customer.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Database or login error.");
        }
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle(title);
        stage.show();
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Inventro - Register");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Cannot open registration page.");
        }
    }
}
