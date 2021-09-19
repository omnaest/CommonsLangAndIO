package org.omnaest.utils.counter;

import java.util.function.Consumer;

import org.omnaest.utils.counter.internal.DefaultProgressCounterContainer;

/**
 * Containers to create and compose {@link DurationProgressCounter} instances
 * 
 * @author omnaest
 */
public interface ProgressCounterContainer extends ImmutableProgressCounterContainer
{
    public ProgressCounter newProgressCounter();

    public ProgressCounterContainer newProgressCounter(Consumer<ProgressCounter> progressCounterConsumer);

    public DurationProgressCounter newDurationProgressCounterWithWeight(String name, double weight);

    public ProgressCounterContainer newDurationProgressCounterWithWeight(String name, double weight,
                                                                         Consumer<DurationProgressCounter> durationProgressCounterConsumer);

    public DurationProgressCounter newDurationProgressCounterWithWeight(double weight);

    public ProgressCounter newProgressCounterWithWeight(String name, double weight);

    public ProgressCounter newProgressCounterWithWeight(double weight);

    public ImmutableProgressCounterContainer asImmutableProgressCounterContainer();

    public static ProgressCounterContainer newInstance()
    {
        return new DefaultProgressCounterContainer();
    }
}
