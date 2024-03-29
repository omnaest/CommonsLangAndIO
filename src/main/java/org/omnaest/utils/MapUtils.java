/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.SetUtils.SetDelta;
import org.omnaest.utils.element.lar.ModifiableUnaryLeftAndRight;
import org.omnaest.utils.element.lar.UnaryLeftAndRight;
import org.omnaest.utils.functional.BidirectionalFunction;
import org.omnaest.utils.map.AbstractSupplierMap;
import org.omnaest.utils.map.AbstractSupplierMap.KeySupplier;
import org.omnaest.utils.map.CRUDMap;
import org.omnaest.utils.map.MediatedMap;
import org.omnaest.utils.map.SupplierMap;

public class MapUtils
{

    public static interface MapAction<K, V>
    {
        MapAction<K, V> put(K key, V value);
    }

    public static <K, V> MapAction<K, V> withMap(Map<K, V> map)
    {
        return new MapAction<K, V>()
        {
            @Override
            public MapAction<K, V> put(K key, V value)
            {
                map.put(key, value);
                return this;
            }
        };
    }

    public static <K, V> Map<V, List<K>> invertMultiValue(Map<K, ? extends Collection<V>> map)
    {
        Map<V, List<K>> retmap = new LinkedHashMap<>();

        map.entrySet()
           .forEach(entry ->
           {
               entry.getValue()
                    .forEach(value ->
                    {
                        retmap.computeIfAbsent(value, v -> new ArrayList<>())
                              .add(entry.getKey());
                    });
           });

        return retmap;
    }

    public static <K, V> Map<V, List<K>> invert(Map<K, V> map)
    {
        Map<V, List<K>> retmap = new LinkedHashMap<>();

        map.entrySet()
           .forEach(entry ->
           {
               retmap.computeIfAbsent(entry.getValue(), v -> new ArrayList<>())
                     .add(entry.getKey());
           });

        return retmap;
    }

    /**
     * Inverts the key and value column and assumes that the values are unique in the source map.
     * 
     * @param map
     * @return
     */
    public static <K, V> Map<V, K> invertUnique(Map<K, V> map)
    {
        return invert(map).entrySet()
                          .stream()
                          .collect(Collectors.toMap(e -> e.getKey(), e -> ListUtils.first(e.getValue())));
    }

    public static interface MapBuilder<K, V>
    {
        public <K2, V2> MapBuilder<K2, V2> put(K2 key, V2 value);

        public <K2, V2> MapBuilder<K2, V2> putAll(Map<K2, V2> map);

        /**
         * Merges two given {@link List}s into the {@link Map} where one {@link List} is the source of the keys and one {@link List} the source of the values,
         * which are both joined in the order given in the {@link List}s
         * 
         * @param keys
         * @param values
         * @return
         */
        public <K2, V2> MapBuilder<K2, V2> putAll(List<K2> keys, List<V2> values);

        /**
         * Merges two given {@link Stream}s into the {@link Map} where the key {@link Stream} and value {@link Stream} elements are joined in their incoming
         * order.
         * 
         * @param keys
         * @param values
         * @return
         */
        public <K2, V2> MapBuilder<K2, V2> putAll(Stream<K2> keys, Stream<V2> values);

        /**
         * Defines the {@link Map} factory used to build the {@link Map}
         * 
         * @see #useMap(Map)
         * @param mapFactory
         * @return
         */
        public <K2, V2> MapBuilder<K2, V2> useFactory(Supplier<? extends Map<K2, V2>> mapFactory);

        /**
         * Similar to {@link #useFactory(Supplier)} but for {@link SortedMap}s
         * 
         * @param mapFactory
         * @return
         */
        public <K2, V2> SortedMapBuilder<K2, V2> useFactoryOfSortedMap(Supplier<SortedMap<K2, V2>> mapFactory);

        /**
         * Uses the given {@link Map} instance to build the result
         * 
         * @see #useFactory(Supplier)
         * @param map
         * @return
         */
        public <K2, V2> MapBuilder<K2, V2> useMap(Map<K2, V2> map);

        /**
         * Similar to {@link #useMap(Map)} but for {@link SortedMap}s
         * 
         * @param map
         * @return
         */
        public <K2, V2> SortedMapBuilder<K2, V2> useMap(SortedMap<K2, V2> map);

        /**
         * Similar to {@link #useMap(SortedMap)} with a {@link TreeMap} instance
         * 
         * @return
         */
        public <K2, V2> SortedMapBuilder<K2, V2> useSortedMap();

        /**
         * Similar to {@link #useSortedMap()} with a given {@link Comparator}
         * 
         * @see ComparatorUtils#builder()
         * @param comparator
         * @return
         */
        public <K2, V2> SortedMapBuilder<K2, V2> useSortedMap(Comparator<K> comparator);

        /**
         * Builds the {@link Map}
         * 
         * @return
         */
        public <K2 extends K, V2 extends V> Map<K2, V2> build();

    }

    public static interface SortedMapBuilder<K, V> extends MapBuilder<K, V>
    {

        @Override
        public <K2 extends K, V2 extends V> SortedMap<K2, V2> build();
    }

    @SuppressWarnings("unchecked")
    private static class MapBuilderImpl implements MapBuilder<Object, Object>
    {
        private Map<Object, Object>           map        = new LinkedHashMap<>();
        private Supplier<Map<Object, Object>> mapFactory = null;

        @Override
        public <K2, V2> MapBuilder<K2, V2> put(K2 key, V2 value)
        {
            this.map.put(key, value);
            return (MapBuilder<K2, V2>) this;
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> putAll(Map<K2, V2> map)
        {
            if (map != null)
            {
                this.map.putAll(map);
            }
            return (MapBuilder<K2, V2>) this;
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> putAll(List<K2> keyList, List<V2> valueList)
        {
            return this.putAll(keyList.stream(), valueList.stream());
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> putAll(Stream<K2> keys, Stream<V2> values)
        {
            StreamUtils.merge(keys, values)
                       .forEach(lar -> this.put(lar.getLeft(), lar.getRight()));
            return (MapBuilder<K2, V2>) this;
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> useFactory(Supplier<? extends Map<K2, V2>> mapFactory)
        {
            this.mapFactory = () -> (Map<Object, Object>) mapFactory.get();
            return (MapBuilder<K2, V2>) this;
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useFactoryOfSortedMap(Supplier<SortedMap<K2, V2>> mapFactory)
        {
            this.useFactory(mapFactory);
            return new SortedMapBuilderImpl<K2, V2>((MapBuilder<K2, V2>) this);
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> useMap(Map<K2, V2> map)
        {
            return this.useFactory(() -> map);
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useMap(SortedMap<K2, V2> map)
        {
            return this.useFactoryOfSortedMap(() -> map);
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useSortedMap()
        {
            return this.useFactoryOfSortedMap(() -> new TreeMap<>());
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useSortedMap(Comparator<Object> comparator)
        {
            return this.useFactoryOfSortedMap(() -> new TreeMap<>(comparator));
        }

        @Override
        public <K2, V2> Map<K2, V2> build()
        {
            Map<K2, V2> retmap;

            if (this.mapFactory != null)
            {
                retmap = (Map<K2, V2>) this.mapFactory.get();
                retmap.putAll((Map<? extends K2, ? extends V2>) this.map);
            }
            else
            {
                retmap = (Map<K2, V2>) this.map;
            }

            return retmap;
        }
    }

    private static class SortedMapBuilderImpl<K, V> implements SortedMapBuilder<K, V>
    {
        private MapBuilder<K, V> builder;

        public SortedMapBuilderImpl(MapBuilder<K, V> builder)
        {
            super();
            this.builder = builder;
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> put(K2 key, V2 value)
        {
            return this.builder.put(key, value);
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> putAll(Map<K2, V2> map)
        {
            return this.builder.putAll(map);
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> putAll(List<K2> keys, List<V2> values)
        {
            return this.builder.putAll(keys, values);
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> putAll(Stream<K2> keys, Stream<V2> values)
        {
            return this.builder.putAll(keys, values);
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> useFactory(Supplier<? extends Map<K2, V2>> mapFactory)
        {
            return this.builder.useFactory(mapFactory);
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useFactoryOfSortedMap(Supplier<SortedMap<K2, V2>> mapFactory)
        {
            return this.builder.useFactoryOfSortedMap(mapFactory);
        }

        @Override
        public <K2, V2> MapBuilder<K2, V2> useMap(Map<K2, V2> map)
        {
            return this.builder.useMap(map);
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useMap(SortedMap<K2, V2> map)
        {
            return this.builder.useMap(map);
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useSortedMap()
        {
            return this.builder.useSortedMap();
        }

        @Override
        public <K2, V2> SortedMapBuilder<K2, V2> useSortedMap(Comparator<K> comparator)
        {
            return this.builder.useSortedMap(comparator);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <K2 extends K, V2 extends V> SortedMap<K2, V2> build()
        {
            SortedMap<K2, V2> map = (SortedMap<K2, V2>) this.builder.build();
            return map;
        }

    }

    @SuppressWarnings("unchecked")
    public static <K, V> MapBuilder<K, V> builder()
    {
        return (MapBuilder<K, V>) new MapBuilderImpl();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, UnaryLeftAndRight<V>> join(Map<K, V> leftMap, Map<K, V> rightMap)
    {
        Map<K, ModifiableUnaryLeftAndRight<V>> retmap = new LinkedHashMap<>();

        if (leftMap != null)
        {
            for (K key : leftMap.keySet())
            {
                V value = leftMap.get(key);
                retmap.computeIfAbsent(key, k -> new ModifiableUnaryLeftAndRight<>())
                      .setLeft(value);
            }
        }
        if (rightMap != null)
        {
            for (K key : rightMap.keySet())
            {
                V value = rightMap.get(key);
                retmap.computeIfAbsent(key, k -> new ModifiableUnaryLeftAndRight<>())
                      .setRight(value);
            }
        }

        return (Map<K, UnaryLeftAndRight<V>>) ((Map<K, ? extends UnaryLeftAndRight<V>>) retmap);
    }

    @SafeVarargs
    public static <K, V> Map<K, List<V>> join(Map<K, V>... maps)
    {
        return join(Arrays.asList(maps));
    }

    /**
     * Joins multiple {@link Map}s with the same value and key type into a single one
     * 
     * @param maps
     * @return never null
     */
    public static <K, V> Map<K, List<V>> join(Collection<Map<K, V>> maps)
    {
        Map<K, List<V>> retmap = Collections.emptyMap();

        if (maps != null)
        {
            retmap = maps.stream()
                         .flatMap(map -> map.keySet()
                                            .stream())
                         .filter(PredicateUtils.notNull())
                         .distinct()
                         .collect(Collectors.toMap(key -> key, key -> maps.stream()
                                                                          .map(map -> map.getOrDefault(key, null))
                                                                          .collect(Collectors.toList())));
        }

        return retmap;
    }

    public static <K, V> CRUDMap<K, V> toCRUDMap(Map<K, V> map)
    {
        return CRUDMap.of(map);
    }

    public static <K, V> Map<K, V> toMap(CRUDMap<K, V> crudMap)
    {
        return crudMap.toMap();
    }

    public static <K, V> Map<K, V> toMap(Collection<K> keys)
    {
        return toMap(keys, null);
    }

    public static <K, V> Map<K, V> toMap(Collection<K> keys, V nullValue)
    {
        Map<K, V> map = new LinkedHashMap<>();
        Optional.ofNullable(keys)
                .orElse(Collections.emptyList())
                .forEach(key -> map.put(key, nullValue));
        return map;
    }

    /**
     * Returns a {@link MediatedMap}
     * 
     * @see BidirectionalFunction
     * @param map
     * @param keyMapper
     * @param valueMapper
     * @return
     */
    public static <K, V, KS, VS> Map<K, V> toMediatedMap(Map<KS, VS> map, BidirectionalFunction<K, KS> keyMapper, BidirectionalFunction<V, VS> valueMapper)
    {
        return new MediatedMap<>(map, keyMapper, valueMapper);
    }

    /**
     * Returns a {@link MediatedMap}
     * 
     * @see BidirectionalFunction
     * @param map
     * @param keyWriteMapper
     * @param keyReadMapper
     * @param valueWriteMapper
     * @param valueReadMapper
     * @return
     */
    public static <K, V, KS, VS> Map<K, V> toMediatedMap(Map<KS, VS> map, BidirectionalFunction<K, KS> keyWriteMapper,
                                                         BidirectionalFunction<K, KS> keyReadMapper, BidirectionalFunction<V, VS> valueWriteMapper,
                                                         BidirectionalFunction<V, VS> valueReadMapper)
    {
        return new MediatedMap<>(map, keyWriteMapper, keyReadMapper, valueWriteMapper, valueReadMapper);
    }

    public static <K, V, SK extends Supplier<K>, SV extends Supplier<V>> SupplierMap<K, V, SK, SV> newConcurrentHashSupplierMap(Function<Supplier<K>, SK> keySupplierFunction,
                                                                                                                                Function<Supplier<V>, SV> valueSupplierFunction)
    {
        Map<AbstractSupplierMap.KeySupplier<K>, Supplier<V>> map = new ConcurrentHashMap<>();
        return newSupplierMap(() -> map, keySupplierFunction, valueSupplierFunction);
    }

    public static <K, V, SK extends Supplier<K>, SV extends Supplier<V>> SupplierMap<K, V, SK, SV> newSupplierMap(Supplier<Map<KeySupplier<K>, Supplier<V>>> mapFactory,
                                                                                                                  Function<Supplier<K>, SK> keySupplierFunction,
                                                                                                                  Function<Supplier<V>, SV> valueSupplierFunction)
    {
        return new AbstractSupplierMap<>(mapFactory, keySupplierFunction, valueSupplierFunction);
    }

    /**
     * Returns a new {@link Map} with the values of the given {@link Map} for each of the given filter keys
     * 
     * @param map
     * @param filterKeys
     * @return
     */
    public static <K, V> Map<K, V> toKeyFilteredMap(Map<K, V> map, Collection<K> filterKeys)
    {
        Map<K, V> retmap = new HashMap<>();
        for (K key : filterKeys)
        {
            retmap.put(key, map.get(key));
        }
        return retmap;
    }

    public static <K, V> boolean containsAll(Map<K, V> map, Map<K, V> subMap)
    {
        boolean retval = true;

        if (subMap != null)
        {
            if (map != null)
            {
                for (K key : subMap.keySet())
                {
                    retval &= map.containsKey(key) && Objects.equals(map.get(key), subMap.get(key));

                    if (!retval)
                    {
                        break;
                    }
                }
            }
            else if (!subMap.isEmpty())
            {
                retval = false;
            }
        }

        return retval;
    }

    /**
     * Returns a new {@link Map} with the entries of the given {@link Map} but all keys filtered which have a null value
     * 
     * @param map
     * @return
     */
    public static <K, V> Map<K, V> toMapWithFilteredKeysHavingNullValue(Map<K, V> map)
    {
        Map<K, V> retmap = new HashMap<>();

        for (K key : map.keySet())
        {
            V value = map.get(key);
            if (value != null)
            {
                retmap.put(key, value);
            }
        }
        return retmap;
    }

    public static class MapDelta<K, V>
    {
        private SetDelta<K>           keyChanges;
        private Map<K, ValueDelta<V>> changes;

        public MapDelta(SetDelta<K> keyChanges, Map<K, ValueDelta<V>> changes)
        {
            super();
            this.keyChanges = keyChanges;
            this.changes = changes;
        }

        public SetDelta<K> getKeyChanges()
        {
            return this.keyChanges;
        }

        public Map<K, ValueDelta<V>> getChanges()
        {
            return this.changes;
        }

        @Override
        public String toString()
        {
            return "MapDelta [keyChanges=" + this.keyChanges + ", changes=" + this.changes + "]";
        }

        /**
         * Applies the key and value delta to a given {@link Map}
         * 
         * @param newMap
         * @return this
         */
        public MapDelta<K, V> applyTo(Map<K, V> map)
        {
            this.keyChanges.getRemoved()
                           .forEach(key -> map.remove(key));
            this.changes.keySet()
                        .stream()
                        .filter(key -> !this.keyChanges.getRemoved()
                                                       .contains(key))
                        .forEach(key -> map.put(key, this.changes.get(key)
                                                                 .getNext()));
            return this;
        }

    }

    public static class ValueDelta<V>
    {
        private V previous;
        private V next;

        public ValueDelta(V previous, V next)
        {
            super();
            this.previous = previous;
            this.next = next;
        }

        public V getPrevious()
        {
            return this.previous;
        }

        public V getNext()
        {
            return this.next;
        }

        @Override
        public String toString()
        {
            return "ValueDelta [previous=" + this.previous + ", next=" + this.next + "]";
        }

    }

    public static <K, V> MapDelta<K, V> delta(Map<K, V> previous, Map<K, V> next)
    {
        SetDelta<K> keyChanges = SetUtils.delta(Optional.ofNullable(previous)
                                                        .orElse(Collections.emptyMap())
                                                        .keySet(),
                                                Optional.ofNullable(next)
                                                        .orElse(Collections.emptyMap())
                                                        .keySet());
        Map<K, ValueDelta<V>> changes = new HashMap<>();
        keyChanges.getAll()
                  .forEach(key ->
                  {
                      V previousValue = previous.get(key);
                      V nextValue = next.get(key);
                      if (!Objects.equals(previousValue, nextValue))
                      {
                          changes.put(key, new ValueDelta<>(previousValue, nextValue));
                      }
                  });

        return new MapDelta<>(keyChanges, changes);
    }

    /**
     * Merges the values of multiple {@link Map}s into a single {@link Map}. Overwrites entries that are duplicates in random order.
     * 
     * @see #join(Map...)
     * @param maps
     * @return
     */
    @SafeVarargs
    public static <K, V> Map<K, V> merge(Map<K, V>... maps)
    {
        Map<K, V> result = new HashMap<>();

        if (maps != null)
        {
            for (Map<K, V> map : maps)
            {
                if (map != null)
                {
                    result.putAll(map);
                }
            }
        }

        return result;
    }

    /**
     * Similar to {@link #merge(Map...)} but allows to define a merge function, which decides about the result in key collision cases
     * 
     * @param valueMergeFunction
     * @param maps
     * @return
     */
    @SafeVarargs
    public static <K, V> Map<K, V> merge(BinaryOperator<V> valueMergeFunction, Map<K, V>... maps)
    {
        Map<K, V> result = new HashMap<>();

        if (maps != null)
        {
            for (Map<K, V> map : maps)
            {
                if (map != null)
                {
                    map.forEach((key, value) -> result.compute(key, (k, previousValue) -> previousValue != null ? valueMergeFunction.apply(previousValue, value)
                            : value));
                }
            }
        }

        return result;
    }

    /**
     * Returns true if the given {@link Map} is not null or empty.
     * 
     * @param map
     * @return
     */
    public static <K, V> boolean isNotEmpty(Map<K, V> map)
    {
        return map != null && !map.isEmpty();
    }

    /**
     * Returns a reducer which {@link #merge(Map...)}s the given {@link Map}s. This will overwrite values in collision cases. Please consider
     * {@link #merger(BinaryOperator)} in cases where collisions should be handled more gracefully.
     * 
     * @return
     */
    public static <K, V> BinaryOperator<Map<K, V>> merger()
    {
        return (map1, map2) -> merge(map1, map2);
    }

    /**
     * Returns a reducer which {@link #merge(BinaryOperator, Map...)}s the given {@link Map}s
     * 
     * @param valueMergeFunction
     * @return
     */
    public static <K, V> BinaryOperator<Map<K, V>> merger(BinaryOperator<V> valueMergeFunction)
    {
        return (map1, map2) -> merge(valueMergeFunction, map1, map2);
    }

    public static <K, V, VR> Map<K, VR> mapValues(Map<K, V> map, Function<V, VR> valueMapper)
    {
        return Optional.ofNullable(map)
                       .map(Map::entrySet)
                       .map(Set::stream)
                       .orElse(Stream.empty())
                       .collect(Collectors.toMap(Entry::getKey, entry -> valueMapper.apply(entry.getValue())));
    }

    /**
     * Returns a {@link SortedMap} with a natural comparator based on the values of the {@link Map}
     * 
     * @param map
     * @return
     */
    public static <K, V extends Comparable<V>> Map<K, V> toValueSortedMap(Map<K, V> map)
    {
        Function<K, V> keyToValueMapper = key -> map.get(key);
        return toSortedMap(map, ComparatorUtils.chainedComparator(ComparatorUtils.builder()
                                                                                 .of(keyToValueMapper)
                                                                                 .natural(),
                                                                  ComparatorUtils.natural()));
    }

    /**
     * Similar to {@link #toValueSortedMap(Map)} but with reverse order
     * 
     * @param map
     * @return
     */
    public static <K, V extends Comparable<V>> Map<K, V> toReverseValueSortedMap(Map<K, V> map)
    {
        Function<K, V> keyToValueMapper = key -> map.get(key);
        return toSortedMap(map, ComparatorUtils.reverse(ComparatorUtils.chainedComparator(ComparatorUtils.builder()
                                                                                                         .of(keyToValueMapper)
                                                                                                         .natural(),
                                                                                          ComparatorUtils.natural())));
    }

    /**
     * Returns a {@link SortedMap} using the given {@link Comparator} with the content of the given {@link Map}
     * 
     * @param map
     * @param comparator
     * @return
     */
    public static <K, V extends Comparable<V>> Map<K, V> toSortedMap(Map<K, V> map, Comparator<K> comparator)
    {
        return Optional.ofNullable(map)
                       .map(sourceMap ->
                       {
                           TreeMap<K, V> result = new TreeMap<>(comparator);
                           result.putAll(sourceMap);
                           return (Map<K, V>) result;
                       })
                       .orElse(Collections.emptyMap());
    }

    public static <K, V> Comparator<? super K> comparatorByMapValue(Map<K, V> map)
    {
        return ComparatorUtils.builder()
                              .of(map::get)
                              .natural()
                              .thenComparing(ComparatorUtils.builder()
                                                            .of(MapperUtils.identity())
                                                            .natural());
    }

    /**
     * Returns a {@link Function} which translates the keys of a {@link Map} into its values
     * 
     * @param map
     * @return
     */
    public static <K, V> Function<K, V> toKeyToValueMapper(Map<K, V> map)
    {
        V defaultValue = null;
        return toKeyToValueMapper(map, defaultValue);
    }

    /**
     * Similar to {@link #toKeyToValueMapper(Map)} allowing to specify a default value, if the {@link Map} would return null.
     * 
     * @param map
     * @param defaultValue
     * @return
     */
    public static <K, V> Function<K, V> toKeyToValueMapper(Map<K, V> map, V defaultValue)
    {
        return key -> Optional.ofNullable(map)
                              .orElse(Collections.emptyMap())
                              .getOrDefault(key, defaultValue);

    }

    /**
     * Returns a new {@link Map} containing the given key and value
     * 
     * @param key
     * @param value
     * @return
     */
    public static <K, V> Map<K, V> of(K key, V value)
    {
        HashMap<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Partitions the entries of a given {@link Map} instance into a {@link Stream} of {@link Map} instances that contain in the maximum case the partition size
     * number of entries each.
     * 
     * @param map
     * @param partitionSize
     * @return
     */
    public static <K, V> Stream<Map<K, V>> partition(Map<K, V> map, int partitionSize)
    {
        return StreamUtils.framedNonNullAsList(partitionSize, Optional.ofNullable(map)
                                                                      .orElse(Collections.emptyMap())
                                                                      .entrySet()
                                                                      .stream())
                          .map(entries -> entries.stream()
                                                 .collect(CollectorUtils.toMap()));
    }

    public static <K, V> Map<K, V> toNewConcurrentHashMap(Map<K, V> map)
    {
        return new ConcurrentHashMap<>(Optional.ofNullable(map)
                                               .orElse(Collections.emptyMap()));
    }

    public static <K> Set<K> toSet(Map<K, Boolean> map)
    {
        return toSet(map, true);
    }

    public static <K, V> Set<K> toSet(Map<K, V> map, V defaultValue)
    {
        Map<K, V> effectiveMap = Optional.ofNullable(map)
                                         .orElse(Collections.emptyMap());
        return new AbstractSet<K>()
        {
            @Override
            public Iterator<K> iterator()
            {
                return effectiveMap.keySet()
                                   .iterator();
            }

            @Override
            public int size()
            {
                return effectiveMap.size();
            }

            @Override
            public boolean contains(Object o)
            {
                return effectiveMap.containsKey(o);
            }

            @Override
            public boolean add(K key)
            {
                return effectiveMap.put(key, defaultValue) == null;
            }

            @Override
            public boolean remove(Object o)
            {
                return effectiveMap.remove(o) != null;
            }

            @Override
            public void clear()
            {
                effectiveMap.clear();
            }

        };
    }
}
