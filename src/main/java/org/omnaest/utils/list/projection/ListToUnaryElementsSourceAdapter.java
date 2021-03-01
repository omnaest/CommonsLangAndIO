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
package org.omnaest.utils.list.projection;

import java.util.List;

import org.omnaest.utils.list.projection.ListAggregationBuilder.UnaryElementsSource;

/**
 * @see ListAggregationBuilder.UnaryElementsSource
 * @see List
 * @author omnaest
 * @param <E>
 */
public class ListToUnaryElementsSourceAdapter<E> implements UnaryElementsSource<E>
{
	private List<E> list;

	public ListToUnaryElementsSourceAdapter(List<E> list)
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
	public String toString()
	{
		return "ListToUnaryElementsSourceAdapter [list=" + this.list + "]";
	}

}
