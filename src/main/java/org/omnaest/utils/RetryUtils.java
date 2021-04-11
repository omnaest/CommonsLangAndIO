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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Helper for retry operations
 * 
 * @author omnaest
 */
public class RetryUtils
{

    public static interface RetryOperationBuilder
    {
        public RetryOperationBuilder times(int times);

        public RetryOperationBuilder withDurationInBetween(int duration, ChronoUnit chronoUnit);

        public RetryOperationBuilder withExceptionFilter(Predicate<Exception> exceptionFilter);

        public RetryOperationBuilder withExceptionFilter(Collection<Class<? extends Exception>> exceptions);

        public RetryOperationBuilder withSingleExceptionFilter(Class<? extends Exception> exception);

        public RetryOperationBuilder withRetryListener(Consumer<Integer> retryListenter);

        public <E> E operation(RetryGetOperation<E> operation) throws Exception;

        public void operation(RetryOperation operation) throws Exception;

        public <E> E silentOperation(RetryGetOperation<E> operation);

        public void silentOperation(RetryOperation operation);

    }

    public static RetryOperationBuilder retry()
    {
        return new RetryOperationBuilder()
        {
            private int                  times           = 3;
            private Duration             durationInBetween;
            private Predicate<Exception> exceptionFilter = e -> true;
            private Consumer<Integer>    retryListenter;

            @Override
            public RetryOperationBuilder times(int times)
            {
                this.times = times + 1;
                return this;
            }

            @Override
            public RetryOperationBuilder withDurationInBetween(int durationInBetween, ChronoUnit chronoUnit)
            {
                this.durationInBetween = Duration.of(durationInBetween, chronoUnit);
                return this;
            }

            @Override
            public RetryOperationBuilder withSingleExceptionFilter(Class<? extends Exception> exception)
            {
                return this.withExceptionFilter(Arrays.asList(exception));
            }

            @Override
            public RetryOperationBuilder withExceptionFilter(Predicate<Exception> exceptionFilter)
            {
                this.exceptionFilter = exceptionFilter;
                return this;
            }

            @Override
            public RetryOperationBuilder withExceptionFilter(Collection<Class<? extends Exception>> exceptions)
            {
                return this.withExceptionFilter(e -> exceptions.stream()
                                                               .anyMatch(exceptionType -> exceptionType.isAssignableFrom(e.getClass())));
            }

            @Override
            public <E> E operation(RetryGetOperation<E> operation) throws Exception
            {
                E retval = null;

                for (int ii = 0; ii < this.times; ii++)
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
                        if (ii >= this.times - 1)
                        {
                            throw e;
                        }
                        else if (!this.exceptionFilter.test(e))
                        {
                            throw e;
                        }
                        else
                        {
                            ThreadUtils.sleepSilently(this.durationInBetween);

                            int retryCount = ii + 1;
                            Optional.ofNullable(this.retryListenter)
                                    .ifPresent(consumer -> consumer.accept(retryCount));
                        }
                    }
                }

                return retval;
            }

            @Override
            public void operation(RetryOperation operation) throws Exception
            {
                this.operation(() ->
                {
                    return null;
                });
            }

            @Override
            public <E> E silentOperation(RetryGetOperation<E> operation)
            {
                try
                {
                    return this.operation(operation);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void silentOperation(RetryOperation operation)
            {
                try
                {
                    this.operation(operation);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public RetryOperationBuilder withRetryListener(Consumer<Integer> retryListenter)
            {
                this.retryListenter = retryListenter;
                return this;
            }
        };
    }

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
        Predicate<E> validResultFilter = element -> true;
        return retry(times, durationInBetween, timeUnit, operation, validResultFilter);
    }

    public static <E> E retry(int times, int durationInBetween, TimeUnit timeUnit, RetryGetOperation<E> operation, Predicate<E> validResultFilter)
            throws Exception
    {
        E retval = null;

        for (int ii = 0; ii < times; ii++)
        {
            try
            {
                //
                retval = operation.run();

                if (validResultFilter != null)
                {
                    if (!validResultFilter.test(retval))
                    {
                        throw new IllegalStateException("Result was invalid");
                    }
                }

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

    public static boolean retryOnFalse(int times, int durationInBetween, TimeUnit timeUnit, RetryGetOperation<Boolean> operation)
    {
        try
        {
            Predicate<Boolean> validResultFilter = result -> result;
            return retry(times, durationInBetween, timeUnit, operation, validResultFilter);
        }
        catch (Exception e)
        {
            return false;
        }
    }

}
