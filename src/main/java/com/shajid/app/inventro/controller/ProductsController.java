package com.shajid.app.inventro.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductsController {

    // --- Table row wrapper for JavaFX binding ---
    public static class ProductRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty category = new SimpleStringProperty();
        private final IntegerProperty stock = new SimpleIntegerProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();

        public ProductRow() {}

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

        public Product toProduct() {
            Product p = new Product();
            p.setId(getId());
            p.setName(getName());
            p.setCategory(getCategory());
            p.setStock(getStock());
            p.setPrice(getPrice());
            return p;
        }
    }

    // --- FXML fields ---

    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField stockField;
    @FXML private TextField priceField;

    @FXML private TableView<ProductRow> productsTable;
    @FXML private TableColumn<ProductRow, Integer> colId;
    @FXML private TableColumn<ProductRow, String> colName;
    @FXML private TableColumn<ProductRow, String> colCategory;
    @FXML private TableColumn<ProductRow, Integer> colStock;
    @FXML private TableColumn<ProductRow, Double> colPrice;

    private final ObservableList<ProductRow> products = FXCollections.observableArrayList();
    private final ObjectMapper mapper = new ObjectMapper();
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

        reloadFromDbAsync();
    }

    private void reloadFromDbAsync() {
        executor.submit(() -> {
            try {
                List<Product> list = ProductDao.findAll();
                Platform.runLater(() -> products.setAll(list.stream().map(ProductRow::new).toList()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Load failed", "Could not load products from database."));
            }
        });
    }

    private void reloadFromDb() {
        reloadFromDbAsync();
    }

    // --- Add product from text fields ---
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

    // --- Delete selected product ---
    @FXML
    private void onDeleteProduct(ActionEvent event) {
        ProductRow selected = productsTable == null ? null : productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Select a product to delete.");
            return;
        }

        int id = selected.getId();
        executor.submit(() -> {
            try {
                ProductDao.deleteById(id);
                reloadFromDbAsync();
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showError("Delete failed", "Could not delete selected product."));
            }
        });
    }

    // --- Export products as JSON ---
    @FXML
    private void onExportJson(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Products to JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        File file = chooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (file == null) return;

        executor.submit(() -> {
            try {
                List<Product> list = products.stream().map(ProductRow::toProduct).toList();
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showError("Export failed", "Could not export products to JSON."));
            }
        });
    }

    // --- Import products from JSON ---
// Java
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

                // Read JSON
                List<Product> imported = mapper.readValue(file, new TypeReference<List<Product>>() {});

                // Basic validation + reset IDs
                for (Product p : imported) {
                    if (p.getName() == null || p.getName().isBlank()) {
                        Platform.runLater(() -> showError("Import failed",
                                "Found product with empty name in JSON."));
                        return;
                    }
                    if (p.getStock() < 0 || p.getPrice() < 0) {
                        Platform.runLater(() -> showError("Import failed",
                                "Stock and price must be non-negative."));
                        return;
                    }
                    p.setId(null); // let DB assign ID
                }

                // Insert all
                for (Product p : imported) {
                    ProductDao.insert(p);
                }

                // Reload table
                reloadFromDbAsync();
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showError(
                        "Import failed",
                        "Could not import products from JSON.\n" +
                                "Make sure JSON is an array of objects with fields: id, name, category, stock, price."
                ));
            }
        });
    }

    // --- Optional: import from PDF (simple line\-based format) ---
    @FXML
    public void onImportFromPdf(ActionEvent event) {
        showError("Not implemented", "PDF import for products is not implemented.\nUse JSON import instead.");
        // If you want full PDF parsing like orders, you can copy the PDFBox logic
        // from OrdersController and define a format such as:
        // name \| category \| stock \| price per line.
    }

    // --- Navigation ---
    @FXML
    private void goToDashboard(ActionEvent event) {
        switchScene(event, "/fxml/dashboard.fxml", "Inventro - Admin Dashboard");
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
            showError("Navigation error", "Failed to load: " + fxml + "\n\n" + (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
        }
    }

    // --- Helpers ---
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
}
