package org.omnaest.utils.lock;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class OnlyOneTimeExecutorTest
{

    @Test
    public void testExecute() throws Exception
    {
        AtomicBoolean state = new AtomicBoolean(false);
        OnlyOneTimeExecutor executor = new OnlyOneTimeExecutor(Executors.newCachedThreadPool(), () -> state.set(true));

        executor.execute()
                .shutdown()
                .awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(state.get());
    }

}
