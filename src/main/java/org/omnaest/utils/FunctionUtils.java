package org.omnaest.utils;

import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.functional.UnaryBiFunction;

/**
 * Helper methods around {@link Function} logic
 * 
 * @author omnaest
 */
public class FunctionUtils
{
    /**
     * Returns a {@link Supplier} that throws the {@link RuntimeException} provided by the given {@link Supplier} if the {@link Supplier#get()} method is
     * called.
     * 
     * @param exceptionSupplier
     * @return
     */
    public static <R> UnaryBiFunction<R> toExceptionThrowingSupplier(Supplier<? extends RuntimeException> exceptionSupplier)
    {
        return (a, b) ->
        {
            throw exceptionSupplier.get();
        };
    }
}
