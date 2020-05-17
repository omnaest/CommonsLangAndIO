package org.omnaest.utils.stream;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilterAllOnFirstFilterFailStreamDecorator<E> extends StreamDecorator<E>
{
    private AtomicBoolean terminate = new AtomicBoolean(false);

    public FilterAllOnFirstFilterFailStreamDecorator(Stream<E> stream)
    {
        super(stream);
    }

    @Override
    public Stream<E> filter(Predicate<? super E> predicate)
    {
        return super.filter(element ->
        {
            boolean result = !this.terminate.get() && predicate.test(element);
            this.terminate.set(!result);
            return result;
        });
    }

    @Override
    public String toString()
    {
        return "FilterAllOnFirstFilterFailStreamDecorator [terminate=" + this.terminate + "]";
    }

}
