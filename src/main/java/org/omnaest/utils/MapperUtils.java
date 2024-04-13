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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.stream.FilterMapper;

/**
 * Utils around {@link Stream#map(java.util.function.Function)}
 * 
 * @see CollectorUtils
 * @author omnaest
 */
public class MapperUtils
{

    public static <K1, V, K2> Function<Map.Entry<K1, V>, Map.Entry<K2, V>> mapEntryKey(Function<K1, K2> keyMapper)
    {
        return mapEntry(keyMapper, v -> v);
    }

    public static <K, V1, V2> Function<Map.Entry<K, V1>, Map.Entry<K, V2>> mapEntryValue(Function<V1, V2> valueMapper)
    {
        return mapEntry(k -> k, valueMapper);
    }

    public static <K1, V1, K2, V2> Function<Map.Entry<K1, V1>, Map.Entry<K2, V2>> mapEntry(Function<K1, K2> keyMapper, Function<V1, V2> valueMapper)
    {
        return entry -> new Map.Entry<K2, V2>()
        {
            @Override
            public K2 getKey()
            {
                return keyMapper.apply(entry.getKey());
            }

            @Override
            public V2 getValue()
            {
                return valueMapper.apply(entry.getValue());
            }

            @Override
            public V2 setValue(V2 value)
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns an {@link UnaryOperator} which maps to the identity
     * 
     * @return
     */
    public static <F extends T, T> Function<F, T> identity()
    {
        return i -> i;
    }

    /**
     * Similar to {@link #identity()}
     * 
     * @return
     */
    public static <T> UnaryOperator<T> unaryIdentity()
    {
        return i -> i;
    }

    /**
     * Returns an {@link UnaryOperator} which casts to the given type and maps to the identity
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <F, T> Function<F, T> identityCast(Class<T> type)
    {
        return i -> (T) i;
    }

    /**
     * Returns an {@link LongFunction} which maps to the identity
     * 
     * @return
     */
    public static LongFunction<Long> identityForLongAsBoxed()
    {
        return i -> i;
    }

    /**
     * Returns an {@link ToLongFunction} which maps to the identity
     * 
     * @return
     */
    public static ToLongFunction<Long> identitiyForLongAsUnboxed()
    {
        return i -> i;
    }

    /**
     * Returns an {@link ToIntFunction} which maps to the identity
     * 
     * @return
     */
    public static ToIntFunction<Integer> identitiyForIntegerAsUnboxed()
    {
        return i -> i;
    }

    public static ToDoubleFunction<? super Double> identitiyForDoubleAsUnboxed()
    {
        return i -> i;
    }

    /**
     * Returns a {@link Function} which maps a {@link Map.Entry} to a {@link BiElement}
     * 
     * @return
     */
    public static <K, V> Function<Map.Entry<K, V>, BiElement<K, V>> mapEntryToBiElement()
    {
        return entry -> BiElement.of(entry.getKey(), entry.getValue());
    }

    /**
     * Returns an immutable {@link Map.Entry} instance wrapping a {@link BiElement}.
     * 
     * @return
     */
    public static <K, V> Function<BiElement<K, V>, Map.Entry<K, V>> mapBiElementToEntry()
    {
        return biElement -> new Map.Entry<K, V>()
        {
            @Override
            public K getKey()
            {
                return biElement.getFirst();
            }

            @Override
            public V getValue()
            {
                return biElement.getSecond();
            }

            @Override
            public V setValue(V value)
            {
                throw new UnsupportedOperationException("This entry is immutable");
            }
        };
    }

    /**
     * Returns a {@link Function} which adds a count to the given element
     * 
     * @return
     */
    public static <E> Function<E, BiElement<E, Long>> withLongCounter()
    {
        AtomicLong counter = new AtomicLong();
        return element -> BiElement.of(element, counter.getAndIncrement());
    }

    /**
     * Returns a {@link Function} which adds a count to the given element. Count start with 0, 1, 2, ...
     * 
     * @return
     */
    public static <E> Function<E, BiElement<E, Integer>> withIntCounter()
    {
        AtomicInteger counter = new AtomicInteger();
        return element -> BiElement.of(element, counter.getAndIncrement());
    }

    /**
     * Maps an element to boolean true if the element is not null, otherwise to false
     * 
     * @return
     */
    public static <E> Function<E, Boolean> isNotNull()
    {
        return element -> element != null;
    }

    public static <E, E2 extends E> Function<E, E2> castToType(Class<E2> type)
    {
        return element -> type.cast(element);
    }

    /**
     * Similar to {@link #castToType(Class)} but catches any {@link ClassCastException} and returns null in that case.
     * 
     * @param <E>
     * @param <E2>
     * @param type
     * @return
     */
    public static <E, E2 extends E> Function<E, E2> castToTypeSilently(Class<E2> type)
    {
        return element ->
        {
            try
            {
                return type.cast(element);
            }
            catch (ClassCastException e)
            {
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <E, E2> Function<E, List<E2>> castToListType(Class<E2> elementType)
    {
        return element -> (List<E2>) element;
    }

    /**
     * Similar to {@link #castToListType(Class)} but catches any {@link ClassCastException} and returns null in that case.
     * 
     * @param <E>
     * @param <E2>
     * @param elementType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E, E2> Function<E, List<E2>> castToListTypeSilently(Class<E2> elementType)
    {
        return element ->
        {
            try
            {
                return (List<E2>) element;
            }
            catch (ClassCastException e)
            {
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <E, E2> Function<E, Set<E2>> castToSetType(Class<E2> elementType)
    {
        return element -> (Set<E2>) element;
    }

    /**
     * Similar to {@link #castToSetType(Class)} but catches any {@link ClassCastException} and returns null in that case.
     * 
     * @param <E>
     * @param <E2>
     * @param elementType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E, E2> Function<E, Set<E2>> castToSetTypeSilently(Class<E2> elementType)
    {
        return element ->
        {
            try
            {
                return (Set<E2>) element;
            }
            catch (ClassCastException e)
            {
                return null;
            }
        };
    }

    /**
     * Maps an {@link Optional} to its value by calling {@link Optional#orElse(Object)} and returning null as default.
     * 
     * @return
     */
    public static <E> Function<Optional<E>, E> mapOptionalToValue()
    {
        return element -> element.orElse(null);
    }

    /**
     * @see StreamUtils#filterMapper(Predicate, Function)
     * @param filter
     * @param mapper
     * @return
     */
    public static <T, R> FilterMapper<T, R> filterMapper(Predicate<T> filter, Function<T, R> mapper)
    {
        return StreamUtils.filterMapper(filter, mapper);
    }

    /**
     * Returns a mapper {@link Function} that returns the invocation count regardless of the given element.
     * 
     * @return
     */
    public static <E> Function<E, Integer> counting()
    {
        AtomicInteger counter = new AtomicInteger();
        return element -> counter.getAndIncrement();
    }

}
