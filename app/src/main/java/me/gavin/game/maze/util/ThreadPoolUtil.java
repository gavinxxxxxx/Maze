package me.gavin.game.maze.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadPoolUtil
 *
 * @author CaMnter by 2015-11-25 11:12
 * @link {https://github.com/CaMnter/AndroidUtils/blob/master/androidutils/src/main/java/com/camnter/androidutils/utils/ThreadPoolUtil.java}
 */
public final class ThreadPoolUtil {

    private static ExecutorService singlePool;
    private static ExecutorService fixedPool;
    private static ExecutorService cachedPool;
    private static ExecutorService scheduledPool;

    /**
     * 创建一个单线程的线程池。这个线程池只有一个线程在工作,也就是相当于单线程串行执行所有任务.
     * 如果这个唯一的线程因为异常结束,那么会有一个新的线程来替代它.此线程池保证所有任务的执行顺
     * 序按照任务的提交顺序执.
     *
     * @return ExecutorService
     */
    public static ExecutorService getSingleThreadExecutor() {
        if (singlePool == null) {
            singlePool = Executors.newSingleThreadExecutor();
        }
        return singlePool;
    }

    /**
     * 创建固定大小的线程池.每次提交一个任务就创建一个线程,直到线程达到线程池的最大大小.
     * 线程池的大小一旦达到最大值就会保持不变,如果某个线程因为执行异常而结束,那么线程池
     * 会补充一个新线程.
     *
     * @param count thread count
     * @return ExecutorService
     */
    public static ExecutorService getFixedThreadPool(int count) {
        if (fixedPool == null) {
            fixedPool = Executors.newFixedThreadPool(count);
        }
        return fixedPool;
    }

    /**
     * 创建一个可缓存的线程池.如果线程池的大小超过了处理任务所需要的线程,那么就会回收部分
     * 空闲（60秒不执行任务）的线程,当任务数增加时,此线程池又可以智能的添加新线程来处理任
     * 务.此线程池不会对线程池大小做限制,线程池大小完全依赖于操作系统（或者说JVM）能够创建
     * 的最大线程大小.
     *
     * @return ExecutorService
     */
    public static ExecutorService getCachedThreadPool() {
        if (cachedPool == null) {
            cachedPool = Executors.newCachedThreadPool();
        }
        return cachedPool;
    }

    /**
     * 创建一个大小无限的线程池.此线程池支持定时以及周期性执行任务的需求.
     *
     * @param corePoolSize corePoolSize
     * @return ExecutorService
     */
    public static ExecutorService getScheduledThreadPool(int corePoolSize) {
        if (scheduledPool == null) {
            scheduledPool = Executors.newScheduledThreadPool(corePoolSize);
        }
        return scheduledPool;
    }


    public static void execute(ExecutorService executorService, Runnable command) {
        executorService.execute(command);
        executorService.shutdown();
    }

}