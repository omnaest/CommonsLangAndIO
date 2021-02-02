package org.omnaest.utils.element.bi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.omnaest.utils.element.tri.TriElement;

/**
 * Wrapper around two elements
 * 
 * @see TriElement
 * @author omnaest
 * @param <E1>
 * @param <E2>
 */
public interface BiElement<E1, E2>
{
    /**
     * Returns the first element
     * 
     * @return
     */
    public E1 getFirst();

    /**
     * Returns the second element
     * 
     * @return
     */
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

    /**
     * Returns a {@link Map} containing the two elements of the {@link BiElement} as key and value
     * 
     * @return
     */
    public default Map<E1, E2> asMap()
    {
        Map<E1, E2> map = new HashMap<>();
        map.put(this.getFirst(), this.getSecond());
        return map;
    }

    /**
     * Returns an {@link UnaryBiElement} instance based on the value of the current {@link BiElement}. If the value types are incompatible a
     * {@link ClassCastException} is thrown.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public default UnaryBiElement<E1> asUnary()
    {
        return UnaryBiElement.of(getFirst(), (E1) getSecond());
    }

    /**
     * Returns true if {@link #getFirst()} or {@link #getSecond()} returns null
     * 
     * @return
     */
    public default boolean hasAnyNullValue()
    {
        return this.isFirstValueNull() || this.isSecondValueNull();
    }

    /**
     * Returns true, if {@link #getSecond()} returns null
     * 
     * @return
     */
    public default boolean isSecondValueNull()
    {
        return this.getSecond() == null;
    }

    /**
     * Returns true, if {@link #getFirst()} returns null
     * 
     * @return
     */
    public default boolean isFirstValueNull()
    {
        return this.getFirst() == null;
    }

}
