package org.omnaest.utils.duration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.omnaest.utils.TimeFormatUtils;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;

/**
 * @see DisplayableDuration
 * @author omnaest
 */
public class DefaultDisplayableDuration implements DisplayableDuration
{
    private long     time;
    private TimeUnit timeUnit;

    protected DefaultDisplayableDuration(long time, TimeUnit timeUnit)
    {
        super();
        this.time = time;
        this.timeUnit = timeUnit;
    }

    @Override
    public String asCanonicalString(TimeUnit... timeUnits)
    {
        return TimeFormatUtils.format()
                              .duration(this.time, this.timeUnit)
                              .asCanonicalString(timeUnits);
    }

    @Override
    public long as(TimeUnit timeUnit)
    {
        return timeUnit.convert(this.time, this.timeUnit);
    }

    @Override
    public String asCanonicalStringWithMinimalTimeUnit(TimeUnit timeUnit)
    {
        return this.asCanonicalString(Arrays.asList(TimeUnit.values())
                                            .stream()
                                            .filter(unit -> unit.ordinal() >= timeUnit.ordinal())
                                            .toArray(size -> new TimeUnit[size]));
    }

    @Override
    public String asCanonicalString()
    {
        return this.asCanonicalStringWithMinimalTimeUnit(TimeUnit.SECONDS);
    }
}