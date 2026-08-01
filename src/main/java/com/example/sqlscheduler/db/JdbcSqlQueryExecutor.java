package com.example.sqlscheduler.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Executes read-only SQL queries through JDBC.
 *
 * <p>All JDBC resources are managed with try-with-resources, so the result set, statement,
 * and connection are closed even when query execution or result processing fails.</p>
 */
public final class JdbcSqlQueryExecutor implements SqlQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSqlQueryExecutor.class);

    private final ConnectionFactory connectionFactory;

    /**
     * Creates an executor using the supplied connection factory.
     *
     * @param connectionFactory factory used to obtain database connections
     */
    public JdbcSqlQueryExecutor(ConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory, "connectionFactory must not be null");
    }

    /**
     * Executes the supplied query and delegates result processing to the handler.
     *
     * @param sql SQL SELECT statement to execute
     * @param timeoutSeconds JDBC query timeout in seconds
     * @param resultHandler handler that processes the returned result set
     * @throws SQLException when JDBC execution or result handling fails
     */
    @Override
    public void execute(String sql,
                        int timeoutSeconds,
                        QueryResultHandler resultHandler) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("sql must not be blank");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than zero");
        }
        Objects.requireNonNull(resultHandler, "resultHandler must not be null");

        LOGGER.debug("Opening database connection for scheduled query");
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setReadOnly(true);
            statement.setQueryTimeout(timeoutSeconds);
            LOGGER.debug("Executing read-only SQL query with timeout={} seconds", timeoutSeconds);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultHandler.handle(resultSet);
            }
            LOGGER.debug("SQL query and result processing completed successfully");
        }
    }
}
