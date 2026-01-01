// Java
package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.OrderDao;
import com.shajid.app.inventro.database.ProductDao;
import com.shajid.app.inventro.model.Product;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BillingController {

    // Row for the cart table
    public static class CartRow {
        private final IntegerProperty productId = new SimpleIntegerProperty();
        private final StringProperty  name      = new SimpleStringProperty();
        private final StringProperty  category  = new SimpleStringProperty();
        private final DoubleProperty  price     = new SimpleDoubleProperty();   // base price
        private final DoubleProperty  soldPrice = new SimpleDoubleProperty();   // customer price
        private final IntegerProperty quantity  = new SimpleIntegerProperty(1);
        private final DoubleProperty  lineTotal = new SimpleDoubleProperty();

        public CartRow(Product p, int qty) {
            setProductId(p.getId() == null ? 0 : p.getId());
            setName(p.getName());
            setCategory(p.getCategory());
            setPrice(p.getPrice());          // base cost
            setSoldPrice(p.getSoldPrice());  // selling price
            setQuantity(qty);
            recalcTotal();
        }

        private void recalcTotal() {
            setLineTotal(getSoldPrice() * getQuantity());
        }

        public int getProductId() { return productId.get(); }
        public void setProductId(int v) { productId.set(v); }
        public IntegerProperty productIdProperty() { return productId; }

        public String getName() { return name.get(); }
        public void setName(String v) { name.set(v); }
        public StringProperty nameProperty() { return name; }

        public String getCategory() { return category.get(); }
        public void setCategory(String v) { category.set(v); }
        public StringProperty categoryProperty() { return category; }

        public double getPrice() { return price.get(); }
        public void setPrice(double v) { price.set(v); }
        public DoubleProperty priceProperty() { return price; }

        public double getSoldPrice() { return soldPrice.get(); }
        public void setSoldPrice(double v) {
            soldPrice.set(v);
            recalcTotal();
        }
        public DoubleProperty soldPriceProperty() { return soldPrice; }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int v) {
            quantity.set(v);
            recalcTotal();
        }
        public IntegerProperty quantityProperty() { return quantity; }

        public double getLineTotal() { return lineTotal.get(); }
        public void setLineTotal(double v) { lineTotal.set(v); }
        public DoubleProperty lineTotalProperty() { return lineTotal; }

        // Convert one cart line to a Product object (used by OrderDao)
        public Product toProduct() {
            Product p = new Product();
            p.setId(getProductId());
            p.setName(getName());
            p.setCategory(getCategory());
            p.setPrice(getPrice());          // base price (cost)
            p.setSoldPrice(getSoldPrice());  // selling price
            return p;
        }
    }

    @FXML private TableView<CartRow>            cartTable;
    @FXML private TableColumn<CartRow, String>  colName;
    @FXML private TableColumn<CartRow, String>  colCategory;
    @FXML private TableColumn<CartRow, Double>  colPrice;     // shows sold price
    @FXML private TableColumn<CartRow, Integer> colQuantity;  // quantity
    @FXML private TableColumn<CartRow, Double>  colTotal;

    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label grandTotalLabel;

    @FXML private Button confirmButton;

    private final ObservableList<CartRow> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colName != null)     colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colCategory != null) colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        // display soldPrice as the price column
        if (colPrice != null)    colPrice.setCellValueFactory(new PropertyValueFactory<>("soldPrice"));
        if (colQuantity != null) colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (colTotal != null)    colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        if (cartTable != null) {
            cartTable.setItems(cartItems);
        }

        updateTotals();
    }

    // Called from CustomerDashboardController to inject selected products
    public void setCartItems(List<Product> products) {
        cartItems.clear();
        if (products == null) return;

        for (Product p : products) {
            cartItems.add(new CartRow(p, 1));
        }
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cartItems.stream()
                .mapToDouble(CartRow::getLineTotal)
                .sum();

        double discount = 0.0; // apply any discount logic here
        double grandTotal = subtotal - discount;

        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("%.2f", subtotal));
        }
        if (discountLabel != null) {
            discountLabel.setText(String.format("%.2f", discount));
        }
        if (grandTotalLabel != null) {
            grandTotalLabel.setText(String.format("%.2f", grandTotal));
        }
    }

    @FXML
    private void onConfirmAndSaveBill(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showError("Empty cart", "There are no items to bill.");
            return;
        }

        try {
            // 1) Build products list for order (1 entry per quantity)
            List<Product> itemsForOrder = new ArrayList<>();
            for (CartRow row : cartItems) {
                for (int i = 0; i < row.getQuantity(); i++) {
                    itemsForOrder.add(row.toProduct());
                }
            }

            // 2) Persist order and revenue
            OrderDao.insertCustomerOrder(itemsForOrder);

            // 3) Decrease stock for each distinct product
            for (CartRow row : cartItems) {
                int pid = row.getProductId();
                if (pid > 0 && row.getQuantity() > 0) {
                    ProductDao.decreaseStock(pid, row.getQuantity());
                }
            }

            showInfo("Success", "Payment confirmed and bill saved.");
            cartItems.clear();
            updateTotals();


        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not save bill.");
        }
    }

    @FXML
    private void onBackToCustomerDashboard(ActionEvent event) {
        switchScene(event, "/fxml/customer_dashboard.fxml", "Inventro - Customer Dashboard");
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

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
