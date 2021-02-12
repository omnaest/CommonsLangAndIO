package org.omnaest.utils.duration;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @see TimeDuration
 * @author omnaest
 */
public class TimeDurationTest
{

    @SuppressWarnings("deprecation")
    @Test
    public void testBetween() throws Exception
    {
        assertEquals(TimeDuration.of(1, TimeUnit.DAYS), TimeDuration.between(new Date(2020, 12, 31), new Date(2020, 12, 30)));
        assertEquals(TimeDuration.of(1, TimeUnit.DAYS), TimeDuration.between(new Date(2020, 12, 30), new Date(2020, 12, 31)));
    }

}
