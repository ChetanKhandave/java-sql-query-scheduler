package com.example.sqlscheduler.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** DriverManager-based connection factory suitable for a small plain Java application. */
public final class DriverManagerConnectionFactory implements ConnectionFactory {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DriverManagerConnectionFactory(String driverClassName,
                                          String jdbcUrl,
                                          String username,
                                          String password) {
        loadDriver(driverClassName);
        this.jdbcUrl = requireText(jdbcUrl, "jdbcUrl");
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
    }

    private static void loadDriver(String driverClassName) {
        String value = requireText(driverClassName, "driverClassName");
        try {
            Class.forName(value);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("JDBC driver not found: " + value, exception);
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    @Override
    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
