package org.omnaest.utils.exception;

import java.io.IOException;

/**
 * {@link RuntimeException} similar to {@link IOException}
 * 
 * @author omnaest
 */
public class RuntimeIOException extends RuntimeException
{
    private static final long serialVersionUID = 1717098863908697550L;

    public RuntimeIOException()
    {
        super();
    }

    public RuntimeIOException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RuntimeIOException(String message)
    {
        super(message);
    }

    public RuntimeIOException(Throwable cause)
    {
        super(cause);
    }

}
