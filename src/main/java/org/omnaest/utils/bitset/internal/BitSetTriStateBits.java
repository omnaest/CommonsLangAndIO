package org.omnaest.utils.bitset.internal;

import java.util.stream.Collectors;

import org.omnaest.utils.bitset.TriStateBits;
import org.omnaest.utils.bitset.TriStateBits.TriState;

public class BitSetTriStateBits extends BitSetEnumBits<TriState> implements TriStateBits
{

    public BitSetTriStateBits()
    {
        super(TriState.class);
    }

    @Override
    public String toString()
    {
        return this.toList()
                   .stream()
                   .map(state ->
                   {
                       if (TriState.TRUE.equals(state))
                       {
                           return "1";
                       }
                       else if (TriState.FALSE.equals(state))
                       {
                           return "0";
                       }
                       else
                       {
                           return "?";
                       }
                   })
                   .collect(Collectors.joining());
    }

}
