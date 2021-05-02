package org.omnaest.utils.file.storage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import org.omnaest.utils.ByteArrayUtils;
import org.omnaest.utils.FileUtils;
import org.omnaest.utils.IOUtils;
import org.omnaest.utils.MathUtils;
import org.omnaest.utils.file.CommitableFile;

public class RandomAccessLogarithmicBlockFileStorage implements ByteArrayBlockFileStorage
{
    private File            directory;
    private int             initialBlockSize = 64 * 1024;
    private CompressionMode compressionMode  = CompressionMode.COMPRESSED;

    public static enum CompressionMode
    {
        UNCOMPRESSED, COMPRESSED
    }

    private RandomAccessLogarithmicBlockFileStorage(File directory)
    {
        super();
        this.directory = directory;
    }

    public RandomAccessLogarithmicBlockFileStorage withCompressionMode(CompressionMode compressionMode)
    {
        this.compressionMode = compressionMode;
        return this;
    }

    public RandomAccessLogarithmicBlockFileStorage withInitialBlockSize(int initialBlockSize)
    {
        if (initialBlockSize > 0)
        {
            this.initialBlockSize = initialBlockSize;
        }
        return this;
    }

    public static RandomAccessLogarithmicBlockFileStorage of(File directory)
    {
        return new RandomAccessLogarithmicBlockFileStorage(directory);
    }

    @Override
    public void write(int rowIndex, byte[] data)
    {
        byte[][] subArrays = ByteArrayUtils.splitIntoTwoPotencySubArrays(Optional.ofNullable(data)
                                                                                 .map(this.createCompressor())
                                                                                 .orElse(new byte[0]),
                                                                         this.initialBlockSize);
        IntStream.range(0, subArrays.length)
                 .forEach(subArrayIndex -> this.createPartition(subArrayIndex)
                                               .write(rowIndex, subArrays[subArrayIndex]));
        this.createPartition(subArrays.length)
            .write(rowIndex, new byte[0]); // end token
    }

    private UnaryOperator<byte[]> createCompressor()
    {
        if (CompressionMode.COMPRESSED.equals(this.compressionMode))
        {
            return data -> IOUtils.compress(data);
        }
        else
        {
            return data -> data;
        }
    }

    private UnaryOperator<byte[]> createUncompressor()
    {
        if (CompressionMode.COMPRESSED.equals(this.compressionMode))
        {
            return data -> IOUtils.uncompress(data);
        }
        else
        {
            return data -> data;
        }
    }

    private PartitionFileAccessor createPartition(int subArrayIndex)
    {
        return new PartitionFileAccessor(subArrayIndex, this.initialBlockSize, this.directory);
    }

    @Override
    public byte[] read(int rowIndex)
    {
        //
        List<byte[]> subArrays = new ArrayList<>();
        {
            byte[] subArray = null;
            int subArrayIndex = 0;
            do
            {
                PartitionFileAccessor partitionFileAccessor = this.createPartition(subArrayIndex++);

                subArray = partitionFileAccessor.read(rowIndex);
                subArrays.add(subArray);
            } while (subArray.length > 0);
        }

        //
        int length = 0;
        {
            for (byte[] subArray : subArrays)
            {
                length += subArray.length;
            }
        }

        //
        byte[] result = new byte[length];
        {
            int position = 0;
            for (byte[] subArray : subArrays)
            {
                for (byte value : subArray)
                {
                    result[position++] = value;
                }
            }
        }

        return Optional.ofNullable(result)
                       .map(this.createUncompressor())
                       .orElse(new byte[0]);
    }

    private static class PartitionFileAccessor
    {
        private int  subArrayIndex;
        private File directory;
        private int  initialBlockSize;

        public PartitionFileAccessor(int subArrayIndex, int initialBlockSize, File directory)
        {
            super();
            this.subArrayIndex = subArrayIndex;
            this.initialBlockSize = initialBlockSize;
            this.directory = directory;
        }

        public byte[] read(int rowIndex)
        {
            return CommitableFile.of(this.directory)
                                 .operateOnCurrentFiles(fileProvider ->
                                 {
                                     File dataFile = fileProvider.apply(this.determineDataFileSlot());
                                     File mappingFile = fileProvider.apply(this.determineMappingFileSlot());

                                     // remaining byte size, rowIndex in data file
                                     long position = (Integer.BYTES + Long.BYTES) * rowIndex;
                                     int size = mappingFile.exists() ? FileUtils.toRandomFileAccessor(mappingFile)
                                                                                .atPosition(position)
                                                                                .readInteger()
                                             : 0;
                                     if (size > 0)
                                     {
                                         long dataFileRowPosition = FileUtils.toRandomFileAccessor(mappingFile)
                                                                             .atPosition(position)
                                                                             .skip(Integer.BYTES)
                                                                             .readLong();

                                         int blockSize = this.determineBlockSize();
                                         return Arrays.copyOf(FileUtils.toRandomFileAccessor(dataFile)
                                                                       .atPosition(dataFileRowPosition * blockSize)
                                                                       .readBytes(blockSize),
                                                              size);
                                     }
                                     else
                                     {
                                         return new byte[0];
                                     }
                                 });
        }

        public void write(int rowIndex, byte[] subArray)
        {
            CommitableFile.of(this.directory)
                          .transaction()
                          .operateOnFiles(fileProvider ->
                          {
                              File dataFile = fileProvider.apply(this.determineDataFileSlot());
                              File mappingFile = fileProvider.apply(this.determineMappingFileSlot());

                              // remaining byte size, rowIndex in data file
                              long position = (Integer.BYTES + Long.BYTES) * rowIndex;
                              int previousSize = mappingFile.exists() ? FileUtils.toRandomFileAccessor(mappingFile)
                                                                                 .atPosition(position)
                                                                                 .readInteger()
                                      : 0;
                              long previousDataFileRowPosition = mappingFile.exists() ? FileUtils.toRandomFileAccessor(mappingFile)
                                                                                                 .atPosition(position)
                                                                                                 .skip(Integer.BYTES)
                                                                                                 .readLong()
                                      : 0;

                              long dataFileRowPosition = previousSize > 0 ? previousDataFileRowPosition : this.determineNextDataFileRowPosition(dataFile);
                              if (dataFileRowPosition < 0)
                              {
                                  throw new IllegalStateException("row position must be greater or equal to zero, but was " + dataFileRowPosition);
                              }
                              FileUtils.toRandomFileAccessor(mappingFile)
                                       .atPosition(position)
                                       .write(ByteArrayUtils.encodeIntegerAsByteArray(subArray.length))
                                       .write(ByteArrayUtils.encodeLongAsByteArray(dataFileRowPosition));

                              int blockSize = this.determineBlockSize();
                              FileUtils.toRandomFileAccessor(dataFile)
                                       .atPosition(dataFileRowPosition * blockSize)
                                       .write(ByteArrayUtils.ensureMinimumSize(subArray, blockSize));
                          })
                          .commitFull();
        }

        private int determineDataFileSlot()
        {
            return this.subArrayIndex * 2;
        }

        private int determineMappingFileSlot()
        {
            return this.subArrayIndex * 2 + 1;
        }

        private long determineNextDataFileRowPosition(File dataFile)
        {
            int blockSize = this.determineBlockSize();
            long nextDataFileRowPosition = dataFile.length() / blockSize;
            return nextDataFileRowPosition;
        }

        private int determineBlockSize()
        {
            return this.initialBlockSize * MathUtils.pow2(this.subArrayIndex);
        }

    }

    @Override
    public StringBlockFileStorage asStringBlockFileStorage()
    {
        return new StringBlockFileStorage()
        {
            @Override
            public void write(int rowIndex, String content)
            {
                RandomAccessLogarithmicBlockFileStorage.this.write(rowIndex, Optional.ofNullable(content)
                                                                                     .map(contentText -> contentText.getBytes(StandardCharsets.UTF_8))
                                                                                     .orElse(null));
            }

            @Override
            public String read(int rowIndex)
            {
                return new String(RandomAccessLogarithmicBlockFileStorage.this.read(rowIndex), StandardCharsets.UTF_8);
            }

        };
    }
}
