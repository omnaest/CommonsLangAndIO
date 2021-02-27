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
package org.omnaest.utils.element.cached;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.SoftReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.duration.TimeDuration;
import org.omnaest.utils.functional.Provider;

/**
 * A single cached element
 * 
 * @see #of(Supplier)
 * @author omnaest
 * @param <E>
 */
public interface CachedElement<E> extends Provider<E>
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
     * Resets the {@link CachedElement} and gets a new element from the {@link Supplier}
     * 
     * @return
     */
    public default E resetAndGet()
    {
        return this.reset()
                   .get();
    }

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
     * Sets a new {@link Supplier} for the cached elements
     * 
     * @param supplier
     * @return
     */
    public CachedElement<E> setSupplier(Supplier<E> supplier);

    /**
     * Sets the {@link #setSupplier(Supplier)} fix to the given element. The {@link #get()} call will immediately return the set value.
     * 
     * @see #setSuppliedValueLazy(Object)
     * @param element
     * @return
     */
    public default CachedElement<E> setSuppliedValue(E element)
    {
        return this.setSupplier(() -> element)
                   .reset();
    }

    /**
     * In contrast to {@link #setSuppliedValue(Object)} this will not reset the current value of the {@link CachedElement} and will return the old value until
     * {@link #reset()} is called or any other cache evicting event does happen.
     * 
     * @see #setSuppliedValue(Object)
     * @param element
     * @return
     */
    public default CachedElement<E> setSuppliedValueLazy(E element)
    {
        return this.setSupplier(() -> element);
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
     * Returns a new {@link CachedElement} instances which wraps this one and enforces a cache eviction after a given {@link TimeDuration}
     * 
     * @param timeDuration
     * @return
     */
    public default CachedElement<E> asDurationLimitedCachedElement(TimeDuration timeDuration)
    {
        return new DurationCachedElement<>(this, timeDuration);
    }

    /**
     * Adds another file cache into the {@link Supplier} source chain.
     * 
     * @param file
     * @param serializer
     * @param deserializer
     * @return
     */
    public default CachedElement<E> withFileCache(File file, Function<E, String> serializer, Function<String, E> deserializer)
    {
        this.setSupplier(new FileCachedElementImpl<E>(this.asNonCachedSupplier(), file, serializer, deserializer));
        return this;
    }

    /**
     * Adds another file cache into the {@link Supplier} source chain.
     * 
     * @param file
     * @param serializer
     * @param deserializer
     * @return
     */
    public default CachedElement<E> withFileCache(File file, BiConsumer<E, Writer> serializer, Function<Reader, E> deserializer)
    {
        this.setSupplier(new FileCachedElementImpl<E>(this.asNonCachedSupplier(), file, serializer, deserializer));
        return this;
    }

    /**
     * Returns a new {@link CachedElement} which is {@link Thread}safe
     * 
     * @see #softOf(Supplier)
     * @param supplier
     * @return
     */
    public static <E> CachedElement<E> of(Supplier<? extends E> supplier)
    {
        return new AtomicCachedElementImpl<>(supplier);
    }

    /**
     * Returns a synchronized {@link CachedElement}
     * 
     * @return
     */
    public default CachedElement<E> asSynchronized()
    {
        return new SynchronizedCachedElementWrapper<>(this);
    }

}
