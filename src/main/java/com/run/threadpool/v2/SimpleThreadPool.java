package com.run.threadpool.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @desc: 简易线程池实现类
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
public class SimpleThreadPool implements ThreadPool {

    // 初始化线程池时的线程数量
    private int initialSize;

    // 核心线程数
    private int coreSize;

    // 最大线程数
    private int maxSize;

    // 任务队列大小
    private int queueSize;

    // 任务队列（阻塞）
    private BlockingQueue<Runnable> taskQueue;

    // 存放工作线程的集合
    private List<WorkerThread> workers;

    // 标志线程池是否已关闭
    private volatile boolean isShutdown = false;

    // 默认拒绝策略
    private final static RejectedExecutionHandler DEFAULT_REJECT_HANDLER = new AbortPolicy();

    // 拒绝策略
    private final RejectedExecutionHandler rejectedExecutionHandler;

    // 临时线程存活时间
    private long keepAliveTime;

    /**
     * 内部类，工作线程
     */
    private final class WorkerThread extends Thread {
        /**
         * 重写 run 方法，让线程执行时从任务队列中取任务执行
         */
        @Override
        public void run() {
            // 记录该工作线程最后执行任务的时间
            long lastActiveTime = System.currentTimeMillis();

            // 需要统一转为 nanosSecond 来比较
            TimeUnit ms = TimeUnit.MILLISECONDS;

            // 循环从 taskQueue 中取任务执行
            while (!Thread.currentThread().isInterrupted() || !taskQueue.isEmpty()) {
                try {
                    // poll()：阻塞直到从队列中取到任务，或者到达超时时间
                    Runnable task = taskQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS);

                    if (task != null) {
                        System.out.printf("WorkerThread %s, executing task: %s\n", Thread.currentThread().getName(), "My Task...");
                        task.run();

                        // 执行完任务后更新 lastActiveTime
                        lastActiveTime = System.currentTimeMillis();
                    } else if (workers.size() > coreSize &&
                            ms.toNanos(System.currentTimeMillis()) - ms.toNanos(lastActiveTime) >= keepAliveTime) {
                        // 临时线程已到达存活时间，则从工作线程集合中移除，然后跳出循环
                        System.out.printf("Temp worker thread %s, exit workers queue\n", Thread.currentThread().getName());
                        workers.remove(this);
                        break;
                    }
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
     * @param initialSize 初始化线程数
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param queueSize 任务队列大小
     * @param keepAliveTime 临时线程存活时间
     * @param unit 临时线程存活时间单位
     */
    public SimpleThreadPool(int initialSize, int coreSize, int maxSize, int queueSize, long keepAliveTime, TimeUnit unit) {
        this(initialSize, coreSize, maxSize, queueSize, keepAliveTime, unit, DEFAULT_REJECT_HANDLER);
    }

    /**
     * 构造函数，初始化线程池
     * @param initialSize 初始化线程数
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param queueSize 任务队列大小
     * @param keepAliveTime 临时线程存活时间
     * @param unit 临时线程存活时间单位
     * @param rejectedHandler 饱和拒绝策略
     */
    public SimpleThreadPool(int initialSize, int coreSize, int maxSize, int queueSize, long keepAliveTime, TimeUnit unit, RejectedExecutionHandler rejectedHandler) {
        if (initialSize < 0 || coreSize < 0 || maxSize <= 0 || maxSize < coreSize || keepAliveTime < 0) {
            throw new IllegalArgumentException();
        }

        this.initialSize = initialSize;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        taskQueue = new LinkedBlockingQueue<>(queueSize);
        workers = new ArrayList<>(initialSize);
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.rejectedExecutionHandler = rejectedHandler;

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

        // 当前工作线程数 < 核心线程数时，启动新的线程来执行任务
        if (workers.size() < coreSize) {
            addWorkerThread(task);
        } else if (!taskQueue.offer(task)) {    // check 任务队列是否已满，未满则添加进入，已满则进入分支
            // 当前工作线程数 < 最大线程数时，启动新的（临时）线程来执行任务
            if (workers.size() < maxSize) {
                addWorkerThread(task);
            } else {
                // 使用饱和拒绝策略
                rejectedExecutionHandler.rejectedExecution(task, this);
            }
        }
    }

    /**
     * 启动新的工作线程，将任务放入队列中以执行
     * @param task 任务
     */
    private void addWorkerThread(Runnable task) {
        WorkerThread workerThread = new WorkerThread();
        workerThread.start();
        workers.add(workerThread);
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
