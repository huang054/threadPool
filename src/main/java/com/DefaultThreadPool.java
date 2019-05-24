package com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池
 *
 * @author hrabbit
 */
public class DefaultThreadPool<T extends Runnable> implements ThreadPools<T>{
    /**
     * 线程池维护工作者线程的最大数量
     */
    private static final int MAX_WORKER_NUMBERS=30;

    /**
     * 线程池维护工作者线程的最默认工作数量
     */
    private static final int DEFAULT_WORKER_NUMBERS = 5;

    /**
     * 线程池维护工作者线程的最小数量
     */
    private static final int MIN_WORKER_NUMBERS = 1;

    /**
     * 维护一个工作列表,里面加入客户端发起的工作
     */
    private final LinkedList<T> jobs = new LinkedList<T>();

    /**
     * 工作者线程的列表
     */
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());

    /**
     * 工作者线程的数量
     */
    private int workerNum;
    /**
     *每个工作者线程编号生成
     */
    private AtomicLong threadNum = new AtomicLong();

    /**
     * 第一步:构造函数，用于初始化线程池
     * 首先判断初始化线程池的线程个数是否大于最大线程数，如果大于则线程池的默认初始化值为 DEFAULT_WORKER_NUMBERS
     */
    public DefaultThreadPool(int num){
        if (num > MAX_WORKER_NUMBERS) {
            this.workerNum =DEFAULT_WORKER_NUMBERS;
        } else {
            this.workerNum = num;
        }
        initializeWorkers(workerNum);
    }

    /**
     * 初始化每个工作者线程
     */
    private void initializeWorkers(int num) {
        for (int i = 0; i < num; i++) {
            Worker worker = new Worker();
            //添加到工作者线程的列表
            workers.add(worker);
            //启动工作者线程
            Thread thread = new Thread(worker);
            thread.start();
        }
    }

    /**
     * 执行一个任务(Job),这个Job必须实现Runnable
     *
     */





    @Override
    public void execute(T t) {
        //如果t为null，抛出空指针
        if (t==null){
            throw new NullPointerException();
        }
        //这里进行执行 TODO 当供大于求时候，考虑如何临时添加线程数
        if (t != null) {
            //根据线程的"等待/通知机制"这里必须对jobs加锁
            synchronized (jobs) {
                jobs.addLast(t);
                jobs.notify();
            }
        }

    }


    /**
     * 关闭线程池
     */
    @Override
    public void shutdown() {
        for (Worker worker:workers) {
            worker.shutdown();
        }
    }

    /**
     * 增加工作者线程，即用来执行任务的线程
     * @param num
     */
    @Override
    public void addWorkers(int num) {
        //加锁，防止该线程还没增加完成而下个线程继续增加导致工作者线程超过最大值
        synchronized (jobs) {
            if (num + this.workerNum > MAX_WORKER_NUMBERS) {
                num = MAX_WORKER_NUMBERS - this.workerNum;
            }
            initializeWorkers(num);
            this.workerNum += num;
        }
    }

    /**
     * 减少工作者线程
     * @param num
     */
    @Override
    public void removeWorker(int num) {
        synchronized (jobs) {
            if(num>=this.workerNum){
                throw new IllegalArgumentException("超过了已有的线程数量");
            }
            for (int i = 0; i < num; i++) {
                Worker worker = workers.get(i);
                if (worker != null) {
                    //关闭该线程并从列表中移除
                    worker.shutdown();
                    workers.remove(i);
                }
            }
            this.workerNum -= num;
        }

    }

    /**
     * 获取正在等待执行的任务数量
     */
    @Override
    public int getJobSize() {
        return workers.size();
    }

    /**
     * 消费者
     */
    class Worker implements Runnable {
        // 表示是否运行该worker
        private volatile boolean running = true;

        @Override
        public void run() {
            while (running) {
                T t = null;
                //线程的等待/通知机制
                synchronized (jobs) {
                    if (jobs.isEmpty()) {
                        try {
                            jobs.wait();//线程等待唤醒
                        } catch (InterruptedException e) {
                            //感知到外部对该线程的中断操作，返回
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    // 取出一个job
                    t = jobs.removeFirst();
                }
                //执行job
                if (t != null) {
                    t.run();
                }
            }
        }

        /**
         * 终止该线程
         */
        public void shutdown() {
            running = false;
        }
    }

}