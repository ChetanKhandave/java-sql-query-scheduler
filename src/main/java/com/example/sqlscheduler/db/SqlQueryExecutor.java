package com.example.sqlscheduler.db;

import java.sql.SQLException;

/** Contract for executing a read-only SQL query. */
public interface SqlQueryExecutor {
    void execute(String sql, int timeoutSeconds, QueryResultHandler resultHandler) throws SQLException;
}
