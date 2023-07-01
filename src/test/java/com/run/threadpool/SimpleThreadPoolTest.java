package com.run.threadpool;

import com.run.threadpool.v1.SimpleThreadPool;
import org.junit.Test;

import java.io.Writer;
import java.util.concurrent.TimeUnit;

/**
 * @desc: SimpleThreadPool 测试类
 * @author: AruNi_Lu
 * @date: 2023-07-01
 */
public class SimpleThreadPoolTest {

    @Test
    public void testV1() throws InterruptedException {
        SimpleThreadPool pool = new SimpleThreadPool(3);

        for (int i = 0; i < 10; i++) {
            pool.execute(() -> System.out.println(Thread.currentThread().getName() + ": executing task..."));
        }

        Thread.sleep(1000);     // 主线程等待任务执行完毕
        pool.shutdown();
    }

    @Test
    public void testV2() throws InterruptedException {
        com.run.threadpool.v2.SimpleThreadPool pool = new com.run.threadpool.v2.SimpleThreadPool(3, 3, 5, 8, 10, TimeUnit.MILLISECONDS);

        // 控制任务数量，不要让饱和拒绝策略触发
        for (int i = 0; i < 12; i++) {
            pool.execute(() -> {
                for (int j = 0; j < 100; j++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        Thread.sleep(10_000_000);   // 让主线程一直睡眠，方便我们观察结果
        pool.shutdown();
    }

}
