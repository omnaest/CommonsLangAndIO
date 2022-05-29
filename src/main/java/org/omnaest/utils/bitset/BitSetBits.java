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
package org.omnaest.utils.bitset;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.omnaest.utils.bitset.binary.BinaryDigits;
import org.omnaest.utils.bitset.hex.HexDigits;

/**
 * Wrapper around {@link BitSet} which provides further functionality
 * 
 * @author omnaest
 */
public class BitSetBits implements Bits
{
    private int    length = 0;
    private BitSet bits   = new BitSet();

    @Override
    public BitSetBits flip(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bits.flip(bitIndex);
        return this;
    }

    @Override
    public BitSetBits clear(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bits.clear(bitIndex);
        return this;
    }

    @Override
    public boolean get(int bitIndex)
    {
        this.assertIndexBounds(bitIndex);

        return this.bits.get(bitIndex);
    }

    @Override
    public int getLength()
    {
        return this.length;
    }

    private void assertIndexBounds(int bitIndex)
    {
        if (bitIndex < 0)
        {
            throw new IndexOutOfBoundsException("Bitset index cannot be lower than zero: " + bitIndex);
        }

        if (bitIndex >= this.length)
        {
            throw new IndexOutOfBoundsException("Bitset length is " + this.length + " but access was on index position " + bitIndex);
        }
    }

    @Override
    public BitSetBits set(int bitIndex)
    {
        return this.set(bitIndex, true);
    }

    @Override
    public BitSetBits set(int bitIndex, boolean value)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bits.set(bitIndex, value);
        return this;
    }

    private void adjustLengthIfNecessary(int bitIndex)
    {
        if (bitIndex >= this.length)
        {
            this.length = bitIndex + 1;
        }
    }

    @Override
    public Bits set(long value)
    {
        IntStream.range(0, Long.SIZE - 1)
                 .forEach(i -> this.set(i, 0 != (value & 1l << i)));
        return this;
    }

    @Override
    public Bits set(boolean[] values)
    {
        if (values != null)
        {
            for (int ii = 0; ii < values.length; ii++)
            {
                this.set(ii, values[ii]);
            }
        }
        return this;
    }

    @Override
    public Bits set(byte[] values)
    {
        return this.set(ArrayUtils.toPrimitive(Optional.ofNullable(values)
                                                       .map(ArrayUtils::toObject)
                                                       .map(Arrays::asList)
                                                       .orElse(Collections.emptyList())
                                                       .stream()
                                                       .flatMap(value ->
                                                       {
                                                           List<Boolean> collect = IntStream.range(0, Byte.SIZE)
                                                                                            .mapToObj(i -> 0 != ((value >> i) & 1))
                                                                                            .collect(Collectors.toList());
                                                           return collect.stream();
                                                       })
                                                       .toArray(size -> new Boolean[size])));
    }

    @Override
    public HexDigits toHexDigits()
    {
        return HexDigits.of(this);
    }

    @Override
    public BinaryDigits toBinaryDigits()
    {
        return BinaryDigits.of(this);
    }

    @Override
    public Stream<Boolean> toBooleanStream()
    {
        return IntStream.range(0, this.length)
                        .mapToObj(index -> this.get(index));
    }

    @Override
    public byte[] toBytes()
    {
        return this.bits.toByteArray();
    }

    @Override
    public String toString()
    {
        return this.toBinaryDigits()
                   .toString();
    }

    @Override
    public Stream<Bits> frames(int frameSize)
    {
        int numberOfFrames = this.length / frameSize + (this.length % frameSize > 0 ? 1 : 0);
        return IntStream.range(0, numberOfFrames)
                        .mapToObj(frameIndex -> this.subset(frameIndex * frameSize, (frameIndex + 1) * frameSize));
    }

    @Override
    public Bits subset(int startInclusive, int endExclusive)
    {
        Bits result = Bits.newInstance();
        for (int iBitIndex = startInclusive; iBitIndex < endExclusive; iBitIndex++)
        {
            result.set(iBitIndex, this.get(iBitIndex));
        }
        return result;
    }

    @Override
    public int toInt()
    {
        return IntStream.range(0, Integer.SIZE)
                        .map(index -> this.getOrDefault(index) ? 1 << index : 0)
                        .reduce(0, (a, b) -> a | b);
    }

    @Override
    public boolean getOrDefault(int bitIndex, boolean defaultValue)
    {
        if (bitIndex >= 0 && bitIndex < this.length)
        {
            return this.bits.get(bitIndex);
        }
        else
        {
            return defaultValue;
        }
    }

    @Override
    public boolean getOrDefault(int bitIndex)
    {
        return this.getOrDefault(bitIndex, false);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.bits == null) ? 0 : this.bits.hashCode());
        result = prime * result + this.length;
        return result;
    }

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
        if (!(obj instanceof BitSetBits))
        {
            return false;
        }
        BitSetBits other = (BitSetBits) obj;
        if (this.bits == null)
        {
            if (other.bits != null)
            {
                return false;
            }
        }
        else if (!this.bits.equals(other.bits))
        {
            return false;
        }
        if (this.length != other.length)
        {
            return false;
        }
        return true;
    }

}
