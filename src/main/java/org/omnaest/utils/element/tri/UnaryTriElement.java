package org.omnaest.utils.element.tri;

import org.omnaest.utils.element.bi.UnaryBiElement;
import org.omnaest.utils.element.multi.UnaryElements;

/**
 * @see #of(Object, Object, Object)
 * @see TriElement
 * @see UnaryElements
 * @author omnaest
 * @param <E>
 */
public interface UnaryTriElement<E> extends TriElement<E, E, E>
{

    @Override
    public UnaryBiElement<E> getFirstAndSecond();

    @Override
    public UnaryBiElement<E> getFirstAndThird();

    @Override
    public UnaryBiElement<E> getSecondAndThird();

    /**
     * Creates a new {@link UnaryTriElement} instance
     * 
     * @param first
     * @param second
     * @param third
     * @return
     */
    public static <E> UnaryTriElement<E> of(E first, E second, E third)
    {
        return new DefaultUnaryTriElement<E>(first, second, third);
    }
}
