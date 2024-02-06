package org.omnaest.utils.bitset;

import java.util.List;
import java.util.function.BiConsumer;

import org.omnaest.utils.bitset.internal.BitSetEnumBits;

/**
 * @see Bits
 * @see MultiBits
 * @author omnaest
 */
public interface EnumBits<E>
{
    public E get(int index);

    public EnumBits<E> setIndex(int index, E value);

    public EnumBits<E> set(EnumBits<E> enumBits);

    public EnumBits<E> setLength(int length);

    public int getLength();

    public EnumBits<E> forEach(BiConsumer<Integer, E> consumer);

    /**
     * Returns true, if any index position has the same value as any of the given enum values.
     * 
     * @param enumValue
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean hasAnyIndexValueOfAny(E... enumValue);

    public List<E> toList();

    public static <E extends Enum<E>> EnumBits<E> newInstance(Class<E> enumType)
    {
        return new BitSetEnumBits<E>(enumType);
    }

}
