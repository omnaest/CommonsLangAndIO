package org.omnaest.utils.counter.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.omnaest.utils.MapperUtils;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.ImmutableCounter;
import org.omnaest.utils.counter.ImmutableDurationProgressCounter;
import org.omnaest.utils.counter.ImmutableProgressCounter;
import org.omnaest.utils.counter.ImmutableProgressCounterContainer;
import org.omnaest.utils.counter.ProgressCounter;
import org.omnaest.utils.counter.ProgressCounterContainer;
import org.omnaest.utils.duration.DurationCapture;
import org.omnaest.utils.duration.DurationCapture.DurationMeasurement;

public class DefaultProgressCounterContainer implements ProgressCounterContainer
{
    private List<ProgressCounterContext> progressCounters = new ArrayList<>();

    @Override
    public Stream<ProgressCounterAccessor> stream()
    {
        return this.progressCounters.stream()
                                    .map(MapperUtils.identity());
    }

    @Override
    public ProgressCounter newProgressCounter()
    {
        double weight = 1.0;
        return this.newProgressCounterWithWeight(weight);
    }

    @Override
    public ProgressCounter newProgressCounterWithWeight(double weight)
    {
        String name = null;
        return this.newDurationProgressCounterWithWeight(name, weight);
    }

    @Override
    public ProgressCounter newProgressCounterWithWeight(String name, double weight)
    {
        return this.newDurationProgressCounterWithWeight(name, weight);
    }

    @Override
    public DurationProgressCounter newDurationProgressCounterWithWeight(double weight)
    {
        String name = null;
        return this.newDurationProgressCounterWithWeight(name, weight);
    }

    @Override
    public DurationProgressCounter newDurationProgressCounterWithWeight(String name, double weight)
    {
        DurationProgressCounter progressCounter = Counter.fromZero()
                                                         .asDurationProgressCounter();
        this.progressCounters.add(new ProgressCounterContext(progressCounter, weight, name));
        return progressCounter;
    }

    @Override
    public ProgressCounterContainer newDurationProgressCounterWithWeight(String name, double weight,
                                                                         Consumer<DurationProgressCounter> durationProgressCounterConsumer)
    {
        durationProgressCounterConsumer.accept(this.newDurationProgressCounterWithWeight(name, weight));
        return this;
    }

    @Override
    public ProgressCounterContainer newProgressCounter(Consumer<ProgressCounter> progressCounterConsumer)
    {
        progressCounterConsumer.accept(this.newProgressCounter());
        return this;
    }

    @Override
    public double getOverallProgress()
    {
        return this.progressCounters.stream()
                                    .mapToDouble(context -> context.getWeight() * context.getProgressCounter()
                                                                                         .getProgress())
                                    .sum();
    }

    @Override
    public long getOverallCount()
    {
        return this.progressCounters.stream()
                                    .map(ProgressCounterContext::getProgressCounter)
                                    .mapToLong(ProgressCounter::getAsLong)
                                    .sum();
    }

    @Override
    public long getOverallMaximum()
    {
        return this.progressCounters.stream()
                                    .map(ProgressCounterContext::getProgressCounter)
                                    .mapToLong(ProgressCounter::getMaximum)
                                    .sum();
    }

    @Override
    public ImmutableProgressCounter asProgressCounter()
    {
        return this.asDurationProgressCounter();
    }

    @Override
    public ImmutableDurationProgressCounter asDurationProgressCounter()
    {
        LongSupplier maximumProvider = () -> this.getOverallMaximum();
        Supplier<Optional<DurationMeasurement>> durationMeasurementSupplier = () ->
        {
            Optional<Instant> earliestStartTime = this.stream()
                                                      .map(ProgressCounterAccessor::getProgressCounter)
                                                      .map(ImmutableDurationProgressCounter::getStartTime)
                                                      .filter(Optional::isPresent)
                                                      .map(Optional::get)
                                                      .sorted()
                                                      .findFirst();
            return earliestStartTime.map(time -> DurationCapture.newInstance()
                                                                .from(time));
        };

        AbstractImmutableProgressCounter progressCounter = new AbstractImmutableProgressCounter(new CounterAdapter(() -> this.getOverallCount()))
        {
            @Override
            protected LongSupplier getMaximumProvider()
            {
                return maximumProvider;
            }

            @Override
            protected void runSynchronizePullOperations()
            {
                // nothing to do here               
            }
        };

        return new AbstractImmutableDurationProgressCounter(progressCounter)
        {
            @Override
            protected Optional<DurationMeasurement> getDurationMeasurement()
            {
                return durationMeasurementSupplier.get();
            }
        };

    }

    private static class CounterAdapter extends AbstractImmutableCounter implements ImmutableCounter
    {
        private LongSupplier counter;

        public CounterAdapter(LongSupplier counter)
        {
            super();
            this.counter = counter;
        }

        @Override
        public long getAsLong()
        {
            return this.counter.getAsLong();
        }

        @Override
        public ImmutableCounter ifModulo(int modulo, LongConsumer counterConsumer)
        {
            super.ifModulo(modulo, counterConsumer);
            return this;
        }

    }

    private static class ProgressCounterContext implements ProgressCounterAccessor
    {
        private DurationProgressCounter progressCounter;
        private double                  weight;
        private String                  name;

        public ProgressCounterContext(DurationProgressCounter progressCounter, double weight, String name)
        {
            super();
            this.progressCounter = progressCounter;
            this.weight = weight;
            this.name = name;
        }

        @Override
        public DurationProgressCounter getProgressCounter()
        {
            return this.progressCounter;
        }

        public double getWeight()
        {
            return this.weight;
        }

        @Override
        public Optional<String> getName()
        {
            return Optional.ofNullable(this.name);
        }

        @Override
        public String toString()
        {
            return "ProgressCounterContext [progressCounter=" + this.progressCounter + ", weight=" + this.weight + ", name=" + this.name + "]";
        }

    }

    @Override
    public ImmutableProgressCounterContainer asImmutableProgressCounterContainer()
    {
        return this;
    }

}
