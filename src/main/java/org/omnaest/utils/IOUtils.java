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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
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
        catch (IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }
}
