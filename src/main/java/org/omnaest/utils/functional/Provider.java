package org.omnaest.utils.functional;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Similar to {@link Supplier}
 * 
 * @author omnaest
 * @param <E>
 */
public interface Provider<E> extends Supplier<E>
{

    /**
     * Allows to chain a mapper {@link Function} to the current provider
     * 
     * @param mapper
     * @return
     */
    public default <R> Provider<R> and(Function<E, R> mapper)
    {
        return () -> mapper.apply(get());
    }
}
