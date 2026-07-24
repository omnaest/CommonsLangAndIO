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
package org.omnaest.utils.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @see Stream
 * @author omnaest
 * @param <E>
 */
public class StreamDecoratorLazyLoading<E> implements Stream<E>
{
    private Stream<Supplier<E>> stream;
    private final LongConsumer  skipObserver;
    private final long          skipOffset;

    public StreamDecoratorLazyLoading(Stream<Supplier<E>> stream)
    {
        this(stream, null);
    }

    /**
     * Creates an instance which notifies the given skip observer with the accumulated number of skipped elements whenever {@link #skip(long)} is called. This
     * allows the producer of the underlying {@link Supplier}s to know the position of the first element which the consumer will actually resolve, e.g. to
     * validate its existence.
     * <br>
     * <br>
     * The accumulated offset refers to the positions of the source {@link Stream}, which stays valid for {@link #skip(long)}, {@link #limit(long)},
     * {@link #map(Function)} and the traversal characteristic operations. It is <b>not</b> valid anymore after a {@link #dropWhile(Predicate)}, since that
     * discards a prefix without being visible as a skip. All other operations resolve the {@link Supplier}s anyway and do not retain this decorator.
     * <br>
     * <br>
     * The observer is shared with all derived instances, so branching one instance into multiple differently skipped variants and traversing more than one of
     * them is not supported.
     *
     * @param stream
     * @param skipObserver
     */
    public StreamDecoratorLazyLoading(Stream<Supplier<E>> stream, LongConsumer skipObserver)
    {
        this(stream, skipObserver, 0);
    }

    private StreamDecoratorLazyLoading(Stream<Supplier<E>> stream, LongConsumer skipObserver, long skipOffset)
    {
        super();
        this.stream = stream;
        this.skipObserver = skipObserver;
        this.skipOffset = skipOffset;
    }

    private Stream<E> resolvedStream()
    {
        return this.stream.map(Supplier::get);
    }

    protected void modifyStream(UnaryOperator<Stream<Supplier<E>>> modifier)
    {
        this.stream = modifier.apply(this.stream);
    }

    protected Stream<E> modifiedStream(UnaryOperator<Stream<Supplier<E>>> modifier)
    {
        return new StreamDecoratorLazyLoading<>(modifier.apply(this.stream), this.skipObserver, this.skipOffset);
    }

    @Override
    public String toString()
    {
        return "StreamDecorator [stream=" + this.stream + "]";
    }

    @Override
    public Iterator<E> iterator()
    {
        return this.resolvedStream()
                   .iterator();
    }

    @Override
    public Spliterator<E> spliterator()
    {
        return this.resolvedStream()
                   .spliterator();
    }

    @Override
    public boolean isParallel()
    {
        return this.stream.isParallel();
    }

    @Override
    public Stream<E> sequential()
    {
        return this.modifiedStream(stream -> stream.sequential());
    }

    @Override
    public Stream<E> parallel()
    {
        return this.modifiedStream(stream -> stream.parallel());
    }

    @Override
    public Stream<E> unordered()
    {
        return this.modifiedStream(stream -> stream.unordered());
    }

    @Override
    public Stream<E> onClose(Runnable closeHandler)
    {
        return this.modifiedStream(stream -> stream.onClose(closeHandler));
    }

    @Override
    public void close()
    {
        this.stream.close();
    }

    @Override
    public Stream<E> filter(Predicate<? super E> predicate)
    {
        return this.resolvedStream()
                   .filter(predicate);
    }

    /**
     * The mapper is pushed into the {@link Supplier}s, so the lazy loading is retained and the mapper is only applied to those elements which are resolved by
     * the consumer. Elements which are discarded by a downstream {@link #skip(long)} or {@link #limit(long)} are neither resolved nor mapped.
     */
    @Override
    public <R> Stream<R> map(Function<? super E, ? extends R> mapper)
    {
        Stream<Supplier<R>> mappedStream = this.stream.map(supplier -> () -> mapper.apply(supplier.get()));
        return new StreamDecoratorLazyLoading<>(mappedStream, this.skipObserver, this.skipOffset);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super E> mapper)
    {
        return this.resolvedStream()
                   .mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super E> mapper)
    {
        return this.resolvedStream()
                   .mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper)
    {
        return this.resolvedStream()
                   .mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper)
    {
        return this.resolvedStream()
                   .flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper)
    {
        return this.resolvedStream()
                   .flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper)
    {
        return this.resolvedStream()
                   .flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper)
    {
        return this.resolvedStream()
                   .flatMapToDouble(mapper);
    }

    @Override
    public Stream<E> distinct()
    {
        return this.resolvedStream()
                   .distinct();
    }

    @Override
    public Stream<E> sorted()
    {
        return this.resolvedStream()
                   .sorted();
    }

    @Override
    public Stream<E> sorted(Comparator<? super E> comparator)
    {
        return this.resolvedStream()
                   .sorted(comparator);
    }

    @Override
    public Stream<E> peek(Consumer<? super E> action)
    {
        return this.resolvedStream()
                   .peek(action);
    }

    @Override
    public Stream<E> limit(long maxSize)
    {
        return this.modifiedStream(stream -> stream.limit(maxSize));
    }

    @Override
    public Stream<E> skip(long n)
    {
        long accumulatedSkipOffset = this.skipOffset + n;
        if (this.skipObserver != null)
        {
            this.skipObserver.accept(accumulatedSkipOffset);
        }
        return new StreamDecoratorLazyLoading<>(this.stream.skip(n), this.skipObserver, accumulatedSkipOffset);
    }

    @Override
    public void forEach(Consumer<? super E> action)
    {
        this.resolvedStream()
            .forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super E> action)
    {
        this.resolvedStream()
            .forEachOrdered(action);
    }

    @Override
    public Object[] toArray()
    {
        return this.resolvedStream()
                   .toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator)
    {
        return this.resolvedStream()
                   .toArray(generator);
    }

    @Override
    public E reduce(E identity, BinaryOperator<E> accumulator)
    {
        return this.resolvedStream()
                   .reduce(identity, accumulator);
    }

    @Override
    public Optional<E> reduce(BinaryOperator<E> accumulator)
    {
        return this.resolvedStream()
                   .reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner)
    {
        return this.resolvedStream()
                   .reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner)
    {
        return this.resolvedStream()
                   .collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super E, A, R> collector)
    {
        return this.resolvedStream()
                   .collect(collector);
    }

    @Override
    public Optional<E> min(Comparator<? super E> comparator)
    {
        return this.resolvedStream()
                   .min(comparator);
    }

    @Override
    public Optional<E> max(Comparator<? super E> comparator)
    {
        return this.resolvedStream()
                   .max(comparator);
    }

    @Override
    public long count()
    {
        return this.stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super E> predicate)
    {
        return this.resolvedStream()
                   .anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super E> predicate)
    {
        return this.resolvedStream()
                   .allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super E> predicate)
    {
        return this.resolvedStream()
                   .noneMatch(predicate);
    }

    @Override
    public Optional<E> findFirst()
    {
        return this.resolvedStream()
                   .findFirst();
    }

    @Override
    public Optional<E> findAny()
    {
        return this.resolvedStream()
                   .findAny();
    }

    @Override
    public Stream<E> takeWhile(Predicate<? super E> predicate)
    {
        return this.modifiedStream(stream -> stream.takeWhile(supplier -> predicate.test(supplier.get())));
    }

    @Override
    public Stream<E> dropWhile(Predicate<? super E> predicate)
    {
        return this.modifiedStream(stream -> stream.dropWhile(supplier -> predicate.test(supplier.get())));
    }

}
