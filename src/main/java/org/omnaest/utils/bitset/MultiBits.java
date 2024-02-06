package org.omnaest.utils.bitset;

import java.util.function.BiConsumer;

import org.omnaest.utils.bitset.internal.BitSetMultiBits;

/**
 * @see Bits
 * @see EnumBits
 * @author omnaest
 */
public interface MultiBits
{
    /**
     * Returns all {@link Bits} at the given index position.
     * 
     * @param index
     * @return
     */
    public Bits get(int index);

    public MultiBits setIndex(int index, Bits bits);

    public int getLength();

    public MultiBits setLength(int length);

    public int getDimension();

    public MultiBits forEach(BiConsumer<Integer, Bits> consumer);

    public static MultiBits newInstance(int dimension)
    {
        return new BitSetMultiBits(dimension);
    }

}
