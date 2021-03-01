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
package org.omnaest.utils.list.crud;

import java.util.List;

public class ListToCRUDAdapter<E> implements CRUDList<E>
{
	private List<E> list;

	public ListToCRUDAdapter(List<E> list)
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
	public E set(int index, E element)
	{
		return this.list.set(index, element);
	}

	@Override
	public void add(int index, E element)
	{
		this.list.add(index, element);
	}

	@Override
	public E remove(int index)
	{
		return this.list.remove(index);
	}

	@Override
	public int size()
	{
		return this.list.size();
	}

}
