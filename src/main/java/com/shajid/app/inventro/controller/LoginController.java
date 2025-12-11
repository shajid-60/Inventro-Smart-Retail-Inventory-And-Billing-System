// language: java
package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.SQLiteConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

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
        // \* only Admin and Customer
        roleCombo.getItems().setAll("Admin", "Customer");

        // \* show "Select" before any role is chosen
        roleCombo.setValue(null);
        roleCombo.setPromptText("Select");
        roleCombo.setEditable(false);

        // \* list items style (dropdown)
        roleCombo.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #223344;");
            }
        });

        // \* selected item / prompt cell style
        roleCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #223344;");
            }
        });
    }

    @FXML
    void onLoginClick(ActionEvent event) {
        String email = emailField.getText();
        String pass = passwordField.getText();
        String role = roleCombo.getValue();

        if (role == null || role.isBlank()) {
            if (errorLabel != null) errorLabel.setText("Please select role.");
            return;
        }

        if (email.isEmpty() || pass.isEmpty()) {
            if (errorLabel != null) errorLabel.setText("Email and Password required.");
            return;
        }

        try (Connection conn = SQLiteConnection.connect()) {
            String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, pass);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (errorLabel != null) errorLabel.setText("Login Successful!");

                // \* if Admin -> go to dashboard
                if ("Admin".equals(role)) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(new Scene(loader.load(), 1200, 800));
                } else {
                    // \* customer: stay or later route to customer page
                    System.out.println("Logged in as Customer");
                }
            } else {
                if (errorLabel != null) errorLabel.setText("Invalid credentials.");
            }

        } catch (Exception e) {
            if (errorLabel != null) errorLabel.setText("Database Error!");
            e.printStackTrace();
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
