package org.omnaest.utils.functional;

import java.util.function.BiFunction;

/**
 * @see BiFunction
 * @author omnaest
 * @param <V1>
 * @param <V2>
 * @param <V3>
 * @param <R>
 */
@FunctionalInterface
public interface TriFunction<V1, V2, V3, R>
{
    public R apply(V1 value1, V2 value2, V3 value3);
}
