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
import java.util.function.Supplier;

/**
 * Decorator of a {@link Map} instance
 * 
 * @author omnaest
 * @param <K>
 * @param <V>
 */
public class MapDecorator<K, V> implements Map<K, V>
{
    protected Supplier<Map<K, V>> map;

    public MapDecorator(Map<K, V> map)
    {
        super();
        this.map = () -> map;
    }

    public MapDecorator(Supplier<Map<K, V>> map)
    {
        super();
        this.map = map;
    }

    @Override
    public int size()
    {
        return this.map.get()
                       .size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.map.get()
                       .isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return this.map.get()
                       .containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return this.map.get()
                       .containsValue(value);
    }

    @Override
    public V get(Object key)
    {
        return this.map.get()
                       .get(key);
    }

    @Override
    public V put(K key, V value)
    {
        return this.map.get()
                       .put(key, value);
    }

    @Override
    public V remove(Object key)
    {
        return this.map.get()
                       .remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        this.map.get()
                .putAll(m);
    }

    @Override
    public void clear()
    {
        this.map.get()
                .clear();
    }

    @Override
    public Set<K> keySet()
    {
        return this.map.get()
                       .keySet();
    }

    @Override
    public Collection<V> values()
    {
        return this.map.get()
                       .values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        return this.map.get()
                       .entrySet();
    }

    @Override
    public boolean equals(Object o)
    {
        return this.map.get()
                       .equals(o);
    }

    @Override
    public int hashCode()
    {
        return this.map.get()
                       .hashCode();
    }

}
