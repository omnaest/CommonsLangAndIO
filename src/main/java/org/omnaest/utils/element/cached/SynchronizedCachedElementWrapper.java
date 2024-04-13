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
package org.omnaest.utils.element.cached;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class SynchronizedCachedElementWrapper<E> implements CachedElement<E>
{
    private CachedElement<E> cachedElement;

    public SynchronizedCachedElementWrapper(CachedElement<E> cachedElement)
    {
        super();
        this.cachedElement = cachedElement;
    }

    @Override
    public synchronized E get()
    {
        return this.cachedElement.get();
    }

    @Override
    public synchronized Optional<E> getIfCached()
    {
        return this.cachedElement.getIfCached();
    }

    @Override
    public synchronized E getAndReset()
    {
        return this.cachedElement.getAndReset();
    }

    @Override
    public synchronized CachedElement<E> reset()
    {
        return this.cachedElement.reset();
    }

    @Override
    public synchronized CachedElement<E> setSupplier(Supplier<E> supplier)
    {
        return this.cachedElement.setSupplier(supplier);
    }

    @Override
    public CachedElement<E> updateValue(UnaryOperator<E> updateFunction)
    {
        return this.cachedElement.updateValue(updateFunction);
    }

    @Override
    public String toString()
    {
        return "SynchronizedCachedElementWrapper [cachedElement=" + this.cachedElement + "]";
    }

}
