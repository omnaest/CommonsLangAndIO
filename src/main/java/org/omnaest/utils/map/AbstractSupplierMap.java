package org.omnaest.utils.map;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.SupplierUtils;
import org.omnaest.utils.map.AbstractSupplierMap.KeySupplier;

public class AbstractSupplierMap<K, V, SK extends Supplier<K>, SV extends Supplier<V>> extends MappingMapDecorator<SK, KeySupplier<K>, SV, Supplier<V>>
        implements SupplierMap<K, V, SK, SV>
{
    private boolean usingSoftReferenceCache = true;

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

    public AbstractSupplierMap(Function<Supplier<K>, SK> keySupplierFunction, Function<Supplier<V>, SV> valueSupplierFunction)
    {
        this(() -> new HashMap<>(), keySupplierFunction, valueSupplierFunction);
    }

    public AbstractSupplierMap(Supplier<Map<KeySupplier<K>, Supplier<V>>> sourceMap, Function<Supplier<K>, SK> keySupplierFunction,
                               Function<Supplier<V>, SV> valueSupplierFunction)
    {
        super(sourceMap, k -> keySupplierFunction.apply(k), k -> new KeySupplier<>(k), v -> valueSupplierFunction.apply(v), v -> v, k -> new KeySupplier<>(k),
                v -> v);
    }

    public boolean isUsingSoftReferenceCache()
    {
        return this.usingSoftReferenceCache;
    }

    /**
     * Enables the use of {@link SoftReference}s as key caches
     * 
     * @return
     */
    public AbstractSupplierMap<K, V, SK, SV> useSoftReferenceCache()
    {
        return this.useSoftReferenceCache(true);
    }

    /**
     * Similar to {@link #useSoftReferenceCache()}
     * 
     * @param enabled
     * @return
     */
    public AbstractSupplierMap<K, V, SK, SV> useSoftReferenceCache(boolean enabled)
    {
        this.usingSoftReferenceCache = enabled;
        this.applySoftReferenceCache();
        return this;
    }

    private void applySoftReferenceCache()
    {
        if (this.usingSoftReferenceCache)
        {
            this.keyToReadableSourceMapper = k -> new KeySupplier<>(SupplierUtils.toSoftReferenceCached(k));
            this.keyToWritableSourceMapper = k -> new KeySupplier<>(SupplierUtils.toSoftReferenceCached(k));
        }
        else
        {
            this.keyToReadableSourceMapper = k -> new KeySupplier<>(k);
            this.keyToWritableSourceMapper = k -> new KeySupplier<>(k);
        }
    }

}
