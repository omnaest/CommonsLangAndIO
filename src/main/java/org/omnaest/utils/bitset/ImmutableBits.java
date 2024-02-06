package org.omnaest.utils.bitset;

import org.omnaest.utils.bitset.hex.HexDigits;

public interface ImmutableBits
{
    public int getLength();

    /**
     * Returns true, if {@link #getLength()} is equal to zero.
     * 
     * @return
     */
    public boolean isEmpty();

    /**
     * Returns true, if {@link #getLength()} is greater than zero.
     * 
     * @return
     */
    public boolean isNotEmpty();

    public boolean get(int bitIndex);

    public boolean getOrDefault(int bitIndex, boolean defaultValue);

    public boolean getOrDefault(int bitIndex);

    public HexDigits toHexDigits();

    public int toInt();

    public boolean hasAnyBitEqualTo(boolean value);
}
