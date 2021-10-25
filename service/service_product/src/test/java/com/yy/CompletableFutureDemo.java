package com.yy;

import java.util.concurrent.*;

/**
 * @author Yu
 * @create 2021-10-10 15:58
 */
public class CompletableFutureDemo {

    public static void main(String[] args) {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,//常驻核心线程数
                5,//同时执行的最大线程数
                2L,//多余空闲线程的存活时间
                TimeUnit.SECONDS,//存活时间单位
                new LinkedBlockingQueue<Runnable>(3),//任务队列
                Executors.defaultThreadFactory(),//线程池工厂,创建线程
                new ThreadPoolExecutor.AbortPolicy());//拒绝策略

        CompletableFuture.runAsync(() -> {
            System.out.println("runAsync-----1"+Thread.currentThread().getName());
        },threadPoolExecutor).thenAccept((t) -> {
            System.out.println("runAsync-----2"+Thread.currentThread().getName());
        });
        CompletableFuture.supplyAsync(() -> {
            System.out.println("supplyAsync-----"+Thread.currentThread().getName());
            return "1";
        });
    }

}
