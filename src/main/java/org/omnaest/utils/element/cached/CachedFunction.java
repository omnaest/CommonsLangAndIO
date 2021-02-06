package org.omnaest.utils.element.cached;

import java.util.Map;
import java.util.function.Function;

/**
 * {@link Function} wrapper which is cached by a {@link Map}
 * 
 * @author omnaest
 * @param <T>
 * @param <R>
 */
public interface CachedFunction<T, R> extends Function<T, R>
{
    /**
     * Creates a new {@link CachedFunction} based on a given {@link Function} and a {@link Map}
     * 
     * @param function
     * @param map
     * @return
     */
    public static <T, R> CachedFunction<T, R> of(Function<T, R> function, Map<T, R> map)
    {
        return new MapCachedFunction<>(map, function);
    }
}
