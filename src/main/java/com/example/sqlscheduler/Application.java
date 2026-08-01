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

/** Application composition root. */
public final class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private Application() {
    }

    public static void main(String[] args) {
        TaskScheduler scheduler = null;
        try {
            Properties properties = new PropertiesLoader().load("application.properties");
            ApplicationConfig config = ApplicationConfig.from(properties);

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    config.getDriverClassName(),
                    config.getJdbcUrl(),
                    config.getUsername(),
                    config.getPassword());

            if (config.isInitializeDemoDatabase()) {
                new DemoDatabaseInitializer(connectionFactory).initialize();
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
                LOGGER.info("Shutdown signal received");
                shutdownScheduler.stop(config.getShutdownTimeoutSeconds(), TimeUnit.SECONDS);
            }, "sql-query-scheduler-shutdown"));

            scheduler.scheduleWithFixedDelay(
                    queryJob,
                    config.getInitialDelaySeconds(),
                    config.getDelaySeconds(),
                    TimeUnit.SECONDS);

            LOGGER.info("Application started. Press Ctrl+C to stop.");
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
