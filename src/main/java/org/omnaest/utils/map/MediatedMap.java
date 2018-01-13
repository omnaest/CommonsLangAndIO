package org.omnaest.utils.map;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.omnaest.utils.functional.BidirectionalFunction;

/**
 * A {@link Map} which is based on a given {@link Map} with differen key and value type. The {@link MediatedMap} applies two
 * {@link BidirectionalFunction}s on the accessor method, so that the underlying {@link Map} can operate on the different types as the
 * {@link MediatedMap} itself.
 * 
 * @author omnaest
 * @param <K>
 * @param <V>
 * @param <KS>
 * @param <VS>
 */
public class MediatedMap<K, V, KS, VS> implements Map<K, V>
{
    private Map<KS, VS>                  map;
    private BidirectionalFunction<K, KS> keyWriteMapper;
    private BidirectionalFunction<K, KS> keyReadMapper;
    private BidirectionalFunction<V, VS> valueWriteMapper;
    private BidirectionalFunction<V, VS> valueReadMapper;

    public MediatedMap(Map<KS, VS> map, BidirectionalFunction<K, KS> keyMapper, BidirectionalFunction<V, VS> valueMapper)
    {
        this(map, keyMapper, keyMapper, valueMapper, valueMapper);
    }

    public MediatedMap(Map<KS, VS> map, BidirectionalFunction<K, KS> keyWriteMapper, BidirectionalFunction<K, KS> keyReadMapper,
                       BidirectionalFunction<V, VS> valueWriteMapper, BidirectionalFunction<V, VS> valueReadMapper)
    {
        super();
        this.map = map;
        this.keyWriteMapper = keyWriteMapper;
        this.keyReadMapper = keyReadMapper;
        this.valueWriteMapper = valueWriteMapper;
        this.valueReadMapper = valueReadMapper;
    }

    @Override
    public int size()
    {
        return this.map.size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public boolean containsKey(Object key)
    {
        boolean retval = false;

        try
        {
            retval = this.map.containsKey(this.keyReadMapper.forward()
                                                            .apply((K) key));
        }
        catch (ClassCastException e)
        {
            //ignore
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
            retval = this.map.containsValue(this.valueReadMapper.forward()
                                                                .apply((V) value));
        }
        catch (ClassCastException e)
        {
            //ignore
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
            retval = this.valueReadMapper.backward()
                                         .apply(this.map.get(this.keyReadMapper.forward()
                                                                               .apply((K) key)));
        }
        catch (ClassCastException e)
        {
            //ignore
        }

        return retval;
    }

    @Override
    public V put(K key, V value)
    {
        V retval = null;

        KS sourceKey = this.keyWriteMapper.forward()
                                          .apply(key);
        VS sourceValue = this.valueWriteMapper.forward()
                                              .apply(value);
        VS removed = this.map.put(sourceKey, sourceValue);
        retval = this.valueReadMapper.backward()
                                     .apply(removed);

        return retval;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key)
    {
        V retval = null;

        try
        {
            retval = this.valueReadMapper.backward()
                                         .apply(this.map.remove(this.keyReadMapper.forward()
                                                                                  .apply((K) key)));
        }
        catch (ClassCastException e)
        {
            //ignore
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
        this.map.clear();
    }

    @Override
    public Set<K> keySet()
    {
        return Collections.unmodifiableSet(this.map.keySet()
                                                   .stream()
                                                   .map(this.keyReadMapper.backward()::apply)
                                                   .collect(Collectors.toSet()));
    }

    @Override
    public Collection<V> values()
    {
        return Collections.unmodifiableList(this.map.values()
                                                    .stream()
                                                    .map(this.valueReadMapper.backward()::apply)
                                                    .collect(Collectors.toList()));
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return Collections.unmodifiableSet(this.map.entrySet()
                                                   .stream()
                                                   .map(entry -> new Map.Entry<K, V>()
                                                   {
                                                       @Override
                                                       public K getKey()
                                                       {
                                                           return MediatedMap.this.keyReadMapper.backward()
                                                                                                .apply(entry.getKey());
                                                       }

                                                       @Override
                                                       public V getValue()
                                                       {
                                                           return MediatedMap.this.valueReadMapper.backward()
                                                                                                  .apply(entry.getValue());
                                                       }

                                                       @Override
                                                       public V setValue(V value)
                                                       {
                                                           return MediatedMap.this.valueReadMapper.backward()
                                                                                                  .apply(entry.setValue(MediatedMap.this.valueWriteMapper.forward()
                                                                                                                                                         .apply(value)));
                                                       }
                                                   })
                                                   .collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object o)
    {
        return this.map.equals(o);
    }

    @Override
    public int hashCode()
    {
        return this.map.hashCode();
    }

    @Override
    public String toString()
    {
        return "MediatedMap [map=" + this.map + ", keyWriteMapper=" + this.keyWriteMapper + ", keyReadMapper=" + this.keyReadMapper + ", valueWriteMapper="
                + this.valueWriteMapper + ", valueReadMapper=" + this.valueReadMapper + "]";
    }

}
