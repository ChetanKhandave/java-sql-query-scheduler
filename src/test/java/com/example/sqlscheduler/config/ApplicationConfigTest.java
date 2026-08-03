package com.example.sqlscheduler.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests validation and mapping of application and Oracle UCP properties. */
class ApplicationConfigTest {

    @Test
    void shouldCreateConfigurationWhenAllRequiredValuesAreValid() {
        ApplicationConfig config = ApplicationConfig.from(validProperties());

        assertEquals("jdbc:oracle:thin:@//localhost:1521/ORCLPDB1", config.getJdbcUrl());
        assertEquals("SELECT 1 FROM DUAL", config.getQuery());
        assertEquals(30, config.getQueryTimeoutSeconds());
        assertEquals(0, config.getInitialDelaySeconds());
        assertEquals(10, config.getDelaySeconds());
        assertEquals("test-pool", config.getPoolName());
        assertEquals(1, config.getInitialPoolSize());
        assertEquals(1, config.getMinPoolSize());
        assertEquals(4, config.getMaxPoolSize());
        assertEquals(20, config.getConnectionWaitTimeoutSeconds());
        assertTrue(config.isValidateConnectionOnBorrow());
    }

    @Test
    void shouldRejectMissingRequiredProperty() {
        Properties properties = validProperties();
        properties.remove("db.pool.name");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));

        assertTrue(exception.getMessage().contains("db.pool.name"));
    }

    @Test
    void shouldRejectMinimumPoolSizeGreaterThanMaximumPoolSize() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.min-size", "5");
        properties.setProperty("db.pool.max-size", "4");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    @Test
    void shouldRejectInitialPoolSizeGreaterThanMaximumPoolSize() {
        Properties properties = validProperties();
        properties.setProperty("db.pool.initial-size", "5");
        properties.setProperty("db.pool.max-size", "4");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

    @Test
    void shouldRejectNonPositiveDelay() {
        Properties properties = validProperties();
        properties.setProperty("scheduler.delay.seconds", "0");

        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));
    }

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
