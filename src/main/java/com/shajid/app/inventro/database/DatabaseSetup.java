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
                + "    price REAL NOT NULL DEFAULT 0.0,"      // base price
                + "    soldPrice REAL NOT NULL DEFAULT 0.0"   // customer price (15% markup)
                + ");"

                + "CREATE TABLE IF NOT EXISTS orders ("
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "    supplier TEXT NOT NULL,"               // you can store \"Customer\" or empty
                + "    date TEXT NOT NULL,"
                + "    total REAL NOT NULL DEFAULT 0.0,"      // sum of sold prices for the bill
                + "    status TEXT NOT NULL,"
                + "    revenue REAL NOT NULL DEFAULT 0.0"     // total \- base cost
                + ");";

        try (Connection conn = SQLiteConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

            // For existing DBs: ensure soldPrice has values (15% markup on price)
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
