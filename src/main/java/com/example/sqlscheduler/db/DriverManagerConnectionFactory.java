package com.example.sqlscheduler.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections using {@link DriverManager}.
 *
 * <p>This implementation is suitable for a small plain Java application. A connection pool
 * can later replace it without changing query execution code because callers depend on the
 * {@link ConnectionFactory} interface.</p>
 */
public final class DriverManagerConnectionFactory implements ConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverManagerConnectionFactory.class);

    private final String jdbcUrl;
    private final String username;
    private final String password;

    /**
     * Loads the JDBC driver and stores connection parameters.
     *
     * @param driverClassName fully qualified JDBC driver class name
     * @param jdbcUrl JDBC connection URL
     * @param username database username; may be blank for embedded databases
     * @param password database password; may be blank for embedded databases
     */
    public DriverManagerConnectionFactory(String driverClassName,
                                          String jdbcUrl,
                                          String username,
                                          String password) {
        loadDriver(driverClassName);
        this.jdbcUrl = requireText(jdbcUrl, "jdbcUrl");
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;

        LOGGER.info("JDBC connection factory configured for URL {} and user {}",
                this.jdbcUrl, this.username.isEmpty() ? "<empty>" : this.username);
    }

    private static void loadDriver(String driverClassName) {
        String value = requireText(driverClassName, "driverClassName");
        try {
            Class.forName(value);
            LOGGER.debug("JDBC driver loaded: {}", value);
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

    /**
     * Opens a new JDBC connection.
     *
     * @return a newly opened connection
     * @throws SQLException when the database connection cannot be established
     */
    @Override
    public Connection openConnection() throws SQLException {
        LOGGER.debug("Opening JDBC connection to {}", jdbcUrl);
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
