package basekownledge.blockqueue;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数组阻塞队列
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title ArrayBlockingQueue
 * @date 2020/2/25
 */
public class ArrayBlockingQueue<E> implements BlockingQueue<E>, Serializable {

    private static final Long serialVersionUID = 829938874745L;
    /**
     * 队列容器
     */
    private Object[] items;
    /**
     * 队尾元素下标
     */
    private AtomicInteger putIndex;
    /**
     * 队首元素下标
     */
    private AtomicInteger takeIndex;
    /**
     * 队列元素个数
     */
    private AtomicInteger count;
    /**
     * 锁
     */
    private ReentrantLock reentrantLock;
    /**
     * 队列未空条件
     */
    private final Condition notEmptyCondition;
    /**
     * 队列未满条件
     */
    private final Condition notFullCondition;
    /**
     * 默认的构造函数
     */
    public ArrayBlockingQueue(Integer length) {
        this(length,false);
    }
    /**
     * 可指定公平性的构造函数
     */
    public ArrayBlockingQueue(Integer length,boolean fair){
        items = new Object[length];
        putIndex = new AtomicInteger(0);
        takeIndex = new AtomicInteger(0);
        count = new AtomicInteger(0);
        reentrantLock = new ReentrantLock(fair);
        notEmptyCondition = reentrantLock.newCondition();
        notFullCondition =  reentrantLock.newCondition();
    }

    @Override
    public boolean add(E e) {
        if(this.offer(e)){
            return true;
        }
        throw new IllegalStateException("队列已满!");
    }

    @Override
    public boolean remove() {
        if(null != poll()){
            return true;
        }
        throw new IllegalStateException("队列为空!");
    }

    @Override
    public boolean offer(E e) {
        if(null == e){
            return false;
        }
        try {
            reentrantLock.lock();
            if(count.get() == items.length){
                //队列已满
                return false;
            }
            this.insert(e);
            return true;
        }finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public E poll() {
        try {
            reentrantLock.lock();
            if(count.get() == 0){
                return null;
            }
            return delete();
        }finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public E peek() {
        try {
            reentrantLock.lock();
            if(count.get() == 0){
                return null;
            }
            return (E)items[takeIndex.get()];
        }finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException{
        reentrantLock.lock();
        E result = null;
        try {
            while (count.get() == 0) {
                notEmptyCondition.await();
            }
            result = delete();
            notFullCondition.signal();
        } finally {
            reentrantLock.unlock();
        }
        return result;
    }

    @Override
    public void put(E e) throws InterruptedException {
        reentrantLock.lock();
        try {
            while (count.get() == items.length) {
                notFullCondition.await();
            }
            insert(e);
            notEmptyCondition.signal();
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if(null == e){
            return false;
        }
        long nanos = unit.toNanos(timeout);
        reentrantLock.lockInterruptibly();
        try {
            while (count.get() == items.length){
                if(nanos <= 0L){
                    return false;
                }
                nanos = notFullCondition.awaitNanos(nanos);
            }
            insert(e);
            notEmptyCondition.signal();
            return true;
        }finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        reentrantLock.lockInterruptibly();
        long nanos = unit.toNanos(timeout);
        try {
            while (count.get() == 0){
                if(nanos <= 0){
                    return null;
                }
                nanos = notEmptyCondition.awaitNanos(nanos);
            }
            E result = delete();
            notEmptyCondition.signal();
            return result;
        }finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public int size() {
        return count.get();
    }

    @Override
    public int length() {
        return items.length;
    }

    /**
     * 插入操作
     * @param e-元素
     * @title insert
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     */
    private void insert(E e){
        items[putIndex.get()] = e;
        if (putIndex.incrementAndGet() == items.length) {
            putIndex.set(0);
        }
        count.incrementAndGet();
    }
    /**
     * 移除元素
     * @title delete
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return E
     */
    private E delete(){
        E result = (E)items[takeIndex.get()];
        if (takeIndex.incrementAndGet() == items.length) {
            takeIndex.set(0);
        }
        count.decrementAndGet();
        return result;
    }
}
