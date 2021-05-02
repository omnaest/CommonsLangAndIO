package org.omnaest.utils.counter;

import java.util.function.Consumer;

import org.omnaest.utils.counter.DurationProgressCounter.DurationProgressConsumer;

/**
 * Utils around {@link Counter}, {@link ProgressCounter} and {@link DurationProgressCounter}
 * 
 * @author omnaest
 */
public class ProgressUtils
{
    public static DurationProgressCounter newDurationProgressCounter()
    {
        return DurationProgressCounter.fromZero();
    }

    public static <E> Consumer<E> newDurationProgressCounterLogger(Consumer<String> messageConsumer, int maxCount)
    {
        int stepsInBetween = Math.max(1, maxCount / 100);
        return newDurationProgressCounterLogger(messageConsumer, maxCount, stepsInBetween);
    }

    public static <E> Consumer<E> newDurationProgressCounterLogger(Consumer<String> messageConsumer, int maxCount, int stepsInBetween)
    {
        DurationProgressCounter durationProgressCounter = DurationProgressCounter.fromZero()
                                                                                 .withMaximum(maxCount);
        return element -> durationProgressCounter.increment()
                                                 .ifModulo(stepsInBetween,
                                                           (DurationProgressConsumer) duration -> messageConsumer.accept(duration.getProgressAndETAasString()));
    }

    public static ProgressCounter newProgressCounter()
    {
        return Counter.fromZero()
                      .asProgressCounter();
    }
}
