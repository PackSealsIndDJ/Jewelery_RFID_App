package com.pda.uhfdemo.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Keeps inventory callbacks short while limiting queued tag work. */
public final class BoundedScanQueue {
    private static final Logger LOGGER = Logger.getLogger(BoundedScanQueue.class.getName());
    private static final int CAPACITY = 16_384;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(CAPACITY),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "rfid-tag-processing");
                    thread.setDaemon(true);
                    return thread;
                }
            });

    public boolean submit(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("A tag-processing task is required.");
        }
        try {
            executor.execute(task);
            return true;
        } catch (RejectedExecutionException error) {
            LOGGER.log(Level.SEVERE, "RFID processing queue is full; tag event was dropped.", error);
            return false;
        }
    }

    public void clearPending() {
        executor.getQueue().clear();
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
