package org.omnaest.utils.functional;

@FunctionalInterface
public interface BiIntFunction<R>
{
    public R apply(int first, int second);
}
