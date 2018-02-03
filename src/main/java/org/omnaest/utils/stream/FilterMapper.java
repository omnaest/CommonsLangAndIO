package org.omnaest.utils.stream;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link FilterMapper} is a combined implementation of a {@link Predicate} and a {@link Function} and can be used in combinations with {@link Stream}s
 * 
 * @author omnaest
 * @param <T>
 * @param <R>
 */
public interface FilterMapper<T, R> extends Predicate<T>, Function<T, R>
{

}
