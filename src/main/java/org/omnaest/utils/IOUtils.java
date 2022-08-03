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
package org.omnaest.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.LineIterator;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.exception.RuntimeIOException;

import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;

/**
 * Helper for I/O operations
 * 
 * @author omnaest
 */
public class IOUtils
{

    private static final String COMPRESS_DEFAULT_ENTRY_NAME = "data";

    public static void copyWithProgess(InputStream inputStream, ByteArrayOutputStream outputStream, long size, int steps, DoubleConsumer progessConsumer)
            throws IOException
    {
        copyWithCounter(inputStream, outputStream, steps, (current, available) ->
        {
            if (progessConsumer != null)
            {
                progessConsumer.accept(current / (1.0 * size));
            }
        });
    }

    public static void copyWithProgess(InputStream inputStream, OutputStream outputStream, DoubleConsumer progessConsumer) throws IOException
    {
        copyWithCounter(inputStream, outputStream, (current, available) ->
        {
            if (progessConsumer != null)
            {
                progessConsumer.accept(current / (1.0 * available + current));
            }
        });
    }

    public static void copyWithCounter(InputStream inputStream, OutputStream outputStream, BiConsumer<Integer, Integer> progessConsumer) throws IOException
    {
        int steps = 1024;
        copyWithCounter(inputStream, outputStream, steps, progessConsumer);
    }

    public static void copyWithCounter(InputStream inputStream, OutputStream outputStream, int steps, BiConsumer<Integer, Integer> progessConsumer)
            throws IOException
    {
        ByteStreams.readBytes(inputStream, new ByteProcessor<Void>()
        {
            private Counter counter         = Counter.fromZero();
            private Counter previousCounter = Counter.from(-steps);

            @Override
            public boolean processBytes(byte[] buf, int off, int len) throws IOException
            {
                outputStream.write(buf, off, len);
                this.counter.incrementBy(len);
                if (progessConsumer != null && this.previousCounter.deltaTo(this.counter) >= steps)
                {
                    int available = inputStream.available();
                    progessConsumer.accept((int) this.previousCounter.synchronizeFrom(this.counter)
                                                                     .getAsLong(),
                                           available);
                }
                return true;
            }

            @Override
            public Void getResult()
            {
                return null;
            }
        });
        outputStream.flush();
        inputStream.close();
    }

    public static void closeSilently(OutputStream outputStream)
    {
        try
        {
            outputStream.close();
        }
        catch (IOException e)
        {
            // ignore
        }
    }

    /**
     * Writes the given {@link String} {@link Stream} into a file where each element of the {@link Stream} is written to an own line.
     * 
     * @param lines
     * @param outputStream
     * @param charset
     * @param lineEnding
     * @throws IOException
     */
    public static void writeLines(Stream<String> lines, OutputStream outputStream, Charset charset, String lineEnding) throws IOException
    {
        if (outputStream != null && lines != null)
        {
            try (OutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, 1024 * 1024))
            {
                final Charset effectiveCharset = Charsets.toCharset(charset);
                byte[] lineEndingBytes = Optional.ofNullable(lineEnding)
                                                 .orElse(System.lineSeparator())
                                                 .getBytes(effectiveCharset);

                boolean first = true;
                for (String line : IterableUtils.from(lines))
                {
                    if (!first)
                    {
                        outputStream.write(lineEndingBytes);
                    }

                    if (line != null)
                    {
                        outputStream.write(line.getBytes(effectiveCharset));
                    }

                    first = false;
                }
            }
        }
    }

    public static byte[] compress(byte[] data)
    {
        return compress(data, COMPRESS_DEFAULT_ENTRY_NAME);
    }

    public static byte[] compress(byte[] data, String name)
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos))
        {
            ZipEntry entry = new ZipEntry(name);
            zos.putNextEntry(entry);
            org.apache.commons.io.IOUtils.copy(new ByteArrayInputStream(data), zos);
            zos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] uncompress(byte[] data)
    {
        return uncompress(data, COMPRESS_DEFAULT_ENTRY_NAME);
    }

    public static byte[] uncompress(byte[] data, String name)
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ZipInputStream zis = new ZipInputStream(bais))
        {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null)
            {
                if (name == null || org.apache.commons.lang3.StringUtils.equals(zipEntry.getName(), name))
                {
                    org.apache.commons.io.IOUtils.copy(zis, baos);
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            baos.flush();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Returns a {@link Stream} of lines for the given {@link InputStream} and {@link Charset}
     * 
     * @see StandardCharsets
     * @param inputStream
     * @param charset
     * @return
     */
    public static Stream<String> toLineStream(InputStream inputStream, Charset charset)
    {
        if (inputStream != null)
        {
            try
            {
                LineIterator iterator = org.apache.commons.io.IOUtils.lineIterator(inputStream, charset);
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
            catch (Exception e)
            {
                throw new RuntimeIOException(e);
            }
        }
        else
        {
            return Stream.empty();
        }
    }

    public static Resource toResource(InputStream inputStream)
    {
        return new Resource()
        {
            @Override
            public String asString()
            {
                return this.asString(StandardCharsets.UTF_8);
            }

            @Override
            public String asString(Charset charset)
            {
                try
                {
                    return org.apache.commons.io.IOUtils.toString(inputStream, charset);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }

            @Override
            public Reader asReader()
            {
                return this.asReader(StandardCharsets.UTF_8);
            }

            @Override
            public Reader asReader(Charset charset)
            {
                return new InputStreamReader(this.asInputStream(), charset);
            }

            @Override
            public InputStream asInputStream()
            {
                return inputStream;
            }

            @Override
            public byte[] asByteArray()
            {
                try
                {
                    return org.apache.commons.io.IOUtils.toByteArray(this.asInputStream());
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }

        };
    }

    public static interface Resource
    {
        public String asString();

        public String asString(Charset charset);

        public Reader asReader();

        public Reader asReader(Charset charset);

        public InputStream asInputStream();

        public byte[] asByteArray();
    }

    public static IOCopy copy()
    {
        return new IOCopy()
        {
            @Override
            public ReaderCopy from(Reader reader)
            {
                return new ReaderCopy()
                {
                    private long numberOfCharactersLimit = Long.MAX_VALUE;
                    private int  bufferSize              = DEFAULT_BUFFER_SIZE;

                    @Override
                    public ReaderCopy withBufferSize(int bufferSize)
                    {
                        this.bufferSize = bufferSize;
                        return this;
                    }

                    @Override
                    public WriteResult to(Writer writer)
                    {
                        try
                        {
                            int bufferSize = (int) Math.min(this.numberOfCharactersLimit, this.bufferSize);

                            long counter = 0;
                            int readLength;
                            CharBuffer buffer = CharBuffer.allocate(bufferSize);
                            while ((counter < this.numberOfCharactersLimit) && (readLength = reader.read(buffer)) >= 0)
                            {
                                writer.write(buffer.array(), 0, readLength);
                                counter += readLength;
                                buffer.clear();
                            }

                            long count = counter;

                            return new WriteResult()
                            {
                                @Override
                                public WriteResult handleException(Consumer<Exception> exceptionHandler)
                                {
                                    // do nothing
                                    return this;
                                }

                                @Override
                                public long getCount()
                                {
                                    return count;
                                }

                                @Override
                                public boolean hasError()
                                {
                                    return false;
                                }
                            };
                        }
                        catch (IOException e)
                        {
                            return new WriteResult()
                            {
                                @Override
                                public WriteResult handleException(Consumer<Exception> exceptionHandler)
                                {
                                    exceptionHandler.accept(e);
                                    return this;
                                }

                                @Override
                                public long getCount()
                                {
                                    return -1;
                                }

                                @Override
                                public boolean hasError()
                                {
                                    return true;
                                }
                            };
                        }
                    }

                    @Override
                    public String toString()
                    {
                        return this.toStringResult()
                                   .get();
                    }

                    @Override
                    public WriteResultElement<String> toStringResult()
                    {
                        return new WriteResultElement<String>()
                        {
                            private Consumer<Exception> exceptionHandler = ConsumerUtils.noOperation();

                            @Override
                            public String get()
                            {
                                StringWriter writer = new StringWriter();
                                boolean success = to(writer).handleException(this.exceptionHandler)
                                                            .isSuccess();
                                return success ? writer.toString() : null;
                            }

                            @Override
                            public Optional<String> and()
                            {
                                return Optional.ofNullable(this.get());
                            }

                            @Override
                            public WriteResultElement<String> handleException(Consumer<Exception> exceptionHandler)
                            {
                                this.exceptionHandler = exceptionHandler;
                                return this;
                            }
                        };
                    }

                    @Override
                    public ReaderCopy withCharacterLimit(long numberOfCharacters)
                    {
                        this.numberOfCharactersLimit = numberOfCharacters;
                        return this;
                    }

                };
            }

            @Override
            public InputStreamCopy from(InputStream inputStream)
            {
                return new InputStreamCopy()
                {
                    @Override
                    public WriteResult to(OutputStream outputStream)
                    {
                        try
                        {
                            long count = org.apache.commons.io.IOUtils.copyLarge(inputStream, outputStream);
                            return new WriteResult()
                            {
                                @Override
                                public long getCount()
                                {
                                    return count;
                                }

                                @Override
                                public WriteResult handleException(Consumer<Exception> exceptionHandler)
                                {
                                    // do nothing
                                    return this;
                                }

                                @Override
                                public boolean hasError()
                                {
                                    return false;
                                }
                            };
                        }
                        catch (IOException e)
                        {
                            return new WriteResult()
                            {
                                @Override
                                public long getCount()
                                {
                                    return -1;
                                }

                                @Override
                                public WriteResult handleException(Consumer<Exception> exceptionHandler)
                                {
                                    exceptionHandler.accept(e);
                                    return this;
                                }

                                @Override
                                public boolean hasError()
                                {
                                    return true;
                                }
                            };
                        }
                    }

                    @Override
                    public WriteResultElement<byte[]> toByteArray()
                    {
                        return new WriteResultElement<byte[]>()
                        {
                            private Consumer<Exception> exceptionHandler = ConsumerUtils.noOperation();

                            @Override
                            public byte[] get()
                            {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                boolean success = to(byteArrayOutputStream).handleException(this.exceptionHandler)
                                                                           .isSuccess();
                                return success ? byteArrayOutputStream.toByteArray() : null;
                            }

                            @Override
                            public Optional<byte[]> and()
                            {
                                return Optional.ofNullable(this.get());
                            }

                            @Override
                            public WriteResultElement<byte[]> handleException(Consumer<Exception> exceptionHandler)
                            {
                                this.exceptionHandler = exceptionHandler;
                                return this;
                            }
                        };
                    }

                    @Override
                    public WriteResult to(File file)
                    {
                        try
                        {
                            return this.to(new FileOutputStream(file));
                        }
                        catch (FileNotFoundException e)
                        {
                            throw new RuntimeIOException(e);
                        }
                    }
                };
            }

            @Override
            public InputStreamCopy from(byte[] data)
            {
                return this.from(new ByteArrayInputStream(data));
            }

            @Override
            public InputStreamCopy from(File file)
            {
                try
                {
                    return this.from(new FileInputStream(file));
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }

        };

    }

    public static interface IOCopy
    {
        public ReaderCopy from(Reader reader);

        public InputStreamCopy from(InputStream inputStream);

        public InputStreamCopy from(byte[] data);

        public InputStreamCopy from(File file);

    }

    public static interface ReaderCopy
    {
        public final int DEFAULT_BUFFER_SIZE = 128 * 1024;

        /**
         * Defines the maximum number of {@link Character}s being transferred
         * 
         * @param numberOfCharacters
         * @return
         */
        public ReaderCopy withCharacterLimit(long numberOfCharacters);

        /**
         * Defines the internal character buffer size. Default is a size of {@value #DEFAULT_BUFFER_SIZE}
         * 
         * @param bufferSize
         * @return
         */
        public ReaderCopy withBufferSize(int bufferSize);

        /**
         * @see #toString()
         * @param writer
         */
        public WriteResult to(Writer writer);

        /**
         * Writes the current source into a {@link String}
         * 
         * @return
         */
        @Override
        public String toString();

        public WriteResultElement<String> toStringResult();
    }

    public static interface InputStreamCopy
    {
        /**
         * @param outputStream
         */
        public WriteResult to(OutputStream outputStream);

        public WriteResultElement<byte[]> toByteArray();

        public WriteResult to(File file);
    }

    public static interface WriteResult
    {
        public long getCount();

        public boolean hasError();

        public default boolean isSuccess()
        {
            return !this.hasError();
        }

        public WriteResult handleException(Consumer<Exception> exceptionHandler);
    }

    public static interface WriteResultElement<E> extends Supplier<E>
    {
        /**
         * Returns an {@link Optional} with the result
         * 
         * @return
         */
        public Optional<E> and();

        public WriteResultElement<E> handleException(Consumer<Exception> exceptionHandler);
    }

}
