// src/main/java/com/shajid/app/inventro/controller/ProductsController.java
package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.ProductDao;
import com.shajid.app.inventro.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class ProductsController {

    @FXML private TextField searchField;

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Double> colPrice;

    @FXML private TextField addNameField;
    @FXML private TextField addCategoryField;
    @FXML private TextField addStockField;
    @FXML private TextField addPriceField;

    private final ObservableList<Product> productsList = FXCollections.observableArrayList();
    private FilteredList<Product> filteredList;

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colName != null) colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colCategory != null) colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (colStock != null) colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (colPrice != null) colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        filteredList = new FilteredList<>(productsList, p -> true);
        if (productsTable != null) productsTable.setItems(filteredList);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, ov, nv) -> applyFilters());
        }

        reloadFromDb();
    }

    private void reloadFromDb() {
        try {
            productsList.setAll(ProductDao.findAll());
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Load failed", "Could not load products from database.");
        }
    }

    private void applyFilters() {
        String q = (searchField == null || searchField.getText() == null)
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        if (filteredList != null) filteredList.setPredicate(createPredicate(q));
    }

    private Predicate<Product> createPredicate(String query) {
        final String q = query == null ? "" : query;

        return p -> {
            if (p == null) return false;
            if (q.isEmpty()) return true;

            if (p.getId() != null && String.valueOf(p.getId()).contains(q)) return true;
            if (p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(q)) return true;
            if (p.getCategory() != null && p.getCategory().toLowerCase(Locale.ROOT).contains(q)) return true;
            if (String.valueOf(p.getStock()).contains(q)) return true;
            if (String.valueOf(p.getPrice()).contains(q)) return true;

            return false;
        };
    }

    @FXML
    private void onAddProduct(ActionEvent event) {
        try {
            String name = valueOrEmpty(addNameField);
            String category = valueOrEmpty(addCategoryField);
            String stockRaw = valueOrEmpty(addStockField);
            String priceRaw = valueOrEmpty(addPriceField);

            if (name.isBlank() || stockRaw.isBlank() || priceRaw.isBlank()) {
                showError("Invalid input", "Name, stock, and price are required.");
                return;
            }

            int stock = Integer.parseInt(stockRaw);
            if (stock < 0) {
                showError("Invalid input", "Stock must be non\\-negative.");
                return;
            }

            double price = new BigDecimal(priceRaw).doubleValue();
            if (price < 0) {
                showError("Invalid input", "Price must be non\\-negative.");
                return;
            }

            ProductDao.insert(new Product(null, name, category, stock, price));

            if (addNameField != null) addNameField.clear();
            if (addCategoryField != null) addCategoryField.clear();
            if (addStockField != null) addStockField.clear();
            if (addPriceField != null) addPriceField.clear();

            reloadFromDb();

        } catch (NumberFormatException ex) {
            showError("Invalid input", "Stock must be an integer and price must be a number.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error", ex.getMessage() == null ? "Unknown error." : ex.getMessage());
        }
    }

    @FXML
    private void onSaveProducts(ActionEvent event) {
        // Minimal: products are saved when added/imported.
        // If you later enable in-table editing, this button can persist edits.
        reloadFromDb();
    }

    @FXML
    private void onImportFromPdf(ActionEvent event) {
        Window owner = event != null && event.getSource() instanceof Node n ? n.getScene().getWindow() : null;
        File pdfFile = choosePdfFile(owner);
        if (pdfFile == null) return;

        try {
            if (!Files.isRegularFile(pdfFile.toPath())) {
                showError("Invalid file", "Selected file is not valid.");
                return;
            }

            String text = extractTextFromPdf(pdfFile);
            List<Product> parsed = parseProductsFromPdfText(text);

            if (parsed.isEmpty()) {
                showError("Nothing imported", "No products found.\\nExpected format per line:\\nname \\| category \\| stock \\| price");
                return;
            }

            ProductDao.insertAll(parsed);
            reloadFromDb();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Import failed", ex.getMessage() == null ? "Unknown error." : ex.getMessage());
        }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        switchScene(event, "/fxml/dashboard.fxml", "Inventro \\- Admin Dashboard");
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File choosePdfFile(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Products from PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        return chooser.showOpenDialog(owner);
    }

    private String extractTextFromPdf(File pdfFile) throws Exception {
        try (PDDocument doc = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private List<Product> parseProductsFromPdfText(String text) {
        List<Product> result = new ArrayList<>();
        if (text == null || text.isBlank()) return result;

        String[] lines = text.split("\\R");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s*\\|\\s*");
            if (parts.length != 4) continue;

            String name = parts[0].trim();
            String category = parts[1].trim();
            Integer stock = tryParseInt(parts[2].trim());
            Double price = tryParseDouble(parts[3].trim());

            if (name.isEmpty() || stock == null || price == null) continue;

            result.add(new Product(null, name, category, stock, price));
        }

        return result;
    }

    private static Integer tryParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private static Double tryParseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static String valueOrEmpty(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
