package org.omnaest.utils.bitset.binary;

import org.apache.commons.lang3.StringUtils;
import org.omnaest.utils.bitset.Bits;

public class BinaryDigitsImpl implements BinaryDigits
{
    private BinaryDigit[] digits;

    public BinaryDigitsImpl(Bits bits)
    {
        super();
        this.digits = bits.toBooleanStream()
                          .map(value -> value ? BinaryDigit.Binary_1 : BinaryDigit.Binary_0)
                          .toArray(size -> new BinaryDigit[size]);
    }

    @Override
    public String toString()
    {
        return this.toUpperCaseString();
    }

    @Override
    public String toUpperCaseString()
    {
        StringBuilder result = new StringBuilder();

        boolean printCharacters = false;
        for (int ii = this.digits.length - 1; ii >= 0; ii--)
        {
            printCharacters |= ii == 0 || !BinaryDigit.Binary_0.equals(this.digits[ii]);
            if (printCharacters)
            {
                result.append(StringUtils.right(this.digits[ii].name(), 1));
            }
        }

        return result.toString();
    }
}
