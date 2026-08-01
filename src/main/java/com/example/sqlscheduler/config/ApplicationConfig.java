package com.example.sqlscheduler.config;

import java.util.Objects;
import java.util.Properties;

/** Immutable application configuration. */
public final class ApplicationConfig {

    private final String driverClassName;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String query;
    private final int queryTimeoutSeconds;
    private final long initialDelaySeconds;
    private final long delaySeconds;
    private final long shutdownTimeoutSeconds;
    private final boolean initializeDemoDatabase;

    private ApplicationConfig(Properties properties) {
        this.driverClassName = required(properties, "db.driver");
        this.jdbcUrl = required(properties, "db.url");
        this.username = properties.getProperty("db.username", "");
        this.password = properties.getProperty("db.password", "");
        this.query = required(properties, "query.sql");
        this.queryTimeoutSeconds = positiveInt(properties, "query.timeout.seconds");
        this.initialDelaySeconds = nonNegativeLong(properties, "scheduler.initial.delay.seconds");
        this.delaySeconds = positiveLong(properties, "scheduler.delay.seconds");
        this.shutdownTimeoutSeconds = positiveLong(properties, "scheduler.shutdown.timeout.seconds");
        this.initializeDemoDatabase = Boolean.parseBoolean(
                properties.getProperty("database.initialize.demo", "false"));
    }

    public static ApplicationConfig from(Properties properties) {
        return new ApplicationConfig(Objects.requireNonNull(properties, "properties must not be null"));
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        return value.trim();
    }

    private static int positiveInt(Properties properties, String key) {
        long value = positiveLong(properties, key);
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(key + " exceeds Integer.MAX_VALUE");
        }
        return (int) value;
    }

    private static long positiveLong(Properties properties, String key) {
        long value = parseLong(properties, key);
        if (value <= 0) {
            throw new IllegalArgumentException(key + " must be greater than zero");
        }
        return value;
    }

    private static long nonNegativeLong(Properties properties, String key) {
        long value = parseLong(properties, key);
        if (value < 0) {
            throw new IllegalArgumentException(key + " must not be negative");
        }
        return value;
    }

    private static long parseLong(Properties properties, String key) {
        String value = required(properties, key);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be a valid number", exception);
        }
    }

    public String getDriverClassName() { return driverClassName; }
    public String getJdbcUrl() { return jdbcUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getQuery() { return query; }
    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public long getInitialDelaySeconds() { return initialDelaySeconds; }
    public long getDelaySeconds() { return delaySeconds; }
    public long getShutdownTimeoutSeconds() { return shutdownTimeoutSeconds; }
    public boolean isInitializeDemoDatabase() { return initializeDemoDatabase; }
}
