package org.omnaest.utils.element.transactional;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.element.transactional.internal.DefaultTransactionalElement;
import org.omnaest.utils.element.transactional.internal.ThreadLocalTransactionalElement;
import org.omnaest.utils.functional.UnaryBiFunction;

/**
 * A {@link TransactionalElement} contains a {@link #getStaging()} and a {@link #getActive()} value. The {@link #getStaging()} value does become the
 * {@link #getActive()} value when the {@link #commit()} method is called.
 * 
 * @author omnaest
 * @param <E>
 */
public interface TransactionalElement<E>
{
    public E getStaging();

    public TransactionalElement<E> setStagingValue(E value);

    public TransactionalElement<E> updateStaging(UnaryOperator<E> updateFunction);

    public E getActive();

    public TransactionalElement<E> mergeActiveIntoStaging(UnaryBiFunction<E> mergeFunction);

    public TransactionCommit<E> withFinalMergeFunction(UnaryBiFunction<E> mergeFunction);

    public static interface TransactionCommit<E>
    {
        public TransactionalElement<E> commit();
    }

    /**
     * Commits the staging value and makes it the active value
     * 
     * @return
     */
    public TransactionalElement<E> commit();

    public Supplier<E> getSupplier();

    public static <E> TransactionalElement<E> of(Supplier<E> supplier)
    {
        return new DefaultTransactionalElement<E>(supplier);
    }

    public default TransactionalElement<E> asThreadLocalStaged()
    {
        return new ThreadLocalTransactionalElement<>(this.getSupplier());
    }

    public Transaction<E> transaction();

    public static interface Transaction<E>
    {
        /**
         * @throws TransactionFailedException
         *             if the {@link Callable} operation throws any {@link Exception}
         * @param operation
         * @return
         */
        public <R> TransactionFinalizer<E, R> execute(Callable<R> operation);
    }

    public static interface TransactionFinalizer<E, R>
    {
        public TransactionFinalizer<E, R> withStagingAndActiveMergeFunction(UnaryBiFunction<E> mergeFunction);

        public R commit();
    }

    public static class TransactionFailedException extends RuntimeException
    {
        private static final long serialVersionUID = -1690082555724072416L;

        public TransactionFailedException(Throwable cause)
        {
            super(cause);
        }
    }
}
