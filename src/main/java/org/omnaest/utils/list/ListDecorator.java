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
package org.omnaest.utils.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListDecorator<E>
{
    protected List<E> list;

    public ListDecorator(List<E> list)
    {
        super();
        this.list = list;
    }

    public int size()
    {
        return this.list.size();
    }

    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    public boolean contains(Object o)
    {
        return this.list.contains(o);
    }

    public Iterator<E> iterator()
    {
        return this.list.iterator();
    }

    public Object[] toArray()
    {
        return this.list.toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return this.list.toArray(a);
    }

    public boolean add(E e)
    {
        return this.list.add(e);
    }

    public boolean remove(Object o)
    {
        return this.list.remove(o);
    }

    public boolean containsAll(Collection<?> c)
    {
        return this.list.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c)
    {
        return this.list.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c)
    {
        return this.list.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c)
    {
        return this.list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c)
    {
        return this.list.retainAll(c);
    }

    public void clear()
    {
        this.list.clear();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.list.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return this.list.hashCode();
    }

    public E get(int index)
    {
        return this.list.get(index);
    }

    public E set(int index, E element)
    {
        return this.list.set(index, element);
    }

    public void add(int index, E element)
    {
        this.list.add(index, element);
    }

    public E remove(int index)
    {
        return this.list.remove(index);
    }

    public int indexOf(Object o)
    {
        return this.list.indexOf(o);
    }

    public int lastIndexOf(Object o)
    {
        return this.list.lastIndexOf(o);
    }

    public ListIterator<E> listIterator()
    {
        return this.list.listIterator();
    }

    public ListIterator<E> listIterator(int index)
    {
        return this.list.listIterator(index);
    }

    public List<E> subList(int fromIndex, int toIndex)
    {
        return this.list.subList(fromIndex, toIndex);
    }

    @Override
    public String toString()
    {
        return "ListDecorator [list=" + this.list + "]";
    }

}
