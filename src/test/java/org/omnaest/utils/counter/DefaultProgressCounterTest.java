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
    private ProgressCounter progressCounter = Counter.fromZero()
                                                     .asProgressCounter()
                                                     .withMaximum(100);

    @Test
    public void testGetProgress() throws Exception
    {
        IntStream.range(0, 100)
                 .forEach(counter -> assertEquals(counter + 1, Math.round(this.progressCounter.increment()
                                                                                              .getProgress()
                         * 100)));
    }

}
