// Java
package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.ProductDao;
import com.shajid.app.inventro.model.Product;
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

import java.util.Comparator;
import java.util.List;
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
        loadSummaryAsync();
        loadChartAsync();
    }

    // --- Load summary labels ---
// Java
    @FXML private Label revenueLabel;
    private void loadSummaryAsync() {
        executor.submit(() -> {
            try {
                List<Product> list = ProductDao.findAll();

                int totalProducts = list.size();
                long lowStock = list.stream()
                        .filter(p -> p.getStock() >= 1 && p.getStock() <= 5)
                        .count();
                long outOfStock = list.stream()
                        .filter(p -> p.getStock() == 0)
                        .count();
                double stockValue = list.stream()
                        .mapToDouble(p -> p.getStock() * p.getPrice())
                        .sum();

                // New: compute total revenue from orders
                double totalRevenue = com.shajid.app.inventro.database.OrderDao.computeTotalRevenue();

                Platform.runLater(() -> {
                    if (totalProductsLabel != null) {
                        totalProductsLabel.setText(String.valueOf(totalProducts));
                    }
                    if (lowStockLabel != null) {
                        lowStockLabel.setText(String.valueOf(lowStock));
                    }
                    if (outOfStockLabel != null) {
                        outOfStockLabel.setText(String.valueOf(outOfStock));
                    }
                    if (stockValueLabel != null) {
                        stockValueLabel.setText(String.format("%.2f", stockValue));
                    }

                    if (revenueLabel != null) {
                        revenueLabel.setText(String.format("%.2f", totalRevenue));
                    }


                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Load failed", "Could not load dashboard summary."));
            }
        });
    }


    // --- Load bar chart (Top 10 by stock) ---
    private void loadChartAsync() {
        executor.submit(() -> {
            try {
                List<Product> list = ProductDao.findAll();

                List<Product> top10 = list.stream()
                        .sorted(Comparator.comparingInt(Product::getStock).reversed())
                        .limit(10)
                        .toList();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                for (Product p : top10) {
                    series.getData().add(new XYChart.Data<>(p.getName(), p.getStock()));
                }

                Platform.runLater(() -> {
                    if (stockChart != null) {
                        stockChart.getData().clear();
                        stockChart.getData().add(series);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Load failed", "Could not load stock chart."));
            }
        });
    }

    // --- Navigation ---
    @FXML
    private void onManageProducts(ActionEvent event) {
        switchScene(event, "/fxml/products.fxml", "Inventro - Products");
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

    private void showError(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
