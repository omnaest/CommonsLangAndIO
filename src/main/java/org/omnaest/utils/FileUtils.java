/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.ArrayUtils;
import org.omnaest.utils.FileUtils.FileReaderLoader.BatchFileReader;
import org.omnaest.utils.FileUtils.FileReaderLoader.SingleFileReader;
import org.omnaest.utils.exception.RuntimeIOException;
import org.omnaest.utils.exception.handler.ExceptionHandler;
import org.omnaest.utils.functional.Accessor;

import lombok.Builder;
import lombok.Data;

/**
 * Utils regarding {@link File} operations
 * 
 * @author omnaest
 */
public class FileUtils
{

    private static abstract class AbstractFileContentConsumer<E, FCC extends FileContentConsumer<E, FCC>> implements FileContentConsumer<E, FCC>
    {
        protected File file;

        public AbstractFileContentConsumer(File file)
        {
            super();
            this.file = file;
        }

        @Override
        public <T> Consumer<T> with(Function<T, E> serializer)
        {
            return serializationObject -> this.accept(serializer.apply(serializationObject));
        }
    }

    private static abstract class AbstractCharacterSetFileContentConsumer<E, FCC extends FileCharacterSetContentConsumer<E, FCC>>
            extends AbstractFileContentConsumer<E, FCC> implements FileCharacterSetContentConsumer<E, FCC>
    {
        protected Charset charset = StandardCharsets.UTF_8;

        public AbstractCharacterSetFileContentConsumer(File file)
        {
            super(file);
        }

        @SuppressWarnings("unchecked")
        @Override
        public FCC using(Charset charset)
        {
            this.charset = charset;
            return (FCC) this;
        }

        @Override
        public FCC usingUTF8()
        {
            return this.using(StandardCharsets.UTF_8);
        }

    }

    private static class FileStringContentConsumerImpl extends AbstractCharacterSetFileContentConsumer<String, FileStringContentConsumer>
            implements FileStringContentConsumer
    {
        private FileStringContentConsumerImpl(File file)
        {
            super(file);
        }

        @Override
        public void accept(String data)
        {
            try
            {
                if (data != null)
                {
                    org.apache.commons.io.FileUtils.write(this.file, data, this.charset);
                }
                else
                {
                    org.apache.commons.io.FileUtils.deleteQuietly(this.file);
                }
            }
            catch (IOException e)
            {
                throw new FileAccessException(e);
            }

        }

    }

    private static class FileByteArrayContentConsumerImpl extends AbstractFileContentConsumer<byte[], FileByteArrayContentConsumer>
            implements FileByteArrayContentConsumer
    {
        private FileByteArrayContentConsumerImpl(File file)
        {
            super(file);
        }

        @Override
        public void accept(byte[] data)
        {
            try
            {
                if (data != null)
                {
                    org.apache.commons.io.FileUtils.writeByteArrayToFile(this.file, data);
                }
                else
                {
                    org.apache.commons.io.FileUtils.deleteQuietly(this.file);
                }
            }
            catch (IOException e)
            {
                throw new FileAccessException(e);
            }

        }

    }

    private static class FileStreamContentConsumerImpl extends AbstractCharacterSetFileContentConsumer<Stream<String>, FileStreamContentConsumer>
            implements FileStreamContentConsumer
    {
        private FileStreamContentConsumerImpl(File file)
        {
            super(file);
        }

        @Override
        public void accept(Stream<String> data)
        {
            try
            {
                if (data != null)
                {
                    writeLines(data, this.file, this.charset);
                }
                else
                {
                    org.apache.commons.io.FileUtils.deleteQuietly(this.file);
                }
            }
            catch (IOException e)
            {
                throw new FileAccessException(e);
            }

        }

    }

    /**
     * {@link Consumer} of {@link String} which will be written into the underlying {@link File}
     * 
     * @author omnaest
     */
    public static interface FileByteArrayContentConsumer extends FileContentConsumer<byte[], FileByteArrayContentConsumer>
    {

    }

    /**
     * {@link Consumer} of {@link String} which will be written into the underlying {@link File}
     * 
     * @author omnaest
     */
    public static interface FileStringContentConsumer extends FileCharacterSetContentConsumer<String, FileStringContentConsumer>
    {

    }

    /**
     * {@link Consumer} of a {@link Stream} of {@link String} lines which will write into an underlying {@link File}
     * 
     * @author omnaest
     */
    public static interface FileStreamContentConsumer extends FileCharacterSetContentConsumer<Stream<String>, FileStreamContentConsumer>
    {

    }

    /**
     * {@link Consumer} which writes into an underlying {@link File}
     * 
     * @author omnaest
     * @param <E>
     */
    public static interface FileContentConsumer<E, FCC extends FileContentConsumer<E, FCC>> extends Consumer<E>
    {

        /**
         * Writes the given {@link String} to the underlying {@link File}.<br>
         * <br>
         * If null is given, then the file is deleted
         * 
         * @throws FileAccessException
         *             for any {@link IOException}
         */
        @Override
        public void accept(E data);

        /**
         * @see #accept(Object)
         * @param dataSupplier
         */
        public default void accept(Supplier<E> dataSupplier)
        {
            this.accept(dataSupplier.get());
        }

        /**
         * Returns a {@link Consumer} which will consume the input of the given {@link Function} which produces the {@link String} result given to the current
         * {@link FileStringContentConsumer}
         * 
         * @param serializer
         * @return
         */
        public <T> Consumer<T> with(Function<T, E> serializer);
    }

    /**
     * {@link FileContentConsumer} which writes into an underlying {@link File} using {@link Charset} encoding
     * 
     * @author omnaest
     * @param <E>
     */
    public static interface FileCharacterSetContentConsumer<E, FCC extends FileCharacterSetContentConsumer<E, FCC>> extends FileContentConsumer<E, FCC>
    {
        public FCC using(Charset charset);

        public FCC usingUTF8();

    }

    /**
     * {@link Supplier} which returns the content of a {@link File} as {@link String}
     * 
     * @author omnaest
     */
    public static interface FileStringContentSupplier extends Supplier<String>
    {
        public FileStringContentSupplier using(Charset charset);

        public FileStringContentSupplier usingUTF8();

        /**
         * Returns the content of the underlying {@link File} as {@link String}.<br>
         * <br>
         * Returns null if the {@link File} does not exists, but throws an {@link FileAccessException} in other {@link IOException} cases.
         * 
         * @see #getLines()
         * @throws FileAccessException
         *             for any {@link IOException}
         */
        @Override
        public String get();

        /**
         * Returns the {@link #get()} content if a file is present or throws the given {@link RuntimeException}
         * 
         * @param exceptionSupplier
         * @return
         */
        public String orElseThrow(Supplier<RuntimeException> exceptionSupplier);

        /**
         * Returns the {@link #get()} content split by line end characters like \n or \r or any combination.
         * 
         * @return
         */
        public Stream<String> getAsLines();

        /**
         * Returns a {@link Supplier} based on the given deserialization {@link Function}
         * 
         * @param deserializer
         * @return
         */
        public <T> Supplier<T> with(Function<String, T> deserializer);

    }

    /**
     * @author omnaest
     */
    public static class FileAccessException extends IllegalStateException
    {
        private static final long serialVersionUID = -6516326101828174660L;

        public FileAccessException(Throwable cause)
        {
            super(cause);
        }
    }

    /**
     * Returns a {@link FileStringContentConsumer} for the given {@link File}
     * 
     * @param file
     * @return
     */
    public static FileStringContentConsumer toConsumer(File file)
    {
        return new FileStringContentConsumerImpl(file);
    }

    /**
     * Returns a {@link FileByteArrayContentConsumer} for the given {@link File}
     * 
     * @param file
     * @return
     */
    public static FileByteArrayContentConsumer toByteArrayConsumer(File file)
    {
        return new FileByteArrayContentConsumerImpl(file);
    }

    /**
     * Returns a {@link FileStreamContentConsumer} for the given {@link File}
     * 
     * @param file
     * @return
     */
    public static FileStreamContentConsumer toLineStreamConsumer(File file)
    {
        return new FileStreamContentConsumerImpl(file);
    }

    /**
     * Returns a {@link FileStringContentSupplier} for the given {@link File}
     * 
     * @param file
     * @return
     */
    public static FileStringContentSupplier toSupplier(File file)
    {
        return new FileStringContentSupplier()
        {
            private Charset charset = StandardCharsets.UTF_8;

            @Override
            public FileStringContentSupplier usingUTF8()
            {
                return this.using(StandardCharsets.UTF_8);
            }

            @Override
            public FileStringContentSupplier using(Charset charset)
            {
                this.charset = charset;
                return this;
            }

            @Override
            public String get()
            {
                try
                {
                    if (file.exists() && file.isFile())
                    {
                        return this.readFileIntoString(file);
                    }
                    else
                    {
                        return null;
                    }
                }
                catch (IOException e)
                {
                    throw new FileAccessException(e);
                }
            }

            private String readFileIntoString(File file) throws IOException
            {
                return org.apache.commons.io.FileUtils.readFileToString(file, this.charset);
            }

            @Override
            public Stream<String> getAsLines()
            {
                return StringUtils.splitToStreamByLineSeparator(this.get());
            }

            @Override
            public <T> Supplier<T> with(Function<String, T> deserializer)
            {
                return () -> deserializer.apply(this.get());
            }

            @Override
            public String orElseThrow(Supplier<RuntimeException> exceptionSupplier)
            {
                try
                {
                    if (file.exists() && file.isFile())
                    {
                        return this.readFileIntoString(file);
                    }
                    else
                    {
                        Optional.ofNullable(exceptionSupplier)
                                .map(Supplier::get)
                                .ifPresent(ConsumerUtils.throwException(MapperUtils.identity()));
                        return null;
                    }
                }
                catch (IOException e)
                {
                    throw new FileAccessException(e);
                }
            }

        };
    }

    /**
     * Creates a random temp {@link File}
     * 
     * @return
     * @throws IOException
     */
    public static File createRandomTempFile() throws IOException
    {
        File tempFile = Files.createTempFile("", "")
                             .toFile();
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static Optional<File> createRandomTempFileQuietly()
    {
        try
        {
            return Optional.of(createRandomTempFile());
        }
        catch (Exception e)
        {
            return Optional.empty();
        }
    }

    /**
     * Creates a random temporary {@link File} directory
     * 
     * @return
     * @throws IOException
     */
    public static File createRandomTempDirectory() throws IOException
    {
        File tempDirectory = Files.createTempDirectory("")
                                  .toFile();
        tempDirectory.deleteOnExit();
        return tempDirectory;
    }

    /**
     * Similar to {@link #createRandomTempDirectory()} without throwing an {@link IOException}
     * 
     * @return
     */
    public static Optional<File> createRandomTempDirectoryQuietly()
    {
        return createRandomTempDirectory(e ->
        {
        });
    }

    /**
     * Similar to {@link #createRandomTempDirectory()} allowing to specify an {@link ExceptionHandler}
     * 
     * @param exceptionHandler
     * @return
     */
    public static Optional<File> createRandomTempDirectory(ExceptionHandler... exceptionHandler)
    {
        try
        {
            return Optional.of(createRandomTempDirectory());
        }
        catch (IOException e)
        {
            Optional.ofNullable(exceptionHandler)
                    .ifPresent(handlers -> Arrays.asList(handlers)
                                                 .forEach(handler -> handler.accept(e)));
            return Optional.empty();
        }
    }

    public static interface FileReaderLoader
    {
        public BatchFileReader from(File... files);

        public BatchFileReader fromDirectory(File directory);

        public SingleFileReader from(File file);

        public static interface FileReaderBase
        {
            public BatchFileReader usingEncoding(Charset encoding);

            public Stream<String> getAsStringStream();

            /**
             * Returns a {@link Stream} of lines through all files in their order.
             * 
             * @return
             */
            public Stream<String> getAsLinesStream();

            /**
             * Similar to {@link #getAsLinesStream()} but loads the whole file content into memory first
             * 
             * @return
             */
            public Stream<String> getAsInMemoryLinesStream();

            public byte[] intoByteArray();
        }

        public static interface SingleFileReader extends FileReaderBase
        {
            public BatchFileReader from(File... files);

            public BatchFileReader fromDirectory(File directory);

            public BatchFileReader from(File file);

            public FileContent intoInMemoryFileContent();
        }

        public static interface BatchFileReader extends FileReaderLoader, FileReaderBase
        {

            public Stream<byte[]> intoByteArrays();

            public MergedFilesContent intoMergedInMemoryFilesContent();

            public Stream<FileContent> intoInMemoryFilesContent();
        }

        @Data
        @Builder
        public static class MergedFilesContent
        {
            private final List<File> files;
            private final byte[]     content;

            public Optional<File> getFirstFile()
            {
                return this.files.stream()
                                 .findFirst();
            }
        }

        @Data
        @Builder
        public static class FileContent
        {
            private final File   file;
            private final byte[] content;
        }

    }

    private static class BatchFileReaderImpl implements BatchFileReader, SingleFileReader
    {
        private List<File> files    = new ArrayList<>();
        private Charset    encoding = StandardCharsets.UTF_8;

        @Override
        public BatchFileReader from(File... files)
        {
            if (files != null)
            {
                for (File file : files)
                {
                    this.from(file);
                }
            }
            return this;
        }

        @Override
        public BatchFileReaderImpl from(File file)
        {
            if (file != null)
            {
                if (file.isFile())
                {
                    this.files.add(file);
                }
                else if (file.isDirectory())
                {
                    this.fromDirectory(file);
                }
            }
            return this;
        }

        @Override
        public BatchFileReader fromDirectory(File directory)
        {
            if (directory != null)
            {
                this.files.addAll(Arrays.asList(directory.listFiles((FileFilter) file -> file.isFile())));
            }
            return this;
        }

        @Override
        public BatchFileReader usingEncoding(Charset encoding)
        {
            this.encoding = encoding;
            return this;
        }

        @Override
        public Stream<String> getAsStringStream()
        {
            return this.mapFileToStream(file ->
            {
                try
                {
                    return Stream.of(org.apache.commons.io.FileUtils.readFileToString(file, this.encoding));
                }
                catch (IOException e)
                {
                    throw new FileAccessRuntimeException(e);
                }
            });
        }

        @Override
        public Stream<String> getAsLinesStream()
        {
            return this.mapFileToStream(file ->
            {
                try
                {
                    return FileUtils.toLineStream(file, this.encoding);
                }
                catch (RuntimeIOException e)
                {
                    throw new FileAccessRuntimeException(e);
                }
            });
        }

        @Override
        public Stream<String> getAsInMemoryLinesStream()
        {
            return this.mapFileToStream(file ->
            {
                try
                {
                    return org.apache.commons.io.FileUtils.readLines(file, this.encoding)
                                                          .stream();
                }
                catch (IOException e)
                {
                    throw new FileAccessRuntimeException(e);
                }
            });
        }

        private <R> Stream<R> mapFileToStream(Function<File, Stream<R>> fileToStreamMapper)
        {
            return this.files.stream()
                             .filter(PredicateUtils.notNull())
                             .flatMap(fileToStreamMapper);
        }

        @Override
        public Stream<byte[]> intoByteArrays()
        {
            return this.mapFileToStream(file ->
            {
                try
                {
                    return Stream.of(org.apache.commons.io.FileUtils.readFileToByteArray(file));
                }
                catch (IOException e)
                {
                    throw new FileAccessRuntimeException(e);
                }
            });
        }

        @Override
        public byte[] intoByteArray()
        {
            return this.intoByteArrays()
                       .reduce(ArrayUtils::addAll)
                       .orElse(new byte[0]);
        }

        @Override
        public MergedFilesContent intoMergedInMemoryFilesContent()
        {
            return MergedFilesContent.builder()
                                     .files(Collections.unmodifiableList(this.files))
                                     .content(this.intoByteArray())
                                     .build();
        }

        @Override
        public Stream<FileContent> intoInMemoryFilesContent()
        {
            return StreamUtils.merge2(this.files.stream(), this.intoByteArrays())
                              .map(fileAndContent -> FileContent.builder()
                                                                .file(fileAndContent.getFirst())
                                                                .content(fileAndContent.getSecond())
                                                                .build());
        }

        @Override
        public FileContent intoInMemoryFileContent()
        {
            return this.intoInMemoryFilesContent()
                       .findFirst()
                       .get();
        }
    }

    public static class FileAccessRuntimeException extends RuntimeException
    {
        private static final long serialVersionUID = -4087596140531337664L;

        public FileAccessRuntimeException(Throwable cause)
        {
            super(cause);
        }

    }

    /**
     * Returns a new {@link FileReaderLoader} instance.<br>
     * <br>
     * Example:<br>
     * 
     * <pre>
     * List<String> lines = FileUtils.read()
     *                               .from(tempFile1, tempFile2)
     *                               .getAsLinesStream()
     *                               .collect(Collectors.toList());
     * </pre>
     * 
     * @return
     */
    public static FileReaderLoader read()
    {
        return new BatchFileReaderImpl();
    }

    /**
     * Similar to {@link #writeTo(File, Charset, Consumer)}
     * 
     * @param file
     * @param writeOperation
     * @throws IOException
     */
    public static void writeTo(File file, Consumer<Writer> writeOperation) throws IOException
    {
        Charset encoding = StandardCharsets.UTF_8;
        writeTo(file, encoding, writeOperation);
    }

    /**
     * Generates a {@link Writer} for the given {@link File} and passes it to the given {@link Consumer}
     * 
     * @param file
     * @param encoding
     * @param writeOperation
     * @throws IOException
     */
    public static void writeTo(File file, Charset encoding, Consumer<Writer> writeOperation) throws IOException
    {
        org.apache.commons.io.FileUtils.forceMkdirParent(file);
        try (Writer writer = new FileWriterWithEncoding(file, encoding))
        {
            writeOperation.accept(writer);
        }
    }

    /**
     * Similar to {@link #readFrom(File, Charset, Function)} with the {@link StandardCharsets#UTF_8} encoding
     * 
     * @param file
     * @param readerFunction
     * @return
     * @throws IOException
     */
    public static <T> T readFrom(File file, Function<? super BufferedReader, T> readerFunction) throws IOException
    {
        Charset encoding = StandardCharsets.UTF_8;
        return readFrom(file, encoding, readerFunction);
    }

    /**
     * Creates a {@link BufferedReader} around the given {@link File} and returns the return object given the {@link BufferedReader} to the {@link Function}
     * <br>
     * <br>
     * The given {@link BufferedReader} is closed after the read {@link Function} has been executed
     * 
     * @param file
     * @param encoding
     * @param readerFunction
     * @return
     * @throws IOException
     */
    public static <T> T readFrom(File file, Charset encoding, Function<? super BufferedReader, T> readerFunction) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), encoding)))
        {
            return readerFunction.apply(reader);
        }
    }

    /**
     * Returns the lines of a {@link File} as {@link Stream}. Uses {@link StandardCharsets#UTF_8} encoding.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static Stream<String> toLineStream(File file)
    {
        return toLineStream(file, StandardCharsets.UTF_8);
    }

    /**
     * Returns a {@link Stream} of lines for the given {@link File}
     * 
     * @param file
     * @param charset
     * @return
     * @throws RuntimeIOException
     */
    public static Stream<String> toLineStream(File file, Charset charset)
    {
        try
        {
            LineIterator iterator = org.apache.commons.io.FileUtils.lineIterator(file, charset.toString());
            return StreamUtils.fromIterator(iterator)
                              .onClose(() ->
                              {
                                  try
                                  {
                                      iterator.close();
                                  }
                                  catch (IOException e)
                                  {
                                      throw new RuntimeIOException(e);
                                  }
                              });
        }
        catch (IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    /**
     * Returns an {@link Accessor} of the given {@link File}
     * 
     * @see #toConsumer(File)
     * @see #toSupplier(File)
     * @param file
     * @return
     */
    public static Accessor<String> toAccessor(File file)
    {
        Consumer<String> consumer = toConsumer(file);
        Supplier<String> supplier = toSupplier(file);
        return Accessor.of(supplier, consumer);
    }

    public static RandomFileAccessor toRandomFileAccessor(File file)
    {
        return new RandomFileAccessorImpl(file);
    }

    private static class RandomFileAccessorImpl implements RandomFileAccessor
    {
        private final File file;
        private long       position       = 0;
        private long       markedPosition = 0;

        private RandomFileAccessorImpl(File file)
        {
            this.file = file;
        }

        @Override
        public FileAccessPosition write(String text)
        {
            return this.write(Optional.ofNullable(text)
                                      .map(String::getBytes)
                                      .orElse(null));
        }

        @Override
        public FileAccessPosition write(byte[] data)
        {
            long newPosition = this.position;

            if (data != null)
            {
                ensureParentFolderExists(this.file);
                Path filePath = RandomFileAccessorImpl.this.file.toPath();

                ByteBuffer buffer = ByteBuffer.wrap(data);

                try (FileChannel fileChannel = (FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))
                {
                    fileChannel.position(this.position);
                    fileChannel.write(buffer);
                    newPosition += data.length;
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException("Exception writing data(lengeth=" + data.length + ") at position " + this.position, e);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Unexpected exception writing data(lengeth=" + data.length + ") at position " + this.position, e);
                }
            }

            return this.atPosition(newPosition);
        }

        @Override
        public FileAccessPosition write(int[] data)
        {
            IntStream.range(0, data.length)
                     .forEach(index -> this.write(data[index]));
            return this;
        }

        @Override
        public FileAccessPosition write(int value)
        {
            return this.write(ByteArrayUtils.encodeIntegerAsByteArray(value));
        }

        @Override
        public int[] readIntegers(int length)
        {
            return ByteArrayUtils.decodeIntegersFromByteArray(this.readBytes(length * Integer.BYTES));
        }

        @Override
        public int readInteger()
        {
            int[] values = this.readIntegers(1);
            return values.length >= 1 ? values[0] : 0;
        }

        @Override
        public long readLong()
        {
            long[] values = this.readLongs(1);
            return values.length >= 1 ? values[0] : 0;
        }

        @Override
        public long[] readLongs(int length)
        {
            return ByteArrayUtils.decodeLongsFromByteArray(this.readBytes(length * Long.BYTES));
        }

        @Override
        public long getAsLong()
        {
            return this.position;
        }

        @Override
        public FileAccessPosition atPosition(long position)
        {
            this.position = position;
            return this;
        }

        @Override
        public FileAccessPosition markPosition()
        {
            this.markedPosition = this.position;
            return this;
        }

        @Override
        public FileAccessPosition skip(int length)
        {
            return this.atPosition(this.position + length);
        }

        @Override
        public FileAccessPosition resetPositionToLastMark()
        {
            return this.atPosition(this.markedPosition);
        }

        @Override
        public FileAccessPosition readStringInto(int length, Consumer<String> textConsumer)
        {
            textConsumer.accept(this.readString(length));
            return this;
        }

        @Override
        public String readString(int length)
        {
            long previousPosition = this.position;
            byte[] bytes = this.readBytes(length * 4);
            String result = Charset.forName("UTF-8")
                                   .decode(ByteBuffer.wrap(bytes))
                                   .toString()
                                   .substring(0, length);
            this.atPosition(previousPosition + result.getBytes().length);
            return result;
        }

        @Override
        public FileAccessPosition readBytesInto(int length, Consumer<byte[]> consumer)
        {
            consumer.accept(this.readBytes(length));
            return this;
        }

        @Override
        public byte[] readBytes(int length)
        {
            Path filePath = this.file.toPath();
            ByteBuffer buffer = ByteBuffer.allocate(length);
            try (FileChannel fileChannel = (FileChannel.open(filePath, StandardOpenOption.READ)))
            {
                fileChannel.position(this.position);
                fileChannel.read(buffer);
                ((Buffer) buffer).flip();
                this.atPosition(this.position + length);
                return buffer.array();
            }
            catch (IOException e)
            {
                throw new RuntimeIOException(e);
            }
            catch (Exception e)
            {
                throw e;
            }
        }
    }

    public static interface RandomFileAccessor extends FileAccessPosition
    {

    }

    public static interface FileAccessPosition extends LongSupplier
    {
        public FileAccessPosition write(String text);

        public FileAccessPosition write(byte[] data);

        public FileAccessPosition write(int[] data);

        public int readInteger();

        public int[] readIntegers(int length);

        public long readLong();

        public long[] readLongs(int length);

        public FileAccessPosition write(int value);

        public FileAccessPosition atPosition(long position);

        public String readString(int length);

        public FileAccessPosition readStringInto(int length, Consumer<String> textConsumer);

        public byte[] readBytes(int length);

        public FileAccessPosition readBytesInto(int length, Consumer<byte[]> consumer);

        public FileAccessPosition markPosition();

        public FileAccessPosition resetPositionToLastMark();

        /**
         * Skips the given number of bytes
         * 
         * @param length
         * @return
         */
        public FileAccessPosition skip(int length);

    }

    /**
     * Returns all {@link File}s of a given directory which {@link File#getName()} do match the given regex {@link Pattern}
     * 
     * @param directory
     * @param regEx
     * @return
     * @throws IOException
     */
    public static Stream<File> findFilesOfDirectoryByName(File directory, String regEx) throws IOException
    {
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        return Files.list(directory.toPath())
                    .map(path -> path.toFile())
                    .filter(file -> pattern.matcher(file.getName())
                                           .matches());
    }

    public static Reader toReader(File file, Charset charset)
    {
        try
        {
            return new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), charset);
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
    }

    public static InputStream toInputStream(File file)
    {
        try
        {
            return new BufferedInputStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
    }

    public static interface FileReaderSupplier extends Supplier<Reader>
    {
        public <E> Supplier<E> toSupplier(Function<Reader, E> acceptor);
    }

    public static FileReaderSupplier toReaderSupplierUTF8(File file)
    {
        return toReaderSupplier(file, StandardCharsets.UTF_8);
    }

    public static FileReaderSupplier toReaderSupplier(File file, Charset charset)
    {
        return new FileReaderSupplier()
        {
            private Supplier<Reader> supplier = () ->
            {
                return toReader(file, charset);
            };

            @Override
            public Reader get()
            {
                return this.supplier.get();
            }

            @Override
            public <E> Supplier<E> toSupplier(Function<Reader, E> acceptor)
            {
                return () -> acceptor.apply(this.get());
            }
        };

    }

    public static Writer toWriter(File file, Charset charset)
    {
        try
        {
            ensureParentFolderExists(file);
            return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), charset);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static interface FileInputSupplier extends Supplier<InputStream>
    {
        public <E> Supplier<E> toSupplier(Function<InputStream, E> acceptor);
    }

    public static FileInputSupplier toInputSupplier(File file)
    {
        return new FileInputSupplier()
        {
            private Supplier<InputStream> supplier = () ->
            {
                return toInputStream(file);
            };

            @Override
            public InputStream get()
            {
                return this.supplier.get();
            }

            @Override
            public <E> Supplier<E> toSupplier(Function<InputStream, E> acceptor)
            {
                return () -> acceptor.apply(this.get());
            }
        };
    }

    public static OutputStream toOutputStream(File file) throws FileNotFoundException
    {
        ensureParentFolderExists(file);
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    private static void ensureParentFolderExists(File file)
    {
        try
        {
            org.apache.commons.io.FileUtils.forceMkdirParent(file);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static interface FileWriterSupplier extends Supplier<Writer>
    {
        public <E> Consumer<E> toConsumerWith(BiConsumer<E, Writer> acceptor);
    }

    public static FileWriterSupplier toWriterSupplierUTF8(File file)
    {
        return toWriterSupplier(file, StandardCharsets.UTF_8);
    }

    public static FileWriterSupplier toWriterSupplier(File file, Charset charset)
    {
        return new FileWriterSupplier()
        {
            private Supplier<Writer> supplier = () ->
            {
                return toWriter(file, charset);
            };

            @Override
            public Writer get()
            {
                return this.supplier.get();
            }

            @Override
            public <E> Consumer<E> toConsumerWith(BiConsumer<E, Writer> acceptor)
            {
                return t ->
                {
                    try (Writer writer = this.get())
                    {
                        acceptor.accept(t, writer);
                    }
                    catch (IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                };
            }
        };

    }

    public static interface FileOutputSupplier extends Supplier<OutputStream>
    {
        public <E> Consumer<E> toConsumerWith(BiConsumer<E, OutputStream> acceptor);
    }

    public static FileOutputSupplier toOutputSupplier(File file)
    {
        return new FileOutputSupplier()
        {
            private Supplier<OutputStream> supplier = () ->
            {
                try
                {
                    return toOutputStream(file);
                }
                catch (FileNotFoundException e)
                {
                    throw new IllegalStateException(e);
                }
            };

            @Override
            public OutputStream get()
            {
                return this.supplier.get();
            }

            @Override
            public <E> Consumer<E> toConsumerWith(BiConsumer<E, OutputStream> acceptor)
            {
                return t ->
                {
                    try (OutputStream outputStream = this.get())
                    {
                        acceptor.accept(t, outputStream);
                    }
                    catch (IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                };
            }
        };

    }

    /**
     * Similar to {@link #writeLines(Stream, File, Charset, String)} with the system default line ending
     * 
     * @param lines
     * @param file
     * @param charset
     * @throws IOException
     */
    public static void writeLines(Stream<String> lines, File file, Charset charset) throws IOException
    {
        String lineEnding = null; // system line ending
        writeLines(lines, file, charset, lineEnding);
    }

    /**
     * @see IOUtils#writeLines(Stream, java.io.OutputStream, Charset, String)
     * @param lines
     * @param file
     * @param charset
     * @param lineEnding
     * @throws IOException
     */
    public static void writeLines(Stream<String> lines, File file, Charset charset, String lineEnding) throws IOException
    {
        org.apache.commons.io.FileUtils.forceMkdirParent(file);
        IOUtils.writeLines(lines, new FileOutputStream(file), charset, lineEnding);
    }

    public static void forceMkdirParentSilently(File file)
    {
        forceMkdirParent(file, ExceptionHandler.noOperationExceptionHandler());
    }

    public static void forceMkdirParent(File file, ExceptionHandler exceptionHandler)
    {
        try
        {
            org.apache.commons.io.FileUtils.forceMkdirParent(file);
        }
        catch (IOException e)
        {
            Optional.ofNullable(exceptionHandler)
                    .ifPresent(consumer -> consumer.accept(e));
        }
    }

    public static void forceMkdirSilently(File file)
    {
        forceMkdir(file, ExceptionHandler.noOperationExceptionHandler());
    }

    public static void forceMkdir(File file, ExceptionHandler exceptionHandler)
    {
        try
        {
            org.apache.commons.io.FileUtils.forceMkdir(file);
        }
        catch (IOException e)
        {
            Optional.ofNullable(exceptionHandler)
                    .ifPresent(consumer -> consumer.accept(e));
        }
    }

    public static Stream<File> listDirectoryFiles(File directory)
    {
        return listDirectoryFiles(directory, file -> true);
    }

    public static Stream<File> listDirectoryFiles(File directory, Predicate<File> filter)
    {
        return Optional.ofNullable(directory)
                       .filter(File::isDirectory)
                       .map(File::listFiles)
                       .map(Arrays::asList)
                       .map(List::stream)
                       .map(files -> files.filter(filter))
                       .orElse(Stream.empty());
    }

    public static Stream<File> listTransitiveDirectoryFiles(File directory)
    {
        return Optional.ofNullable(directory)
                       .filter(File::isDirectory)
                       .map(File::listFiles)
                       .map(Arrays::asList)
                       .map(List::stream)
                       .orElse(Stream.empty())
                       .flatMap(file ->
                       {
                           if (file.isDirectory())
                           {
                               return Stream.concat(Stream.of(file), listTransitiveDirectoryFiles(file));
                           }
                           else
                           {
                               return Stream.of(file);
                           }
                       });
    }

    /**
     * Returns the files in the given folder and all of its sub folders
     * 
     * @param directory
     * @param filter
     * @return
     */
    public static Stream<File> listTransitiveDirectoryFiles(File directory, Predicate<File> filter)
    {
        return listTransitiveDirectoryFiles(directory).filter(filter);
    }

    /**
     * Allows to sink {@link String} or {@link Byte} data into a {@link File} and creates an {@link InputStream} or {@link Reader} on top of the file.
     * 
     * @author omnaest
     */
    public static interface FileSinkInputStreamSupplier
    {
        /**
         * @param text
         * @throws RuntimeIOException
         * @return
         */
        public FileSinkedInputStreamSupplier accept(String text);

        public FileSinkedInputStreamSupplier accept(Consumer<Writer> writerConsumer);

        FileSinkInputStreamSupplier withCharset(Charset charset);
    }

    public static interface FileSinkedInputStreamSupplier extends Supplier<String>
    {

        /**
         * @see Supplier#get()
         * @throws RuntimeIOException
         */
        @Override
        public String get();

        public <E> Supplier<E> toSupplier(Function<Reader, E> readerToElementMapper);

        /**
         * Returns a {@link Stream} of elements utilizing the given {@link Function} which consumes the given {@link Reader}. The {@link Reader} uses the
         * {@link Charset} that is defined by {@link #withCharset(Charset)} or {@link StandardCharsets#UTF_8} by default.
         * 
         * @see #withCharset(Charset)
         * @param readerToStreamMapper
         * @return
         */
        public <E> Stream<E> toStream(Function<Reader, Stream<E>> readerToStreamMapper);

        /**
         * Defines the {@link Charset} that is used in combination with {@link Reader} functions. By default {@link StandardCharsets#UTF_8} is used.
         * 
         * @param charset
         * @return
         */
        public FileSinkedInputStreamSupplier withCharset(Charset charset);

        /**
         * Returns a {@link Reader} for the underlying {@link File}
         * 
         * @see #withCharset(Charset)
         * @return
         */
        public Reader toReader();

        /**
         * Similar to {@link #toReader()} but allows to specify a {@link Charset} directly.
         * 
         * @see #toReader()
         * @see StandardCharsets
         * @param charset
         * @return
         */
        public Reader toReader(Charset charset);

    }

    /**
     * A {@link FileSinkInputStreamSupplier} has the purpose of consuming an object and writing it into a {@link File} and then allow to read it again using
     * e.g. a {@link Supplier} or {@link Stream} approach. This is helpful for reducing memory consumption of large objects.
     * 
     * @see FileSinkInputStreamSupplier
     * @param file
     * @return
     */
    public static FileSinkInputStreamSupplier toFileSinkInputStreamSupplier(File file)
    {
        return new FileSinkInputStreamSupplier()
        {
            private Charset charset = StandardCharsets.UTF_8;

            @Override
            public FileSinkedInputStreamSupplier accept(String text)
            {
                return this.accept(writer ->
                {
                    try
                    {
                        writer.append(text);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeIOException(e);
                    }
                });
            }

            @Override
            public FileSinkInputStreamSupplier withCharset(Charset charset)
            {
                this.charset = charset;
                return this;
            }

            @Override
            public FileSinkedInputStreamSupplier accept(Consumer<Writer> writerConsumer)
            {
                try (Writer writer = toWriter(file, this.charset))
                {
                    writerConsumer.accept(writer);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }

                return new FileSinkedInputStreamSupplierImpl(file, this.charset);
            }
        };
    }

    private static final class FileSinkedInputStreamSupplierImpl implements FileSinkedInputStreamSupplier
    {
        private final File file;
        private Charset    charset;

        private FileSinkedInputStreamSupplierImpl(File file, Charset charset)
        {
            this.file = file;
            this.charset = charset;
        }

        @Override
        public FileSinkedInputStreamSupplier withCharset(Charset charset)
        {
            this.charset = charset;
            return this;
        }

        @Override
        public Reader toReader()
        {
            return this.toReader(this.charset);
        }

        @Override
        public Reader toReader(Charset charset)
        {
            return FileUtils.toReader(this.file, charset);
        }

        @Override
        public String get()
        {
            try
            {
                return org.apache.commons.io.IOUtils.toString(this.toReader(this.charset));
            }
            catch (IOException e)
            {
                throw new RuntimeIOException(e);
            }
        }

        @Override
        public <E> Supplier<E> toSupplier(Function<Reader, E> readerToElementMapper)
        {
            return () -> readerToElementMapper.apply(this.toReader(this.charset));
        }

        @Override
        public <E> Stream<E> toStream(Function<Reader, Stream<E>> readerToStreamMapper)
        {
            return readerToStreamMapper.apply(this.toReader(this.charset));
        }
    }

    /**
     * Creates the given directory.
     * 
     * @throws RuntimeIOException
     * @param directory
     */
    public static void createDirectory(File directory)
    {
        try
        {
            org.apache.commons.io.FileUtils.forceMkdir(directory);
        }
        catch (IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public static interface DirectoryNavigator extends Supplier<File>
    {

        public Stream<DirectoryNavigator> listDirectories();

        /**
         * Lists non directory files
         * 
         * @return
         */
        public Stream<FileNavigator> listFiles();

        public boolean isNameMatchingRegEx(String regEx);

        public Optional<FileNavigator> findFileByName(String name);

        public Stream<FileNavigator> findFileByNameSuffix(String suffix);

        public Stream<FileNavigator> findFileByNamePrefix(String prefix);

        public FileNavigator newFile(String name);

        /**
         * Returns the directory
         * 
         * @return
         */
        @Override
        public File get();

        /**
         * Finds a {@link File} by name while transitively traversing through the sub directories.
         * 
         * @param name
         * @return
         */
        public Stream<FileNavigator> findTransitivelyFileByName(String name);

        /**
         * Finds all {@link File}s by name prefix while transitively traversing through the sub directories.
         * 
         * @param prefix
         * @return
         */
        public Stream<FileNavigator> findTransitivelyFileByNamePrefix(String prefix);

    }

    public static interface FileNavigator extends Supplier<File>, Consumer<String>
    {

        /**
         * Overwrites the file content with the given text
         */
        @Override
        public void accept(String text);

        /**
         * Returns the underlying {@link File} for this {@link FileNavigator} instance.
         */
        @Override
        public File get();

        /**
         * Reads the file content (utf-8) into a {@link String}
         * 
         * @return
         */
        public String getAsString();

    }

    public static DirectoryNavigator navigate(File directory)
    {
        return new DirectoryNavigator()
        {
            @Override
            public Stream<DirectoryNavigator> listDirectories()
            {
                return FileUtils.listDirectoryFiles(directory)
                                .filter(File::isDirectory)
                                .map(FileUtils::navigate);
            }

            @Override
            public Stream<FileNavigator> listFiles()
            {
                return FileUtils.listDirectoryFiles(directory)
                                .filter(File::isFile)
                                .map(this.createFileToFileNavigatorMapper());
            }

            @Override
            public boolean isNameMatchingRegEx(String regEx)
            {
                return MatcherUtils.matcher()
                                   .ofRegEx("[0-9]+ \\-.*")
                                   .matchAgainst(directory.getName())
                                   .isPresent();
            }

            @Override
            public Optional<FileNavigator> findFileByName(String name)
            {
                return listDirectoryFiles(directory).filter(file -> org.apache.commons.lang3.StringUtils.equals(file.getName(), name))
                                                    .findFirst()
                                                    .map(this.createFileToFileNavigatorMapper());

            }

            private Function<File, FileNavigator> createFileToFileNavigatorMapper()
            {
                return file -> new FileNavigator()
                {
                    @Override
                    public File get()
                    {
                        return file;
                    }

                    @Override
                    public void accept(String text)
                    {
                        FileUtils.toConsumer(file)
                                 .accept(text);
                    }

                    @Override
                    public String getAsString()
                    {
                        return FileUtils.toSupplier(file)
                                        .get();
                    }
                };
            }

            @Override
            public FileNavigator newFile(String name)
            {
                File file = new File(directory, name);
                return this.createFileToFileNavigatorMapper()
                           .apply(file);
            }

            @Override
            public File get()
            {
                return directory;
            }

            @Override
            public Stream<FileNavigator> findFileByNameSuffix(String suffix)
            {
                return this.listFiles()
                           .filter(navigator -> org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(navigator.get()
                                                                                                                 .getName(),
                                                                                                        suffix));
            }

            @Override
            public Stream<FileNavigator> findFileByNamePrefix(String prefix)
            {
                return this.listFiles()
                           .filter(navigator -> org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(navigator.get()
                                                                                                                   .getName(),
                                                                                                          prefix));
            }

            @Override
            public Stream<FileNavigator> findTransitivelyFileByNamePrefix(String prefix)
            {
                return this.listDirectories()
                           .flatMap(this.createTransitiveFileFinder2(directory -> directory.findFileByNamePrefix(prefix)));
            }

            @Override
            public Stream<FileNavigator> findTransitivelyFileByName(String name)
            {
                return this.listDirectories()
                           .flatMap(this.createTransitiveFileFinder(name));
            }

            private Function<DirectoryNavigator, Stream<FileNavigator>> createTransitiveFileFinder(String name)
            {
                return this.createTransitiveFileFinder2(directory -> directory.findFileByName(name)
                                                                              .map(Stream::of)
                                                                              .orElse(Stream.empty()));
            }

            private Function<DirectoryNavigator, Stream<FileNavigator>> createTransitiveFileFinder2(Function<DirectoryNavigator, Stream<FileNavigator>> function)
            {
                return directory -> Stream.concat(function.apply(directory), directory.listDirectories()
                                                                                      .flatMap(this.createTransitiveFileFinder2(function)));
            }
        };
    }

}
