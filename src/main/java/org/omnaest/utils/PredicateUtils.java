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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
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
        return new PredicateMappingImpl<E, R>(mapper);
    }

    private static class PredicateMappingImpl<E, R> implements PredicateMapping<E, R>
    {
        private final Function<E, R> mapper;

        private PredicateMappingImpl(Function<E, R> mapper)
        {
            this.mapper = mapper;
        }

        @Override
        public Predicate<E> and(Predicate<? super R> predicate)
        {
            return element -> predicate.test(this.mapper.apply(element));
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

        @Override
        public <NR> PredicateMapping<E, NR> map(Function<R, NR> mapper2)
        {
            return new PredicateMappingImpl<>(this.mapper.andThen(mapper2));
        }
    }

    public static interface PredicateMapping<E, R>
    {
        public Predicate<E> and(Predicate<? super R> predicate);

        public Predicate<E> andNotNull();

        public Predicate<E> andIsContainedIn(Set<R> elements);

        public <NR> PredicateMapping<E, NR> map(Function<R, NR> mapper);
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

    public static <E> MappablePredicate<E> isCollectionContaining(Collection<E> elements)
    {
        Collection<E> effectiveElements = Optional.ofNullable(elements)
                                                  .orElse(Collections.emptySet());
        return new MappablePredicateImpl<E>(effectiveElements);
    }

    public static <E> MappablePredicate<E> isCollectionNotContaining(Collection<E> elements)
    {
        return isCollectionContaining(elements).negate();
    }

    public static interface MappablePredicate<E> extends Predicate<E>
    {
        public <S> MappablePredicate<S> from(Function<S, E> mapper);

        @Override
        public MappablePredicate<E> negate();

        /**
         * If {@link #test(Object)} returns true the given {@link Consumer} is called with the current element.
         * 
         * @param elementConsumer
         * @return
         */
        public MappablePredicate<E> ifTrueThen(Consumer<E> elementConsumer);

        /**
         * If {@link #test(Object)} returns the condition value then the given {@link Consumer} is called with the current element.
         * 
         * @param elementConsumer
         * @return
         */
        public MappablePredicate<E> ifConditionThen(boolean condition, Consumer<E> elementConsumer);

        /**
         * If {@link #test(Object)} returns false the given {@link Consumer} is called with the current element.
         * 
         * @param elementConsumer
         * @return
         */
        public MappablePredicate<E> ifFalseThen(Consumer<E> elementConsumer);

    }

    private static class MappablePredicateDecorator<E> implements MappablePredicate<E>
    {
        private MappablePredicate<E> mappablePredicate;

        public MappablePredicateDecorator(MappablePredicate<E> mappablePredicate)
        {
            super();
            this.mappablePredicate = mappablePredicate;
        }

        @Override
        public boolean test(E element)
        {
            return this.mappablePredicate.test(element);
        }

        @Override
        public <S> MappablePredicate<S> from(Function<S, E> mapper)
        {
            return this.mappablePredicate.from(mapper);
        }

        @Override
        public MappablePredicate<E> negate()
        {
            return this.mappablePredicate.negate();
        }

        @Override
        public MappablePredicate<E> ifTrueThen(Consumer<E> elementConsumer)
        {
            return this.mappablePredicate.ifTrueThen(elementConsumer);
        }

        @Override
        public MappablePredicate<E> ifConditionThen(boolean condition, Consumer<E> elementConsumer)
        {
            return this.mappablePredicate.ifConditionThen(condition, elementConsumer);
        }

        @Override
        public MappablePredicate<E> ifFalseThen(Consumer<E> elementConsumer)
        {
            return this.mappablePredicate.ifFalseThen(elementConsumer);
        }

    }

    private static abstract class AbstractMappablePredicate<E> implements MappablePredicate<E>
    {
        @Override
        public <S> MappablePredicate<S> from(Function<S, E> mapper)
        {
            return new MappablePredicateAdapter<>(this, mapper);
        }

        @Override
        public MappablePredicate<E> negate()
        {
            return new NegatedMappablePredicate<>(this);
        }

        @Override
        public MappablePredicate<E> ifTrueThen(Consumer<E> elementConsumer)
        {
            return this.ifConditionThen(true, elementConsumer);
        }

        @Override
        public MappablePredicate<E> ifFalseThen(Consumer<E> elementConsumer)
        {
            return this.ifConditionThen(false, elementConsumer);
        }

        @Override
        public MappablePredicate<E> ifConditionThen(boolean condition, Consumer<E> elementConsumer)
        {
            return new MappablePredicateDecorator<E>(this)
            {
                @Override
                public boolean test(E element)
                {
                    boolean result = super.test(element);

                    if (result == condition && elementConsumer != null)
                    {
                        elementConsumer.accept(element);
                    }

                    return result;
                }
            };
        }
    }

    private static class MappablePredicateImpl<E> extends AbstractMappablePredicate<E>
    {
        private final Collection<E> effectiveElements;

        private MappablePredicateImpl(Collection<E> effectiveElements)
        {
            this.effectiveElements = effectiveElements;
        }

        @Override
        public boolean test(E element)
        {
            return this.effectiveElements.contains(element);
        }
    }

    private static class MappablePredicateAdapter<S, T> extends AbstractMappablePredicate<S>
    {
        private final MappablePredicate<T> mappablePredicate;
        private final Function<S, T>       mapper;

        public MappablePredicateAdapter(MappablePredicate<T> mappablePredicate, Function<S, T> mapper)
        {
            super();
            this.mappablePredicate = mappablePredicate;
            this.mapper = mapper;
        }

        @Override
        public boolean test(S element)
        {
            return this.mappablePredicate.test(this.mapper.apply(element));
        }

    }

    private static class NegatedMappablePredicate<E> extends AbstractMappablePredicate<E>
    {
        private MappablePredicate<E> mappablePredicate;

        public NegatedMappablePredicate(MappablePredicate<E> mappablePredicate)
        {
            super();
            this.mappablePredicate = mappablePredicate;
        }

        @Override
        public boolean test(E t)
        {
            return !this.mappablePredicate.test(t);
        }
    }

    /**
     * Returns a {@link Predicate} that returns true, if the given {@link Map} does NOT contain the tested element as a key.
     * 
     * @see PredicateUtils#isMapContainingKey(Map)
     * @param map
     * @return
     */
    public static <K> Predicate<K> isMapNotContainingKey(Map<K, ?> map)
    {
        return isMapContainingKey(map).negate();
    }

    /**
     * Returns a {@link Predicate} that returns true, if the given {@link Map} does contain the tested element as a key.
     * 
     * @see PredicateUtils#isMapNotContainingKey(Map)
     * @param map
     * @return
     */
    public static <K> Predicate<K> isMapContainingKey(Map<K, ?> map)
    {
        Map<K, ?> effectiveMap = Optional.ofNullable(map)
                                         .orElse(Collections.emptyMap());
        return key -> key != null && effectiveMap.containsKey(key);
    }

    @SafeVarargs
    public static <E> Predicate<E> all(Predicate<E>... predicates)
    {
        return all(Arrays.asList(predicates));
    }

    public static <E> Predicate<E> all(Collection<? extends Predicate<E>> predicates)
    {
        return element -> Optional.ofNullable(predicates)
                                  .orElse(Collections.emptyList())
                                  .stream()
                                  .allMatch(predicate -> predicate.test(element));
    }

    public static interface FirstEncounterTypeMappablePredicate<E, ME> extends Predicate<E>
    {
        public <NE> FirstEncounterTypeMappablePredicate<E, NE> withMapping(Function<ME, NE> mapper);
    }

    @SuppressWarnings("unchecked")
    public static <E> Predicate<E> encounteredFirst()
    {
        return (Predicate<E>) encounteredFirst(Object.class);
    }

    public static <E> FirstEncounterTypeMappablePredicate<E, E> encounteredFirst(Class<E> type)
    {
        return new FirstEncounterTypeMappablePredicateImpl<E, E>(MapperUtils.identity());
    }

    private static class FirstEncounterTypeMappablePredicateImpl<E, ME> implements FirstEncounterTypeMappablePredicate<E, ME>
    {
        private Set<ME>               visitedElements = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private final Predicate<ME>   filter          = element -> this.visitedElements.add(element);
        private final Function<E, ME> mapper;

        private FirstEncounterTypeMappablePredicateImpl(Function<E, ME> mapper)
        {
            this.mapper = mapper;
        }

        @Override
        public boolean test(E element)
        {
            return this.filter.test(this.mapper.apply(element));
        }

        @Override
        public <NE> FirstEncounterTypeMappablePredicate<E, NE> withMapping(Function<ME, NE> mapper)
        {
            return new FirstEncounterTypeMappablePredicateImpl<E, NE>(this.mapper.andThen(mapper));
        }
    }

    /**
     * Allows to consume the excluded (false) elements based on the given filter {@link Predicate}.
     * 
     * @param filter
     * @param consumer
     * @return
     */
    public static <E> Predicate<E> consumeExcluded(Predicate<E> filter, Consumer<E> consumer)
    {
        return consume(filter, null, consumer);
    }

    /**
     * Allows to consume the included (true) elements based on the given filter {@link Predicate}.
     * 
     * @param filter
     * @param consumer
     * @return
     */
    public static <E> Predicate<E> consumeIncluded(Predicate<E> filter, Consumer<E> consumer)
    {
        return consume(filter, consumer, null);
    }

    /**
     * Allows to consume the elements depending on the result of the given filter {@link Predicate}.
     * 
     * @param filter
     * @param includedElementConsumer
     * @param excludedElementConsumer
     * @return
     */
    public static <E> Predicate<E> consume(Predicate<E> filter, Consumer<E> includedElementConsumer, Consumer<E> excludedElementConsumer)
    {
        return element ->
        {
            boolean result = filter.test(element);
            if (result && includedElementConsumer != null)
            {
                includedElementConsumer.accept(element);
            }
            else if (!result && excludedElementConsumer != null)
            {
                excludedElementConsumer.accept(element);
            }
            return result;
        };
    }
}
