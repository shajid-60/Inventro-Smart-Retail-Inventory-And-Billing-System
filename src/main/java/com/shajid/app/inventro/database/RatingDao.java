package com.shajid.app.inventro.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RatingDao {

    /**
     * Add or update a rating for a product by a user.
     */
    public static void addOrUpdateRating(int productId, int userId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO ratings (productId, userId, rating, comment, date) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON CONFLICT(productId, userId) DO UPDATE SET "
                + "rating = excluded.rating, comment = excluded.comment, date = excluded.date;";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, rating);
            pstmt.setString(4, comment);
            pstmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            pstmt.executeUpdate();
        }
    }

    /**
     * Get average rating for a product.
     */
    public static double getAverageRating(int productId) throws SQLException {
        String sql = "SELECT AVG(rating) as avgRating FROM ratings WHERE productId = ?";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avgRating");
            }
            return 0.0;
        }
    }

    /**
     * Get rating count for a product.
     */
    public static int getRatingCount(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM ratings WHERE productId = ?";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }

    /**
     * Check if user has already rated a product.
     */
    public static Integer getUserRating(int productId, int userId) throws SQLException {
        String sql = "SELECT rating FROM ratings WHERE productId = ? AND userId = ?";

        try (Connection conn = SQLiteConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("rating");
            }
            return null;
        }
    }
}

