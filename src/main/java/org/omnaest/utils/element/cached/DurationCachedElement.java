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
package org.omnaest.utils.element.cached;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.omnaest.utils.duration.DurationCapture;
import org.omnaest.utils.duration.DurationCapture.DurationMeasurement;
import org.omnaest.utils.duration.TimeDuration;

/**
 * {@link CachedElement} wrapper which caches only for a given {@link TimeDuration}
 * 
 * @author omnaest
 * @param <E>
 */
public class DurationCachedElement<E> implements CachedElement<E>
{
    private CachedElement<E>                     cachedElement;
    private TimeDuration                         timeDuration;
    private AtomicReference<DurationMeasurement> measurement = new AtomicReference<>();

    public DurationCachedElement(CachedElement<E> cachedElement, TimeDuration timeDuration)
    {
        super();
        this.cachedElement = cachedElement;
        this.timeDuration = timeDuration;
    }

    @Override
    public E get()
    {
        this.resetIfDurationExceeded();
        return this.cachedElement.get();
    }

    private void resetIfDurationExceeded()
    {
        DurationMeasurement durationMeasurement = this.measurement.updateAndGet(previous -> Optional.ofNullable(previous)
                                                                                                    .orElseGet(() -> DurationCapture.newInstance()
                                                                                                                                    .start()));
        if (durationMeasurement.stop()
                               .isDurationLargerThen(this.timeDuration))
        {
            this.reset();
        }
    }

    private void resetDurationMeasurement()
    {
        this.measurement.set(DurationCapture.newInstance()
                                            .start());
    }

    @Override
    public E getAndReset()
    {
        this.resetDurationMeasurement();
        return this.cachedElement.get();
    }

    @Override
    public CachedElement<E> reset()
    {
        this.resetDurationMeasurement();
        return this.cachedElement.reset();
    }

    @Override
    public CachedElement<E> setSupplier(Supplier<E> supplier)
    {
        return this.cachedElement.setSupplier(supplier);
    }

}
