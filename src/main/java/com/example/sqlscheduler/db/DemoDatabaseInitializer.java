package com.example.sqlscheduler.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Creates demo data only when explicitly enabled in configuration. */
public final class DemoDatabaseInitializer {

    private final ConnectionFactory connectionFactory;

    public DemoDatabaseInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory, "connectionFactory must not be null");
    }

    public void initialize() throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("CREATE TABLE IF NOT EXISTS SCHEDULED_EVENT (" +
                    "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "EVENT_NAME VARCHAR(100) NOT NULL, " +
                    "CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");

            statement.execute("MERGE INTO SCHEDULED_EVENT (ID, EVENT_NAME) KEY(ID) " +
                    "VALUES (1, 'Application started')");
        }
    }
}
