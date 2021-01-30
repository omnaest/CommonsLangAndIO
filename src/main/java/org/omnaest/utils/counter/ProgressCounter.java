package org.omnaest.utils.counter;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * {@link Counter} with additional methods to track a progress
 * 
 * @author omnaest
 */
public interface ProgressCounter extends Counter
{

    public ProgressCounter withMaximumProvider(Supplier<Long> maximumProvider);

    public ProgressCounter withMaximum(long maximum);

    public double getProgress();

    public ProgressCounter doWithProgress(DoubleConsumer progressConsumer);

    @Override
    public ProgressCounter synchronizeWith(Counter sourceCounter);

    public @Override ProgressCounter incrementBy(int delta);

    @Override
    public ProgressCounter increment();

    @Override
    public ProgressCounter ifModulo(int modulo, LongConsumer counterConsumer);

    public ProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer);

    public static interface ProgressConsumer extends Consumer<Progress>
    {
    }

    public static interface Progress
    {
        public long getCounter();

        public double getProgress();

        /**
         * Returns the progress as integer percentage
         * 
         * @return
         */
        public String getProgressAsString();
    }
}
