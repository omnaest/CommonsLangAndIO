package org.omnaest.utils.optional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

/**
 * In contrast to {@link Optional} a {@link NullOptional} can also return a null value at the end of the chain.
 * If {@link #mapToNullable(Function)} is called, also a returned null value will not prevent the {@link #isPresent()} function to return true if it was true
 * before.
 * 
 * @see Optional
 * @see #mapToNullable(Function)
 * @see #asOptional()
 * @author omnaest
 * @param <T>
 */
public class NullOptional<T>
{
    private static final NullOptional<?> EMPTY = new NullOptional<>(null, false);

    private T       value;
    private boolean present;

    protected NullOptional(T value, boolean present)
    {
        super();
        this.value = value;
        this.present = present;
    }

    public static <T> NullOptional<T> ofNullable(T value)
    {
        return value == null ? empty() : new NullOptional<>(value, true);
    }

    /**
     * Returns a {@link NullOptional} which will return true for {@link #isPresent()} even if a null value is supplied.
     * 
     * @see #ofPresenceAndNullable(boolean, Object)
     * @param value
     * @return
     */
    public static <T> NullOptional<T> ofPresentNullable(T value)
    {
        return new NullOptional<>(value, true);
    }

    /**
     * Returns a {@link NullOptional} that returns true for {@link #isPresent()} if the parameter present is true. This allows to define the exact state of the
     * {@link NullOptional}.<br>
     * If present is false the value {@link Supplier} is not called and null is used as value.
     * 
     * @param present
     * @param value
     * @return
     */
    public static <T> NullOptional<T> ofPresenceAndNullable(boolean present, Supplier<T> valueSupplier)
    {
        return new NullOptional<>(present ? valueSupplier.get() : null, present);
    }

    public static <T> NullOptional<T> of(Optional<T> optional)
    {
        return new NullOptional<>(optional.orElse(null), optional.isPresent());
    }

    @SuppressWarnings("unchecked")
    public static <T> NullOptional<T> empty()
    {
        return (NullOptional<T>) EMPTY;
    }

    public Optional<T> asOptional()
    {
        return Optional.ofNullable(this.value);
    }

    /**
     * Returns a {@link Stream} representation which is {@link Stream#empty()} if {@link #isPresent()} returns false.
     * 
     * @return
     */
    public Stream<T> asStream()
    {
        return this.isPresent() ? Stream.of(this.value) : Stream.empty();
    }

    public boolean isPresent()
    {
        return this.present;
    }

    public NullOptional<T> ifPresent(Consumer<? super T> consumer)
    {
        if (this.isPresent())
        {
            consumer.accept(this.value);
        }
        return this;
    }

    public NullOptional<T> ifNotPresent(Consumer<? super T> consumer)
    {
        if (!this.isPresent())
        {
            consumer.accept(this.value);
        }
        return this;
    }

    /**
     * Returns an {@link NullOptional} where {@link NullOptional#isPresent()} will only return true if also the mapped return value is not null.
     * 
     * @see #mapToNullable(Function)
     * @param mapper
     * @return
     */
    public <U> NullOptional<U> map(Function<? super T, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper);
        if (!this.isPresent())
        {
            return empty();
        }
        else
        {
            return NullOptional.ofNullable(mapper.apply(this.value));
        }
    }

    /**
     * Allows to map to a null value but {@link #isPresent()} still returns true
     * 
     * @see #map(Function)
     * @param mapper
     * @return
     */
    public <U> NullOptional<U> mapToNullable(Function<? super T, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper);
        if (!this.isPresent())
        {
            return empty();
        }
        else
        {
            return new NullOptional<U>(mapper.apply(this.value), true);
        }
    }

    @SuppressWarnings("unchecked")
    public <U> NullOptional<U> flatMap(Function<? super T, NullOptional<? extends U>> mapper)
    {
        Objects.requireNonNull(mapper);
        if (!this.isPresent())
        {
            return empty();
        }
        else
        {
            return (NullOptional<U>) mapper.apply(this.value);
        }
    }

    public NullOptional<T> filter(Predicate<? super T> predicate)
    {
        Objects.requireNonNull(predicate);
        if (!this.isPresent())
        {
            return this;
        }
        else
        {
            return predicate.test(this.value) ? this : empty();
        }
    }

    public T orElse(T other)
    {
        return this.isPresent() ? this.value : other;
    }

    public T orElseGet(Supplier<? extends T> other)
    {
        return this.isPresent() ? this.value : other.get();
    }

    public NullOptional<T> orElseGetOptional(Supplier<NullOptional<T>> other)
    {
        return this.isPresent() ? this : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X
    {
        if (this.value != null)
        {
            return this.value;
        }
        else
        {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof NullOptional))
        {
            return false;
        }

        NullOptional<?> other = (NullOptional<?>) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.value);
    }

    @Override
    public String toString()
    {
        return this.isPresent() ? String.format("NullOptional[%s]", this.value) : "NullOptional.empty";
    }

    /**
     * Returns the internal value if {@link #isPresent()} is true otherwise raises an {@link NoSuchElementException}. This can also return a null value if e.g.
     * {@link #mapToNullable(Function)} is called in between.
     * 
     * @return
     */
    public T get()
    {
        if (!this.isPresent())
        {
            throw new NoSuchElementException("No value present");
        }

        return this.value;
    }

    /**
     * Returns a non empty {@link NullOptional} if both given values are not null
     * 
     * @param firstValue
     * @param secondValue
     * @return
     */
    public static <T1, T2> NullOptional<BiElement<T1, T2>> ofTwoNullable(T1 firstValue, T2 secondValue)
    {
        return ofNullable(BiElement.of(firstValue, secondValue)).filter(bi -> bi.isFirstValueNotNull())
                                                                .filter(bi -> bi.isSecondValueNotNull());
    }

    /**
     * Returns a new {@link NullOptional} instance generated by {@link #ofNullable(Object)} and the element returned from {@link Supplier#get()} of the given
     * {@link Supplier}, if the current {@link NullOptional} state is empty. Otherwise the current {@link NullOptional} instance is returned.
     * 
     * @see #orElseGet(Supplier)
     * @see #ofNullable(Object)
     * @param supplier
     * @return
     */
    public NullOptional<T> orElseMapNullable(Supplier<T> supplier)
    {
        return this.isPresent() ? this : ofNullable(supplier.get());
    }

    /**
     * Returns a new {@link NullOptional} instance generated by the given {@link Supplier} if the current {@link NullOptional} state is empty. Otherwise the
     * current instance is returned.
     * 
     * @see #orElseMapNullable(Supplier)
     * @see #orElseGet(Supplier)
     * @param supplier
     * @return
     */
    public NullOptional<T> orElseFlatMap(Supplier<NullOptional<T>> supplier)
    {
        return this.isPresent() ? this : supplier.get();
    }
}
