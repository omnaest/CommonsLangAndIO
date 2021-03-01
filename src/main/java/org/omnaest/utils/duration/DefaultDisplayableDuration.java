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
