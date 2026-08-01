package com.example.sqlscheduler.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/** JDBC implementation that closes Connection, PreparedStatement and ResultSet correctly. */
public final class JdbcSqlQueryExecutor implements SqlQueryExecutor {

    private final ConnectionFactory connectionFactory;

    public JdbcSqlQueryExecutor(ConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory, "connectionFactory must not be null");
    }

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

        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setReadOnly(true);
            statement.setQueryTimeout(timeoutSeconds);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultHandler.handle(resultSet);
            }
        }
    }
}
