package com.shajid.app.inventro.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {

    private static final String URL = "jdbc:sqlite:inventro.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
