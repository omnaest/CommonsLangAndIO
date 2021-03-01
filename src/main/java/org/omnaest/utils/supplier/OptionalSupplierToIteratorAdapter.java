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

import org.omnaest.utils.element.cached.CachedElement;

public class OptionalSupplierToIteratorAdapter<E> implements Iterator<E>
{
    private CachedElement<Optional<E>> cache;

    public OptionalSupplierToIteratorAdapter(Supplier<Optional<E>> supplier)
    {
        super();
        this.cache = CachedElement.of(supplier);
    }

    @Override
    public boolean hasNext()
    {
        return this.cache.get()
                         .isPresent();
    }

    @Override
    public E next()
    {
        return this.cache.getAndReset()
                         .get();
    }

}
