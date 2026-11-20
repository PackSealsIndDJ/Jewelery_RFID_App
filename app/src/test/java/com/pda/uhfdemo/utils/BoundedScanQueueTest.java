package com.pda.uhfdemo.utils;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoundedScanQueueTest {
    @Test
    public void processesOneThousandQueuedTagEventsOffCallerThread() throws Exception {
        BoundedScanQueue queue = new BoundedScanQueue();
        AtomicInteger processed = new AtomicInteger();
        AtomicBoolean ranOffCallerThread = new AtomicBoolean(true);
        Thread caller = Thread.currentThread();
        CountDownLatch completed = new CountDownLatch(1000);
        for (int i = 0; i < 1000; i++) {
            assertTrue(queue.submit(() -> {
                if (Thread.currentThread() == caller) {
                    ranOffCallerThread.set(false);
                }
                processed.incrementAndGet();
                completed.countDown();
            }));
        }

        assertTrue(completed.await(5, TimeUnit.SECONDS));
        assertEquals(1000, processed.get());
        assertTrue(ranOffCallerThread.get());
        queue.shutdown();
    }
}
