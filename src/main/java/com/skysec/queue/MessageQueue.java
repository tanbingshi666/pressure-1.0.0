package com.skysec.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MessageQueue {

    private static final Integer CAPACITY_SIZE = 4096;

    public static final BlockingQueue<Object> bq = new ArrayBlockingQueue<>(CAPACITY_SIZE);
    public static final AtomicReference<BlockingQueue<Object>> putRef = new AtomicReference<>(bq);
    public static final AtomicReference<BlockingQueue<Object>> takeRef = new AtomicReference<>(bq);

    public static void put(Object e) {
        try {
            while (!putRef.get().offer(e)) {
                TimeUnit.MILLISECONDS.sleep(100L);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static Object take() throws InterruptedException {
        return takeRef.get().take();
    }

}
