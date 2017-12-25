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
package org.omnaest.utils.list.crud;

import java.util.List;

import org.omnaest.utils.ListUtils;

/**
 * @see List
 * @see ListUtils#toList(CRUDList)
 * @see #valueOf(List)
 * @author omnaest
 * @param <E>
 */
public interface CRUDList<E> extends ReadList<E>
{
	public E set(int index, E element);

	public void add(int index, E element);

	public E remove(int index);

	/**
	 * @see ListUtils#toCRUDList(List)
	 * @param list
	 * @return
	 */
	public static <E> CRUDList<E> valueOf(List<E> list)
	{
		return new ListToCRUDAdapter<>(list);
	}

	public static <E> CRUDList<E> valueOf(ReadList<E> readList)
	{
		return new ReadListToCRUDListAdapter<>(readList);
	}

}
