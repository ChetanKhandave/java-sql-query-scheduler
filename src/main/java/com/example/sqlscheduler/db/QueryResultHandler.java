package com.example.sqlscheduler.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Processes the ResultSet while JDBC resources are still open. */
public interface QueryResultHandler {
    void handle(ResultSet resultSet) throws SQLException;
}
