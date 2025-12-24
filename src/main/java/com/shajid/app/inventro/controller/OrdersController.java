package com.shajid.app.inventro.controller;

import com.shajid.app.inventro.database.OrderDao;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

public class OrdersController {

    public void onSaveOrders(ActionEvent actionEvent) {
        reloadFromDb();
    }

    public static class OrderRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty supplier = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final DoubleProperty total = new SimpleDoubleProperty();
        private final StringProperty status = new SimpleStringProperty();

        public OrderRow() {}

        public OrderRow(int id, String supplier, String date, double total, String status) {
            setId(id);
            setSupplier(supplier);
            setDate(date);
            setTotal(total);
            setStatus(status);
        }

        public int getId() { return id.get(); }
        public void setId(int v) { id.set(v); }
        public IntegerProperty idProperty() { return id; }

        public String getSupplier() { return supplier.get(); }
        public void setSupplier(String v) { supplier.set(v); }
        public StringProperty supplierProperty() { return supplier; }

        public String getDate() { return date.get(); }
        public void setDate(String v) { date.set(v); }
        public StringProperty dateProperty() { return date; }

        public double getTotal() { return total.get(); }
        public void setTotal(double v) { total.set(v); }
        public DoubleProperty totalProperty() { return total; }

        public String getStatus() { return status.get(); }
        public void setStatus(String v) { status.set(v); }
        public StringProperty statusProperty() { return status; }
    }

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<OrderRow> ordersTable;
    @FXML private TableColumn<OrderRow, Integer> colId;
    @FXML private TableColumn<OrderRow, String> colSupplier;
    @FXML private TableColumn<OrderRow, String> colDate;
    @FXML private TableColumn<OrderRow, Double> colTotal;
    @FXML private TableColumn<OrderRow, String> colStatus;

    @FXML private TextField addSupplierField;
    @FXML private TextField addDateField;
    @FXML private TextField addTotalField;
    @FXML private TextField addStatusField;

    private final ObservableList<OrderRow> ordersList = FXCollections.observableArrayList();
    private FilteredList<OrderRow> filteredList;

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colSupplier != null) colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        if (colDate != null) colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (colTotal != null) colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        if (statusFilter != null) {
            statusFilter.getItems().setAll("All", "Unfulfilled", "Received", "Cancelled");
            statusFilter.setValue("All");
        }

        filteredList = new FilteredList<>(ordersList, r -> true);
        if (ordersTable != null) ordersTable.setItems(filteredList);

        if (searchField != null) searchField.textProperty().addListener((obs, ov, nv) -> applyFilters());
        if (statusFilter != null) statusFilter.valueProperty().addListener((obs, ov, nv) -> applyFilters());

        reloadFromDb();
    }

    private void reloadFromDb() {
        try {
            List<OrderDao.OrderRecord> rows = OrderDao.findAll();
            ordersList.setAll(rows.stream()
                    .map(r -> new OrderRow(r.id, r.supplier, r.date, r.total, r.status))
                    .toList());
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Load failed", "Could not load orders from database.");
        }
    }

    private void applyFilters() {
        String q = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        String status = statusFilter == null ? "All" : statusFilter.getValue();
        filteredList.setPredicate(createPredicate(q, status));
    }

    private Predicate<OrderRow> createPredicate(String query, String statusFilterValue) {
        final String q = query == null ? "" : query;
        final String sf = statusFilterValue == null ? "All" : statusFilterValue;

        return row -> {
            if (row == null) return false;

            boolean statusOk = "All".equalsIgnoreCase(sf) || (row.getStatus() != null && row.getStatus().equalsIgnoreCase(sf));
            if (!statusOk) return false;

            if (q.isEmpty()) return true;

            if (String.valueOf(row.getId()).contains(q)) return true;
            if (row.getSupplier() != null && row.getSupplier().toLowerCase(Locale.ROOT).contains(q)) return true;
            if (row.getDate() != null && row.getDate().toLowerCase(Locale.ROOT).contains(q)) return true;
            if (String.valueOf(row.getTotal()).contains(q)) return true;
            if (row.getStatus() != null && row.getStatus().toLowerCase(Locale.ROOT).contains(q)) return true;

            return false;
        };
    }

    @FXML
    private void onImportFromPdf(ActionEvent event) {
        File pdfFile = choosePdfFile(((Node) event.getSource()).getScene().getWindow());
        if (pdfFile == null) return;

        try {
            if (!Files.isRegularFile(pdfFile.toPath())) {
                showError("Invalid file", "Selected file is not valid.");
                return;
            }

            String text = extractTextFromPdf(pdfFile);
            List<OrderRow> parsed = parseOrdersFromPdfText(text);

            if (parsed.isEmpty()) {
                showError("Nothing imported", "No orders found.\nExpected format per line:\nsupplier \\| date \\| total \\| status");
                return;
            }

            List<OrderDao.OrderRecord> toInsert = parsed.stream()
                    .map(r -> new OrderDao.OrderRecord(0, r.getSupplier(), r.getDate(), r.getTotal(), r.getStatus()))
                    .toList();

            OrderDao.insertAll(toInsert);
            reloadFromDb();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Import failed", ex.getMessage() == null ? "Unknown error." : ex.getMessage());
        }
    }

    @FXML
    private void onNewOrder(ActionEvent event) {
        try {
            String supplier = valueOrEmpty(addSupplierField);
            String date = valueOrEmpty(addDateField);
            String totalRaw = valueOrEmpty(addTotalField);
            String status = valueOrEmpty(addStatusField);

            if (supplier.isBlank() || date.isBlank() || status.isBlank() || totalRaw.isBlank()) {
                showError("Invalid input", "Supplier, date, total, and status are required.");
                return;
            }

            double total = new BigDecimal(totalRaw).doubleValue();
            if (total < 0) {
                showError("Invalid input", "Total must be non-negative.");
                return;
            }

            OrderDao.insertAll(List.of(new OrderDao.OrderRecord(0, supplier, date, total, status)));

            if (addSupplierField != null) addSupplierField.clear();
            if (addDateField != null) addDateField.clear();
            if (addTotalField != null) addTotalField.clear();
            if (addStatusField != null) addStatusField.clear();

            reloadFromDb();

        } catch (Exception ex) {
            showError("Invalid input", "Enter a valid total (number).");
        }
    }

    @FXML
    private void onMarkReceived(ActionEvent event) {
        OrderRow selected = ordersTable == null ? null : ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Select an order first.");
            return;
        }
        try {
            OrderDao.updateStatus(selected.getId(), "Received");
            reloadFromDb();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Update failed", "Could not update order status.");
        }
    }

    @FXML
    private void onCancelOrder(ActionEvent event) {
        OrderRow selected = ordersTable == null ? null : ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Select an order first.");
            return;
        }
        try {
            OrderDao.updateStatus(selected.getId(), "Cancelled");
            reloadFromDb();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Update failed", "Could not update order status.");
        }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        switchScene(event, "/fxml/dashboard.fxml", "Inventro \\- Admin Dashboard");
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
        }
    }

    private File choosePdfFile(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Orders from PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        return chooser.showOpenDialog(owner);
    }

    private String extractTextFromPdf(File pdfFile) throws Exception {
        try (PDDocument doc = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private List<OrderRow> parseOrdersFromPdfText(String text) {
        List<OrderRow> result = new ArrayList<>();
        if (text == null || text.isBlank()) return result;

        String[] lines = text.split("\\R");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s*\\|\\s*");
            if (parts.length != 4) continue;

            String supplier = parts[0].trim();
            String date = parts[1].trim();
            Double total = tryParseDouble(parts[2].trim());
            String status = parts[3].trim();

            if (supplier.isEmpty() || date.isEmpty() || total == null || status.isEmpty()) continue;

            result.add(new OrderRow(0, supplier, date, total, status));
        }
        return result;
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
