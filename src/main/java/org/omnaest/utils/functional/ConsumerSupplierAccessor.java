package org.omnaest.utils.functional;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConsumerSupplierAccessor<E> implements Accessor<E>
{
    private Consumer<E> consumer;
    private Supplier<E> supplier;

    public ConsumerSupplierAccessor(Consumer<E> consumer, Supplier<E> supplier)
    {
        super();
        this.consumer = consumer;
        this.supplier = supplier;
    }

    @Override
    public void accept(E t)
    {
        this.consumer.accept(t);
    }

    @Override
    public E get()
    {
        return this.supplier.get();
    }

    @Override
    public String toString()
    {
        return "ConsumerSupplierAccessor [consumer=" + this.consumer + ", supplier=" + this.supplier + "]";
    }

}
