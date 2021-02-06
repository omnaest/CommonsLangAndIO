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
