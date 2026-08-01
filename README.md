# SQL Query Scheduler — Plain Java 8

A Maven-based Java 8 application that executes a configurable SQL query repeatedly using `ScheduledExecutorService.scheduleWithFixedDelay`.

## Design

- `ApplicationConfig`: validates immutable configuration.
- `ConnectionFactory`: abstracts JDBC connection creation.
- `SqlQueryExecutor`: abstracts query execution.
- `QueryResultHandler`: separates result processing from JDBC execution.
- `QueryJob`: represents scheduled business work.
- `TaskScheduler`: abstracts scheduling and shutdown.
- `Application`: composition root that wires concrete implementations.

This applies SRP, OCP, LSP, ISP and DIP by keeping responsibilities small and making higher-level behavior depend on interfaces.

## Prerequisites

- JDK 8
- Maven 3.6+

Check:

```bash
java -version
mvn -version
```

## Build and test

```bash
mvn clean test
mvn clean package
```

The executable fat JAR is created as:

```text
target/sql-query-scheduler.jar
```

Run it:

```bash
java -jar target/sql-query-scheduler.jar
```

Press `Ctrl+C` to stop gracefully.

## Configuration

Edit `src/main/resources/application.properties`.

```properties
db.driver=org.h2.Driver
db.url=jdbc:h2:file:./data/schedulerdb;AUTO_SERVER=TRUE
db.username=sa
db.password=
query.sql=SELECT ID, EVENT_NAME, CREATED_AT FROM SCHEDULED_EVENT ORDER BY ID
query.timeout.seconds=30
scheduler.initial.delay.seconds=2
scheduler.delay.seconds=30
scheduler.shutdown.timeout.seconds=10
database.initialize.demo=true
```

`scheduleWithFixedDelay` starts the next execution only after the previous execution finishes and the configured delay passes. This prevents overlapping executions in this single-threaded scheduler.

## Oracle 19c configuration

The `pom.xml` includes the Oracle JDBC dependency for JDK 8:

```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <version>19.27.0.0</version>
    <scope>runtime</scope>
</dependency>
```

Configure Oracle in `application.properties`:

```properties
db.driver=oracle.jdbc.OracleDriver
db.url=jdbc:oracle:thin:@//localhost:1521/ORCLPDB1
db.username=app_user
db.password=change_me
query.sql=SELECT CUST_NUMBER, PASSKEY_HASH, HASH_UPDATATION_DATE FROM PASSKEY ORDER BY HASH_UPDATATION_DATE DESC
query.timeout.seconds=30
scheduler.initial.delay.seconds=5
scheduler.delay.seconds=600
scheduler.shutdown.timeout.seconds=10
database.initialize.demo=false
```

Do not commit real database credentials. In production, load them from a protected external source or secret manager.

## Tests included

- Configuration loading and validation.
- JDBC delegation, timeout assignment and resource closure.
- Real H2 integration test.
- Query-job success and exception handling.
- Generic result handling.
- Repeated scheduler execution and validation.

## Production considerations

For frequent or high-volume jobs, replace `DriverManagerConnectionFactory` with a connection-pool implementation while retaining the same `ConnectionFactory` interface. For multiple application instances, use a database lock, leader election, or a clustered scheduler to avoid duplicate execution.
