package com.example.sqlscheduler.db;

import java.sql.Connection;
import java.sql.SQLException;

/** Abstraction for obtaining JDBC connections. */
public interface ConnectionFactory {
    Connection openConnection() throws SQLException;
}
