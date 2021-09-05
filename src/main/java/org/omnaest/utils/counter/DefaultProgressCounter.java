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
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * Special counter implementation that also tracks the progress
 * 
 * @author omnaest
 */
public class DefaultProgressCounter implements ProgressCounter
{
    private Counter                         counter;
    private AtomicReference<Supplier<Long>> maximumProvider      = new AtomicReference<Supplier<Long>>(() -> Long.MAX_VALUE);
    private Runnable                        synchronizeOperation = () ->
                                                                 {
                                                                 };

    private DefaultProgressCounter(Counter counter)
    {
        super();
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
    public double getProgress()
    {
        long maximum = this.maximumProvider.get()
                                           .get();
        if (Long.MAX_VALUE == maximum || maximum <= 0l)
        {
            return 1.0 - 1.0 / (1.0 + Math.log10(1.0 + this.getAsLong()));
        }
        else
        {
            return this.getAsLong() / Math.max(1.0, maximum);
        }
    }

    @Override
    public ProgressCounter setProgress(double progress)
    {
        this.counter.accept(Math.round(progress * this.maximumProvider.get()
                                                                      .get()));
        return this;
    }

    @Override
    public ProgressCounter synchronizeProgressContinouslyWith(ProgressCounter progressCounter)
    {
        this.synchronizeOperation = () -> this.setProgress(progressCounter.getProgress());
        return this;
    }

    @Override
    public ProgressCounter synchronizeCountContinouslyWith(Counter counter)
    {
        this.counter.synchronizeCountContinouslyWith(counter);
        return this;
    }

    @Override
    public String getProgressAsString()
    {
        return formatProgress(this.getProgress());
    }

    @Override
    public ProgressCounter doWithProgress(DoubleConsumer progressConsumer)
    {
        Optional.ofNullable(progressConsumer)
                .ifPresent(consumer -> consumer.accept(this.getProgress()));
        return this;
    }

    public static ProgressCounter of(Counter counter)
    {
        return new DefaultProgressCounter(counter);
    }

    @Override
    public long deltaTo(Counter otherCounter)
    {
        return this.counter.deltaTo(otherCounter);
    }

    @Override
    public ProgressCounter synchronizeWith(Counter sourceCounter)
    {
        this.counter.synchronizeWith(sourceCounter);
        return this;
    }

    @Override
    public ProgressCounter incrementBy(int delta)
    {
        this.counter.incrementBy(delta);
        return this;
    }

    @Override
    public ProgressCounter increment()
    {
        this.counter.increment();
        return this;
    }

    @Override
    public ProgressCounter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        this.counter.ifModulo(modulo, counterConsumer);
        return this;
    }

    @Override
    public ProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer)
    {
        this.counter.ifModulo(modulo, counter ->
        {
            double progress = this.getProgress();

            progressConsumer.accept(new Progress()
            {
                @Override
                public long getCounter()
                {
                    return counter;
                }

                @Override
                public double getProgress()
                {
                    return progress;
                }

                @Override
                public String getProgressAsString()
                {
                    return formatProgress(progress);
                }

            });
        });
        return this;
    }

    private static String formatProgress(double progress)
    {
        return Math.round(progress * 100) + "%";
    }

    @Override
    public long getAsLong()
    {
        this.synchronizeOperation.run();
        return this.counter.getAsLong();
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
    }

    @Override
    public ImmutableProgressCounter asImmutableProgressCounter()
    {
        return this;
    }

}
