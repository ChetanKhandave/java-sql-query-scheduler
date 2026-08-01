package com.example.sqlscheduler.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstraction for obtaining JDBC connections.
 *
 * <p>Depending on this interface allows the application to switch from direct
 * {@code DriverManager} connections to a pooled data source without changing query logic.</p>
 */
public interface ConnectionFactory {

    /**
     * Opens a new JDBC connection.
     *
     * @return an open connection owned by the caller
     * @throws SQLException when a connection cannot be established
     */
    Connection openConnection() throws SQLException;
}
