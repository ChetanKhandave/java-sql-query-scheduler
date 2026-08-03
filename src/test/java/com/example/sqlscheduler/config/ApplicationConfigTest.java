package com.example.sqlscheduler.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies mapping and validation of scheduler, query and Oracle UCP configuration.
 */
class ApplicationConfigTest {

    /**
     * Verifies that a complete and valid properties set is converted into the expected
     * immutable configuration values, including all Oracle UCP settings.
     */
    @Test
    void shouldCreateConfigurationWhenAllRequiredValuesAreValid() {
        ApplicationConfig config = ApplicationConfig.from(validProperties());

        assertEquals("jdbc:oracle:thin:@//localhost:1521/ORCLPDB1", config.getJdbcUrl());
        assertEquals("app_user", config.getUsername());
        assertEquals("secret", config.getPassword());
        assertEquals("SELECT 1 FROM DUAL", config.getQuery());
        assertEquals(30, config.getQueryTimeoutSeconds());
        assertEquals(0, config.getInitialDelaySeconds());
        assertEquals(10, config.getDelaySeconds());
        assertEquals(5, config.getShutdownTimeoutSeconds());
        assertEquals("test-pool", config.getPoolName());
        assertEquals(1, config.getInitialPoolSize());
        assertEquals(1, config.getMinPoolSize());
        assertEquals(4, config.getMaxPoolSize());
        assertEquals(20, config.getConnectionWaitTimeoutSeconds());
        assertEquals(0, config.getInactiveConnectionTimeoutSeconds());
        assertTrue(config.isValidateConnectionOnBorrow());
        assertFalse(config.isInitializeDemoDatabase());
    }

    /**
     * Verifies that configuration creation fails with a useful message when a mandatory
     * Oracle UCP property is absent.
     */
    @Test
    void shouldRejectMissingRequiredProperty() {
        Properties properties = validProperties();
        properties.remove("db.pool.name");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));

        assertTrue(exception.getMessage().contains("db.pool.name"));
    }

    /**
     * Verifies that the minimum pool size cannot exceed the maximum pool size because
     * such a pool cannot satisfy its own lower and upper bounds.
     */
    @Test
    void shouldRejectMinimumPoolSizeGreaterThanMaximumPoolSize() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.min-size", "5");
        properties.setProperty("db.pool.max-size", "4");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    /**
     * Verifies that the initial pool size cannot exceed the maximum number of physical
     * connections permitted for the pool.
     */
    @Test
    void shouldRejectInitialPoolSizeGreaterThanMaximumPoolSize() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.initial-size", "5");
        properties.setProperty("db.pool.max-size", "4");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    /**
     * Verifies that a negative initial pool size is rejected instead of being passed to UCP.
     */
    @Test
    void shouldRejectNegativeInitialPoolSize() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.initial-size", "-1");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    /**
     * Verifies that the maximum pool size must be positive so the application can obtain
     * at least one database connection.
     */
    @Test
    void shouldRejectNonPositiveMaximumPoolSize() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.max-size", "0");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    /**
     * Verifies that the connection wait timeout must be positive; otherwise callers could
     * fail immediately or receive an invalid UCP configuration.
     */
    @Test
    void shouldRejectNonPositiveConnectionWaitTimeout() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.connection-wait-timeout-seconds", "0");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    /**
     * Verifies that the inactive connection timeout may be zero to disable reclamation,
     * but cannot be negative.
     */
    @Test
    void shouldRejectNegativeInactiveConnectionTimeout() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.inactive-connection-timeout-seconds", "-1");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    /**
     * Verifies that the fixed scheduling delay must be greater than zero to prevent a
     * continuously resubmitted task loop.
     */
    @Test
    void shouldRejectNonPositiveDelay() {
        Properties properties = validProperties();
        properties.setProperty("scheduler.delay.seconds", "0");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    /**
     * Verifies that numeric properties reject non-numeric text with a configuration error.
     */
    @Test
    void shouldRejectNonNumericTimeout() {
        Properties properties = validProperties();
        properties.setProperty("query.timeout.seconds", "abc");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    private static Properties validProperties() {
        Properties properties = new Properties();
        properties.setProperty("db.url", "jdbc:oracle:thin:@//localhost:1521/ORCLPDB1");
        properties.setProperty("db.username", "app_user");
        properties.setProperty("db.password", "secret");
        properties.setProperty("db.pool.name", "test-pool");
        properties.setProperty("db.pool.initial-size", "1");
        properties.setProperty("db.pool.min-size", "1");
        properties.setProperty("db.pool.max-size", "4");
        properties.setProperty("db.pool.connection-wait-timeout-seconds", "20");
        properties.setProperty("db.pool.inactive-connection-timeout-seconds", "0");
        properties.setProperty("db.pool.validate-connection-on-borrow", "true");
        properties.setProperty("query.sql", "SELECT 1 FROM DUAL");
        properties.setProperty("query.timeout.seconds", "30");
        properties.setProperty("scheduler.initial.delay.seconds", "0");
        properties.setProperty("scheduler.delay.seconds", "10");
        properties.setProperty("scheduler.shutdown.timeout.seconds", "5");
        properties.setProperty("database.initialize.demo", "false");
        return properties;
    }
}
