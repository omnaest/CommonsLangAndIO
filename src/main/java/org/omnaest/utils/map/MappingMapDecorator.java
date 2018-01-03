package org.omnaest.utils.map;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Similar to {@link MapDecorator} but allows to define mapper {@link Function}s for keys and values
 * 
 * @author omnaest
 * @param <K>
 * @param <KS>
 * @param <V>
 * @param <VS>
 */
public class MappingMapDecorator<K, KS, V, VS> implements Map<K, V>
{
    protected Supplier<Map<KS, VS>> map;
    protected Function<KS, K>       keyFromSourceMapper;
    protected Function<K, KS>       keyToReadableSourceMapper;
    protected Function<K, KS>       keyToWritableSourceMapper;
    protected Function<VS, V>       valueFromSourceMapper;
    protected Function<V, VS>       valueToReadableSourceMapper;
    protected Function<V, VS>       valueToWritableSourceMapper;

    public MappingMapDecorator(Supplier<Map<KS, VS>> sourceMap, Function<KS, K> keyFromSourceMapper, Function<K, KS> keyToReadableSourceMapper,
                               Function<VS, V> valueFromSourceMapper, Function<V, VS> valueToReadableSourceMapper, Function<K, KS> keyToWritableSourceMapper,
                               Function<V, VS> valueToWritableSourceMapper)
    {
        super();
        this.map = sourceMap;
        this.keyFromSourceMapper = keyFromSourceMapper;
        this.keyToReadableSourceMapper = keyToReadableSourceMapper;
        this.keyToWritableSourceMapper = keyToWritableSourceMapper;
        this.valueFromSourceMapper = valueFromSourceMapper;
        this.valueToReadableSourceMapper = valueToReadableSourceMapper;
        this.valueToWritableSourceMapper = valueToWritableSourceMapper;
    }

    public MappingMapDecorator(Map<KS, VS> sourceMap, Function<KS, K> keyFromSourceMapper, Function<K, KS> keyToSourceMapper,
                               Function<VS, V> valueFromSourceMapper, Function<V, VS> valueToSourceMapper, Function<K, KS> keyToWritableSourceMapper,
                               Function<V, VS> valueToWritableSourceMapper)
    {
        this(() -> sourceMap, keyFromSourceMapper, keyToSourceMapper, valueFromSourceMapper, valueToSourceMapper, keyToWritableSourceMapper,
                valueToWritableSourceMapper);
    }

    @Override
    public int size()
    {
        return this.map.get()
                       .size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.map.get()
                       .isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key)
    {
        boolean retval = false;
        try
        {
            KS sourceKey = this.keyToReadableSourceMapper.apply((K) key);
            retval = this.map.get()
                             .containsKey(sourceKey);
        }
        catch (ClassCastException e)
        {
            //do nothing
        }
        return retval;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsValue(Object value)
    {
        boolean retval = false;
        try
        {
            VS sourceValue = this.valueToReadableSourceMapper.apply((V) value);
            retval = this.map.get()
                             .containsValue(sourceValue);
        }
        catch (ClassCastException e)
        {
            //do nothing
        }
        return retval;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key)
    {
        V retval = null;
        try
        {
            KS sourceKey = this.keyToReadableSourceMapper.apply((K) key);
            retval = this.valueFromSourceMapper.apply(this.map.get()
                                                              .get(sourceKey));
        }
        catch (ClassCastException e)
        {
            //do nothing
        }
        return retval;
    }

    @Override
    public V put(K key, V value)
    {
        V retval = null;
        try
        {
            KS sourceKey = this.keyToWritableSourceMapper.apply(key);
            VS sourceValue = this.valueToWritableSourceMapper.apply(value);
            retval = this.valueFromSourceMapper.apply(this.map.get()
                                                              .put(sourceKey, sourceValue));
        }
        catch (ClassCastException e)
        {
            //do nothing
        }
        return retval;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key)
    {
        V retval = null;
        try
        {
            KS sourceKey = this.keyToReadableSourceMapper.apply((K) key);
            retval = this.valueFromSourceMapper.apply(this.map.get()
                                                              .remove(sourceKey));
        }
        catch (ClassCastException e)
        {
            //do nothing
        }
        return retval;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        if (m != null)
        {
            m.forEach(this::put);
        }
    }

    @Override
    public void clear()
    {
        this.map.get()
                .clear();
    }

    @Override
    public Set<K> keySet()
    {
        Set<KS> keySet = this.map.get()
                                 .keySet();
        return Collections.unmodifiableSet(keySet.stream()
                                                 .map(this.keyFromSourceMapper)
                                                 .collect(Collectors.toSet()));
    }

    @Override
    public Collection<V> values()
    {
        return Collections.unmodifiableList(this.map.get()
                                                    .values()
                                                    .stream()
                                                    .map(this.valueFromSourceMapper)
                                                    .collect(Collectors.toList()));
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return Collections.unmodifiableSet(this.map.get()
                                                   .entrySet()
                                                   .stream()
                                                   .map(entry -> new Map.Entry<K, V>()
                                                   {
                                                       @Override
                                                       public K getKey()
                                                       {
                                                           return MappingMapDecorator.this.keyFromSourceMapper.apply(entry.getKey());
                                                       }

                                                       @Override
                                                       public V getValue()
                                                       {
                                                           return MappingMapDecorator.this.valueFromSourceMapper.apply(entry.getValue());
                                                       }

                                                       @Override
                                                       public V setValue(V value)
                                                       {
                                                           VS retval = entry.setValue(MappingMapDecorator.this.valueToWritableSourceMapper.apply(value));
                                                           return MappingMapDecorator.this.valueFromSourceMapper.apply(retval);
                                                       }
                                                   })
                                                   .collect(Collectors.toSet()));
    }

}
