package com.shajid.app.inventro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class OrdersController {

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TableView<?> ordersTable;

    @FXML
    private TableColumn<?, ?> colId;

    @FXML
    private TableColumn<?, ?> colSupplier;

    @FXML
    private TableColumn<?, ?> colDate;

    @FXML
    private TableColumn<?, ?> colTotal;

    @FXML
    private TableColumn<?, ?> colStatus;

    @FXML
    public void initialize() {
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All", "Unfulfilled", "Received", "Cancelled");
            statusFilter.setValue("All");
        }
        // TODO: load orders from DB
    }

    @FXML
    private void onNewOrder(ActionEvent event) {
        // TODO: open create-order dialog
    }

    @FXML
    private void onMarkReceived(ActionEvent event) {
        // TODO: mark selected order as received
    }

    @FXML
    private void onCancelOrder(ActionEvent event) {
        // TODO: cancel selected order
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
