package org.omnaest.utils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.omnaest.utils.functional.Action;

public class EnumUtils
{

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
