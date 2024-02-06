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
        assertEquals("100000000000000000000000000000000000000000000000000000000000000", Bits.of(1)
                                                                                            .toString());
        assertEquals("010000000000000000000000000000000000000000000000000000000000000", Bits.of(2)
                                                                                            .toString());
        assertEquals("110000000000000000000000000000000000000000000000000000000000000", Bits.of(3)
                                                                                            .toString());
        assertEquals("111000000000000000000000000000000000000000000000000000000000000", Bits.of(7)
                                                                                            .toString());
        assertEquals("000100000000000000000000000000000000000000000000000000000000000", Bits.of(8)
                                                                                            .toString());
    }

    @Test
    public void testToBinaryDigits() throws Exception
    {
        assertEquals("1", Bits.of(1)
                              .toBinaryDigits()
                              .toString());
        assertEquals("10", Bits.of(2)
                               .toBinaryDigits()
                               .toString());
        assertEquals("11", Bits.of(3)
                               .toBinaryDigits()
                               .toString());
        assertEquals("111", Bits.of(7)
                                .toBinaryDigits()
                                .toString());
        assertEquals("1000", Bits.of(8)
                                 .toBinaryDigits()
                                 .toString());
    }

    @Test
    public void testSetByteArray() throws Exception
    {
        assertEquals("100000101011111", Bits.of(new byte[] { 0b01011111, 0b01000001 })
                                            .toBinaryDigits()
                                            .toString());
    }

    @Test
    public void testSetBooleanArray() throws Exception
    {
        assertEquals("101001", Bits.of(new boolean[] { true, false, false, true, false, true })
                                   .toBinaryDigits()
                                   .toString());
    }

    @Test
    public void testToBytes() throws Exception
    {
        byte[] bytes = "I am a little fox".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(bytes, Bits.of(bytes)
                                     .toBytes());
    }

    @Test
    public void testToIndexPositionArray() throws Exception
    {
        assertArrayEquals(new int[] { 0, 3, 8 }, Bits.ofIndexPositions(new int[] { 0, 3, 8 })
                                                     .toIndexPositionArray());
    }

    @Test
    public void testToBitSet() throws Exception
    {
        assertArrayEquals(new int[] { 0, 3, 8 }, Bits.ofIndexPositions(new int[] { 0, 3, 8 })
                                                     .clone()
                                                     .toIndexPositionArray());
    }

    @Test
    public void testAnd() throws Exception
    {
        assertArrayEquals(new int[] { 3 }, Bits.ofIndexPositions(new int[] { 0, 3, 8 })
                                               .and(Bits.ofIndexPositions(new int[] { 3, 5 }))
                                               .toIndexPositionArray());
    }

    @Test
    public void testOr() throws Exception
    {
        assertArrayEquals(new int[] { 0, 3, 5, 8 }, Bits.ofIndexPositions(new int[] { 0, 3, 8 })
                                                        .or(Bits.ofIndexPositions(new int[] { 3, 5 }))
                                                        .toIndexPositionArray());
    }

    @Test
    public void testXor() throws Exception
    {
        assertArrayEquals(new int[] { 0, 5, 8 }, Bits.ofIndexPositions(new int[] { 0, 3, 8 })
                                                     .xor(Bits.ofIndexPositions(new int[] { 3, 5 }))
                                                     .toIndexPositionArray());
    }

    @Test
    public void testNegate() throws Exception
    {
        assertArrayEquals(new boolean[] { false, true, false }, Bits.of(new boolean[] { true, false, true })
                                                                    .negate()
                                                                    .toBooleanArray());
    }

    @Test
    public void testSetLengthIntBoolean() throws Exception
    {
        assertArrayEquals(new boolean[] { true, false }, Bits.of(new boolean[] { true, false, false })
                                                             .setLength(2)
                                                             .toBooleanArray());
        assertArrayEquals(new boolean[] { false, false, false }, Bits.of(new boolean[] { false, false, false, true })
                                                                     .setLength(3)
                                                                     .toBooleanArray());
        assertArrayEquals(new boolean[] { false, false, false, true, true }, Bits.of(new boolean[] { false, false, false })
                                                                                 .setLength(5, true)
                                                                                 .toBooleanArray());
    }

    @Test
    public void testAppend() throws Exception
    {
        assertEquals(Bits.of(true, false, true), Bits.of(true)
                                                     .append(Bits.of(false, true)));
        assertEquals(Bits.of(false, true, false), Bits.of(false)
                                                      .append(Bits.of(true, false)));
    }

    @Test
    public void testDrainFromLeft() throws Exception
    {
        {
            Bits bits = Bits.of(false, true, true, false);
            Bits removedBits = bits.drainFromLeft(1);
            assertEquals(Bits.of(true, true, false), bits);
            assertEquals(Bits.of(false), removedBits);
        }
        {
            Bits bits = Bits.of(false, true, true, false);
            Bits removedBits = bits.drainFromLeft(4);
            assertEquals(Bits.newInstance(), bits);
            assertEquals(Bits.of(false, true, true, false), removedBits);
        }
        {
            Bits bits = Bits.of(false, true, true, false);
            Bits removedBits = bits.drainFromLeft(5);
            assertEquals(Bits.newInstance(), bits);
            assertEquals(Bits.of(false, true, true, false), removedBits);
        }
    }

    @Test
    public void testShiftLeft() throws Exception
    {
        assertEquals(Bits.of(true, false, false), Bits.of(false, true, false)
                                                      .shiftLeft(1));
        assertEquals(Bits.of(false, true, false), Bits.of(true, false, true)
                                                      .shiftLeft(1));
    }

    @Test
    public void testDrainFromLeftOrDefaultInt() throws Exception
    {
        Bits bits = Bits.of(false, true, true, false);
        Bits removedBits = bits.drainFromLeftOrDefault(5);
        assertEquals(Bits.newInstance(), bits);
        assertEquals(Bits.of(false, true, true, false, false), removedBits);
    }

    @Test
    public void testDrainBlocksFromLeftOfSize() throws Exception
    {
        assertEquals(Bits.of(true, false, true, false, true, false), Bits.of(true, false, true, false, true)
                                                                         .drainBlocksFromLeftOfSize(2)
                                                                         .reduce(Bits::append)
                                                                         .get());
    }

    @Test
    public void testDrainBlocksFromLeftOfMaxSize() throws Exception
    {
        assertEquals(Bits.of(true, false, true, false, true), Bits.of(true, false, true, false, true)
                                                                  .drainBlocksFromLeftOfMaxSize(2)
                                                                  .reduce(Bits::append)
                                                                  .get());
    }

    @Test
    public void testGetOrSetIntBoolean() throws Exception
    {
        assertEquals(true, Bits.of(true)
                               .getOrSet(1, true));
    }

}
