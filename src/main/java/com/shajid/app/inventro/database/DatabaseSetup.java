// src/main/java/com/shajid/app/inventro/database/DatabaseSetup.java
package com.shajid.app.inventro.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {
        String sql = """
                PRAGMA foreign_keys = ON;

                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fullName TEXT NOT NULL,
                    age INTEGER,
                    phone TEXT,
                    address TEXT,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT,
                    stock INTEGER NOT NULL DEFAULT 0,
                    price REAL NOT NULL DEFAULT 0.0
                );

                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    supplier TEXT NOT NULL,
                    date TEXT NOT NULL,
                    total REAL NOT NULL DEFAULT 0.0,
                    status TEXT NOT NULL
                );
                """;

        try (Connection conn = SQLiteConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            System.out.println("Users, products, and orders tables created/verified.");
        } catch (Exception e) {
            System.out.println("DB Setup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
