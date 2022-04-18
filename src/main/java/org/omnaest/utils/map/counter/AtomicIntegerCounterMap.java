package org.omnaest.utils.map.counter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @see CounterMap
 * @author omnaest
 * @param <K>
 */
public class AtomicIntegerCounterMap<K> extends AbstractCounterMap<K, Integer> implements IntegerCounterMap<K>
{
    private Map<K, AtomicInteger> keyToCounter = new ConcurrentHashMap<>();
    private int                   initialValue = 0;

    public int getInitialValue()
    {
        return this.initialValue;
    }

    public AtomicIntegerCounterMap<K> setInitialValue(int initialValue)
    {
        this.initialValue = initialValue;
        return this;
    }

    @Override
    public int incrementByOneAndGet(K key)
    {
        return this.incrementByAndGet(key, 1);
    }

    @Override
    public AtomicIntegerCounterMap<K> incrementBy(K key, int delta)
    {
        this.incrementByAndGet(key, delta);
        return this;
    }

    @Override
    public AtomicIntegerCounterMap<K> incrementByOne(K key)
    {
        super.incrementByOne(key);
        return this;
    }

    @Override
    public AtomicIntegerCounterMap<K> decrementByOne(K key)
    {
        super.decrementByOne(key);
        return this;
    }

    @Override
    public AtomicIntegerCounterMap<K> decrementBy(K key, int delta)
    {
        super.decrementBy(key, delta);
        return this;
    }

    @Override
    public int incrementByAndGet(K key, int delta)
    {
        return this.keyToCounter.computeIfAbsent(key, k -> new AtomicInteger(this.initialValue))
                                .addAndGet(delta);
    }

    @Override
    public Optional<Integer> get(K key)
    {
        return Optional.ofNullable(this.keyToCounter.get(key))
                       .map(AtomicInteger::get);
    }

    @Override
    public Set<K> keySet()
    {
        return this.keyToCounter.keySet();
    }

    @Override
    public int getMaxCount()
    {
        return this.keyToCounter.values()
                                .stream()
                                .mapToInt(AtomicInteger::get)
                                .max()
                                .orElse(0);
    }

    @Override
    public Stream<CountedKey<K>> entries()
    {
        return this.keyToCounter.entrySet()
                                .stream()
                                .map(entry -> new CountedKey<K>()
                                {
                                    @Override
                                    public K getKey()
                                    {
                                        return entry.getKey();
                                    }

                                    @Override
                                    public int getCount()
                                    {
                                        return entry.getValue()
                                                    .get();
                                    }
                                });
    }

    @Override
    public AtomicIntegerCounterMap<K> clone()
    {
        AtomicIntegerCounterMap<K> result = new AtomicIntegerCounterMap<>();
        this.entries()
            .forEach(entry -> result.incrementBy(entry.getKey(), entry.getCount()));
        return result;
    }

}