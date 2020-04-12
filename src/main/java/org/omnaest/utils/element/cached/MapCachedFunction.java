package org.omnaest.utils.element.cached;

import java.util.Map;
import java.util.function.Function;

public class MapCachedFunction<T, R> implements CachedFunction<T, R>
{
    private Map<T, R>      map;
    private Function<T, R> function;

    public MapCachedFunction(Map<T, R> map, Function<T, R> function)
    {
        super();
        this.map = map;
        this.function = function;
    }

    @Override
    public R apply(T t)
    {
        return this.map.computeIfAbsent(t, this.function);
    }

}
