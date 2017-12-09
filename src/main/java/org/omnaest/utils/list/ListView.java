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

public class ListView<E> extends AbstractList<E>
{
	private List<E>	list;
	private int		fromIndex;
	private int		toIndex;

	public ListView(int fromIndex, int toIndex, List<E> list)
	{
		super();
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		this.list = list;
	}

	@Override
	public E get(int index)
	{
		int effectiveIndex = this.determineEffectiveIndex(index);
		this.assertValidEffectiveIndex(effectiveIndex);
		return this.list.get(effectiveIndex);
	}

	private void assertValidEffectiveIndex(int effectiveIndex)
	{
		if (effectiveIndex < 0 || effectiveIndex > this.size() - 1)
		{
			throw new IndexOutOfBoundsException();
		}
	}

	private int determineEffectiveIndex(int index)
	{
		return this.fromIndex + index;
	}

	@Override
	public E set(int index, E element)
	{
		int effectiveIndex = this.determineEffectiveIndex(index);
		this.assertValidEffectiveIndex(effectiveIndex);
		return this.list.set(effectiveIndex, element);
	}

	@Override
	public void add(int index, E element)
	{
		int effectiveIndex = this.determineEffectiveIndex(index);
		this.assertValidEffectiveIndex(effectiveIndex);
		this.list.add(effectiveIndex, element);
	}

	@Override
	public E remove(int index)
	{
		int effectiveIndex = this.determineEffectiveIndex(index);
		this.assertValidEffectiveIndex(effectiveIndex);
		return this.list.remove(effectiveIndex);
	}

	@Override
	public int size()
	{
		return this.toIndex - this.fromIndex;
	}

}
