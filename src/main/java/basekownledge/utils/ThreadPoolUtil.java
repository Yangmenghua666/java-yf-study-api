package basekownledge.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * 线程池工具类
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title ThreadPoolUtil
 * @date 2020/2/28
 */
public class ThreadPoolUtil {
    /**
     * 核心线程数
     */
    private static final int CORE_SIZE = 20;
    /**
     * 最大线程数
     */
    private static final int MAX_SIZE = 200;
    /**
     * 默认等待3s
     */
    private static final long KEEPALIVE_TIME = 3;
    /**
     * 线程工程
     */
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("yf-threadPool").build();
    /**
     * 任务队列
     */
    private static final ArrayBlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(1024);

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            CORE_SIZE,
            MAX_SIZE,
            KEEPALIVE_TIME,
            TimeUnit.SECONDS,
            taskQueue,
            threadFactory,new ThreadPoolExecutor.AbortPolicy());
    /**
     * 提交任务执行
     * @param task-任务
     * @title submit
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    public static void submit(Runnable task){
        executorService.execute(task);
    }
    /**
     * 批量提交任务
     * @param tasks-任务数组
     * @title submitTaskList
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    public static void submitTaskList(ArrayList<Runnable> tasks){
        if(CollectionUtils.isEmpty(tasks)){
            throw new NullPointerException("任务队列为空!");
        }
        tasks.forEach(executorService::execute);
    }
}
