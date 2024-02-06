package org.omnaest.utils.bitset.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.EnumUtils;
import org.omnaest.utils.SetUtils;
import org.omnaest.utils.bitset.Bits;
import org.omnaest.utils.bitset.EnumBits;
import org.omnaest.utils.bitset.MultiBits;

public class BitSetEnumBits<E extends Enum<E>> implements EnumBits<E>
{
    private final List<E>   enumValues;
    private final MultiBits bits;

    public BitSetEnumBits(Class<E> enumType)
    {
        this.enumValues = EnumUtils.getEnumList(enumType);
        this.bits = MultiBits.newInstance(Bits.of(this.enumValues.size())
                                              .findLastSetBitIndex()
                                              .orElse(0)
                + 1);
    }

    @Override
    public E get(int index)
    {
        return this.enumValues.get(this.bits.get(index)
                                            .toInt());
    }

    @Override
    public EnumBits<E> setIndex(int index, E value)
    {
        Bits ordinal = Bits.of(value.ordinal());
        ordinal.setLength(this.bits.getDimension());
        this.bits.setIndex(index, ordinal);
        return this;
    }

    @Override
    public List<E> toList()
    {
        List<E> result = new ArrayList<>();
        for (int ii = 0; ii < this.bits.getLength(); ii++)
        {
            result.add(this.get(ii));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public EnumBits<E> setLength(int length)
    {
        this.bits.setLength(length);
        return this;
    }

    @Override
    public int getLength()
    {
        return this.bits.getLength();
    }

    @Override
    public EnumBits<E> set(EnumBits<E> enumBits)
    {
        if (enumBits != null)
        {
            enumBits.forEach(this::setIndex);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasAnyIndexValueOfAny(E... enumValue)
    {
        Set<E> enumValues = SetUtils.toSet(enumValue);
        for (int ii = 0; ii < this.bits.getLength(); ii++)
        {
            if (enumValues.contains(this.get(ii)))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumBits<E> forEach(BiConsumer<Integer, E> consumer)
    {
        this.bits.forEach((index, bits) -> consumer.accept(index, this.enumValues.get(bits.toInt())));
        return this;
    }

}
