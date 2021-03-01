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
package org.omnaest.utils.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CRUDMapToMapAdapter<K, V> implements Map<K, V>
{
	private CRUDMap<K, V> map;

	public CRUDMapToMapAdapter(CRUDMap<K, V> map)
	{
		super();
		this.map = map;
	}

	@Override
	public int size()
	{
		return this.map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.size() == 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key)
	{
		boolean retval = false;
		try
		{
			retval = this.map.containsKey((K) key);
		} catch (ClassCastException e)
		{
			retval = false;
		}
		return retval;
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this	.values()
					.contains(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key)
	{
		V retval = null;
		try
		{
			retval = this.map.get((K) key);
		} catch (ClassCastException e)
		{
			retval = null;
		}
		return retval;
	}

	@Override
	public V put(K key, V value)
	{
		return this.map.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key)
	{
		V retval = null;
		try
		{
			retval = this.map.remove((K) key);
		} catch (ClassCastException e)
		{
			retval = null;
		}
		return retval;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		if (m != null)
		{
			for (java.util.Map.Entry<? extends K, ? extends V> entry : m.entrySet())
			{
				this.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void clear()
	{
		this.map.clear();

	}

	@Override
	public Set<K> keySet()
	{
		return this.map.keySet();
	}

	@Override
	public Collection<V> values()
	{
		return this	.keySet()
					.stream()
					.map(key -> this.get(key))
					.collect(Collectors.toList());
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet()
	{
		return this	.keySet()
					.stream()
					.map(key -> (Map.Entry<K, V>) new Map.Entry<K, V>()
					{
						@Override
						public K getKey()
						{
							return key;
						}

						@Override
						public V getValue()
						{
							return CRUDMapToMapAdapter.this.get(key);
						}

						@Override
						public V setValue(V value)
						{
							return CRUDMapToMapAdapter.this.put(key, value);
						}
					})
					.collect(Collectors.toSet());
	}

}
