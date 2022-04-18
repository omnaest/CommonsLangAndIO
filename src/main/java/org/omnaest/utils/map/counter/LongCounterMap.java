package org.omnaest.utils.map.counter;

import org.omnaest.utils.functional.OptionalFunction;

/**
 * A {@link CounterMap} with {@link Long} support
 * 
 * @author omnaest
 * @param <K>
 */
public interface LongCounterMap<K> extends LongCountedMap<K>, CounterMap<K, Long>, OptionalFunction<K, Long>
{

    public long incrementByOneAndGet(K key);

    public long incrementByAndGet(K key, long delta);

    @Override
    public LongCounterMap<K> incrementByOne(K key);

    @Override
    public LongCounterMap<K> incrementBy(K key, int delta);

    @Override
    public LongCounterMap<K> decrementByOne(K key);

    @Override
    public LongCounterMap<K> decrementBy(K key, int delta);

    public default LongCountedMap<K> asImmutable()
    {
        return this;
    }

    public static <K> LongCounterMap<K> newInstance()
    {
        return new AtomicLongCounterMap<K>();
    }
}
