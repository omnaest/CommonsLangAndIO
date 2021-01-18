package org.omnaest.utils.counter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Wrapper around a {@link AtomicLong} with additional helper methods
 * 
 * @author omnaest
 */
public class Counter implements Supplier<Long>
{
    private AtomicLong counter;

    private Counter(long start)
    {
        this.counter = new AtomicLong(start);
    }

    public static Counter fromZero()
    {
        return new Counter(0l);
    }

    public static Counter from(int start)
    {
        return new Counter(start);
    }

    public static Counter from(long start)
    {
        return new Counter(start);
    }

    public Counter ifModulo(int modulo, Consumer<Long> counterConsumer)
    {
        long count = this.counter.get();
        if (counterConsumer != null && count % modulo == 0)
        {
            counterConsumer.accept(count);
        }
        return this;
    }

    public Counter increment()
    {
        return this.incrementBy(1);
    }

    public Counter incrementBy(int delta)
    {
        this.counter.addAndGet(delta);
        return this;
    }

    @Override
    public Long get()
    {
        return this.counter.get();
    }

    public Counter synchronizeWith(Counter sourceCounter)
    {
        this.counter.set(sourceCounter.get());
        return this;
    }

    /**
     * Returns the delta = other counter - current counter
     * 
     * @param otherCounter
     * @return
     */
    public long deltaTo(Counter otherCounter)
    {
        return otherCounter.get() - this.counter.get();
    }

}
