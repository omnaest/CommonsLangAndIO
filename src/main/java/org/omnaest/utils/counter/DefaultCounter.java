package org.omnaest.utils.counter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * @see Counter
 * @author omnaest
 */
public class DefaultCounter implements Counter
{
    private AtomicLong counter;

    protected DefaultCounter(long start)
    {
        this.counter = new AtomicLong(start);
    }

    @Override
    public Counter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        long count = this.counter.get();
        if (counterConsumer != null && count % modulo == 0)
        {
            counterConsumer.accept(count);
        }
        return this;
    }

    @Override
    public Counter increment()
    {
        return this.incrementBy(1);
    }

    @Override
    public Counter incrementBy(int delta)
    {
        this.counter.addAndGet(delta);
        return this;
    }

    @Override
    public long getAsLong()
    {
        return this.counter.get();
    }

    @Override
    public Counter synchronizeWith(Counter sourceCounter)
    {
        this.counter.set(sourceCounter.getAsLong());
        return this;
    }

    /**
     * Returns the delta = other counter - current counter
     * 
     * @param otherCounter
     * @return
     */
    @Override
    public long deltaTo(Counter otherCounter)
    {
        return otherCounter.getAsLong() - this.counter.get();
    }

    @Override
    public ProgressCounter asProgressCounter()
    {
        return DefaultProgressCounter.of(this);
    }

    @Override
    public DurationProgressCounter asDurationProgressCounter()
    {
        return this.asProgressCounter()
                   .asDurationProgressCounter();
    }

}
