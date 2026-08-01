package com.example.sqlscheduler.job;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static org.mockito.Mockito.*;

class LoggingQueryResultHandlerTest {

    @Test
    void shouldReadEveryColumnFromEveryRow() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);

        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(2);
        when(metadata.getColumnLabel(1)).thenReturn("ID");
        when(metadata.getColumnLabel(2)).thenReturn("NAME");
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(1)).thenReturn(1, 2);
        when(resultSet.getObject(2)).thenReturn("A", "B");

        new LoggingQueryResultHandler().handle(resultSet);

        verify(resultSet, times(3)).next();
        verify(resultSet, times(2)).getObject(1);
        verify(resultSet, times(2)).getObject(2);
    }
}
