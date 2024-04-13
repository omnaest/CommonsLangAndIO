package org.omnaest.utils.optional;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Similar to {@link Optional} for two values
 * 
 * @author omnaest
 */
public class BiOptional<E1, E2>
{
    private Optional<E1> firstValue;
    private Optional<E2> secondValue;

    protected BiOptional(Optional<E1> firstValue, Optional<E2> secondValue)
    {
        super();
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public <R> Optional<R> map(BiFunction<E1, E2, R> mapper)
    {
        if (mapper != null && this.firstValue.isPresent() && this.secondValue.isPresent())
        {
            return Optional.ofNullable(mapper.apply(this.firstValue.get(), this.secondValue.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    public BiOptional<E1, E2> ifBothPresent(BiConsumer<E1, E2> consumer)
    {
        if (consumer != null && this.firstValue.isPresent() && this.secondValue.isPresent())
        {
            consumer.accept(this.firstValue.get(), this.secondValue.get());
        }
        return this;
    }

    public static <E1, E2> BiOptional<E1, E2> ofNullable(E1 firstValue, E2 secondValue)
    {
        return new BiOptional<>(Optional.ofNullable(firstValue), Optional.ofNullable(secondValue));
    }

    public static <E1, E2> BiOptional<E1, E2> of(E1 firstValue, E2 secondValue)
    {
        return new BiOptional<>(Optional.of(firstValue), Optional.of(secondValue));
    }

    public static <E1, E2> BiOptional<E1, E2> of(Optional<E1> firstValue, Optional<E2> secondValue)
    {
        return new BiOptional<>(firstValue, secondValue);
    }

}
