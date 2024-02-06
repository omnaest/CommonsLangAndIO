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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.functional.Action;

public class EnumUtils
{
    /**
     * Maps a given source {@link Enum} value to a target {@link Enum} value by name. If the mapping is not possible an {@link Optional#empty()} is returned.
     * 
     * @param sourceEnumValue
     * @param targetEnumType
     * @return
     */
    public static <S extends Enum<S>, T extends Enum<T>> Optional<T> mapByName(S sourceEnumValue, Class<T> targetEnumType)
    {
        return mapBy(sourceEnumValue, s -> s.name(), targetEnumType);
    }

    public static <S extends Enum<S>, T extends Enum<T>> Optional<T> mapBy(S sourceEnumValue, Function<S, String> nameFunction, Class<T> targetEnumType)
    {
        try
        {
            return Optional.of(Enum.valueOf(targetEnumType, nameFunction.apply(sourceEnumValue)));
        }
        catch (Exception e)
        {
            return Optional.empty();
        }
    }

    /**
     * Returns the {@link Enum} value matching the given name.
     * 
     * @param name
     * @param type
     * @return
     */
    public static <E extends Enum<E>> Optional<E> toEnumValue(String name, Class<E> type)
    {
        try
        {
            return Optional.of(Enum.valueOf(type, name));
        }
        catch (Exception e)
        {
            return Optional.empty();
        }
    }

    public static <E extends Enum<?>> Supplier<E> cyclicEnumValueSupplier(Class<E> enumType)
    {
        E[] enumConstants = enumType.getEnumConstants();
        AtomicInteger index = new AtomicInteger();
        return () -> enumConstants[index.getAndUpdate(previous -> (previous + 1) % enumConstants.length)];
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<?>> E cyclicNextEnumValue(E value)
    {
        Class<E> enumType = (Class<E>) Optional.ofNullable(value)
                                               .map(E::getClass)
                                               .orElseThrow(() -> new IllegalArgumentException("No valid enum value has been given."));
        E[] enumConstants = enumType.getEnumConstants();
        return enumConstants[(value.ordinal() + 1) % enumConstants.length];
    }

    public static <E extends Enum<?>> Stream<E> toStream(Class<E> enumType)
    {
        return Optional.ofNullable(enumType)
                       .map(Class::getEnumConstants)
                       .map(Stream::of)
                       .orElse(Stream.empty());
    }

    /**
     * Allows to decide based on the underlying value what to execute
     * 
     * @author omnaest
     * @param <E>
     */
    public static interface Decider<E extends Enum<E>>
    {
        public Decider<E> orElse(E defaultValue);

        public Decider<E> ifEqualTo(E value, Action action);

        /**
         * Returns the result of {@link Supplier#get()} if the given enum value is equal to the underlying
         * 
         * @param value
         * @param resultSupplier
         * @return
         */
        public <R> OptionalDecision<E, R> ifEqualTo(E value, Supplier<R> resultSupplier);

        public <R> OptionalDecision<E, R> ifEqualToAny(Iterable<E> values, Function<List<E>, R> resultSupplier);

        public Decider<E> ifAnyEqualTo(Iterable<E> values, Consumer<List<E>> action);

        public boolean isEqualTo(E value);

        @SuppressWarnings("unchecked")
        public boolean isEqualToAny(E... values);

    }

    public static interface OptionalDecision<E extends Enum<E>, R>
    {
        public OptionalDecision<E, R> orIfEqualTo(E value, Supplier<R> resultSupplier);

        public OptionalDecision<E, R> orIfEqualToAny(Iterable<E> values, Function<List<E>, R> resultSupplier);

        public R get();

        public R orElse(R result);

        public R orElseGet(Supplier<R> resultSupplier);

        public <X extends Throwable> R orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

    }

    /**
     * @see Decider
     * @param value
     * @return
     */
    public static <E extends Enum<E>> Decider<E> decideOn(E value)
    {
        return new Decider<E>()
        {
            private E effectiveValue = value;

            @Override
            public Decider<E> ifEqualTo(E value, Action action)
            {
                if (this.isEqualTo(value))
                {
                    action.run();
                }
                return this;
            }

            @Override
            public <R> OptionalDecision<E, R> ifEqualTo(E value, Supplier<R> resultSupplier)
            {
                if (this.isEqualTo(value))
                {
                    return new OptionalDecisionImpl<>(Optional.ofNullable(resultSupplier), this);
                }
                else
                {
                    return new OptionalDecisionImpl<>(Optional.empty(), this);
                }
            }

            @Override
            public <R> OptionalDecision<E, R> ifEqualToAny(Iterable<E> values, Function<List<E>, R> resultSupplier)
            {
                List<E> matchingEnumValues = this.determineMatchingEnumValues(values);
                if (!matchingEnumValues.isEmpty())
                {
                    return new OptionalDecisionImpl<>(Optional.ofNullable(resultSupplier)
                                                              .map(rs -> () -> rs.apply(matchingEnumValues)),
                                                      this);
                }
                else
                {
                    return new OptionalDecisionImpl<>(Optional.empty(), this);
                }
            }

            @Override
            public Decider<E> orElse(E defaultValue)
            {
                if (this.effectiveValue == null)
                {
                    this.effectiveValue = defaultValue;
                }
                return this;
            }

            @Override
            public Decider<E> ifAnyEqualTo(Iterable<E> values, Consumer<List<E>> action)
            {
                List<E> matchingEnumValues = this.determineMatchingEnumValues(values);
                if (!matchingEnumValues.isEmpty())
                {
                    action.accept(matchingEnumValues);
                }

                return this;
            }

            private List<E> determineMatchingEnumValues(Iterable<E> values)
            {
                return StreamUtils.fromIterable(values)
                                  .filter(value -> this.isEqualTo(value))
                                  .collect(Collectors.toList());
            }

            @Override
            public boolean isEqualTo(E value)
            {
                return Objects.equals(this.effectiveValue, value);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean isEqualToAny(E... values)
            {
                return StreamUtils.fromArray(values)
                                  .anyMatch(value -> this.isEqualTo(value));

            }
        };
    }

    private static class OptionalDecisionImpl<E extends Enum<E>, R> implements OptionalDecision<E, R>
    {
        private Decider<E>            decider;
        private Optional<Supplier<R>> resultSupplier;

        public OptionalDecisionImpl(Optional<Supplier<R>> resultSupplier, Decider<E> decider)
        {
            this.resultSupplier = resultSupplier;
            this.decider = decider;
        }

        @Override
        public OptionalDecision<E, R> orIfEqualTo(E value, Supplier<R> resultSupplier)
        {
            if (!this.resultSupplier.isPresent())
            {
                return this.decider.ifEqualTo(value, resultSupplier);
            }
            else
            {
                return this;
            }
        }

        @Override
        public OptionalDecision<E, R> orIfEqualToAny(Iterable<E> values, Function<List<E>, R> resultSupplier)
        {
            if (!this.resultSupplier.isPresent())
            {
                return this.decider.ifEqualToAny(values, resultSupplier);
            }
            else
            {
                return this;
            }
        }

        @Override
        public R get()
        {
            return this.resultSupplier.map(supplier -> supplier.get())
                                      .orElse(null);
        }

        @Override
        public R orElse(R result)
        {
            return this.resultSupplier.map(supplier -> supplier.get())
                                      .orElse(result);
        }

        @Override
        public R orElseGet(Supplier<R> resultSupplier)
        {
            return this.resultSupplier.map(supplier -> supplier.get())
                                      .orElseGet(resultSupplier);
        }

        @Override
        public <X extends Throwable> R orElseThrow(Supplier<? extends X> exceptionSupplier) throws X
        {
            return this.resultSupplier.map(supplier -> supplier.get())
                                      .orElseThrow(exceptionSupplier);
        }

    }

}
