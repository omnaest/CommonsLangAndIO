package org.omnaest.utils.supplier;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * {@link OptionalSupplier} which is based on an {@link Iterator}
 * 
 * @author omnaest
 * @param <E>
 */
public class IteratorToOptionalSupplierAdapter<E> implements OptionalSupplier<E>
{
    private Iterator<E> iterator;

    public IteratorToOptionalSupplierAdapter(Iterator<E> iterator)
    {
        super();
        this.iterator = iterator;
    }

    @Override
    public Optional<E> get()
    {
        E value = null;
        if (this.iterator.hasNext())
        {
            try
            {
                value = this.iterator.next();
            }
            catch (NoSuchElementException e)
            {
                //do nothing
            }
        }
        return Optional.ofNullable(value);
    }

}
