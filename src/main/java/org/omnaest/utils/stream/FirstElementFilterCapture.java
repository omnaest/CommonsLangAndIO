package org.omnaest.utils.stream;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Filters the first tested element ( this{@link #test(Object)} returns false ) and captures it, so that it can be retrieved via {@link #get()}
 * <br>
 * <br>
 * A {@link FirstElementFilterCapture} can be used in combination with {@link Stream#filter(Predicate)}
 * 
 * @author omnaest
 * @param <E>
 */
public class FirstElementFilterCapture<E> implements Predicate<E>, Supplier<E>
{
    private AtomicReference<E> element  = new AtomicReference<>();
    private AtomicBoolean      captured = new AtomicBoolean();

    @Override
    public boolean test(E element)
    {
        boolean updated = this.captured.compareAndSet(false, true);
        if (updated)
        {
            this.element.set(element);
        }
        return !updated;
    }

    @Override
    public E get()
    {
        return this.element.get();
    }
}