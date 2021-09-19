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

import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * {@link Counter} with additional methods to track a progress
 * 
 * @author omnaest
 */
public interface ProgressCounter extends Counter, ImmutableProgressCounter, DoubleConsumer
{
    public ProgressCounter withMaximumProvider(Supplier<Long> maximumProvider);

    public ProgressCounter withMaximum(long maximum);

    public ProgressCounter setProgress(double progress);

    /**
     * Similar to {@link #setProgress(double)}
     */
    @Override
    public void accept(double value);

    public ProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter);

    public ProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter, double weight);

    public ProgressCounter synchronizeProgressContinouslyFromAndRegisterTo(ProgressCounter progressCounter, double weight);

    public ProgressCounter synchronizeProgressContinouslyTo(DoubleConsumer progressConsumer);

    public ProgressCounter synchronizeProgressContinouslyTo(ProgressCounter progressCounter);

    /**
     * Registers this {@link ProgressCounter} at the give {@link ProgressCounter} using
     * {@link ProgressCounter#synchronizeProgressContinouslyFrom(ProgressCounter)}
     * 
     * @param progressCounter
     * @return
     */
    public ProgressCounter synchronizeProgressContinouslyToByRegistrationTo(ProgressCounter progressCounter);

    /**
     * Synchronizes all registered {@link ProgressCounter}s
     * 
     * @see #synchronizeProgressContinouslyFrom(ProgressCounter)
     * @see #synchronizeProgressContinouslyFrom(ProgressCounter, double)
     * @see #synchronizeProgressContinouslyTo(ProgressCounter)
     * @return
     */
    @Override
    public ProgressCounter synchronize();

    @Override
    public ProgressCounter synchronizeCountContinouslyFrom(Counter counter);

    @Override
    public ProgressCounter synchronizeFrom(Counter sourceCounter);

    public @Override ProgressCounter incrementBy(int delta);

    @Override
    public ProgressCounter increment();

    public ImmutableProgressCounter asImmutableProgressCounter();

    @Override
    public ProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer);

    @Override
    public ProgressCounter ifModulo(int modulo, LongConsumer counterConsumer);

}
