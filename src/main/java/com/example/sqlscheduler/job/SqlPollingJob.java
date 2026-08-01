package com.example.sqlscheduler.job;

import com.example.sqlscheduler.db.QueryResultHandler;
import com.example.sqlscheduler.db.SqlQueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;

/** Executes one configured SQL query and prevents failures from terminating future schedules. */
public final class SqlPollingJob implements QueryJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlPollingJob.class);

    private final SqlQueryExecutor queryExecutor;
    private final String sql;
    private final int timeoutSeconds;
    private final QueryResultHandler resultHandler;

    public SqlPollingJob(SqlQueryExecutor queryExecutor,
                         String sql,
                         int timeoutSeconds,
                         QueryResultHandler resultHandler) {
        this.queryExecutor = Objects.requireNonNull(queryExecutor, "queryExecutor must not be null");
        this.sql = requireText(sql, "sql");
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than zero");
        }
        this.timeoutSeconds = timeoutSeconds;
        this.resultHandler = Objects.requireNonNull(resultHandler, "resultHandler must not be null");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    @Override
    public void run() {
        long startNanos = System.nanoTime();
        LOGGER.info("Scheduled SQL query execution started");
        try {
            queryExecutor.execute(sql, timeoutSeconds, resultHandler);
            LOGGER.info("Scheduled SQL query execution completed in {} ms", elapsedMillis(startNanos));
        } catch (SQLException exception) {
            LOGGER.error("Scheduled SQL query execution failed after {} ms", elapsedMillis(startNanos), exception);
        } catch (RuntimeException exception) {
            LOGGER.error("Unexpected failure during scheduled SQL query execution after {} ms",
                    elapsedMillis(startNanos), exception);
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
