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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.omnaest.utils.element.lar.LeftAndRight;

public class StreamUtils
{

    @SafeVarargs
    public static <E> Stream<E> concat(Stream<E>... streams)
    {
        return concat(Arrays.asList(streams)
                            .stream());
    }

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

    public static <E> Stream<E> fromIterator(Iterator<E> iterator)
    {
        return StreamSupport.stream(((Iterable<E>) () -> iterator).spliterator(), false);
    }

    public static <E> Stream<E> fromSupplier(Supplier<E> supplier, Predicate<E> terminateMatcher)
    {
        return fromIterator(new Iterator<E>()
        {
            private AtomicReference<E> takenElement = new AtomicReference<>();

            @Override
            public boolean hasNext()
            {
                this.takeOneElement();
                return terminateMatcher.negate()
                                       .test(this.takenElement.get());
            }

            @Override
            public E next()
            {
                this.takeOneElement();
                return this.takenElement.getAndSet(null);
            }

            private void takeOneElement()
            {
                this.takenElement.getAndUpdate(e -> e != null ? e : supplier.get());
            }
        }).filter(terminateMatcher.negate());

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
     * Similar to {@link #fromReader(Reader)} using a given {@link InputStream} and a the given {@link Charset}
     * 
     * @see StandardCharsets
     * @param inputStream
     * @param charset
     * @return
     */
    public static Stream<String> fromInputStream(InputStream inputStream, Charset charset)
    {
        return fromReader(new InputStreamReader(inputStream, charset));
    }

    /**
     * Returns a {@link Stream} of lines of the given {@link Reader}
     * 
     * @param reader
     * @return
     */
    public static Stream<String> fromReader(Reader reader)
    {
        BufferedReader bufferedReader = new BufferedReader(reader);
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
        Supplier<Spliterator<E>> supplier = () -> iterable.spliterator();
        int characteristics = iterable.spliterator()
                                      .characteristics();
        return StreamSupport.stream(supplier, characteristics, false);
    }

    /**
     * Similar to {@link #framed(int, Stream)} for an {@link IntStream}
     * 
     * @see IntStream
     * @see #framed(int, Stream)
     * @param frameSize
     * @param stream
     * @return
     */
    public static Stream<int[]> framed(int frameSize, IntStream stream)
    {
        return framed(frameSize, stream.mapToObj(Integer::valueOf)).map(array -> ArrayUtils.toPrimitive(array));
    }

    /**
     * Creates block frames of a given size based on a given {@link Stream} of elements.<br>
     * <br>
     * E.g. [1,2,3,4,5,6] -> [1,2],[3,4],[5,6] for a frame size = 2
     * 
     * @see #framed(int, IntStream)
     * @param frameSize
     * @param stream
     * @return
     */
    public static <E> Stream<E[]> framed(int frameSize, Stream<E> stream)
    {
        AtomicLong position = new AtomicLong();
        AtomicReference<E[]> frame = new AtomicReference<E[]>();
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

            boolean frameFinished = frameIndex == frameSize - 1;
            return frameFinished;
        };
        Function<E, E[]> mapper = element -> frame.getAndSet(null);
        Stream<E[]> retval = stream.filter(filter)
                                   .map(mapper);
        return Stream.concat(retval, Stream.of(1)
                                           .filter(n -> frame.get() != null)
                                           .map(n -> frame.getAndSet(null)));
    }

    public static interface Window<E>
    {
        public List<E> getBefore();

        public List<E> getAfter();

        public E get();

        public List<E> getAll();
    }

    public static <E> Stream<Window<E>> windowed(Stream<E> stream, int before, int after)
    {
        int step = 1;
        return windowed(stream, before, after, step);
    }

    public static <E> Stream<Window<E>> windowed(Stream<E> stream, int before, int after, int step)
    {
        List<E> elementsBefore = new LinkedList<>();
        List<E> elementsAfter = new LinkedList<>();
        AtomicReference<E> currentElement = new AtomicReference<>();
        AtomicBoolean first = new AtomicBoolean(true);
        AtomicLong stepCounter = new AtomicLong();

        Supplier<Boolean> stepTest = () ->
        {
            return stepCounter.incrementAndGet() % step == 0;
        };

        Function<? super E, ? extends Window<E>> mapper = element ->
        {
            List<E> localElementsBefore = new ArrayList<>(elementsBefore);
            List<E> localElementsAfter = new ArrayList<>(elementsAfter);
            E localCurrentElement = currentElement.get();

            return new Window<E>()
            {
                @Override
                public List<E> getBefore()
                {
                    return localElementsBefore;
                }

                @Override
                public List<E> getAfter()
                {
                    return localElementsAfter;
                }

                @Override
                public E get()
                {
                    return localCurrentElement;
                }

                @Override
                public List<E> getAll()
                {
                    return ListUtils.mergedList(localElementsBefore, Arrays.asList(localCurrentElement), localElementsAfter);
                }

            };
        };

        Stream<Window<E>> primaryStream = stream.filter(element ->
        {

            elementsAfter.add(element);

            while (elementsAfter.size() > after)
            {
                E next = elementsAfter.remove(0);
                E last = currentElement.getAndSet(next);
                if (!first.getAndSet(false))
                {
                    elementsBefore.add(last);
                }
            }

            while (elementsBefore.size() > before)
            {
                elementsBefore.remove(0);
            }

            boolean isStep = stepTest.get();
            return isStep && elementsAfter.size() >= after && !first.get();
        })
                                                .map(mapper);
        Stream<Window<E>> secondaryStream = Stream.of(1)
                                                  .flatMap(i -> elementsAfter.stream()
                                                                             .collect(Collectors.toList())
                                                                             .stream()
                                                                             .filter(e -> stepTest.get())
                                                                             .peek(element ->
                                                                             {
                                                                                 E next = elementsAfter.remove(0);
                                                                                 E last = currentElement.getAndSet(next);
                                                                                 elementsBefore.add(last);

                                                                                 while (elementsBefore.size() > before)
                                                                                 {
                                                                                     elementsBefore.remove(0);
                                                                                 }
                                                                             })
                                                                             .map(mapper));

        return Stream.concat(primaryStream, secondaryStream);

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

}
