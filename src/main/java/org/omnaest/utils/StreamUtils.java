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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
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
import org.omnaest.utils.element.lar.LeftAndRight;
import org.omnaest.utils.functional.PredicateConsumer;
import org.omnaest.utils.stream.DefaultSupplierStream;
import org.omnaest.utils.stream.FilterAllOnFirstFilterFailStreamDecorator;
import org.omnaest.utils.stream.FilterMapper;
import org.omnaest.utils.stream.SupplierStream;
import org.omnaest.utils.supplier.OptionalSupplier;

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
                return "Options [start=" + start + ", increment=" + increment + ", terminationPredicate=" + terminationPredicate + "]";
            }

            public int getStart()
            {
                return start;
            }

            public int getIncrement()
            {
                return increment;
            }

            public Predicate<Integer> getTerminationPredicate()
            {
                return terminationPredicate;
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

}
