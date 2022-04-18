package org.omnaest.utils.map.counter;

import java.util.Map;

/**
 * Defines a {@link Map} like interface which allows to count elements
 * 
 * @see LongCounterMap
 * @see IntegerCounterMap
 * @author omnaest
 * @param <K>
 */
public interface CounterMap<K, N extends Number> extends CountedMap<K, N>
{
    public CounterMap<K, N> incrementByOne(K key);

    public CounterMap<K, N> incrementBy(K key, int delta);

    public CounterMap<K, N> decrementByOne(K key);

    public CounterMap<K, N> decrementBy(K key, int delta);

}