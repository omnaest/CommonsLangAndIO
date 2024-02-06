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
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.IntStream.Builder;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.bitset.binary.BinaryDigits;
import org.omnaest.utils.bitset.hex.HexDigits;

/**
 * Wrapper around {@link BitSet} which provides further functionality
 * 
 * @see Bits
 * @see Bits#newInstance()
 * @author omnaest
 */
public class BitSetBits implements Bits
{
    private int    length = 0;
    private BitSet bits   = new BitSet();

    @Override
    public BitSetBits flip(int bitIndex)
    {
        return this.flipIndex(bitIndex);
    }

    @Override
    public BitSetBits flipIndex(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bits.flip(bitIndex);
        return this;
    }

    @Override
    public BitSetBits clear(int bitIndex)
    {
        return this.clearIndex(bitIndex);
    }

    @Override
    public BitSetBits clearIndex(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bits.clear(bitIndex);
        return this;
    }

    @Override
    public Bits drainFromLeft(int numberOfBits)
    {
        int effectiveNumberOfBits = Math.min(this.length, numberOfBits);
        Bits result = Bits.of(this.bits.get(0, effectiveNumberOfBits), effectiveNumberOfBits);
        this.shiftLeft(effectiveNumberOfBits);
        this.setLength(Math.max(0, this.length - numberOfBits));
        return result;
    }

    @Override
    public Bits drainFromLeftOrDefault(int numberOfBits)
    {
        return this.drainFromLeftOrDefault(numberOfBits, false);
    }

    @Override
    public Bits drainFromLeftOrDefault(int numberOfBits, boolean defaultValue)
    {
        return this.drainFromLeft(numberOfBits)
                   .setLength(numberOfBits, defaultValue);
    }

    @Override
    public Stream<Bits> drainBlocksFromLeftOfSize(int numberOfBitsPerBlock)
    {
        return StreamUtils.fromOptionalSupplier(() ->
        {
            if (this.isEmpty())
            {
                return Optional.empty();
            }
            else
            {
                return Optional.of(this.drainFromLeftOrDefault(numberOfBitsPerBlock));
            }
        });
    }

    @Override
    public Stream<Bits> drainBlocksFromLeftOfMaxSize(int numberOfMaxBitsPerBlock)
    {
        return StreamUtils.fromSupplier(() -> this.drainFromLeft(numberOfMaxBitsPerBlock), Bits::isEmpty);
    }

    @Override
    public Bits shiftLeft(int numberOfBits)
    {
        this.bits = this.bits.get(numberOfBits, Math.max(numberOfBits, this.bits.length()));
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
        return this.setIndex(bitIndex);
    }

    @Override
    public BitSetBits setIndex(int bitIndex)
    {
        return this.set(bitIndex, true);
    }

    @Override
    public Bits setIndex(int[] bitIndex)
    {
        return this.setIndex(bitIndex, true);
    }

    @Override
    public Bits setIndex(int[] bitIndex, boolean value)
    {
        if (bitIndex != null)
        {
            for (int index : bitIndex)
            {
                this.setIndex(index, value);
            }
        }
        return this;
    }

    @Override
    public boolean getOrSet(int bitIndex)
    {
        return this.getOrSet(bitIndex, false);
    }

    @Override
    public boolean getOrSet(int bitIndex, boolean defaultValue)
    {
        this.adjustLengthIfNecessary(bitIndex, defaultValue);
        return this.get(bitIndex);
    }

    @Override
    public Bits setLength(int length)
    {
        return this.setLength(length, false);
    }

    @Override
    public Bits setLength(int length, boolean defaultValue)
    {
        if (length < 0)
        {
            throw new IllegalArgumentException("Length must be greater or equal to zero.");
        }
        if (this.bits.length() > length)
        {
            this.bits.clear(length, this.bits.length());
        }
        else if (this.length < length)
        {
            this.bits.set(this.length, length, defaultValue);
        }
        this.length = length;
        return this;
    }

    @Override
    public Bits append(Bits bits)
    {
        int initialLength = this.length;
        for (int ii = 0; ii < bits.getLength(); ii++)
        {
            this.setIndex(initialLength + ii, bits.get(ii));
        }
        return this;
    }

    @Override
    public BitSetBits set(int bitIndex, boolean value)
    {
        return this.setIndex(bitIndex, value);
    }

    @Override
    public BitSetBits setIndex(int bitIndex, boolean value)
    {
        this.adjustLengthIfNecessary(bitIndex);
        this.bits.set(bitIndex, value);
        return this;
    }

    @Override
    public Bits setIndex(int index, Bits bits)
    {
        if (bits != null)
        {
            bits.forEach((ii, value) -> this.setIndex(index + ii, value));
        }
        return this;
    }

    private void adjustLengthIfNecessary(int bitIndex)
    {
        this.adjustLengthIfNecessary(bitIndex, false);
    }

    private void adjustLengthIfNecessary(int bitIndex, boolean defaultValue)
    {
        if (bitIndex >= this.length)
        {
            int newLength = bitIndex + 1;
            this.bits.set(this.length, newLength, defaultValue);
            this.length = newLength;
        }
    }

    @Override
    public Bits forEach(BiConsumer<Integer, Boolean> consumer)
    {
        if (consumer != null)
        {
            for (int ii = 0; ii < this.length; ii++)
            {
                consumer.accept(ii, this.get(ii));
            }
        }
        return this;
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
                this.setIndex(ii, values[ii]);
            }
        }
        return this;
    }

    @Override
    public Bits set(Bits bits)
    {
        if (bits != null)
        {
            for (int ii = 0; ii < bits.getLength(); ii++)
            {
                this.setIndex(ii, bits.get(ii));
            }
        }
        return this;
    }

    @Override
    public Bits set(BitSet bitSet)
    {
        return this.set(bitSet, bitSet.length());
    }

    @Override
    public Bits set(BitSet bitSet, int length)
    {
        this.length = length;
        if (bitSet != null)
        {
            for (int ii = 0; ii < length; ii++)
            {
                this.setIndex(ii, bitSet.get(ii));
            }
        }
        return this;
    }

    @Override
    public Bits set(byte[] values)
    {
        Boolean[] bits = Optional.ofNullable(values)
                                 .map(ArrayUtils::toObject)
                                 .map(Arrays::asList)
                                 .orElse(Collections.emptyList())
                                 .stream()
                                 .flatMap(value -> IntStream.range(0, Byte.SIZE)
                                                            .mapToObj(i -> 0 != ((value >> i) & 1))
                                                            .collect(Collectors.toList())
                                                            .stream())
                                 .toArray(Boolean[]::new);
        return this.set(ArrayUtils.toPrimitive(bits));
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
                        .mapToObj(this::get);
    }

    @Override
    public boolean[] toBooleanArray()
    {
        boolean[] result = new boolean[this.length];
        for (int ii = 0; ii < this.length; ii++)
        {
            result[ii] = this.get(ii);
        }
        return result;
    }

    @Override
    public byte[] toBytes()
    {
        return this.bits.toByteArray();
    }

    @Override
    public String toString()
    {
        return IntStream.range(0, this.length)
                        .boxed()
                        .map(index -> this.get(index) ? "1" : "0")
                        .collect(Collectors.joining());
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
        Bits result = Bits.newInstance()
                          .setLength(endExclusive - startInclusive);
        for (int iBitIndex = startInclusive; iBitIndex < endExclusive && iBitIndex < this.length; iBitIndex++)
        {
            result.setIndex(iBitIndex - startInclusive, this.get(iBitIndex));
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

    @Override
    public Bits clone()
    {
        return Bits.of(this);
    }

    @Override
    public Bits and(Bits bits)
    {
        if (bits != null)
        {
            this.bits.and(bits.toBitSet());
            this.length = Math.max(this.length, bits.getLength());
        }
        return this;
    }

    @Override
    public Bits or(Bits bits)
    {
        if (bits != null)
        {
            this.bits.or(bits.toBitSet());
            this.length = Math.max(this.length, bits.getLength());
        }
        return this;
    }

    @Override
    public Bits xor(Bits bits)
    {
        if (bits != null)
        {
            this.bits.xor(bits.toBitSet());
            this.length = Math.max(this.length, bits.getLength());
        }
        return this;
    }

    @Override
    public Bits negate()
    {
        if (this.bits != null)
        {
            this.bits.flip(0, this.length);
        }
        return this;
    }

    @Override
    public BitSet toBitSet()
    {
        return (BitSet) this.bits.clone();
    }

    @Override
    public IntStream toIndexPositions()
    {
        Builder builder = IntStream.builder();

        for (int index = this.bits.nextSetBit(0); index >= 0 && index < this.length; index = this.bits.nextSetBit(index + 1))
        {
            builder.add(index);
        }

        return builder.build();
    }

    @Override
    public OptionalInt findNextClearBitIndex()
    {
        int index = this.bits.nextClearBit(0);
        return index >= 0 && index < this.length ? OptionalInt.of(index) : OptionalInt.empty();
    }

    @Override
    public OptionalInt findNextSetBitIndex()
    {
        int index = this.bits.nextSetBit(0);
        return index >= 0 ? OptionalInt.of(index) : OptionalInt.empty();
    }

    @Override
    public OptionalInt findLastSetBitIndex()
    {
        for (int ii = this.length - 1; ii >= 0; ii--)
        {
            if (this.get(ii))
            {
                return OptionalInt.of(ii);
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public int[] toIndexPositionArray()
    {
        return this.toIndexPositions()
                   .toArray();
    }

    @Override
    public boolean hasAnyBitEqualTo(boolean value)
    {
        return value ? this.bits.nextSetBit(0) >= 0 : this.bits.nextClearBit(0) >= 0;
    }

    @Override
    public boolean isEmpty()
    {
        return this.getLength() == 0;
    }

    @Override
    public boolean isNotEmpty()
    {
        return !this.isEmpty();
    }

}
