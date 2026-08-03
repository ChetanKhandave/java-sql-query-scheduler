package com.example.sqlscheduler;

import com.example.sqlscheduler.config.ApplicationConfig;
import com.example.sqlscheduler.config.PropertiesLoader;
import com.example.sqlscheduler.db.ConnectionFactory;
import com.example.sqlscheduler.db.DemoDatabaseInitializer;
import com.example.sqlscheduler.db.JdbcSqlQueryExecutor;
import com.example.sqlscheduler.db.OracleUcpConnectionFactory;
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
 * <p>This class creates Oracle UCP, query-processing, and scheduling components and
 * coordinates their startup and graceful shutdown.</p>
 */
public final class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final String CONFIG_RESOURCE = "application.properties";

    private Application() {
        // Entry-point class; instances are not required.
    }

    /** Starts the scheduler and keeps the JVM alive until shutdown. */
    public static void main(String[] args) {
        TaskScheduler scheduler = null;
        ConnectionFactory connectionFactory = null;
        try {
            LOGGER.info("Starting SQL Query Scheduler with Oracle UCP");
            Properties properties = new PropertiesLoader().load(CONFIG_RESOURCE);
            ApplicationConfig config = ApplicationConfig.from(properties);

            LOGGER.info("Configuration loaded: databaseUrl={}, poolName={}, initialPoolSize={}, "
                            + "minPoolSize={}, maxPoolSize={}, initialDelay={} seconds, "
                            + "fixedDelay={} seconds, queryTimeout={} seconds",
                    config.getJdbcUrl(), config.getPoolName(), config.getInitialPoolSize(),
                    config.getMinPoolSize(), config.getMaxPoolSize(),
                    config.getInitialDelaySeconds(), config.getDelaySeconds(),
                    config.getQueryTimeoutSeconds());

            connectionFactory = new OracleUcpConnectionFactory(
                    config.getJdbcUrl(), config.getUsername(), config.getPassword(),
                    config.getPoolName(), config.getInitialPoolSize(), config.getMinPoolSize(),
                    config.getMaxPoolSize(), config.getConnectionWaitTimeoutSeconds(),
                    config.getInactiveConnectionTimeoutSeconds(),
                    config.isValidateConnectionOnBorrow());

            if (config.isInitializeDemoDatabase()) {
                LOGGER.warn("Demo initialization is enabled; disable it for an existing Oracle schema");
                new DemoDatabaseInitializer(connectionFactory).initialize();
            }

            SqlQueryExecutor queryExecutor = new JdbcSqlQueryExecutor(connectionFactory);
            QueryResultHandler resultHandler = new LoggingQueryResultHandler();
            QueryJob queryJob = new SqlPollingJob(queryExecutor, config.getQuery(),
                    config.getQueryTimeoutSeconds(), resultHandler);

            scheduler = new ExecutorTaskScheduler("sql-query-scheduler");
            final TaskScheduler shutdownScheduler = scheduler;
            final ConnectionFactory shutdownConnectionFactory = connectionFactory;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutdown signal received; stopping scheduled work");
                shutdownScheduler.stop(config.getShutdownTimeoutSeconds(), TimeUnit.SECONDS);
                LOGGER.info("Scheduled work stopped; closing Oracle UCP");
                shutdownConnectionFactory.close();
                LOGGER.info("SQL Query Scheduler stopped cleanly");
            }, "sql-query-scheduler-shutdown"));

            scheduler.scheduleWithFixedDelay(queryJob, config.getInitialDelaySeconds(),
                    config.getDelaySeconds(), TimeUnit.SECONDS);
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
            if (connectionFactory != null) {
                connectionFactory.close();
            }
            System.exit(1);
        }
    }
}
