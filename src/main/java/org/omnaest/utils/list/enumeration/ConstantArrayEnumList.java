package org.omnaest.utils.list.enumeration;

import java.util.Arrays;
import java.util.List;

import org.omnaest.utils.ListUtils;
import org.omnaest.utils.list.ListDecorator;

/**
 * Uses a constant array for the given {@link Enum} elements
 * 
 * @see EnumArrayList
 * @author omnaest
 * @param <E>
 */
public class ConstantArrayEnumList<E extends Enum<E>> extends ListDecorator<E> implements EnumList<E>
{

    @SafeVarargs
    public ConstantArrayEnumList(E... elements)
    {
        super(Arrays.asList(elements));
    }

    public ConstantArrayEnumList(List<E> list, Class<E> elementType)
    {
        this(ListUtils.toArray(list, elementType));
    }

    @Override
    public String toString()
    {
        return "ConstantArrayEnumList [list=" + this.list + "]";
    }

}
