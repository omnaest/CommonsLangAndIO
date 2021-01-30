package org.omnaest.utils.counter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/**
 * Represents an {@link AtomicLong} counter with additional methods for counting support
 * 
 * @see #from(int)
 * @see #from(long)
 * @see #fromZero()
 * @see #asProgressCounter()
 * @see #asDurationProgressCounter()
 * @author omnaest
 */
public interface Counter extends LongSupplier
{
    public long deltaTo(Counter otherCounter);

    public Counter synchronizeWith(Counter sourceCounter);

    public Counter incrementBy(int delta);

    public Counter increment();

    public Counter ifModulo(int modulo, LongConsumer counterConsumer);

    /**
     * @see ProgressCounter
     * @return
     */
    public ProgressCounter asProgressCounter();

    /**
     * @see DurationProgressCounter
     * @return
     */
    public DurationProgressCounter asDurationProgressCounter();

    public static Counter fromZero()
    {
        return new DefaultCounter(0l);
    }

    public static Counter from(int start)
    {
        return new DefaultCounter(start);
    }

    public static Counter from(long start)
    {
        return new DefaultCounter(start);
    }
}
