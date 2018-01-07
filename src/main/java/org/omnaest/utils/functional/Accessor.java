package org.omnaest.utils.functional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Combination of a {@link Supplier} and {@link Consumer}
 * 
 * @author omnaest
 * @param <E>
 */
public interface Accessor<E> extends Supplier<E>, Consumer<E>
{

    /**
     * Concatenates this {@link Accessor} with a {@link BidirectionalFunction} and provides a new {@link Accessor} based on that chain.
     * 
     * @param bidirectionalFunction
     * @return
     */
    public default <T> Accessor<T> with(BidirectionalFunction<T, E> bidirectionalFunction)
    {
        Supplier<T> supplier = () -> bidirectionalFunction.backward()
                                                          .apply(this.get());
        Consumer<T> consumer = element -> this.accept(bidirectionalFunction.forward()
                                                                           .apply(element));
        return new ConsumerSupplierAccessor<>(consumer, supplier);
    }

    /**
     * Similar to {@link #with(BidirectionalFunction)}
     * 
     * @param forward
     * @param backward
     * @return
     */
    public default <T> Accessor<T> with(Function<T, E> forward, Function<E, T> backward)
    {
        return with(BidirectionalFunction.of(forward, backward));
    }

    /**
     * Returns a new {@link Accessor} based on a {@link Supplier} and {@link Consumer}
     * 
     * @param supplier
     * @param consumer
     * @return
     */
    public static <E> Accessor<E> of(Supplier<E> supplier, Consumer<E> consumer)
    {
        return new ConsumerSupplierAccessor<>(consumer, supplier);
    }
}
