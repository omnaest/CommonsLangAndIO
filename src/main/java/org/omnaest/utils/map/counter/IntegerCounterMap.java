package org.omnaest.utils.map.counter;

import org.omnaest.utils.functional.OptionalFunction;

/**
 * A {@link CounterMap} with {@link Long} support
 * 
 * @author omnaest
 * @param <K>
 */
public interface IntegerCounterMap<K> extends IntegerCountedMap<K>, CounterMap<K, Integer>, OptionalFunction<K, Integer>
{
    public int incrementByOneAndGet(K key);

    public int incrementByAndGet(K key, int delta);

    @Override
    public IntegerCounterMap<K> incrementByOne(K key);

    @Override
    public IntegerCounterMap<K> incrementBy(K key, int delta);

    @Override
    public IntegerCounterMap<K> decrementByOne(K key);

    @Override
    public IntegerCounterMap<K> decrementBy(K key, int delta);

    public IntegerCounterMap<K> clone();

    public default IntegerCountedMap<K> asImmutable()
    {
        return this;
    }

    public static <K> IntegerCounterMap<K> newInstance()
    {
        return new AtomicIntegerCounterMap<K>();
    }

}
