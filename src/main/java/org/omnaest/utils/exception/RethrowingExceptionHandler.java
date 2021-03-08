package org.omnaest.utils.exception;

public class RethrowingExceptionHandler implements ExceptionHandler
{

    @Override
    public void accept(Exception e)
    {
        if (e instanceof RuntimeException)
        {
            throw (RuntimeException) e;
        }
        else
        {
            throw new RuntimeException(e);
        }
    }

}
