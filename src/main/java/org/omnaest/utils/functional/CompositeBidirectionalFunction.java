package org.omnaest.utils.functional;

import java.util.function.Function;

public class CompositeBidirectionalFunction<S, T> implements BidirectionalFunction<S, T>
{
    private Function<S, T> forward;
    private Function<T, S> backward;

    public CompositeBidirectionalFunction(Function<S, T> forward, Function<T, S> backward)
    {
        super();
        this.forward = forward;
        this.backward = backward;
    }

    @Override
    public Function<S, T> forward()
    {
        return this.forward;
    }

    @Override
    public Function<T, S> backward()
    {
        return this.backward;
    }

    @Override
    public String toString()
    {
        return "CompositeBidirectionalFunction [forward=" + this.forward + ", backward=" + this.backward + "]";
    }

}
