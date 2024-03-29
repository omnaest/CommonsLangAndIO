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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.omnaest.utils.optional.NullOptional;

public class ComparatorUtils
{

    public static <T, R extends Comparable<? super R>> Comparator<T> chainedComparator(Function<T, R> function)
    {
        return chainedComparator(Arrays.asList(function)
                                       .stream()
                                       .map(ifunction -> new Comparator<T>()
                                       {
                                           @Override
                                           public int compare(T o1, T o2)
                                           {
                                               return ComparatorUtils.compare(ifunction.apply(o1), ifunction.apply(o2));
                                           }
                                       }));
    }

    public static <T, R1 extends Comparable<? super R1>, R2 extends Comparable<? super R2>> Comparator<T> chainedComparator(Function<T, R1> function1,
                                                                                                                            Function<T, R2> function2)
    {
        return chainedComparator(new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return ComparatorUtils.compare(function1.apply(o1), function1.apply(o2));
            }
        }, new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return ComparatorUtils.compare(function2.apply(o1), function2.apply(o2));
            }
        });
    }

    public static <T, R1 extends Comparable<? super R1>, R2 extends Comparable<? super R2>, R3 extends Comparable<? super R3>> Comparator<T> chainedComparator(Function<T, R1> function1,
                                                                                                                                                               Function<T, R2> function2,
                                                                                                                                                               Function<T, R3> function3)
    {
        return chainedComparator(new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return ComparatorUtils.compare(function1.apply(o1), function1.apply(o2));
            }
        }, new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return ComparatorUtils.compare(function2.apply(o1), function2.apply(o2));
            }
        }, new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return ComparatorUtils.compare(function3.apply(o1), function3.apply(o2));
            }
        });
    }

    @SafeVarargs
    public static <T, R extends Comparable<? super R>> Comparator<T> chainedComparator(Function<T, R>... functions)
    {
        return chainedComparator(Arrays.asList(functions)
                                       .stream()
                                       .map(function -> new Comparator<T>()
                                       {
                                           @Override
                                           public int compare(T o1, T o2)
                                           {
                                               return ComparatorUtils.compare(function.apply(o1), function.apply(o2));
                                           }
                                       }));
    }

    @SafeVarargs
    public static <T> Comparator<T> chainedComparator(Comparator<T>... comparators)
    {
        return chainedComparator(Arrays.asList(comparators)
                                       .stream());
    }

    public static <T> Comparator<T> chainedComparator(Stream<Comparator<T>> comparators)
    {
        List<Comparator<T>> comparatorList = comparators.collect(Collectors.toList());
        return new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                int retval = 0;

                for (Comparator<T> comparator : IterableUtils.from(comparatorList.stream()
                                                                                 .filter(comparator -> comparator != null)))
                {
                    retval = comparator.compare(o1, o2);
                    if (retval != 0)
                    {
                        break;
                    }
                }

                return retval;
            }
        };
    }

    /**
     * @see StringUtils#compare(String, String)
     * @param str1
     * @param str2
     * @return
     */
    public static <E> int compare(String str1, String str2)
    {
        return StringUtils.compare(str1, str2);
    }

    /**
     * @see ObjectUtils#compare(Comparable, Comparable)
     * @param c1
     * @param c2
     * @return
     */
    public static <E extends Comparable<? super E>> int compare(E c1, E c2)
    {
        return ObjectUtils.compare(c1, c2);
    }

    @SuppressWarnings({ "unchecked" })
    public static int compareObject(Object o1, Object o2)
    {
        if (o1 instanceof Comparable && o2 instanceof Comparable)
        {
            return compare((Comparable<Object>) o1, (Comparable<Object>) o2);
        }
        else
        {
            return StringUtils.compare(Objects.toString(o1), Objects.toString(o2));
        }
    }

    protected static class TypedComparatorBuilderImpl<T> implements TypedComparatorBuilder<T>
    {
        public TypedComparatorBuilderImpl()
        {
            super();
        }

        @Override
        public <C extends Comparable<C>> ComparatorBuilderLoaded<T> with(Function<T, C> compareFunction)
        {
            return this.with((Comparator<T>) (o1, o2) -> compareFunction.apply(o1)
                                                                        .compareTo(compareFunction.apply(o2)));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public ComparatorBuilderLoaded<T> withIdentity()
        {
            return this.with((T i) -> (Comparable) i);
        }

        @Override
        public <C extends Comparable<C>, I> ComparatorBuilderLoaded<T> with(Function<T, I> compareFunction, Function<I, C> mapFunction)
        {
            return this.with(compareFunction.andThen(mapFunction));
        }

        @Override
        public ComparatorBuilderLoaded<T> with(Comparator<T> comparator)
        {
            return new ComparatorBuilderLoaded<T>()
            {
                private List<Comparator<T>> comparators = new ArrayList<>(Arrays.asList(comparator));
                private boolean             reversed    = false;

                @Override
                public ComparatorBuilderLoaded<T> and(Comparator<T> comparator)
                {
                    this.comparators.add(comparator);
                    return this;
                }

                @Override
                public ComparatorBuilderLoaded<T> reverse()
                {
                    this.reversed = true;
                    return this;
                }

                @Override
                public <C extends Comparable<C>> ComparatorBuilderLoaded<T> and(Function<T, C> compareFunction)
                {
                    return this.and((Comparator<T>) (o1, o2) -> compareFunction.apply(o1)
                                                                               .compareTo(compareFunction.apply(o2)));
                }

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public ComparatorBuilderLoaded<T> andIdentity()
                {
                    return this.and(i -> (Comparable) i);
                }

                @Override
                public Comparator<T> build()
                {
                    Comparator<T> retval = chainedComparator(this.comparators.stream());
                    return this.reversed ? retval.reversed() : retval;
                }

                @Override
                public <C extends Comparable<C>, I> ComparatorBuilderLoaded<T> and(Function<T, I> compareFunction, Function<I, C> mapFunction)
                {
                    return this.and(compareFunction.andThen(mapFunction));
                }
            };
        }

    }

    /**
     * Builder for a {@link Comparator}
     * 
     * @author omnaest
     */
    public static interface ComparatorBuilder
    {
        public <T> TypedComparatorBuilder<T> of(Class<? super T> type);

        public <T> ExecutableComparatorBuilder<T> of(T o1, T o2);

        public <S, T> MapperComparatorBuilder<S, T> of(Function<S, T> mapper);

        public <S, T> MapperComparatorBuilder<S, T> of(Function<S, T> mapper, Class<S> primaryType);

        public <S, T> MapperComparatorBuilder<S, T> of(Function<S, T> mapper, Class<S> primaryType, Class<T> targetType);
    }

    public static interface MapperComparatorBuilder<S, T>
    {
        public <U> MapperComparatorBuilder<S, U> and(Function<T, U> mapper);

        public Comparator<S> natural();

        public Comparator<S> with(Comparator<T> comparator);
    }

    public static interface TypedComparatorBuilder<T>
    {
        public ComparatorBuilderLoaded<T> with(Comparator<T> comparator);

        public <C extends Comparable<C>> ComparatorBuilderLoaded<T> with(Function<T, C> compareFunction);

        public ComparatorBuilderLoaded<T> withIdentity();

        public <C extends Comparable<C>, I> ComparatorBuilderLoaded<T> with(Function<T, I> compareFunction, Function<I, C> mapFunction);

    }

    public static interface ExecutableComparatorBuilder<T>
    {
        public ExecutableComparatorBuilderLoaded<T> with(Comparator<T> comparator);

        public <C extends Comparable<C>> ExecutableComparatorBuilderLoaded<T> with(Function<T, C> compareFunction);

        public ExecutableComparatorBuilderLoaded<T> withIdentity();

        public <C extends Comparable<C>, I> ExecutableComparatorBuilderLoaded<T> with(Function<T, I> compareFunction, Function<I, C> mapFunction);
    }

    public static interface ExecutableComparatorBuilderLoaded<T>
    {
        public ExecutableComparatorBuilderLoaded<T> and(Comparator<T> comparator);

        public <C extends Comparable<C>> ExecutableComparatorBuilderLoaded<T> and(Function<T, C> compareFunction);

        public <C extends Comparable<C>, I> ExecutableComparatorBuilderLoaded<T> and(Function<T, I> compareFunction, Function<I, C> mapFunction);

        public ExecutableComparatorBuilderLoaded<T> reverse();

        public int compare();

    }

    public static interface ComparatorBuilderLoaded<T>
    {
        public ComparatorBuilderLoaded<T> and(Comparator<T> comparator);

        public <C extends Comparable<C>> ComparatorBuilderLoaded<T> and(Function<T, C> compareFunction);

        public ComparatorBuilderLoaded<T> andIdentity();

        public <C extends Comparable<C>, I> ComparatorBuilderLoaded<T> and(Function<T, I> compareFunction, Function<I, C> mapFunction);

        /**
         * Inverts the defined {@link Comparator}
         * 
         * @return
         */
        public ComparatorBuilderLoaded<T> reverse();

        public Comparator<T> build();

    }

    /**
     * Example:<br>
     * <br>
     * 
     * <pre>
     * ComparatorUtils.builder()
     *                .of(ProteinTokenResult.class)
     *                .with(result -> result.getCoverage())
     *                .build()
     * </pre>
     * 
     * @return
     */
    public static ComparatorBuilder builder()
    {
        return new ComparatorBuilder()
        {
            @Override
            public <T> TypedComparatorBuilder<T> of(Class<? super T> type)
            {
                return new TypedComparatorBuilderImpl<>();
            }

            @Override
            public <T> ExecutableComparatorBuilder<T> of(T o1, T o2)
            {
                return new ExecutableComparatorBuilder<T>()
                {
                    @Override
                    public ExecutableComparatorBuilderLoaded<T> with(Comparator<T> comparator)
                    {
                        return new ExecutableComparatorBuilderLoaded<T>()
                        {
                            @SuppressWarnings("unchecked")
                            private ComparatorBuilderLoaded<T> comparatorBuilderLoaded = ComparatorUtils.builder()
                                                                                                        .of((Class<T>) o1.getClass())
                                                                                                        .with(comparator);

                            @Override
                            public int compare()
                            {
                                return this.comparatorBuilderLoaded.build()
                                                                   .compare(o1, o2);
                            }

                            @Override
                            public ExecutableComparatorBuilderLoaded<T> and(Comparator<T> comparator)
                            {
                                this.comparatorBuilderLoaded = this.comparatorBuilderLoaded.and(comparator);
                                return this;
                            }

                            @Override
                            public <C extends Comparable<C>> ExecutableComparatorBuilderLoaded<T> and(Function<T, C> compareFunction)
                            {
                                this.comparatorBuilderLoaded = this.comparatorBuilderLoaded.and(compareFunction);
                                return this;
                            }

                            @Override
                            public <C extends Comparable<C>, I> ExecutableComparatorBuilderLoaded<T> and(Function<T, I> compareFunction,
                                                                                                         Function<I, C> mapFunction)
                            {
                                this.comparatorBuilderLoaded = this.comparatorBuilderLoaded.and(compareFunction, mapFunction);
                                return this;
                            }

                            @Override
                            public ExecutableComparatorBuilderLoaded<T> reverse()
                            {
                                this.comparatorBuilderLoaded = this.comparatorBuilderLoaded.reverse();
                                return this;
                            }
                        };
                    }

                    @Override
                    public <C extends Comparable<C>> ExecutableComparatorBuilderLoaded<T> with(Function<T, C> compareFunction)
                    {
                        return this.with((Comparator<T>) (o1, o2) -> compareFunction.apply(o1)
                                                                                    .compareTo(compareFunction.apply(o2)));
                    }

                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    @Override
                    public ExecutableComparatorBuilderLoaded<T> withIdentity()
                    {
                        return this.with(i -> (Comparable) i);
                    }

                    @Override
                    public <C extends Comparable<C>, I> ExecutableComparatorBuilderLoaded<T> with(Function<T, I> compareFunction, Function<I, C> mapFunction)
                    {
                        return this.with(compareFunction.andThen(mapFunction));
                    }
                };
            }

            @Override
            public <S, T> MapperComparatorBuilder<S, T> of(Function<S, T> mapper)
            {
                return new MapperComparatorBuilder<S, T>()
                {
                    @Override
                    public Comparator<S> natural()
                    {
                        return this.with(ComparatorUtils.natural());
                    }

                    @Override
                    public Comparator<S> with(Comparator<T> comparator)
                    {
                        return (v1, v2) -> comparator.compare(mapper.apply(v1), mapper.apply(v2));
                    }

                    @Override
                    public <U> MapperComparatorBuilder<S, U> and(Function<T, U> secondMapper)
                    {
                        return of(mapper.andThen(secondMapper));
                    }
                };
            }

            @Override
            public <S, T> MapperComparatorBuilder<S, T> of(Function<S, T> mapper, Class<S> primaryType)
            {
                return this.of(mapper);
            }

            @Override
            public <S, T> MapperComparatorBuilder<S, T> of(Function<S, T> mapper, Class<S> primaryType, Class<T> targetType)
            {
                return this.of(mapper);
            }
        };
    }

    public static <E> Comparator<E> reverse(Comparator<E> comparator)
    {
        return (e1, e2) -> -1 * comparator.compare(e1, e2);
    }

    public static <T, C extends Comparable<C>> Comparator<T> comparatorFunction(Function<T, C> extractFunction)
    {
        return (o1, o2) -> extractFunction.apply(o1)
                                          .compareTo(extractFunction.apply(o2));
    }

    public static <T> boolean isBefore(Comparator<T> comparator, T value1, T value2)
    {
        return comparator.compare(value1, value2) < 0;
    }

    public static <T> boolean isAfter(Comparator<T> comparator, T value1, T value2)
    {
        return comparator.compare(value1, value2) > 0;
    }

    public static <T> boolean isEqual(Comparator<T> comparator, T value1, T value2)
    {
        return comparator.compare(value1, value2) == 0;
    }

    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> natural()
    {
        return (c1, c2) -> NullOptional.ofNullable(c1)
                                       .filter(value -> value instanceof Comparable)
                                       .map(value -> (Comparable<T>) value)
                                       .map(value -> value.compareTo(c2))
                                       .orElseGetAndFlatMap(() -> NullOptional.ofNullable(c2)
                                                                              .filter(v -> v instanceof Comparable)
                                                                              .map(v -> (Comparable<T>) v)
                                                                              .map(v -> -1 * v.compareTo(c2)))
                                       .orElse(0);
    }

}
