package org.omnaest.utils.stream;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link Stream} which is based on a given {@link Supplier}
 * 
 * @see #withTerminationMatcher(Predicate)
 * @see #withTerminationMatcherInclusive(Predicate)
 * @author omnaest
 * @param <E>
 */
public interface SupplierStream<E> extends Stream<E>
{
    /**
     * Terminates the {@link Stream} if the {@link Predicate#test(Object)} is true excluding the matched element
     * 
     * @param terminationMatcher
     * @return
     */
    public SupplierStream<E> withTerminationMatcher(Predicate<E> terminationMatcher);

    /**
     * Terminates the {@link Stream} if the {@link Predicate#test(Object)} is true including the matched element
     * 
     * @param terminationMatcher
     * @return
     */
    public SupplierStream<E> withTerminationMatcherInclusive(Predicate<E> terminationMatcher);
}
