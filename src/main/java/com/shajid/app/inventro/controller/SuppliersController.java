package com.shajid.app.inventro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SuppliersController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<?> suppliersTable;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableColumn<?, ?> colPhone;

    @FXML
    private TableColumn<?, ?> colEmail;

    @FXML
    private TableColumn<?, ?> colAddress;

    @FXML
    public void initialize() {
        // TODO: load suppliers from DB and bind columns
    }

    @FXML
    private void onSearch(ActionEvent event) {
        // TODO: filter suppliersTable by searchField.getText()
    }

    @FXML
    private void onAddSupplier(ActionEvent event) {
        // TODO: open add-supplier dialog
    }

    @FXML
    private void onEditSupplier(ActionEvent event) {
        // TODO: open edit dialog for selected row
    }

    @FXML
    private void onDeleteSupplier(ActionEvent event) {
        // TODO: delete selected supplier from DB
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        switchScene(event, "/fxml/dashboard.fxml", "Inventro - Admin Dashboard");
    }

    private void switchScene(ActionEvent event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
