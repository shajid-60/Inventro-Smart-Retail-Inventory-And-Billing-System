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

public class ProductsController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<?> productsTable;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableColumn<?, ?> colCategory;

    @FXML
    private TableColumn<?, ?> colStock;

    @FXML
    private TableColumn<?, ?> colPrice;

    @FXML
    public void initialize() {
        // TODO: load products from DB and bind columns
    }

    @FXML
    private void onSearch(ActionEvent event) {
        // TODO: filter productsTable by searchField.getText()
    }

    @FXML
    private void onAddProduct(ActionEvent event) {
        // TODO: open add-product dialog
    }

    @FXML
    private void onEditProduct(ActionEvent event) {
        // TODO: open edit dialog for selected row
    }

    @FXML
    private void onDeleteProduct(ActionEvent event) {
        // TODO: delete selected product from DB
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
