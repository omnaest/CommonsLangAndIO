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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.omnaest.utils.duration.DefaultDurationCapture.TimeUnitDisplay;

/**
 * @see #newInstance()
 * @see #measure(MeasuredOperation)
 * @author omnaest
 */
public interface DurationCapture
{
	public static interface MeasuredOperation<R>
	{
		public R execute();
	}

	public static interface MeasuredVoidOperation
	{
		public void execute();
	}

	public static interface MeasurementResult
	{

		public long getDuration(TimeUnit timeUnit);

		public String getDurationAsString(TimeUnit timeUnit);

		public String getDurationAsCanonicalString();

		public MeasurementResult doWithResult(Consumer<MeasurementResult> resultConsumer);

        TimeUnitDisplay toETA(double progress);

        TimeUnitDisplay asTimeUnitDisplay();

	}

	public static interface MeasurementResultWithReturnValue<R> extends MeasurementResult
	{
		public R getReturnValue();

		@Override
		public MeasurementResultWithReturnValue<R> doWithResult(Consumer<MeasurementResult> resultConsumer);

	}

	public MeasurementResult measure(MeasuredVoidOperation operation);

	public <R> MeasurementResultWithReturnValue<R> measure(MeasuredOperation<R> operation);

	public static DurationCapture newInstance()
	{
		return new DefaultDurationCapture();
	}

	public static interface DurationMeasurement
	{
		public MeasurementResult stop();
	}

	public DurationMeasurement start();
}
