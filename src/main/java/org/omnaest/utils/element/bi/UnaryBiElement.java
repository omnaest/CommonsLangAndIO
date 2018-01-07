package org.omnaest.utils.element.bi;

import java.util.List;

/**
 * {@link BiElement} with two elements of the same type
 * 
 * @author omnaest
 * @param <E>
 */
public interface UnaryBiElement<E> extends BiElement<E, E>
{
    /**
     * Returns a non modifiable {@link List} for the given elements
     * 
     * @return
     */
    public List<E> asList();

    /**
     * Returns a new {@link UnaryBiElement} for the given elements
     * 
     * @param element1
     * @param element2
     * @return
     */
    public static <E> UnaryBiElement<E> of(E element1, E element2)
    {
        return new DefaultUnaryBiElement<E>(element1, element2);
    }
}
