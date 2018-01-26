package org.omnaest.utils.supplier;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @see Supplier
 * @see Consumer
 * @author omnaest
 * @param <E>
 */
public interface SupplierConsumer<E> extends Supplier<E>, Consumer<E>
{
}
