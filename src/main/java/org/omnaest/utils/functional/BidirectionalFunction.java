package org.omnaest.utils.functional;

import java.util.function.Function;

/**
 * Defines two {@link Function}s in contrary mapping directions
 * 
 * @author omnaest
 * @param <S>
 * @param <T>
 */
public interface BidirectionalFunction<S, T>
{
    /**
     * Returns the {@link Function} of the forward direction
     * 
     * @return
     */
    public Function<S, T> forward();

    /**
     * Returns the {@link Function} of the backward direction
     * 
     * @return
     */
    public Function<T, S> backward();

    /**
     * Returns a {@link BidirectionalFunction} consisting of the two given {@link Function}s
     * 
     * @param forward
     * @param backward
     * @return
     */
    public static <S, T> BidirectionalFunction<S, T> of(Function<S, T> forward, Function<T, S> backward)
    {
        return new CompositeBidirectionalFunction<>(forward, backward);
    }

    public static <U> BidirectionalFunction<U, U> identity()
    {
        return new BidirectionalIdentityFunction<U>();
    }
}
