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
package org.omnaest.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.omnaest.utils.collectors.ResultMappedCollector;
import org.omnaest.utils.element.bi.BiElement;

/**
 * Helper around {@link Collector}s
 * 
 * @see MapperUtils
 * @author omnaest
 */
public class CollectorUtils
{
    public static <K, V, VR> Collector<Map.Entry<K, V>, ?, Map<K, VR>> toValueMappedMap(Function<Map.Entry<K, V>, VR> valueMapper)
    {
        return Collectors.toMap(Entry::getKey, valueMapper);
    }

    public static <K, V, KR> Collector<Map.Entry<K, V>, ?, Map<KR, V>> toKeyMappedMap(Function<Map.Entry<K, V>, KR> keyMapper)
    {
        return Collectors.toMap(keyMapper, entry -> entry.getValue());
    }

    public static <K, V, KR, VR> Collector<Map.Entry<K, V>, ?, Map<KR, VR>> toMap(Function<Map.Entry<K, V>, KR> keyMapper,
                                                                                  Function<Map.Entry<K, V>, VR> valueMapper)
    {
        return toMap(keyMapper, valueMapper, () -> new HashMap<>());
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> appendToMap(Map<K, V> map)
    {
        return Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (e1, e2) -> e1, () -> map);
    }

    public static <E, K> Collector<E, ?, Map<K, E>> groupingByUnique(Function<E, K> classifier)
    {
        Function<Map<K, List<E>>, Map<K, E>> mapper = result -> result.entrySet()
                                                                      .stream()
                                                                      .collect(toValueMappedMap(entry -> ListUtils.first(entry.getValue())));
        return new ResultMappedCollector<>(Collectors.groupingBy(classifier), mapper);
    }

    public static <K, V> Collector<Entry<K, V>, ?, Map<K, V>> toMap()
    {
        return appendToMap(new HashMap<>());
    }

    public static <E1, E2> Collector<BiElement<E1, E2>, ?, Map<E1, E2>> toMapByBiElement()
    {
        return Collectors.toMap(biElement -> biElement.getFirst(), biElement -> biElement.getSecond());
    }

    public static <E1, E2> Collector<BiElement<E1, E2>, ?, Map<E1, E2>> toMapByBiElement(BinaryOperator<E2> mergeFunction)
    {
        return Collectors.toMap(biElement -> biElement.getFirst(), biElement -> biElement.getSecond(), mergeFunction);
    }

    public static <E1, E2> Collector<BiElement<E1, E2>, ?, Map<E1, E2>> toMapByBiElement(Supplier<Map<E1, E2>> mapFactory)
    {
        return Collectors.toMap(biElement -> biElement.getFirst(), biElement -> biElement.getSecond(), (v1, v2) ->
        {
            if (!Objects.equals(v1, v2))
            {
                throw new IllegalStateException(String.format("Duplicate key %s", v1));
            }
            return v1;
        }, mapFactory);
    }

    public static <E1, E2> Collector<BiElement<E1, E2>, ?, Map<E1, List<E2>>> toGroupedMapByBiElement()
    {
        return Collectors.groupingBy(BiElement::getFirst, Collectors.mapping(BiElement::getSecond, Collectors.toList()));
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper, Supplier<Map<K, V>> mapSupplier)
    {
        return Collectors.toMap(keyMapper, valueMapper, (v1, v2) ->
        {
            if (!Objects.equals(v1, v2))
            {
                throw new IllegalStateException(String.format("Duplicate key %s", v1));
            }
            return v1;
        }, mapSupplier);
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toLinkedHashMap(Function<T, K> keyMapper, Function<T, V> valueMapper)
    {
        return toMap(keyMapper, valueMapper, () -> new LinkedHashMap<>());
    }

    public static <E> Collector<E, ?, Set<E>> toSet(Set<E> set)
    {
        return toSet(() -> set);
    }

    public static <E> Collector<E, ?, SortedSet<E>> toSortedSet(SortedSet<E> set)
    {
        return toSortedSet(() -> set);
    }

    public static <E> Collector<E, ?, SortedSet<E>> toSortedSet()
    {
        return toSortedSet(() -> new TreeSet<>());
    }

    public static <E> Collector<E, ?, Set<E>> toSet(Supplier<Set<E>> factory)
    {
        return Collectors.toCollection(factory);
    }

    public static <E> Collector<E, ?, SortedSet<E>> toSortedSet(Supplier<SortedSet<E>> factory)
    {
        return Collectors.toCollection(factory);
    }

}
