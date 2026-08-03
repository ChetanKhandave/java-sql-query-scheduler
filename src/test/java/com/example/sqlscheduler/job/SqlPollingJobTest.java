package com.example.sqlscheduler.job;

import com.example.sqlscheduler.db.QueryResultHandler;
import com.example.sqlscheduler.db.SqlQueryExecutor;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Verifies scheduled-job delegation and failure isolation between executions. */
class SqlPollingJobTest {

    /** Verifies that a scheduled run delegates the configured SQL, timeout and handler. */
    @Test
    void shouldDelegateQueryExecution() throws Exception {
        SqlQueryExecutor executor = mock(SqlQueryExecutor.class);
        QueryResultHandler handler = mock(QueryResultHandler.class);
        SqlPollingJob job = new SqlPollingJob(executor, "SELECT 1", 30, handler);

        job.run();

        verify(executor).execute("SELECT 1", 30, handler);
    }

    /**
     * Verifies that a database failure is logged and contained instead of escaping from
     * {@code run()}, which would otherwise stop future scheduled executions.
     */
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

    /** Verifies that unexpected runtime failures are also contained to keep scheduling alive. */
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
