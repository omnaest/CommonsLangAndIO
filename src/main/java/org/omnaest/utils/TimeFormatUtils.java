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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.omnaest.utils.StringUtils.StringTextBuilder;

/**
 * Helper with time formatting
 * 
 * @see #format()
 * @author omnaest
 */
public class TimeFormatUtils
{
    private static final Map<TimeUnit, String> timeUnitToName = MapUtils.builder()
                                                                        .put(TimeUnit.DAYS, "d")
                                                                        .put(TimeUnit.HOURS, "h")
                                                                        .put(TimeUnit.MICROSECONDS, "microsecond(s)")
                                                                        .put(TimeUnit.MILLISECONDS, "ms")
                                                                        .put(TimeUnit.MINUTES, "min")
                                                                        .put(TimeUnit.NANOSECONDS, "ns")
                                                                        .put(TimeUnit.SECONDS, "sec")
                                                                        .build();

    public static interface TimeFormatter
    {
        public static interface TimeWithUnitFormatter
        {
            public String asString();

            public String asCanonicalString();

            public String asCanonicalString(TimeUnit... timeUnits);
        }

        /**
         * Returns a {@link TimeWithUnitFormatter}
         * 
         * @param duration
         * @param timeUnit
         * @return
         */
        public TimeWithUnitFormatter duration(long duration, TimeUnit timeUnit);
    }

    private static class TimeUnitAndDuration
    {
        private long     duration;
        private TimeUnit timeUnit;

        public TimeUnitAndDuration(long duration, TimeUnit timeUnit)
        {
            super();
            this.duration = duration;
            this.timeUnit = timeUnit;
        }

        public long getDuration()
        {
            return this.duration;
        }

        public TimeUnit getTimeUnit()
        {
            return this.timeUnit;
        }

        @Override
        public String toString()
        {
            return "TimeUnitAndDuration [duration=" + this.duration + ", timeUnit=" + this.timeUnit + "]";
        }

    }

    /**
     * Returns a {@link TimeFormatter}
     * 
     * @return
     */
    public static TimeFormatter format()
    {
        return new TimeFormatter()
        {
            @Override
            public TimeWithUnitFormatter duration(long duration, TimeUnit timeUnit)
            {
                return new TimeWithUnitFormatter()
                {
                    @Override
                    public String asString()
                    {
                        return StringUtils.builder()
                                          .add(duration)
                                          .add(timeUnitToName.get(timeUnit))
                                          .build();
                    }

                    @Override
                    public String asCanonicalString()
                    {
                        return this.asCanonicalString(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS);
                    }

                    @Override
                    public String asCanonicalString(TimeUnit... timeUnits)
                    {
                        StringTextBuilder stringBuilder = StringUtils.builder();

                        AtomicLong decreasingDuration = new AtomicLong(duration);
                        TimeUnit lastTimeUnit = ArrayUtils.last(timeUnits);
                        AtomicBoolean forceDisplay = new AtomicBoolean(false);

                        Arrays.asList(timeUnits)
                              .stream()
                              .sorted(ComparatorUtils.reverse(ComparatorUtils.comparatorFunction(TimeUnit::ordinal)))
                              .forEach(iTimeUnit ->
                              {
                                  Optional.of(iTimeUnit)
                                          .map(unit -> new TimeUnitAndDuration(unit.convert(decreasingDuration.get(), timeUnit), unit))
                                          .filter(tuad -> tuad.getDuration() > 0 || forceDisplay.get() || tuad.getTimeUnit()
                                                                                                              .equals(lastTimeUnit))
                                          .ifPresent(timeUnitAndDuration ->
                                          {
                                              decreasingDuration.getAndAdd(-timeUnit.convert(timeUnitAndDuration.getDuration(),
                                                                                             timeUnitAndDuration.getTimeUnit()));

                                              stringBuilder.add(format().duration(timeUnitAndDuration.getDuration(), timeUnitAndDuration.getTimeUnit())
                                                                        .asString())
                                                           .add(" ");
                                              forceDisplay.set(true);
                                          });
                              });

                        return stringBuilder.build()
                                            .trim();
                    }

                };
            }
        };
    }
}
