// src/main/java/com/shajid/app/inventro/database/ProductDao.java
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
                out.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("stock"),
                        rs.getDouble("price")
                ));
            }
        }
        return out;
    }

    public static void insert(Product p) throws SQLException {
        String sql = "INSERT INTO products(name, category, stock, price) VALUES(?, ?, ?, ?)";
        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrice());
            ps.executeUpdate();
        }
    }

    public static void insertAll(List<Product> products) throws SQLException {
        String sql = "INSERT INTO products(name, category, stock, price) VALUES(?, ?, ?, ?)";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            try {
                for (Product p : products) {
                    ps.setString(1, p.getName());
                    ps.setString(2, p.getCategory());
                    ps.setInt(3, p.getStock());
                    ps.setDouble(4, p.getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
