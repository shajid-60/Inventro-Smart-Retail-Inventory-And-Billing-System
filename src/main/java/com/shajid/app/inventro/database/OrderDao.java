// src/main/java/com/shajid/app/inventro/database/OrderDao.java
package com.shajid.app.inventro.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class OrderDao {

    public static class OrderRecord {
        public final int id;
        public final String supplier;
        public final String date;
        public final double total;
        public final String status;

        public OrderRecord(int id, String supplier, String date, double total, String status) {
            this.id = id;
            this.supplier = supplier;
            this.date = date;
            this.total = total;
            this.status = status;
        }
    }

    private OrderDao() {}

    public static List<OrderRecord> findAll() throws SQLException {
        String sql = "SELECT id, supplier, date, total, status FROM orders ORDER BY id DESC";
        List<OrderRecord> out = new ArrayList<>();

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new OrderRecord(
                        rs.getInt("id"),
                        rs.getString("supplier"),
                        rs.getString("date"),
                        rs.getDouble("total"),
                        rs.getString("status")
                ));
            }
        }
        return out;
    }

    public static void insertAll(List<OrderRecord> orders) throws SQLException {
        String sql = "INSERT INTO orders(supplier, date, total, status) VALUES(?, ?, ?, ?)";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            try {
                for (OrderRecord o : orders) {
                    ps.setString(1, o.supplier);
                    ps.setString(2, o.date);
                    ps.setDouble(3, o.total);
                    ps.setString(4, o.status);
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

    public static void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE orders SET status=? WHERE id=?";
        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
