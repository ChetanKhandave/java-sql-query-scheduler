package com.example.sqlscheduler.config;

import java.util.Objects;
import java.util.Properties;

/** Immutable, validated application and Oracle UCP configuration. */
public final class ApplicationConfig {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String query;
    private final int queryTimeoutSeconds;
    private final long initialDelaySeconds;
    private final long delaySeconds;
    private final long shutdownTimeoutSeconds;
    private final boolean initializeDemoDatabase;
    private final String poolName;
    private final int initialPoolSize;
    private final int minPoolSize;
    private final int maxPoolSize;
    private final int connectionWaitTimeoutSeconds;
    private final int inactiveConnectionTimeoutSeconds;
    private final boolean validateConnectionOnBorrow;

    private ApplicationConfig(Properties properties) {
        this.jdbcUrl = required(properties, "db.url");
        this.username = properties.getProperty("db.username", "");
        this.password = properties.getProperty("db.password", "");
        this.query = required(properties, "query.sql");
        this.queryTimeoutSeconds = positiveInt(properties, "query.timeout.seconds");
        this.initialDelaySeconds = nonNegativeLong(properties, "scheduler.initial.delay.seconds");
        this.delaySeconds = positiveLong(properties, "scheduler.delay.seconds");
        this.shutdownTimeoutSeconds = positiveLong(properties, "scheduler.shutdown.timeout.seconds");
        this.initializeDemoDatabase = booleanValue(properties, "database.initialize.demo", false);
        this.poolName = required(properties, "db.pool.name");
        this.initialPoolSize = nonNegativeInt(properties, "db.pool.initial-size");
        this.minPoolSize = nonNegativeInt(properties, "db.pool.min-size");
        this.maxPoolSize = positiveInt(properties, "db.pool.max-size");
        this.connectionWaitTimeoutSeconds = positiveInt(properties,
                "db.pool.connection-wait-timeout-seconds");
        this.inactiveConnectionTimeoutSeconds = nonNegativeInt(properties,
                "db.pool.inactive-connection-timeout-seconds");
        this.validateConnectionOnBorrow = booleanValue(properties,
                "db.pool.validate-connection-on-borrow", true);
        validatePoolSizes();
    }

    /** Creates configuration from classpath properties. */
    public static ApplicationConfig from(Properties properties) {
        return new ApplicationConfig(Objects.requireNonNull(properties,
                "properties must not be null"));
    }

    private void validatePoolSizes() {
        if (minPoolSize > maxPoolSize) {
            throw new IllegalArgumentException("db.pool.min-size must not exceed db.pool.max-size");
        }
        if (initialPoolSize > maxPoolSize) {
            throw new IllegalArgumentException("db.pool.initial-size must not exceed db.pool.max-size");
        }
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        return value.trim();
    }

    private static boolean booleanValue(Properties properties, String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    private static int positiveInt(Properties properties, String key) {
        int value = intValue(properties, key);
        if (value <= 0) {
            throw new IllegalArgumentException(key + " must be greater than zero");
        }
        return value;
    }

    private static int nonNegativeInt(Properties properties, String key) {
        int value = intValue(properties, key);
        if (value < 0) {
            throw new IllegalArgumentException(key + " must not be negative");
        }
        return value;
    }

    private static int intValue(Properties properties, String key) {
        long value = parseLong(properties, key);
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

    public String getJdbcUrl() { return jdbcUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getQuery() { return query; }
    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public long getInitialDelaySeconds() { return initialDelaySeconds; }
    public long getDelaySeconds() { return delaySeconds; }
    public long getShutdownTimeoutSeconds() { return shutdownTimeoutSeconds; }
    public boolean isInitializeDemoDatabase() { return initializeDemoDatabase; }
    public String getPoolName() { return poolName; }
    public int getInitialPoolSize() { return initialPoolSize; }
    public int getMinPoolSize() { return minPoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public int getConnectionWaitTimeoutSeconds() { return connectionWaitTimeoutSeconds; }
    public int getInactiveConnectionTimeoutSeconds() { return inactiveConnectionTimeoutSeconds; }
    public boolean isValidateConnectionOnBorrow() { return validateConnectionOnBorrow; }
}
