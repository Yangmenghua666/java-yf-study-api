package basekownledge.lock;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 自定义lock
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title ReentrantLock
 * @date 2020/2/28
 */
public class ReentrantLock {

    private final Sync sync;

    /**
     * 默认非公平锁
     */
    public ReentrantLock(){
        this(false);
    }

    public ReentrantLock(boolean fair) {
        this.sync = fair ? new FairSync() : new NoFairSync();
    }
    /**
     * 加锁-直接调用sync中的lock
     * @title lock
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     */
    public void lock(){
        sync.lock();
    }

    public void unlock(){
        sync.release(1);
    }
    /**
     * 真正的锁
     * @author yuanfei0241@hsyuntai.com
     * @version V1.0.0
     * @title ReentrantLock
     * @date 2020/2/28
     */
    abstract static class Sync extends AbstractQueuedSynchronizer{
        /**
         * 加锁
         * @title lock
         * @author yuanfei0241@hsyuntai.com
         * @since v1.0.0
         */
        abstract void lock();

        @Override
        protected boolean tryRelease(int releases) {
            //计算待更新的state值
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread()){
                throw new IllegalMonitorStateException();
            }
            boolean free = false;
            if (c == 0) {
                //待更新的state值为0,说明持有锁的线程未重入,一旦释放锁其他线程将能获取
                free = true;
                //清除锁的持有线程标记
                setExclusiveOwnerThread(null);
            }
            //更新state值
            setState(c);
            return free;
        }
    }
    /**
     * 公平锁
     * @author yuanfei0241@hsyuntai.com
     * @version V1.0.0
     * @title ReentrantLock
     * @date 2020/2/28
     */
    static final class FairSync extends Sync{

        @Override
        void lock() {
            acquire(1);
        }

        @Override
        protected boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                //hasQueuedPredecessors就进行了优先级判断
                if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }
    }
    /**
     * 非公平锁
     * @author yuanfei0241@hsyuntai.com
     * @version V1.0.0
     * @title ReentrantLock
     * @date 2020/2/28
     */
    static final class NoFairSync extends Sync{

        @Override
        void lock() {
            //以cas方式尝试将AQS中的state从0更新为1
            if(compareAndSetState(0,1)){
                //获取锁成功则将当前线程标记为持有锁的线程,然后直接返回
                setExclusiveOwnerThread(Thread.currentThread());
                return;
            }
            acquire(1);
        }

        @Override
        protected boolean tryAcquire(int arg) {
            return nonfairTryAcquire(arg);
        }

        private boolean nonfairTryAcquire(int acquire){
            final Thread currentThread = Thread.currentThread();
            //获取当前锁被重入的次数
            int c = getState();
            if(c == 0){
                //说明当前锁没有被任何线程持有
                if(compareAndSetState(0,acquire)){
                    //将当前线程标记为持有锁的线程
                    setExclusiveOwnerThread(currentThread);
                    return true;
                }
            }else if(currentThread == getExclusiveOwnerThread()){
                //当前线程就是持有锁的线程,所以在这里不需要考虑同步问题
                int nextc = c + acquire;
                if (nextc < 0) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }
    }
}
