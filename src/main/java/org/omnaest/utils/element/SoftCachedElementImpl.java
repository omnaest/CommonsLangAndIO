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
 * @see CachedElement#of(Supplier)
 * @author omnaest
 * @param <E>
 */
public class SoftCachedElementImpl<E> implements CachedElement<E>
{
    private volatile SoftReference<E> element = new SoftReference<E>(null);
    private Supplier<E>               supplier;

    public SoftCachedElementImpl(Supplier<E> supplier)
    {
        super();
        this.supplier = supplier;
    }

    @Override
    public E get()
    {
        E retval = this.element.get();
        retval = this.getFromSupplierIfNull(retval);
        return retval;
    }

    private E getFromSupplierIfNull(E retval)
    {
        if (retval == null)
        {
            retval = this.supplier.get();
            this.element = new SoftReference<E>(retval);
        }
        return retval;
    }

    @Override
    public E getAndReset()
    {
        E retval = this.element.get();
        this.element.clear();
        retval = this.getFromSupplierIfNull(retval);
        return retval;
    }

    @Override
    public CachedElement<E> reset()
    {
        this.element.clear();
        return this;
    }

    @Override
    public String toString()
    {
        return "SoftCachedElementImpl [element=" + this.element + ", supplier=" + this.supplier + "]";
    }

}
