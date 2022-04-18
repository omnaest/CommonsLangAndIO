package org.omnaest.utils.map.counter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @see CounterMap
 * @author omnaest
 * @param <K>
 */
public class AtomicLongCounterMap<K> extends AbstractCounterMap<K, Long> implements LongCounterMap<K>
{
    private Map<K, AtomicLong> keyToCounter = new ConcurrentHashMap<>();
    private long               initialValue = 0;

    public long getInitialValue()
    {
        return this.initialValue;
    }

    public AtomicLongCounterMap<K> setInitialValue(long initialValue)
    {
        this.initialValue = initialValue;
        return this;
    }

    @Override
    public long incrementByOneAndGet(K key)
    {
        return this.incrementByAndGet(key, 1);
    }

    @Override
    public LongCounterMap<K> incrementBy(K key, int delta)
    {
        this.keyToCounter.computeIfAbsent(key, k -> new AtomicLong(this.initialValue))
                         .addAndGet(delta);
        return this;
    }

    @Override
    public long incrementByAndGet(K key, long delta)
    {
        return this.keyToCounter.computeIfAbsent(key, k -> new AtomicLong(this.initialValue))
                                .incrementAndGet();
    }

    @Override
    public AtomicLongCounterMap<K> incrementByOne(K key)
    {
        super.incrementByOne(key);
        return this;
    }

    @Override
    public AtomicLongCounterMap<K> decrementByOne(K key)
    {
        super.decrementByOne(key);
        return this;
    }

    @Override
    public AtomicLongCounterMap<K> decrementBy(K key, int delta)
    {
        super.decrementBy(key, delta);
        return this;
    }

    @Override
    public Optional<Long> get(K key)
    {
        return Optional.ofNullable(this.keyToCounter.get(key))
                       .map(AtomicLong::get);
    }

    @Override
    public Set<K> keySet()
    {
        return this.keyToCounter.keySet();
    }

}