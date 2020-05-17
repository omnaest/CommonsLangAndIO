package org.omnaest.utils.functional;

import java.util.function.Supplier;

@FunctionalInterface
public interface Factory<T>
{
    public T newInstance();

    public default Supplier<T> asSupplier()
    {
        return () -> this.newInstance();
    }

    public static <T> Factory<T> from(Supplier<T> supplier)
    {
        return () -> supplier.get();
    }
}
