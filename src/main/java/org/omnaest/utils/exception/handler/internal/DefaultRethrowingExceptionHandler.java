package org.omnaest.utils.exception.handler.internal;

import org.omnaest.utils.exception.handler.ExceptionHandler;
import org.omnaest.utils.exception.handler.RethrowingExceptionHandler;

/**
 * @see ExceptionHandler#rethrowingExceptionHandler()
 * @author omnaest
 */
public class DefaultRethrowingExceptionHandler implements ExceptionHandler, RethrowingExceptionHandler
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
