package org.omnaest.utils.bitset.hex;

import java.util.function.Supplier;

import org.omnaest.utils.bitset.Bits;

public interface HexDigits extends Supplier<String>
{
    public String toUpperCaseString();

    public static HexDigits of(long value)
    {
        return new HexDigitsImpl(value);
    }

    public static HexDigits of(Bits bits)
    {
        return new HexDigitsImpl(bits);
    }

    public static enum HexDigit
    {
        Hex_0, Hex_1, Hex_2, Hex_3, Hex_4, Hex_5, Hex_6, Hex_7, Hex_8, Hex_9, Hex_A, Hex_B, Hex_C, Hex_D, Hex_E, Hex_F
    }

}
