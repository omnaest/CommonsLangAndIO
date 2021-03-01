/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.omnaest.utils.duration;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.omnaest.utils.optional.NullOptional;

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

    /**
     * Returns a {@link TimeDuration} for the given {@link Duration}.<br>
     * <br>
     * Be aware that a {@link TimeDuration} is always the absolute duration and cannot indicate a negative duration compared to {@link Duration}.
     * 
     * @param duration
     * @return
     */
    public static TimeDuration of(Duration duration)
    {
        return new TimeDuration(Math.abs(duration.toNanos()), TimeUnit.NANOSECONDS);
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

    /**
     * Returns a {@link TimeDuration} representing the absolute distance between two {@link Date} instances. If any or both {@link Date} instance is null, then
     * a {@link TimeDuration} of 0 is returned.
     * 
     * @param firstDate
     * @param secondDate
     * @return
     */
    public static TimeDuration between(Date firstDate, Date secondDate)
    {
        return NullOptional.ofTwoNullable(firstDate, secondDate)
                           .map(bi -> of(Duration.between(bi.getFirst()
                                                            .toInstant(),
                                                          bi.getSecond()
                                                            .toInstant())))
                           .orElse(of(0, TimeUnit.NANOSECONDS));
    }

    @Override
    public int hashCode()
    {
        long value = this.as(TimeUnit.NANOSECONDS);
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        long value = this.as(TimeUnit.NANOSECONDS);
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        TimeDuration other = (TimeDuration) obj;
        long otherValue = other.as(TimeUnit.NANOSECONDS);
        if (value != otherValue)
        {
            return false;
        }
        return true;
    }

}
