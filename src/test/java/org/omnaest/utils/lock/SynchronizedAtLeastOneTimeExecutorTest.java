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
package org.omnaest.utils.lock;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class SynchronizedAtLeastOneTimeExecutorTest
{

    @Test
    public void testFire() throws Exception
    {
        AtomicBoolean state = new AtomicBoolean(false);
        SynchronizedAtLeastOneTimeExecutor executor = new SynchronizedAtLeastOneTimeExecutor(Executors.newCachedThreadPool(), () -> state.set(true));

        executor.fire()
                .shutdown()
                .awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(state.get());
    }

}
