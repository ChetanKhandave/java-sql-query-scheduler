package com.example.sqlscheduler.db;

import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.jdbc.PoolDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies Oracle UCP connection borrowing and pool lifecycle behavior without requiring
 * a live Oracle database.
 */
class OracleUcpConnectionFactoryTest {

    /**
     * Verifies that {@link OracleUcpConnectionFactory#openConnection()} delegates to the
     * configured {@link PoolDataSource} and returns the same logical connection supplied by UCP.
     */
    @Test
    void shouldBorrowConnectionFromPool() throws Exception {
        PoolDataSource poolDataSource = mock(PoolDataSource.class);
        UniversalConnectionPoolManager poolManager = mock(UniversalConnectionPoolManager.class);
        Connection connection = mock(Connection.class);
        when(poolDataSource.getConnectionPoolName()).thenReturn("test-pool");
        when(poolDataSource.getConnection()).thenReturn(connection);

        OracleUcpConnectionFactory factory =
                new OracleUcpConnectionFactory(poolDataSource, poolManager);

        assertSame(connection, factory.openConnection());
        verify(poolDataSource).getConnection();
    }

    /**
     * Verifies that an exception raised by UCP while borrowing a connection is propagated
     * to the caller so the scheduled job can log and handle the database failure correctly.
     */
    @Test
    void shouldPropagateExceptionWhenPoolCannotProvideConnection() throws Exception {
        PoolDataSource poolDataSource = mock(PoolDataSource.class);
        UniversalConnectionPoolManager poolManager = mock(UniversalConnectionPoolManager.class);
        when(poolDataSource.getConnectionPoolName()).thenReturn("test-pool");
        when(poolDataSource.getConnection()).thenThrow(new SQLException("pool exhausted"));

        OracleUcpConnectionFactory factory =
                new OracleUcpConnectionFactory(poolDataSource, poolManager);

        assertThrows(SQLException.class, factory::openConnection);
        verify(poolDataSource).getConnection();
    }

    /**
     * Verifies that closing the connection factory asks Oracle's UCP manager to destroy
     * the named pool. {@link PoolDataSource} itself does not define a {@code close()} method.
     */
    @Test
    void shouldDestroyPoolThroughPoolManager() throws Exception {
        PoolDataSource poolDataSource = mock(PoolDataSource.class);
        UniversalConnectionPoolManager poolManager = mock(UniversalConnectionPoolManager.class);
        when(poolDataSource.getConnectionPoolName()).thenReturn("test-pool");

        OracleUcpConnectionFactory factory =
                new OracleUcpConnectionFactory(poolDataSource, poolManager);

        factory.close();

        verify(poolManager).destroyConnectionPool("test-pool");
    }

    /**
     * Verifies that a UCP pool-destruction failure is converted into an unchecked lifecycle
     * exception because the connection-factory close contract does not expose checked exceptions.
     */
    @Test
    void shouldWrapPoolDestructionFailure() throws Exception {
        PoolDataSource poolDataSource = mock(PoolDataSource.class);
        UniversalConnectionPoolManager poolManager = mock(UniversalConnectionPoolManager.class);
        when(poolDataSource.getConnectionPoolName()).thenReturn("test-pool");
        org.mockito.Mockito.doThrow(new UniversalConnectionPoolException("destroy failed"))
                .when(poolManager).destroyConnectionPool("test-pool");

        OracleUcpConnectionFactory factory =
                new OracleUcpConnectionFactory(poolDataSource, poolManager);

        assertThrows(IllegalStateException.class, factory::close);
        verify(poolManager).destroyConnectionPool("test-pool");
    }
}
