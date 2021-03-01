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

import java.util.Map;
import java.util.Set;

public class MapToCRUDMapAdapter<K, V> implements CRUDMap<K, V>
{
	private Map<K, V> map;

	public MapToCRUDMapAdapter(Map<K, V> map)
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
	public boolean containsKey(Object key)
	{
		return this.map.containsKey(key);
	}

	@Override
	public V get(Object key)
	{
		return this.map.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		return this.map.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		return this.map.remove(key);
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
	public String toString()
	{
		return "MapToCRUDMapAdapter [map=" + this.map + "]";
	}

}
