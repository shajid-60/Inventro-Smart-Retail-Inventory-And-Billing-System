package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.SQLiteConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardController {

    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label stockValueLabel;
    @FXML private BarChart<String, Number> stockChart;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        reloadAsync();
    }

    private void reloadAsync() {
        executor.submit(() -> {
            try (Connection conn = SQLiteConnection.connect()) {

                int totalProducts = countInt(conn, "SELECT COUNT(*) FROM products");
                int lowStock = countInt(conn, "SELECT COUNT(*) FROM products WHERE stock BETWEEN 1 AND 5");
                int outOfStock = countInt(conn, "SELECT COUNT(*) FROM products WHERE stock <= 0");

                double stockValue = sumDouble(conn, "SELECT SUM(stock * price) FROM products");

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT name, stock FROM products ORDER BY id DESC LIMIT 10");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        series.getData().add(
                                new XYChart.Data<>(rs.getString("name"), rs.getInt("stock")));
                    }
                }

                Platform.runLater(() -> {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);

                    if (totalProductsLabel != null) totalProductsLabel.setText(String.valueOf(totalProducts));
                    if (lowStockLabel != null)     lowStockLabel.setText(String.valueOf(lowStock));
                    if (outOfStockLabel != null)   outOfStockLabel.setText(String.valueOf(outOfStock));

                    if (stockValueLabel != null)   stockValueLabel.setText(nf.format(stockValue));

                    if (stockChart != null) {
                        stockChart.getData().clear();
                        stockChart.getData().add(series);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Dashboard error", "Failed to load dashboard data."));
            }
        });
    }

    private int countInt(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private double sumDouble(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // --- Navigation handlers for buttons in `dashboard.fxml` ---

    @FXML
    private void onManageProducts(ActionEvent event) {
        switchScene(event, "/fxml/products.fxml", "Inventro - Manage Products");
    }

    @FXML
    private void onViewCustomers(ActionEvent event) {
        switchScene(event, "/fxml/customers.fxml", "Inventro - Customers");
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
            showError("Navigation error", "Failed to load: " + fxml);
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
