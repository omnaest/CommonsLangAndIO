package org.omnaest.utils.bitset.binary;

import org.omnaest.utils.bitset.Bits;

public interface BinaryDigits
{
    public String toUpperCaseString();

    public static BinaryDigits of(Bits bits)
    {
        return new BinaryDigitsImpl(bits);
    }

    public static enum BinaryDigit
    {
        Binary_0, Binary_1
    }

}
