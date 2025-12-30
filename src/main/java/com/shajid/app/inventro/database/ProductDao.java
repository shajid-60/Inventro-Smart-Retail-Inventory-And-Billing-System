package com.shajid.app.inventro.database;

import com.shajid.app.inventro.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ProductDao {

    private ProductDao() {}

    public static List<Product> findAll() throws SQLException {
        String sql = "SELECT id, name, category, stock, price FROM products ORDER BY id DESC";
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
                out.add(p);
            }
        }
        return out;
    }

    public static void insert(Product p) throws SQLException {
        String sql = "INSERT INTO products(name, category, stock, price) VALUES(?,?,?,?)";
        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrice());
            ps.executeUpdate();
        }
    }

    public static void update(Product p) throws SQLException {
        if (p.getId() == null) {
            throw new IllegalArgumentException("Product id required for update");
        }
        String sql = "UPDATE products SET name=?, category=?, stock=?, price=? WHERE id=?";
        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getId());
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
}
