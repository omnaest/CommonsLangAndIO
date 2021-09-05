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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * @see Counter
 * @author omnaest
 */
public class DefaultCounter implements Counter
{
    private AtomicLong counter;
    private Runnable   synchronizeOperation = () ->
                                            {
                                            };

    protected DefaultCounter(long start)
    {
        this.counter = new AtomicLong(start);
    }

    @Override
    public Counter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        this.synchronizeOperation.run();
        long count = this.getAsLong();
        if (counterConsumer != null && count % modulo == 0)
        {
            counterConsumer.accept(count);
        }
        return this;
    }

    @Override
    public Counter increment()
    {
        return this.incrementBy(1);
    }

    @Override
    public Counter incrementBy(int delta)
    {
        this.counter.addAndGet(delta);
        return this;
    }

    @Override
    public long getAsLong()
    {
        this.synchronizeOperation.run();
        return this.counter.get();
    }

    @Override
    public void accept(long value)
    {
        this.counter.set(value);
    }

    @Override
    public Counter synchronizeCountContinouslyWith(Counter counter)
    {
        this.synchronizeOperation = () -> this.accept(counter.getAsLong());
        return this;
    }

    @Override
    public Counter synchronizeWith(Counter sourceCounter)
    {
        this.counter.set(sourceCounter.getAsLong());
        return this;
    }

    /**
     * Returns the delta = other counter - current counter
     * 
     * @param otherCounter
     * @return
     */
    @Override
    public long deltaTo(Counter otherCounter)
    {
        return otherCounter.getAsLong() - this.counter.get();
    }

    @Override
    public ProgressCounter asProgressCounter()
    {
        return DefaultProgressCounter.of(this);
    }

    @Override
    public DurationProgressCounter asDurationProgressCounter()
    {
        return this.asProgressCounter()
                   .asDurationProgressCounter();
    }

}
