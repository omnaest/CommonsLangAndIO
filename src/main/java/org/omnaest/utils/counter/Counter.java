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

import org.omnaest.utils.counter.internal.DefaultCounter;

/**
 * Represents a thread safe {@link AtomicLong} counter with additional methods for counting support
 * 
 * @see #from(int)
 * @see #from(long)
 * @see #fromZero()
 * @see #asProgressCounter()
 * @see #asDurationProgressCounter()
 * @author omnaest
 */
public interface Counter extends LongConsumer, ImmutableCounter
{
    /**
     * Synchronizes this {@link Counter} from a given source {@link Counter}
     * 
     * @param sourceCounter
     * @return
     */
    public Counter synchronizeFrom(Counter sourceCounter);

    public Counter incrementBy(int delta);

    public Counter increment();

    public Counter synchronizeCountContinouslyFrom(Counter counter);

    /**
     * Synchronizes all registered {@link Counter}s
     * 
     * @see #synchronizeCountContinouslyFrom(Counter)
     * @return
     */
    public Counter synchronize();

    @Override
    public Counter ifModulo(int modulo, LongConsumer counterConsumer);

    /**
     * @see ProgressCounter
     * @return
     */
    public ProgressCounter asProgressCounter();

    /**
     * @see DurationProgressCounter
     * @return
     */
    public DurationProgressCounter asDurationProgressCounter();

    public static Counter fromZero()
    {
        return new DefaultCounter(0l);
    }

    public static Counter from(int start)
    {
        return new DefaultCounter(start);
    }

    public static Counter from(long start)
    {
        return new DefaultCounter(start);
    }

}
