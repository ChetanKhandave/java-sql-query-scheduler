package com.example.sqlscheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Single-threaded scheduler. Fixed delay ensures scheduled executions do not overlap. */
public final class ExecutorTaskScheduler implements TaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorTaskScheduler.class);

    private final ScheduledExecutorService executorService;
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    public ExecutorTaskScheduler(String threadName) {
        this(Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(threadName)));
    }

    ExecutorTaskScheduler(ScheduledExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService, "executorService must not be null");
    }

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

        executorService.scheduleWithFixedDelay(task, initialDelay, delay, timeUnit);
        LOGGER.info("Task scheduled with initial delay {} and fixed delay {} {}",
                initialDelay, delay, timeUnit.name().toLowerCase());
    }

    @Override
    public void stop(long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, timeUnit)) {
                LOGGER.warn("Scheduler did not stop within the timeout; forcing shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            LOGGER.warn("Scheduler shutdown was interrupted", exception);
        }
    }

    @Override
    public void close() {
        stop(10, TimeUnit.SECONDS);
    }

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
            return thread;
        }
    }
}
