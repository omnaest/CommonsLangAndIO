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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
         * @param mapFactory
         * @return
         */
        public <K2, V2> MapBuilder<K2, V2> useFactory(Supplier<Map<K2, V2>> mapFactory);

        /**
         * Builds the {@link Map}
         * 
         * @return
         */
        public <K2 extends K, V2 extends V> Map<K2, V2> build();

    }

    @SuppressWarnings("unchecked")
    public static MapBuilder<?, ?> builder()
    {
        return new MapBuilder<Object, Object>()
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
            public <K2, V2> MapBuilder<K2, V2> useFactory(Supplier<Map<K2, V2>> mapFactory)
            {
                this.mapFactory = () -> (Map<Object, Object>) mapFactory.get();
                return (MapBuilder<K2, V2>) this;
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

        };
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

}
