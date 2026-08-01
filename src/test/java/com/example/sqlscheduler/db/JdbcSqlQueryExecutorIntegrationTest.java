package com.example.sqlscheduler.db;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcSqlQueryExecutorIntegrationTest {

    @Test
    void shouldExecuteRealQueryAgainstInMemoryH2Database() throws Exception {
        String url = "jdbc:h2:mem:integration-test;DB_CLOSE_DELAY=-1";
        ConnectionFactory factory = new DriverManagerConnectionFactory(
                "org.h2.Driver", url, "sa", "");

        try (java.sql.Connection connection = factory.openConnection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE PERSON (ID INT PRIMARY KEY, NAME VARCHAR(50))");
            statement.execute("INSERT INTO PERSON VALUES (1, 'Chetan'), (2, 'Asha')");
        }

        List<String> names = new ArrayList<String>();
        new JdbcSqlQueryExecutor(factory).execute(
                "SELECT NAME FROM PERSON ORDER BY ID",
                5,
                resultSet -> {
                    while (resultSet.next()) {
                        names.add(resultSet.getString("NAME"));
                    }
                });

        assertEquals(java.util.Arrays.asList("Chetan", "Asha"), names);
    }
}
