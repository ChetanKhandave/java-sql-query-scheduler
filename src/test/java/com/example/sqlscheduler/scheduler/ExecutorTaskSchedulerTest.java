package com.example.sqlscheduler.scheduler;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies repeated scheduling, single-task protection and delay validation. */
class ExecutorTaskSchedulerTest {

    /**
     * Verifies that a scheduled task executes repeatedly with a fixed delay and that the
     * scheduler can be stopped cleanly after the expected executions occur.
     */
    @Test
    void shouldExecuteTaskRepeatedly() throws Exception {
        ExecutorTaskScheduler scheduler = new ExecutorTaskScheduler("scheduler-test");
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger executionCount = new AtomicInteger();

        try {
            scheduler.scheduleWithFixedDelay(() -> {
                executionCount.incrementAndGet();
                latch.countDown();
            }, 0, 20, TimeUnit.MILLISECONDS);

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertTrue(executionCount.get() >= 2);
        } finally {
            scheduler.stop(1, TimeUnit.SECONDS);
        }
    }

    /**
     * Verifies that one scheduler instance accepts only one recurring task, preventing
     * accidental duplicate polling of the same database workload.
     */
    @Test
    void shouldRejectSecondScheduledTask() {
        ExecutorTaskScheduler scheduler = new ExecutorTaskScheduler("scheduler-test");
        try {
            scheduler.scheduleWithFixedDelay(() -> { }, 1, 1, TimeUnit.DAYS);
            assertThrows(IllegalStateException.class,
                    () -> scheduler.scheduleWithFixedDelay(() -> { }, 1, 1, TimeUnit.DAYS));
        } finally {
            scheduler.stop(1, TimeUnit.SECONDS);
        }
    }

    /** Verifies that a zero fixed delay is rejected before a task is registered. */
    @Test
    void shouldRejectInvalidDelay() {
        ExecutorTaskScheduler scheduler = new ExecutorTaskScheduler("scheduler-test");
        try {
            assertThrows(IllegalArgumentException.class,
                    () -> scheduler.scheduleWithFixedDelay(() -> { }, 0, 0, TimeUnit.SECONDS));
        } finally {
            scheduler.stop(1, TimeUnit.SECONDS);
        }
    }
}
