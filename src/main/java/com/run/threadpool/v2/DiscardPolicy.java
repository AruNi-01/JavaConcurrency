package com.run.threadpool.v2;

/**
 * @desc: 拒绝任务策略
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
public class DiscardPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable task, ThreadPool pool) {
        // 拒绝该任务，什么也不做
        System.out.println("Discard task: " + task);
    }

}
