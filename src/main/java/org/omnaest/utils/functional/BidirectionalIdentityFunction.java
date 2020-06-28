package org.omnaest.utils.functional;

import java.util.function.Function;

public class BidirectionalIdentityFunction<U> implements BidirectionalFunction<U, U>
{
    @Override
    public Function<U, U> forward()
    {
        return v -> v;
    }

    @Override
    public Function<U, U> backward()
    {
        return v -> v;
    }
}