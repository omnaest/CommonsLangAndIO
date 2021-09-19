package org.omnaest.utils.counter;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/**
 * Immutable {@link Counter}
 * 
 * @author omnaest
 */
public interface ImmutableCounter extends LongSupplier
{
    public long deltaTo(Counter otherCounter);

    /**
     * Calls the given {@link LongConsumer} for all counted values that have counter % 'modulo' value == 0.<br>
     * <br>
     * Be aware that for this method to work the {@link #increment()} and not the {@link #incrementBy(int)} method should be used.
     * 
     * @param modulo
     * @param counterConsumer
     * @return
     */
    public ImmutableCounter ifModulo(int modulo, LongConsumer counterConsumer);
}
