package com.example.sqlscheduler.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Initializes the embedded demonstration database when explicitly enabled.
 *
 * <p>The initialization statements are idempotent, allowing the application to restart
 * without creating duplicate demo rows.</p>
 */
public final class DemoDatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDatabaseInitializer.class);

    private final ConnectionFactory connectionFactory;

    /**
     * Creates a demo database initializer.
     *
     * @param connectionFactory factory used to open the initialization connection
     */
    public DemoDatabaseInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory, "connectionFactory must not be null");
    }

    /**
     * Creates the demo table if required and inserts or updates the sample row.
     *
     * @throws SQLException when database initialization fails
     */
    public void initialize() throws SQLException {
        LOGGER.info("Initializing demo database objects");
        try (Connection connection = connectionFactory.openConnection();
             Statement statement = connection.createStatement()) {

            LOGGER.debug("Creating SCHEDULED_EVENT table when it does not already exist");
            statement.execute("CREATE TABLE IF NOT EXISTS SCHEDULED_EVENT (" +
                    "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "EVENT_NAME VARCHAR(100) NOT NULL, " +
                    "CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");

            LOGGER.debug("Creating or updating the demo SCHEDULED_EVENT row");
            statement.execute("MERGE INTO SCHEDULED_EVENT (ID, EVENT_NAME) KEY(ID) " +
                    "VALUES (1, 'Application started')");
        }
        LOGGER.info("Demo database initialization completed");
    }
}
