package org.omnaest.utils.stream;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.omnaest.utils.StreamUtils;

/**
 * @see SupplierStream
 * @see StreamUtils#fromSupplier(Supplier)
 * @author omnaest
 * @param <E>
 */
public class DefaultSupplierStream<E> extends StreamDecorator<E> implements SupplierStream<E>
{
    private Predicate<E> terminationMatcher = e -> false;
    private boolean      exclusive          = true;

    public DefaultSupplierStream(Supplier<E> supplier)
    {
        super(null);

        this.modifyStream(stream -> StreamUtils.fromIterator(new Iterator<E>()
        {
            private AtomicReference<E> takenElement = new AtomicReference<>();
            private AtomicBoolean      terminated   = new AtomicBoolean();

            @Override
            public boolean hasNext()
            {
                boolean previouslyTerminated = this.terminated.get();
                if (previouslyTerminated)
                {
                    return false;
                }
                else
                {
                    this.takeOneElement();
                    boolean notTerminated = DefaultSupplierStream.this.terminationMatcher.negate()
                                                                                         .test(this.takenElement.get());

                    this.terminated.compareAndSet(false, !notTerminated);

                    return !DefaultSupplierStream.this.exclusive || notTerminated;
                }
            }

            @Override
            public E next()
            {
                this.takeOneElement();
                return this.takenElement.getAndSet(null);
            }

            private void takeOneElement()
            {
                this.takenElement.getAndUpdate(e -> e != null ? e : supplier.get());
            }
        }));

    }

    @Override
    public SupplierStream<E> withTerminationMatcher(Predicate<E> terminationMatcher)
    {
        this.terminationMatcher = terminationMatcher;
        return this;
    }

    @Override
    public SupplierStream<E> withTerminationMatcherInclusive(Predicate<E> terminationMatcher)
    {
        this.terminationMatcher = terminationMatcher;
        this.exclusive = false;
        return this;
    }

}
