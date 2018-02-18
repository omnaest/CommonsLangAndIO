package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;
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

}
