package com.simple;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Test {
    public static void main(String[] args) {
       final AtomicInteger atomicLong = new AtomicInteger();
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (atomicLong.getAndIncrement()%2==0)
                    ThreadTest.threadLocal.set("haha");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                   System.out.println(ThreadTest.threadLocal.get());
                }

            }).start();

        }
    }
}