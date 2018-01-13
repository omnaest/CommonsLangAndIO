package org.omnaest.utils.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wrapper around a {@link ExecutorService} which does {@link #execute()} a given {@link Runnable} only one time if it is multiple times invoked
 * 
 * @author omnaest
 */
public class OnlyOneTimeExecutor
{
    private AtomicInteger   barrierCounter = new AtomicInteger();
    private Lock            lock           = new ReentrantLock();
    private ExecutorService executorService;
    private Runnable        runnable;

    public OnlyOneTimeExecutor(ExecutorService executorService, Runnable runnable)
    {
        super();
        this.executorService = executorService;
        this.runnable = runnable;
    }

    public OnlyOneTimeExecutor execute()
    {
        if (this.barrierCounter.get() <= 0)
        {
            this.barrierCounter.incrementAndGet();
            this.lock.lock();
            try
            {
                this.barrierCounter.decrementAndGet();

                this.executorService.submit(this.runnable);
            }
            finally
            {
                this.lock.unlock();
            }
        }

        return this;
    }

    public OnlyOneTimeExecutor shutdown()
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
