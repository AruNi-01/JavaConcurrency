package com.run.threadpool.v1;

/**
 * @desc: 线程池接口
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
public interface ThreadPool {

    // 添加任务并执行
    void execute(Runnable task);

    // 优雅关闭，等待已添加的任务执行完毕后再关闭
    void shutdown();

}
