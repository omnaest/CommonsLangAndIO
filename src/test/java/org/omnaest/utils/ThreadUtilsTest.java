package org.omnaest.utils;

import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.omnaest.utils.duration.DurationCapture;
import org.omnaest.utils.duration.DurationCapture.DurationMeasurement;
import org.omnaest.utils.duration.TimeDuration;

public class ThreadUtilsTest
{
    @Test
    public void testSleepSilently()
    {
        assertTrue(DurationCapture.newInstance()
                                  .start()
                                  .run(() -> ThreadUtils.sleepSilently(Duration.ofMillis(200)))
                                  .stop()
                                  .getDuration(TimeUnit.MILLISECONDS) >= 180);
    }

    @Test
    public void testSleepInIntervalForDuration() throws Exception
    {
        DurationMeasurement duration = DurationCapture.newInstance()
                                                      .start();
        ThreadUtils.sleep()
                   .inIntervalOf(10, TimeUnit.MILLISECONDS)
                   .forDuration(() -> TimeDuration.of(100, TimeUnit.MILLISECONDS));
        assertTrue(duration.stop()
                           .isDurationLargerThan(TimeDuration.ofMilliseconds(90)));
        assertTrue(duration.stop()
                           .isDurationLessThan(TimeDuration.ofMilliseconds(1000)));
    }

    @Test
    public void testSleepInIntervalWithMinimalNumberOfIntervalsForDuration() throws Exception
    {
        DurationMeasurement duration = DurationCapture.newInstance()
                                                      .start();
        ThreadUtils.sleep()
                   .inIntervalOf(10, TimeUnit.MILLISECONDS)
                   .withMinimumNumberOfIntervals(3)
                   .forDuration(() -> TimeDuration.of(10, TimeUnit.MILLISECONDS));
        assertTrue(duration.stop()
                           .isDurationLargerThan(TimeDuration.ofMilliseconds(20)));
        assertTrue(duration.stop()
                           .isDurationLessThan(TimeDuration.ofMilliseconds(20000)));
    }
}
