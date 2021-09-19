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

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import org.omnaest.utils.counter.internal.DefaultDurationProgressCounter;
import org.omnaest.utils.duration.DurationCapture;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;

/**
 * {@link ProgressCounter} in combination with a {@link DurationCapture} instance. The duration measurement starts with the first counter {@link #increment()}.
 * 
 * @author omnaest
 */
public interface DurationProgressCounter extends ProgressCounter, ImmutableDurationProgressCounter
{

    @Override
    public DurationProgressCounter withMaximumProvider(Supplier<Long> maximumProvider);

    @Override
    public DurationProgressCounter withMaximum(long maximum);

    @Override
    public DurationProgressCounter synchronizeFrom(Counter sourceCounter);

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter);

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyTo(ProgressCounter progressCounter);

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter, double weight);

    @Override
    public DurationProgressCounter synchronizeCountContinouslyFrom(Counter counter);

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyToByRegistrationTo(ProgressCounter progressCounter);

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyFromAndRegisterTo(ProgressCounter progressCounter, double weight);

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyTo(DoubleConsumer progressConsumer);

    @Override
    public DurationProgressCounter synchronize();

    @Override
    public DurationProgressCounter incrementBy(int delta);

    @Override
    public DurationProgressCounter increment();

    @Override
    public DurationProgressCounter doWithETA(Consumer<DisplayableDuration> etaConsumer);

    @Override
    public DurationProgressCounter doWithPassedTime(Consumer<DisplayableDuration> passedTimeConsumer);

    @Override
    public DurationProgressCounter ifModulo(int modulo, DurationProgressConsumer durationProgressConsumer);

    @Override
    public DurationProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer);

    @Override
    public DurationProgressCounter ifModulo(int modulo, LongConsumer counterConsumer);

    public ImmutableDurationProgressCounter asImmutableDurationProgressCounter();

    public static DurationProgressCounter of(ProgressCounter progressCounter)
    {
        return new DefaultDurationProgressCounter(progressCounter);
    }

    public static DurationProgressCounter fromZero()
    {
        return Counter.fromZero()
                      .asDurationProgressCounter();
    }

    public static DurationProgressCounter from(int start)
    {
        return Counter.from(start)
                      .asDurationProgressCounter();
    }

}
