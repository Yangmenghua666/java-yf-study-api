package basekownledge.threadpool;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程池具体实现类
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title ThreadPoolExecutor
 * @date 2020/2/27
 */
public class ThreadPoolExecutor implements ExecutorService {
    /**
     * 是否允许核心线程超时终止(默认false)
     */
    private volatile boolean allowShutdownCoreThreadTimeOut;
    /**
     * 超时时间
     */
    private volatile long keepAliveTime;
    /**
     * 时间单位
     */
    private volatile TimeUnit unit;
    /**
     * 核心线程数
     */
    private volatile int coreThreadSize;
    /**
     * 最大线程数
     */
    private volatile int maxThreadSize;
    /**
     * 任务队列
     */
    private final BlockingQueue<Runnable> taskQueue;
    /**
     * 工作线程
     */
    private final HashSet<Worker> workers = new HashSet<>();
    /**
     * 线程工厂
     */
    private ThreadFactory threadFactory;
    /**
     * 拒绝策略
     */
    private RejectedExecutionHandler rejectedHandler;
    /**
     * 锁
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");
    /**
     * ctl是控制线程的状态的，里面包含两个状态，线程的数量和线程池运行的状态
     * 高3位是用来保存线程池运行的状态的，低29位用于保存线程的数量，
     */
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    /**
     * COUNT_BITS ＝29，用于位于操作
     */
    private static final int COUNT_BITS = Integer.SIZE - 3;
    /**
     * 线程的容量
     */
    private static final int CAPACITY = (1 << COUNT_BITS) - 1;
    /**
     * 高3位：111：接受新任务并且继续处理阻塞队列中的任务(-536870912)
     */
    private static final int RUNNING = -1 << COUNT_BITS;
    /**
     * 高3位：000：不接受新任务但是会继续处理阻塞队列中的任务(0)
     */
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    /**
     * 计算线程池状态
     * @param c
     * @return
     */
    private static int runStateOf(int c) {
        return c & ~CAPACITY;
    }
    /**
     * 计算当前工作线程数
     * @param c
     * @return
     */
    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }
    /**
     * 计算ctl
     * @param rs
     * @param wc
     * @return
     */
    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }


    /**
     * 构造函数
     * @param coreThreadSize
     * @param maxThreadSize
     * @param keepAliveTime
     * @param unit
     * @param threadFactory
     * @param rejectedHandler
     */
    public ThreadPoolExecutor(int coreThreadSize,int maxThreadSize,long keepAliveTime
            ,TimeUnit unit,ThreadFactory threadFactory,BlockingQueue<Runnable> taskQueue,RejectedExecutionHandler rejectedHandler){
        this(coreThreadSize,maxThreadSize,keepAliveTime,unit,threadFactory,taskQueue,rejectedHandler,false);
    }

    public ThreadPoolExecutor(int coreThreadSize,int maxThreadSize,long keepAliveTime
            ,TimeUnit unit,ThreadFactory threadFactory,BlockingQueue<Runnable> taskQueue
            ,RejectedExecutionHandler rejectedHandler,Boolean allowShutdownCoreThreadTimeOut){

        if(coreThreadSize < 0 || maxThreadSize <= 0 || keepAliveTime <= 0){
            throw new IllegalArgumentException();
        }
        if(null == unit || null == threadFactory || null== taskQueue
                || null == rejectedHandler || null == allowShutdownCoreThreadTimeOut){
            throw  new NullPointerException();
        }
        this.coreThreadSize = coreThreadSize;
        this.maxThreadSize = maxThreadSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.threadFactory = threadFactory;
        this.rejectedHandler = rejectedHandler;
        this.allowShutdownCoreThreadTimeOut = allowShutdownCoreThreadTimeOut;
        this.taskQueue = taskQueue;
    }

    @Override
    public void submit(Runnable command) {
        execute(command);
    }

    @Override
    public <T> Future<T> submit(Runnable command, T result) {
        RunnableFuture<T> fTask = new FutureTask<>(command,result);
        execute(fTask);
        return fTask;
    }

    @Override
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            //将线程池状态设置为shutdown
            ctl.compareAndSet(ctl.get(), ctlOf(SHUTDOWN, workerCountOf(ctl.get())));
            //检查调用shutdown的线程是否有权限操作线程池
            checkShutdownAccess();
            //尝试中断所有线程
            interruptIdleWorkers();
        } finally {
            mainLock.unlock();
        }
    }
    /**
     * 尝试中断所有线程
     * @title interruptIdleWorkers
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    private void interruptIdleWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
            }
        } finally {
            mainLock.unlock();
        }
    }
    /**
     * 检查是否有权限对woker进行操作
     * @title checkShutdownAccess
     * @author yuanfei0241@hsyuntai.com
     * @since v1.0.0
     * @return
     */
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers) {
                    security.checkAccess(w.thread);
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    @Override
    public boolean isShutdown() {
        return (0 == runStateOf(ctl.get()));
    }

    @Override
    public void execute(Runnable command) {
        if(null == command){
            throw new NullPointerException();
        }
        /**
         * 紧接着会进行如下三个步骤：
         *
         * 1：如果当前运行的线程数小于 corePoolSize，则马上尝试使用command对象创建一个新线程。
         * 调用addWorker()方法进行原子性检查runState和workerCount,然后通过返回false来防止在不应该
         * 添加线程时添加了线程产生的错误警告。
         *
         * 2：如果一个任务能成功添加到任务队列，在我们添加一个新的线程时仍然需要进行双重检查
         * (因为自 上一次检查后，可能线程池中的其它线程全部都被回收了) 或者在进入此方法后，
         * 线程池已经 shutdown了。所以我们必须重新检查状态，如果有必要，就在线程池shutdown时采取
         * 回滚入队操作移除任务，如果线程池的工作线程数为0，就启动新的线程。
         *
         * 3：如果任务不能入队，那么需要尝试添加一个新的线程，但如果这个操作失败了，那么我们知道线程
         * 池可能已经shutdown了或者已经饱和了，从而拒绝任务。
         */

        //获取线程池控制状态
        int c = ctl.get();
        if (workerCountOf(c) < coreThreadSize) {
            //如果工作线程数 < 核心线程数
            if (addWorker(command, true)) {
                //添加一个工作线程来运行任务，如果成功了，则直接返回
                return;
            }
            //如果添加线程失败了，就再次获取线程池控制状态
            c = ctl.get();
        }
        //如果线程池处理RUNNING状态，则尝试把任务添加到任务队列
        if (isRunning(c) && taskQueue.offer(command)) {
            // // 再次检查，获取线程池控制状态
            int recheck = ctl.get();
            //如果线程池已经不是RUNNING状态了，把任务从队列中移除，并执行拒绝任务策略
            //「可能线程池已经被关闭了」
            if (!isRunning(recheck) && remove(command)){
                reject(command);
            }
            //如果工作线程数为0，就添加一个新的工作线程
            //「因为旧线程可能已经被回收了，所以工作线程数可能为0」
            else if(workerCountOf(recheck) == 0){
                addWorker(command, false);
            }
        }else if(!addWorker(command,false)){
            //这个else if里面的addWorker就是添加非核心线程
            reject(command);
        }
    }

    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            //先获取线程池控制状态
            int c = ctl.get();
            //获取线程池的状态
            int rs = runStateOf(c);
            //如果线程池的状态为RUNNING状态，直接跳过这个检查
            //如果线程池状态不为RUNNING，那么当满足
            // 状态为SHUTDOWN并且任务为null，任务队列不为空的时候也跳过这个检查
            //「也就是说：如果线程池的状态SHUTDOWN时,它不接收新任务,但是会继续运行任务队列中的任务」
            if (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workers.isEmpty())){
                return false;
            }
            for (;;) {
                //如果线程池继续工作！获取工作线程的数量
                int wc = workerCountOf(c);
                if (wc >= CAPACITY || wc >= (core ? coreThreadSize : maxThreadSize)){
                    return false;
                }
                if (compareAndIncrementWorkerCount(c)){
                    break retry;
                }
                c = ctl.get();
                if (runStateOf(c) != rs){
                    continue retry;
                }
            }
        }
        //工作线程开始的标记
        boolean workerStarted = false;
        //工作线程被添加的标记
        boolean workerAdded = false;
        //Worker包装了线程和任务
        Worker w = null;
        try {
            w = new Worker(threadFactory,firstTask);
            //获取worker对应的线程
            final Thread t = w.thread;
            if (t != null) {
                //如果线程不为null
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    //拿着锁重新检查池程池的状
                    int rs = runStateOf(ctl.get());
                    //线程池的状态为RUNNING或者(线程池的状态SHUTDOWN并且提交的任务为null时)
                    if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                        //如果线程已经运行了或者还没有死掉，抛出一个IllegalThreadStateException异常
                        if (t.isAlive()){
                            throw new IllegalThreadStateException();
                        }
                        //把worker加入到工作线程Set里面
                        workers.add(w);
                        //工作线程被添加的标记置为true
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    //如果工作线程已经被添加到工作线程池了
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted){
                //如果没有添加，那么移除任务，并减少工作线程的数量(-1)
                addWorkerFailed(w);
            }
        }
        return workerStarted;
    }

    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null) {
                workers.remove(w);
            }
            ctl.compareAndSet(ctl.get(), ctl.get() - 1);
        } finally {
            mainLock.unlock();
        }
    }
    /**
     * 判断当前线程池是否正在运行
     */
    final boolean isRunning(int c){
        return c < SHUTDOWN;
    }

    final boolean remove(Runnable command){
        return taskQueue.remove(command);
    }

    private void reject(Runnable command){
        //暂时什么都不做
    }

    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    private final class Worker extends ReentrantLock implements Runnable{
        /**
         * 线程
         */
        public final Thread thread;
        /**
         * 任务
         */
        public Runnable task;

        public Worker(ThreadFactory threadFactory, Runnable task){
            this.thread = threadFactory.newThread(this);
            this.task = task;
        }

        @Override
        public void run() {
            runWorker(this);
        }

        final void runWorker(Worker w) {
            //获取当前线程（和worker绑定的线程）
            Runnable task = w.task;
            w.task = null;
            //这个while循环，保证了如果任务队列中还有任务就继续拿出来执行，注意这里的短路情况
            while (task != null || (task = getTask()) != null) {
                w.lock();
                try {
                    try {
                        //开始正式运行任务
                        task.run();
                    } catch (RuntimeException x) {
                        throw x;
                    } catch (Error x) {
                        throw x;
                    } catch (Throwable x) {
                        throw new Error(x);
                    }
                } finally {
                    task = null;
                    w.unlock();
                }
            }
            final ReentrantLock reentrantLock = mainLock;
            reentrantLock.lock();
            try {
                workers.remove(w);
                ctl.compareAndSet(ctl.get(),ctl.get() - 1);
            }finally {
                reentrantLock.unlock();
            }
        }

        private Runnable getTask() {
            for (;;) {
                int c = ctl.get();
                //获取工作线程的数量
                int wc = workerCountOf(c);
                // 是否允许核心线程超时或者当前工作线程数是否大于核心线程数
                boolean timed = allowShutdownCoreThreadTimeOut || wc > coreThreadSize;
                try {
                    Runnable r = timed ? taskQueue.poll(keepAliveTime, unit) : taskQueue.take();
                    return r;
                } catch (InterruptedException retry) {
                    retry.printStackTrace();
                }
            }
        }
    }
}
