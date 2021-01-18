package org.omnaest.utils;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

import org.omnaest.utils.element.cached.CachedElement;

/**
 * Helper for operations regarding {@link Supplier}s
 * 
 * @author omnaest
 */
public class SupplierUtils
{
    /**
     * Similar to {@link #circularSupplierOf(Collection)}
     * 
     * @param elements
     * @return
     */
    @SafeVarargs
    public static <E> Supplier<E> circularSupplierOf(E... elements)
    {
        return circularSupplierOf(Arrays.asList(elements));
    }

    /**
     * Returns a circulating {@link Supplier} of the given {@link Collection}s elements
     * 
     * @see IteratorUtils#roundRobinIterator(Collection)
     * @param elements
     * @return
     */
    public static <E> Supplier<E> circularSupplierOf(Collection<E> elements)
    {
        return from(IteratorUtils.roundRobinIterator(elements));
    }

    /**
     * Returns a {@link Supplier} for a given {@link Iterator} which calls the {@link Iterator#hasNext()} and {@link Iterator#next()} method on every
     * {@link Supplier#get()} call. If the {@link Iterator#hasNext()} is false, null is returned
     * 
     * @param iterator
     * @return
     */
    public static <E> Supplier<E> from(Iterator<E> iterator)
    {
        return () -> iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Returns a {@link Supplier} around the given {@link Supplier} where a {@link SoftReference} does cache the {@link Object} returned from
     * {@link Supplier#get()}
     * 
     * @see CachedElement#asSoftReferenceCachedElement()
     * @param supplier
     * @return
     */
    public static <E> Supplier<E> toSoftReferenceCached(Supplier<E> supplier)
    {
        return CachedElement.of(supplier)
                            .asSoftReferenceCachedElement();
    }

    /**
     * Returns a {@link Supplier} that throws the {@link RuntimeException} provided by the given {@link Supplier} if the {@link Supplier#get()} method is
     * called.
     * 
     * @param exceptionSupplier
     * @return
     */
    public static <R> Supplier<R> toExceptionThrowingSupplier(Supplier<? extends RuntimeException> exceptionSupplier)
    {
        return () ->
        {
            throw exceptionSupplier.get();
        };
    }

}
