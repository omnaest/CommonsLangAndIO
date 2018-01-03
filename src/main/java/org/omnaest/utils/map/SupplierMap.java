package org.omnaest.utils.map;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.omnaest.utils.map.SupplierMap.KeySupplier;

public class SupplierMap<K, V> extends MappingMapDecorator<Supplier<K>, KeySupplier<K>, Supplier<V>, Supplier<V>>
{
    public static class KeySupplier<K> implements Supplier<K>, Comparable<Supplier<K>>
    {
        private Supplier<K> supplier;

        public KeySupplier(Supplier<K> supplier)
        {
            super();
            this.supplier = supplier;
        }

        @Override
        public K get()
        {
            return this.supplier.get();
        }

        @Override
        public int hashCode()
        {
            return this.supplier.get()
                                .hashCode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof Supplier && this.supplier.get()
                                                           .equals(((Supplier<K>) obj).get());
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(Supplier<K> o)
        {
            return ((Comparable<K>) this.supplier.get()).compareTo(o.get());
        }

        @Override
        public String toString()
        {
            return "KeySupplier [supplier=" + this.supplier + ", get()=" + this.get() + "]";
        }

    }

    public SupplierMap()
    {
        this(() -> new HashMap<>());
    }

    public SupplierMap(Supplier<Map<KeySupplier<K>, Supplier<V>>> sourceMap)
    {
        super(sourceMap, k -> k, k -> new KeySupplier<>(k), v -> v, v -> v, k -> new KeySupplier<>(k), v -> v);
    }

}
