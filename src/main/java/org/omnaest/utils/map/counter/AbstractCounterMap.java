package org.omnaest.utils.map.counter;

public abstract class AbstractCounterMap<K, N extends Number> implements CounterMap<K, N>
{
    public AbstractCounterMap()
    {
        super();
    }

    @Override
    public CounterMap<K, N> incrementByOne(K key)
    {
        return this.incrementBy(key, 1);
    }

    @Override
    public CounterMap<K, N> decrementByOne(K key)
    {
        return this.decrementBy(key, 1);
    }

    @Override
    public CounterMap<K, N> decrementBy(K key, int delta)
    {
        return this.incrementBy(key, -delta);
    }

    @Override
    public long getAsLongOrDefault(K key, N defaultValue)
    {
        return this.get(key)
                   .map(N::longValue)
                   .orElse(defaultValue.longValue());
    }

    @Override
    public int getAsIntOrDefault(K key, N defaultValue)
    {
        return this.get(key)
                   .map(N::intValue)
                   .orElse(defaultValue.intValue());
    }

}