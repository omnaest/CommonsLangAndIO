package org.omnaest.utils.element.cached.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.element.cached.CachedElement.InputOutputStreamSerializerAndDeserializer;
import org.omnaest.utils.exception.RuntimeIOException;

/**
 * @see InputOutputStreamSerializerAndDeserializer
 * @author omnaest
 */
public class ByteArrayInputOutputStreamSerializerAndDeserializer implements CachedElement.InputOutputStreamSerializerAndDeserializer<byte[]>
{
    @Override
    public void accept(byte[] data, OutputStream outputStream)
    {
        try
        {
            IOUtils.write(data, outputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeIOException(e);
        }

    }

    @Override
    public byte[] apply(InputStream inputStream)
    {
        if (inputStream != null)
        {
            try
            {
                return IOUtils.toByteArray(inputStream);
            }
            catch (Exception e)
            {
                throw new RuntimeIOException(e);
            }
        }
        else
        {
            return null;
        }
    }
}