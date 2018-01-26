package org.omnaest.utils;

import java.util.Map;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

/**
 * Utils around {@link Stream#map(java.util.function.Function)}
 * 
 * @author omnaest
 */
public class MapperUtils
{

    public static <K1, V, K2> Function<Map.Entry<K1, V>, Map.Entry<K2, V>> mapEntryKey(Function<K1, K2> keyMapper)
    {
        return mapEntry(keyMapper, v -> v);
    }

    public static <K, V1, V2> Function<Map.Entry<K, V1>, Map.Entry<K, V2>> mapEntryValue(Function<V1, V2> valueMapper)
    {
        return mapEntry(k -> k, valueMapper);
    }

    public static <K1, V1, K2, V2> Function<Map.Entry<K1, V1>, Map.Entry<K2, V2>> mapEntry(Function<K1, K2> keyMapper, Function<V1, V2> valueMapper)
    {
        return entry -> new Map.Entry<K2, V2>()
        {
            @Override
            public K2 getKey()
            {
                return keyMapper.apply(entry.getKey());
            }

            @Override
            public V2 getValue()
            {
                return valueMapper.apply(entry.getValue());
            }

            @Override
            public V2 setValue(V2 value)
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns an {@link UnaryOperator} which maps to the identity
     * 
     * @return
     */
    public static <E> UnaryOperator<E> identity()
    {
        return i -> i;
    }

    /**
     * Returns an {@link LongFunction} which maps to the identity
     * 
     * @return
     */
    public static LongFunction<Long> identityForLongAsBoxed()
    {
        return i -> i;
    }

    /**
     * Returns an {@link ToLongFunction} which maps to the identity
     * 
     * @return
     */
    public static ToLongFunction<Long> identitiyForLongAsUnboxed()
    {
        return i -> i;
    }

    /**
     * Returns an {@link ToIntFunction} which maps to the identity
     * 
     * @return
     */
    public static ToIntFunction<Integer> identitiyForIntegerAsUnboxed()
    {
        return i -> i;
    }

    /**
     * Returns a {@link Function} which maps a {@link Map.Entry} to a {@link BiElement}
     * 
     * @return
     */
    public static <K, V> Function<Map.Entry<K, V>, BiElement<K, V>> mapEntryToBiElement()
    {
        return entry -> BiElement.of(entry.getKey(), entry.getValue());
    }
}
