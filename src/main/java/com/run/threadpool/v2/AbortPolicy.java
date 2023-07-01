package com.run.threadpool.v2;

/**
 * @desc: 抛出异常策略
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
public class AbortPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable task, ThreadPool pool) {
        throw new RuntimeException("Task queue is full and maximum number of threads has been reached");
    }

}
