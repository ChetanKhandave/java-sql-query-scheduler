package com.example.sqlscheduler.job;

import com.example.sqlscheduler.db.QueryResultHandler;
import com.example.sqlscheduler.db.SqlQueryExecutor;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class SqlPollingJobTest {

    @Test
    void shouldDelegateQueryExecution() throws Exception {
        SqlQueryExecutor executor = mock(SqlQueryExecutor.class);
        QueryResultHandler handler = mock(QueryResultHandler.class);
        SqlPollingJob job = new SqlPollingJob(executor, "SELECT 1", 30, handler);

        job.run();

        verify(executor).execute("SELECT 1", 30, handler);
    }

    @Test
    void shouldCatchSqlExceptionSoFutureSchedulesCanContinue() throws Exception {
        SqlQueryExecutor executor = mock(SqlQueryExecutor.class);
        QueryResultHandler handler = mock(QueryResultHandler.class);
        doThrow(new SQLException("database unavailable"))
                .when(executor).execute(anyString(), anyInt(), any(QueryResultHandler.class));

        SqlPollingJob job = new SqlPollingJob(executor, "SELECT 1", 30, handler);

        assertDoesNotThrow(job::run);
        verify(executor).execute("SELECT 1", 30, handler);
    }

    @Test
    void shouldCatchUnexpectedRuntimeException() throws Exception {
        SqlQueryExecutor executor = mock(SqlQueryExecutor.class);
        QueryResultHandler handler = mock(QueryResultHandler.class);
        doThrow(new IllegalStateException("unexpected"))
                .when(executor).execute(anyString(), anyInt(), any(QueryResultHandler.class));

        SqlPollingJob job = new SqlPollingJob(executor, "SELECT 1", 30, handler);

        assertDoesNotThrow(job::run);
    }
}
