package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.ProductDao;
import com.shajid.app.inventro.model.Product;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerDashboardController {

    public static class ProductRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty category = new SimpleStringProperty();
        private final IntegerProperty stock = new SimpleIntegerProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();

        public ProductRow(Product p) {
            setId(p.getId());
            setName(p.getName());
            setCategory(p.getCategory());
            setStock(p.getStock());
            setPrice(p.getPrice());
        }

        public Integer getId() { return id.get(); }
        public void setId(Integer v) { id.set(v == null ? 0 : v); }
        public IntegerProperty idProperty() { return id; }

        public String getName() { return name.get(); }
        public void setName(String v) { name.set(v); }
        public StringProperty nameProperty() { return name; }

        public String getCategory() { return category.get(); }
        public void setCategory(String v) { category.set(v); }
        public StringProperty categoryProperty() { return category; }

        public int getStock() { return stock.get(); }
        public void setStock(int v) { stock.set(v); }
        public IntegerProperty stockProperty() { return stock; }

        public double getPrice() { return price.get(); }
        public void setPrice(double v) { price.set(v); }
        public DoubleProperty priceProperty() { return price; }
    }

    @FXML private TableView<ProductRow> productsTable;
    @FXML private TableColumn<ProductRow, Integer> colId;
    @FXML private TableColumn<ProductRow, String> colName;
    @FXML private TableColumn<ProductRow, String> colCategory;
    @FXML private TableColumn<ProductRow, Integer> colStock;
    @FXML private TableColumn<ProductRow, Double> colPrice;

    private final ObservableList<ProductRow> products = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        if (colId != null)      colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colName != null)    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colCategory != null)colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (colStock != null)   colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (colPrice != null)   colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        if (productsTable != null) {
            productsTable.setItems(products);
        }

        reloadProductsAsync();
    }

    private void reloadProductsAsync() {
        executor.submit(() -> {
            try {
                List<Product> list = ProductDao.findAll();
                List<ProductRow> rows = list.stream().map(ProductRow::new).toList();
                Platform.runLater(() -> products.setAll(rows));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Load failed", "Could not load products for customer."));
            }
        });
    }

    @FXML
    private void onGoToBilling(ActionEvent event) {
        switchScene(event, "/fxml/billing.fxml", "Inventro - Billing");
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
