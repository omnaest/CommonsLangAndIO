package org.omnaest.utils.element.cached;

import java.util.function.Supplier;

public class SynchronizedCachedElementWrapper<E> implements CachedElement<E>
{
    private CachedElement<E> cachedElement;

    public SynchronizedCachedElementWrapper(CachedElement<E> cachedElement)
    {
        super();
        this.cachedElement = cachedElement;
    }

    @Override
    public synchronized E get()
    {
        return this.cachedElement.get();
    }

    @Override
    public synchronized E getAndReset()
    {
        return this.cachedElement.getAndReset();
    }

    @Override
    public synchronized CachedElement<E> reset()
    {
        return this.cachedElement.reset();
    }

    @Override
    public synchronized CachedElement<E> setSupplier(Supplier<E> supplier)
    {
        return this.cachedElement.setSupplier(supplier);
    }

    @Override
    public String toString()
    {
        return "SynchronizedCachedElementWrapper [cachedElement=" + this.cachedElement + "]";
    }

}
