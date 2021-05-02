package org.omnaest.utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Helper around byte arrays
 * 
 * @author omnaest
 */
public class ByteArrayUtils
{
    public static byte[][] splitIntoTwoPotencySubArrays(byte[] array, int initialSize)
    {
        if (initialSize <= 0)
        {
            throw new IllegalArgumentException("initial size must be >= 1");
        }
        IntSupplier twoPotencySizeProvider = new IntSupplier()
        {
            private int size = initialSize;

            @Override
            public int getAsInt()
            {
                int size = this.size;
                this.size *= 2;
                return size;
            }
        };
        return splitIntoSubArrays(array, twoPotencySizeProvider);
    }

    public static byte[][] splitIntoSubArrays(byte[] array, IntSupplier sizeProvider)
    {
        if (array == null)
        {
            return null;
        }
        else
        {
            //
            byte[][] result;

            // determine number of sub arrays
            List<Integer> subArraySizes = new ArrayList<>();
            int sizeInSum = 0;
            while (sizeInSum < array.length)
            {
                int subArraySize = sizeProvider.getAsInt();
                subArraySizes.add(subArraySize);
                sizeInSum += subArraySize;
            }
            result = new byte[subArraySizes.size()][];

            // fill sub arrays
            int lastEndPositionExclusive = 0;
            for (int ii = 0; ii < subArraySizes.size(); ii++)
            {
                int subArraySize = subArraySizes.get(ii);
                int startPositionInclusive = lastEndPositionExclusive;
                int endPositionExclusive = Math.min(startPositionInclusive + subArraySize, array.length);
                byte[] subArray = Arrays.copyOfRange(array, startPositionInclusive, endPositionExclusive);
                result[ii] = subArray;
                lastEndPositionExclusive = endPositionExclusive;
            }

            return result;
        }
    }

    public static int[] decodeIntegersFromByteArray(byte[] array)
    {
        return IntBuffer.allocate(array.length / Integer.BYTES)
                        .put(((ByteBuffer) ByteBuffer.wrap(array)
                                                     .position(0)).asIntBuffer())
                        .array();
    }

    public static long[] decodeLongsFromByteArray(byte[] array)
    {
        return LongBuffer.allocate(array.length / Long.BYTES)
                         .put(((ByteBuffer) ByteBuffer.wrap(array)
                                                      .position(0)).asLongBuffer())
                         .array();
    }

    public static byte[] encodeIntegerAsByteArray(int value)
    {
        return ByteBuffer.allocate(Integer.BYTES)
                         .putInt(value)
                         .array();
    }

    public static byte[] encodeLongAsByteArray(long value)
    {
        return ByteBuffer.allocate(Long.BYTES)
                         .putLong(value)
                         .array();
    }

    public static int decodeIntegerFromByteArray(byte[] array)
    {
        return ByteBuffer.allocate(Integer.BYTES)
                         .put(array)
                         .getInt(0);
    }

    public static long decodeLongFromByteArray(byte[] array)
    {
        return ByteBuffer.allocate(Long.BYTES)
                         .put(array)
                         .getLong(0);
    }

    public static byte[] ensureMinimumSize(byte[] array, int size)
    {
        return ensureMinimumSize(array, size, (byte) 0);
    }

    public static byte[] ensureMinimumSize(byte[] array, int size, byte defaultValue)
    {
        if (array != null)
        {
            byte[] result = new byte[Math.max(array.length, size)];
            for (int ii = 0; ii < array.length; ii++)
            {
                result[ii] = array[ii];
            }
            return result;
        }
        else
        {
            return new byte[size];
        }
    }

}
