/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
