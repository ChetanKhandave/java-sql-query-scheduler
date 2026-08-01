package com.example.sqlscheduler;

import com.example.sqlscheduler.config.ApplicationConfig;
import com.example.sqlscheduler.config.PropertiesLoader;
import com.example.sqlscheduler.db.ConnectionFactory;
import com.example.sqlscheduler.db.DemoDatabaseInitializer;
import com.example.sqlscheduler.db.DriverManagerConnectionFactory;
import com.example.sqlscheduler.db.JdbcSqlQueryExecutor;
import com.example.sqlscheduler.db.QueryResultHandler;
import com.example.sqlscheduler.db.SqlQueryExecutor;
import com.example.sqlscheduler.job.LoggingQueryResultHandler;
import com.example.sqlscheduler.job.QueryJob;
import com.example.sqlscheduler.job.SqlPollingJob;
import com.example.sqlscheduler.scheduler.ExecutorTaskScheduler;
import com.example.sqlscheduler.scheduler.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Application entry point and composition root.
 *
 * <p>This class creates and connects all concrete components. Business classes depend on
 * interfaces, while this class is the only place that knows which implementations are used.</p>
 */
public final class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final String CONFIG_RESOURCE = "application.properties";

    private Application() {
        // Utility-style entry-point class; instances are not required.
    }

    /**
     * Starts the SQL polling application and keeps the main thread alive until shutdown.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        TaskScheduler scheduler = null;
        try {
            LOGGER.info("Starting SQL Query Scheduler");
            LOGGER.debug("Loading configuration resource: {}", CONFIG_RESOURCE);

            Properties properties = new PropertiesLoader().load(CONFIG_RESOURCE);
            ApplicationConfig config = ApplicationConfig.from(properties);

            LOGGER.info("Configuration loaded: databaseUrl={}, initialDelay={} seconds, "
                            + "fixedDelay={} seconds, queryTimeout={} seconds, demoInitialization={}",
                    config.getJdbcUrl(),
                    config.getInitialDelaySeconds(),
                    config.getDelaySeconds(),
                    config.getQueryTimeoutSeconds(),
                    config.isInitializeDemoDatabase());

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    config.getDriverClassName(),
                    config.getJdbcUrl(),
                    config.getUsername(),
                    config.getPassword());

            if (config.isInitializeDemoDatabase()) {
                LOGGER.info("Demo database initialization is enabled");
                new DemoDatabaseInitializer(connectionFactory).initialize();
            } else {
                LOGGER.debug("Demo database initialization is disabled");
            }

            SqlQueryExecutor queryExecutor = new JdbcSqlQueryExecutor(connectionFactory);
            QueryResultHandler resultHandler = new LoggingQueryResultHandler();
            QueryJob queryJob = new SqlPollingJob(
                    queryExecutor,
                    config.getQuery(),
                    config.getQueryTimeoutSeconds(),
                    resultHandler);

            scheduler = new ExecutorTaskScheduler("sql-query-scheduler");
            final TaskScheduler shutdownScheduler = scheduler;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutdown signal received; stopping scheduler");
                shutdownScheduler.stop(config.getShutdownTimeoutSeconds(), TimeUnit.SECONDS);
                LOGGER.info("SQL Query Scheduler stopped");
            }, "sql-query-scheduler-shutdown"));

            scheduler.scheduleWithFixedDelay(
                    queryJob,
                    config.getInitialDelaySeconds(),
                    config.getDelaySeconds(),
                    TimeUnit.SECONDS);

            LOGGER.info("Application started successfully. Press Ctrl+C to stop.");
            new CountDownLatch(1).await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Main thread interrupted; application will stop");
        } catch (Exception exception) {
            LOGGER.error("Application startup failed", exception);
            if (scheduler != null) {
                scheduler.close();
            }
            System.exit(1);
        }
    }
}
