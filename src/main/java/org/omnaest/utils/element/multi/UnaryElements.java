package org.omnaest.utils.element.multi;

import org.omnaest.utils.element.bi.UnaryBiElement;
import org.omnaest.utils.element.tri.UnaryTriElement;

/**
 * Wrapper around multiple elements of the same type.
 * 
 * @see #of(Object...)
 * @see UnaryBiElement
 * @see UnaryTriElement
 * @author omnaest
 * @param <E>
 */
public interface UnaryElements<E> extends UnaryTriElement<E>
{
    /**
     * Returns the n-th value. index = 1,2,3, ...
     * 
     * @param index
     * @return
     */
    public E getNTh(int index);

    public E getFourth();

    public E getFifth();

    /**
     * Creates a new {@link UnaryElements} instance with the given values
     * 
     * @param values
     * @return
     */
    @SafeVarargs
    public static <E> UnaryElements<E> of(E... values)
    {
        return new DefaultUnaryElements<>(values);
    }
}
