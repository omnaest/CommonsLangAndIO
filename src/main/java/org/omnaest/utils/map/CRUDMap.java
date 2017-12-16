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

/**
 * Simple CRUD interface related to the main {@link Map} interface
 * 
 * @author omnaest
 */
public interface CRUDMap<K, V>
{

	public int size();

	public boolean containsKey(K key);

	public V get(K key);

	public V put(K key, V value);

	public V remove(K key);

	public void clear();

	public Set<K> keySet();

	public default Map<K, V> toMap()
	{
		return new CRUDMapToMapAdapter<>(this);
	}

	public static <K, V> CRUDMap<K, V> of(Map<K, V> map)
	{
		return new MapToCRUDMapAdapter<>(map);
	}

}
