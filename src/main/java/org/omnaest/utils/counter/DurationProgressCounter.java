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

import java.util.function.Supplier;

import org.omnaest.utils.duration.DurationCapture;

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
    public DurationProgressCounter synchronizeWith(Counter sourceCounter);

    @Override
    public DurationProgressCounter synchronizeProgressContinouslyWith(ProgressCounter progressCounter);

    @Override
    public DurationProgressCounter synchronizeCountContinouslyWith(Counter counter);

    @Override
    public DurationProgressCounter incrementBy(int delta);

    @Override
    public DurationProgressCounter increment();

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
