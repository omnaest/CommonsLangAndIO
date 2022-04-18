package org.omnaest.utils.map.counter;

import java.util.Optional;

public interface LongCountedMap<K> extends CountedMap<K, Long>
{
    @Override
    public Optional<Long> get(K key);

    @Override
    public default Optional<Long> apply(K key)
    {
        return this.get(key);
    }
}
