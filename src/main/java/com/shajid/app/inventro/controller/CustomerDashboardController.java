// Java
package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.ProductDao;
import com.shajid.app.inventro.database.RatingDao;
import com.shajid.app.inventro.model.Product;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerDashboardController {

    @FXML private ScrollPane productScrollPane;
    @FXML private FlowPane productsContainer;
    @FXML private Label cartTotalLabel;

    private final List<Product> cart = new ArrayList<>();
    private double cartTotal = 0.0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        if (productsContainer != null) {
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPadding(new Insets(20));
        }

        reloadProductsAsync();
        updateCartTotalLabel();
    }

    private void reloadProductsAsync() {
        executor.submit(() -> {
            try {
                List<Product> list = ProductDao.findAll();
                Platform.runLater(() -> {
                    if (productsContainer != null) {
                        productsContainer.getChildren().clear();
                        for (Product p : list) {
                            productsContainer.getChildren().add(createProductCard(p));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Load failed", "Could not load products for customer."));
            }
        });
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(280);
        card.setMinHeight(380);
        card.setMaxHeight(420);
        card.setStyle(
            "-fx-background-color: rgba(17,34,51,0.95);" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0.3, 0, 5);"
        );

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: #223344; -fx-background-radius: 8;");

        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                File imageFile = new File(product.getImagePath());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), 240, 140, true, true);
                    imageView.setImage(image);
                } else {
                    // Show placeholder if image file doesn't exist
                    Label placeholderLabel = new Label("No Image");
                    placeholderLabel.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 14;");
                    StackPane placeholder = new StackPane(placeholderLabel);
                    placeholder.setPrefSize(240, 140);
                    placeholder.setStyle("-fx-background-color: #223344; -fx-background-radius: 8;");
                    card.getChildren().add(placeholder);
                }
            } catch (Exception e) {
                // If image loading fails, show placeholder
                Label placeholderLabel = new Label("No Image");
                placeholderLabel.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 14;");
                StackPane placeholder = new StackPane(placeholderLabel);
                placeholder.setPrefSize(240, 140);
                placeholder.setStyle("-fx-background-color: #223344; -fx-background-radius: 8;");
                card.getChildren().add(placeholder);
            }
        } else {
            // Show placeholder if no image path
            Label placeholderLabel = new Label("No Image");
            placeholderLabel.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 14;");
            StackPane placeholder = new StackPane(placeholderLabel);
            placeholder.setPrefSize(240, 140);
            placeholder.setStyle("-fx-background-color: #223344; -fx-background-radius: 8;");
            card.getChildren().add(placeholder);
        }

        // Only add imageView if it has an image
        if (imageView.getImage() != null) {
            card.getChildren().add(imageView);
        }

        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(240);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setFont(Font.font("System Bold", 16));
        nameLabel.setStyle("-fx-text-fill: white;");

        // Rating display
        HBox ratingBox = createRatingDisplay(product.getAverageRating(), product.getRatingCount());

        // Price (customer price)
        Label priceLabel = new Label(String.format("৳%.2f", product.getSoldPrice()));
        priceLabel.setFont(Font.font("System Bold", 18));
        priceLabel.setStyle("-fx-text-fill: #00f2ea;");

        // Stock
        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.setFont(Font.font(12));
        stockLabel.setStyle("-fx-text-fill: " + (product.getStock() > 0 ? "#00f2ea" : "#C70039") + ";");

        // Add to cart button
        Button addButton = new Button("Add to Cart");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setStyle(
            "-fx-background-color: linear-gradient(to right,#00f2ea,#00c4c4);" +
            "-fx-background-radius: 12;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14;"
        );
        addButton.setPrefHeight(36);

        if (product.getStock() <= 0) {
            addButton.setDisable(true);
            addButton.setText("Out of Stock");
        }

        addButton.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(nameLabel, ratingBox, priceLabel, stockLabel, addButton);

        return card;
    }

    private HBox createRatingDisplay(double avgRating, int count) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER);

        if (count == 0) {
            Label noRatingLabel = new Label("Not rated yet");
            noRatingLabel.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 11;");
            box.getChildren().add(noRatingLabel);
        } else {
            // Show stars
            for (int i = 1; i <= 5; i++) {
                Label star = new Label(i <= Math.round(avgRating) ? "★" : "☆");
                star.setStyle("-fx-text-fill: #FFC300; -fx-font-size: 16;");
                box.getChildren().add(star);
            }

            Label ratingText = new Label(String.format("%.1f (%d)", avgRating, count));
            ratingText.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 11;");
            box.getChildren().add(ratingText);
        }

        return box;
    }

    private void addToCart(Product product) {
        if (product.getStock() <= 0) {
            showError("Out of stock", "This product is out of stock.");
            return;
        }
        cart.add(product);
        cartTotal += product.getSoldPrice();
        updateCartTotalLabel();
        showInfo("Added", product.getName() + " added to cart!");
    }

    private void updateCartTotalLabel() {
        if (cartTotalLabel != null) {
            cartTotalLabel.setText(String.format("৳%.2f", cartTotal));
        }
    }

    @FXML
    private void onGoToBilling(ActionEvent event) {
        if (cart.isEmpty()) {
            showError("Empty Cart", "Please add items to cart before checkout.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billing.fxml"));
            Parent root = loader.load();

            BillingController billingController = loader.getController();
            billingController.setCartItems(new ArrayList<>(cart));

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
            SessionManager.clearSession();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
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

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}
