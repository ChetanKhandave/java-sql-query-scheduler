package com.example.sqlscheduler.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationConfigTest {

    @Test
    void shouldCreateConfigurationWhenAllRequiredValuesAreValid() {
        ApplicationConfig config = ApplicationConfig.from(validProperties());

        assertEquals("org.h2.Driver", config.getDriverClassName());
        assertEquals("jdbc:h2:mem:test", config.getJdbcUrl());
        assertEquals("SELECT 1", config.getQuery());
        assertEquals(30, config.getQueryTimeoutSeconds());
        assertEquals(0, config.getInitialDelaySeconds());
        assertEquals(10, config.getDelaySeconds());
        assertTrue(config.isInitializeDemoDatabase());
    }

    @Test
    void shouldRejectMissingRequiredProperty() {
        Properties properties = validProperties();
        properties.remove("query.sql");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApplicationConfig.from(properties));

        assertTrue(exception.getMessage().contains("query.sql"));
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
        properties.setProperty("db.driver", "org.h2.Driver");
        properties.setProperty("db.url", "jdbc:h2:mem:test");
        properties.setProperty("db.username", "sa");
        properties.setProperty("db.password", "");
        properties.setProperty("query.sql", "SELECT 1");
        properties.setProperty("query.timeout.seconds", "30");
        properties.setProperty("scheduler.initial.delay.seconds", "0");
        properties.setProperty("scheduler.delay.seconds", "10");
        properties.setProperty("scheduler.shutdown.timeout.seconds", "5");
        properties.setProperty("database.initialize.demo", "true");
        return properties;
    }
}
