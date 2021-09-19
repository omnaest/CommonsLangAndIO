package org.omnaest.utils.counter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @see ProgressCounterContainer
 * @author omnaest
 */
public interface ImmutableProgressCounterContainer
{

    public double getOverallProgress();

    public long getOverallMaximum();

    public long getOverallCount();

    public ImmutableProgressCounter asProgressCounter();

    public ImmutableDurationProgressCounter asDurationProgressCounter();

    public Stream<ProgressCounterAccessor> stream();

    public static interface ProgressCounterAccessor
    {
        public Optional<String> getName();

        public ImmutableDurationProgressCounter getProgressCounter();
    }

}