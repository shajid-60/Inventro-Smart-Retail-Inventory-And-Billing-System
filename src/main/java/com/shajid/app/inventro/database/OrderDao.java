// Java
package com.shajid.app.inventro.database;

import com.shajid.app.inventro.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class OrderDao {

    private OrderDao() { }

    // Save a customer bill as one order
    // \- `items` are products in the cart with quantity 1 each (you can later extend with quantity)
    public static void insertCustomerOrder(java.util.List<Product> items) throws SQLException {
        if (items == null || items.isEmpty()) return;

        double totalSold = 0.0;
        double totalBase = 0.0;

        for (Product p : items) {
            totalSold += p.getSoldPrice();
            totalBase += p.getPrice();
        }
        double revenue = totalSold - totalBase;

        String sql = "INSERT INTO orders(supplier, date, total, status, revenue) VALUES(?,?,?,?,?)";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "Customer");
            ps.setString(2, java.time.LocalDateTime.now().toString());
            ps.setDouble(3, totalSold);
            ps.setString(4, "COMPLETED");
            ps.setDouble(5, revenue);
            ps.executeUpdate();
        }
    }

    public static double computeTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(revenue) AS r FROM orders WHERE status='COMPLETED'";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("r");
            }
            return 0.0;
        }
    }
}
