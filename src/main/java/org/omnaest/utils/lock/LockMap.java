package org.omnaest.utils.lock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.omnaest.utils.ListUtils;

/**
 * A {@link LockMap} contains an internal {@link Map} of keys to {@link Lock} objects and allows to synchronize {@link Thread}s based on a given key.
 * 
 * @see #run(Object, Runnable)
 * @author omnaest
 */
public class LockMap
{
    private static final Object     DEFAULT_KEY = LockMap.class;
    private Map<Object, LockHolder> locks       = new ConcurrentHashMap<>();

    public LockMap run(Object key, Runnable runnable)
    {
        return this.run(Arrays.asList(key), runnable);
    }

    public <R> R call(Object key, Callable<R> callable) throws Exception
    {
        return this.call(Arrays.asList(key), callable);
    }

    /**
     * Executes the given {@link Runnable} and blocks all available keys.
     * 
     * @param runnable
     * @return
     */
    public LockMap run(Runnable runnable)
    {
        return this.run(ListUtils.addToNew(this.locks.keySet(), DEFAULT_KEY), runnable);
    }

    public <R> R call(Callable<R> callable) throws Exception
    {
        return this.call(ListUtils.addToNew(this.locks.keySet(), DEFAULT_KEY), callable);
    }

    public LockMap run(Collection<Object> keys, Runnable runnable)
    {
        try
        {
            this.call(keys, () ->
            {
                runnable.run();
                return null;
            });
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    public <R> R call(Collection<Object> keys, Callable<R> callable) throws Exception
    {
        List<LockHolder> lockHolders = Optional.ofNullable(keys)
                                               .orElse(Collections.emptyList())
                                               .stream()
                                               .map(key ->

                                               this.locks.compute(key, (k, l) -> Optional.ofNullable(l)
                                                                                         .orElseGet(() -> new LockHolder())
                                                                                         .incrementThreadCounter()
                                                                                         .lock()))
                                               .collect(Collectors.toList());
        try
        {
            return callable.call();
        }
        finally
        {
            lockHolders.forEach(lockHolder -> lockHolder.unlock()
                                                        .decrementThreadCounter());
            Optional.ofNullable(keys)
                    .orElse(Collections.emptyList())
                    .forEach(key -> this.locks.computeIfPresent(key, (k, l) -> l.hasNoActiveThreads() ? null : l));
        }
    }

    private static class LockHolder
    {
        private Lock          lock                = new ReentrantLock(true);
        private AtomicInteger activeThreadCounter = new AtomicInteger();

        public LockHolder unlock()
        {
            this.lock.unlock();
            return this;
        }

        public LockHolder lock()
        {
            this.lock.lock();
            return this;
        }

        public LockHolder incrementThreadCounter()
        {
            this.activeThreadCounter.incrementAndGet();
            return this;
        }

        public LockHolder decrementThreadCounter()
        {
            this.activeThreadCounter.decrementAndGet();
            return this;
        }

        public boolean hasNoActiveThreads()
        {
            return this.activeThreadCounter.get() == 0;
        }
    }

}
