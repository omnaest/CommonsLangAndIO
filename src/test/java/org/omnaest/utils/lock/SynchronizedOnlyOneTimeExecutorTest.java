package org.omnaest.utils.lock;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class SynchronizedOnlyOneTimeExecutorTest
{

    @Test
    public void testExecute() throws Exception
    {
        AtomicBoolean state = new AtomicBoolean(false);
        SynchronizedOnlyOneTimeExecutor executor = new SynchronizedOnlyOneTimeExecutor(Executors.newCachedThreadPool(), () -> state.set(true));

        executor.execute()
                .shutdown()
                .awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(state.get());
    }

}
