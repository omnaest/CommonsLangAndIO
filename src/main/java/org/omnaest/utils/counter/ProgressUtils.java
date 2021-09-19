package org.omnaest.utils.counter;

import java.util.function.Consumer;

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

    public static <E> IncrementCounterLogger<E> newDurationProgressCounterLogger(Consumer<String> messageConsumer, int maxCount)
    {
        int stepsInBetween = Math.max(1, maxCount / 100);
        return newDurationProgressCounterLogger(messageConsumer, maxCount, stepsInBetween);
    }

    public static interface IncrementCounterLogger<E> extends Consumer<E>
    {
        public IncrementCounterLogger<E> by(int increment);
    }

    public static <E> IncrementCounterLogger<E> newDurationProgressCounterLogger(Consumer<String> messageConsumer, int maxCount, int stepsInBetween)
    {
        DurationProgressCounter durationProgressCounter = DurationProgressCounter.fromZero()
                                                                                 .withMaximum(maxCount);
        return new IncrementCounterLogger<E>()
        {
            private int increment = 1;

            @Override
            public void accept(E element)
            {
                durationProgressCounter.incrementBy(this.increment)
                                       .ifModulo(stepsInBetween,
                                                 (ImmutableDurationProgressCounter.DurationProgressConsumer) duration -> messageConsumer.accept(duration.getProgressAndETAasString()));
            }

            @Override
            public IncrementCounterLogger<E> by(int increment)
            {
                this.increment = increment;
                return this;
            }
        };
    }

    public static ProgressCounter newProgressCounter()
    {
        return Counter.fromZero()
                      .asProgressCounter();
    }
}
