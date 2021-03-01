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
package org.omnaest.utils.list;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.omnaest.utils.ListUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.list.crud.CRUDList;

public abstract class AbstractList<E> implements List<E>, CRUDList<E>
{
    public AbstractList()
    {
        super();
    }

    @Override
    public boolean isEmpty()
    {
        return this.size() == 0;
    }

    @Override
    public boolean contains(Object o)
    {
        return this.stream()
                   .anyMatch(element -> Objects.equals(element, o));
    }

    @Override
    public Iterator<E> iterator()
    {
        return this.listIterator();
    }

    @Override
    public Object[] toArray()
    {
        return this.stream()
                   .toArray(size -> new Object[size]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a)
    {
        return this.stream()
                   .toArray(size -> (T[]) Array.newInstance(a.getClass()
                                                             .getComponentType(),
                                                            size));
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return collection == null || collection.stream()
                                               .allMatch(element -> this.contains(element));
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        return c != null && c.stream()
                             .map(this::add)
                             .reduce((b1, b2) -> b1 | b2)
                             .orElse(false);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        boolean retval = c != null;
        if (retval)
        {
            StreamUtils.reverse(c.stream())
                       .forEach(element -> this.add(index, element));
        }
        return retval;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean retval = false;
        if (c != null)
        {
            retval = c.stream()
                      .anyMatch(this::remove);
        }
        return retval;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return this.removeAll(this.stream()
                                  .filter(element -> !c.contains(element))
                                  .collect(Collectors.toList()));
    }

    @Override
    public void clear()
    {
        IntStream.range(0, this.size())
                 .forEach(this::remove);
    }

    @Override
    public boolean add(E element)
    {
        int index = this.size();
        this.add(index, element);
        return true;
    }

    @Override
    public boolean remove(Object o)
    {
        return this.remove(this.indexOf(o)) != null;
    }

    @Override
    public int indexOf(Object o)
    {
        return IntStream.range(0, this.size())
                        .filter(index -> Objects.equals(this.get(index), o))
                        .findFirst()
                        .orElse(-1);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return StreamUtils.reverse(IntStream.range(0, this.size())
                                            .filter(index -> Objects.equals(this.get(index), o))
                                            .sorted())
                          .findFirst()
                          .orElse(-1);
    }

    @Override
    public ListIterator<E> listIterator()
    {
        return this.listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index)
    {
        return new DefaultListIterator<>(index, this);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        return new ListView<>(fromIndex, toIndex, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object object)
    {
        return object instanceof List && ListUtils.equals(this, (List<E>) object);
    }

    @Override
    public int hashCode()
    {
        return ListUtils.hashCode(this);
    }

    @Override
    public String toString()
    {
        return ListUtils.toString(this);
    }

}
