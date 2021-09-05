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
package org.omnaest.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.ProgressUtils;

/**
 * Helper for {@link Stream#peek(java.util.function.Consumer)}
 * 
 * @author omnaest
 */
public class PeekUtils
{
    /**
     * Returns a {@link Consumer} with an internal {@link AtomicLong} counter, which gets incremented for each call
     * 
     * @param consumer
     * @return
     */
    public static <E> Consumer<E> counter(Consumer<Long> consumer)
    {
        AtomicLong counter = new AtomicLong();
        return e -> consumer.accept(counter.getAndIncrement());
    }

    /**
     * @see ProgressUtils#newDurationProgressCounterLogger(Consumer)
     * @param messageConsumer
     * @param maxCoun
     * @return
     */
    public static <E> Consumer<E> newDurationProgressCounterLogger(Consumer<String> messageConsumer, int maxCount)
    {
        return ProgressUtils.newDurationProgressCounterLogger(messageConsumer, maxCount);
    }

    public static <E> Consumer<E> incrementCounter(Counter counter)
    {
        return e -> counter.increment();
    }
}
