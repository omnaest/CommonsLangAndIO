package org.omnaest.utils.element.transactional.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.ExecutorUtils;
import org.omnaest.utils.element.transactional.TransactionalElement;
import org.omnaest.utils.exception.handler.ExceptionHandler;

/**
 * @see ThreadLocalTransactionalElement
 * @author omnaest
 */
public class ThreadLocalTransactionalElementTest
{
    @Test
    public void testMultithreadedStaging() throws Exception
    {
        TransactionalElement<Map<String, List<String>>> element = TransactionalElement.<Map<String, List<String>>>of(() -> new ConcurrentHashMap<String, List<String>>())
                                                                                      .asThreadLocalStaged();

        ExecutorUtils.parallel()
                     .withNumberOfThreads(10)
                     .withExceptionHandler(ExceptionHandler.rethrowingExceptionHandler())
                     .executeOperations(IntStream.range(0, 50)
                                                 .mapToObj(index -> () ->
                                                 {
                                                     assertEquals(Collections.emptyMap(), element.getStaging());
                                                     element.getStaging()
                                                            .computeIfAbsent("1", i -> new ArrayList<>())
                                                            .addAll(Arrays.asList("a", "b"));
                                                     element.withFinalMergeFunction((staging, active) ->
                                                     {
                                                         Optional.ofNullable(active)
                                                                 .orElse(Collections.emptyMap())
                                                                 .forEach(staging::putIfAbsent);
                                                         return staging;
                                                     })
                                                            .commit();
                                                     assertEquals(Collections.emptyMap(), element.getStaging());
                                                 }))
                     .handleExceptions();
        assertEquals(Collections.emptyMap(), element.getStaging());
        assertEquals(Arrays.asList("a", "b"), element.getActive()
                                                     .get("1"));
    }

}
