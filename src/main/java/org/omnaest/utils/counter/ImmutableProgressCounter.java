package org.omnaest.utils.counter;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

/**
 * Immutable {@link ProgressCounter} which only allows to read the underlying progress.
 * 
 * @author omnaest
 */
public interface ImmutableProgressCounter extends ImmutableCounter
{
    public double getProgress();

    /**
     * Returns a formatted progress {@link String} like e.g. '15%'
     * 
     * @return
     */
    public String getProgressAsString();

    public ImmutableProgressCounter doWithProgress(DoubleConsumer progressConsumer);

    @Override
    public ImmutableProgressCounter ifModulo(int modulo, LongConsumer counterConsumer);

    /**
     * Similar to {@link #ifModulo(int, LongConsumer)} but provides a {@link ProgressCounter} via the given {@link ProgressConsumer}
     * 
     * @param modulo
     * @param progressConsumer
     * @return
     */
    public ImmutableProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer);

    /**
     * Returns the current maximum
     * 
     * @return
     */
    public long getMaximum();

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
