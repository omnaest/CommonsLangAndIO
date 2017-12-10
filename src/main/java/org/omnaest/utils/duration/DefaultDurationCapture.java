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
package org.omnaest.utils.duration;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.omnaest.utils.MapUtils;

public class DefaultDurationCapture implements DurationCapture
{
	private static class MeasurementResultWithReturnValueImpl<R> implements MeasurementResultWithReturnValue<R>
	{
		private R		retval;
		private long	duration;

		public MeasurementResultWithReturnValueImpl(R retval, long duration)
		{
			super();
			this.retval = retval;
			this.duration = duration;
		}

		@Override
		public R getReturnValue()
		{
			return this.retval;
		}

		@Override
		public long getDuration(TimeUnit timeUnit)
		{
			return timeUnit.convert(this.duration, TimeUnit.MILLISECONDS);
		}

		@Override
		public String getDurationAsString(TimeUnit timeUnit)
		{
			return this.getDuration(timeUnit) + " " + timeUnitToLabelMap.getOrDefault(timeUnit, timeUnit.toString());
		}

		@Override
		public MeasurementResultWithReturnValue<R> doWithResult(Consumer<MeasurementResult> resultConsumer)
		{
			if (resultConsumer != null)
			{
				resultConsumer.accept(this);
			}
			return this;
		}

		@Override
		public String toString()
		{
			return this.getDurationAsString(TimeUnit.MILLISECONDS);
		}

	}

	protected static final Map<TimeUnit, String> timeUnitToLabelMap = MapUtils	.builder()
																				.put(TimeUnit.MILLISECONDS, "ms")
																				.put(TimeUnit.MICROSECONDS, "microseconds")
																				.put(TimeUnit.NANOSECONDS, "ns")
																				.put(TimeUnit.SECONDS, "sec")
																				.put(TimeUnit.MINUTES, "min")
																				.put(TimeUnit.DAYS, "d")
																				.put(TimeUnit.HOURS, "h")
																				.build();

	@Override
	public MeasurementResult measure(MeasuredVoidOperation operation)
	{
		return this.measure(() ->
		{
			operation.execute();
			return null;
		});
	}

	@Override
	public <R> MeasurementResultWithReturnValue<R> measure(MeasuredOperation<R> operation)
	{
		//
		long duration;
		long stopTime;
		long startTime;
		AtomicReference<R> retval = new AtomicReference<>();

		//
		startTime = System.currentTimeMillis();
		try
		{
			retval.set(operation.execute());
		} finally
		{
			stopTime = System.currentTimeMillis();
			duration = stopTime - startTime;
		}

		//
		return new MeasurementResultWithReturnValueImpl<>(retval.get(), duration);
	}

}
