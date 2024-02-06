package org.omnaest.utils.bitset.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.omnaest.utils.bitset.TriStateBits;
import org.omnaest.utils.bitset.TriStateBits.TriState;

public class BitSetTriStateBitsTest
{

    @Test
    public void testToList() throws Exception
    {
        assertEquals(Arrays.asList(TriState.TRUE, TriState.FALSE, TriState.UNDEFINED), TriStateBits.newInstance()
                                                                                                   .setIndex(0, TriState.TRUE)
                                                                                                   .setIndex(1, TriState.FALSE)
                                                                                                   .setIndex(2, TriState.UNDEFINED)
                                                                                                   .toList());
    }

    @Test
    public void testInitialValue() throws Exception
    {
        assertEquals(TriState.UNDEFINED, TriStateBits.newInstance()
                                                     .setLength(1)
                                                     .get(0));
    }
}
