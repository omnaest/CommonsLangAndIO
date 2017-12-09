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
package org.omnaest.utils.list;

import java.util.List;
import java.util.ListIterator;

/**
 * @see ListIterator
 * @author omnaest
 * @param <E>
 */
public class DefaultListIterator<E> implements ListIterator<E>
{
	private int		position	= -1;
	private List<E>	list;

	public DefaultListIterator(List<E> list)
	{
		this.list = list;
	}

	public DefaultListIterator(int index, List<E> list)
	{
		super();
		this.position = index - 1;
		this.list = list;
	}

	@Override
	public boolean hasNext()
	{
		return this.position + 1 < this.list.size();
	}

	@Override
	public E next()
	{
		return this.list.get(++this.position);
	}

	@Override
	public void remove()
	{
		this.list.remove(this.position);
	}

	@Override
	public boolean hasPrevious()
	{
		return this.position > 0;
	}

	@Override
	public E previous()
	{
		return this.list.get(--this.position);
	}

	@Override
	public int nextIndex()
	{
		return this.position + 1;
	}

	@Override
	public int previousIndex()
	{
		return this.position - 1;
	}

	@Override
	public void set(E element)
	{
		this.list.set(this.position, element);

	}

	@Override
	public void add(E element)
	{
		this.list.add(this.position, element);
	}
}