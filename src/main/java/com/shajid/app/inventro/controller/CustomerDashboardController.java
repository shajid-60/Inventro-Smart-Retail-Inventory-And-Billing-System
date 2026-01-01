// Java
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerDashboardController {

    public static class ProductRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty category = new SimpleStringProperty();
        private final IntegerProperty stock = new SimpleIntegerProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();      // base price
        private final DoubleProperty soldPrice = new SimpleDoubleProperty();  // customer price

        public ProductRow(Product p) {
            setId(p.getId());
            setName(p.getName());
            setCategory(p.getCategory());
            setStock(p.getStock());
            setPrice(p.getPrice());
            setSoldPrice(p.getSoldPrice() == 0.0 ? p.getPrice() * 1.15 : p.getSoldPrice());
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

        public double getSoldPrice() { return soldPrice.get(); }
        public void setSoldPrice(double v) { soldPrice.set(v); }
        public DoubleProperty soldPriceProperty() { return soldPrice; }

        public Product toProduct() {
            Product p = new Product();
            p.setId(getId());
            p.setName(getName());
            p.setCategory(getCategory());
            p.setStock(getStock());
            p.setPrice(getPrice());
            p.setSoldPrice(getSoldPrice());
            return p;
        }
    }

    @FXML private TableView<ProductRow> productsTable;
    @FXML private TableColumn<ProductRow, Integer> colId;
    @FXML private TableColumn<ProductRow, String>  colName;
    @FXML private TableColumn<ProductRow, String>  colCategory;
    @FXML private TableColumn<ProductRow, Integer> colStock;
    @FXML private TableColumn<ProductRow, Double>  colSoldPrice;
    @FXML private TableColumn<ProductRow, Void>    colAction;

    @FXML private Label cartTotalLabel;

    private final ObservableList<ProductRow> products = FXCollections.observableArrayList();
    private final List<ProductRow> cart = new ArrayList<>();
    private double cartTotal = 0.0;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        if (colId != null)       colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colName != null)     colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colCategory != null) colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (colStock != null)    colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (colSoldPrice != null)colSoldPrice.setCellValueFactory(new PropertyValueFactory<>("soldPrice"));

        if (productsTable != null) {
            productsTable.setItems(products);
        }

        setupActionColumn();
        reloadProductsAsync();
        updateCartTotalLabel();
    }

    private void setupActionColumn() {
        if (colAction == null) return;

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Add to cart");

            {
                btn.setOnAction(e -> {
                    ProductRow row = getTableView().getItems().get(getIndex());
                    addToCart(row);
                });
                btn.setStyle(
                        "-fx-background-color: linear-gradient(to right,#00f2ea,#00c4c4);" +
                                "-fx-text-fill:white; -fx-background-radius:12; -fx-font-weight:bold;"
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void addToCart(ProductRow row) {
        if (row == null) return;
        if (row.getStock() <= 0) {
            showError("Out of stock", "This product is out of stock.");
            return;
        }
        cart.add(row);
        cartTotal += row.getSoldPrice();
        updateCartTotalLabel();
    }

    private void updateCartTotalLabel() {
        if (cartTotalLabel != null) {
            cartTotalLabel.setText(String.format("%.2f", cartTotal));
        }
    }

    private void reloadProductsAsync() {
        executor.submit(() -> {
            try {
                List<Product> list = ProductDao.findAll();
                List<ProductRow> rows = list.stream().map(ProductRow::new).toList();
                Platform.runLater(() -> products.setAll(rows));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Load failed", "Could not load products for customer."));
            }
        });
    }

    @FXML
    private void onGoToBilling(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/billing.fxml"));
            Parent root = loader.load();

            BillingController billingController = loader.getController();

            // Convert ProductRow cart items to Product list
            List<Product> cartProducts = new ArrayList<>();
            for (ProductRow r : cart) {
                cartProducts.add(r.toProduct());
            }
            billingController.setCartItems(cartProducts);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Inventro - Billing");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation error", "Failed to open billing page.");
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Inventro - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation error", "Failed to go back to login.");
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
