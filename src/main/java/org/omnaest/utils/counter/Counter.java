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
import java.util.function.LongSupplier;

/**
 * Represents an {@link AtomicLong} counter with additional methods for counting support
 * 
 * @see #from(int)
 * @see #from(long)
 * @see #fromZero()
 * @see #asProgressCounter()
 * @see #asDurationProgressCounter()
 * @author omnaest
 */
public interface Counter extends LongSupplier
{
    public long deltaTo(Counter otherCounter);

    public Counter synchronizeWith(Counter sourceCounter);

    public Counter incrementBy(int delta);

    public Counter increment();

    /**
     * Calls the given {@link LongConsumer} for all counted values that have counter % 'modulo' value == 0.<br>
     * <br>
     * Be aware that for this method to work the {@link #increment()} and not the {@link #incrementBy(int)} method should be used.
     * 
     * @param modulo
     * @param counterConsumer
     * @return
     */
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
