package org.omnaest.utils.element.bi;

/**
 * Special {@link UnaryBiElement} which contains two int values
 * 
 * @see UnaryBiElement
 * @author omnaest
 */
public interface IntUnaryBiElement
{
    /**
     * Returns the first element
     * 
     * @return
     */
    public int getFirst();

    /**
     * Returns the second element
     * 
     * @return
     */
    public int getSecond();

    public default UnaryBiElement<Integer> axBoxed()
    {
        return UnaryBiElement.of(this.getFirst(), this.getSecond());
    }

    public static IntUnaryBiElement of(int first, int second)
    {
        return new DefaultIntUnaryBiElement(first, second);
    }
}
