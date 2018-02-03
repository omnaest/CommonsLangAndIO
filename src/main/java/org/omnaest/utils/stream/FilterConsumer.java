package org.omnaest.utils.stream;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Combination of a {@link Predicate} and {@link Consumer} which can be used in combinations with {@link Stream}s
 * 
 * @author omnaest
 * @param <T>
 */
public interface FilterConsumer<T> extends Predicate<T>, Consumer<T>
{

}
