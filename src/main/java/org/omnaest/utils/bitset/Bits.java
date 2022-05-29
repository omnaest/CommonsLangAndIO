package org.omnaest.utils.bitset;

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.omnaest.utils.bitset.binary.BinaryDigits;

public interface Bits extends ImmutableBits
{

    public Bits flip(int bitIndex);

    public Bits clear(int bitIndex);

    public Bits set(long value);

    public Bits set(boolean[] values);

    public Bits set(byte[] values);

    public Bits set(int bitIndex, boolean value);

    public Bits set(int bitIndex);

    public Stream<Bits> frames(int frameSize);

    public Bits subset(int startInclusive, int endExclusive);

    public Stream<Boolean> toBooleanStream();

    public BinaryDigits toBinaryDigits();

    public byte[] toBytes();

    public static Bits newInstance()
    {
        return new BitSetBits();
    }

    public static Bits of(long value)
    {
        return newInstance().set(value);
    }

    public static Bits of(boolean[] values)
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

}
