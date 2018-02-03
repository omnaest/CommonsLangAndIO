package org.omnaest.utils.element.bi;

import java.util.function.Function;

/**
 * Wrapper around two elements
 * 
 * @author omnaest
 * @param <E1>
 * @param <E2>
 */
public interface BiElement<E1, E2>
{
    public E1 getFirst();

    public E2 getSecond();

    public static <E1, E2> BiElement<E1, E2> of(E1 element1, E2 element2)
    {
        return new DefaultBiElement<E1, E2>(element1, element2);
    }

    public default <NE1> BiElement<NE1, E2> applyToFirstArgument(Function<E1, NE1> mapper)
    {
        return BiElement.of(mapper.apply(this.getFirst()), this.getSecond());
    }

    public default <NE2> BiElement<E1, NE2> applyToSecondArgument(Function<E2, NE2> mapper)
    {
        return BiElement.of(this.getFirst(), mapper.apply(this.getSecond()));
    }

}
