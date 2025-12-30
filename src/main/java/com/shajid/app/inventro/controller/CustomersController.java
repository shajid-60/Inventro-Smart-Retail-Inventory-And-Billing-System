package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.SQLiteConnection;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomersController {

    public static class UserRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty fullName = new SimpleStringProperty();
        private final StringProperty email = new SimpleStringProperty();
        private final StringProperty phone = new SimpleStringProperty();
        private final StringProperty role = new SimpleStringProperty();

        public int getId() { return id.get(); }
        public void setId(int v) { id.set(v); }
        public IntegerProperty idProperty() { return id; }

        public String getFullName() { return fullName.get(); }
        public void setFullName(String v) { fullName.set(v); }
        public StringProperty fullNameProperty() { return fullName; }

        public String getEmail() { return email.get(); }
        public void setEmail(String v) { email.set(v); }
        public StringProperty emailProperty() { return email; }

        public String getPhone() { return phone.get(); }
        public void setPhone(String v) { phone.set(v); }
        public StringProperty phoneProperty() { return phone; }

        public String getRole() { return role.get(); }
        public void setRole(String v) { role.set(v); }
        public StringProperty roleProperty() { return role; }
    }

    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, Integer> colId;
    @FXML private TableColumn<UserRow, String> colFullName;
    @FXML private TableColumn<UserRow, String> colEmail;
    @FXML private TableColumn<UserRow, String> colPhone;
    @FXML private TableColumn<UserRow, String> colRole;

    private final ObservableList<UserRow> users = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colFullName != null) colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colPhone != null) colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (colRole != null) colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        if (usersTable != null) usersTable.setItems(users);

        reloadAsync();
    }

    private void reloadAsync() {
        executor.submit(() -> {
            try (Connection conn = SQLiteConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT id, fullName, email, phone, role FROM users ORDER BY id DESC");
                 ResultSet rs = ps.executeQuery()) {

                ObservableList<UserRow> tmp = FXCollections.observableArrayList();
                while (rs.next()) {
                    UserRow r = new UserRow();
                    r.setId(rs.getInt("id"));
                    r.setFullName(rs.getString("fullName"));
                    r.setEmail(rs.getString("email"));
                    r.setPhone(rs.getString("phone"));
                    r.setRole(rs.getString("role"));
                    tmp.add(r);
                }

                Platform.runLater(() -> users.setAll(tmp));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Load failed", "Could not load customers."));
            }
        });
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Inventro \\- Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation error", "Cannot go back to dashboard.");
        }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
