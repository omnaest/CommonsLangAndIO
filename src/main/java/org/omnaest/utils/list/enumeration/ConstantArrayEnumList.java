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
package org.omnaest.utils.list.enumeration;

import java.util.Arrays;
import java.util.List;

import org.omnaest.utils.ListUtils;
import org.omnaest.utils.list.ListDecorator;

/**
 * Uses a constant array for the given {@link Enum} elements
 * 
 * @see EnumArrayList
 * @author omnaest
 * @param <E>
 */
public class ConstantArrayEnumList<E extends Enum<E>> extends ListDecorator<E> implements EnumList<E>
{

    @SafeVarargs
    public ConstantArrayEnumList(E... elements)
    {
        super(Arrays.asList(elements));
    }

    public ConstantArrayEnumList(List<E> list, Class<E> elementType)
    {
        this(ListUtils.toArray(list, elementType));
    }

    @Override
    public String toString()
    {
        return "ConstantArrayEnumList [list=" + this.list + "]";
    }

}
