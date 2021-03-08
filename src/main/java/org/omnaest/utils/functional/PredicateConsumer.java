package org.omnaest.utils.functional;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Combination of {@link Predicate} and {@link Consumer}
 * 
 * @see #of(Predicate, Consumer)
 * @author omnaest
 * @param <E>
 */
public interface PredicateConsumer<E> extends Predicate<E>, Consumer<E>
{

    /**
     * Returns a new {@link PredicateConsumer} instance based on the given {@link Predicate} and {@link Consumer}
     * 
     * @param predicate
     * @param consumer
     * @return
     */
    public static <E> PredicateConsumer<E> of(Predicate<E> predicate, Consumer<E> consumer)
    {
        return new PredicateConsumer<E>()
        {
            @Override
            public void accept(E element)
            {
                consumer.accept(element);
            }

            @Override
            public boolean test(E element)
            {
                return predicate.test(element);
            }
        };
    }
}
