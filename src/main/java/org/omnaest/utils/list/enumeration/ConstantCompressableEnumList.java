package org.omnaest.utils.list.enumeration;

import java.util.Collection;

/**
 * A constant {@link CompressableEnumList} which uses an array as uncompressed implementation
 * 
 * @author omnaest
 * @param <E>
 */
public class ConstantCompressableEnumList<E extends Enum<E>> extends CompressableEnumList<E>
{
    public ConstantCompressableEnumList(Class<E> enumType, Collection<E> collection)
    {
        this(enumType, new EnumArrayList<>(collection));
    }

    public ConstantCompressableEnumList(Class<E> enumType, EnumList<E> enumList)
    {
        super(enumType);
        this.list = enumList;
        this.setUnCompressFunction(l -> new ConstantArrayEnumList<>(l, enumType));
        this.uncompress();
    }

    @Override
    public ConstantCompressableEnumList<E> usingInMemoryCompression(boolean active)
    {
        super.usingInMemoryCompression(active);
        return this;
    }

    @Override
    public String toString()
    {
        return "ConstantCompressableEnumList [list=" + this.list + "]";
    }

}
