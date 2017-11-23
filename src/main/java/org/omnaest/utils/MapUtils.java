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
package org.omnaest.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.omnaest.utils.element.LeftAndRight;
import org.omnaest.utils.element.ModifiableLeftAndRight;

public class MapUtils
{

	public static interface MapAction<K, V>
	{
		MapAction<K, V> put(K key, V value);
	}

	public static <K, V> MapAction<K, V> withMap(Map<K, V> map)
	{
		return new MapAction<K, V>()
		{
			@Override
			public MapAction<K, V> put(K key, V value)
			{
				map.put(key, value);
				return this;
			}
		};
	}

	public static <K, V> Map<V, List<K>> invertMultiValue(Map<K, ? extends Collection<V>> map)
	{
		Map<V, List<K>> retmap = new LinkedHashMap<>();

		map	.entrySet()
			.forEach(entry ->
			{
				entry	.getValue()
						.forEach(value ->
						{
							retmap	.computeIfAbsent(value, v -> new ArrayList<>())
									.add(entry.getKey());
						});
			});

		return retmap;
	}

	public static <K, V> Map<V, List<K>> invert(Map<K, V> map)
	{
		Map<V, List<K>> retmap = new LinkedHashMap<>();

		map	.entrySet()
			.forEach(entry ->
			{
				retmap	.computeIfAbsent(entry.getValue(), v -> new ArrayList<>())
						.add(entry.getKey());
			});

		return retmap;
	}

	public static interface MapBuilder<K, V>
	{
		public <K2, V2> MapBuilder<K2, V2> put(K2 key, V2 value);

		public <K2, V2> MapBuilder<K2, V2> putAll(Map<K2, V2> map);

		public <K2, V2> MapBuilder<K2, V2> useFactory(Supplier<Map<K2, V2>> mapFactory);

		public <K2 extends K, V2 extends V> Map<K2, V2> build();
	}

	@SuppressWarnings("unchecked")
	public static MapBuilder<?, ?> builder()
	{
		return new MapBuilder<Object, Object>()
		{
			private Map<Object, Object>				map			= new LinkedHashMap<>();
			private Supplier<Map<Object, Object>>	mapFactory	= null;

			@Override
			public <K2, V2> MapBuilder<K2, V2> put(K2 key, V2 value)
			{
				this.map.put(key, value);
				return (MapBuilder<K2, V2>) this;
			}

			@Override
			public <K2, V2> MapBuilder<K2, V2> putAll(Map<K2, V2> map)
			{
				if (map != null)
				{
					this.map.putAll(map);
				}
				return (MapBuilder<K2, V2>) this;
			}

			@Override
			public <K2, V2> MapBuilder<K2, V2> useFactory(Supplier<Map<K2, V2>> mapFactory)
			{
				this.mapFactory = () -> (Map<Object, Object>) mapFactory.get();
				return (MapBuilder<K2, V2>) this;
			}

			@Override
			public <K2, V2> Map<K2, V2> build()
			{
				Map<K2, V2> retmap;

				if (this.mapFactory != null)
				{
					retmap = (Map<K2, V2>) this.mapFactory.get();
					retmap.putAll((Map<? extends K2, ? extends V2>) this.map);
				}
				else
				{
					retmap = (Map<K2, V2>) this.map;
				}

				return retmap;
			}

		};
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, LeftAndRight<V>> join(Map<K, V> leftMap, Map<K, V> rightMap)
	{
		Map<K, ModifiableLeftAndRight<V>> retmap = new LinkedHashMap<>();

		if (leftMap != null)
		{
			for (K key : leftMap.keySet())
			{
				V value = leftMap.get(key);
				retmap	.computeIfAbsent(key, k -> new ModifiableLeftAndRight<>())
						.setLeft(value);
			}
		}
		if (rightMap != null)
		{
			for (K key : rightMap.keySet())
			{
				V value = rightMap.get(key);
				retmap	.computeIfAbsent(key, k -> new ModifiableLeftAndRight<>())
						.setRight(value);
			}
		}

		return (Map<K, LeftAndRight<V>>) ((Map<K, ? extends LeftAndRight<V>>) retmap);
	}
}
