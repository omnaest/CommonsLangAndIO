package org.omnaest.utils.counter.internal;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.ImmutableDurationProgressCounter;
import org.omnaest.utils.counter.ImmutableProgressCounter;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;
import org.omnaest.utils.duration.DurationCapture.DurationMeasurement;
import org.omnaest.utils.duration.DurationCapture.MeasurementResult;

public abstract class AbstractImmutableDurationProgressCounter implements ImmutableDurationProgressCounter
{
    private ImmutableProgressCounter progressCounter;

    public AbstractImmutableDurationProgressCounter(ImmutableProgressCounter progressCounter)
    {
        super();
        this.progressCounter = progressCounter;
    }

    protected abstract Optional<DurationMeasurement> getDurationMeasurement();

    @Override
    public String getProgressAsString()
    {
        return this.progressCounter.getProgressAsString();
    }

    @Override
    public long getMaximum()
    {
        return this.progressCounter.getMaximum();
    }

    @Override
    public Optional<DisplayableDuration> getETA()
    {
        return this.getDurationMeasurement()
                   .map(DurationMeasurement::stop)
                   .map(measurementResult -> measurementResult.toETA(this.getProgress()));
    }

    @Override
    public Optional<DisplayableDuration> getPassedTime()
    {
        return this.getDurationMeasurement()
                   .map(DurationMeasurement::stop)
                   .map(MeasurementResult::asTimeUnitDisplay);
    }

    @Override
    public Optional<Instant> getStartTime()
    {
        return this.getDurationMeasurement()
                   .map(DurationMeasurement::getStartTime);
    }

    @Override
    public String getProgressAndETAasString()
    {
        return this.getProgressAsString() + this.getETA()
                                                .map(DisplayableDuration::asCanonicalString)
                                                .map(this.createEtaToStringMapper())
                                                .orElse("");
    }

    @Override
    public ImmutableDurationProgressCounter doWithETA(Consumer<DisplayableDuration> etaConsumer)
    {
        Optional.ofNullable(etaConsumer)
                .ifPresent(consumer -> this.getETA()
                                           .ifPresent(consumer::accept));
        return this;
    }

    @Override
    public ImmutableDurationProgressCounter doWithPassedTime(Consumer<DisplayableDuration> passedTimeConsumer)
    {
        Optional.ofNullable(passedTimeConsumer)
                .ifPresent(consumer -> this.getPassedTime()
                                           .ifPresent(consumer::accept));
        return this;
    }

    @Override
    public long deltaTo(Counter otherCounter)
    {
        return this.progressCounter.deltaTo(otherCounter);
    }

    @Override
    public double getProgress()
    {
        return this.progressCounter.getProgress();
    }

    @Override
    public ImmutableDurationProgressCounter ifModulo(int modulo, LongConsumer counterConsumer)
    {
        this.progressCounter.ifModulo(modulo, counterConsumer);
        return this;
    }

    @Override
    public ImmutableDurationProgressCounter ifModulo(int modulo, ProgressConsumer progressConsumer)
    {
        this.progressCounter.ifModulo(modulo, progressConsumer);
        return this;
    }

    @Override
    public ImmutableDurationProgressCounter ifModulo(int modulo, DurationProgressConsumer durationProgressConsumer)
    {
        this.progressCounter.ifModulo(modulo, (Progress progressCounter) ->
        {
            Optional<DisplayableDuration> eta = this.getETA();
            Optional<DisplayableDuration> passedTime = this.getPassedTime();
            durationProgressConsumer.accept(new DurationProgress()
            {
                @Override
                public long getCounter()
                {
                    return progressCounter.getCounter();
                }

                @Override
                public double getProgress()
                {
                    return progressCounter.getProgress();
                }

                @Override
                public Optional<DisplayableDuration> getETA()
                {
                    return eta;
                }

                @Override
                public String getProgressAsString()
                {
                    return progressCounter.getProgressAsString();
                }

                @Override
                public Optional<DisplayableDuration> getPassedTime()
                {
                    return passedTime;
                }

                @Override
                public String getProgressAndETAasString()
                {
                    return this.getProgressAsString() + this.getETA()
                                                            .map(DisplayableDuration::asCanonicalString)
                                                            .map(AbstractImmutableDurationProgressCounter.this.createEtaToStringMapper())
                                                            .orElse("");
                }

            });
        });
        return this;
    }

    private Function<String, String> createEtaToStringMapper()
    {
        return eta -> " ( " + eta + " )";
    }

    @Override
    public long getAsLong()
    {
        return this.progressCounter.getAsLong();
    }

    @Override
    public ImmutableDurationProgressCounter doWithProgress(DoubleConsumer progressConsumer)
    {
        this.progressCounter.doWithProgress(progressConsumer);
        return this;
    }

}