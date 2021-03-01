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
public class StreamDecorator<E> implements Stream<E>
{
    private Stream<E> stream;

    public StreamDecorator(Stream<E> stream)
    {
        super();
        this.stream = stream;
    }

    protected void modifyStream(UnaryOperator<Stream<E>> modifier)
    {
        this.stream = modifier.apply(this.stream);
    }

    @Override
    public String toString()
    {
        return "StreamDecorator [stream=" + this.stream + "]";
    }

    @Override
    public Iterator<E> iterator()
    {
        return this.stream.iterator();
    }

    @Override
    public Spliterator<E> spliterator()
    {
        return this.stream.spliterator();
    }

    @Override
    public boolean isParallel()
    {
        return this.stream.isParallel();
    }

    @Override
    public Stream<E> sequential()
    {
        return this.stream.sequential();
    }

    @Override
    public Stream<E> parallel()
    {
        return this.stream.parallel();
    }

    @Override
    public Stream<E> unordered()
    {
        return this.stream.unordered();
    }

    @Override
    public Stream<E> onClose(Runnable closeHandler)
    {
        return this.stream.onClose(closeHandler);
    }

    @Override
    public void close()
    {
        this.stream.close();
    }

    @Override
    public Stream<E> filter(Predicate<? super E> predicate)
    {
        return this.stream.filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super E, ? extends R> mapper)
    {
        return this.stream.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super E> mapper)
    {
        return this.stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super E> mapper)
    {
        return this.stream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper)
    {
        return this.stream.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper)
    {
        return this.stream.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper)
    {
        return this.stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper)
    {
        return this.stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper)
    {
        return this.stream.flatMapToDouble(mapper);
    }

    @Override
    public Stream<E> distinct()
    {
        return this.stream.distinct();
    }

    @Override
    public Stream<E> sorted()
    {
        return this.stream.sorted();
    }

    @Override
    public Stream<E> sorted(Comparator<? super E> comparator)
    {
        return this.stream.sorted(comparator);
    }

    @Override
    public Stream<E> peek(Consumer<? super E> action)
    {
        return this.stream.peek(action);
    }

    @Override
    public Stream<E> limit(long maxSize)
    {
        return this.stream.limit(maxSize);
    }

    @Override
    public Stream<E> skip(long n)
    {
        return this.stream.skip(n);
    }

    @Override
    public void forEach(Consumer<? super E> action)
    {
        this.stream.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super E> action)
    {
        this.stream.forEachOrdered(action);
    }

    @Override
    public Object[] toArray()
    {
        return this.stream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator)
    {
        return this.stream.toArray(generator);
    }

    @Override
    public E reduce(E identity, BinaryOperator<E> accumulator)
    {
        return this.stream.reduce(identity, accumulator);
    }

    @Override
    public Optional<E> reduce(BinaryOperator<E> accumulator)
    {
        return this.stream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner)
    {
        return this.stream.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner)
    {
        return this.stream.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super E, A, R> collector)
    {
        return this.stream.collect(collector);
    }

    @Override
    public Optional<E> min(Comparator<? super E> comparator)
    {
        return this.stream.min(comparator);
    }

    @Override
    public Optional<E> max(Comparator<? super E> comparator)
    {
        return this.stream.max(comparator);
    }

    @Override
    public long count()
    {
        return this.stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super E> predicate)
    {
        return this.stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super E> predicate)
    {
        return this.stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super E> predicate)
    {
        return this.stream.noneMatch(predicate);
    }

    @Override
    public Optional<E> findFirst()
    {
        return this.stream.findFirst();
    }

    @Override
    public Optional<E> findAny()
    {
        return this.stream.findAny();
    }

}
