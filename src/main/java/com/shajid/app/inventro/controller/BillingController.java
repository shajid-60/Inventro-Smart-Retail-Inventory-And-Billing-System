
package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.OrderDao;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BillingController {

    // --- Row model for the cart table ---
    public static class CartRow {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty category = new SimpleStringProperty();
        private final DoubleProperty soldPrice = new SimpleDoubleProperty();
        private final IntegerProperty quantity = new SimpleIntegerProperty(1);
        private final DoubleProperty lineTotal = new SimpleDoubleProperty();

        public CartRow(Product p, int qty) {
            setName(p.getName());
            setCategory(p.getCategory());
            setSoldPrice(p.getSoldPrice());
            setQuantity(qty);
            recalcTotal();
        }

        private void recalcTotal() {
            setLineTotal(getSoldPrice() * getQuantity());
        }

        public String getName() { return name.get(); }
        public void setName(String v) { name.set(v); }
        public StringProperty nameProperty() { return name; }

        public String getCategory() { return category.get(); }
        public void setCategory(String v) { category.set(v); }
        public StringProperty categoryProperty() { return category; }

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

        // Convert back to Product for saving the order
        public Product toProduct() {
            Product p = new Product();
            p.setName(getName());
            p.setCategory(getCategory());
            p.setSoldPrice(getSoldPrice());
            return p;
        }
    }

    // --- FXML fields ---
    @FXML private TableView<CartRow> cartTable;
    @FXML private TableColumn<CartRow, String> colName;
    @FXML private TableColumn<CartRow, String> colCategory;
    @FXML private TableColumn<CartRow, Double> colPrice;
    @FXML private TableColumn<CartRow, Integer> colStock;   // quantity
    @FXML private TableColumn<CartRow, Double> colTotal;

    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label grandTotalLabel;

    @FXML private javafx.scene.control.Button confirmButton;

    private final ObservableList<CartRow> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colName != null)      colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colCategory != null)  colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (colPrice != null)     colPrice.setCellValueFactory(new PropertyValueFactory<>("soldPrice"));
        if (colStock != null)     colStock.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (colTotal != null)     colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        if (cartTable != null) {
            cartTable.setItems(cartItems);
        }

        updateTotals();
    }

    /**
     * Call this from the previous screen to pass the cart items.
     * If you have quantities, adjust the method signature accordingly.
     */
    public void setCartItems(List<Product> products) {
        cartItems.clear();
        if (products == null) return;
        for (Product p : products) {
            cartItems.add(new CartRow(p, 1)); // default quantity 1
        }
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cartItems.stream()
                .mapToDouble(CartRow::getLineTotal)
                .sum();

        double discount = 0.0; // put your discount logic here if needed
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

    // --- Confirm and save bill ---
    @FXML
    private void onConfirmAndSaveBill(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showError("Empty cart", "There are no items to bill.");
            return;
        }

        try {
            // Expand quantities into a list of products for OrderDao
            List<Product> itemsForOrder = new ArrayList<>();
            for (CartRow row : cartItems) {
                for (int i = 0; i < row.getQuantity(); i++) {
                    itemsForOrder.add(row.toProduct());
                }
            }

            OrderDao.insertCustomerOrder(itemsForOrder);

            showInfo("Success", "Payment confirmed and bill saved.");
            // Optionally clear the cart after saving
            cartItems.clear();
            updateTotals();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not save bill.");
        }
    }

    // --- Navigation back to customer dashboard ---
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
