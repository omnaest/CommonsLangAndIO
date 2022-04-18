package org.omnaest.utils.functional;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author omnaest
 * @param <E>
 * @param <R>
 */
public interface OptionalFunction<E, R> extends Function<E, Optional<R>>
{
}
