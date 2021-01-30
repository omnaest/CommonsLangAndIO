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
import org.omnaest.utils.TimeFormatUtils;

public class DefaultDurationCapture implements DurationCapture
{

    private static class MeasurementResultImpl implements MeasurementResult
    {
        private long duration;

        public MeasurementResultImpl(long duration)
        {
            super();
            this.duration = duration;
        }

        @Override
        public long getDuration(TimeUnit timeUnit)
        {
            return timeUnit.convert(this.duration, TimeUnit.MILLISECONDS);
        }

        @Override
        public String getDurationAsString(TimeUnit timeUnit)
        {
            return TimeFormatUtils.format()
                                  .duration(this.getDuration(timeUnit), timeUnit)
                                  .asString();
        }

        @Override
        public String getDurationAsCanonicalString()
        {
            return TimeFormatUtils.format()
                                  .duration(this.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                                  .asCanonicalString();
        }

        @Override
        public String toString()
        {
            return this.getDurationAsString(TimeUnit.MILLISECONDS);
        }

        @Override
        public MeasurementResult doWithResult(Consumer<MeasurementResult> resultConsumer)
        {
            if (resultConsumer != null)
            {
                resultConsumer.accept(this);
            }
            return this;
        }

        @Override
        public DisplayableDuration asTimeUnitDisplay()
        {
            return DisplayableDuration.of(this.duration, TimeUnit.MILLISECONDS);
        }

        @Override
        public DisplayableDuration toETA(double progress)
        {
            return DisplayableDuration.of(Math.round(this.duration / progress - this.duration), TimeUnit.MILLISECONDS);
        }

    }

    private static class MeasurementResultWithReturnValueImpl<R> extends MeasurementResultImpl implements MeasurementResultWithReturnValue<R>
    {
        private R retval;

        public MeasurementResultWithReturnValueImpl(R retval, long duration)
        {
            super(duration);
            this.retval = retval;
        }

        @Override
        public R getReturnValue()
        {
            return this.retval;
        }

        @SuppressWarnings("unchecked")
        @Override
        public MeasurementResultWithReturnValue<R> doWithResult(Consumer<MeasurementResult> resultConsumer)
        {
            return (MeasurementResultWithReturnValue<R>) super.doWithResult(resultConsumer);
        }

    }

    protected static final Map<TimeUnit, String> timeUnitToLabelMap = MapUtils.builder()
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
        }
        finally
        {
            stopTime = System.currentTimeMillis();
            duration = stopTime - startTime;
        }

        //
        return new MeasurementResultWithReturnValueImpl<>(retval.get(), duration);
    }

    @Override
    public DurationMeasurement start()
    {
        long start = System.currentTimeMillis();
        return new DurationMeasurement()
        {
            @Override
            public MeasurementResult stop()
            {
                long duration = System.currentTimeMillis() - start;
                return new MeasurementResultImpl(duration);
            }

        };
    }
}
