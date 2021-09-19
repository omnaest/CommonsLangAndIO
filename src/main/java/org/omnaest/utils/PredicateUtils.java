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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.omnaest.utils.stream.FirstElementFilterCapture;

/**
 * Utils regarding {@link Predicate} logic
 * 
 * @author omnaest
 */
public class PredicateUtils
{
    /**
     * Returns a {@link Predicate} that returns true if the given test object is not null
     * 
     * @return
     */
    public static <E> Predicate<E> notNull()
    {
        return Objects::nonNull;
    }

    public static interface ModuloPredicateBuilder<E>
    {
        public Predicate<E> equals(int value);

        public Predicate<E> equalsZero();
    }

    public static <E> ModuloPredicateBuilder<E> modulo(int modulo)
    {
        return new ModuloPredicateBuilder<E>()
        {
            @Override
            public Predicate<E> equals(int value)
            {
                return new Predicate<E>()
                {
                    private AtomicLong counter = new AtomicLong();

                    @Override
                    public boolean test(E t)
                    {
                        return this.counter.getAndIncrement() % modulo == value;
                    }
                };
            }

            @Override
            public Predicate<E> equalsZero()
            {
                return this.equals(0);
            }
        };
    }

    /**
     * Returns a {@link FirstElementFilterCapture} which can be used in combination with {@link Stream#filter(Predicate)}
     * 
     * @return
     */
    public static <E> FirstElementFilterCapture<E> firstElementFilterCapture()
    {
        return new FirstElementFilterCapture<>();
    }

    public static <E> Predicate<E> firstElement()
    {
        AtomicBoolean first = new AtomicBoolean(true);
        return e -> first.getAndSet(false);
    }

    /**
     * @see StringUtils#isNotBlank(CharSequence)
     * @return
     */
    public static Predicate<? super String> notBlank()
    {
        return str -> StringUtils.isNotBlank(str);
    }

    /**
     * @see StringUtils#isNotEmpty(CharSequence)
     * @return
     */
    public static Predicate<? super String> notEmpty()
    {
        return str -> StringUtils.isNotEmpty(str);
    }

    /**
     * Returns true if the tested object is null
     * 
     * @return
     */
    public static <T> Predicate<T> isNull()
    {
        return o -> o == null;
    }

    /**
     * Returns true if the tested element does not match the given
     * 
     * @param gene
     * @return
     */
    public static <T> Predicate<T> notEqueals(T object)
    {
        return t -> !Objects.equals(t, object);
    }

    /**
     * Returns allways true for all elements
     * 
     * @return
     */
    public static <T> Predicate<T> allMatching()
    {
        return t -> true;
    }

    public static <E> Predicate<? super List<E>> listNotEmpty()
    {
        return list -> list != null && !list.isEmpty();
    }

    public static <E> Predicate<? super E> isContainedIn(Set<E> elements)
    {
        return element -> elements.contains(element);
    }

    public static <E, R> PredicateMapping<E, R> map(Function<E, R> mapper)
    {
        return new PredicateMapping<E, R>()
        {
            @Override
            public Predicate<E> and(Predicate<? super R> predicate)
            {
                return element -> predicate.test(mapper.apply(element));
            }

            @Override
            public Predicate<E> andNotNull()
            {
                return this.and(notNull());
            }

            @Override
            public Predicate<E> andIsContainedIn(Set<R> elements)
            {
                return this.and(isContainedIn(elements));
            }
        };
    }

    public static interface PredicateMapping<E, R>
    {
        public Predicate<E> and(Predicate<? super R> predicate);

        public Predicate<E> andNotNull();

        public Predicate<E> andIsContainedIn(Set<R> elements);
    }

    /**
     * Returns true until the given {@link Predicate} filter returns true once.
     * 
     * @param filter
     * @return
     */
    public static <E> Predicate<E> until(Predicate<E> filter)
    {
        AtomicBoolean onceMatched = new AtomicBoolean(false);
        return element ->
        {
            boolean test = filter.test(element);
            onceMatched.compareAndSet(false, test);
            return !onceMatched.get() && !test;
        };
    }

    /**
     * Filters on non empty {@link Optional} instances and tests negative for {@link Optional#empty()}
     * 
     * @return
     */
    public static <E> Predicate<Optional<E>> filterNonEmptyOptional()
    {
        return element -> element != null && element.isPresent();
    }

    public static <E extends S, S> Predicate<S> matchesType(Class<E> type)
    {
        return element -> Optional.ofNullable(element)
                                  .map(S::getClass)
                                  .map(type::isAssignableFrom)
                                  .orElse(false);
    }

}
