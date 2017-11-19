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
package org.omnaest.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Helper for {@link Iterator}s
 * 
 * @author omnaest
 */
public class IteratorUtils
{
	public static <E> Iterator<E> roundRobinIterator(Collection<E> collection)
	{
		Supplier<Iterator<E>> supplier = () -> collection.iterator();
		return new Iterator<E>()
		{
			private AtomicReference<Iterator<E>> currentIterator = new AtomicReference<>();

			@Override
			public boolean hasNext()
			{
				return !collection.isEmpty();
			}

			@Override
			public E next()
			{
				E retval = null;

				do
				{
					Iterator<E> iterator = this.currentIterator.get();
					try
					{
						if (iterator == null || !iterator.hasNext())
						{
							this.resetIterator();
						}

						retval = this.currentIterator	.get()
														.next();
					} catch (ConcurrentModificationException e)
					{
						this.resetIterator();
					}
				} while (retval == null);

				return retval;
			}

			private void resetIterator()
			{
				this.currentIterator.set(supplier.get());
			}
		};
	}

	public static <E> List<E> drain(Iterator<E> iterator, int size)
	{
		List<E> retlist = new ArrayList<>();

		for (int ii = 0; ii < size && iterator.hasNext(); ii++)
		{
			retlist.add(iterator.next());
		}

		return retlist;
	}

	public static <E> List<E> drain(Iterator<E> iterator, Predicate<E> terminateDrainPredicate)
	{
		List<E> retlist = new ArrayList<>();

		boolean terminate = false;
		while (!terminate && iterator.hasNext())
		{
			E element = iterator.next();
			terminate = terminateDrainPredicate.test(element);
			retlist.add(element);
		}

		return retlist;
	}

	/**
	 * Returns an {@link Iterator} based on the given {@link Iterator}, but attaches a {@link Consumer} to the {@link Iterator#next()} method
	 * 
	 * @param iterator
	 * @param consumer
	 * @return
	 */
	public static <E> Iterator<E> withConsumerListener(Iterator<E> iterator, Consumer<E> consumer)
	{
		return new Iterator<E>()
		{
			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public E next()
			{
				E element = iterator.next();
				consumer.accept(element);
				return element;
			}

			@Override
			public void remove()
			{
				iterator.remove();
			}

		};
	}

	/**
	 * Returns an {@link Iterable} around the given {@link Iterator} {@link Supplier}
	 * 
	 * @param iterator
	 * @return
	 */
	public static <E> Iterable<E> toIterable(Supplier<Iterator<E>> iterator)
	{
		return new Iterable<E>()
		{
			@Override
			public Iterator<E> iterator()
			{
				return iterator.get();
			}
		};
	}
}
