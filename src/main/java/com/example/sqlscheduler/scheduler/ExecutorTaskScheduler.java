package com.example.sqlscheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Single-threaded task scheduler backed by {@link ScheduledExecutorService}.
 *
 * <p>Fixed-delay scheduling is used so the delay begins only after the previous execution
 * finishes. Because the executor has one worker thread, scheduled executions never overlap.</p>
 */
public final class ExecutorTaskScheduler implements TaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorTaskScheduler.class);

    private final ScheduledExecutorService executorService;
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    /**
     * Creates a scheduler whose worker thread uses the supplied name.
     *
     * @param threadName scheduler worker-thread name
     */
    public ExecutorTaskScheduler(String threadName) {
        this(Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(threadName)));
    }

    ExecutorTaskScheduler(ScheduledExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService, "executorService must not be null");
    }

    /**
     * Registers exactly one recurring task with a fixed delay between completed executions.
     *
     * @param task task to execute
     * @param initialDelay delay before the first execution
     * @param delay delay after one execution completes and before the next begins
     * @param timeUnit unit used for both delay values
     */
    @Override
    public void scheduleWithFixedDelay(Runnable task,
                                       long initialDelay,
                                       long delay,
                                       TimeUnit timeUnit) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        if (initialDelay < 0) {
            throw new IllegalArgumentException("initialDelay must not be negative");
        }
        if (delay <= 0) {
            throw new IllegalArgumentException("delay must be greater than zero");
        }
        if (!scheduled.compareAndSet(false, true)) {
            throw new IllegalStateException("A task has already been scheduled");
        }

        LOGGER.debug("Registering recurring task with scheduler");
        executorService.scheduleWithFixedDelay(task, initialDelay, delay, timeUnit);
        LOGGER.info("Task scheduled with initial delay {} and fixed delay {} {}",
                initialDelay, delay, timeUnit.name().toLowerCase());
    }

    /**
     * Requests graceful shutdown and forces termination if the timeout expires.
     *
     * @param timeout maximum time to wait for running work to complete
     * @param timeUnit timeout unit
     */
    @Override
    public void stop(long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }

        LOGGER.info("Stopping scheduler; graceful shutdown timeout={} {}",
                timeout, timeUnit.name().toLowerCase());
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, timeUnit)) {
                LOGGER.warn("Scheduler did not stop within the timeout; forcing shutdown");
                executorService.shutdownNow();
            } else {
                LOGGER.info("Scheduler stopped gracefully");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            LOGGER.warn("Scheduler shutdown was interrupted; forced shutdown requested", exception);
        }
    }

    /**
     * Stops the scheduler using the default ten-second timeout.
     */
    @Override
    public void close() {
        stop(10, TimeUnit.SECONDS);
    }

    /** Creates consistently named non-daemon worker threads. */
    private static final class NamedThreadFactory implements ThreadFactory {
        private final String threadName;

        private NamedThreadFactory(String threadName) {
            if (threadName == null || threadName.trim().isEmpty()) {
                throw new IllegalArgumentException("threadName must not be blank");
            }
            this.threadName = threadName.trim();
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(false);
            thread.setUncaughtExceptionHandler((failedThread, exception) ->
                    LOGGER.error("Uncaught exception in thread {}", failedThread.getName(), exception));
            LOGGER.debug("Created scheduler worker thread: {}", thread.getName());
            return thread;
        }
    }
}
