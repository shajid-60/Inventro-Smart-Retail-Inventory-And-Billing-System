package com.shajid.app.inventro.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shajid.app.inventro.database.ProductDao;
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
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductsController {

    @FXML private ScrollPane productScrollPane;
    @FXML private FlowPane productsContainer;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField stockField;
    @FXML private TextField priceField;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Integer selectedProductId = null;

    @FXML
    public void initialize() {
        if (productsContainer != null) {
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPadding(new Insets(20));
        }

        reloadFromDbAsync();
    }

    private void reloadFromDbAsync() {
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
                Platform.runLater(() -> showError("Load failed", "Could not load products from database."));
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

        // click to select product (used for Import Image)
        card.setOnMouseClicked(e -> selectProduct(product.getId(), card));

        // Product Image
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                File imageFile = new File(product.getImagePath());
                if (imageFile.exists()) {
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(240);
                    imageView.setFitHeight(140);
                    imageView.setPreserveRatio(true);
                    Image image = new Image(imageFile.toURI().toString(), 240, 140, true, true);
                    imageView.setImage(image);
                    card.getChildren().add(imageView);
                } else {
                    Label placeholderLabel = new Label("No Image");
                    placeholderLabel.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 14;");
                    StackPane placeholder = new StackPane(placeholderLabel);
                    placeholder.setPrefSize(240, 140);
                    placeholder.setStyle("-fx-background-color: #223344; -fx-background-radius: 8;");
                    card.getChildren().add(placeholder);
                }
            } catch (Exception e) {
                Label placeholderLabel = new Label("No Image");
                placeholderLabel.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 14;");
                StackPane placeholder = new StackPane(placeholderLabel);
                placeholder.setPrefSize(240, 140);
                placeholder.setStyle("-fx-background-color: #223344; -fx-background-radius: 8;");
                card.getChildren().add(placeholder);
            }
        } else {
            Label placeholderLabel = new Label("No Image");
            placeholderLabel.setStyle("-fx-text-fill: #88ffffff; -fx-font-size: 14;");
            StackPane placeholder = new StackPane(placeholderLabel);
            placeholder.setPrefSize(240, 140);
            placeholder.setStyle("-fx-background-color: #223344; -fx-background-radius: 8;");
            card.getChildren().add(placeholder);
        }

        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(240);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setFont(Font.font("System Bold", 16));
        nameLabel.setStyle("-fx-text-fill: white;");

        // Category
        Label categoryLabel = new Label(product.getCategory());
        categoryLabel.setFont(Font.font(12));
        categoryLabel.setStyle("-fx-text-fill: #88ffffff;");

        // Stock
        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.setFont(Font.font(12));
        stockLabel.setStyle("-fx-text-fill: " + (product.getStock() > 0 ? "#00f2ea" : "#C70039") + ";");

        // Price (base price for admin)
        Label priceLabel = new Label(String.format("Base Price: ৳%.2f", product.getPrice()));
        priceLabel.setFont(Font.font("System Bold", 14));
        priceLabel.setStyle("-fx-text-fill: #00f2ea;");

        // Sold price (customer price)
        Label soldPriceLabel = new Label(String.format("Sold Price: ৳%.2f", product.getSoldPrice()));
        soldPriceLabel.setFont(Font.font(12));
        soldPriceLabel.setStyle("-fx-text-fill: #FFC300;");

        // Rating display
        HBox ratingBox = createRatingDisplay(product.getAverageRating(), product.getRatingCount());

        // Buttons HBox
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);

        // Delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
                "-fx-background-color: #C70039;" +
                "-fx-background-radius: 8;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11;"
        );
        deleteBtn.setPrefWidth(120);
        deleteBtn.setOnAction(e -> deleteProduct(product));

        buttonBox.getChildren().add(deleteBtn);

        card.getChildren().addAll(nameLabel, categoryLabel, stockLabel, priceLabel, soldPriceLabel, ratingBox, buttonBox);

        return card;
    }

    private void selectProduct(Integer productId, VBox card) {
        selectedProductId = productId;
        if (productsContainer != null) {
            for (Node n : productsContainer.getChildren()) {
                if (n instanceof VBox v) {
                    v.setStyle(
                            "-fx-background-color: rgba(17,34,51,0.95);" +
                            "-fx-background-radius: 16;" +
                            "-fx-padding: 16;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0.3, 0, 5);"
                    );
                }
            }
        }
        // highlight selected card
        if (card != null) {
            card.setStyle(
                    "-fx-background-color: rgba(17,34,51,0.95);" +
                    "-fx-background-radius: 16;" +
                    "-fx-padding: 16;" +
                    "-fx-border-color: #00f2ea;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 16;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0.3, 0, 5);"
            );
        }
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

    @FXML
    private void onImportImage(ActionEvent event) {
        if (selectedProductId == null || selectedProductId <= 0) {
            showError("No selection", "Click a product card first to select it, then click Import Image.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Product Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = chooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file == null) return;

        String imagePath = file.getAbsolutePath();

        executor.submit(() -> {
            try {
                ProductDao.updateImagePath(selectedProductId, imagePath);
                Platform.runLater(() -> {
                    showInfo("Success", "Image saved for selected product.");
                    reloadFromDbAsync();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showError("Error", "Could not save image path."));
            }
        });
    }

    private void deleteProduct(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Product?");
        confirm.setContentText("Are you sure you want to delete: " + product.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                executor.submit(() -> {
                    try {
                        ProductDao.deleteById(product.getId());
                        Platform.runLater(() -> {
                            showInfo("Deleted", "Product deleted successfully!");
                            reloadFromDbAsync();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> showError("Delete failed", "Could not delete product."));
                    }
                });
            }
        });
    }

    @FXML
    private void onAddProduct(ActionEvent event) {
        try {
            String name = valueOrEmpty(nameField);
            String category = valueOrEmpty(categoryField);
            String stockText = valueOrEmpty(stockField);
            String priceText = valueOrEmpty(priceField);

            if (name.isBlank() || stockText.isBlank() || priceText.isBlank()) {
                showError("Invalid input", "Name, stock, and price are required.");
                return;
            }

            int stock = Integer.parseInt(stockText);
            double price = Double.parseDouble(priceText);

            if (stock < 0 || price < 0) {
                showError("Invalid input", "Stock and price must be non-negative.");
                return;
            }

            Product p = new Product(null, name, category, stock, price);

            executor.submit(() -> {
                try {
                    ProductDao.insert(p);
                    Platform.runLater(() -> {
                        if (nameField != null) nameField.clear();
                        if (categoryField != null) categoryField.clear();
                        if (stockField != null) stockField.clear();
                        if (priceField != null) priceField.clear();
                        showInfo("Added", "Product added successfully!");
                    });
                    reloadFromDbAsync();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> showError("Error", "Could not add product."));
                }
            });

        } catch (NumberFormatException ex) {
            showError("Invalid input", "Stock must be integer and price must be a number.");
        }
    }

    @FXML
    private void onExportJson(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Products to JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        File file = chooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (file == null) return;

        executor.submit(() -> {
            try {
                List<Product> list = ProductDao.findAll();
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
                Platform.runLater(() -> showInfo("Success", "Products exported successfully!"));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showError("Export failed", "Could not export products to JSON."));
            }
        });
    }

    @FXML
    private void onImportJson(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Products from JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        File file = chooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file == null) return;

        executor.submit(() -> {
            try {
                if (!Files.isRegularFile(file.toPath())) {
                    Platform.runLater(() -> showError("Invalid file", "Selected file is not valid."));
                    return;
                }

                List<Product> imported = mapper.readValue(file, new TypeReference<List<Product>>() {});

                for (Product p : imported) {
                    if (p.getName() == null || p.getName().isBlank()) {
                        Platform.runLater(() -> showError("Import failed", "Found product with empty name in JSON."));
                        return;
                    }
                    if (p.getStock() < 0 || p.getPrice() < 0) {
                        Platform.runLater(() -> showError("Import failed", "Stock and price must be non-negative."));
                        return;
                    }
                    p.setId(null);
                }

                for (Product p : imported) {
                    ProductDao.insert(p);
                }

                Platform.runLater(() -> showInfo("Success", "Products imported successfully!"));
                reloadFromDbAsync();
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showError("Import failed", "Could not import products from JSON."));
            }
        });
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        switchScene(event, "/fxml/dashboard.fxml", "Inventro - Admin Dashboard");
    }

    @FXML
    private void onDeleteProduct(ActionEvent event) {
        if (selectedProductId == null || selectedProductId <= 0) {
            showError("No selection", "Please click a product card first to select it, then click Delete Selected.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Selected Product?");
        confirm.setContentText("Are you sure you want to delete this product?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                executor.submit(() -> {
                    try {
                        ProductDao.deleteById(selectedProductId);
                        Platform.runLater(() -> {
                            showInfo("Deleted", "Product deleted successfully!");
                            selectedProductId = null;
                            reloadFromDbAsync();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> showError("Delete failed", "Could not delete product."));
                    }
                });
            }
        });
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

    private static String valueOrEmpty(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
