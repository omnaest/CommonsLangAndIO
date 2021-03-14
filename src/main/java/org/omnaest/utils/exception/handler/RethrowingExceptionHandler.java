package org.omnaest.utils.exception.handler;

public interface RethrowingExceptionHandler
{
    public void accept(Exception e) throws Exception;
}