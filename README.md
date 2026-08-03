# SQL Query Scheduler — Java 8 with Oracle UCP

A Maven-based Java 8 application that executes a configurable SQL query repeatedly with `ScheduledExecutorService.scheduleWithFixedDelay` and obtains database connections from Oracle Universal Connection Pool (UCP).

## Main design

- `ApplicationConfig`: validates scheduler, query, and UCP settings.
- `ConnectionFactory`: hides connection-acquisition details.
- `OracleUcpConnectionFactory`: owns Oracle UCP and returns logical pooled connections.
- `JdbcSqlQueryExecutor`: executes read-only queries and closes JDBC resources safely.
- `SqlPollingJob`: protects future schedules from one failed execution.
- `ExecutorTaskScheduler`: provides non-overlapping fixed-delay execution.
- `Application`: wires components and closes the scheduler and pool gracefully.

## Prerequisites

- JDK 8
- Maven 3.6+
- Oracle Database 19c connection details

## Build and test

```bash
mvn clean test
mvn clean package
```

Run:

```bash
java -jar target/sql-query-scheduler.jar
```

## Oracle UCP configuration

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:oracle:thin:@//localhost:1521/ORCLPDB1
db.username=app_user
db.password=change_me

db.pool.name=sql-query-scheduler-pool
db.pool.initial-size=2
db.pool.min-size=2
db.pool.max-size=6
db.pool.connection-wait-timeout-seconds=30
db.pool.inactive-connection-timeout-seconds=0
db.pool.validate-connection-on-borrow=true

query.sql=SELECT ID, EVENT_NAME, CREATED_AT FROM SCHEDULED_EVENT ORDER BY ID
query.timeout.seconds=30

scheduler.initial.delay.seconds=2
scheduler.delay.seconds=30
scheduler.shutdown.timeout.seconds=10

database.initialize.demo=false
```

### Pool-property meaning

- `initial-size`: physical connections created when UCP starts.
- `min-size`: minimum number of connections retained.
- `max-size`: maximum physical Oracle connections for this application instance.
- `connection-wait-timeout-seconds`: maximum wait when all pooled connections are busy.
- `inactive-connection-timeout-seconds`: reclaims inactive borrowed connections; `0` disables it.
- `validate-connection-on-borrow`: validates a connection before the application receives it.

Pool size must be planned across all application instances. For example, five instances with `max-size=6` can create up to 30 physical database connections.

## Connection lifecycle

Each scheduled execution borrows a logical connection:

```text
Delay expires → borrow connection → execute SQL → close connection → return to UCP
```

Never hold a connection while an item is waiting in a `DelayQueue`.

## Logging and security

Lifecycle, pool configuration, connection borrowing, query duration, scheduler shutdown, and failures are logged. Passwords and complete SQL statements are not logged. Do not commit real credentials; use protected external configuration or a secret manager in production.

## Tests

The test suite covers configuration validation, UCP delegation, JDBC resource handling, query jobs, result processing, classpath property loading, and scheduler behavior. UCP unit tests use mocks and do not require a live Oracle database.
