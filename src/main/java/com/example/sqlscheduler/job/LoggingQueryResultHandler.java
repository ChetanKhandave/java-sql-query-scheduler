package com.example.sqlscheduler.job;

import com.example.sqlscheduler.db.QueryResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Generic result handler that logs every column returned by the configured query.
 *
 * <p>This implementation is intended for demonstration and operational visibility. For
 * sensitive production data, replace it with a handler that masks or selectively logs fields.</p>
 */
public final class LoggingQueryResultHandler implements QueryResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingQueryResultHandler.class);

    /**
     * Iterates over the result set, logs each row, and records the total row count.
     *
     * @param resultSet result set positioned before the first row
     * @throws SQLException when metadata or row access fails
     */
    @Override
    public void handle(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        int rowCount = 0;

        LOGGER.debug("Processing query result with {} column(s)", columnCount);
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
