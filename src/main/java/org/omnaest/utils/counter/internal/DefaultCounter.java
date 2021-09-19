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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.ProgressCounter;

/**
 * @see Counter
 * @author omnaest
 */
public class DefaultCounter extends AbstractImmutableCounter implements Counter
{
    private AtomicLong counter;
    private Runnable   synchronizeOperation = () ->
                                            {
                                            };

    public DefaultCounter(long start)
    {
        this.counter = new AtomicLong(start);
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
        this.synchronize();
        return this.counter.get();
    }

    @Override
    public void accept(long value)
    {
        this.counter.set(value);
    }

    @Override
    public Counter synchronizeCountContinouslyFrom(Counter counter)
    {
        this.synchronizeOperation = () -> this.accept(counter.getAsLong());
        return this;
    }

    @Override
    public Counter synchronizeFrom(Counter sourceCounter)
    {
        this.counter.set(sourceCounter.getAsLong());
        return this;
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

    @Override
    public Counter synchronize()
    {
        this.synchronizeOperation.run();
        return this;
    }

    @Override
    public Counter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        super.ifModulo(modulo, counterConsumer);
        return this;
    }

}
