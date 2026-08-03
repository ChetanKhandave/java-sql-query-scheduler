package com.example.sqlscheduler.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies JDBC query execution, timeout application and deterministic resource cleanup. */
class JdbcSqlQueryExecutorTest {

    /**
     * Verifies that the executor borrows a connection, marks it read-only, configures the
     * query timeout, executes the SQL, delegates result processing and closes every resource.
     * With UCP, closing the connection returns it to the pool.
     */
    @Test
    void shouldExecuteQuerySetTimeoutAndPassResultSetToHandler() throws Exception {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        QueryResultHandler handler = mock(QueryResultHandler.class);

        when(connectionFactory.openConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM TEST")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        JdbcSqlQueryExecutor executor = new JdbcSqlQueryExecutor(connectionFactory);
        executor.execute("SELECT * FROM TEST", 15, handler);

        verify(connection).setReadOnly(true);
        verify(statement).setQueryTimeout(15);
        verify(statement).executeQuery();
        verify(handler).handle(resultSet);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    /**
     * Verifies that ResultSet, PreparedStatement and Connection are still closed when result
     * processing fails, preventing pooled connection leaks.
     */
    @Test
    void shouldCloseJdbcResourcesWhenHandlerFails() throws Exception {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        QueryResultHandler handler = mock(QueryResultHandler.class);

        when(connectionFactory.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        doThrow(new SQLException("handler failure")).when(handler).handle(resultSet);

        JdbcSqlQueryExecutor executor = new JdbcSqlQueryExecutor(connectionFactory);

        assertThrows(SQLException.class,
                () -> executor.execute("SELECT 1", 5, handler));

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    /** Verifies that blank SQL is rejected before a database connection is borrowed. */
    @Test
    void shouldRejectBlankSql() {
        JdbcSqlQueryExecutor executor = new JdbcSqlQueryExecutor(mock(ConnectionFactory.class));
        assertThrows(IllegalArgumentException.class,
                () -> executor.execute("  ", 5, mock(QueryResultHandler.class)));
    }
}
