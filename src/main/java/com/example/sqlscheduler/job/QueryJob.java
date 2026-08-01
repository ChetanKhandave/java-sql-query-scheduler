package com.example.sqlscheduler.job;

/** Scheduled unit of work. */
public interface QueryJob extends Runnable {
    @Override
    void run();
}
