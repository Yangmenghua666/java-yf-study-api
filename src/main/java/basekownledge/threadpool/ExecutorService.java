package basekownledge.threadpool;

import java.util.concurrent.Future;

/**
 * 线程池默认方法定义接口
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title ExecutorService
 * @date 2020/2/27
 */
public interface ExecutorService extends Executor{

    /**
     * 任务提交(无返回值)
     * @param command-任务
     * @title submit
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    void submit(Runnable command);
    /**
     * 任务提交(有返回值)
     * @param command-任务
     * @param result-保存返回结果
     * @title submit
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return Future<T>
     */
     <T> Future<T> submit(Runnable command,T result);
     /**
      * 关闭线程池
      * @title shutdown
      * @author yuanfei0241@hsyuntai.com
      * @since v1.0.0
      * @return
      */
     void shutdown();
     /**
      * 检查线程池是否关闭
      * @title isShutdown
      * @author yuanfei0241@hsyuntai.com
      * @since v1.0.0
      * @return
      */
     boolean isShutdown();
}
