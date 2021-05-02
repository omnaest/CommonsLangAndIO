package org.omnaest.utils.file.storage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.utils.ByteArrayUtils;
import org.omnaest.utils.FileUtils;

import com.google.common.collect.ImmutableList;

public class RandomAccessLogarithmicBlockFileStorageTest
{
    private RandomAccessLogarithmicBlockFileStorage fileStorage = RandomAccessLogarithmicBlockFileStorage.of(FileUtils.createRandomTempDirectoryQuietly()
                                                                                                                      .orElseThrow(() -> new IllegalStateException("Unable to create temp folder")))
                                                                                                         .withInitialBlockSize(64);

    @Test
    public void testWriteAndReadByteArray() throws Exception
    {

        List<byte[]> dataSet = ImmutableList.<byte[]>builder()
                                            .add(ByteArrayUtils.encodeIntegerAsByteArray((int) (System.currentTimeMillis() % 9999)))
                                            .add(ByteArrayUtils.encodeIntegerAsByteArray((int) (System.currentTimeMillis() % 7777)))
                                            .add(ByteArrayUtils.encodeIntegerAsByteArray((int) (System.currentTimeMillis() % 3333)))
                                            .build();

        IntStream.range(0, dataSet.size())
                 .forEach(index -> this.fileStorage.write(index, dataSet.get(index)));

        IntStream.range(0, dataSet.size())
                 .forEach(index -> assertArrayEquals(dataSet.get(index), this.fileStorage.read(index)));
    }

    @Test
    public void testWriteAndReadString() throws Exception
    {
        StringBlockFileStorage stringBlockFileStorage = this.fileStorage.asStringBlockFileStorage();

        for (int ii = 0; ii < 5; ii++)
        {
            List<String> dataSet = ImmutableList.<String>builder()
                                                .add(StringUtils.repeat("" + RandomUtils.nextInt(10), RandomUtils.nextInt(128)))
                                                .add(StringUtils.repeat("" + RandomUtils.nextInt(10), RandomUtils.nextInt(1000)))
                                                .add(StringUtils.repeat("" + RandomUtils.nextInt(10), RandomUtils.nextInt(10000)))
                                                .build();

            IntStream.range(0, dataSet.size())
                     .forEach(index -> stringBlockFileStorage.write(index, dataSet.get(index)));

            IntStream.range(0, dataSet.size())
                     .forEach(index -> assertEquals(dataSet.get(index), stringBlockFileStorage.read(index)));
        }
    }

    @Test
    @Ignore
    public void testWriteAndReadStringLocally() throws Exception
    {
        StringBlockFileStorage stringBlockFileStorage = RandomAccessLogarithmicBlockFileStorage.of(new File("C://Temp//blockFileStorageTest"))
                                                                                               .withInitialBlockSize(64)
                                                                                               .asStringBlockFileStorage();

        List<String> dataSet = ImmutableList.<String>builder()
                                            .add(StringUtils.repeat("" + RandomUtils.nextInt(10), RandomUtils.nextInt(128)))
                                            .add(StringUtils.repeat("" + RandomUtils.nextInt(10), RandomUtils.nextInt(1000)))
                                            .add(StringUtils.repeat("" + RandomUtils.nextInt(10), RandomUtils.nextInt(10000)))
                                            .build();

        stringBlockFileStorage.write(0, dataSet.get(0));

        assertEquals(dataSet.get(0), stringBlockFileStorage.read(0));

    }

}
