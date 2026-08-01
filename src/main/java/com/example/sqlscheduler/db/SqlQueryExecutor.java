package com.example.sqlscheduler.db;

import java.sql.SQLException;

/**
 * Contract for executing a read-only SQL query.
 */
public interface SqlQueryExecutor {

    /**
     * Executes a SQL query and delegates result-set processing to the supplied handler.
     *
     * @param sql SQL SELECT statement to execute
     * @param timeoutSeconds maximum query duration in seconds
     * @param resultHandler result-set processing strategy
     * @throws SQLException when JDBC execution or result processing fails
     */
    void execute(String sql,
                 int timeoutSeconds,
                 QueryResultHandler resultHandler) throws SQLException;
}
