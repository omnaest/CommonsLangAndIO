package org.omnaest.utils.element.tri;

import org.omnaest.utils.element.bi.BiElement;

/**
 * Representation of a tuple of three elements
 * 
 * @see BiElement
 * @author omnaest
 * @param <E1>
 * @param <E2>
 * @param <E3>
 */
public interface TriElement<E1, E2, E3>
{
    public E1 getFirst();

    public E2 getSecond();

    public E3 getThird();

    public BiElement<E1, E2> getFirstAndSecond();

    public BiElement<E1, E3> getFirstAndThird();

    public BiElement<E2, E3> getSecondAndThird();

    /**
     * Creates a new {@link TriElement} instance
     * 
     * @param first
     * @param second
     * @param third
     * @return
     */
    public static <E1, E2, E3> TriElement<E1, E2, E3> of(E1 first, E2 second, E3 third)
    {
        return new DefaultTriElement<E1, E2, E3>(first, second, third);
    }

    /**
     * Returns an empty {@link TriElement}
     * 
     * @return
     */
    public static <R1, R2, R3> TriElement<R1, R2, R3> empty()
    {
        return TriElement.of(null, null, null);
    }

}
