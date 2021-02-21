package org.omnaest.utils.stream;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Similar to {@link Iterable} but provides additionally the {@link #stream()} method which is to be implemented.
 * 
 * @see Stream
 * @author omnaest
 * @param <E>
 */
public interface Streamable<E> extends Iterable<E>
{
    public Stream<E> stream();

    /**
     * Default implementation based on the {@link #stream()} method and the {@link Stream#iterator()}.
     * 
     * @return
     */
    @Override
    public default Iterator<E> iterator()
    {
        return this.stream()
                   .iterator();
    }
}
