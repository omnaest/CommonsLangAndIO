package org.omnaest.utils.element.transactional.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.element.transactional.TransactionalElement;
import org.omnaest.utils.functional.UnaryBiFunction;

/**
 * @see TransactionalElement
 * @author omnaest
 * @param <E>
 */
public class DefaultTransactionalElement<E> implements TransactionalElement<E>
{
    private final CachedElement<E>   staging;
    private final AtomicReference<E> active = new AtomicReference<>();
    private Supplier<E>              supplier;

    public DefaultTransactionalElement(Supplier<E> supplier)
    {
        this.supplier = supplier;
        this.staging = this.createStagingInstance(supplier);
    }

    protected CachedElement<E> createStagingInstance(Supplier<E> supplier)
    {
        return CachedElement.of(supplier);
    }

    @Override
    public E getStaging()
    {
        return this.staging.get();
    }

    @Override
    public TransactionalElement<E> setStagingValue(E value)
    {
        this.staging.set(value);
        return this;
    }

    @Override
    public TransactionalElement<E> updateStaging(UnaryOperator<E> updateFunction)
    {
        this.staging.updateValue(updateFunction);
        return this;
    }

    @Override
    public TransactionalElement<E> mergeActiveIntoStaging(UnaryBiFunction<E> updateFunction)
    {
        this.staging.resolve()
                    .updateValue(staging -> updateFunction.apply(staging, this.active.get()));
        return this;
    }

    @Override
    public TransactionCommit<E> withFinalMergeFunction(UnaryBiFunction<E> mergeFunction)
    {
        return new TransactionCommit<E>()
        {
            @Override
            public TransactionalElement<E> commit()
            {
                DefaultTransactionalElement.this.active.updateAndGet(previous -> mergeFunction.apply(DefaultTransactionalElement.this.staging.getAndReset(),
                                                                                                     previous));
                return DefaultTransactionalElement.this;
            }
        };
    }

    @Override
    public E getActive()
    {
        return this.active.get();
    }

    @Override
    public TransactionalElement<E> commit()
    {
        this.active.set(this.staging.getAndReset());
        return this;
    }

    @Override
    public Transaction<E> transaction()
    {
        CachedElement<E> staging = this.createStagingInstance(this.supplier);
        AtomicReference<E> active = this.active;
        return new Transaction<E>()
        {
            @Override
            public <R> TransactionFinalizer<E, R> execute(Callable<R> operation)
            {
                try
                {
                    R result = operation.call();
                    return new TransactionFinalizer<E, R>()
                    {
                        private UnaryBiFunction<E> mergeFunction = (staging, previousActive) -> staging;

                        @Override
                        public TransactionFinalizer<E, R> withStagingAndActiveMergeFunction(UnaryBiFunction<E> mergeFunction)
                        {
                            this.mergeFunction = mergeFunction;
                            return this;
                        }

                        @Override
                        public R commit()
                        {
                            active.updateAndGet(previous -> this.mergeFunction.apply(staging.getAndReset(), previous));
                            return result;
                        }
                    };
                }
                catch (Exception e)
                {
                    throw new TransactionFailedException(e);
                }
            }
        };
    }

    @Override
    public Supplier<E> getSupplier()
    {
        return this.supplier;
    }

}
