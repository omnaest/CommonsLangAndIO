package org.omnaest.utils.bitset.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.omnaest.utils.bitset.Bits;
import org.omnaest.utils.bitset.MultiBits;

public class BitSetMultiBitsTest
{
    @Test
    public void testGet() throws Exception
    {
        MultiBits bits = MultiBits.newInstance(64);
        assertEquals(127, bits.setIndex(0, Bits.of(127))
                              .get(0)
                              .toInt());
        assertEquals(91, bits.setIndex(1, Bits.of(91))
                             .get(1)
                             .toInt());
    }
}
