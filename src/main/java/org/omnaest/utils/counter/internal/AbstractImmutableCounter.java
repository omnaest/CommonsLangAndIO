package org.omnaest.utils.counter.internal;

import java.util.function.LongConsumer;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.ImmutableCounter;

public abstract class AbstractImmutableCounter implements ImmutableCounter
{
    public AbstractImmutableCounter()
    {
        super();
    }

    @Override
    public abstract long getAsLong();

    @Override
    public ImmutableCounter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        long count = this.getAsLong();
        if (counterConsumer != null && count % modulo == 0)
        {
            counterConsumer.accept(count);
        }
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
        return otherCounter.getAsLong() - this.getAsLong();
    }

}