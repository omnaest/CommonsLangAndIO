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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @see CachedElement#of(Supplier)
 * @author omnaest
 * @param <E>
 */
public class AtomicCachedElementImpl<E> implements CachedElement<E>
{
	private AtomicReference<E>	element	= new AtomicReference<E>();
	private Supplier<E>			supplier;

	public AtomicCachedElementImpl(Supplier<E> supplier)
	{
		super();
		this.supplier = supplier;
	}

	@Override
	public E get()
	{
		E retval = this.element.get();
		if (retval == null)
		{
			retval = this.element.updateAndGet(e -> e == null ? this.supplier.get() : e);
		}
		return retval;
	}

	@Override
	public CachedElement<E> reset()
	{
		this.element.set(null);
		return this;
	}

}