package org.omnaest.utils.exception;

public class RuntimeFileNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = -6165412076951872491L;

    public RuntimeFileNotFoundException()
    {
        super();
    }

    public RuntimeFileNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RuntimeFileNotFoundException(String message)
    {
        super(message);
    }

    public RuntimeFileNotFoundException(Throwable cause)
    {
        super(cause);
    }

}
