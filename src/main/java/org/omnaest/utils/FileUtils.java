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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.omnaest.utils.FileUtils.BatchFileReader.BatchFileReaderLoaded;
import org.omnaest.utils.exception.RuntimeIOException;
import org.omnaest.utils.exception.handler.ExceptionHandler;
import org.omnaest.utils.functional.Accessor;

/**
 * Utils regarding {@link File} operations
 * 
 * @author omnaest
 */
public class FileUtils
{

    private static abstract class AbstractFileContentConsumer<E, FCC extends FileContentConsumer<E, FCC>> implements FileContentConsumer<E, FCC>
    {
        protected File    file;
        protected Charset charset = StandardCharsets.UTF_8;

        public AbstractFileContentConsumer(File file)
        {
            super();
            this.file = file;
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

        @Override
        public <T> Consumer<T> with(Function<T, E> serializer)
        {
            return serializationObject -> this.accept(serializer.apply(serializationObject));
        }
    }

    private static class FileStringContentConsumerImpl extends AbstractFileContentConsumer<String, FileStringContentConsumer>
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

    private static class FileStreamContentConsumerImpl extends AbstractFileContentConsumer<Stream<String>, FileStreamContentConsumer>
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
    public static interface FileStringContentConsumer extends FileContentConsumer<String, FileStringContentConsumer>
    {

    }

    /**
     * {@link Consumer} of a {@link Stream} of {@link String} lines which will write into an underlying {@link File}
     * 
     * @author omnaest
     */
    public static interface FileStreamContentConsumer extends FileContentConsumer<Stream<String>, FileStreamContentConsumer>
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
        public FCC using(Charset charset);

        public FCC usingUTF8();

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
         * Returns a {@link Consumer} which will consume the input of the given {@link Function} which produces the {@link String} result given to the current
         * {@link FileStringContentConsumer}
         * 
         * @param serializer
         * @return
         */
        public <T> Consumer<T> with(Function<T, E> serializer);
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
                String retval = null;
                try
                {
                    if (file.exists() && file.isFile())
                    {
                        retval = org.apache.commons.io.FileUtils.readFileToString(file, this.charset);
                    }
                }
                catch (IOException e)
                {
                    throw new FileAccessException(e);
                }
                return retval;
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

    public static interface BatchFileReader
    {
        public BatchFileReaderLoaded from(File... files);

        public BatchFileReaderLoaded fromDirectory(File directory);

        public BatchFileReaderLoaded from(File file);

        public static interface BatchFileReaderLoaded extends BatchFileReader
        {
            public BatchFileReaderLoaded usingEncoding(Charset encoding);

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
     * Returns a new {@link BatchFileReader} instance.<br>
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
    public static BatchFileReader read()
    {
        return new BatchFileReaderLoaded()
        {
            private List<File> files    = new ArrayList<>();
            private Charset    encoding = StandardCharsets.UTF_8;

            @Override
            public BatchFileReaderLoaded from(File... files)
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
            public BatchFileReaderLoaded from(File file)
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
            public BatchFileReaderLoaded fromDirectory(File directory)
            {
                if (directory != null)
                {
                    this.files.addAll(Arrays.asList(directory.listFiles((FileFilter) file -> file.isFile())));
                }
                return this;
            }

            @Override
            public BatchFileReaderLoaded usingEncoding(Charset encoding)
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
                    catch (IOException e)
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

            private Stream<String> mapFileToStream(Function<File, Stream<String>> fileToStreamMapper)
            {
                return this.files.stream()
                                 .filter(PredicateUtils.notNull())
                                 .flatMap(fileToStreamMapper);
            }
        };
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
     * Returns the lines of a {@link File} as {@link Stream}
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static Stream<String> toLineStream(File file) throws IOException
    {
        return toLineStream(file, StandardCharsets.UTF_8);
    }

    /**
     * Returns a {@link Stream} of lines for the given {@link File}
     * 
     * @param file
     * @param charset
     * @return
     * @throws IOException
     */
    public static Stream<String> toLineStream(File file, Charset charset) throws IOException
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
                                  throw new IllegalStateException(e);
                              }
                          });

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
        private long       position = 0;

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
                Path filePath = RandomFileAccessorImpl.this.file.toPath();

                ByteBuffer buffer = ByteBuffer.wrap(data);

                try (FileChannel fileChannel = (FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE)))
                {
                    fileChannel.position(this.position);
                    fileChannel.write(buffer);
                    newPosition += data.length;
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }

            return this.atPosition(newPosition);
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
        public FileAccessPosition readStringInto(int length, Consumer<String> textConsumer)
        {
            long newPosition = this.position;
            String text = this.readString(length);
            textConsumer.accept(text);
            return this.atPosition(newPosition + text.getBytes().length);
        }

        @Override
        public String readString(int length)
        {
            Path filePath = this.file.toPath();
            ByteBuffer buffer = ByteBuffer.allocate(length * 4);
            try (FileChannel fileChannel = (FileChannel.open(filePath, StandardOpenOption.READ)))
            {
                fileChannel.position(this.position);
                fileChannel.read(buffer);
                buffer.flip();
                String result = Charset.forName("UTF-8")
                                       .decode(buffer)
                                       .toString()
                                       .substring(0, length);
                this.atPosition(this.position + result.getBytes().length);
                return result;
            }
            catch (IOException e)
            {
                throw new RuntimeIOException(e);
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

        public FileAccessPosition atPosition(long position);

        public String readString(int length);

        public FileAccessPosition readStringInto(int length, Consumer<String> textConsumer);
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

    public static Writer toWriter(File file, Charset charset) throws FileNotFoundException
    {
        ensureParentFolderExists(file);
        return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), charset);
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
                try
                {
                    return toWriter(file, charset);
                }
                catch (FileNotFoundException e)
                {
                    throw new IllegalStateException(e);
                }
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
}
