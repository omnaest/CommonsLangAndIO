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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
		public static interface TimeUnitFormatter
		{
			public String asString();

			public String asCanonicalString();
		}

		/**
		 * Returns a {@link TimeUnitFormatter}
		 * 
		 * @param duration
		 * @param timeUnit
		 * @return
		 */
		public TimeUnitFormatter duration(long duration, TimeUnit timeUnit);
	}

	private static class TimeUnitAndDuration
	{
		private long		duration;
		private TimeUnit	timeUnit;

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
			public TimeUnitFormatter duration(long duration, TimeUnit timeUnit)
			{
				return new TimeUnitFormatter()
				{
					@Override
					public String asString()
					{
						return StringUtils	.builder()
											.append(duration)
											.append(" ")
											.append(timeUnitToName.get(timeUnit))
											.toString();
					}

					@Override
					public String asCanonicalString()
					{
						StringBuilder stringBuilder = StringUtils.builder();

						AtomicLong decreasingDuration = new AtomicLong(duration);
						while (decreasingDuration.get() > 0)
						{
							TimeUnitAndDuration timeUnitAndDuration = Arrays.asList(TimeUnit.values())
																			.stream()
																			.map(itimeUnit -> new TimeUnitAndDuration(	itimeUnit.convert(	decreasingDuration.get(),
																																			timeUnit),
																														itimeUnit))
																			.filter(tuad -> tuad.getDuration() > 0)
																			.sorted(ComparatorUtils	.builder()
																									.of(TimeUnitAndDuration.class)
																									.with(tuad -> tuad.getDuration())
																									.build())
																			.findFirst()
																			.get();

							decreasingDuration.getAndAdd(-timeUnit.convert(timeUnitAndDuration.getDuration(), timeUnitAndDuration.getTimeUnit()));

							stringBuilder	.append(format().duration(timeUnitAndDuration.getDuration(), timeUnitAndDuration.getTimeUnit())
															.asString())
											.append(" ");
						}

						return stringBuilder.toString()
											.trim();
					}

				};
			}
		};
	}
}
