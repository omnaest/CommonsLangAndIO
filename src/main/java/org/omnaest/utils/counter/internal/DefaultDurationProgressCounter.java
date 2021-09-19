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
package org.omnaest.utils.counter.internal;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.ImmutableDurationProgressCounter;
import org.omnaest.utils.counter.ImmutableProgressCounter;
import org.omnaest.utils.counter.ProgressCounter;
import org.omnaest.utils.duration.DurationCapture;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;
import org.omnaest.utils.duration.DurationCapture.DurationMeasurement;

/**
 * @see DurationProgressCounter
 * @author omnaest
 */
public class DefaultDurationProgressCounter extends AbstractImmutableDurationProgressCounter implements DurationProgressCounter
{
    private ProgressCounter                      progressCounter;
    private DurationCapture                      durationCapture     = DurationCapture.newInstance();
    private AtomicReference<DurationMeasurement> durationMeasurement = new AtomicReference<>();

    public DefaultDurationProgressCounter(ProgressCounter progressCounter)
    {
        super(progressCounter);
        this.progressCounter = progressCounter.synchronizeProgressContinouslyTo(progess -> this.startDurationMeasurementIfNotStartedYet());
    }

    @Override
    protected Optional<DurationMeasurement> getDurationMeasurement()
    {
        return Optional.ofNullable(this.durationMeasurement.get());
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
    public DurationProgressCounter synchronizeFrom(Counter sourceCounter)
    {
        this.progressCounter.synchronizeFrom(sourceCounter);
        return this;
    }

    @Override
    public DurationProgressCounter incrementBy(int delta)
    {
        this.startDurationMeasurementIfNotStartedYet();
        this.progressCounter.incrementBy(delta);
        return this;
    }

    private void startDurationMeasurementIfNotStartedYet()
    {
        this.durationMeasurement.updateAndGet(previous -> previous != null ? previous : this.durationCapture.start());
    }

    @Override
    public DurationProgressCounter increment()
    {
        this.startDurationMeasurementIfNotStartedYet();
        this.progressCounter.increment();
        return this;
    }

    @Override
    public ProgressCounter asProgressCounter()
    {
        return this.progressCounter.asProgressCounter();
    }

    @Override
    public DurationProgressCounter asDurationProgressCounter()
    {
        return this;
    }

    @Override
    public void accept(long value)
    {
        this.progressCounter.accept(value);
    }

    @Override
    public ProgressCounter setProgress(double progress)
    {
        this.startDurationMeasurementIfNotStartedYet();
        return this.progressCounter.setProgress(progress);
    }

    @Override
    public ImmutableDurationProgressCounter asImmutableDurationProgressCounter()
    {
        return this;
    }

    @Override
    public ImmutableProgressCounter asImmutableProgressCounter()
    {
        return this;
    }

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter)
    {
        this.progressCounter.synchronizeProgressContinouslyFrom(progressCounter);
        return this;
    }

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyTo(ProgressCounter progressCounter)
    {
        progressCounter.synchronizeProgressContinouslyTo(progressCounter);
        return this;
    }

    @Override
    public DurationProgressCounter synchronizeCountContinouslyFrom(Counter counter)
    {
        this.progressCounter.synchronizeCountContinouslyFrom(counter);
        return this;
    }

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter, double weight)
    {
        progressCounter.synchronizeProgressContinouslyFrom(progressCounter, weight);
        return this;
    }

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyToByRegistrationTo(ProgressCounter progressCounter)
    {
        progressCounter.synchronizeProgressContinouslyToByRegistrationTo(progressCounter);
        return this;
    }

    @Override
    public DurationProgressCounter synchronize()
    {
        this.progressCounter.synchronize();
        return this;
    }

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyFromAndRegisterTo(ProgressCounter progressCounter, double weight)
    {
        progressCounter.synchronizeProgressContinouslyFromAndRegisterTo(progressCounter, weight);
        return this;
    }

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyTo(DoubleConsumer progressConsumer)
    {
        this.progressCounter.synchronizeProgressContinouslyTo(progressConsumer);
        return this;
    }

    @Override
    public void accept(double value)
    {
        this.progressCounter.accept(value);
    }

    @Override
    public DurationProgressCounter doWithETA(Consumer<DisplayableDuration> etaConsumer)
    {
        super.doWithETA(etaConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter doWithPassedTime(Consumer<DisplayableDuration> passedTimeConsumer)
    {
        super.doWithPassedTime(passedTimeConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        super.ifModulo(modulo, counterConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer)
    {
        super.ifModulo(modulo, progressConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter ifModulo(int modulo, DurationProgressConsumer durationProgressConsumer)
    {
        super.ifModulo(modulo, durationProgressConsumer);
        return this;
    }

    @Override
    public DurationProgressCounter doWithProgress(DoubleConsumer progressConsumer)
    {
        super.doWithProgress(progressConsumer);
        return this;
    }

}
