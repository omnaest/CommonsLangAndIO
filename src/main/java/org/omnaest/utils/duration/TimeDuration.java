package org.omnaest.utils.duration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A {@link TimeDuration} wraps a duration and a {@link TimeUnit}
 * 
 * @author omnaest
 */
public class TimeDuration
{
    private long     duration;
    private TimeUnit timeUnit;

    private TimeDuration(long duration, TimeUnit timeUnit)
    {
        super();
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public static TimeDuration of(long duration, TimeUnit timeUnit)
    {
        return new TimeDuration(duration, timeUnit);
    }

    public static TimeDuration of(int duration, TimeUnit timeUnit)
    {
        return new TimeDuration(duration, timeUnit);
    }

    public static TimeDuration of(Duration duration)
    {
        return new TimeDuration(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    public long getDuration()
    {
        return this.duration;
    }

    public TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }

    public long as(TimeUnit timeUnit)
    {
        return timeUnit.convert(this.duration, this.timeUnit);
    }

    @Override
    public String toString()
    {
        return "TimeDuration [duration=" + this.duration + ", timeUnit=" + this.timeUnit + "]";
    }

    /**
     * Returns a zero duration
     * 
     * @return
     */
    public static TimeDuration zero()
    {
        return of(0, TimeUnit.NANOSECONDS);
    }

    /**
     * Returns true if the this {@link TimeDuration} is larger than the given {@link TimeDuration}
     * 
     * @param timeDuration
     * @return
     */
    public boolean isLargerThan(TimeDuration timeDuration)
    {
        return this.duration > timeDuration.as(this.timeUnit);
    }

    /**
     * Returns true if the this {@link TimeDuration} is smaller than the given {@link TimeDuration}
     * 
     * @param timeDuration
     * @return
     */
    public boolean isSmallerThan(TimeDuration timeDuration)
    {
        return this.duration < timeDuration.as(this.timeUnit);
    }

}
