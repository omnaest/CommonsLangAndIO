package org.omnaest.utils.counter;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;

/**
 * Immutable {@link DurationProgressCounter} which only allows to read the underlying progress.
 * 
 * @author omnaest
 */
public interface ImmutableDurationProgressCounter extends ImmutableProgressCounter
{
    public Optional<DisplayableDuration> getETA();

    public Optional<DisplayableDuration> getPassedTime();

    public String getProgressAndETAasString();

    public DurationProgressCounter doWithETA(Consumer<DisplayableDuration> etaConsumer);

    public DurationProgressCounter doWithPassedTime(Consumer<DisplayableDuration> passedTimeConsumer);

    @Override
    public DurationProgressCounter ifModulo(int modulo, LongConsumer counterConsumer);

    @Override
    public DurationProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer);

    /**
     * Similar to {@link #ifModulo(int, LongConsumer)} but provides a {@link DurationProgress} via a {@link DurationProgressConsumer}
     * 
     * @param modulo
     * @param durationProgressConsumer
     * @return
     */
    public DurationProgressCounter ifModulo(int modulo, DurationProgressConsumer durationProgressConsumer);

    @Override
    public DurationProgressCounter doWithProgress(DoubleConsumer progressConsumer);

    public static interface DurationProgressConsumer extends Consumer<DurationProgress>
    {
    }

    public static interface DurationProgress extends Progress
    {
        public Optional<DisplayableDuration> getETA();

        public Optional<DisplayableDuration> getPassedTime();

        public String getProgressAndETAasString();

    }
}
