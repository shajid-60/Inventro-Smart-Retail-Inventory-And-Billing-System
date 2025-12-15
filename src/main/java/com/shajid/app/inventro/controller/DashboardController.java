package com.shajid.app.inventro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label inventroLabel;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label outOfStockLabel;

    @FXML
    private Label suppliersLabel;

    @FXML
    private Label stockValueLabel;

    @FXML
    private Label unfulfilledLabel;

    @FXML
    private Label receivedLabel;

    @FXML
    private BarChart<String, Number> stockChart;

    @FXML
    private CategoryAxis stockChartXAxis;

    @FXML
    private NumberAxis stockChartYAxis;

    @FXML
    public void initialize() {
        if (totalProductsLabel != null) totalProductsLabel.setText("0");
        if (lowStockLabel != null) lowStockLabel.setText("0");
        if (outOfStockLabel != null) outOfStockLabel.setText("0");
        if (suppliersLabel != null) suppliersLabel.setText("0");
        if (stockValueLabel != null) stockValueLabel.setText("$0.00");
        if (unfulfilledLabel != null) unfulfilledLabel.setText("0");
        if (receivedLabel != null) receivedLabel.setText("0");
    }

    @FXML
    private void goToProducts(ActionEvent event) {
        switchScene(event, "/fxml/products.fxml", "Inventro - Products");
    }

    @FXML
    private void goToSuppliers(ActionEvent event) {
        switchScene(event, "/fxml/suppliers.fxml", "Inventro - Suppliers");
    }

    @FXML
    private void goToOrders(ActionEvent event) {
        switchScene(event, "/fxml/orders.fxml", "Inventro - Stock Orders");
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
