/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils.element;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

/**
 * A single cached element
 * 
 * @see #of(Supplier)
 * @author omnaest
 * @param <E>
 */
public interface CachedElement<E> extends Supplier<E>
{
    /**
     * Returns the cached element of if the cached element is null will resolve a new element from the {@link Supplier}
     */
    @Override
    public E get();

    /**
     * Returns the cached element and {@link #reset()}s the cache
     * 
     * @return
     */
    public E getAndReset();

    /**
     * Resets the cache, so that the next call to {@link #get()} will resolve a new element from the {@link Supplier}
     * 
     * @return
     */
    public CachedElement<E> reset();

    /**
     * Returns the underlying {@link Supplier} of the {@link CachedElement}
     * 
     * @return
     */
    public default Supplier<E> asNonCachedSupplier()
    {
        return () -> this.getAndReset();
    }

    /**
     * Returns a new {@link CachedElement} which uses a {@link SoftReference} and is not {@link Thread}safe
     * 
     * @param supplier
     * @return
     */
    public default CachedElement<E> asSoftReferenceCachedElement()
    {
        return new SoftCachedElementImpl<>(this.asNonCachedSupplier());
    }

    /**
     * Returns a new {@link CachedElement} which is {@link Thread}safe
     * 
     * @see #softOf(Supplier)
     * @param supplier
     * @return
     */
    public static <E> CachedElement<E> of(Supplier<E> supplier)
    {
        return new AtomicCachedElementImpl<>(supplier);
    }

}
