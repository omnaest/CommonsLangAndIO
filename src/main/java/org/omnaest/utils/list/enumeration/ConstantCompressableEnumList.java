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

import java.util.Collection;

/**
 * A constant {@link CompressableEnumList} which uses an array as uncompressed implementation
 * 
 * @author omnaest
 * @param <E>
 */
public class ConstantCompressableEnumList<E extends Enum<E>> extends CompressableEnumList<E>
{
    public ConstantCompressableEnumList(Class<E> enumType, Collection<E> collection)
    {
        this(enumType, new EnumArrayList<>(collection));
    }

    public ConstantCompressableEnumList(Class<E> enumType, EnumList<E> enumList)
    {
        super(enumType);
        this.list = enumList;
        this.setUnCompressFunction(l -> new ConstantArrayEnumList<>(l, enumType));
        this.uncompress();
    }

    @Override
    public ConstantCompressableEnumList<E> usingInMemoryCompression(boolean active)
    {
        super.usingInMemoryCompression(active);
        return this;
    }

    @Override
    public String toString()
    {
        return "ConstantCompressableEnumList [list=" + this.list + "]";
    }

}
