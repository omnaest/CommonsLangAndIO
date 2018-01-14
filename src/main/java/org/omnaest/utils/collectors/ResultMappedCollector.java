package org.omnaest.utils.collectors;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.omnaest.utils.SetUtils;

/**
 * A {@link Collector} wich decorates another {@link Collector} with an additional {@link Function} which is applied to the finisher.
 * 
 * @author omnaest
 * @param <T>
 * @param <A>
 * @param <R1>
 * @param <R>
 */
public class ResultMappedCollector<T, A, R1, R> implements Collector<T, A, R>
{
    private Collector<T, A, R1> collector;
    private Function<R1, R>     resultMapper;

    public ResultMappedCollector(Collector<T, A, R1> collector, Function<R1, R> resultMapper)
    {
        super();
        this.collector = collector;
        this.resultMapper = resultMapper;
    }

    @Override
    public Supplier<A> supplier()
    {
        return this.collector.supplier();
    }

    @Override
    public BiConsumer<A, T> accumulator()
    {
        return this.collector.accumulator();
    }

    @Override
    public BinaryOperator<A> combiner()
    {
        return this.collector.combiner();
    }

    @Override
    public Function<A, R> finisher()
    {
        return this.collector.finisher()
                             .andThen(this.resultMapper);
    }

    @Override
    public Set<Characteristics> characteristics()
    {
        return SetUtils.copyAndRemove(this.collector.characteristics(), Collector.Characteristics.IDENTITY_FINISH);
    }

    @Override
    public String toString()
    {
        return "ResultMappedCollector [collector=" + this.collector + ", resultMapper=" + this.resultMapper + "]";
    }

}
