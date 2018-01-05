package org.omnaest.utils.map;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.function.Supplier;

public interface SupplierMap<K, V, SK extends Supplier<K>, SV extends Supplier<V>> extends Map<SK, SV>
{

    /**
     * Enables the use of {@link SoftReference} caching for the keys
     * 
     * @return
     */
    public SupplierMap<K, V, SK, SV> useSoftReferenceCache();

    /**
     * Enables or disables the use of {@link SoftReference} caching for the keys
     * 
     * @param enabled
     * @return
     */
    public SupplierMap<K, V, SK, SV> useSoftReferenceCache(boolean enabled);

    /**
     * Returns true if {@link SoftReference}s are used to cache the keys
     * 
     * @return
     */
    public boolean isUsingSoftReferenceCache();

}
