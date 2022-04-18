package org.omnaest.utils.map.counter;

import java.util.Optional;
import java.util.stream.Stream;

public interface IntegerCountedMap<K> extends CountedMap<K, Integer>
{
    @Override
    public Optional<Integer> get(K key);

    @Override
    public default Optional<Integer> apply(K key)
    {
        return this.get(key);
    }

    public int getMaxCount();

    public Stream<CountedKey<K>> entries();

    public static interface CountedKey<K>
    {
        public K getKey();

        public int getCount();
    }

    public static <K> IntegerCountedMap<K> empty()
    {
        return IntegerCounterMap.<K>newInstance()
                                .asImmutable();
    }
}
