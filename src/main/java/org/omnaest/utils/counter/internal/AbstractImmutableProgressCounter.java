package org.omnaest.utils.counter.internal;

import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.ImmutableCounter;
import org.omnaest.utils.counter.ImmutableProgressCounter;

/**
 * @see ImmutableProgressCounter
 * @author omnaest
 */
public abstract class AbstractImmutableProgressCounter implements ImmutableProgressCounter
{
    private ImmutableCounter counter;

    public AbstractImmutableProgressCounter(ImmutableCounter counter)
    {
        super();
        this.counter = counter;
    }

    @Override
    public double getProgress()
    {
        long maximum = this.getMaximum();
        if (Long.MAX_VALUE == maximum || maximum <= 0l)
        {
            return 1.0 - 1.0 / (1.0 + Math.log10(1.0 + this.getAsLong()));
        }
        else
        {
            return this.getAsLong() / Math.max(1.0, maximum);
        }
    }

    @Override
    public ImmutableProgressCounter doWithProgress(DoubleConsumer progressConsumer)
    {
        Optional.ofNullable(progressConsumer)
                .ifPresent(consumer -> consumer.accept(this.getProgress()));
        return this;
    }

    @Override
    public long getMaximum()
    {
        return this.getMaximumProvider()
                   .getAsLong();
    }

    protected abstract LongSupplier getMaximumProvider();

    @Override
    public String getProgressAsString()
    {
        return formatProgress(this.getProgress());
    }

    private static String formatProgress(double progress)
    {
        return Math.round(progress * 100) + "%";
    }

    @Override
    public ImmutableProgressCounter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        this.counter.ifModulo(modulo, counterConsumer);
        return this;
    }

    @Override
    public ImmutableProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer)
    {
        this.counter.ifModulo(modulo, counter ->
        {
            double progress = this.getProgress();

            progressConsumer.accept(new Progress()
            {
                @Override
                public long getCounter()
                {
                    return counter;
                }

                @Override
                public double getProgress()
                {
                    return progress;
                }

                @Override
                public String getProgressAsString()
                {
                    return formatProgress(progress);
                }

            });
        });
        return this;
    }

    @Override
    public long deltaTo(Counter otherCounter)
    {
        return this.counter.deltaTo(otherCounter);
    }

    @Override
    public long getAsLong()
    {
        this.runSynchronizePullOperations();
        return this.counter.getAsLong();
    }

    protected abstract void runSynchronizePullOperations();

}