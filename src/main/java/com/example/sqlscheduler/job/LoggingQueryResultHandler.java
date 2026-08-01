package com.example.sqlscheduler.job;

import com.example.sqlscheduler.db.QueryResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/** Generic handler that logs every column returned by the configured query. */
public final class LoggingQueryResultHandler implements QueryResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingQueryResultHandler.class);

    @Override
    public void handle(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        int rowCount = 0;

        while (resultSet.next()) {
            rowCount++;
            StringBuilder row = new StringBuilder("Row ").append(rowCount).append(": ");
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                if (columnIndex > 1) {
                    row.append(", ");
                }
                row.append(metadata.getColumnLabel(columnIndex))
                        .append('=')
                        .append(resultSet.getObject(columnIndex));
            }
            LOGGER.info(row.toString());
        }

        LOGGER.info("Query returned {} row(s)", rowCount);
    }
}
