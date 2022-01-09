package org.omnaest.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.omnaest.utils.ByteArrayUtils.MultiByteArrayContainer.ByteArrayContainerEntry;

public class ByteArrayUtilsTest
{

    @Test
    public void testSplitIntoTwoPotencySubArrays() throws Exception
    {
        {
            byte[][] subArrays = ByteArrayUtils.splitIntoTwoPotencySubArrays(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }, 1);
            assertEquals(4, subArrays.length);
            assertArrayEquals(new byte[] { 1 }, subArrays[0]);
            assertArrayEquals(new byte[] { 2, 3 }, subArrays[1]);
            assertArrayEquals(new byte[] { 4, 5, 6, 7 }, subArrays[2]);
            assertArrayEquals(new byte[] { 8 }, subArrays[3]);
        }
        {
            byte[][] subArrays = ByteArrayUtils.splitIntoTwoPotencySubArrays(new byte[] { 1, 2, 3, 4, 5, 6, 7 }, 1);
            assertEquals(3, subArrays.length);
            assertArrayEquals(new byte[] { 1 }, subArrays[0]);
            assertArrayEquals(new byte[] { 2, 3 }, subArrays[1]);
            assertArrayEquals(new byte[] { 4, 5, 6, 7 }, subArrays[2]);
        }
        {
            byte[][] subArrays = ByteArrayUtils.splitIntoTwoPotencySubArrays(new byte[] { 1, 2, 3, 4, 5, 6, 7 }, 2);
            assertEquals(3, subArrays.length);
            assertArrayEquals(new byte[] { 1, 2 }, subArrays[0]);
            assertArrayEquals(new byte[] { 3, 4, 5, 6 }, subArrays[1]);
            assertArrayEquals(new byte[] { 7 }, subArrays[2]);
        }
        {
            byte[][] subArrays = ByteArrayUtils.splitIntoTwoPotencySubArrays(new byte[] {}, 1);
            assertEquals(0, subArrays.length);
        }
        {
            assertNull(ByteArrayUtils.splitIntoTwoPotencySubArrays(null, 1));
        }
    }

    @Test
    public void testEncodeIntegerAsByteArray() throws Exception
    {
        {
            assertEquals(423333333, ByteArrayUtils.decodeIntegerFromByteArray(ByteArrayUtils.encodeIntegerAsByteArray(423333333)));
            assertArrayEquals(new int[] { 423333333 }, ByteArrayUtils.decodeIntegersFromByteArray(ByteArrayUtils.encodeIntegerAsByteArray(423333333)));
        }
        {
            assertEquals(8192, ByteArrayUtils.decodeIntegerFromByteArray(ByteArrayUtils.encodeIntegerAsByteArray(8192)));
        }
        {
            IntStream.iterate(Integer.MIN_VALUE, i -> i + Integer.MAX_VALUE / 100)
                     .limit(100)
                     .forEach(value ->
                     {
                         byte[] encodedValue = ByteArrayUtils.encodeIntegerAsByteArray(value);
                         assertEquals(Integer.BYTES, encodedValue.length);
                         assertEquals(value, ByteArrayUtils.decodeIntegerFromByteArray(encodedValue));
                     });
        }
        {
            IntStream.generate(() -> RandomUtils.nextInt())
                     .limit(100)
                     .forEach(value ->
                     {
                         byte[] encodedValue = ByteArrayUtils.encodeIntegerAsByteArray(value);
                         assertEquals(Integer.BYTES, encodedValue.length);
                         assertEquals(value, ByteArrayUtils.decodeIntegerFromByteArray(encodedValue));
                     });
        }
        {
            IntStream.generate(() -> -RandomUtils.nextInt())
                     .limit(100)
                     .forEach(value ->
                     {
                         byte[] encodedValue = ByteArrayUtils.encodeIntegerAsByteArray(value);
                         assertEquals(Integer.BYTES, encodedValue.length);
                         assertEquals(value, ByteArrayUtils.decodeIntegerFromByteArray(encodedValue));
                     });
        }
    }

    @Test
    public void testEnsureMinimumSizeByteArrayInt() throws Exception
    {
        assertArrayEquals(new byte[] { 1, 2, 0, 0 }, ByteArrayUtils.ensureMinimumSize(new byte[] { 1, 2 }, 4));
    }

    @Test
    public void testDecodeLongFromByteArray() throws Exception
    {
        {
            assertEquals(423333333333l, ByteArrayUtils.decodeLongFromByteArray(ByteArrayUtils.encodeLongAsByteArray(423333333333l)));
            assertArrayEquals(new long[] { 423333333333l }, ByteArrayUtils.decodeLongsFromByteArray(ByteArrayUtils.encodeLongAsByteArray(423333333333l)));
        }
        {
            assertEquals(8192, ByteArrayUtils.decodeLongFromByteArray(ByteArrayUtils.encodeLongAsByteArray(8192)));
        }
        {
            LongStream.range(0, 1000)
                      .forEach(value ->
                      {
                          byte[] encodedValue = ByteArrayUtils.encodeLongAsByteArray(value);
                          assertEquals(Long.BYTES, encodedValue.length);
                          assertEquals(value, ByteArrayUtils.decodeLongFromByteArray(encodedValue));
                      });
        }
        {
            LongStream.range(0, -1000)
                      .forEach(value ->
                      {
                          byte[] encodedValue = ByteArrayUtils.encodeLongAsByteArray(value);
                          assertEquals(Long.BYTES, encodedValue.length);
                          assertEquals(value, ByteArrayUtils.decodeLongFromByteArray(encodedValue));
                      });
        }
        {
            LongStream.iterate(Long.MIN_VALUE, i -> i + Long.MAX_VALUE / 100)
                      .limit(100)
                      .forEach(value ->
                      {
                          byte[] encodedValue = ByteArrayUtils.encodeLongAsByteArray(value);
                          assertEquals(Long.BYTES, encodedValue.length);
                          assertEquals(value, ByteArrayUtils.decodeLongFromByteArray(encodedValue));
                      });
        }
        {
            LongStream.generate(() -> RandomUtils.nextLong())
                      .limit(100)
                      .forEach(value ->
                      {
                          byte[] encodedValue = ByteArrayUtils.encodeLongAsByteArray(value);
                          assertEquals(Long.BYTES, encodedValue.length);
                          assertEquals(value, ByteArrayUtils.decodeLongFromByteArray(encodedValue));
                      });
        }
        {
            LongStream.generate(() -> -RandomUtils.nextLong())
                      .limit(100)
                      .forEach(value ->
                      {
                          byte[] encodedValue = ByteArrayUtils.encodeLongAsByteArray(value);
                          assertEquals(Long.BYTES, encodedValue.length);
                          assertEquals(value, ByteArrayUtils.decodeLongFromByteArray(encodedValue));
                      });
        }
    }

    @Test
    public void testToMultiByteArrayContainer() throws Exception
    {
        List<ByteArrayContainerEntry<String>> entries = ByteArrayUtils.toMultiByteArrayContainer(MapUtils.builder()
                                                                                                         .put("1", new byte[] { 0, 1 })
                                                                                                         .put("2", new byte[] { 2, 3 })
                                                                                                         .build())
                                                                      .stream()
                                                                      .collect(Collectors.toList());
        assertEquals(2, entries.size());
        assertEquals(SetUtils.toSet("1", "2"), entries.stream()
                                                      .map(ByteArrayContainerEntry<String>::getKey)
                                                      .collect(Collectors.toSet()));
        assertArrayEquals(new byte[] { 0, 1 }, entries.get(0)
                                                      .get()
                                                      .get());
        assertArrayEquals(new byte[] { 2, 3 }, entries.get(1)
                                                      .get()
                                                      .toByteArray());
    }

    @Test
    public void testToByteArrayContainer() throws Exception
    {
        assertEquals("abc", ByteArrayUtils.toByteArrayContainer("abc")
                                          .toString());
    }

}
