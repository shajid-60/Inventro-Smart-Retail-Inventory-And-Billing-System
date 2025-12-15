package com.shajid.app.inventro.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {
        String sql = """
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
                """;

        try (Connection conn = SQLiteConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            System.out.println("Users and products tables created/verified.");
        } catch (Exception e) {
            System.out.println("DB Setup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
