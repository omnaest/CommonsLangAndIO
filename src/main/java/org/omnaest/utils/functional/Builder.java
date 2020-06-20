package org.omnaest.utils.functional;

import java.util.function.Supplier;

@FunctionalInterface
public interface Builder<T>
{
    public T build();

    /**
     * @see Supplier
     * @return
     */
    public default Supplier<T> asSupplier()
    {
        return () -> this.build();
    }

    /**
     * @see Factory
     * @return
     */
    public default Factory<T> asFactory()
    {
        return () -> this.build();
    }
}
