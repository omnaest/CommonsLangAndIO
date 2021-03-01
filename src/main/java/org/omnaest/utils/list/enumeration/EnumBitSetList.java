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

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.omnaest.utils.BitSetUtils;
import org.omnaest.utils.BitSetUtils.BitSetFrame;
import org.omnaest.utils.BitSetUtils.EnumBitSetTranslator;
import org.omnaest.utils.list.AbstractList;

/**
 * {@link List} implementation for storing a {@link List} of {@link Enum}s based on an internal {@link BitSet}. This minimizes the memory consumption, as only
 * as much bits are used per {@link Enum} instance as necessary
 * 
 * @author omnaest
 * @param <E>
 */
public class EnumBitSetList<E extends Enum<?>> extends AbstractList<E> implements EnumList<E>
{
	private int		size	= 0;
	private BitSet	bitSet	= new BitSet();

	private EnumBitSetTranslator<E> enumTranslator;

	public EnumBitSetList(Class<E> enumType)
	{
		super();
		this.enumTranslator = BitSetUtils.enumTranslator(enumType);
	}

	public EnumBitSetList(Class<E> enumType, Collection<E> collection)
	{
		super();
		this.enumTranslator = BitSetUtils.enumTranslator(enumType);
		this.addAll(collection);
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@Override
	public E get(int index)
	{
		E retval = this.enumTranslator.toEnum(this	.determineBitSetFrame(index)
													.get());
		return retval;
	}

	private BitSetFrame determineBitSetFrame(int index)
	{
		int frameSize = this.enumTranslator.getNumberOfBitsPerEnum();
		int frameIndex = index;
		BitSetFrame frame = BitSetUtils.frame(this.bitSet, frameSize, frameIndex);
		return frame;
	}

	@Override
	public E set(int index, E element)
	{
		BitSetFrame frame = this.determineBitSetFrame(index);

		E retval = this.enumTranslator.toEnum(frame.get());
		frame.set(this.enumTranslator.toBitSet(element));

		return retval;
	}

	@Override
	public void add(int index, E element)
	{
		for (int ii = this.size; ii > index; ii--)
		{
			this.set(ii, this.get(ii - 1));
		}

		this.set(index, element);
		this.size++;
	}

	@Override
	public E remove(int index)
	{
		E retval = null;

		for (int ii = index; ii < this.size; ii++)
		{
			E removedElement = this.set(ii, this.get(ii + 1));
			if (retval == null)
			{
				retval = removedElement;
			}
		}

		this.size--;

		return retval;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.bitSet == null) ? 0 : this.bitSet.hashCode());
		result = prime * result + this.size;
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (this.getClass() != obj.getClass())
		{
			return false;
		}
		EnumBitSetList other = (EnumBitSetList) obj;
		if (this.bitSet == null)
		{
			if (other.bitSet != null)
			{
				return false;
			}
		}
		else if (!this.bitSet.equals(other.bitSet))
		{
			return false;
		}
		if (this.size != other.size)
		{
			return false;
		}
		return true;
	}

	public List<E> toEnumList()
	{
		return this.enumTranslator	.toEnumStream(this.bitSet)
									.collect(Collectors.toList());
	}

	@Override
	public String toString()
	{
		return "EnumBitSetList [" + this.toEnumList() + "]";
	}

}
