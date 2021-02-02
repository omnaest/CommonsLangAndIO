package org.omnaest.utils.counter;

import static org.junit.Assert.assertEquals;

import java.util.stream.IntStream;

import org.junit.Test;

/**
 * @see DefaultProgressCounter
 * @author omnaest
 */
public class DefaultProgressCounterTest
{
    @Test
    public void testGetProgress() throws Exception
    {
        ProgressCounter progressCounter = Counter.fromZero()
                                                 .asProgressCounter()
                                                 .withMaximum(100);

        IntStream.range(0, 100)
                 .forEach(counter -> assertEquals(counter + 1, Math.round(progressCounter.increment()
                                                                                         .getProgress()
                         * 100)));
    }

    @Test
    public void testGetProgressWithUnlimitedMaximum() throws Exception
    {
        ProgressCounter progressCounter = Counter.fromZero()
                                                 .asProgressCounter();
        IntStream.range(0, 100)
                 .forEach(counter -> assertEquals(100 * (1 - 1.0 / (1.0 + Math.log10(counter + 1.0 + 1))), progressCounter.increment()
                                                                                                                          .getProgress()
                         * 100, 0.01));
    }
}
