// Java
package com.shajid.app.inventro.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {
        String sql = ""
                + "PRAGMA foreign_keys = ON;"

                + "CREATE TABLE IF NOT EXISTS users ("
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "    fullName TEXT NOT NULL,"
                + "    age INTEGER,"
                + "    phone TEXT,"
                + "    address TEXT,"
                + "    email TEXT UNIQUE NOT NULL,"
                + "    password TEXT NOT NULL,"
                + "    role TEXT NOT NULL"
                + ");"

                + "CREATE TABLE IF NOT EXISTS products ("
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "    name TEXT NOT NULL,"
                + "    category TEXT,"
                + "    stock INTEGER NOT NULL DEFAULT 0,"
                + "    price REAL NOT NULL DEFAULT 0.0,"
                + "    soldPrice REAL NOT NULL DEFAULT 0.0,"
                + "    imagePath TEXT"
                + ");"

                + "CREATE TABLE IF NOT EXISTS ratings ("
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "    productId INTEGER NOT NULL,"
                + "    userId INTEGER NOT NULL,"
                + "    rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),"
                + "    comment TEXT,"
                + "    date TEXT NOT NULL,"
                + "    FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE,"
                + "    FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,"
                + "    UNIQUE(productId, userId)"
                + ");"

                + "CREATE TABLE IF NOT EXISTS orders ("
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "    supplier TEXT NOT NULL,"
                + "    date TEXT NOT NULL,"
                + "    total REAL NOT NULL DEFAULT 0.0,"
                + "    status TEXT NOT NULL,"
                + "    revenue REAL NOT NULL DEFAULT 0.0"
                + ");";

        try (Connection conn = SQLiteConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

            // Migrate: add imagePath column if it doesn't exist
            try {
                stmt.executeUpdate("ALTER TABLE products ADD COLUMN imagePath TEXT;");
                System.out.println("Added imagePath column to products table.");
            } catch (Exception e) {
                // Column already exists, ignore
            }

            String patchSql =
                    "UPDATE products SET soldPrice = price * 1.15 WHERE soldPrice IS NULL OR soldPrice = 0;";
            stmt.executeUpdate(patchSql);

            System.out.println("DB checked/created. Existing data preserved.");
        } catch (Exception e) {
            System.out.println("DB Setup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
