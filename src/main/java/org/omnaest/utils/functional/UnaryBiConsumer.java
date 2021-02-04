package org.omnaest.utils.functional;

import java.util.function.BiConsumer;

/**
 * Unary version of the {@link BiConsumer}
 * 
 * @author omnaest
 * @param <E>
 */
public interface UnaryBiConsumer<E> extends BiConsumer<E, E>
{
}
