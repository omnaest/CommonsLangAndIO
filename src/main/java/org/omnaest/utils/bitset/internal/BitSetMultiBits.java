package org.omnaest.utils.bitset.internal;

import java.util.function.BiConsumer;

import org.omnaest.utils.bitset.Bits;
import org.omnaest.utils.bitset.MultiBits;

public class BitSetMultiBits implements MultiBits
{
    private final int  dimension;
    private final Bits bits = Bits.newInstance();

    public BitSetMultiBits(int dimension)
    {
        this.dimension = dimension;
    }

    @Override
    public Bits get(int index)
    {
        return this.bits.subset(index * this.dimension, (index + 1) * this.dimension);
    }

    @Override
    public MultiBits setIndex(int index, Bits bits)
    {
        this.bits.setIndex(index * this.dimension, bits);
        return this;
    }

    @Override
    public int getLength()
    {
        return this.bits.getLength() / this.dimension;
    }

    @Override
    public MultiBits setLength(int length)
    {
        this.bits.setLength(length * this.dimension);
        return this;
    }

    @Override
    public int getDimension()
    {
        return this.dimension;
    }

    @Override
    public MultiBits forEach(BiConsumer<Integer, Bits> consumer)
    {
        if (consumer != null)
        {
            for (int ii = 0; ii < this.getLength(); ii++)
            {
                consumer.accept(ii, this.get(ii));
            }
        }
        return this;
    }

}
