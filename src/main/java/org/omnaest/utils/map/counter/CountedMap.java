package org.omnaest.utils.map.counter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.omnaest.utils.functional.OptionalFunction;

/**
 * Defines a {@link Map} like interface which allows to count elements
 * 
 * @see CounterMap
 * @see LongCounterMap
 * @see IntegerCounterMap
 * @author omnaest
 * @param <K>
 */
public interface CountedMap<K, N extends Number> extends OptionalFunction<K, N>
{
    public Optional<N> get(K key);

    public Set<K> keySet();

    public long getAsLongOrDefault(K key, N defaultValue);

    public int getAsIntOrDefault(K key, N defaultValue);

    /**
     * Returns the count for the given key or 0, if the key is not present
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public default long getAsLong(K key)
    {
        return this.getAsLongOrDefault(key, (N) Long.valueOf(0));
    }

    /**
     * Returns the count for the given key or 0, if the key is not present
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public default int getAsInt(K key)
    {
        return this.getAsIntOrDefault(key, (N) Integer.valueOf(0));
    }
}