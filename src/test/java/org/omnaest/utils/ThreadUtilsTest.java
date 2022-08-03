package org.omnaest.utils;

import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.omnaest.utils.duration.DurationCapture;

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
}
