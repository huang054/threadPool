package com;

/**
 * 线程池方法定义
 * @author hrabbit
 */
public interface ThreadPools<T extends Runnable>{

    /**
     * 执行一个任务(Job),这个Job必须实现Runnable
     *
     */
    public void execute(T t);

    /**
     * 关闭线程池
     */
    public void shutdown();

    /**
     * 增加工作者线程，即用来执行任务的线程
     * @param num
     */
    public void addWorkers(int num);

    /**
     * 减少工作者线程
     * @param num
     */
    public void removeWorker(int num);

    /**
     * 获取正在等待执行的任务数量
     */
    public int getJobSize();
}