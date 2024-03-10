package org.omnaest.utils.list.cyclic;

import java.util.List;

import org.omnaest.utils.list.AbstractList;

/**
 * Non thread safe cyclic {@link List} implementation that is based on an static internal array structure.<br>
 * <br>
 * The primary method should be the {@link #add(Object)} that will cycle through all positions of the {@link List}.<br>
 * <br>
 * Initially the size will be 0 and it will grow until the given capacity limit with more and more added items.
 * 
 * @author omnaest
 * @param <E>
 */
public class CyclicArrayList<E> extends AbstractList<E>
{
    private final Items<E> items;
    private Cursor         cursor;
    private int            size = 0;
    private boolean        floating;

    public CyclicArrayList(int capacity)
    {
        this(capacity, false);
    }

    public CyclicArrayList(int capacity, boolean floating)
    {
        super();
        this.floating = floating;
        this.cursor = new Cursor(capacity);
        this.items = new Items<>(capacity, floating, this.cursor);
    }

    @Override
    public int size()
    {
        return this.size;
    }

    @Override
    public E get(int index)
    {
        this.validateReadIndexPosition(index);
        return this.items.getRawItem(index);
    }

    private void validateReadIndexPosition(int index)
    {
        if (index < 0 || index >= this.size())
        {
            throw new IndexOutOfBoundsException(index);
        }
    }

    @Override
    public E set(int index, E element)
    {
        this.validateWriteIndexPosition(index);
        return this.items.setRawItem(index, element);
    }

    private void validateWriteIndexPosition(int index)
    {
        if (index < 0 || index >= this.items.getSize())
        {
            throw new IndexOutOfBoundsException(index);
        }
    }

    @Override
    public void add(int index, E element)
    {
        for (int i = this.size - 1; i > index; i--)
        {
            this.set(i, this.get(i - 1));
        }

        this.set(index, element);

        this.size = Math.min(this.size + 1, this.items.getSize());
    }

    @Override
    public boolean add(E element)
    {
        int index = this.cursor.incrementAndGetPosition();

        if (this.floating)
        {
            if (this.size == this.items.getSize())
            {
                this.set(this.size - 1, element);
            }
            else
            {
                this.add(this.size, element);
            }
        }
        else
        {
            if (this.cursor.hasExceededLimit())
            {
                this.set(index, element);
            }
            else
            {
                this.add(index, element);
            }
        }

        return true;
    }

    @Override
    public E remove(int index)
    {
        this.validateReadIndexPosition(index);

        E item = this.items.getRawItem(index);

        int size = this.size();
        int lastIndexPosition = index + size - 1;
        for (int i = index; i < lastIndexPosition; i++)
        {
            this.items.setRawItem(i, this.items.getRawItem((i + 1)));
        }

        if (lastIndexPosition >= 0)
        {
            this.items.setRawItem(lastIndexPosition, null);
        }

        this.size--;

        return item;
    }

    private static class Cursor
    {
        private final int limit;
        private int       position      = -1;
        private boolean   exceededLimit = false;

        public Cursor(int limit)
        {
            super();
            this.limit = limit;
        }

        public int getPosition()
        {
            return this.position;
        }

        public int incrementAndGetPosition()
        {
            int result = ++this.position % this.limit;

            if (this.position >= this.limit)
            {
                this.exceededLimit = true;
                this.position = this.position % this.limit;
            }
            return result;
        }

        public boolean hasExceededLimit()
        {
            return this.exceededLimit;
        }
    }

    private static class Items<E>
    {
        private final Object[] items;
        private final Cursor   cursor;
        private final boolean  floating;

        public Items(int capacity, boolean floating, Cursor cursor)
        {
            this.cursor = cursor;
            this.items = new Object[capacity];
            this.floating = floating;
        }

        public int getSize()
        {
            return this.items.length;
        }

        @SuppressWarnings("unchecked")
        public E setRawItem(int index, E element)
        {
            int position = this.calculateEffectiveIndexPosition(index);
            E result = (E) this.items[position];
            this.items[position] = element;
            return result;
        }

        @SuppressWarnings("unchecked")
        public E getRawItem(int index)
        {
            int position = this.calculateEffectiveIndexPosition(index);
            return (E) this.items[position];
        }

        private int calculateEffectiveIndexPosition(int index)
        {
            if (this.floating)
            {
                return !this.cursor.hasExceededLimit() ? index % this.items.length : (this.cursor.getPosition() + index + 1) % this.items.length;
            }
            else
            {
                return index;
            }
        }
    }
}
