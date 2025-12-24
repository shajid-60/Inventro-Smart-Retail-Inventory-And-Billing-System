// src/main/java/com/shajid/app/inventro/controller/DashboardController.java
package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.SQLiteConnection;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    @FXML private Label inventroLabel;

    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label suppliersLabel;

    @FXML private Label stockValueLabel;
    @FXML private Label unfulfilledLabel;
    @FXML private Label receivedLabel;

    @FXML private BarChart<String, Number> stockChart;
    @FXML private CategoryAxis stockChartXAxis;
    @FXML private NumberAxis stockChartYAxis;

    @FXML
    public void initialize() {
        refreshStats();
    }

    private void refreshStats() {
        setSafe(totalProductsLabel, "0");
        setSafe(lowStockLabel, "0");
        setSafe(outOfStockLabel, "0");
        setSafe(suppliersLabel, "0");
        setSafe(stockValueLabel, "$0.00");
        setSafe(unfulfilledLabel, "0");
        setSafe(receivedLabel, "0");

        try (Connection conn = SQLiteConnection.connect()) {

            int totalProducts = queryInt(conn, "SELECT COUNT(*) FROM products");
            int lowStock = queryInt(conn, "SELECT COUNT(*) FROM products WHERE stock > 0 AND stock <= 5");
            int outOfStock = queryInt(conn, "SELECT COUNT(*) FROM products WHERE stock = 0");

            double stockValue = queryDouble(conn, "SELECT COALESCE(SUM(stock * price), 0) FROM products");

            int unfulfilled = queryInt(conn, "SELECT COUNT(*) FROM orders WHERE LOWER(status) = 'unfulfilled'");
            int received = queryInt(conn, "SELECT COUNT(*) FROM orders WHERE LOWER(status) = 'received'");

            setSafe(totalProductsLabel, String.valueOf(totalProducts));
            setSafe(lowStockLabel, String.valueOf(lowStock));
            setSafe(outOfStockLabel, String.valueOf(outOfStock));

            // No suppliers table in your DB setup yet, keep 0 for now.
            setSafe(suppliersLabel, "0");

            setSafe(stockValueLabel, formatCurrency(stockValue));
            setSafe(unfulfilledLabel, String.valueOf(unfulfilled));
            setSafe(receivedLabel, String.valueOf(received));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int queryInt(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static double queryDouble(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    private static void setSafe(Label label, String value) {
        if (label != null) label.setText(value);
    }

    private static String formatCurrency(double v) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        return nf.format(v);
    }

    @FXML
    private void goToProducts(ActionEvent event) {
        switchScene(event, "/fxml/products.fxml", "Inventro \\- Products");
    }

    @FXML
    private void goToSuppliers(ActionEvent event) {
        switchScene(event, "/fxml/suppliers.fxml", "Inventro \\- Suppliers");
    }

    @FXML
    private void goToOrders(ActionEvent event) {
        switchScene(event, "/fxml/orders.fxml", "Inventro \\- Stock Orders");
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
