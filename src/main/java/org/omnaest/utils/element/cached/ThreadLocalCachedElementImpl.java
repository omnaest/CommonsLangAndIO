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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @see CachedElement#of(Supplier)
 * @author omnaest
 * @param <E>
 */
public class ThreadLocalCachedElementImpl<E> implements CachedElement<E>
{
    private ThreadLocal<E>               element  = new ThreadLocal<E>();
    private AtomicReference<Supplier<E>> supplier = new AtomicReference<>();

    public ThreadLocalCachedElementImpl(Supplier<E> supplier)
    {
        super();
        this.supplier.set(supplier);
    }

    @Override
    public E get()
    {
        E retval = this.element.get();
        retval = this.getFromSupplierIfNull(retval);
        return retval;
    }

    @Override
    public Optional<E> getIfCached()
    {
        E retval = this.element.get();
        return Optional.ofNullable(retval);
    }

    private E getFromSupplierIfNull(E retval)
    {
        if (retval == null)
        {
            E currentValue = this.element.get();
            this.element.set(currentValue == null ? this.supplier.get()
                                                                 .get()
                    : currentValue);
            retval = this.element.get();
        }
        return retval;
    }

    private E getFromSupplierIfNullWithoutCacheUpdate(E retval)
    {
        if (retval == null)
        {
            retval = this.supplier.get()
                                  .get();
        }
        return retval;
    }

    @Override
    public E getAndReset()
    {
        E retval = this.element.get();
        this.element.set(null);
        retval = this.getFromSupplierIfNullWithoutCacheUpdate(retval);
        return retval;
    }

    @Override
    public CachedElement<E> reset()
    {
        this.element.set(null);
        return this;
    }

    @Override
    public CachedElement<E> setSupplier(Supplier<E> supplier)
    {
        this.supplier.set(supplier);
        return this;
    }

    @Override
    public String toString()
    {
        return "AtomicCachedElementImpl [element=" + this.element + ", supplier=" + this.supplier + "]";
    }

    @Override
    public Supplier<E> asNonCachedSupplier()
    {
        return this.supplier.get();
    }

    @Override
    public CachedElement<E> updateValue(UnaryOperator<E> updateFunction)
    {
        this.element.set(updateFunction.apply(this.element.get()));
        return this;
    }

}
