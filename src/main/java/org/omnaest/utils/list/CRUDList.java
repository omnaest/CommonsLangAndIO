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

import org.omnaest.utils.ListUtils;

/**
 * @see ListUtils#toList(CRUDList)
 * @author omnaest
 * @param <E>
 */
public interface CRUDList<E>
{
	public E get(int index);

	public E set(int index, E element);

	public void add(int index, E element);

	public E remove(int index);

	public int size();

	public static <E> CRUDList<E> valueOf(List<E> list)
	{
		return new ListToCRUDAdapter<>(list);
	}

}
