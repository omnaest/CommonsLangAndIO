package org.omnaest.utils.supplier;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

import org.omnaest.utils.element.cached.CachedElement;

public class OptionalSupplierToIteratorAdapter<E> implements Iterator<E>
{
    private CachedElement<Optional<E>> cache;

    public OptionalSupplierToIteratorAdapter(Supplier<Optional<E>> supplier)
    {
        super();
        this.cache = CachedElement.of(supplier);
    }

    @Override
    public boolean hasNext()
    {
        return this.cache.get()
                         .isPresent();
    }

    @Override
    public E next()
    {
        return this.cache.getAndReset()
                         .get();
    }

}
