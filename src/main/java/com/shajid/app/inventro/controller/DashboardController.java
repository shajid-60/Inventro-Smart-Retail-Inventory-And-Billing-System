package com.shajid.app.inventro.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;

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
        // Optional: set initial demo values
        if (totalProductsLabel != null) totalProductsLabel.setText("0");
        if (lowStockLabel != null) lowStockLabel.setText("0");
        if (outOfStockLabel != null) outOfStockLabel.setText("0");
        if (suppliersLabel != null) suppliersLabel.setText("0");
        if (stockValueLabel != null) stockValueLabel.setText("$0.00");
        if (unfulfilledLabel != null) unfulfilledLabel.setText("0");
        if (receivedLabel != null) receivedLabel.setText("0");
    }
}
