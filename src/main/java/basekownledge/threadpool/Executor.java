package basekownledge.threadpool;
/**
 * 线程池顶层框架
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title Executor
 * @date 2020/2/27
 */
public interface Executor {
    /**
     * 执行任务-在这里面会执行worker等操作
     * @param command-需要执行的任务
     * @title execute
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    void execute(Runnable command);
}
