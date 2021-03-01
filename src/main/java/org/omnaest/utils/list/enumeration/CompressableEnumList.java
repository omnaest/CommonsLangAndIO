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
package org.omnaest.utils.list.enumeration;

import java.util.function.Function;

/**
 * {@link EnumList} which allows to compress the content in memory. As default this uses an {@link EnumArrayList} and a {@link EnumBitSetList} as underlying
 * {@link EnumList} structures, but any other {@link EnumList}s can be used.
 * 
 * @see #usingInMemoryCompression(boolean)
 * @see #setCompressFunction(Function)
 * @see #setUnCompressFunction(Function)
 * @author omnaest
 * @param <E>
 */
public class CompressableEnumList<E extends Enum<E>> extends EnumListDecorator<E> implements EnumList<E>
{
    private boolean  inMemoryCompression = false;
    private Class<E> enumType;

    private Function<EnumList<E>, EnumList<E>> compressFunction   = list -> new EnumBitSetList<>(this.enumType, list);
    private Function<EnumList<E>, EnumList<E>> unCompressFunction = list -> new EnumArrayList<>(list);

    public CompressableEnumList(Class<E> enumType)
    {
        super(new EnumArrayList<>());
        this.enumType = enumType;
    }

    /**
     * Activates or deactivates the in memory compression
     * 
     * @param active
     * @return
     */
    public CompressableEnumList<E> usingInMemoryCompression(boolean active)
    {
        if (active && !this.inMemoryCompression)
        {
            this.compress();
        }
        else if (!active && this.inMemoryCompression)
        {
            this.uncompress();
        }
        return this;
    }

    /**
     * Returns if the in memory compression is active or not
     * 
     * @return
     */
    public boolean isInMemoryCompressionActive()
    {
        return this.inMemoryCompression;
    }

    protected void uncompress()
    {
        this.list = this.unCompressFunction.apply(this.list);
    }

    protected void compress()
    {
        this.list = this.compressFunction.apply(this.list);
    }

    /**
     * Sets the compress {@link Function}. As default an {@link EnumBitSetList} is used.
     * 
     * @param compressFunction
     * @return
     */
    public CompressableEnumList<E> setCompressFunction(Function<EnumList<E>, EnumList<E>> compressFunction)
    {
        this.compressFunction = compressFunction;

        if (this.inMemoryCompression)
        {
            this.compress();
        }

        return this;
    }

    /**
     * Sets the uncompress {@link Function}. As default an {@link EnumArrayList} is used.
     * 
     * @param unCompressFunction
     * @return
     */
    public CompressableEnumList<E> setUnCompressFunction(Function<EnumList<E>, EnumList<E>> unCompressFunction)
    {
        this.unCompressFunction = unCompressFunction;

        if (!this.inMemoryCompression)
        {
            this.uncompress();
        }

        return this;
    }

    /**
     * Returns a {@link ConstantCompressableEnumList} based on this {@link EnumList}
     * 
     * @return
     */
    public ConstantCompressableEnumList<E> toConstantCompressableEnumList()
    {
        return new ConstantCompressableEnumList<>(this.enumType, this).usingInMemoryCompression(this.inMemoryCompression);
    }
}
