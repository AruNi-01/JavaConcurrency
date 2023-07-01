package com.run.threadpool.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @desc: 简易线程池实现类
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
public class SimpleThreadPool implements ThreadPool {

    // 初始化线程池时的线程数量
    private int initialSize;

    // 任务队列（阻塞）
    private BlockingQueue<Runnable> taskQueue;

    // 存放工作线程的集合
    private List<WorkerThread> workers;

    // 标志线程池是否已关闭
    private volatile boolean isShutdown = false;


    /**
     * 内部类，工作线程
     */
    private final class WorkerThread extends Thread {

        /**
         * 重写 run 方法，让线程执行时从任务队列中取任务执行
         */
        @Override
        public void run() {
            // 循环从 taskQueue 中取任务执行
            while (!Thread.currentThread().isInterrupted() || !taskQueue.isEmpty()) {
                try {
                    // take()：阻塞直到从队列中取到任务
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    // 当线程阻塞时，收到 interrupt 信号会抛出 InterruptedException，故在此捕获处理
                    workers.remove(this);
                    break;
                }
            }
        }

    }

    /**
     * 构造函数，初始化线程池
     * @param initialSize 线程数量
     */
    public SimpleThreadPool(int initialSize) {
        this.initialSize = initialSize;
        taskQueue = new LinkedBlockingQueue<>();
        workers = new ArrayList<>(initialSize);

        // 初始化线程池时，创建并调用 start 方法启动工作线程
        for (int i = 0; i < initialSize; i++) {
            WorkerThread workerThread = new WorkerThread();
            workerThread.start();
            workers.add(workerThread);
        }
    }

    /**
     * 添加任务并执行，由于使用了阻塞队列，因此无需通知工作线程
     * @param task 任务
     */
    @Override
    public void execute(Runnable task) {
        // 线程池已关闭后，不允许再添加任务
        if (isShutdown) {
            throw new IllegalStateException("ThreadPool is shutdown.");
        }
        taskQueue.offer(task);
    }

    /**
     * 关闭线程池（优雅）：
     * 1. 修改 isShutdown 标志；
     * 2. 遍历所有工作线程，中断它们（interrupt() 方法并不会立即执行中断，取决于其线程本身）
     */
    @Override
    public void shutdown() {
        isShutdown = true;

        for (WorkerThread thread : workers) {
            thread.interrupt();
        }
    }

}
