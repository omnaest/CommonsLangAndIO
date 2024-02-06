package org.omnaest.utils.bitset;

import org.omnaest.utils.bitset.TriStateBits.TriState;
import org.omnaest.utils.bitset.internal.BitSetTriStateBits;

public interface TriStateBits extends EnumBits<TriState>
{
    public static enum TriState
    {
        UNDEFINED, FALSE, TRUE
    }

    public static TriStateBits newInstance()
    {
        return new BitSetTriStateBits();
    }

}
