package com.example.sqlscheduler.scheduler;

import java.util.concurrent.TimeUnit;

/** Abstraction for scheduling and lifecycle management. */
public interface TaskScheduler extends AutoCloseable {
    void scheduleWithFixedDelay(Runnable task,
                                long initialDelay,
                                long delay,
                                TimeUnit timeUnit);

    void stop(long timeout, TimeUnit timeUnit);

    @Override
    void close();
}
