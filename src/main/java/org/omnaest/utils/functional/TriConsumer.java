package org.omnaest.utils.functional;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Similar to {@link BiConsumer} or {@link Consumer} but with three arguments
 * 
 * @author omnaest
 * @param <E1>
 * @param <E2>
 * @param <E3>
 */
@FunctionalInterface
public interface TriConsumer<E1, E2, E3>
{
    public void accept(E1 first, E2 second, E3 third);

    default TriConsumer<E1, E2, E3> andThen(TriConsumer<? super E1, ? super E2, ? super E3> after)
    {
        Objects.requireNonNull(after);
        return (f, s, t) ->
        {
            accept(f, s, t);
            after.accept(f, s, t);
        };
    }
}
