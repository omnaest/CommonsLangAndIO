package org.omnaest.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.omnaest.utils.exception.RuntimeIOException;
import org.omnaest.utils.stream.Streamable;

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

    public static ByteArrayContainer toByteArrayContainer(String text)
    {
        return toByteArrayContainer(Optional.ofNullable(text)
                                            .map(string -> string.getBytes(StandardCharsets.UTF_8))
                                            .orElse(new byte[0]));
    }

    public static ByteArrayContainer toByteArrayContainer(byte[] data)
    {
        return new ByteArrayContainer()
        {
            @Override
            public byte[] get()
            {
                return data;
            }

            @Override
            public InputStream toInputStream()
            {
                return new ByteArrayInputStream(data);
            }

            @Override
            public String toString()
            {
                try
                {
                    return IOUtils.toString(this.toInputStream(), StandardCharsets.UTF_8);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }

        };
    }

    public static <K> MultiByteArrayContainer<K> toMultiByteArrayContainer(Map<K, byte[]> keyToData)
    {
        return new MultiByteArrayContainer<K>()
        {
            @Override
            public Stream<ByteArrayContainerEntry<K>> stream()
            {
                return Optional.ofNullable(keyToData)
                               .map(Map::entrySet)
                               .map(Set<Entry<K, byte[]>>::stream)
                               .orElse(Stream.empty())
                               .map(entry -> new ByteArrayContainerEntry<K>()
                               {
                                   @Override
                                   public K getKey()
                                   {
                                       return entry.getKey();
                                   }

                                   @Override
                                   public ByteArrayContainer get()
                                   {
                                       return toByteArrayContainer(entry.getValue());
                                   }
                               });
            }
        };
    }

    public static interface ByteArrayContainer extends Supplier<byte[]>
    {
        public InputStream toInputStream();

        /**
         * Similar to {@link #get()}
         * 
         * @return
         */
        public default byte[] toByteArray()
        {
            return this.get();
        }

        /**
         * Returns the {@link String} representation of the {@link Byte}s as {@link StandardCharsets#UTF_8} encoded {@link String}
         * 
         * @return
         */
        @Override
        public String toString();
    }

    public static interface MultiByteArrayContainer<K> extends Streamable<ByteArrayContainerEntry<K>>
    {
    }

    public static interface ByteArrayContainerEntry<K> extends Supplier<ByteArrayContainer>
    {
        public K getKey();

        @Override
        public ByteArrayContainer get();

    }

}
