/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.omnaest.utils.ExecutorUtils.ParallelExecution;
import org.omnaest.utils.buffer.CyclicBuffer;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.ImmutableDurationProgressCounter.DurationProgressConsumer;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.bi.IntUnaryBiElement;
import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.element.cached.CachedFunction;
import org.omnaest.utils.element.cached.SingleKeyCachedElement;
import org.omnaest.utils.element.lar.LeftAndRight;
import org.omnaest.utils.functional.PredicateConsumer;
import org.omnaest.utils.stream.DefaultSupplierStream;
import org.omnaest.utils.stream.FilterAllOnFirstFilterFailStreamDecorator;
import org.omnaest.utils.stream.FilterMapper;
import org.omnaest.utils.stream.StreamDecoratorLazyLoading;
import org.omnaest.utils.stream.Streamable;
import org.omnaest.utils.stream.SupplierStream;
import org.omnaest.utils.supplier.OptionalSupplier;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;

/**
 * Utils around {@link Stream}s
 * 
 * @author omnaest
 */
public class StreamUtils
{

    /**
     * Concatenates two or more {@link Stream}s
     * 
     * @param streams
     * @return
     */
    @SafeVarargs
    public static <E> Stream<E> concat(Stream<E>... streams)
    {
        return concat(Stream.of(streams));
    }

    /**
     * Concatenates a {@link Stream} of {@link Stream}s
     * 
     * @param streams
     * @return
     */
    public static <E> Stream<E> concat(Stream<Stream<E>> streams)
    {
        return streams.reduce(Stream::concat)
                      .orElseGet(() -> Stream.empty());
    }

    /**
     * Returns a {@link Stream} from a {@link Queue} using the {@link Queue#poll()} method
     * 
     * @param queue
     * @return
     */
    public static <E> Stream<E> fromQueue(Queue<E> queue)
    {
        return fromIterator(IteratorUtils.from(queue));
    }

    /**
     * Returns a {@link Stream} from the given {@link Queue} using any of its methods
     * 
     * @param queue
     * @param elementDrainFunction
     * @return
     */
    public static <E> Stream<E> fromQueue(Queue<E> queue, Function<Queue<E>, E> elementDrainFunction)
    {
        return fromIterator(IteratorUtils.from(queue, elementDrainFunction));
    }

    /**
     * Returns a {@link Stream} from a given {@link Iterator}
     * 
     * @param iterator
     * @return
     */
    public static <E> Stream<E> fromIterator(Iterator<E> iterator)
    {
        return StreamSupport.stream(((Iterable<E>) () -> iterator).spliterator(), false)
                            .sequential();
    }

    /**
     * Returns a {@link Stream} from an {@link OptionalSupplier}
     * 
     * @param optionalSupplier
     * @return
     */
    public static <E> Stream<E> fromOptionalSupplier(Supplier<Optional<E>> optionalSupplier)
    {
        return fromSupplier(optionalSupplier).withTerminationMatcher(optional -> !optional.isPresent())
                                             .map(optional -> optional.get());
    }

    /**
     * Returns a {@link SupplierStream} based on the given {@link Supplier} where the given {@link Predicate} does return true at the end element of the
     * {@link Stream}
     * 
     * @param supplier
     * @param terminationMatcher
     * @return
     */
    public static <E> SupplierStream<E> fromSupplier(Supplier<E> supplier, Predicate<E> terminationMatcher)
    {
        return fromSupplier(supplier).withTerminationMatcher(terminationMatcher);
    }

    /**
     * Similar to {@link #fromSupplier(Supplier, Predicate)} for {@link IntStream}
     * 
     * @param supplier
     * @param terminationMatcher
     * @return
     */
    public static IntStream fromIntSupplier(IntSupplier supplier, IntPredicate terminationMatcher)
    {
        return fromSupplier(() -> supplier.getAsInt(), v -> terminationMatcher.test(v)).mapToInt(MapperUtils.identitiyForIntegerAsUnboxed());
    }

    public static <E> SupplierStream<E> fromSupplier(Supplier<E> supplier)
    {
        return new DefaultSupplierStream<>(supplier);
    }

    /**
     * Reverses the order of the given {@link Stream}. Be aware that this will terminate the given {@link Stream} and returns a new {@link Stream}, which makes
     * this a TERMINAL operation!!<br>
     * <br>
     * Also important: this will need to read the whole source {@link Stream} into a memory object to reverse it.
     *
     * @param stream
     * @return
     */
    public static <E> Stream<E> reverse(Stream<E> stream)
    {
        List<E> list = Optional.ofNullable(stream)
                               .orElse(Stream.empty())
                               .collect(Collectors.toList());
        Collections.reverse(list);
        return list.stream();
    }

    /**
     * Similar to {@link #reverse(Stream)} but for {@link IntStream}
     * 
     * @param stream
     * @return
     */
    public static IntStream reverse(IntStream stream)
    {
        return reverse(stream.mapToObj(Integer::valueOf)).mapToInt(value -> value);
    }

    /**
     * Similar to {@link #fromReaderAsLines(Reader)} using a given {@link InputStream} and a the given {@link Charset}
     * 
     * @see StandardCharsets
     * @param inputStream
     * @param charset
     * @return
     */
    public static Stream<String> fromInputStream(InputStream inputStream, Charset charset)
    {
        return fromReaderAsLines(new InputStreamReader(inputStream, charset));
    }

    /**
     * Returns a {@link Stream} of lines of the given {@link Reader}
     * 
     * @param reader
     * @return
     */
    public static Stream<String> fromReaderAsLines(Reader reader)
    {
        BufferedReader bufferedReader = new BufferedReader(reader, 128 * 1024);
        return fromSupplier(() ->
        {
            try
            {
                return bufferedReader.readLine();
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        }, line -> line == null).onClose(() ->
        {
            try
            {
                bufferedReader.close();
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        });
    }

    private static class UnaryMergeEntryImpl<E> implements UnaryMergeEntry<E>
    {
        private final MultiTypedMergeEntry entry;
        private final Class<E>             elementType;

        private UnaryMergeEntryImpl(MultiTypedMergeEntry entry, Class<E> elementType)
        {
            this.entry = entry;
            this.elementType = elementType;
        }

        @Override
        public Optional<E> getFirst()
        {
            return this.entry.getFirstAs(this.elementType);
        }

        @Override
        public Optional<E> getSecond()
        {
            return this.entry.getSecondAs(this.elementType);
        }

        @Override
        public Optional<E> getNth(int index)
        {
            return this.entry.getNthAs(this.elementType, index);
        }

        @Override
        public Optional<E> reduce(BinaryOperator<E> mergeFunction)
        {
            return this.stream()
                       .reduce(mergeFunction);
        }

        @Override
        public Iterator<E> iterator()
        {
            return this.stream()
                       .iterator();
        }

        @Override
        public int size()
        {
            return this.entry.size();
        }

        @Override
        public Stream<E> stream()
        {
            return IntStream.range(0, this.size())
                            .mapToObj(index -> this.getNth(index))
                            .filter(Optional::isPresent)
                            .map(Optional::get);
        }
    }

    private static class MultiTypedMergeEntryImpl implements MultiTypedMergeEntry
    {
        private final Object[] elements;

        private MultiTypedMergeEntryImpl(Object[] elements)
        {
            this.elements = elements;
        }

        @Override
        public <E> Optional<E> getFirstAs(Class<E> type)
        {
            return this.getNthAs(type, 0);
        }

        @Override
        public <E> Optional<E> getSecondAs(Class<E> type)
        {
            return this.getNthAs(type, 1);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E> Optional<E> getNthAs(Class<E> type, int index)
        {
            return Optional.ofNullable((E) this.elements[index]);
        }

        @Override
        public int size()
        {
            return this.elements.length;
        }
    }

    private static class OrderedStreamSynchronizer<E extends Comparable<E>> implements Function<LeftAndRight<E, E>, Stream<LeftAndRight<E, E>>>
    {
        private List<E> leftStack  = new ArrayList<>();
        private List<E> rightStack = new ArrayList<>();

        @Override
        public Stream<LeftAndRight<E, E>> apply(LeftAndRight<E, E> lar)
        {
            if (lar.hasLeft())
            {
                this.leftStack.add(lar.getLeft());
            }
            if (lar.hasRight())
            {
                this.rightStack.add(lar.getRight());
            }

            List<LeftAndRight<E, E>> results = new ArrayList<>();
            while (!this.leftStack.isEmpty() && !this.rightStack.isEmpty())
            {
                E left = ListUtils.first(this.leftStack);
                E right = ListUtils.first(this.rightStack);

                if (Objects.equals(left, right))
                {
                    results.add(new LeftAndRight<E, E>(left, right));
                    this.leftStack.remove(0);
                    this.rightStack.remove(0);
                }
                else if (left != null && right != null)
                {
                    if (left.compareTo(right) < 0)
                    {
                        results.add(new LeftAndRight<E, E>(left, null));
                        this.leftStack.remove(0);
                    }
                    else
                    {
                        results.add(new LeftAndRight<E, E>(null, right));
                        this.rightStack.remove(0);
                    }
                }
            }

            return results.stream();
        }

        public Stream<LeftAndRight<E, E>> remaining()
        {
            return Stream.of(1)
                         .flatMap(unused -> !this.leftStack.isEmpty() ? this.leftStack.stream()
                                                                                      .map(left -> new LeftAndRight<>(left, null))
                                 : this.rightStack.stream()
                                                  .map(right -> new LeftAndRight<>(null, right)));
        }
    }

    private static class IncrementalNumberSupplier implements IntSupplier
    {
        private AtomicInteger counter;
        private int           increment;

        public IncrementalNumberSupplier(int start, int increment)
        {
            super();
            this.increment = increment;
            this.counter = new AtomicInteger(start);
        }

        @Override
        public int getAsInt()
        {
            return this.counter.getAndAdd(this.increment);
        }
    }

    private static class RandomNumberSupplier implements IntSupplier
    {
        private int maxValue;
        private int minValue;

        public RandomNumberSupplier(int minValue, int maxValue)
        {
            super();
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public int getAsInt()
        {
            return RandomUtils.nextInt(0, this.maxValue - this.minValue + 1) + this.minValue;
        }
    }

    public static interface Drainage<E>
    {
        public Stream<E> getStream();

        public Stream<E> getPrefetch();

        public Stream<E> getStreamIncludingPrefetch();
    }

    public static <E> Drainage<E> drain(Stream<E> stream, Predicate<E> terminatePrefetchPredicate)
    {
        Iterator<E> iterator = stream.iterator();
        List<E> buffer = new ArrayList<>();
        AtomicBoolean terminated = new AtomicBoolean();
        Iterator<E> bufferIterator = IteratorUtils.withConsumerListener(iterator, e ->
        {
            buffer.add(e);
            if (terminatePrefetchPredicate.test(e))
            {
                terminated.set(true);
            }
        });
        Iterator<E> prefetchIterator = new Iterator<E>()
        {
            @Override
            public boolean hasNext()
            {
                return !terminated.get() && bufferIterator.hasNext();
            }

            @Override
            public E next()
            {
                return bufferIterator.next();
            }

            @Override
            public void remove()
            {
                bufferIterator.remove();
            }

        };
        return new Drainage<E>()
        {
            @Override
            public Stream<E> getStream()
            {
                return fromIterator(iterator);
            }

            @Override
            public Stream<E> getPrefetch()
            {
                return fromIterator(prefetchIterator);
            }

            @Override
            public Stream<E> getStreamIncludingPrefetch()
            {
                return Stream.concat(buffer.stream(), this.getStream());
            }
        };
    }

    public static <E> Stream<E> fromIterable(Iterable<E> iterable)
    {
        return Optional.ofNullable(iterable)
                       .map(iIterable ->
                       {
                           Supplier<Spliterator<E>> supplier = () -> iIterable.spliterator();
                           int characteristics = iIterable.spliterator()
                                                          .characteristics();
                           return StreamSupport.stream(supplier, characteristics, false);
                       })
                       .orElse(Stream.empty());
    }

    /**
     * Similar to {@link #framedPreserveSize(int, Stream)} for an {@link IntStream}
     * 
     * @see IntStream
     * @see #framedPreserveSize(int, Stream)
     * @param frameSize
     * @param stream
     * @return
     */
    public static Stream<int[]> framedPreserveSize(int frameSize, IntStream stream)
    {
        return framedPreserveSize(frameSize, stream.mapToObj(Integer::valueOf)).map(array -> ArrayUtils.toPrimitive(array));
    }

    /**
     * Similar to {@link #framed(int, Stream)} but returns a {@link List} wrapper
     * 
     * @param frameSize
     * @param stream
     * @return
     */
    public static <E> Stream<List<E>> framedAsList(int frameSize, Stream<E> stream)
    {
        return framed(frameSize, stream).map(frame -> Arrays.asList(frame));
    }

    /**
     * Similar to {@link #framedAsList(int, Stream)} but does not return any null values in the frame
     * 
     * @param frameSize
     * @param stream
     * @return
     */
    public static <E> Stream<List<E>> framedNonNullAsList(int frameSize, Stream<E> stream)
    {
        return framedAsList(frameSize, stream).map(frame -> frame.stream()
                                                                 .filter(PredicateUtils.notNull())
                                                                 .collect(Collectors.toList()));
    }

    /**
     * Creates block frames of a given size based on a given {@link Stream} of elements.<br>
     * <br>
     * E.g. [1,2,3,4,5] -> [1,2],[3,4],[5,null] for a frame size = 2
     * <br>
     * <br>
     * In comparison to {@link #framed(int, Stream)} this will always return the same frame array size also if there are not enough elements in the
     * {@link Stream} to fill the last frame.
     * 
     * @see #framedPreserveSize(int, IntStream)
     * @see #framed(int, Stream)
     * @param frameSize
     * @param stream
     * @return
     */
    public static <E> Stream<E[]> framedPreserveSize(int frameSize, Stream<E> stream)
    {
        boolean preserveTokens = true;
        return framed(frameSize, stream, preserveTokens);
    }

    /**
     * Creates block frames of a given size based on a given {@link Stream} of elements.<br>
     * <br>
     * E.g. [1,2,3,4,5] -> [1,2],[3,4],[5] for a frame size = 2
     * <br>
     * <br>
     * In comparison to {@link #framedPreserveSize(int, Stream)} this will reduce the arrays size of the last element frame, if there are not enough elements in
     * the {@link Stream} left.
     * 
     * @see #framedPreserveSize(int, IntStream)
     * @see #framedPreserveSize(int, Stream)
     * @param frameSize
     * @param stream
     * @return
     */
    public static <E> Stream<E[]> framed(int frameSize, Stream<E> stream)
    {
        boolean preserveTokens = false;
        return framed(frameSize, stream, preserveTokens);
    }

    /**
     * Similar to {@link #framed(int, Stream)} for a given {@link IntStream}
     * 
     * @param <E>
     * @param frameSize
     * @param stream
     * @return
     */
    public static <E> Stream<int[]> framed(int frameSize, IntStream stream)
    {
        return framed(frameSize, stream.boxed()).map(ArrayUtils::toPrimitive);
    }

    private static <E> Stream<E[]> framed(int frameSize, Stream<E> stream, boolean preserveTokens)
    {
        if (stream != null)
        {
            AtomicLong position = new AtomicLong();
            AtomicReference<E[]> frame = new AtomicReference<E[]>();
            AtomicInteger lastFrameIndex = new AtomicInteger();
            @SuppressWarnings("unchecked")
            Predicate<E> filter = element ->
            {
                int frameIndex = (int) (position.getAndIncrement() % frameSize);
                if (frame.get() == null && element != null)
                {
                    frame.set((E[]) Array.newInstance(element.getClass(), frameSize));
                }

                if (frame.get() != null)
                {
                    frame.get()[frameIndex] = element;
                }

                lastFrameIndex.set(frameIndex);

                boolean frameFinished = frameIndex == frameSize - 1;
                return frameFinished;
            };
            Function<E, E[]> mapper = element -> frame.getAndSet(null);
            Stream<E[]> retval = stream.filter(filter)
                                       .map(mapper);
            return Stream.concat(retval, Stream.of(1)
                                               .filter(n -> frame.get() != null)
                                               .map(n -> preserveTokens ? frame.getAndSet(null)
                                                       : ArrayUtils.subarray(frame.getAndSet(null), 0, lastFrameIndex.get() + 1)));
        }
        else
        {
            return Stream.empty();
        }
    }

    public static interface Window<E>
    {
        public List<E> getBefore();

        public List<E> getAfter();

        public E get();

        public List<E> getAll();

        public long getPosition();
    }

    public static <E> Stream<Window<E>> windowed(Stream<E> stream, int before, int after)
    {
        int step = 1;
        return windowed(stream, before, after, step);
    }

    public static <E> Stream<Window<E>> windowed(Stream<E> stream, int before, int after, int step)
    {

        CyclicBuffer<E> cyclicBuffer = new CyclicBuffer<E>(1 + before + after + 2).withSource(stream);

        return cyclicBuffer.asStream()
                           .filter(PredicateUtils.modulo(step)
                                                 .equalsZero())
                           .map(window -> new Window<E>()
                           {
                               @Override
                               public List<E> getBefore()
                               {
                                   return window.getBefore(before);
                               }

                               @Override
                               public List<E> getAfter()
                               {
                                   return window.getAfter(after);
                               }

                               @Override
                               public E get()
                               {
                                   return window.get();
                               }

                               @Override
                               public List<E> getAll()
                               {
                                   return window.getWindow(before, after);
                               }

                               @Override
                               public long getPosition()
                               {
                                   return window.getPosition();
                               }
                           });
    }

    /**
     * Merges to given {@link Stream} instances into a single {@link Stream} of {@link LeftAndRight} elements
     * 
     * @param stream1
     * @param stream2
     * @return
     */
    public static <L, R> Stream<LeftAndRight<L, R>> merge(Stream<L> stream1, Stream<R> stream2)
    {
        return StreamUtils.fromIterator(IteratorUtils.merge(stream1.iterator(), stream2.iterator()));
    }

    public static <E extends Comparable<E>> Stream<LeftAndRight<E, E>> mergeOrderedAndSynchronize(Stream<E> stream1, Stream<E> stream2)
    {
        OrderedStreamSynchronizer<E> synchronizer = new OrderedStreamSynchronizer<E>();
        return concat(merge(stream1, stream2).flatMap(synchronizer), synchronizer.remaining());
    }

    public static <L, R> Stream<BiElement<L, R>> merge2(Stream<L> stream1, Stream<R> stream2)
    {
        return merge(stream1, stream2).map(lar -> lar.asBiElement());
    }

    /**
     * Alternates elements from two {@link Stream}s into a single flat {@link Stream}: a0, b0, a1, b1, a2, b2, ...
     * Stops when the shorter stream is exhausted.
     * <br>
     * <br>
     * Example:
     * 
     * <pre>
     * StreamUtils.interleave(Stream.of(1, 2, 3), Stream.of(10, 20))
     * // → [1, 10, 2, 20]
     * </pre>
     * 
     * Null streams are treated as empty streams.
     *
     * @param streamA
     *            first source stream; may be {@code null}
     * @param streamB
     *            second source stream; may be {@code null}
     * @return a {@link Stream} alternating elements from streamA and streamB, stopping when either is exhausted
     */
    public static <E> Stream<E> interleave(Stream<E> streamA, Stream<E> streamB)
    {
        Iterator<E> iterA = Optional.ofNullable(streamA)
                                    .orElse(Stream.empty())
                                    .iterator();
        Iterator<E> iterB = Optional.ofNullable(streamB)
                                    .orElse(Stream.empty())
                                    .iterator();
        return fromIterator(new Iterator<E>()
        {
            private boolean useA = true;

            @Override
            public boolean hasNext()
            {
                return this.useA ? (iterA.hasNext() && iterB.hasNext()) : iterB.hasNext();
            }

            @Override
            public E next()
            {
                boolean currentUseA = this.useA;
                this.useA = !this.useA;
                return currentUseA ? iterA.next() : iterB.next();
            }
        });
    }

    public static interface StreamMerger
    {
        public SortedStreamMergerChooser ofSorted();
    }

    public static interface SortedStreamMergerChooser
    {
        public MultiTypedSortedStreamMergerIdentityDefiner multiTyped();

        public UnaryTypedSortedStreamMergerIdentityDefiner unary();
    }

    public static interface MultiTypedSortedStreamMergerIdentityDefiner
    {
        public <I extends Comparable<I>> MultiTypedSortedStreamMerger<I> withIdentityType(Class<I> type);
    }

    public static interface MultiTypedSortedStreamMerger<I extends Comparable<I>>
    {
        public <E> MultiTypedSortedStreamMerger<I> withSourceStream(Stream<E> stream, Function<E, I> identityFunction);

        public <E> MultiTypedSortedStreamMerger<I> withSourceStreams(List<Stream<E>> streams, Function<E, I> identityFunction);

        public Stream<MultiTypedMergeEntry> merge();
    }

    public static interface MultiTypedMergeEntry
    {
        public <E> Optional<E> getFirstAs(Class<E> type);

        public <E> Optional<E> getSecondAs(Class<E> type);

        public <E> Optional<E> getNthAs(Class<E> type, int index);

        public int size();
    }

    public static interface UnaryTypedSortedStreamMergerIdentityDefiner
    {
        public <E, I extends Comparable<I>> UnaryTypedSortedStreamMerger<E, I> withIdentityFunction(Class<E> elementType, Class<I> identityType,
                                                                                                    Function<E, I> identityFunction);
    }

    public static interface UnaryTypedSortedStreamMerger<E, I extends Comparable<I>>
    {
        public UnaryTypedSortedStreamMerger<E, I> withSourceStream(Stream<E> stream);

        public UnaryTypedSortedStreamMerger<E, I> withSourceStreams(Collection<Stream<E>> streams);

        public Stream<UnaryMergeEntry<E>> merge();

        public Stream<E> reduce(BinaryOperator<E> mergeFunction);

    }

    public static interface UnaryMergeEntry<E> extends Iterable<E>
    {
        public Optional<E> getFirst();

        public Optional<E> getSecond();

        public Optional<E> getNth(int index);

        public int size();

        public Stream<E> stream();

        public Optional<E> reduce(BinaryOperator<E> mergeFunction);
    }

    public static StreamMerger merger()
    {
        return new StreamMerger()
        {
            @Override
            public SortedStreamMergerChooser ofSorted()
            {
                return new SortedStreamMergerChooser()
                {

                    @Override
                    public UnaryTypedSortedStreamMergerIdentityDefiner unary()
                    {
                        MultiTypedSortedStreamMergerIdentityDefiner multiTypedSortedStreamMerger = this.multiTyped();
                        return new UnaryTypedSortedStreamMergerIdentityDefiner()
                        {
                            @Override
                            public <E, I extends Comparable<I>> UnaryTypedSortedStreamMerger<E, I> withIdentityFunction(Class<E> elementType,
                                                                                                                        Class<I> identityType,
                                                                                                                        Function<E, I> identityFunction)
                            {
                                return new UnaryTypedSortedStreamMerger<E, I>()
                                {
                                    private List<Stream<E>> streams = new ArrayList<>();

                                    @Override
                                    public UnaryTypedSortedStreamMerger<E, I> withSourceStream(Stream<E> stream)
                                    {
                                        return this.withSourceStreams(Arrays.asList(stream));
                                    }

                                    @Override
                                    public UnaryTypedSortedStreamMerger<E, I> withSourceStreams(Collection<Stream<E>> streams)
                                    {
                                        Optional.ofNullable(streams)
                                                .ifPresent(this.streams::addAll);
                                        return this;
                                    }

                                    @Override
                                    public Stream<UnaryMergeEntry<E>> merge()
                                    {
                                        return multiTypedSortedStreamMerger.withIdentityType(identityType)
                                                                           .withSourceStreams(this.streams, identityFunction)
                                                                           .merge()
                                                                           .map(entry -> new UnaryMergeEntryImpl<E>(entry, elementType));
                                    }

                                    @Override
                                    public Stream<E> reduce(BinaryOperator<E> mergeFunction)
                                    {
                                        // recursive build up of binary tree of merge points
                                        if (this.streams.size() > 2)
                                        {
                                            List<Stream<E>> mergedBinaryStreams = StreamUtils.framedNonNullAsList(2, this.streams.stream())
                                                                                             .map(binaryStreams -> this.applyOuterReducer(elementType,
                                                                                                                                          identityType,
                                                                                                                                          identityFunction,
                                                                                                                                          mergeFunction,
                                                                                                                                          binaryStreams))
                                                                                             .collect(Collectors.toList());
                                            return this.applyOuterReducer(elementType, identityType, identityFunction, mergeFunction, mergedBinaryStreams);
                                        }
                                        else
                                        {
                                            return this.merge()
                                                       .map(entry -> entry.reduce(mergeFunction))
                                                       .filter(Optional::isPresent)
                                                       .map(Optional::get);
                                        }
                                    }

                                    private Stream<E> applyOuterReducer(Class<E> elementType, Class<I> identityType, Function<E, I> identityFunction,
                                                                        BinaryOperator<E> mergeFunction, Collection<Stream<E>> currentStreams)
                                    {
                                        return StreamUtils.merger()
                                                          .ofSorted()
                                                          .unary()
                                                          .withIdentityFunction(elementType, identityType, identityFunction)
                                                          .withSourceStreams(currentStreams)
                                                          .reduce(mergeFunction);
                                    }
                                };
                            }
                        };
                    }

                    @Override
                    public MultiTypedSortedStreamMergerIdentityDefiner multiTyped()
                    {
                        return new MultiTypedSortedStreamMergerIdentityDefiner()
                        {
                            @Override
                            public <I extends Comparable<I>> MultiTypedSortedStreamMerger<I> withIdentityType(Class<I> type)
                            {
                                return new MultiTypedSortedStreamMerger<I>()
                                {
                                    private List<StreamAndIdentityFunction<?, I>> streams = new ArrayList<>();

                                    @Override
                                    public <E> MultiTypedSortedStreamMerger<I> withSourceStream(Stream<E> stream, Function<E, I> identityFunction)
                                    {
                                        return this.withSourceStreams(Arrays.asList(stream), identityFunction);
                                    }

                                    @Override
                                    public Stream<MultiTypedMergeEntry> merge()
                                    {
                                        List<StreamElementAndIdentitySupplier<Object, I>> streamElementSuppliers = this.createStreamElementSuppliers();

                                        return generate().intStream()
                                                         .unlimitedWithTerminationPredicate(index ->
                                                         {
                                                             boolean hasAnyElementLeft = streamElementSuppliers.stream()
                                                                                                               .anyMatch(StreamElementAndIdentitySupplier::hasNext);
                                                             return !hasAnyElementLeft;
                                                         })
                                                         .boxed()
                                                         .map(index ->
                                                         {
                                                             Optional<I> identity = streamElementSuppliers.stream()
                                                                                                          .map(StreamElementAndIdentitySupplier::getIdentity)
                                                                                                          .filter(Optional::isPresent)
                                                                                                          .map(Optional::get)
                                                                                                          .distinct()
                                                                                                          .sorted()
                                                                                                          .findFirst();

                                                             Object[] elements = streamElementSuppliers.stream()
                                                                                                       .map(supplier ->
                                                                                                       {
                                                                                                           boolean hasMatchingIdentity = Objects.equals(identity.get(),
                                                                                                                                                        supplier.getIdentity()
                                                                                                                                                                .orElse(null));
                                                                                                           return hasMatchingIdentity ? supplier.removeHead()
                                                                                                                   : null;
                                                                                                       })
                                                                                                       .toArray(size -> new Object[size]);
                                                             return elements;
                                                         })
                                                         .map(MultiTypedMergeEntryImpl::new);

                                    }

                                    @SuppressWarnings("unchecked")
                                    private List<StreamElementAndIdentitySupplier<Object, I>> createStreamElementSuppliers()
                                    {
                                        return this.streams.stream()
                                                           .map(streamAndIdentityFunction -> new StreamElementAndIdentitySupplier<Object, I>((Iterator<Object>) streamAndIdentityFunction.getStream()
                                                                                                                                                                                         .iterator(),
                                                                                                                                             (Function<Object, I>) streamAndIdentityFunction.getIdentityFunction()))
                                                           .collect(Collectors.toList());
                                    }

                                    @Override
                                    public <E> MultiTypedSortedStreamMerger<I> withSourceStreams(List<Stream<E>> streams, Function<E, I> identityFunction)
                                    {
                                        Optional.ofNullable(streams)
                                                .orElse(Collections.emptyList())
                                                .forEach(stream -> this.streams.add(new StreamAndIdentityFunction<>(stream, identityFunction)));
                                        return this;
                                    }
                                };
                            }
                        };
                    }
                };
            }
        };
    }

    private static class StreamElementAndIdentitySupplier<E, I> implements Supplier<E>
    {
        private CachedElement<E> cachedElement;
        private Function<E, I>   identityFunction;

        public StreamElementAndIdentitySupplier(Iterator<E> iterator, Function<E, I> identityFunction)
        {
            super();
            this.identityFunction = identityFunction;
            this.cachedElement = CachedElement.of(() -> iterator.hasNext() ? iterator.next() : null);
        }

        public Optional<I> getIdentity()
        {
            return Optional.ofNullable(this.get())
                           .map(this.identityFunction);
        }

        public boolean hasNext()
        {
            return this.cachedElement.get() != null;
        }

        @Override
        public E get()
        {
            return this.cachedElement.get();
        }

        public E removeHead()
        {
            return this.cachedElement.getAndReset();
        }

    }

    private static class StreamAndIdentityFunction<E, I>
    {
        private Stream<E>      stream;
        private Function<E, I> identityFunction;

        public StreamAndIdentityFunction(Stream<E> stream, Function<E, I> identityFunction)
        {
            super();
            this.stream = stream;
            this.identityFunction = identityFunction;
        }

        public Stream<E> getStream()
        {
            return this.stream;
        }

        public Function<E, I> getIdentityFunction()
        {
            return this.identityFunction;
        }

    }

    /**
     * Similar to {@link #withIntCounter(Stream, int)} with seed = 0
     * 
     * @param stream
     * @return
     */
    public static <E> Stream<BiElement<E, Integer>> withIntCounter(Stream<E> stream)
    {
        int seed = 0;
        return withIntCounter(stream, seed);
    }

    /**
     * Returns a {@link Stream} with {@link BiElement} based on the elements of the given {@link Stream} and a counter starting with the given seed value.
     * 
     * @param stream
     * @param seed
     * @return
     */
    public static <E> Stream<BiElement<E, Integer>> withIntCounter(Stream<E> stream, int seed)
    {
        AtomicInteger counter = new AtomicInteger(seed);
        return stream.map(element -> BiElement.of(element, counter.getAndIncrement()));
    }

    /**
     * Pairs each element of the given {@link Stream} with its 0-based position index,
     * returning a {@link Stream} of {@link BiElement} where the first value is the index ({@link Long})
     * and the second value is the element.
     * <br>
     * <br>
     * Example:
     * 
     * <pre>
     * StreamUtils.enumerate(Stream.of("a", "b", "c"))
     * // → BiElement(0, "a"), BiElement(1, "b"), BiElement(2, "c")
     * </pre>
     * 
     * A {@code null} stream is treated as an empty stream.
     *
     * @param stream
     *            source stream; may be {@code null}
     * @return a {@link Stream} of index-element pairs in encounter order
     */
    public static <E> Stream<BiElement<Long, E>> enumerate(Stream<E> stream)
    {
        AtomicLong counter = new AtomicLong(0L);
        return Optional.ofNullable(stream)
                       .orElse(Stream.empty())
                       .map(element -> BiElement.of(counter.getAndIncrement(), element));
    }

    public static <E> Stream<List<E>> chop(Stream<E> stream, Predicate<E> chopStartMatcher)
    {
        AtomicReference<List<E>> chunk = new AtomicReference<>();
        return Stream.concat(stream.peek(element -> chunk.getAndUpdate(elements -> ListUtils.addTo(elements, element)))
                                   .filter(chopStartMatcher)
                                   .map((Function<E, List<E>>) element -> ListUtils.modified(chunk.getAndSet(ListUtils.of(ListUtils.last(chunk.get()))),
                                                                                             list -> list.subList(0, list.size() - 1))),
                             Stream.of(1)
                                   .flatMap(i -> chunk.get() != null ? Stream.of(chunk.get()) : Stream.empty()));
    }

    /**
     * Returns a {@link Stream} which contains the elements of the {@link Stream}s generated by the given {@link Supplier}
     * <br>
     * <br>
     * Terminates if the given {@link Supplier} returns null the first time
     * 
     * @param supplier
     * @return
     */
    public static <E> Stream<E> fromStreamSupplier(Supplier<Stream<E>> supplier)
    {
        return fromSupplier(supplier, s -> s == null).flatMap(stream -> stream != null ? stream : Stream.empty());
    }

    /**
     * Returns a {@link Stream} based on a concatenation of the given {@link Supplier}s of {@link Stream} instances. <br>
     * The {@link Supplier#get()} method is only called once per {@link Supplier} instance.<br>
     * <br>
     * For {@link Supplier} which return multiple different instances of a {@link Stream} please see {@link #fromSupplier(Supplier)}.
     * 
     * @param streamSuppliers
     * @return
     */
    @SafeVarargs
    public static <E> Stream<E> fromStreams(Supplier<Stream<? extends E>>... streamSuppliers)
    {
        return Arrays.asList(streamSuppliers)
                     .stream()
                     .flatMap(supplier -> supplier.get());
    }

    /**
     * Returns a {@link Stream} which removes elements from the given {@link Collection} one by one
     * 
     * @param collection
     * @return
     */
    public static <E> Stream<E> removeStream(Collection<E> collection)
    {
        return fromIterator(IteratorUtils.removeIterator(collection));
    }

    private static class RoutedCollector<E> implements Predicate<E>, Function<E, Stream<E>>
    {
        private List<E>      tokens      = null;
        private List<E>      readyTokens = null;
        private Predicate<E> matcher;
        private boolean      first       = true;

        public RoutedCollector(Predicate<E> matcher)
        {
            super();
            this.matcher = matcher;
        }

        @Override
        public boolean test(E t)
        {
            boolean matches = this.matcher.test(t);
            boolean test = !this.first && matches;

            //
            if (matches)
            {
                this.first = false;

                this.readyTokens = this.tokens;
                this.tokens = null;
            }

            //
            if (this.tokens == null)
            {
                this.tokens = new ArrayList<>();
            }
            this.tokens.add(t);

            //
            return test;
        }

        @Override
        public Stream<E> apply(E t)
        {
            Stream<E> stream = this.readyTokens.stream();
            this.readyTokens = null;
            return stream;
        }

        public Stream<Stream<E>> getUnreturned()
        {
            return this.tokens == null ? Stream.empty() : Stream.of(this.tokens.stream());
        }
    }

    public static <E> Stream<Stream<E>> routeByMatch(Stream<E> stream, Predicate<E> matcher)
    {
        RoutedCollector<E> collector = new RoutedCollector<>(matcher);
        return Stream.concat(stream.filter(collector)
                                   .map(collector),
                             Stream.of(1)
                                   .flatMap(i -> collector.getUnreturned()));
    }

    /**
     * Returns the last element of a {@link Stream}
     * 
     * @param stream
     * @return
     */
    public static <E> E last(Stream<E> stream)
    {
        E retval = null;

        for (E element : IterableUtils.from(stream))
        {
            retval = element;
        }

        return retval;
    }

    public static class Parallelism
    {
        private int numberOfThreads;

        public Parallelism()
        {
            super();
            this.withNumberOfThreadsPerCPUCore(4);
        }

        public int getNumberOfThreads()
        {
            return this.numberOfThreads;
        }

        public Parallelism withNumberOfThreads(int numberOfThreads)
        {
            this.numberOfThreads = numberOfThreads;
            return this;
        }

        public Parallelism withNumberOfThreadsPerCPUCore(double numberOfThreadsPerCPUCore)
        {
            this.numberOfThreads = ExecutorUtils.calculateNumberOfThreadsByPerCPU(numberOfThreadsPerCPUCore);
            return this;
        }

        @Override
        public String toString()
        {
            return "Parallelism [numberOfThreads=" + this.numberOfThreads + "]";
        }

    }

    public static <T, R> Stream<R> parallel(Stream<T> stream, Function<T, R> mappingFunction)
    {
        return parallel(stream, mappingFunction, new Parallelism());
    }

    public static <T, R> Stream<R> parallel(Stream<T> stream, Function<T, R> mappingFunction, Parallelism parallelism)
    {
        ParallelExecution parallelExecution = ExecutorUtils.parallel()
                                                           .withNumberOfThreads(parallelism.getNumberOfThreads());

        return framedAsList(parallelism.getNumberOfThreads(), stream).flatMap(elements -> parallelExecution.executeTasks(elements.stream()
                                                                                                                                 .map(element -> new Callable<R>()
                                                                                                                                 {
                                                                                                                                     @Override
                                                                                                                                     public R call()
                                                                                                                                             throws Exception
                                                                                                                                     {
                                                                                                                                         return mappingFunction.apply(element);
                                                                                                                                     }
                                                                                                                                 }))
                                                                                                           .get());

    }

    public static interface StreamBuilder
    {
        public <E> TypedStreamBuilder<E> add(E element);

        @SuppressWarnings("unchecked")
        public <E> TypedStreamBuilder<E> addAll(E... elements);

        public <E> TypedStreamBuilder<E> addAll(Collection<E> elements);

        public <E> TypedStreamBuilder<E> addAll(Stream<E> elements);

        public <E> Stream<E> build();
    }

    public static interface TypedStreamBuilder<E>
    {

        public TypedStreamBuilder<E> add(E element);

        @SuppressWarnings("unchecked")
        public TypedStreamBuilder<E> addAll(E... elements);

        public TypedStreamBuilder<E> addAll(Collection<E> elements);

        public TypedStreamBuilder<E> addAll(Stream<E> elements);

        public Stream<E> build();

    }

    private static class TypedStreamBuilderImpl<E> implements TypedStreamBuilder<E>
    {
        private List<Stream<E>> streams = new ArrayList<>();

        @Override
        public Stream<E> build()
        {
            return this.streams.stream()
                               .flatMap(s -> s);
        }

        @Override
        public TypedStreamBuilder<E> add(E element)
        {
            this.streams.add(Stream.of(element));
            return this;
        }

        @Override
        public TypedStreamBuilder<E> addAll(Collection<E> elements)
        {
            return this.addAll(elements.stream());
        }

        @Override
        public TypedStreamBuilder<E> addAll(Stream<E> elements)
        {
            this.streams.add(elements);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TypedStreamBuilder<E> addAll(E... elements)
        {
            return this.addAll(Arrays.asList(elements));
        }

    }

    public static StreamBuilder builder()
    {
        return new StreamBuilder()
        {
            @Override
            public <E> Stream<E> build()
            {
                return Stream.empty();
            }

            @Override
            public <E> TypedStreamBuilder<E> addAll(Stream<E> elements)
            {
                return new TypedStreamBuilderImpl<E>().addAll(elements);
            }

            @Override
            public <E> TypedStreamBuilder<E> addAll(Collection<E> elements)
            {
                return new TypedStreamBuilderImpl<E>().addAll(elements);
            }

            @Override
            public <E> TypedStreamBuilder<E> add(E element)
            {
                return new TypedStreamBuilderImpl<E>().add(element);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <E> TypedStreamBuilder<E> addAll(E... elements)
            {
                return new TypedStreamBuilderImpl<E>().addAll(elements);
            }
        };
    }

    public static interface StreamGenerator
    {
        public IntStreamGenerator intStream();

        public <E, S extends E> Stream<E> recursive(S startElement, UnaryOperator<E> function);

        public <E, R> Stream<R> recursive(E startElement, Function<E, R> mapper, Function<R, E> nextElementFunction);

        BiIntStreamGenerator biIntStream();
    }

    public static interface IntStreamConfigurator
    {
        public IntStreamConfigurator withIncrement(int increment);

        public IntStream fromZero();

        public IntStream from(int start);

        public IntStream fromOne();

        public IntStream withRandomNumbers(int maxValue);

        public IntStream withRandomNumbers(int minValue, int maxValue);

        public IntStream until(IntPredicate terminationPredicate);

    }

    public static interface LimitedIntStreamConfigurator
    {
        public IntStreamConfigurator withTerminationPredicate(IntPredicate terminationPredicate);

        public IntStreamConfigurator withMaxExclusive(int max);

        public IntStreamConfigurator withMaxInclusive(int max);
    }

    public static interface IntStreamGenerator
    {

        /**
         * Generates an {@link IntStream} based on the given {@link Options}
         * 
         * @param options
         * @return
         */
        public IntStream with(Options options);

        /**
         * Generates an unlimited {@link IntStream} with an given increment
         * 
         * @param start
         * @param increment
         * @return
         */
        public IntStream unlimited(int start, int increment);

        /**
         * Similar to {@link #unlimited(int, int)} with a start of 0
         * 
         * @param increment
         * @return
         */
        public IntStream unlimited(int increment);

        /**
         * Returns an {@link IntStreamConfigurator} instance with an unlimited underlying {@link IntStream}
         * 
         * @see #limited()
         * @return
         */
        public IntStreamConfigurator unlimited();

        /**
         * Returns an {@link LimitedIntStreamConfigurator} instance
         * 
         * @see #unlimited()
         * @return
         */
        public LimitedIntStreamConfigurator limited();

        /**
         * Similar to {@link #unlimited()} but allows to provide a termination {@link Predicate} which should return true if the {@link Stream}
         * should terminate.
         * 
         * @param terminationPredicate
         * @return
         */
        public IntStream unlimitedWithTerminationPredicate(Predicate<Integer> terminationPredicate);

        public static class Options
        {
            private int                start                = 0;
            private int                increment            = 1;
            private Predicate<Integer> terminationPredicate = null;

            public Options withStart(int start)
            {
                this.start = start;
                return this;
            }

            public Options withIncrement(int increment)
            {
                this.increment = increment;
                return this;
            }

            public Options withEndInclusive(int end)
            {
                this.terminationPredicate = ii -> ii > end;
                return this;
            }

            public Options withEndExclusive(int end)
            {
                this.terminationPredicate = ii -> ii >= end;
                return this;
            }

            public Options withTerminationPredicate(Predicate<Integer> terminationPredicate)
            {
                this.terminationPredicate = terminationPredicate;
                return this;
            }

            @Override
            public String toString()
            {
                return "Options [start=" + this.start + ", increment=" + this.increment + ", terminationPredicate=" + this.terminationPredicate + "]";
            }

            public int getStart()
            {
                return this.start;
            }

            public int getIncrement()
            {
                return this.increment;
            }

            public Predicate<Integer> getTerminationPredicate()
            {
                return this.terminationPredicate;
            }

        }
    }

    protected static class AbstractIntStreamConfigurator implements IntStreamConfigurator
    {
        private int                                        increment      = 1;
        private int                                        start          = 0;
        private Supplier<IntSupplier>                      numberSupplier = () -> new IncrementalNumberSupplier(this.start, this.increment);
        private Function<Supplier<IntSupplier>, IntStream> intStreamGenerator;

        public AbstractIntStreamConfigurator(Function<Supplier<IntSupplier>, IntStream> intStreamGenerator)
        {
            super();
            this.intStreamGenerator = intStreamGenerator;
        }

        @Override
        public IntStream withRandomNumbers(int maxValue)
        {
            int minValue = 0;
            return this.withRandomNumbers(minValue, maxValue);
        }

        @Override
        public IntStream withRandomNumbers(int minValue, int maxValue)
        {
            this.numberSupplier = () -> new RandomNumberSupplier(minValue, maxValue);
            return IntStream.generate(this.numberSupplier.get());
        }

        @Override
        public IntStream until(IntPredicate terminationPredicate)
        {
            AtomicInteger counter = new AtomicInteger(this.start);
            return StreamUtils.fromSupplier(() -> counter.getAndAdd(this.increment), count -> terminationPredicate.test(count))
                              .mapToInt(MapperUtils.identitiyForIntegerAsUnboxed());
        }

        @Override
        public IntStreamConfigurator withIncrement(int increment)
        {
            this.increment = increment;
            return this;
        }

        @Override
        public IntStream fromZero()
        {
            return this.from(0);
        }

        @Override
        public IntStream from(int start)
        {
            this.start = start;
            return this.intStreamGenerator.apply(this.numberSupplier);
        }

        @Override
        public IntStream fromOne()
        {
            return this.from(1);
        }
    }

    public static interface BiIntStreamGenerator
    {
        public LeftSidedBiIntStreamGenerator withLeftSide(int start, int endExclusive);
    }

    public static interface LeftSidedBiIntStreamGenerator
    {
        public Stream<IntUnaryBiElement> withRightSide(int start, int endExclusive);
    }

    public static StreamGenerator generate()
    {
        return new StreamGenerator()
        {

            @Override
            public BiIntStreamGenerator biIntStream()
            {
                return new BiIntStreamGenerator()
                {

                    @Override
                    public LeftSidedBiIntStreamGenerator withLeftSide(int leftSideStartInclusive, int leftSideEndExclusive)
                    {
                        return new LeftSidedBiIntStreamGenerator()
                        {
                            @Override
                            public Stream<IntUnaryBiElement> withRightSide(int rightSideStartInclusive, int rightSideEndExclusive)
                            {
                                return IntStream.range(leftSideStartInclusive, leftSideEndExclusive)
                                                .boxed()
                                                .flatMap(leftSide -> IntStream.range(rightSideStartInclusive, rightSideEndExclusive)
                                                                              .mapToObj(rightSide -> IntUnaryBiElement.of(leftSide, rightSide)));
                            }
                        };
                    }
                };
            }

            @Override
            public IntStreamGenerator intStream()
            {
                return new IntStreamGenerator()
                {
                    @Override
                    public IntStream with(Options options)
                    {
                        AtomicInteger counter = new AtomicInteger(options.getStart());
                        return StreamUtils.fromSupplier(() -> counter.getAndAdd(options.getIncrement()), options.getTerminationPredicate())
                                          .mapToInt(MapperUtils.identitiyForIntegerAsUnboxed());
                    }

                    @Override
                    public IntStream unlimitedWithTerminationPredicate(Predicate<Integer> terminationPredicate)
                    {
                        return this.with(new Options().withTerminationPredicate(terminationPredicate));
                    }

                    @Override
                    public IntStream unlimited(int start, int increment)
                    {
                        AtomicInteger counter = new AtomicInteger(start);
                        return IntStream.generate(() -> counter.getAndAdd(increment));
                    }

                    @Override
                    public IntStream unlimited(int increment)
                    {
                        return this.unlimited(0, increment);
                    }

                    @Override
                    public LimitedIntStreamConfigurator limited()
                    {
                        return new LimitedIntStreamConfigurator()
                        {
                            @Override
                            public IntStreamConfigurator withTerminationPredicate(IntPredicate terminationPredicate)
                            {
                                return new AbstractIntStreamConfigurator(ns -> StreamUtils.fromIntSupplier(ns.get(), terminationPredicate));
                            }

                            @Override
                            public IntStreamConfigurator withMaxInclusive(int max)
                            {
                                return this.withTerminationPredicate(value -> value > max);
                            }

                            @Override
                            public IntStreamConfigurator withMaxExclusive(int max)
                            {
                                return this.withTerminationPredicate(value -> value >= max);
                            }
                        };
                    }

                    @Override
                    public IntStreamConfigurator unlimited()
                    {
                        return new AbstractIntStreamConfigurator(ns -> IntStream.generate(ns.get()));
                    }

                };
            }

            @Override
            public <E, R> Stream<R> recursive(E startElement, Function<E, R> mapper, Function<R, E> nextElementFunction)
            {
                return StreamUtils.fromSupplier(new Supplier<R>()
                {
                    private AtomicReference<E> element = new AtomicReference<>(startElement);

                    @Override
                    public R get()
                    {
                        try
                        {
                            CachedFunction<E, R> cachedMapper = CachedFunction.of(mapper, new HashMap<>());
                            return cachedMapper.apply(this.element.getAndUpdate(currentValue -> Optional.ofNullable(cachedMapper.apply(currentValue))
                                                                                                        .map(mappedValue -> nextElementFunction.apply(mappedValue))
                                                                                                        .orElse(null)));
                        }
                        catch (Exception e)
                        {
                            throw new IllegalStateException("Excpetion for current element: " + this.element, e);
                        }
                    }
                }, PredicateUtils.isNull());
            }

            @Override
            public <E, S extends E> Stream<E> recursive(S startElement, UnaryOperator<E> function)
            {
                return StreamUtils.fromSupplier(new Supplier<E>()
                {
                    private E element = startElement;

                    @Override
                    public E get()
                    {
                        E result = this.element;

                        this.element = function.apply(this.element);

                        return result;
                    }
                }, PredicateUtils.isNull());
            }

        };
    }

    public static <E> Stream<E> withFilterAllOnAnyFilterFails(Stream<E> stream)
    {
        return new FilterAllOnFirstFilterFailStreamDecorator<>(stream);
    }

    @SafeVarargs
    public static <E> Stream<E> fromArray(E... elements)
    {
        return Optional.ofNullable(elements)
                       .map(iElements -> Stream.of(iElements))
                       .orElse(Stream.empty());
    }

    public static <E> Consumer<E> peekProgressCounter(int modulo, long maximum, DurationProgressConsumer durationProgressConsumer)
    {
        DurationProgressCounter progressCounter = Counter.fromZero()
                                                         .asDurationProgressCounter()
                                                         .withMaximum(maximum);
        return element -> progressCounter.increment()
                                         .ifModulo(modulo, durationProgressConsumer);
    }

    /**
     * Similar to {@link #aggregate(Stream, Predicate, Predicate, Function)} but without an explicit end barrier matcher
     * 
     * @param stream
     * @param startBarrierMatcher
     * @param aggregationFunction
     * @return
     */
    public static <E, A> Stream<A> aggregateByStart(Stream<E> stream, Predicate<E> startBarrierMatcher, Function<Stream<E>, Stream<A>> aggregationFunction)
    {
        Predicate<E> endBarrierMatcher = e -> false;
        return aggregate(stream, startBarrierMatcher, endBarrierMatcher, aggregationFunction);
    }

    /**
     * Allows to aggregate groups of elements which are identified by a start and end barrier {@link Predicate} matcher
     * 
     * @param stream
     * @param startBarrierMatcher
     * @param endBarrierMatcher
     * @param aggregationFunction
     * @return
     */
    public static <E, A> Stream<A> aggregate(Stream<E> stream, Predicate<E> startBarrierMatcher, Predicate<E> endBarrierMatcher,
                                             Function<Stream<E>, Stream<A>> aggregationFunction)
    {
        return Optional.ofNullable(stream)
                       .map(s ->
                       {
                           AtomicReference<List<E>> currentFrame = new AtomicReference<>();
                           AtomicReference<List<E>> completeFrame = new AtomicReference<>();

                           Predicate<E> filter = element ->
                           {
                               boolean startBarrierReached = startBarrierMatcher.test(element);
                               boolean endBarrierReached = endBarrierMatcher.test(element);
                               boolean activeFrame = currentFrame.get() != null;
                               boolean ejectFrameByNewStart = startBarrierReached && activeFrame;
                               boolean ejectFrame = endBarrierReached || ejectFrameByNewStart;

                               if (ejectFrame && ejectFrameByNewStart)
                               {
                                   completeFrame.set(currentFrame.getAndSet(null));
                               }

                               if (startBarrierReached)
                               {
                                   currentFrame.set(new ArrayList<>());
                               }

                               Optional.ofNullable(currentFrame.get())
                                       .ifPresent(frame -> frame.add(element));

                               if (ejectFrame && !ejectFrameByNewStart)
                               {
                                   completeFrame.set(currentFrame.getAndSet(null));
                               }

                               return ejectFrame;
                           };
                           Function<E, Stream<E>> completeFrameMapper = element -> Optional.ofNullable(completeFrame.getAndSet(null))
                                                                                           .map(List::stream)
                                                                                           .orElse(Stream.empty());
                           Function<E, Stream<E>> currentFrameMapper = element -> Optional.ofNullable(currentFrame.getAndSet(null))
                                                                                          .map(List::stream)
                                                                                          .orElse(Stream.empty());

                           Stream<A> retval = s.sequential()
                                               .filter(filter)
                                               .map(completeFrameMapper)
                                               .flatMap(aggregationFunction);
                           return Stream.concat(retval, Stream.of((E) null)
                                                              .filter(e -> currentFrame.get() != null)
                                                              .map(currentFrameMapper)
                                                              .flatMap(aggregationFunction));
                       })
                       .orElse(Stream.empty());
    }

    /**
     * Operates with all mappers on the same incoming element and returns the aggregated result of each mapping operation as {@link Stream}<br>
     * <br>
     * This is helpful if one and the same element in a {@link Stream} should be processed multiple times with different operations and all the results should
     * be combined into the orginal {@link Stream}.<br>
     * <br>
     * 
     * <pre>
     * assertEquals(Arrays.asList("a", "ab"), Stream.of("a")
     *                                              .flatMap(StreamUtils.redundant(element -> element, element -> element + "b"))
     *                                              .collect(Collectors.toList()));
     * </pre>
     * 
     * @param mappers
     * @return
     */
    @SafeVarargs
    public static <E, R> Function<E, Stream<R>> redundant(Function<E, R>... mappers)
    {
        return element -> Optional.ofNullable(mappers)
                                  .map(Arrays::asList)
                                  .map(List::stream)
                                  .orElse(Stream.empty())
                                  .map(mapper -> mapper.apply(element));
    }

    /**
     * Similar to {@link #redundant(Function...)} but allows each mapper to return a {@link Stream}
     * 
     * @param mappers
     * @return
     */
    @SafeVarargs
    public static <E, R> Function<E, Stream<R>> redundantFlattener(Function<E, Stream<R>>... mappers)
    {
        return element -> Optional.ofNullable(mappers)
                                  .map(Arrays::asList)
                                  .map(List::stream)
                                  .orElse(Stream.empty())
                                  .flatMap(mapper -> mapper.apply(element));
    }

    /**
     * Similar to {@link #splitAtIndex(Stream, int)} but splits the first element from the given {@link Stream}
     * 
     * @param stream
     * @return
     */
    public static <E> BiElement<Optional<E>, Stream<E>> splitOne(Stream<E> stream)
    {
        return splitAtIndex(stream, 1).applyToFirstArgument(s -> s.findFirst());
    }

    /**
     * Splits the {@link Stream} into a {@link Stream} until the given index (exclusive) and a {@link Stream} starting at the given index from the original
     * {@link Stream}
     * 
     * @param stream
     * @param index
     * @return
     */
    public static <E> BiElement<Stream<E>, Stream<E>> splitAtIndex(Stream<E> stream, int index)
    {
        Iterator<E> iterator = Optional.ofNullable(stream)
                                       .orElse(Stream.empty())
                                       .iterator();
        return BiElement.of(fromIterator(iterator).limit(index), fromIterator(iterator));
    }

    /**
     * Splits a given {@link Stream} into two {@link Stream}s based on the result of the given inclusion filter predicate.
     * 
     * @param stream
     * @param inclusionFilter
     * @return
     */
    public static <E> SplittedStream<E> splitByFilter(Stream<E> stream, Predicate<E> inclusionFilter)
    {
        Iterator<E> iterator = Optional.ofNullable(stream)
                                       .orElse(Stream.empty())
                                       .iterator();
        return new SplittedStream<E>()
        {
            private List<E> includedStack = new ArrayList<>();
            private List<E> excludedStack = new ArrayList<>();

            @Override
            public Stream<E> included()
            {
                return Stream.concat(fromIterator(iterator).filter(PredicateUtils.consumeExcluded(inclusionFilter, element -> this.excludedStack.add(element))),
                                     this.includedStack.stream());
            }

            @Override
            public Stream<E> excluded()
            {
                return Stream.concat(fromIterator(iterator).filter(PredicateUtils.consumeExcluded(inclusionFilter.negate(),
                                                                                                  element -> this.includedStack.add(element))),
                                     this.excludedStack.stream());
            }
        };
    }

    public static interface SplittedStream<E>
    {
        public Stream<E> included();

        public Stream<E> excluded();
    }

    /**
     * Filters out elements of the given {@link Stream} matched by the given {@link Predicate} and applies them to the given {@link Consumer}.
     * 
     * @param stream
     * @param filterAndConsumer
     * @return
     */
    public static <E> Stream<E> filterAndConsume(Stream<E> stream, Predicate<E> filter, Consumer<E> consumer)
    {
        return filterAndConsume(stream, PredicateConsumer.of(filter, consumer));
    }

    /**
     * Similar to {@link #filterAndConsume(Stream, Predicate, Consumer)} but using a {@link PredicateConsumer}
     * 
     * @param stream
     * @param filterAndConsumer
     * @return
     */
    public static <E> Stream<E> filterAndConsume(Stream<E> stream, PredicateConsumer<E> filterAndConsumer)
    {
        return Optional.ofNullable(stream)
                       .orElse(Stream.empty())
                       .filter(filterConsumer(filterAndConsumer));
    }

    /**
     * @see #filterAndConsume(Stream, Predicate, Consumer)
     * @param filter
     * @param consumer
     * @return
     */
    public static <E> Predicate<E> filterConsumer(Predicate<E> filter, Consumer<E> consumer)
    {
        return filterConsumer(PredicateConsumer.of(filter, consumer));
    }

    /**
     * @see #filterAndConsume(Stream, PredicateConsumer)
     * @param filterAndConsumer
     * @return
     */
    public static <E> Predicate<E> filterConsumer(PredicateConsumer<E> filterAndConsumer)
    {
        return element ->
        {
            boolean matched = filterAndConsumer.test(element);
            if (matched)
            {
                filterAndConsumer.accept(element);
            }
            return !matched;
        };
    }

    /**
     * Returns a stateful {@link Predicate} that passes only the first element seen for each key
     * computed by the given key extractor. Subsequent elements whose key was already seen are rejected.
     * Suitable for use with {@link Stream#filter(Predicate)} to deduplicate a stream by a custom key
     * while preserving encounter order.
     * <br>
     * <br>
     * Example:
     * 
     * <pre>
     * Stream.of("apple", "ant", "banana", "avocado")
     *       .filter(StreamUtils.distinctBy(s -&gt; s.charAt(0)))
     * // → "apple", "banana"
     * </pre>
     * 
     * Note: The returned {@link Predicate} is stateful and not thread-safe. Create a fresh instance
     * for each pipeline.
     *
     * @param keyExtractor
     *            function that derives the deduplication key from each element
     * @return a stateful {@link Predicate} that returns {@code true} only for the first element per key
     */
    public static <E, K> Predicate<E> distinctBy(Function<E, K> keyExtractor)
    {
        java.util.Set<K> seen = new java.util.HashSet<>();
        return element -> seen.add(keyExtractor.apply(element));
    }

    /**
     * Returns a {@link Stream} containing only the first element per key value as computed by
     * the given key extractor, preserving encounter order. Analogous to {@link Stream#distinct()}
     * but using a custom key instead of element equality.
     * <br>
     * <br>
     * A {@code null} stream is treated as an empty stream.
     *
     * @param stream
     *            source stream; may be {@code null}
     * @param keyExtractor
     *            function that derives the deduplication key from each element
     * @return a new {@link Stream} with at most one element per distinct key
     */
    public static <E, K> Stream<E> distinctBy(Stream<E> stream, Function<E, K> keyExtractor)
    {
        return Optional.ofNullable(stream)
                       .orElse(Stream.empty())
                       .filter(distinctBy(keyExtractor));
    }

    /**
     * Recursively flattens the given {@link Stream} using the flattening mapper. The result {@link Stream} will contain the original elements but also the
     * elements created by the flattener function and this recursively until the flattener function does return an empty {@link Stream}.
     *
     * @param stream
     * @param flattener
     * @return
     */
    public static <E> Stream<E> recursiveFlattened(Stream<E> stream, Function<E, Stream<E>> flattener)
    {
        return Optional.ofNullable(stream)
                       .orElse(Stream.empty())
                       .flatMap(createRecursiveFlattener(flattener));
    }

    public static <E> Function<E, Stream<E>> createRecursiveFlattener(Function<E, Stream<E>> flattener)
    {
        return element -> Stream.concat(Stream.of(element), flattener.apply(element)
                                                                     .flatMap(createRecursiveFlattener(flattener)));
    }

    /**
     * Creates a {@link FilterMapper} based on the given {@link Predicate} and {@link Function}
     * 
     * @param filter
     * @param mapper
     * @return
     */
    public static <E, R> FilterMapper<E, R> filterMapper(Predicate<E> filter, Function<E, R> mapper)
    {
        return new FilterMapper<E, R>()
        {
            @Override
            public boolean test(E t)
            {
                return filter.test(t);
            }

            @Override
            public R apply(E t)
            {
                return mapper.apply(t);
            }
        };

    }

    public static <E1, E2> Stream<BiElement<E1, E2>> cartesianProductOf(Stream<E1> streamLeft, Stream<E2> streamRight)
    {
        List<E2> rightElements = Optional.ofNullable(streamRight)
                                         .orElse(Stream.empty())
                                         .collect(Collectors.toList());
        return Optional.ofNullable(streamLeft)
                       .orElse(Stream.empty())
                       .flatMap(left -> rightElements.stream()
                                                     .map(right -> BiElement.of(left, right)));
    }

    /**
     * Ensures that the returned {@link Stream} contains all the elements of the given {@link Stream} but at least the given number of elements. If the original
     * {@link Stream} does not provide enough element the given {@link IntFunction} is used as element factory.
     * 
     * @param stream
     * @param numberOfElements
     * @param elementFactory
     * @return
     */
    public static <E> Stream<E> ensureNumberOfElements(Stream<E> stream, int numberOfElements, IntFunction<E> elementFactory)
    {
        AtomicInteger counter = new AtomicInteger();
        Stream<E> secondStream = Stream.of(1)
                                       .flatMap(dummy -> IntStream.range(0, Math.max(0, numberOfElements - counter.get()))
                                                                  .mapToObj(elementFactory));
        return Stream.concat(stream.peek(element -> counter.incrementAndGet()), secondStream);
    }

    /**
     * Creates a {@link Stream} that returns elements as long as the given {@link Supplier} returns {@link Optional}s that are not {@link Optional#empty()}.
     * 
     * @param supplier
     * @return
     */
    public static <E> Stream<E> takeOptionalUntilEmpty(Supplier<Optional<E>> supplier)
    {
        AtomicBoolean terminate = new AtomicBoolean(false);
        return generate().intStream()
                         .unlimitedWithTerminationPredicate(element -> terminate.get())
                         .mapToObj(index -> supplier.get())
                         .peek(optional -> terminate.set(!optional.isPresent()))
                         .filter(Optional::isPresent)
                         .map(Optional::get);
    }

    /**
     * Limits the given {@link Stream} until the last element has been included.
     * <br>
     * <br>
     * Synonym of {@link #takeUntilInclusive(Stream, Predicate)}.
     *
     * @see #takeUntilInclusive(Stream, Predicate)
     * @see #takeUntilExclusive(Stream, Predicate)
     * @param <E>
     * @param stream
     * @param lastElementPredicate
     * @return
     */
    public static <E> Stream<E> takeUntilLastElementIncluded(Stream<E> stream, Predicate<E> lastElementPredicate)
    {
        return takeUntilInclusive(stream, lastElementPredicate);
    }

    /**
     * Limits the given {@link Stream} to the elements up to and <b>including</b> the first element matching the given {@link Predicate}. If no element matches,
     * all elements are returned.
     * <br>
     * <br>
     * The returned {@link Stream} is inherently sequential, since the termination decision depends on the encounter order. It stays correct even if the caller
     * invokes {@link Stream#parallel()} on it, but such a call will not result in any parallel execution of the operations before a further splitting
     * intermediate operation.
     * <br>
     * <br>
     * The given {@link Predicate} is invoked at most once per element and never for elements after the terminating one. The source {@link Stream} is not
     * advanced beyond the terminating element, which allows expensive or side effecting sources to be used.
     *
     * @see #takeUntilExclusive(Stream, Predicate)
     * @param <E>
     * @param stream
     * @param terminationPredicate
     * @return
     */
    public static <E> Stream<E> takeUntilInclusive(Stream<E> stream, Predicate<E> terminationPredicate)
    {
        return takeUntil(stream, terminationPredicate, true);
    }

    /**
     * Limits the given {@link Stream} to the elements up to but <b>excluding</b> the first element matching the given {@link Predicate}. If no element matches,
     * all elements are returned.
     * <br>
     * <br>
     * Shares all traversal and parallelism characteristics of {@link #takeUntilInclusive(Stream, Predicate)}.
     *
     * @see #takeUntilInclusive(Stream, Predicate)
     * @param <E>
     * @param stream
     * @param terminationPredicate
     * @return
     */
    public static <E> Stream<E> takeUntilExclusive(Stream<E> stream, Predicate<E> terminationPredicate)
    {
        return takeUntil(stream, terminationPredicate, false);
    }

    /**
     * Returns a {@link Stream} which traverses the elements of the given {@link Stream} strictly sequential, which means that the consumer of an element has
     * finished its processing before the next element is pulled from the source.
     * <br>
     * <br>
     * The returned {@link Stream} can not be splitted, so a {@link Stream#parallel()} call of the caller will neither result in parallel execution nor break
     * any consumer side state which relies on the encounter order. This is the underlying primitive for all stateful and order dependent operations like
     * {@link #takeUntilInclusive(Stream, Predicate)} or {@link #takeUntilObservedTermination(Stream, BiFunction)}.
     * <br>
     * <br>
     * A null {@link Stream} is treated like an empty {@link Stream}.
     *
     * @param <E>
     * @param stream
     * @return
     */
    public static <E> Stream<E> sequentialized(Stream<E> stream)
    {
        Stream<E> sourceStream = Optional.ofNullable(stream)
                                         .orElse(Stream.empty());

        Spliterator<E> sourceSpliterator = sourceStream.sequential()
                                                       .spliterator();

        // no element is dropped, so the size stays valid, but there are no sub spliterators anymore
        int characteristics = sourceSpliterator.characteristics() & ~Spliterator.SUBSIZED;

        Spliterator<E> sequentialSpliterator = new Spliterators.AbstractSpliterator<E>(sourceSpliterator.estimateSize(), characteristics)
        {
            @Override
            public boolean tryAdvance(Consumer<? super E> action)
            {
                return sourceSpliterator.tryAdvance(action);
            }

            @Override
            public Spliterator<E> trySplit()
            {
                // never split, otherwise consumer side state would be shared between the split parts
                return null;
            }

            @Override
            public Comparator<? super E> getComparator()
            {
                return sourceSpliterator.getComparator();
            }
        };

        return StreamSupport.stream(sequentialSpliterator, false)
                            .onClose(sourceStream::close);
    }

    /**
     * Returns a {@link Stream} which pulls elements from the given source {@link Stream} until an already emitted element has signalled the termination via the
     * {@link TerminationSignal} given to the element decorator.
     * <br>
     * <br>
     * In contrast to {@link #takeUntilInclusive(Stream, Predicate)} no {@link Predicate} is applied to the elements by this operation itself, so the elements
     * are never resolved or dereferenced here. Only the consumer decides by its own access to an element whether the termination is signalled. This allows a
     * downstream {@link Stream#skip(long)} to discard elements without paying for them, at the price that a consumer which never touches any element will never
     * terminate the {@link Stream}. Use a {@link Stream#limit(long)} or a short circuiting terminal operation for such consumers.
     * <br>
     * <br>
     * The given decorator is invoked for every emitted element and must be cheap, since it is called before the consumer decides to use the element. Typically
     * it wraps the element into a lazy decorator which calls {@link TerminationSignal#terminateIf(boolean)} as soon as the element gets resolved.
     * <br>
     * <br>
     * A null {@link Stream} is treated like an empty {@link Stream}, a null decorator returns the source {@link Stream} unchanged.
     *
     * @see #sequentialized(Stream)
     * @param <E>
     * @param stream
     * @param elementDecorator
     * @return
     */
    public static <E> Stream<E> takeUntilObservedTermination(Stream<E> stream, BiFunction<E, TerminationSignal, E> elementDecorator)
    {
        if (elementDecorator == null)
        {
            return Optional.ofNullable(stream)
                           .orElse(Stream.empty());
        }

        return takeUntilObservedTermination(terminationSignal -> Optional.ofNullable(stream)
                                                                         .orElse(Stream.<E>empty())
                                                                         .map(element -> elementDecorator.apply(element, terminationSignal)));
    }

    /**
     * Variant of {@link #takeUntilObservedTermination(Stream, BiFunction)} where the source {@link Stream} is created by the given factory with the
     * {@link TerminationSignal} at hand. Use this if the elements already know how to signal the termination by themselves, e.g. because the decision needs
     * context like an element index which is only available during the {@link Stream} creation.
     *
     * @see #takeUntilObservedTermination(Stream, BiFunction)
     * @param <E>
     * @param streamFactory
     * @return
     */
    public static <E> Stream<E> takeUntilObservedTermination(Function<TerminationSignal, Stream<E>> streamFactory)
    {
        if (streamFactory == null)
        {
            return Stream.empty();
        }

        AtomicBoolean terminated = new AtomicBoolean(false);
        TerminationSignal terminationSignal = new TerminationSignal()
        {
            @Override
            public void terminate()
            {
                terminated.set(true);
            }

            @Override
            public boolean isTerminated()
            {
                return terminated.get();
            }
        };

        // the termination flag is checked before the next element is emitted, which is safe since the sequentialized stream
        // guarantees that the consumer has finished the previous element already
        return sequentialized(streamFactory.apply(terminationSignal)).takeWhile(element -> !terminated.get());
    }

    /**
     * Signal given to the element decorator of {@link StreamUtils#takeUntilObservedTermination(Stream, BiFunction)} which allows a consumed element to
     * terminate the {@link Stream} it originates from.
     *
     * @see StreamUtils#takeUntilObservedTermination(Stream, BiFunction)
     */
    public static interface TerminationSignal
    {
        /**
         * Signals that the currently observed element is the last one, so no further element is pulled from the source {@link Stream}.
         */
        public void terminate();

        /**
         * Invokes {@link #terminate()} if the given condition is true.
         *
         * @param condition
         */
        public default void terminateIf(boolean condition)
        {
            if (condition)
            {
                this.terminate();
            }
        }

        /**
         * Returns true if {@link #terminate()} has been called already.
         *
         * @return
         */
        public boolean isTerminated();

        /**
         * Returns a {@link TerminationSignal} which ignores any {@link #terminate()} call. Use this to probe an element without terminating the
         * {@link Stream} it belongs to.
         *
         * @return
         */
        public static TerminationSignal noOperation()
        {
            return new TerminationSignal()
            {
                @Override
                public void terminate()
                {
                    // do nothing
                }

                @Override
                public boolean isTerminated()
                {
                    return false;
                }
            };
        }
    }

    private static <E> Stream<E> takeUntil(Stream<E> stream, Predicate<E> terminationPredicate, boolean terminatingElementIncluded)
    {
        Stream<E> sourceStream = Optional.ofNullable(stream)
                                         .orElse(Stream.empty());

        if (terminationPredicate == null)
        {
            return sourceStream;
        }

        Spliterator<E> sourceSpliterator = sourceStream.sequential()
                                                       .spliterator();

        // the size is not known anymore and a prefix of a sorted source must not claim to provide a comparator
        int characteristics = sourceSpliterator.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.SORTED);

        Spliterator<E> limitedSpliterator = new Spliterators.AbstractSpliterator<E>(sourceSpliterator.estimateSize(), characteristics)
        {
            private boolean lastElementReached = false;

            @Override
            public boolean tryAdvance(Consumer<? super E> action)
            {
                if (this.lastElementReached)
                {
                    return false;
                }
                return sourceSpliterator.tryAdvance(element ->
                {
                    if (terminationPredicate.test(element))
                    {
                        this.lastElementReached = true;
                        if (terminatingElementIncluded)
                        {
                            action.accept(element);
                        }
                    }
                    else
                    {
                        action.accept(element);
                    }
                });
            }

            @Override
            public Spliterator<E> trySplit()
            {
                // the termination state is inherently sequential and must not be shared between split parts
                return null;
            }
        };

        return StreamSupport.stream(limitedSpliterator, false)
                            .onClose(sourceStream::close);
    }

    public static interface StreamPipeline
    {

        public <A> SourcedStreamPipeline<A> source(Stream<A> stream);

        public <A, B> BiSourcedStreamPipeline<A, B> sources(Stream<A> streamA, Stream<B> streamB);

        public <S> UnbatchedSeededStreamPipeline<S> seed(Stream<S> seeds);

        public <S> SeededStreamPipeline<S> seed(Collection<S> seeds);

    }

    public static interface UnbatchedSeededStreamPipeline<S>
    {
        public SeededStreamPipeline<S> batch(int batchSize);
    }

    public static interface SeededStreamPipeline<S>
    {
        public <A> SourcedStreamPipeline<A> source(Function<Stream<S>, Stream<A>> streamProvider);

        public <A, B> BiSourcedStreamPipeline<A, B> sources(Function<Stream<S>, Stream<A>> streamProviderA, Function<Stream<S>, Stream<B>> streamProviderB);

    }

    public static interface SourcedStreamPipeline<A> extends Streamable<A>
    {

        public <B> BiSourcedStreamPipeline<A, B> andSource(Stream<B> streamB);

        public BiSourcedStreamPipeline<A, A> fork();
    }

    public static interface BiSourcedStreamPipeline<A, B>
    {
        public <AB> SourcedStreamPipeline<AB> combine(BiFunction<A, B, AB> combiner);

        public <AB> SourcedStreamPipeline<AB> combineAsOptionals(BiFunction<Optional<A>, Optional<B>, Optional<AB>> combiner);

        public <AR, BR> BiSourcedStreamPipeline<AR, BR> map(Function<A, AR> mapperA, Function<B, BR> mapperB);

        public <AR, BR> BiSourcedStreamPipeline<AR, BR> flatMap(Function<A, Stream<AR>> mapperA, Function<B, Stream<BR>> mapperB);
    }

    public static StreamPipeline pipeline()
    {
        return new StreamPipelineImpl();
    }

    private static class StreamPipelineImpl implements StreamPipeline
    {
        @Override
        public <A> SourcedStreamPipeline<A> source(Stream<A> stream)
        {
            return new SourcedStreamPipeline<A>()
            {
                @Override
                public Stream<A> stream()
                {
                    return stream;
                }

                @Override
                public <B> BiSourcedStreamPipeline<A, B> andSource(Stream<B> streamB)
                {
                    return pipeline().sources(stream, streamB);
                }

                @Override
                public BiSourcedStreamPipeline<A, A> fork()
                {
                    return new BiSourcedStreamPipelineImpl<>(stream.map(a -> new LeftAndRight<>(a, a)));
                }
            };
        }

        @Override
        public <A, B> BiSourcedStreamPipeline<A, B> sources(Stream<A> streamA, Stream<B> streamB)
        {
            return new BiSourcedStreamPipelineImpl<A, B>(StreamUtils.merge(streamA, streamB));
        }

        @Override
        public <S> SeededStreamPipeline<S> seed(Collection<S> seeds)
        {
            return new SeededStreamPipeline<S>()
            {
                @Override
                public <A> SourcedStreamPipeline<A> source(Function<Stream<S>, Stream<A>> stream)
                {
                    return pipeline().source(stream.apply(seeds.stream()));
                }

                @Override
                public <A, B> BiSourcedStreamPipeline<A, B> sources(Function<Stream<S>, Stream<A>> streamProviderA,
                                                                    Function<Stream<S>, Stream<B>> streamProviderB)
                {
                    return pipeline().sources(streamProviderA.apply(seeds.stream()), streamProviderB.apply(seeds.stream()));
                }
            };
        }

        @Override
        public <S> UnbatchedSeededStreamPipeline<S> seed(Stream<S> seeds)
        {
            return new UnbatchedSeededStreamPipeline<S>()
            {
                @Override
                public SeededStreamPipeline<S> batch(int batchSize)
                {
                    Stream<List<S>> seedBatches = StreamUtils.framedAsList(batchSize, seeds);
                    return new SeededStreamPipeline<S>()
                    {
                        @Override
                        public <A> SourcedStreamPipeline<A> source(Function<Stream<S>, Stream<A>> streamProvider)
                        {
                            return pipeline().source(seedBatches.flatMap(batch -> streamProvider.apply(batch.stream())));
                        }

                        @Override
                        public <A, B> BiSourcedStreamPipeline<A, B> sources(Function<Stream<S>, Stream<A>> streamProviderA,
                                                                            Function<Stream<S>, Stream<B>> streamProviderB)
                        {
                            return pipeline().source(seedBatches)
                                             .fork()
                                             .flatMap(batch -> streamProviderA.apply(batch.stream()), batch -> streamProviderB.apply(batch.stream()));
                        }
                    };
                }
            };
        }

        private static class BiSourcedStreamPipelineImpl<A, B> implements BiSourcedStreamPipeline<A, B>
        {
            private final Stream<LeftAndRight<A, B>> mergedStreamAB;

            private BiSourcedStreamPipelineImpl(Stream<LeftAndRight<A, B>> mergedStreamAB)
            {
                this.mergedStreamAB = mergedStreamAB;
            }

            @Override
            public <AB> SourcedStreamPipeline<AB> combineAsOptionals(BiFunction<Optional<A>, Optional<B>, Optional<AB>> combiner)
            {
                Optional<BiFunction<Optional<A>, Optional<B>, Optional<AB>>> optionalCombiner = Optional.ofNullable(combiner);
                return pipeline().source(this.mergedStreamAB.map(lar -> optionalCombiner.flatMap(iCombiner -> iCombiner.apply(Optional.ofNullable(lar.getLeft()),
                                                                                                                              Optional.ofNullable(lar.getRight()))))
                                                            .filter(Optional::isPresent)
                                                            .map(Optional::get));
            }

            @Override
            public <AB> SourcedStreamPipeline<AB> combine(BiFunction<A, B, AB> combiner)
            {
                if (combiner == null)
                {
                    return pipeline().source(Stream.empty());
                }
                else
                {
                    return pipeline().source(this.mergedStreamAB.map(lar -> combiner.apply(lar.getLeft(), lar.getRight())));
                }
            }

            @Override
            public <AR, BR> BiSourcedStreamPipeline<AR, BR> map(Function<A, AR> mapperA, Function<B, BR> mapperB)
            {
                return new BiSourcedStreamPipelineImpl<>(this.mergedStreamAB.map(lar -> new LeftAndRight<>(mapperA.apply(lar.getLeft()),
                                                                                                           mapperB.apply(lar.getRight()))));
            }

            @Override
            public <AR, BR> BiSourcedStreamPipeline<AR, BR> flatMap(Function<A, Stream<AR>> mapperA, Function<B, Stream<BR>> mapperB)
            {
                return new BiSourcedStreamPipelineImpl<AR, BR>(this.mergedStreamAB.flatMap(lar -> StreamUtils.<AR, BR>merge(mapperA.apply(lar.getLeft()),
                                                                                                                            mapperB.apply(lar.getRight()))));
            }

        }

    }

    public static <E, R> Stream<R> mapWithPrevious(Stream<E> stream, BiFunction<R, E, R> mapper)
    {
        AtomicReference<R> previous = new AtomicReference<>();
        return Optional.ofNullable(stream)
                       .orElse(Stream.empty())
                       .sequential()
                       .map(value -> previous.updateAndGet(previousValue -> mapper.apply(previousValue, value)));
    }

    public static <E, R> Optional<R> reduceWithPrevious(Stream<E> stream, BiFunction<R, E, R> mapper)
    {
        return mapWithPrevious(stream, mapper).map(Optional::ofNullable)
                                              .reduce((r1, r2) -> r2)
                                              .flatMap(Function.identity());
    }

    public static <E> Stream<E> lazyLoading(Stream<Supplier<E>> stream)
    {
        return new StreamDecoratorLazyLoading<>(stream);
    }

    /**
     * Variant of {@link #lazyLoading(Stream)} where the given skip observer is notified with the accumulated number of skipped elements whenever
     * {@link Stream#skip(long)} is called on the returned {@link Stream} or any {@link Stream} derived from it. This allows the producer of the
     * {@link Supplier}s to know the position of the first element which the consumer will actually resolve.
     *
     * @see StreamDecoratorLazyLoading#StreamDecoratorLazyLoading(Stream, LongConsumer)
     * @param <E>
     * @param stream
     * @param skipObserver
     * @return
     */
    public static <E> Stream<E> lazyLoading(Stream<Supplier<E>> stream, LongConsumer skipObserver)
    {
        return new StreamDecoratorLazyLoading<>(stream, skipObserver);
    }

    public static PagesProvider fromPageProvider()
    {
        return new PagesProvider()
        {
            private int pageSize = 100;

            @Override
            public PagesProvider withPageSize(int pageSize)
            {
                this.pageSize = Math.max(0, pageSize);
                return this;
            }

            @Override
            public <E> StreamablePages<E> usingPageProvider(Function<Paging, Page<E>> paging)
            {
                return new StreamablePagesImpl<>(createStreamablePage(paging, this.pageSize), this.pageSize);
            }
        };
    }

    private static <E> IntFunction<Supplier<StreamablePage<E>>> createStreamablePage(Function<Paging, Page<E>> pagingToPageMapper, int pageSize)
    {
        return index ->
        {
            return CachedElement.of(() -> new StreamablePage<E>()
            {
                private CachedElement<Page<E>> pageProvider = CachedElement.of(() -> this.resolvePage(pagingToPageMapper, pageSize, index));

                @Override
                public Stream<E> stream()
                {
                    return this.getElements()
                               .stream();
                }

                private List<E> getElements()
                {
                    return Optional.of(this.pageProvider.get())
                                   .map(Page::getElements)
                                   .orElse(Collections.emptyList());
                }

                private Page<E> resolvePage(Function<Paging, Page<E>> pagingToPageMapper, int pageSize, int index)
                {
                    return pagingToPageMapper.apply(new Paging()
                    {
                        @Override
                        public int getPageIndex()
                        {
                            return index;
                        }

                        @Override
                        public int getPageSize()
                        {
                            return pageSize;
                        }

                        @Override
                        public int getStartIndex()
                        {
                            return index * pageSize;
                        }

                        @Override
                        public int getStopIndexExclusive()
                        {
                            return (index + 1) * pageSize;
                        }
                    });
                }

                @Override
                public boolean isLastPage()
                {
                    // if the page does not declare it explicitly, a page which is not filled up to the page size is the last one
                    return Optional.ofNullable(this.pageProvider.get()
                                                                .getIsLastPage())
                                   .orElseGet(() -> this.getElements()
                                                        .size() < pageSize);
                }

                @Override
                public Stream<Supplier<Stream<E>>> elementProviderStream()
                {
                    return IntStream.range(0, pageSize)
                                    .mapToObj(elementIndex -> () ->
                                    {
                                        List<E> elements = this.getElements();
                                        if (elementIndex >= 0 && elementIndex < elements.size())
                                        {
                                            return Stream.of(ListUtils.get(elements, elementIndex));
                                        }
                                        else
                                        {
                                            return Stream.empty();
                                        }
                                    });
                }
            });
        };
    }

    /**
     * {@link StreamablePage} decorator which signals the termination of the surrounding page {@link Stream} as soon as the consumer resolves a page which is
     * the last one. A page which is never touched by the consumer, e.g. because it has been skipped, does not signal anything and does not resolve the
     * underlying page.
     *
     * @see StreamUtils#takeUntilObservedTermination(Stream, BiFunction)
     * @param <E>
     */
    private static class TerminationSignalingStreamablePage<E> implements StreamablePage<E>
    {
        private final StreamablePage<E> page;
        private final TerminationSignal terminationSignal;

        public TerminationSignalingStreamablePage(StreamablePage<E> page, TerminationSignal terminationSignal)
        {
            this.page = page;
            this.terminationSignal = terminationSignal;
        }

        @Override
        public Stream<E> stream()
        {
            Stream<E> stream = this.page.stream();
            this.signalTerminationIfLastPage();
            return stream;
        }

        @Override
        public Stream<Supplier<Stream<E>>> elementProviderStream()
        {
            Stream<Supplier<Stream<E>>> elementProviderStream = this.page.elementProviderStream();
            this.signalTerminationIfLastPage();
            return elementProviderStream;
        }

        @Override
        public boolean isLastPage()
        {
            boolean lastPage = this.page.isLastPage();
            this.terminationSignal.terminateIf(lastPage);
            return lastPage;
        }

        private void signalTerminationIfLastPage()
        {
            // the page is resolved already at this point, so this does not trigger any further page retrieval
            this.terminationSignal.terminateIf(this.page.isLastPage());
        }
    }

    private static class StreamablePagesImpl<E> implements StreamablePages<E>
    {
        private final IntFunction<Supplier<StreamablePage<E>>> randomAccessPageProvider;
        private final int                                      pageSize;

        public StreamablePagesImpl(IntFunction<Supplier<StreamablePage<E>>> randomAccessPageProvider, int pageSize)
        {
            this.randomAccessPageProvider = randomAccessPageProvider;
            this.pageSize = pageSize;
        }

        @Override
        public StreamablePageElements<E> asElements()
        {
            int pageSize = this.pageSize;
            IntFunction<Supplier<StreamablePage<E>>> randomAccessPageProvider = this.randomAccessPageProvider;
            return new StreamablePageElements<E>()
            {
                @Override
                public Stream<E> stream()
                {
                    SingleKeyCachedElement<Integer, StreamablePage<E>> pageIndexToStreamablePage = new SingleKeyCachedElement<>();
                    AtomicReference<int[]> lastResolvedPosition = new AtomicReference<>();

                    // the index of the first element which the consumer will resolve, moved forward by any skip of the consumer
                    AtomicInteger anchorIndex = new AtomicInteger(0);

                    // the highest index which is known to be available, since a resolved page reveals all of its remaining elements at once
                    AtomicInteger knownAvailableUpToIndex = new AtomicInteger(-1);

                    BiFunction<Integer, TerminationSignal, Optional<Element<E>>> elementResolver = (index, terminationSignal) ->
                    {
                        int[] previousPosition = lastResolvedPosition.get();
                        int pageIndex;
                        int elementIndex;
                        if (previousPosition != null && previousPosition[0] == index)
                        {
                            // the same index has been resolved before, e.g. by a probe, so its position is reused to stay consistent
                            pageIndex = previousPosition[1];
                            elementIndex = previousPosition[2];
                        }
                        else if (previousPosition != null && previousPosition[0] == index - 1)
                        {
                            pageIndex = previousPosition[1];
                            elementIndex = previousPosition[2] + 1;
                        }
                        else
                        {
                            // no predecessor has been resolved, e.g. because of a skip, so the page size based mapping is used
                            pageIndex = index / pageSize;
                            elementIndex = index - pageIndex * pageSize;
                        }

                        StreamablePage<E> page = pageIndexToStreamablePage.apply(pageIndex, randomAccessPageProvider.apply(pageIndex))
                                                                          .get();
                        Optional<Element<E>> element = page.nthElement(elementIndex);

                        // a page which provides less elements than the page size and which is not the last one rolls over to the next page
                        while (element.isEmpty() && !page.isLastPage())
                        {
                            pageIndex = pageIndex + 1;
                            elementIndex = 0;
                            page = pageIndexToStreamablePage.apply(pageIndex, randomAccessPageProvider.apply(pageIndex))
                                                            .get();
                            element = page.nthElement(elementIndex);
                        }

                        if (element.isPresent())
                        {
                            lastResolvedPosition.set(new int[] { index, pageIndex, elementIndex });

                            // the resolved page reveals all of its remaining elements, so they do not have to be probed anymore
                            int remainingElementsOfPage = (int) page.stream()
                                                                    .count()
                                    - 1 - elementIndex;
                            knownAvailableUpToIndex.updateAndGet(knownIndex -> Math.max(knownIndex, index + remainingElementsOfPage));
                        }

                        // the look ahead stays within the already resolved page, so it does not retrieve any further page
                        terminationSignal.terminateIf(page.isLastPage() && page.nthElement(elementIndex + 1)
                                                                              .isEmpty());

                        return element;
                    };

                    // no predicate is applied to the elements here, the termination is decided by the element which the consumer resolves,
                    // which allows a downstream skip to discard elements without retrieving their pages
                    Stream<Supplier<Optional<Element<E>>>> elementProviderStream = StreamUtils.takeUntilObservedTermination(terminationSignal ->
                    {
                        return StreamUtils.generate()
                                          .intStream()
                                          .unlimited()
                                          .fromZero()
                                          // an element is only emitted if it is known to be available. This is the case for elements which are discarded by a
                                          // skip of the consumer anyway and for elements of an already resolved page. All other elements are probed before
                                          // they are emitted, which keeps an exhausted source from emitting an unresolvable element. The probe must not
                                          // signal the termination, since the probed element still has to be emitted.
                                          .takeWhile(index -> !terminationSignal.isTerminated()
                                                  && (index < anchorIndex.get() || index <= knownAvailableUpToIndex.get()
                                                          || elementResolver.apply(index, TerminationSignal.noOperation())
                                                                            .isPresent()))
                                          .mapToObj(index -> () -> elementResolver.apply(index, terminationSignal));
                    });

                    return StreamUtils.lazyLoading(elementProviderStream,
                                                   skipOffset -> anchorIndex.set((int) Math.min(Integer.MAX_VALUE, skipOffset)))
                                      .map(element -> element.map(Element::getValue)
                                                             .orElse(null));
                }

            };
        }

        @Override
        public Stream<StreamablePage<E>> stream()
        {
            // the lazy loading decorator stays on the outside, so that a downstream skip discards the page providers without resolving any page
            return StreamUtils.lazyLoading(StreamUtils.takeUntilObservedTermination(StreamUtils.generate()
                                                                                               .intStream()
                                                                                               .unlimited()
                                                                                               .fromZero()
                                                                                               .mapToObj(this.randomAccessPageProvider),
                                                                                    (pageProvider, terminationSignal) -> () -> new TerminationSignalingStreamablePage<>(pageProvider.get(),
                                                                                                                                                                        terminationSignal)));
        }

    }

    public static interface PagesProvider
    {

        public PagesProvider withPageSize(int pageSize);

        public <E> StreamablePages<E> usingPageProvider(Function<Paging, Page<E>> page);

    }

    public static interface Paging
    {
        public int getPageSize();

        /**
         * Returns the absolute page index position (0,1,2,...)
         * 
         * @return
         */
        public int getPageIndex();

        /**
         * Returns the absolute start index position (inclusive) for individual elements (0,1,2,...)
         * 
         * @return
         */
        public int getStartIndex();

        /**
         * Returns the absolute stop index position (exclusive) (0,1,2, ...)
         * 
         * @return
         */
        public int getStopIndexExclusive();

    }

    @Value
    @Builder
    public static class Page<E>
    {
        @Default
        private List<E> elements = Collections.emptyList();

        /**
         * Optional flag which marks this {@link Page} as the last one. If it is not set, the last page is determined by the fill level of the {@link Page}: a
         * {@link Page} which provides less elements than the requested {@link Paging#getPageSize()} is the last one.
         */
        private Boolean isLastPage;

        /**
         * Creates a {@link Page} with the given elements where the last page is determined by the fill level of the {@link Page}.
         *
         * @see #of(List, boolean)
         * @param <E>
         * @param elements
         * @return
         */
        public static <E> Page<E> of(List<E> elements)
        {
            return Page.<E>builder()
                       .elements(elements)
                       .build();
        }

        /**
         * Creates a {@link Page} with the given elements and an explicit last page flag.
         *
         * @see #of(List)
         * @param <E>
         * @param elements
         * @param isLastPage
         * @return
         */
        public static <E> Page<E> of(List<E> elements, boolean isLastPage)
        {
            return Page.<E>builder()
                       .elements(elements)
                       .isLastPage(isLastPage)
                       .build();
        }
    }

    public static interface StreamablePages<E> extends Streamable<StreamablePage<E>>
    {
        public StreamablePageElements<E> asElements();
    }

    public static interface StreamablePageElements<E> extends Streamable<E>
    {

    }

    public static interface StreamablePage<E> extends Streamable<E>
    {
        public boolean isLastPage();

        public Stream<Supplier<Stream<E>>> elementProviderStream();
    }
}
