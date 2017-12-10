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

/**
 * @see CRUDList
 * @see ReadList
 * @author omnaest
 * @param <E>
 */
public class ReadListToCRUDListAdapter<E> implements CRUDList<E>
{
	private ReadList<E> list;

	public ReadListToCRUDListAdapter(ReadList<E> list)
	{
		super();
		this.list = list;
	}

	@Override
	public E get(int index)
	{
		return this.list.get(index);
	}

	@Override
	public int size()
	{
		return this.list.size();
	}

	@Override
	public E set(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return "ReadListToCRUDListAdapter [list=" + this.list + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.list == null) ? 0 : this.list.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (this.getClass() != obj.getClass())
		{
			return false;
		}
		ReadListToCRUDListAdapter other = (ReadListToCRUDListAdapter) obj;
		if (this.list == null)
		{
			if (other.list != null)
			{
				return false;
			}
		}
		else if (!this.list.equals(other.list))
		{
			return false;
		}
		return true;
	}

}
