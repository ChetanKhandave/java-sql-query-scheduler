package com.example.sqlscheduler.db;

import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Oracle Universal Connection Pool implementation of {@link ConnectionFactory}.
 *
 * <p>The factory owns one {@link PoolDataSource}. Each call to
 * {@link #openConnection()} borrows a logical connection from the pool. Closing that
 * connection returns it to the pool rather than closing the underlying physical Oracle
 * session.</p>
 *
 * <p>The pool itself is stopped and removed through Oracle's
 * {@link UniversalConnectionPoolManager}; {@link PoolDataSource} does not expose a
 * {@code close()} method.</p>
 */
public final class OracleUcpConnectionFactory implements ConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleUcpConnectionFactory.class);
    private static final String ORACLE_DATA_SOURCE_CLASS = "oracle.jdbc.pool.OracleDataSource";

    private final PoolDataSource poolDataSource;
    private final UniversalConnectionPoolManager poolManager;

    /**
     * Creates and configures an Oracle UCP data source.
     *
     * @param jdbcUrl Oracle JDBC URL
     * @param username database username
     * @param password database password
     * @param poolName descriptive pool name used in logs and monitoring
     * @param initialPoolSize number of connections created when the pool starts
     * @param minPoolSize minimum number of connections retained by the pool
     * @param maxPoolSize maximum number of concurrent physical connections
     * @param connectionWaitTimeoutSeconds maximum time to wait for an available connection
     * @param inactiveConnectionTimeoutSeconds timeout for reclaiming inactive connections; zero disables it
     * @param validateConnectionOnBorrow whether UCP validates a connection before returning it
     * @throws SQLException when UCP configuration or manager initialization fails
     */
    public OracleUcpConnectionFactory(String jdbcUrl,
                                      String username,
                                      String password,
                                      String poolName,
                                      int initialPoolSize,
                                      int minPoolSize,
                                      int maxPoolSize,
                                      int connectionWaitTimeoutSeconds,
                                      int inactiveConnectionTimeoutSeconds,
                                      boolean validateConnectionOnBorrow) throws SQLException {
        validatePoolSizes(initialPoolSize, minPoolSize, maxPoolSize);

        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setConnectionFactoryClassName(ORACLE_DATA_SOURCE_CLASS);
        dataSource.setURL(requireText(jdbcUrl, "jdbcUrl"));
        dataSource.setUser(username == null ? "" : username);
        dataSource.setPassword(password == null ? "" : password);
        dataSource.setConnectionPoolName(requireText(poolName, "poolName"));
        dataSource.setInitialPoolSize(initialPoolSize);
        dataSource.setMinPoolSize(minPoolSize);
        dataSource.setMaxPoolSize(maxPoolSize);
        dataSource.setConnectionWaitTimeout(positive(connectionWaitTimeoutSeconds,
                "connectionWaitTimeoutSeconds"));
        dataSource.setInactiveConnectionTimeout(nonNegative(inactiveConnectionTimeoutSeconds,
                "inactiveConnectionTimeoutSeconds"));
        dataSource.setValidateConnectionOnBorrow(validateConnectionOnBorrow);

        this.poolDataSource = dataSource;
        this.poolManager = obtainPoolManager();

        LOGGER.info("Oracle UCP configured: poolName={}, initialSize={}, minSize={}, maxSize={}, "
                        + "connectionWaitTimeout={} seconds, inactiveConnectionTimeout={} seconds, "
                        + "validateOnBorrow={}",
                poolName, initialPoolSize, minPoolSize, maxPoolSize,
                connectionWaitTimeoutSeconds, inactiveConnectionTimeoutSeconds,
                validateConnectionOnBorrow);
    }

    /**
     * Package-private constructor used by unit tests to inject mocked UCP collaborators.
     *
     * @param poolDataSource mocked or externally configured pool data source
     * @param poolManager mocked or externally supplied UCP manager
     */
    OracleUcpConnectionFactory(PoolDataSource poolDataSource,
                               UniversalConnectionPoolManager poolManager) {
        this.poolDataSource = Objects.requireNonNull(poolDataSource,
                "poolDataSource must not be null");
        this.poolManager = Objects.requireNonNull(poolManager,
                "poolManager must not be null");
    }

    /**
     * Borrows a connection from Oracle UCP.
     *
     * @return logical pooled JDBC connection
     * @throws SQLException when no connection can be obtained
     */
    @Override
    public Connection openConnection() throws SQLException {
        LOGGER.debug("Borrowing Oracle connection from pool {}",
                poolDataSource.getConnectionPoolName());
        Connection connection = poolDataSource.getConnection();
        LOGGER.debug("Oracle connection borrowed successfully from pool {}",
                poolDataSource.getConnectionPoolName());
        return connection;
    }

    /**
     * Stops and removes the named UCP pool through Oracle's pool manager.
     *
     * <p>This releases the physical Oracle connections owned by the pool. A failure is
     * surfaced as an {@link IllegalStateException} because the {@link ConnectionFactory}
     * lifecycle contract does not declare checked exceptions.</p>
     */
    @Override
    public void close() {
        String poolName = safePoolName();
        LOGGER.info("Destroying Oracle UCP pool {}", poolName);
        try {
            poolManager.destroyConnectionPool(poolName);
            LOGGER.info("Oracle UCP pool {} destroyed", poolName);
        } catch (UniversalConnectionPoolException exception) {
            LOGGER.error("Failed to destroy Oracle UCP pool {}", poolName, exception);
            throw new IllegalStateException("Failed to destroy Oracle UCP pool: " + poolName,
                    exception);
        }
    }

    private static UniversalConnectionPoolManager obtainPoolManager() throws SQLException {
        try {
            return UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
        } catch (UniversalConnectionPoolException exception) {
            throw new SQLException("Unable to obtain Oracle UCP pool manager", exception);
        }
    }

    private String safePoolName() {
        try {
            return poolDataSource.getConnectionPoolName();
        } catch (SQLException exception) {
            LOGGER.warn("Unable to read Oracle UCP pool name", exception);
            return "unknown";
        }
    }

    private static void validatePoolSizes(int initial, int min, int max) {
        nonNegative(initial, "initialPoolSize");
        nonNegative(min, "minPoolSize");
        positive(max, "maxPoolSize");
        if (min > max) {
            throw new IllegalArgumentException("minPoolSize must not exceed maxPoolSize");
        }
        if (initial > max) {
            throw new IllegalArgumentException("initialPoolSize must not exceed maxPoolSize");
        }
    }

    private static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero");
        }
        return value;
    }

    private static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
