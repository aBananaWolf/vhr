package cn.com.mq.threadpool;

import com.rabbitmq.client.impl.SetQueue;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wyl
 * @create 2020-08-14 12:29
 */
public class MessageScheduleThreadPool extends ScheduledThreadPoolExecutor {
    private ReentrantLock lock;
    private int blockQueueSize;


    public MessageScheduleThreadPool(int fixedThreadCoreCount,int blockQueueSize, RejectedExecutionHandler handler) {
        super(fixedThreadCoreCount, new VhrThreadFactory(), handler);
        super.setCorePoolSize(fixedThreadCoreCount);
        super.setMaximumPoolSize(fixedThreadCoreCount);
        this.blockQueueSize = blockQueueSize;
        Object blockingQueue = getQueue();
        try {
            Field lockField = blockingQueue.getClass().getDeclaredField("lock");
            lockField.setAccessible(true);
            this.lock = (ReentrantLock)lockField.get(blockingQueue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        lock.lock();
        try {
            if (super.getQueue().size() > blockQueueSize) {
                super.getRejectedExecutionHandler().rejectedExecution(command, this);
            }
            return super.schedule(command, delay, unit);
        } finally {
            lock.unlock();
        }
    }
    public static class VhrThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        VhrThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "vhr延迟任务调度-MessageScheduleThreadPool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
