package org.omnaest.utils.lock;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class SynchronizedAtLeastOneTimeExecutorTest
{

    @Test
    public void testFire() throws Exception
    {
        AtomicBoolean state = new AtomicBoolean(false);
        SynchronizedAtLeastOneTimeExecutor executor = new SynchronizedAtLeastOneTimeExecutor(Executors.newCachedThreadPool(), () -> state.set(true));

        executor.fire()
                .shutdown()
                .awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(state.get());
    }

}
