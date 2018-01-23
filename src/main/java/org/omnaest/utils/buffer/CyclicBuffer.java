package org.omnaest.utils.buffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.omnaest.utils.StreamUtils;

/**
 * A cyclic buffer is based on a {@link Object} array which is filled by the elements read by a source {@link Iterator}. The buffer cycles the position the
 * elements are written from the source into the buffer, so that the buffer allows to read a window around the current read position.
 * 
 * @see #withSource(Iterator)
 * @see #withSource(Stream)
 * @see #withSource(Collection)
 * @see #asIterator()
 * @see #asStream()
 * @author omnaest
 * @param <E>
 */
public class CyclicBuffer<E>
{
    private Iterator<E> source;
    private AtomicLong  sourcePosition = new AtomicLong();
    private AtomicLong  readPosition   = new AtomicLong(0);
    private Object[]    buffer;

    public CyclicBuffer(int windowSize)
    {
        super();
        this.buffer = new Object[windowSize];
    }

    public CyclicBuffer<E> withSource(Iterator<E> source)
    {
        this.source = source;
        this.initializeBufferWithSource();
        return this;
    }

    public CyclicBuffer<E> withSource(Stream<E> source)
    {
        return this.withSource(source.iterator());
    }

    public CyclicBuffer<E> withSource(Collection<E> source)
    {
        return this.withSource(source.iterator());
    }

    /**
     * Accessor of a single read window
     * 
     * @see #get()
     * @see #getPosition()
     * @author omnaest
     * @param <E>
     */
    public static interface Window<E>
    {
        /**
         * Returns the element at the current read {@link #getPosition()}
         * 
         * @return
         */
        public E get();

        /**
         * Returns the current read position
         * 
         * @return
         */
        public long getPosition();

        /**
         * Returns the left side of the {@link #getPosition()} with the given number of elements, excluding the element at the current position.
         * 
         * @param size
         * @return
         */
        public List<E> getBefore(int size);

        /**
         * Returns the right side of the {@link #getPosition()} with the given number of elements, excluding the element at the current position.
         * 
         * @param size
         * @return
         */
        public List<E> getAfter(int size);

        /**
         * Returns the window around the {@link #getPosition()} including the {@link #getBefore(int)} and {@link #get()} and {@link #getAfter(int)} with the given
         * sizes.
         * 
         * @param left
         * @param right
         * @return
         */
        public List<E> getWindow(int left, int right);
    }

    public static interface BufferAccessor<E>
    {
        public E get(int index);
    }

    private void initializeBufferWithSource()
    {
        int halfWindowSize = (int) Math.round(this.buffer.length / 2.0);
        for (int ii = 0; ii < halfWindowSize; ii++)
        {
            this.readFromSource();
        }
    }

    private void readFromSource()
    {
        if (this.source.hasNext())
        {
            E element = this.source.next();
            long position = this.sourcePosition.getAndIncrement();

            int index = this.determineBufferIndex(position, this.buffer);
            this.buffer[index] = element;
        }
    }

    private Window<E> readFromBuffer()
    {
        //
        long position = this.readPosition.getAndIncrement();

        Window<E> retval = new Window<E>()
        {
            private Object[] buffer = CyclicBuffer.this.cloneBuffer();

            @Override
            public E get()
            {
                E element = this.readPosition(position);
                return element;
            }

            @Override
            public List<E> getBefore(int size)
            {
                return this.readFromTo(position - size, position - 1);
            }

            @Override
            public List<E> getAfter(int size)
            {
                return this.readFromTo(position + 1, position + size);
            }

            @Override
            public List<E> getWindow(int left, int right)
            {
                return this.readFromTo(position - left, position + right);
            }

            @Override
            public long getPosition()
            {
                return position;
            }

            @SuppressWarnings("unchecked")
            private E readPosition(long position)
            {
                int index = CyclicBuffer.this.determineBufferIndex(position, this.buffer);
                E element = (E) this.buffer[index];
                return element;
            }

            @SuppressWarnings("unchecked")
            private List<E> readFromTo(long fromInclusive, long toInclusive)
            {
                //
                List<E> retlist = Collections.emptyList();

                //
                fromInclusive = fromInclusive >= 0 ? fromInclusive : 0;
                toInclusive = toInclusive <= CyclicBuffer.this.sourcePosition.get() - 1 ? toInclusive : CyclicBuffer.this.sourcePosition.get() - 1;

                //
                this.assertIndexPositionsInBufferRange(position, fromInclusive, toInclusive);

                //
                int size = (int) (toInclusive - fromInclusive + 1);
                if (size > 0)
                {
                    Object[] retval = new Object[size];
                    for (long ii = fromInclusive; ii <= toInclusive; ii++)
                    {
                        E element = this.readPosition(ii);
                        retval[(int) (ii - fromInclusive)] = element;
                    }
                    retlist = (List<E>) Arrays.asList(retval);
                }

                //
                return retlist;
            }

            private void assertIndexPositionsInBufferRange(long position, long fromInclusive, long toInclusive)
            {
                int maxHalfWindowSize = (this.buffer.length - 1) / 2;
                if (position - fromInclusive > maxHalfWindowSize || toInclusive - position > maxHalfWindowSize)
                {
                    throw new IndexOutOfBoundsException("Window size is too small for requested size");
                }
            }
        };

        //
        this.readFromSource();

        //
        return retval;
    }

    private Object[] cloneBuffer()
    {
        return ArrayUtils.clone(this.buffer);
    }

    private int determineBufferIndex(long position, Object[] buffer)
    {
        return (int) (position % buffer.length);
    }

    private boolean hasUnreadElement()
    {
        return this.readPosition.get() < this.sourcePosition.get();
    }

    public Iterator<Window<E>> asIterator()
    {
        return new Iterator<Window<E>>()
        {
            @Override
            public boolean hasNext()
            {
                return CyclicBuffer.this.hasUnreadElement();
            }

            @Override
            public Window<E> next()
            {
                return CyclicBuffer.this.readFromBuffer();
            }
        };
    }

    public Stream<Window<E>> asStream()
    {
        return StreamUtils.fromIterator(this.asIterator());
    }
}
