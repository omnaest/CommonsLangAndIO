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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.ThreadUtils;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.internal.DefaultDurationProgressCounter;

/**
 * @see DefaultDurationProgressCounter
 * @author omnaest
 */
public class DefaultDurationProgressCounterTest
{
    private DurationProgressCounter progressCounter = Counter.fromZero()
                                                             .asDurationProgressCounter();

    @Test
    public void testGetETA() throws Exception
    {
        int maximum = 5;
        int sleepDuration = 100;

        List<Long> etas = new ArrayList<>();

        IntStream.range(0, maximum)
                 .forEach(counter ->
                 {
                     this.progressCounter.increment()
                                         .withMaximum(maximum)
                                         .doWithETA(duration -> etas.add(duration.as(TimeUnit.MILLISECONDS)));
                     ThreadUtils.sleepSilently(sleepDuration, TimeUnit.MILLISECONDS);
                 });

        assertEquals(true, etas.stream()
                               .mapToLong(v -> (long) v)
                               .max()
                               .getAsLong() > sleepDuration);
    }

}
