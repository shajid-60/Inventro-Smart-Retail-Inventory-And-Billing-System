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
                """;

        try (Connection conn = SQLiteConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Users table created/verified.");
        } catch (Exception e) {
            System.out.println("DB Setup Error: " + e.getMessage());
        }
    }
}
