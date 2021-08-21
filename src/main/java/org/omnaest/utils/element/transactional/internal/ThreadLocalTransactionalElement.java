package org.omnaest.utils.element.transactional.internal;

import java.util.function.Supplier;

import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.element.transactional.TransactionalElement;

/**
 * @see TransactionalElement
 * @author omnaest
 * @param <E>
 */
public class ThreadLocalTransactionalElement<E> extends DefaultTransactionalElement<E>
{

    public ThreadLocalTransactionalElement(Supplier<E> supplier)
    {
        super(supplier);
    }

    @Override
    protected CachedElement<E> createStagingInstance(Supplier<E> supplier)
    {
        return super.createStagingInstance(supplier).asThreadLocal();
    }

}
