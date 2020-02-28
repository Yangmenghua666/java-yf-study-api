package basekownledge.blockqueue;

import java.util.concurrent.TimeUnit;

/**
 * 阻塞队列顶级接口
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title BlockingQueue
 * @date 2020/2/25
 */
public interface BlockingQueue<E> {

    /**
     * 向队列中添加元素，成功则返回true,失败(队列已满)则抛出异常
     * @param e-元素
     * @title add
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return boolean
     */
    boolean add(E e);
    /**
     * 移除队首元素，成功则返回true,失败(队列为空)则抛异常
     * @title
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    boolean remove();
    /**
     * 向队尾插入元素，成功则返回true,失败(队列已满)则返回false
     * @param e-元素
     * @title offer
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return boolean
     */
    boolean offer(E e);
    /**
     * 移除队首元素，成功则返回元素，失败则返回null
     * @title poll
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return E
     */
    E poll();
    /**
     * 获取队首元素，成功则返回元素，失败返回null
     * @title peek
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return E
     */
    E peek();
    /**
     * 移除队首元素，成功则返回元素，失败(队列为空)则等待
     * @title take
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return E
     */
    E take() throws InterruptedException;
    /**
     * 向队尾插入元素，成功则返回true,失败(队列已满)则等待
     * @param e-元素
     * @title put
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return void
     */
    void put(E e) throws InterruptedException;
    /**
     * 向队尾插入元素，成功则返回true,若队列已满，则等待timeout的时间，如果还是失败，则返回false
     * @param e-元素
     * @param timeout-超时时间
     * @param unit-时间单位
     * @title offer
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return boolean
     */
    boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;
    /**
     * 移除队首元素，成功则返回元素e,若队列为空，则等待timeout指定的时间，如果还是失败，则返回null
     * @param timeout-超时时间
     * @param unit-时间单位
     * @title poll
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return E
     */
    E poll(long timeout,TimeUnit unit) throws InterruptedException;
    /**
     * 获取队列元素个数
     * @title size
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return int
     */
    int size();
    /**
     * 获取对列容量
     * @title length
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return int
     */
    int length();
}
