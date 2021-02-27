package org.omnaest.utils.counter;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import org.omnaest.utils.duration.DurationCapture;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;

/**
 * {@link ProgressCounter} in combination with a {@link DurationCapture} instance. The duration measurement starts with the first counter {@link #increment()}.
 * 
 * @author omnaest
 */
public interface DurationProgressCounter extends ProgressCounter
{

    public Optional<DisplayableDuration> getETA();

    public Optional<DisplayableDuration> getPassedTime();

    public String getProgressAndETAasString();

    public DurationProgressCounter doWithETA(Consumer<DisplayableDuration> etaConsumer);

    public DurationProgressCounter doWithPassedTime(Consumer<DisplayableDuration> passedTimeConsumer);

    @Override
    public DurationProgressCounter withMaximumProvider(Supplier<Long> maximumProvider);

    @Override
    public DurationProgressCounter withMaximum(long maximum);

    @Override
    public DurationProgressCounter synchronizeWith(Counter sourceCounter);

    @Override
    public DurationProgressCounter incrementBy(int delta);

    @Override
    public DurationProgressCounter increment();

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

    public static DurationProgressCounter of(ProgressCounter progressCounter)
    {
        return new DefaultDurationProgressCounter(progressCounter);
    }

    public static DurationProgressCounter fromZero()
    {
        return Counter.fromZero()
                      .asDurationProgressCounter();
    }

    public static DurationProgressCounter from(int start)
    {
        return Counter.from(start)
                      .asDurationProgressCounter();
    }

    public static interface DurationProgressConsumer extends Consumer<DurationProgress>
    {
    }

    public static interface DurationProgress extends Progress
    {
        public Optional<DisplayableDuration> getETA();

        public Optional<DisplayableDuration> getPassedTime();
    }
}
