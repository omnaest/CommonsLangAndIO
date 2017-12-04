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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
		return streams	.reduce(Stream::concat)
						.orElseGet(() -> Stream.empty());
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
				return terminateMatcher	.negate()
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
	 * this a TERMINAL operation!!
	 *
	 * @param stream
	 * @return
	 */
	public static <E> Stream<E> reverse(Stream<E> stream)
	{
		List<E> list = Optional	.ofNullable(stream)
								.orElse(Stream.empty())
								.collect(Collectors.toList());
		Collections.reverse(list);
		return list.stream();
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
			} catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
		}, line -> line == null).onClose(() ->
		{
			try
			{
				bufferedReader.close();
			} catch (IOException e)
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
		int characteristics = iterable	.spliterator()
										.characteristics();
		return StreamSupport.stream(supplier, characteristics, false);
	}

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
		Stream<E[]> retval = stream	.filter(filter)
									.map(mapper);
		return Stream.concat(retval, Stream	.of(1)
											.filter(n -> frame.get() != null)
											.map(n -> frame.getAndSet(null)));
	}

}
