package org.omnaest.utils.bitset;

import java.util.BitSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.bitset.binary.BinaryDigits;

/**
 * @see EnumBits
 * @see MultiBits
 * @author omnaest
 */
public interface Bits extends ImmutableBits
{

    /**
     * @deprecated please use {@link #flipIndex(int)}
     * @param bitIndex
     * @return
     */
    @Deprecated
    public Bits flip(int bitIndex);

    public Bits flipIndex(int bitIndex);

    /**
     * @deprecated please use {@link #clearIndex(int)}
     * @param bitIndex
     * @return
     */
    @Deprecated
    public Bits clear(int bitIndex);

    public Bits clearIndex(int bitIndex);

    /**
     * Drains/removes the bits from the left and returns them. If the remaining {@link Bits} have a length smaller than the given number of bits, the available
     * length of {@link Bits}s is returned instead.
     * 
     * @see #drainBlocksFromLeftOfMaxSize(int)
     * @see #drainFromLeftOrDefault(int)
     * @param numberOfBits
     * @return
     */
    public Bits drainFromLeft(int numberOfBits);

    /**
     * Drains/removes the bits from the left and returns them. If the remaining {@link Bits} have a length smaller than the given number of bits all the bit
     * positions are filled with the default value of 'false'.
     * 
     * @see #drainFromLeft(int)
     * @see #drainFromLeftOrDefault(int, boolean)
     * @param numberOfBits
     * @return
     */
    public Bits drainFromLeftOrDefault(int numberOfBits);

    /**
     * Similar to {@link #drainFromLeftOrDefault(int)} but allows to define the default value that is used to fill up remaining bit positions.
     * 
     * @param numberOfBits
     * @param defaultValue
     * @return
     */
    public Bits drainFromLeftOrDefault(int numberOfBits, boolean defaultValue);

    /**
     * Partitions the current {@link Bits} into blocks of exactly the given bit size. If the current {@link Bits} cannot fill the last block, the remaining bit
     * positions are filled with 'false' values.
     * 
     * @see #drainBlocksFromLeftOfMaxSize(int)
     * @param numberOfBitsPerBlock
     * @return
     */
    public Stream<Bits> drainBlocksFromLeftOfSize(int numberOfBitsPerBlock);

    /**
     * Partitions the current {@link Bits} into blocks of exactly the given bit size. If the current {@link Bits} cannot fill the last block, the last returned
     * block will only contain the available bits.
     * 
     * @see #drainBlocksFromLeftOfSize(int)
     * @param numberOfMaxBitsPerBlock
     * @return
     */
    public Stream<Bits> drainBlocksFromLeftOfMaxSize(int numberOfMaxBitsPerBlock);

    /**
     * Shifts all bits to the right by the given number of bit positions. Keeps the {@link #getLength()} of the {@link Bits} unchanged. Missing bits from the
     * left will be set to false.
     * 
     * @param numberOfBits
     * @return
     */
    public Bits shiftRight(int numberOfBits);

    /**
     * Shifts all bits to the left by the given number of bit positions. Keeps the {@link #getLength()} of the {@link Bits} unchanged. Missing bits from the
     * right will be set to false.
     * 
     * @param numberOfBits
     * @return
     */
    public Bits shiftLeft(int numberOfBits);

    public Bits set(long value);

    public Bits set(boolean[] values);

    public Bits set(byte[] values);

    public Bits set(Bits bits);

    public Bits set(BitSet bitSet);

    public Bits set(BitSet bitSet, int length);

    /**
     * @deprecated please use {@link #setIndex(int, boolean)}
     * @param bitIndex
     * @param value
     * @return
     */
    @Deprecated
    public Bits set(int bitIndex, boolean value);

    public Bits setIndex(int bitIndex, boolean value);

    /**
     * Sets the given {@link Bits} from the given index position and following.
     * 
     * @param index
     * @param bits
     * @return
     */
    public Bits setIndex(int index, Bits bits);

    /**
     * @deprecated please use {@link #setIndex(int)}
     * @param bitIndex
     * @return
     */
    @Deprecated
    public Bits set(int bitIndex);

    public Bits setIndex(int bitIndex);

    public Bits setIndex(int[] bitIndex);

    public Bits setIndex(int[] bitIndex, boolean value);

    /**
     * Gets the value of the given bit index. If the bit index is beyond the current length of the {@link Bits} then the length is increased and a default value
     * is permanently set (which is returned then).
     * 
     * @param bitIndex
     * @return
     */
    public boolean getOrSet(int bitIndex);

    public boolean getOrSet(int bitIndex, boolean defaultValue);

    /**
     * Gets the value of the given bit index and sets the given value to it.
     * 
     * @param bitIndex
     * @param value
     * @return previous value
     */
    public boolean getAndSet(int bitIndex, boolean value);

    /**
     * Sets the length and fills possible gaps with false/0 values
     * 
     * @param length
     * @return
     */
    public Bits setLength(int length);

    /**
     * Similar to {@link #setLength(int)} but allows to set the default value for gaps.
     * 
     * @param length
     * @param defaultValue
     * @return
     */
    public Bits setLength(int length, boolean defaultValue);

    public Stream<Bits> frames(int frameSize);

    public Bits append(Bits bits);

    public Bits subset(int startInclusive, int endExclusive);

    /**
     * Returns a new instance with a clone of the current instance.
     * 
     * @return
     */
    public Bits clone();

    /**
     * Returns this instance with the given {@link Bits} applied
     * 
     * @param bits
     * @return
     */
    public Bits and(Bits bits);

    public Bits or(Bits bits);

    public Bits xor(Bits bits);

    /**
     * Negates all available bits
     * 
     * @return
     */
    public Bits negate();

    /**
     * Reverses the current {@link Bits}
     * 
     * @return
     */
    public Bits reverse();

    public Stream<Boolean> toBooleanStream();

    /**
     * Partitions the current {@link Bits} into a {@link Stream} of smaller {@link Bits} with the given partition size. The last Bits will always have the left
     * over size, please set the {@link #setLength(int)} upfront to ensure that also the last {@link Bits} of the partitions will have the same partition size.
     * 
     * @param partitionSize
     * @return
     */
    public Stream<Bits> partition(int partitionSize);

    public boolean[] toBooleanArray();

    public boolean[] toReverseBooleanArray();

    public BinaryDigits toBinaryDigits();

    /**
     * Returns a {@link String} representation like e.g. "...0101010101" where the last character is the index = 0 of the Bits.
     * 
     * @see Bits#ofBinaryString(String)
     * @return
     */
    public String toBinaryString();

    public byte[] toBytes();

    public byte toByte();

    public BitSet toBitSet();

    /**
     * Returns an {@link IntStream} of the bit index positions with flags set to true.
     * 
     * @return
     */
    public IntStream toIndexPositions();

    /**
     * Finds the next bit index where the bit is set to false.
     * 
     * @see #findNextClearBitIndex(int)
     * @return
     */
    public OptionalInt findFirstClearBitIndex();

    /**
     * Finds the next bit index where the bit is set to true.
     * 
     * @see #findNextSetBitIndex(int)
     * @return
     */
    public OptionalInt findFirstSetBitIndex();

    /**
     * Finds the index of the last bit that is set.
     * 
     * @return
     */
    public OptionalInt findLastSetBitIndex();

    /**
     * @see #findFirstSetBitIndex()
     * @param startIndex
     * @return
     */
    public OptionalInt findNextSetBitIndex(int startIndex);

    /**
     * @see #findFirstClearBitIndex()
     * @param startIndex
     * @return
     */
    public OptionalInt findNextClearBitIndex(int startIndex);

    /**
     * Returns an array of the bit index positions with flags set to true.
     * 
     * @return
     */
    public int[] toIndexPositionArray();

    public Bits forEach(BiConsumer<Integer, Boolean> consumer);

    public static Bits newInstance()
    {
        return new BitSetBits();
    }

    public static Bits of(Bits bits)
    {
        return newInstance().or(bits);
    }

    public static Bits of(Bits... bits)
    {
        return Stream.of(bits)
                     .reduce(Bits.newInstance(), (b1, b2) -> Bits.newInstance()
                                                                 .append(b1)
                                                                 .append(b2));
    }

    public static Bits of(long value)
    {
        return newInstance().set(value);
    }

    public static Bits of(int value)
    {
        return newInstance().set((long) value)
                            .setLength(Integer.SIZE);
    }

    public static Bits of(byte value)
    {
        return newInstance().set((long) value)
                            .setLength(Byte.SIZE);
    }

    public static Bits of(boolean... values)
    {
        return newInstance().set(values);
    }

    public static Bits of(Boolean[] values)
    {
        return of(ArrayUtils.toPrimitive(values));
    }

    public static Bits of(byte[] values)
    {
        return newInstance().set(values);
    }

    public static Bits of(Byte[] values)
    {
        return of(ArrayUtils.toPrimitive(values));
    }

    public static Bits of(BitSet bitSet)
    {
        return newInstance().set(bitSet);
    }

    public static Bits of(BitSet bitSet, int length)
    {
        return newInstance().set(bitSet, length);
    }

    /**
     * Interprets {@link String}s in the format like e.g. "...010000101". The index = 0 of the Bits instance is represented by the last character.
     * 
     * @see Bits#toBinaryString()
     * @param binaryString
     * @return
     */
    public static Bits ofBinaryString(String binaryString)
    {
        return of(StreamUtils.reverse(Optional.ofNullable(binaryString)
                                              .map(String::toCharArray)
                                              .map(ArrayUtils::toObject)
                                              .map(Stream::of)
                                              .orElse(Stream.empty()))
                             .map(character -> character != '0')
                             .toArray(Boolean[]::new));
    }

    /**
     * Returns {@link Bits} instance with the give bit index positions set to true.
     * 
     * @param bitIndex
     * @return
     */
    public static Bits ofIndexPositions(int... bitIndex)
    {
        return newInstance().setIndex(bitIndex);
    }

}
