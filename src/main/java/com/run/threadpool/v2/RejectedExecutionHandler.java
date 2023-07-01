package com.run.threadpool.v2;

/**
 * @desc: 饱和拒绝策略接口
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
public interface RejectedExecutionHandler {

    /**
     * 拒绝执行（任务）
     * @param task 被拒绝的任务
     * @param pool 哪个线程池拒绝
     */
    void rejectedExecution(Runnable task, ThreadPool pool);

}
