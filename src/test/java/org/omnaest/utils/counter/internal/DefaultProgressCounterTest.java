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

import static org.junit.Assert.assertEquals;

import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.ProgressCounter;
import org.omnaest.utils.counter.internal.DefaultProgressCounter;

/**
 * @see DefaultProgressCounter
 * @author omnaest
 */
public class DefaultProgressCounterTest
{
    @Test
    public void testGetProgress() throws Exception
    {
        ProgressCounter progressCounter = Counter.fromZero()
                                                 .asProgressCounter()
                                                 .withMaximum(100);

        IntStream.range(0, 100)
                 .forEach(counter -> assertEquals(counter + 1, Math.round(progressCounter.increment()
                                                                                         .getProgress()
                         * 100)));
    }

    @Test
    public void testGetProgressWithUnlimitedMaximum() throws Exception
    {
        ProgressCounter progressCounter = Counter.fromZero()
                                                 .asProgressCounter();
        IntStream.range(0, 100)
                 .forEach(counter -> assertEquals(100 * (1 - 1.0 / (1.0 + Math.log10(counter + 1.0 + 1))), progressCounter.increment()
                                                                                                                          .getProgress()
                         * 100, 0.01));
    }
}
