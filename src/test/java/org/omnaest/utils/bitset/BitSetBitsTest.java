package org.omnaest.utils.bitset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class BitSetBitsTest
{

    @Test
    public void testEquals() throws Exception
    {
        assertEquals(Bits.of(123), Bits.of(123));
        assertNotEquals(Bits.of(123), Bits.of(124));
    }

    @Test
    public void testToString() throws Exception
    {
        assertEquals("1", Bits.of(1)
                              .toString());
        assertEquals("10", Bits.of(2)
                               .toString());
        assertEquals("11", Bits.of(3)
                               .toString());
        assertEquals("111", Bits.of(7)
                                .toString());
        assertEquals("1000", Bits.of(8)
                                 .toString());
    }

    @Test
    public void testSetByteArray() throws Exception
    {
        assertEquals("100000101011111", Bits.of(new byte[] { 0b01011111, 0b01000001 })
                                            .toString());
    }

    @Test
    public void testSetBooleanArray() throws Exception
    {
        assertEquals("101001", Bits.of(new boolean[] { true, false, false, true, false, true })
                                   .toString());
    }

    @Test
    public void testToBytes() throws Exception
    {
        byte[] bytes = "I am a little fox".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(bytes, Bits.of(bytes)
                                     .toBytes());
    }

}
