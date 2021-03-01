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
package org.omnaest.utils.counter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import org.omnaest.utils.duration.DurationCapture;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;
import org.omnaest.utils.duration.DurationCapture.DurationMeasurement;
import org.omnaest.utils.duration.DurationCapture.MeasurementResult;

/**
 * @see DurationProgressCounter
 * @author omnaest
 */
public class DefaultDurationProgressCounter implements DurationProgressCounter
{
    private ProgressCounter                      progressCounter;
    private DurationCapture                      durationCapture     = DurationCapture.newInstance();
    private AtomicReference<DurationMeasurement> durationMeasurement = new AtomicReference<>();

    protected DefaultDurationProgressCounter(ProgressCounter progressCounter)
    {
        super();
        this.progressCounter = progressCounter;
    }

    @Override
    public String getProgressAsString()
    {
        return this.progressCounter.getProgressAsString();
    }

    @Override
    public Optional<DisplayableDuration> getETA()
    {
        return Optional.ofNullable(this.durationMeasurement.get())
                       .map(DurationMeasurement::stop)
                       .map(measurementResult -> measurementResult.toETA(this.getProgress()));
    }

    @Override
    public Optional<DisplayableDuration> getPassedTime()
    {
        return Optional.ofNullable(this.durationMeasurement.get())
                       .map(DurationMeasurement::stop)
                       .map(MeasurementResult::asTimeUnitDisplay);
    }

    @Override
    public String getProgressAndETAasString()
    {
        return this.getProgressAsString() + this.getETA()
                                                .map(DisplayableDuration::asCanonicalString)
                                                .map(eta -> " ( " + eta + " )")
                                                .orElse("");
    }

    @Override
    public DurationProgressCounter doWithETA(Consumer<DisplayableDuration> etaConsumer)
    {
        Optional.ofNullable(etaConsumer)
                .ifPresent(consumer -> this.getETA()
                                           .ifPresent(consumer::accept));
        return this;
    }

    @Override
    public DurationProgressCounter doWithPassedTime(Consumer<DisplayableDuration> passedTimeConsumer)
    {
        Optional.ofNullable(passedTimeConsumer)
                .ifPresent(consumer -> this.getPassedTime()
                                           .ifPresent(consumer::accept));
        return this;
    }

    @Override
    public DurationProgressCounter withMaximumProvider(Supplier<Long> maximumProvider)
    {
        this.progressCounter.withMaximumProvider(maximumProvider);
        return this;
    }

    @Override
    public DurationProgressCounter withMaximum(long maximum)
    {
        this.progressCounter.withMaximum(maximum);
        return this;
    }

    @Override
    public long deltaTo(Counter otherCounter)
    {
        return this.progressCounter.deltaTo(otherCounter);
    }

    @Override
    public double getProgress()
    {
        return this.progressCounter.getProgress();
    }

    @Override
    public DurationProgressCounter synchronizeWith(Counter sourceCounter)
    {
        this.progressCounter.synchronizeWith(sourceCounter);
        return this;
    }

    @Override
    public DurationProgressCounter incrementBy(int delta)
    {
        this.startDurationMeasurement();
        this.progressCounter.incrementBy(delta);
        return this;
    }

    private void startDurationMeasurement()
    {
        this.durationMeasurement.updateAndGet(previous -> previous != null ? previous : this.durationCapture.start());
    }

    @Override
    public DurationProgressCounter increment()
    {
        this.startDurationMeasurement();
        this.progressCounter.increment();
        return this;
    }

    @Override
    public ProgressCounter asProgressCounter()
    {
        return this.progressCounter.asProgressCounter();
    }

    @Override
    public DurationProgressCounter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        this.progressCounter.ifModulo(modulo, counterConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer)
    {
        this.progressCounter.ifModulo(modulo, progressConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter ifModulo(int modulo, DurationProgressConsumer durationProgressConsumer)
    {
        this.progressCounter.ifModulo(modulo, (Progress progressCounter) ->
        {
            Optional<DisplayableDuration> eta = this.getETA();
            Optional<DisplayableDuration> passedTime = this.getPassedTime();
            durationProgressConsumer.accept(new DurationProgress()
            {
                @Override
                public long getCounter()
                {
                    return progressCounter.getCounter();
                }

                @Override
                public double getProgress()
                {
                    return progressCounter.getProgress();
                }

                @Override
                public Optional<DisplayableDuration> getETA()
                {
                    return eta;
                }

                @Override
                public String getProgressAsString()
                {
                    return progressCounter.getProgressAsString();
                }

                @Override
                public Optional<DisplayableDuration> getPassedTime()
                {
                    return passedTime;
                }
            });
        });
        return this;
    }

    @Override
    public long getAsLong()
    {
        return this.progressCounter.getAsLong();
    }

    @Override
    public DurationProgressCounter doWithProgress(DoubleConsumer progressConsumer)
    {
        this.progressCounter.doWithProgress(progressConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter asDurationProgressCounter()
    {
        return this;
    }

}
