package org.omnaest.utils.bitset;

import org.omnaest.utils.bitset.hex.HexDigits;

public interface ImmutableBits
{
    public int getLength();

    public boolean get(int bitIndex);

    public boolean getOrDefault(int bitIndex, boolean defaultValue);

    public boolean getOrDefault(int bitIndex);

    public HexDigits toHexDigits();

    public int toInt();
}
