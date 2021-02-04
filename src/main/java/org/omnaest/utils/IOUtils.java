package org.omnaest.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.stream.Stream;

import org.apache.commons.io.Charsets;
import org.omnaest.utils.counter.Counter;

import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;

/**
 * Helper for I/O operations
 * 
 * @author omnaest
 */
public class IOUtils
{

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
                    progessConsumer.accept((int) this.previousCounter.synchronizeWith(this.counter)
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
}
