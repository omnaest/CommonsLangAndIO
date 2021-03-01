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

/**
 * Helper for {@link Exception} handling
 * 
 * @author omnaest
 */
public class ExceptionUtils
{
    public static interface Operation<R>
    {
        public R execute();
    }

    public static interface VoidOperationWithException
    {
        public void execute() throws Exception;
    }

    public static interface OperationWithException<R>
    {
        public R execute() throws Exception;
    }

    public static interface ExceptionHandler
    {
        public void handle(Exception e) throws Exception;
    }

    public static interface RuntimeExceptionHandler
    {
        public void handle(RuntimeException e);
    }

    /**
     * Similar to {@link #executeSilent(Operation, RuntimeExceptionHandler...)} but throwing an {@link Exception}
     * 
     * @param operation
     * @param exceptionHandlers
     * @return
     * @throws Exception
     */
    public static <R> R execute(OperationWithException<R> operation, ExceptionHandler... exceptionHandlers) throws Exception
    {
        R retval = null;

        try
        {
            retval = operation.execute();
        }
        catch (Exception e)
        {
            if (exceptionHandlers != null)
            {
                for (ExceptionHandler exceptionHandler : exceptionHandlers)
                {
                    exceptionHandler.handle(e);
                }
            }
        }

        return retval;
    }

    /**
     * Similar to {@link #executeSilent(Operation, RuntimeExceptionHandler...)}
     * 
     * @param operation
     * @param exceptionHandlers
     * @return
     */
    public static void executeSilentVoid(VoidOperationWithException operation, ExceptionHandler... exceptionHandlers)
    {
        executeThrowingSilent((OperationWithException<Void>) () ->
        {
            operation.execute();
            return null;
        }, exceptionHandlers);
    }

    /**
     * Executes the given {@link Operation} using the given {@link RuntimeExceptionHandler} to handle any exception
     * 
     * @param operation
     * @param exceptionHandlers
     * @return
     */
    public static <R> R executeSilent(Operation<R> operation, RuntimeExceptionHandler... exceptionHandlers)
    {
        R retval = null;

        try
        {
            retval = operation.execute();
        }
        catch (RuntimeException e)
        {
            if (exceptionHandlers != null)
            {
                for (RuntimeExceptionHandler exceptionHandler : exceptionHandlers)
                {
                    exceptionHandler.handle(e);
                }
            }
        }

        return retval;
    }

    public static <R> R executeThrowingSilent(OperationWithException<R> operation, ExceptionHandler... exceptionHandlers)
    {
        R retval = null;

        try
        {
            retval = operation.execute();
        }
        catch (Exception e)
        {
            if (exceptionHandlers != null)
            {
                for (ExceptionHandler exceptionHandler : exceptionHandlers)
                {
                    try
                    {
                        exceptionHandler.handle(e);
                    }
                    catch (Exception e1)
                    {
                        throw new RuntimeException(e1);
                    }
                }
            }
        }

        return retval;
    }
}
