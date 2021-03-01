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
package org.omnaest.utils.supplier;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.omnaest.utils.StreamUtils;

/**
 * {@link Supplier} which returns an {@link Optional}. This allows to generate {@link Iterator} like {@link Stream}s
 * 
 * @see #of(Iterable)
 * @see #of(Iterator)
 * @see #iterator()
 * @see #stream()
 * @author omnaest
 * @param <E>
 */
public interface OptionalSupplier<E> extends Supplier<Optional<E>>, Iterable<E>
{

    /**
     * Returns an {@link OptionalSupplier} based on the given {@link Iterator}
     * 
     * @param iterator
     * @return
     */
    public static <E> OptionalSupplier<E> of(Iterator<E> iterator)
    {
        return new IteratorToOptionalSupplierAdapter<>(iterator);
    }

    /**
     * Returns an {@link OptionalSupplier} based on the given {@link Iterable#iterator()}
     * 
     * @param iterable
     * @return
     */
    public static <E> OptionalSupplier<E> of(Iterable<E> iterable)
    {
        return of(iterable.iterator());
    }

    /**
     * Returns an {@link Iterator} over the given {@link OptionalSupplier}
     * 
     * @return
     */
    @Override
    public default Iterator<E> iterator()
    {
        return new OptionalSupplierToIteratorAdapter<>(this);
    }

    /**
     * Returns a {@link Stream} based on this {@link OptionalSupplier}
     * 
     * @return
     */
    public default Stream<E> stream()
    {
        return StreamUtils.fromOptionalSupplier(this);
    }
}
