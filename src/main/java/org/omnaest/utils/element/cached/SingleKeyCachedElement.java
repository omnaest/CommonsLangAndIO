package org.omnaest.utils.element.cached;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SingleKeyCachedElement<K, E> implements BiFunction<K, Supplier<E>, Supplier<E>>
{
    private Map<K, CachedElement<E>> keyToElement = new ConcurrentHashMap<>();

    @Override
    public Supplier<E> apply(K key, Supplier<E> provider)
    {
        if (!this.keyToElement.containsKey(key))
        {
            this.keyToElement.clear();
        }
        return this.keyToElement.computeIfAbsent(key, k -> CachedElement.of(provider));
    }
}
