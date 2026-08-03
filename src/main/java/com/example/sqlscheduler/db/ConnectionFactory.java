package com.example.sqlscheduler.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides JDBC connections without exposing whether they are created directly or
 * borrowed from a connection pool.
 */
public interface ConnectionFactory extends AutoCloseable {

    /**
     * Obtains a connection for one unit of work.
     *
     * <p>The caller must close the returned connection. With a pooled implementation,
     * closing the logical connection returns it to the pool.</p>
     *
     * @return open JDBC connection
     * @throws SQLException when a connection cannot be obtained
     */
    Connection openConnection() throws SQLException;

    /** Releases factory-level resources. Stateless implementations use this no-op default. */
    @Override
    default void close() {
        // No shared resources by default.
    }
}
