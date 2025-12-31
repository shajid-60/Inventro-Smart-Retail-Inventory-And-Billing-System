// Java
package com.shajid.app.inventro.database;

import com.shajid.app.inventro.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ProductDao {

    private ProductDao() { }

    public static List<Product> findAll() throws SQLException {
        String sql = "SELECT id, name, category, stock, price, soldPrice FROM products ORDER BY id DESC";
        List<Product> out = new ArrayList<>();

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setStock(rs.getInt("stock"));
                p.setPrice(rs.getDouble("price"));
                p.setSoldPrice(rs.getDouble("soldPrice"));
                out.add(p);
            }
        }
        return out;
    }

    public static void insert(Product p) throws SQLException {
        if (p.getSoldPrice() == 0.0) {
            p.setSoldPrice(p.getPrice() * 1.15);
        }

        String sql = "INSERT INTO products(name, category, stock, price, soldPrice) VALUES(?,?,?,?,?)";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrice());
            ps.setDouble(5, p.getSoldPrice());
            ps.executeUpdate();
        }
    }

    public static void update(Product p) throws SQLException {
        if (p.getId() == null) {
            throw new IllegalArgumentException("Product id required for update");
        }
        if (p.getSoldPrice() == 0.0) {
            p.setSoldPrice(p.getPrice() * 1.15);
        }

        String sql = "UPDATE products SET name=?, category=?, stock=?, price=?, soldPrice=? WHERE id=?";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrice());
            ps.setDouble(5, p.getSoldPrice());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        }
    }

    public static void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Decrease stock by quantity (used when items are sold)
    public static void decreaseStock(int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Not enough stock for product id=" + productId);
            }
        }
    }

    public static double computeTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(stock * (soldPrice - price)) AS revenue FROM products";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("revenue");
            }
            return 0.0;
        }
    }


    // Get one product (for billing calculations)
    public static Product findById(int id) throws SQLException {
        String sql = "SELECT id, name, category, stock, price, soldPrice FROM products WHERE id=?";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setStock(rs.getInt("stock"));
                p.setPrice(rs.getDouble("price"));
                p.setSoldPrice(rs.getDouble("soldPrice"));
                return p;
            }
        }
    }
}
