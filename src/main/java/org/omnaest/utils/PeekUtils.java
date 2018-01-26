package org.omnaest.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Helper for {@link Stream#peek(java.util.function.Consumer)}
 * 
 * @author omnaest
 */
public class PeekUtils
{
    /**
     * Returns a {@link Consumer} with an internal {@link AtomicLong} counter, which gets incremented for each call
     * 
     * @param consumer
     * @return
     */
    public static <E> Consumer<E> counter(Consumer<Long> consumer)
    {
        AtomicLong counter = new AtomicLong();
        return e -> consumer.accept(counter.getAndIncrement());
    }
}
