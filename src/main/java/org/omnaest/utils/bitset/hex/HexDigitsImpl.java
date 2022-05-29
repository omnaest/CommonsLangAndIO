package org.omnaest.utils.bitset.hex;

import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.omnaest.utils.bitset.Bits;

public class HexDigitsImpl implements HexDigits
{
    private HexDigit[] digits;

    public HexDigitsImpl(long value)
    {

        super();

        this.digits = IntStream.range(0, Long.SIZE / 4)
                               .map(i -> (int) (value >> (i * 4) & 0xF))
                               .mapToObj(positionValue -> HexDigit.values()[positionValue])
                               .toArray(size -> new HexDigit[size]);
    }

    public HexDigitsImpl(Bits bits)
    {
        super();

        this.digits = bits.frames(4)
                          .mapToInt(Bits::toInt)
                          .mapToObj(positionValue -> HexDigit.values()[positionValue])
                          .toArray(size -> new HexDigit[size]);
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
            printCharacters |= ii == 0 || !HexDigit.Hex_0.equals(this.digits[ii]);
            if (printCharacters)
            {
                result.append(StringUtils.right(this.digits[ii].name(), 1));
            }
        }

        return result.toString();
    }

    @Override
    public String get()
    {
        return this.toUpperCaseString();
    }

}
