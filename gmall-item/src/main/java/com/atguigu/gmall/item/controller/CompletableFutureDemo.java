package com.atguigu.gmall.item.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 *      不带Async的是同步的，使用当前线程，不需要指定线程池，所有带Async指是异步的，不使用当前的线程，可以指定线程池，如果不指定则使用默认的
 *
 *      创建异步对象
 *      public static CompletableFuture<Void> runAsync(Runnable runnable)
 *      public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
 *      public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
 *      public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
 *
 *      计算完成时回调方法
 *      public CompletableFuture<T> whenComplete(BiConsumer<? super T,? super Throwable> action);
 *      public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action);
 *      public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action, Executor executor);
 *      public CompletableFuture<T> exceptionally(Function<Throwable,? extends T> fn);
 *
 *      线程串行化方法
 *      1、当一个线程依赖另一个线程时，获取上一个任务返回的结果，并返回当前任务的返回值。
 *      public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
 *      public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
 *      public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
 *      2、获取上一个任务返回的结果，无返回结果。
 *      public CompletionStage<Void> thenAccept(Consumer<? super T> action);
 *      public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
 *      public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);
 *      3、没有接受上一个任务的返回结果，无返回值
 *      public CompletionStage<Void> thenRun(Runnable action);
 *      public CompletionStage<Void> thenRunAsync(Runnable action);
 *      public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);
 *
 *      等待所有所有任务都完成
 *      public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs);
 *      任意一个任务完成
 *      public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs);
 */
public class CompletableFutureDemo {

    public static void main(String[] args) {
        CompletableFuture<Void> runAsyncCompletableFuture = CompletableFuture.runAsync(() -> {
            System.out.println("通过runAsync方法创建异步对象");
        });

//        System.out.println(ForkJoinPool.getCommonPoolParallelism());

        CompletableFuture.runAsync(() -> {
            System.out.println("通过runAsync方法创建异步对象");
        }, Executors.newFixedThreadPool(10));

        CompletableFuture<String> sopplyCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("通过supplyAsync方法创建异步对象");
//            int i = 1/0;
            return "haha";
        });

        CompletableFuture<String> applyCompletableFuture = sopplyCompletableFuture.thenApplyAsync(t -> {
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个线程的返回结果集是: " + t);
            return "apply";
        });
        CompletableFuture<Void> acceptCompletableFuture = sopplyCompletableFuture.thenAcceptAsync(t -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个线程的返回结果集是: " + t);
        });
        CompletableFuture<Void> runCompletableFuture = sopplyCompletableFuture.thenRunAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("没有上一个线程的返回结果集,也没有返回值");
        });
        sopplyCompletableFuture.whenCompleteAsync((t, u) ->{
            System.out.println("正常的返回结果集是: " + t);
            System.out.println("异常信息是: " + u);
        });

        CompletableFuture.allOf(applyCompletableFuture,acceptCompletableFuture,runCompletableFuture).join();

        System.out.println("可以提交了");
    }
}
