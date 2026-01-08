package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.OrderDao;
import com.shajid.app.inventro.database.ProductDao;
import com.shajid.app.inventro.database.RatingDao;
import com.shajid.app.inventro.model.Product;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingController {

    public static class CartRow {
        private final IntegerProperty productId = new SimpleIntegerProperty();
        private final StringProperty  name      = new SimpleStringProperty();
        private final StringProperty  category  = new SimpleStringProperty();
        private final DoubleProperty  price     = new SimpleDoubleProperty();
        private final DoubleProperty  soldPrice = new SimpleDoubleProperty();
        private final IntegerProperty quantity  = new SimpleIntegerProperty(1);
        private final DoubleProperty  lineTotal = new SimpleDoubleProperty();

        public CartRow(Product p, int qty) {
            setProductId(p.getId() == null ? 0 : p.getId());
            setName(p.getName());
            setCategory(p.getCategory());
            setPrice(p.getPrice());
            setSoldPrice(p.getSoldPrice());
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

        public Product toProduct() {
            Product p = new Product();
            p.setId(getProductId());
            p.setName(getName());
            p.setCategory(getCategory());
            p.setPrice(getPrice());
            p.setSoldPrice(getSoldPrice());
            return p;
        }
    }

    @FXML private TableView<CartRow>            cartTable;
    @FXML private TableColumn<CartRow, String>  colName;
    @FXML private TableColumn<CartRow, String>  colCategory;
    @FXML private TableColumn<CartRow, Double>  colPrice;
    @FXML private TableColumn<CartRow, Integer> colQuantity;
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
        if (colPrice != null)    colPrice.setCellValueFactory(new PropertyValueFactory<>("soldPrice"));
        if (colQuantity != null) colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (colTotal != null)    colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        if (cartTable != null) {
            cartTable.setItems(cartItems);
        }

        updateTotals();
    }

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

        double discount = 0.0;
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
            List<Product> itemsForOrder = new ArrayList<>();
            for (CartRow row : cartItems) {
                for (int i = 0; i < row.getQuantity(); i++) {
                    itemsForOrder.add(row.toProduct());
                }
            }

            OrderDao.insertCustomerOrder(itemsForOrder);

            for (CartRow row : cartItems) {
                int pid = row.getProductId();
                if (pid > 0 && row.getQuantity() > 0) {
                    ProductDao.decreaseStock(pid, row.getQuantity());
                }
            }

            showInfo("Success", "Payment confirmed and bill saved.");

            // Show rating dialog
            if (SessionManager.getCurrentUserId() != null) {
                showRatingDialog();
            }

            cartItems.clear();
            updateTotals();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not save bill.");
        }
    }

    private void showRatingDialog() {
        // Get unique products from cart
        Map<Integer, String> uniqueProducts = new HashMap<>();
        for (CartRow row : cartItems) {
            if (row.getProductId() > 0) {
                uniqueProducts.put(row.getProductId(), row.getName());
            }
        }

        if (uniqueProducts.isEmpty()) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Rate Your Purchase");
        dialog.setHeaderText("Please rate the products you purchased");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #112233;");

        Map<Integer, ComboBox<Integer>> ratingBoxes = new HashMap<>();

        for (Map.Entry<Integer, String> entry : uniqueProducts.entrySet()) {
            int productId = entry.getKey();
            String productName = entry.getValue();

            VBox productBox = new VBox(5);
            Label nameLabel = new Label(productName);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

            HBox ratingRow = new HBox(10);
            ratingRow.setAlignment(Pos.CENTER_LEFT);

            Label ratingLabel = new Label("Rating:");
            ratingLabel.setStyle("-fx-text-fill: #88ffffff;");

            ComboBox<Integer> ratingBox = new ComboBox<>();
            ratingBox.getItems().addAll(1, 2, 3, 4, 5);
            ratingBox.setValue(5);
            ratingBox.setStyle("-fx-background-color: #223344; -fx-text-fill: white;");

            Label starLabel = new Label("★★★★★");
            starLabel.setStyle("-fx-text-fill: #FFC300; -fx-font-size: 16;");

            ratingRow.getChildren().addAll(ratingLabel, ratingBox, starLabel);
            productBox.getChildren().addAll(nameLabel, ratingRow);

            ratingBoxes.put(productId, ratingBox);
            content.getChildren().add(productBox);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the dialog
        dialog.getDialogPane().setStyle("-fx-background-color: #112233;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    Integer userId = SessionManager.getCurrentUserId();
                    if (userId != null) {
                        for (Map.Entry<Integer, ComboBox<Integer>> entry : ratingBoxes.entrySet()) {
                            int productId = entry.getKey();
                            Integer rating = entry.getValue().getValue();
                            if (rating != null) {
                                RatingDao.addOrUpdateRating(productId, userId, rating, null);
                            }
                        }
                        showInfo("Thank you!", "Your ratings have been saved.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", "Could not save ratings.");
                }
            }
            return null;
        });

        dialog.showAndWait();
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
