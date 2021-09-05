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

import java.util.function.Supplier;

/**
 * {@link Counter} with additional methods to track a progress
 * 
 * @author omnaest
 */
public interface ProgressCounter extends Counter, ImmutableProgressCounter
{
    public ProgressCounter withMaximumProvider(Supplier<Long> maximumProvider);

    public ProgressCounter withMaximum(long maximum);

    public ProgressCounter setProgress(double progress);

    public ProgressCounter synchronizeProgressContinouslyWith(ProgressCounter progressCounter);

    @Override
    public ProgressCounter synchronizeCountContinouslyWith(Counter counter);

    @Override
    public ProgressCounter synchronizeWith(Counter sourceCounter);

    public @Override ProgressCounter incrementBy(int delta);

    @Override
    public ProgressCounter increment();

    public ImmutableProgressCounter asImmutableProgressCounter();
}
