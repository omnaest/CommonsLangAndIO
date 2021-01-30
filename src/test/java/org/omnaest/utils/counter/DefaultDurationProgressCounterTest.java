package org.omnaest.utils.counter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.ThreadUtils;

/**
 * @see DefaultDurationProgressCounter
 * @author omnaest
 */
public class DefaultDurationProgressCounterTest
{
    private DurationProgressCounter progressCounter = Counter.fromZero()
                                                             .asDurationProgressCounter();

    @Test
    public void testGetETA() throws Exception
    {
        int maximum = 5;
        int sleepDuration = 100;

        List<Long> etas = new ArrayList<>();

        IntStream.range(0, maximum)
                 .forEach(counter ->
                 {
                     this.progressCounter.increment()
                                         .withMaximum(maximum)
                                         .doWithETA(duration -> etas.add(duration.as(TimeUnit.MILLISECONDS)));
                     ThreadUtils.sleepSilently(sleepDuration, TimeUnit.MILLISECONDS);
                 });

        assertEquals(true, etas.stream()
                               .mapToLong(v -> (long) v)
                               .max()
                               .getAsLong() > sleepDuration);
    }

}
