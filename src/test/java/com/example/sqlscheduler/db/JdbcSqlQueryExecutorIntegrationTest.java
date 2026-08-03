package com.example.sqlscheduler.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies end-to-end JDBC execution against an in-memory H2 database. */
class JdbcSqlQueryExecutorIntegrationTest {

    /**
     * Creates a real test table, inserts sample rows and verifies that
     * {@link JdbcSqlQueryExecutor} can execute a query and pass the actual ResultSet to the
     * supplied handler. H2 is used so the test does not require an Oracle environment.
     */
    @Test
    void shouldExecuteRealQueryAgainstInMemoryH2Database() throws Exception {
        String url = "jdbc:h2:mem:integration-test;DB_CLOSE_DELAY=-1";
        ConnectionFactory factory = new DriverManagerConnectionFactory(
                "org.h2.Driver", url, "sa", "");

        try {
            try (Connection connection = factory.openConnection();
                 Statement statement = connection.createStatement()) {
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

            assertEquals(Arrays.asList("Chetan", "Asha"), names);
        } finally {
            factory.close();
        }
    }
}
