package com.shajid.app.inventro.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fullName TEXT,
                    email TEXT UNIQUE,
                    password TEXT,
                    role TEXT
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
