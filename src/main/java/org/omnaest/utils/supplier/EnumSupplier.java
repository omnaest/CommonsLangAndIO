package org.omnaest.utils.supplier;

import java.util.function.Supplier;

/**
 * Supplier like {@link FunctionalInterface} which allows to attach {@link Enum} instances to it without any further need of implementing anything.
 * 
 * @see #toSupplier()
 * @author omnaest
 */
@FunctionalInterface
public interface EnumSupplier
{
    public String name();

    public default Supplier<String> toSupplier()
    {
        return () -> this.name();
    }

}
