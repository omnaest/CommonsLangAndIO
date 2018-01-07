package org.omnaest.utils.element.bi;

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

}
