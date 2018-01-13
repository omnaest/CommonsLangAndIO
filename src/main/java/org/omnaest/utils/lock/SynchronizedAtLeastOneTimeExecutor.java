package org.omnaest.utils.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wrapper around a {@link ExecutorService} which does {@link #fire()} a given {@link Runnable} only one time if {@link #fire()} is multiple times invoked
 * 
 * @see #fire()
 * @author omnaest
 */
public class SynchronizedAtLeastOneTimeExecutor
{
    private AtomicInteger   barrierCounter = new AtomicInteger();
    private Lock            lock           = new ReentrantLock();
    private ExecutorService executorService;
    private Runnable        runnable;

    public SynchronizedAtLeastOneTimeExecutor(ExecutorService executorService, Runnable runnable)
    {
        super();
        this.executorService = executorService;
        this.runnable = runnable;
    }

    /**
     * Fires the execution of the {@link Runnable}
     * 
     * @return
     */
    public SynchronizedAtLeastOneTimeExecutor fire()
    {
        if (this.barrierCounter.get() <= 0)
        {
            this.executorService.submit(() ->
            {
                this.barrierCounter.incrementAndGet();
                this.lock.lock();
                this.barrierCounter.decrementAndGet();
                try
                {
                    this.runnable.run();
                }
                finally
                {
                    this.lock.unlock();
                }
            });
        }

        return this;
    }

    public SynchronizedAtLeastOneTimeExecutor shutdown()
    {
        this.executorService.shutdown();
        return this;
    }

    public boolean awaitTermination(long timeout, TimeUnit timeUnit)
    {
        try
        {
            return this.executorService.awaitTermination(timeout, timeUnit);
        }
        catch (InterruptedException e)
        {
            return false;
        }
    }

    public ExecutorService getExecutorService()
    {
        return this.executorService;
    }

}
