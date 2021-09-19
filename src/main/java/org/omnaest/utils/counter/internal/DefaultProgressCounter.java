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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.ImmutableProgressCounter;
import org.omnaest.utils.counter.ProgressCounter;

/**
 * Special counter implementation that also tracks the progress
 * 
 * @author omnaest
 */
public class DefaultProgressCounter extends AbstractImmutableProgressCounter implements ProgressCounter
{
    private Counter                         counter;
    private AtomicReference<Supplier<Long>> maximumProvider                  = new AtomicReference<Supplier<Long>>(() -> Long.MAX_VALUE);
    private List<DoubleSupplier>            synchronizePullProgressSuppliers = new ArrayList<>();
    private List<Runnable>                  synchronizePushOperation         = new ArrayList<>();

    public DefaultProgressCounter(Counter counter)
    {
        super(counter);
        this.counter = counter;
    }

    @Override
    public ProgressCounter withMaximum(long maximum)
    {
        this.maximumProvider.set(() -> maximum);
        return this;
    }

    @Override
    public ProgressCounter withMaximumProvider(Supplier<Long> maximumProvider)
    {
        this.maximumProvider.set(maximumProvider);
        return this;
    }

    @Override
    public ProgressCounter setProgress(double progress)
    {
        this.counter.accept(Math.round(progress * this.getMaximum()));
        return this;
    }

    @Override
    public void accept(double value)
    {
        this.setProgress(value);
    }

    @Override
    public ProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter)
    {
        return this.synchronizeProgressContinouslyFrom(progressCounter, 1.0);
    }

    @Override
    public ProgressCounter synchronizeProgressContinouslyFrom(ProgressCounter progressCounter, double weight)
    {
        this.synchronizePullProgressSuppliers.add(() -> weight * Optional.ofNullable(progressCounter)
                                                                         .map(ProgressCounter::getProgress)
                                                                         .orElse(0.0));
        return this;
    }

    @Override
    public ProgressCounter doWithProgress(DoubleConsumer progressConsumer)
    {
        super.doWithProgress(progressConsumer);
        return this;
    }

    @Override
    public ProgressCounter synchronizeProgressContinouslyFromAndRegisterTo(ProgressCounter progressCounter, double weight)
    {
        progressCounter.synchronizeProgressContinouslyTo((progress) -> this.synchronize());
        return this.synchronizeProgressContinouslyFrom(progressCounter, weight);
    }

    @Override
    public ProgressCounter synchronizeProgressContinouslyTo(ProgressCounter progressCounter)
    {
        return this.synchronizeProgressContinouslyTo((DoubleConsumer) progressCounter);
    }

    @Override
    public ProgressCounter synchronizeProgressContinouslyTo(DoubleConsumer progressCounter)
    {
        this.synchronizePushOperation.add(() -> Optional.ofNullable(progressCounter)
                                                        .ifPresent(counter -> counter.accept(this.getProgress())));
        return this;
    }

    @Override
    public ProgressCounter synchronizeProgressContinouslyToByRegistrationTo(ProgressCounter progressCounter)
    {
        progressCounter.synchronizeProgressContinouslyFrom(this);
        return this;
    }

    @Override
    public ProgressCounter synchronize()
    {
        this.counter.synchronize();
        this.runSynchronizePullOperations();
        this.runSynchronizePushOperations();
        return this;
    }

    @Override
    public ProgressCounter synchronizeCountContinouslyFrom(Counter counter)
    {
        this.counter.synchronizeCountContinouslyFrom(counter);
        return this;
    }

    public static ProgressCounter of(Counter counter)
    {
        return new DefaultProgressCounter(counter);
    }

    @Override
    public ProgressCounter synchronizeFrom(Counter sourceCounter)
    {
        this.counter.synchronizeFrom(sourceCounter);
        return this;
    }

    @Override
    public ProgressCounter incrementBy(int delta)
    {
        this.counter.incrementBy(delta);
        this.runSynchronizePushOperations();
        return this;
    }

    @Override
    public ProgressCounter increment()
    {
        this.counter.increment();
        this.runSynchronizePushOperations();
        return this;
    }

    private void runSynchronizePushOperations()
    {
        this.synchronizePushOperation.forEach(Runnable::run);
    }

    @Override
    public ProgressCounter asProgressCounter()
    {
        return this;
    }

    @Override
    public DurationProgressCounter asDurationProgressCounter()
    {
        return DurationProgressCounter.of(this);
    }

    @Override
    public void accept(long value)
    {
        this.counter.accept(value);
        this.runSynchronizePullOperations();
    }

    @Override
    protected LongSupplier getMaximumProvider()
    {
        return () -> this.maximumProvider.get()
                                         .get();
    }

    @Override
    protected void runSynchronizePullOperations()
    {
        if (!this.synchronizePullProgressSuppliers.isEmpty())
        {
            this.setProgress(this.synchronizePullProgressSuppliers.stream()
                                                                  .mapToDouble(DoubleSupplier::getAsDouble)
                                                                  .sum());
        }
    }

    @Override
    public ImmutableProgressCounter asImmutableProgressCounter()
    {
        return this;
    }

    @Override
    public ProgressCounter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        super.ifModulo(modulo, counterConsumer);
        return this;
    }

    @Override
    public ProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer)
    {
        super.ifModulo(modulo, progressConsumer);
        return this;
    }

}
