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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.omnaest.utils.FileUtils.BatchFileReader.BatchFileReaderLoaded;
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
                    org.apache.commons.io.FileUtils.writeLines(this.file, this.charset.toString(), data.collect(Collectors.toList()));
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
         * @throws FileAccessException
         *             for any {@link IOException}
         */
        @Override
        public String get();

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
    public static FileStreamContentConsumer toStreamConsumer(File file)
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
            public <T> Supplier<T> with(Function<String, T> deserializer)
            {
                return () -> deserializer.apply(this.get());
            }

        };
    }

    public static File createRandomTempFile() throws IOException
    {
        File tempFile = Files.createTempFile("", "")
                             .toFile();
        tempFile.deleteOnExit();
        return tempFile;
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

            public Stream<String> getAsLinesStream();
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
                return this.files.stream()
                                 .filter(PredicateUtils.notNull())
                                 .map(file ->
                                 {
                                     try
                                     {
                                         return org.apache.commons.io.FileUtils.readFileToString(file, this.encoding);
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
                return this.files.stream()
                                 .filter(PredicateUtils.notNull())
                                 .flatMap(file ->
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
}
