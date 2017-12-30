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

import java.util.concurrent.TimeUnit;

/**
 * Helper for retry operations
 * 
 * @author omnaest
 */
public class RetryHelper
{

    public static interface RetryOperation
    {
        public void run() throws Exception;
    }

    public static interface RetryGetOperation<E>
    {
        public E run() throws Exception;
    }

    /**
     * Similar to {@link #retry(int, int, TimeUnit, RetryGetOperation)}
     * 
     * @param times
     * @param durationInBetween
     * @param timeUnit
     * @param operation
     * @throws Exception
     */
    public static void retry(int times, int durationInBetween, TimeUnit timeUnit, RetryOperation operation) throws Exception
    {
        retry(times, durationInBetween, timeUnit, () ->
        {
            operation.run();
            return null;
        });
    }

    /**
     * Retries the {@link RetryOperation} an unlimited number of times
     * 
     * @see #retry(int, int, TimeUnit, RetryOperation)
     * @param durationInBetween
     * @param timeUnit
     * @param operation
     * @throws Exception
     */
    public static void retryUnlimited(int durationInBetween, TimeUnit timeUnit, RetryOperation operation)
    {
        try
        {
            retry(Integer.MAX_VALUE, durationInBetween, timeUnit, operation);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retries a given {@link RetryGetOperation} the given number of time and waits the given duration in between
     * 
     * @param times
     * @param durationInBetween
     * @param timeUnit
     * @param operation
     * @return
     * @throws Exception
     */
    public static <E> E retry(int times, int durationInBetween, TimeUnit timeUnit, RetryGetOperation<E> operation) throws Exception
    {
        E retval = null;

        for (int ii = 0; ii < times; ii++)
        {
            try
            {
                //
                retval = operation.run();

                //
                break;
            }
            catch (Exception e)
            {
                if (ii >= times - 1)
                {
                    throw e;
                }
                else
                {
                    ThreadUtils.sleepSilently(durationInBetween, timeUnit);
                }
            }
        }

        return retval;
    }
}
