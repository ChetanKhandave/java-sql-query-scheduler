package com.example.sqlscheduler.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Strategy for processing a JDBC {@link ResultSet} while its owning JDBC resources remain open.
 */
public interface QueryResultHandler {

    /**
     * Processes the supplied result set.
     *
     * @param resultSet result set positioned before the first row
     * @throws SQLException when metadata or row access fails
     */
    void handle(ResultSet resultSet) throws SQLException;
}
