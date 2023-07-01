package com.run.threadpool.v1;

import java.util.concurrent.BlockingQueue;

/**
 * @desc: 工作线程
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
@Deprecated
public class WorkerThread extends Thread{

    // 任务队列（阻塞）
    private BlockingQueue<Runnable> taskQueue;

    /**
     * 构造函数，将 taskQueue 注入进来，方便从中取任务执行
     * @param taskQueue 任务队列
     */
    public WorkerThread(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

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
                // 当线程阻塞时，收到 interrupt 信号会抛出 InterruptedException，故在此捕获处理（中断一下此线程）
                interrupt();
                break;
            }
        }
    }
}
