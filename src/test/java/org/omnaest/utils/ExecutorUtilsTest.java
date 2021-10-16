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
package org.omnaest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class ExecutorUtilsTest
{

    @Test
    public void testParallel() throws Exception
    {
        int numberOfElements = 10000;
        List<Callable<String>> tasks = IntStream.range(0, numberOfElements)
                                                .mapToObj(ii -> (Callable<String>) () -> ("" + ii))
                                                .collect(Collectors.toList());
        List<String> result = ExecutorUtils.parallel()
                                           .withNumberOfThreads(4)
                                           .executeTasks(tasks)
                                           .get()
                                           .collect(Collectors.toList());
        assertEquals(numberOfElements, result.size());
        assertEquals("0", result.get(0));
        assertEquals("" + (numberOfElements - 1), result.get(numberOfElements - 1));
    }

    @Test
    public void testParallel2() throws Exception
    {
        ExecutorUtils.parallel()
                     .withUnlimitedNumberOfThreads()
                     .execute(collector ->
                     {
                         Supplier<String> result1 = collector.add(() -> "1");
                         Supplier<Integer> result2 = collector.add(() -> 2);

                         assertEquals("1", result1.get());
                         assertEquals(2, result2.get()
                                                .intValue());
                     });
    }

    @Test
    public void testAsynchronously() throws Exception
    {
        AtomicBoolean result = new AtomicBoolean(false);
        ExecutorUtils.parallel()
                     .withUnlimitedNumberOfThreads()
                     .asynchronously()
                     .executeOperation(() ->
                     {
                         ThreadUtils.sleepSilently(200, TimeUnit.MILLISECONDS);
                         result.set(true);
                     });
        // should still be false as the async thread needs 100ms longer
        assertFalse(result.get());
    }

    @Test
    public void testWrapStream() throws Exception
    {
        int numberOfElements = 1000;
        assertEquals(IntStream.range(0, numberOfElements)
                              .boxed()
                              .sorted()
                              .collect(Collectors.toList()),
                     ExecutorUtils.parallel()
                                  .withNumberOfThreads(4)
                                  .wrap(IntStream.range(0, numberOfElements)
                                                 .boxed()
                                                 .collect(Collectors.toList())
                                                 .stream())
                                  .get()
                                  .sorted()
                                  .collect(Collectors.toList()));
    }

}
