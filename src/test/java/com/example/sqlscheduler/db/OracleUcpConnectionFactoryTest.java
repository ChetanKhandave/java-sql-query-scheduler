package com.example.sqlscheduler.db;

import oracle.ucp.jdbc.PoolDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies delegation to Oracle UCP without requiring a live Oracle database. */
class OracleUcpConnectionFactoryTest {

    @Test
    void shouldBorrowConnectionFromPool() throws Exception {
        PoolDataSource poolDataSource = mock(PoolDataSource.class);
        Connection connection = mock(Connection.class);
        when(poolDataSource.getConnectionPoolName()).thenReturn("test-pool");
        when(poolDataSource.getConnection()).thenReturn(connection);

        OracleUcpConnectionFactory factory =
                new OracleUcpConnectionFactory(poolDataSource);

        assertSame(connection, factory.openConnection());
        verify(poolDataSource).getConnection();
    }

    @Test
    void shouldClosePool() throws Exception {
        PoolDataSource poolDataSource = mock(PoolDataSource.class);
        when(poolDataSource.getConnectionPoolName()).thenReturn("test-pool");
        OracleUcpConnectionFactory factory =
                new OracleUcpConnectionFactory(poolDataSource);

        factory.close();

        verify(poolDataSource).close();
    }
}
