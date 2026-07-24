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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.omnaest.utils.StreamUtils.Drainage;
import org.omnaest.utils.StreamUtils.Page;
import org.omnaest.utils.StreamUtils.SplittedStream;
import org.omnaest.utils.StreamUtils.StreamablePage;
import org.omnaest.utils.StreamUtils.TerminationSignal;
import org.omnaest.utils.StreamUtils.UnaryMergeEntry;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.bi.IntUnaryBiElement;
import org.omnaest.utils.element.lar.LeftAndRight;

public class StreamUtilsTest
{

    @Test
    public void testFromSupplier() throws Exception
    {
        List<String> source = Arrays.asList("1", "2", "3");
        Iterator<String> iterator = source.iterator();
        List<String> result = StreamUtils.fromSupplier(() -> iterator.hasNext() ? iterator.next() : null, e -> e == null)
                                         .collect(Collectors.toList());

        assertEquals(source, result);
    }

    @Test
    public void testFromIterator() throws Exception
    {
        Iterator<String> iterator = Arrays.asList("1", "2", "3")
                                          .iterator();

        List<String> collect = StreamUtils.fromIterator(iterator)
                                          .limit(1)
                                          .collect(Collectors.toList());
        List<String> rest = StreamUtils.fromIterator(iterator)
                                       .collect(Collectors.toList());

        assertEquals(1, collect.size());
        assertEquals(2, rest.size());
    }

    @Test
    public void testFromIteratorFlatMap() throws Exception
    {
        AtomicInteger counter = new AtomicInteger();
        Stream<String> stream = Arrays.asList(new String[] { "1", "2" }, new String[] { "3", "4" })
                                      .stream()
                                      .map(Arrays::asList)
                                      .map(List::iterator)
                                      .map(iterator -> StreamUtils.fromIterator(iterator))
                                      .flatMap(iStream -> iStream.peek(value -> counter.getAndIncrement()));

        List<String> collect = stream.limit(1)
                                     .collect(Collectors.toList());

        assertEquals(1, collect.size());
        assertEquals(true, counter.get() <= 2);
        assertEquals(true, counter.get() >= 1);
    }

    @Test
    public void testConcat() throws Exception
    {
        assertEquals(Arrays.asList("1", "2", "3", "4"), StreamUtils.concat(Arrays.asList(Arrays.asList("1", "2")
                                                                                               .stream(),
                                                                                         Arrays.asList("3", "4")
                                                                                               .stream())
                                                                                 .stream())
                                                                   .collect(Collectors.toList()));
        assertEquals(Arrays.asList("1", "2", "3", "4"), StreamUtils.concat(Arrays.asList("1", "2")
                                                                                 .stream(),
                                                                           Arrays.asList("3", "4")
                                                                                 .stream())
                                                                   .collect(Collectors.toList()));
    }

    @Test
    public void testReverse() throws Exception
    {
        assertEquals(Arrays.asList("c", "b", "a"), StreamUtils.reverse(Arrays.asList("a", "b", "c")
                                                                             .stream())
                                                              .collect(Collectors.toList()));
    }

    @Test
    public void testFromInputStream() throws Exception
    {
        assertEquals(Arrays.asList("one", "two"), StreamUtils.fromReaderAsLines(new StringReader("one\ntwo"))
                                                             .collect(Collectors.toList()));
    }

    @Test
    public void testDrain() throws Exception
    {
        Drainage<String> drainage = StreamUtils.drain(Arrays.asList("1", "2", "3")
                                                            .stream(),
                                                      e -> e.equals("2"));
        assertEquals(Arrays.asList("1", "2"), drainage.getPrefetch()
                                                      .collect(Collectors.toList()));
        assertEquals(Arrays.asList("1", "2", "3"), drainage.getStreamIncludingPrefetch()
                                                           .collect(Collectors.toList()));

    }

    @Test
    public void testFramedPreserveSizePreserveSize() throws Exception
    {
        {
            List<String[]> frames = StreamUtils.framedPreserveSize(3, Arrays.asList("1", "2", "3", "4", "5", "6")
                                                                            .stream())
                                               .collect(Collectors.toList());
            assertEquals(2, frames.size());
            assertArrayEquals(new String[] { "1", "2", "3" }, frames.get(0));
            assertArrayEquals(new String[] { "4", "5", "6" }, frames.get(1));
        }
        {
            List<String[]> frames = StreamUtils.framedPreserveSize(3, Arrays.asList("1", null, "3", "4", "5")
                                                                            .stream())
                                               .collect(Collectors.toList());
            assertEquals(2, frames.size());
            assertArrayEquals(new String[] { "1", null, "3" }, frames.get(0));
            assertArrayEquals(new String[] { "4", "5", null }, frames.get(1));
        }
    }

    @Test
    public void testFramedPreserveSize() throws Exception
    {
        {
            List<String[]> frames = StreamUtils.framed(3, Arrays.asList("1", "2", "3", "4", "5", "6")
                                                                .stream())
                                               .collect(Collectors.toList());
            assertEquals(2, frames.size());
            assertArrayEquals(new String[] { "1", "2", "3" }, frames.get(0));
            assertArrayEquals(new String[] { "4", "5", "6" }, frames.get(1));
        }
        {
            List<String[]> frames = StreamUtils.framed(3, Arrays.asList("1", null, "3", "4", "5")
                                                                .stream())
                                               .collect(Collectors.toList());
            assertEquals(2, frames.size());
            assertArrayEquals(new String[] { "1", null, "3" }, frames.get(0));
            assertArrayEquals(new String[] { "4", "5" }, frames.get(1));
        }
    }

    @Test
    public void testFramedPreserveSizeAsList() throws Exception
    {
        {
            List<List<String>> frames = StreamUtils.framedAsList(3, Arrays.asList("1", null, "3", "4", "5")
                                                                          .stream())
                                                   .collect(Collectors.toList());
            assertEquals(2, frames.size());
            assertEquals(Arrays.asList("1", null, "3"), frames.get(0));
            assertEquals(Arrays.asList("4", "5"), frames.get(1));
        }
        {
            List<List<String>> frames = StreamUtils.framedAsList(6, Arrays.asList("1", null, "3", "4", "5")
                                                                          .stream())
                                                   .collect(Collectors.toList());
            assertEquals(1, frames.size());
            assertEquals(Arrays.asList("1", null, "3", "4", "5"), frames.get(0));
        }
        {
            List<List<String>> frames = StreamUtils.framedAsList(3, (Stream<String>) null)
                                                   .collect(Collectors.toList());
            assertEquals(0, frames.size());
        }
    }

    @Test
    public void testFramedPreserveSizeAsListNonNull() throws Exception
    {
        {
            List<List<String>> frames = StreamUtils.framedNonNullAsList(3, Arrays.asList("1", null, "3", "4", "5")
                                                                                 .stream())
                                                   .collect(Collectors.toList());
            assertEquals(2, frames.size());
            assertEquals(Arrays.asList("1", "3"), frames.get(0));
            assertEquals(Arrays.asList("4", "5"), frames.get(1));
        }
        {
            List<List<String>> frames = StreamUtils.framedNonNullAsList(6, Arrays.asList("1", null, "3", "4", "5")
                                                                                 .stream())
                                                   .collect(Collectors.toList());
            assertEquals(1, frames.size());
            assertEquals(Arrays.asList("1", "3", "4", "5"), frames.get(0));
        }
        {
            List<List<String>> frames = StreamUtils.framedNonNullAsList(3, (Stream<String>) null)
                                                   .collect(Collectors.toList());
            assertEquals(0, frames.size());
        }
    }

    @Test
    public void testFramedIntIntStream()
    {
        assertEquals(List.of(List.of(0, 1, 2), List.of(3, 4, 5), List.of(6, 7)), StreamUtils.framed(3, IntStream.range(0, 8))
                                                                                            .map(org.apache.commons.lang3.ArrayUtils::toObject)
                                                                                            .map(Arrays::asList)
                                                                                            .toList());
    }

    @Test
    public void testMerge() throws Exception
    {
        assertEquals("A1B2C", StreamUtils.merge(Arrays.asList("A", "B", "C")
                                                      .stream(),
                                                Arrays.asList("1", "2")
                                                      .stream())
                                         .flatMap(lar -> Stream.of(lar.getLeft(), lar.getRight()))
                                         .filter(value -> value != null)
                                         .collect(Collectors.joining()));
    }

    @Test
    public void testChop() throws Exception
    {
        assertEquals("1,234", StreamUtils.chop(Arrays.asList("1", "2", "3", "4")
                                                     .stream(),
                                               e -> "2".equals(e))
                                         .map(chunk -> chunk.stream()
                                                            .collect(Collectors.joining()))
                                         .collect(Collectors.joining(",")));
    }

    @Test
    public void testWindowed() throws Exception
    {
        {
            List<String> windows = StreamUtils.windowed(Arrays.asList("1", "2", "3", "4")
                                                              .stream(),
                                                        1, 1)
                                              .map(window -> window.getAll()
                                                                   .stream()
                                                                   .collect(Collectors.joining()))
                                              .collect(Collectors.toList());
            assertEquals(4, windows.size());
            assertEquals("12", windows.get(0));
            assertEquals("123", windows.get(1));
            assertEquals("234", windows.get(2));
            assertEquals("34", windows.get(3));
        }
        {
            List<String> windows = StreamUtils.windowed(Arrays.asList("1", "2", "3", "4", "5")
                                                              .stream(),
                                                        1, 1)
                                              .map(window -> window.getAll()
                                                                   .stream()
                                                                   .collect(Collectors.joining()))
                                              .collect(Collectors.toList());
            assertEquals(5, windows.size());
            assertEquals("12", windows.get(0));
            assertEquals("123", windows.get(1));
            assertEquals("234", windows.get(2));
            assertEquals("345", windows.get(3));
            assertEquals("45", windows.get(4));
        }
        {
            List<String> windows = StreamUtils.windowed(Arrays.asList("1", "2", "3", "4", "5")
                                                              .stream(),
                                                        0, 2)
                                              .map(window -> window.getAll()
                                                                   .stream()
                                                                   .collect(Collectors.joining()))
                                              .collect(Collectors.toList());
            assertEquals(5, windows.size());
            assertEquals("123", windows.get(0));
            assertEquals("234", windows.get(1));
            assertEquals("345", windows.get(2));
            assertEquals("45", windows.get(3));
            assertEquals("5", windows.get(4));
        }
        {
            List<String> windows = StreamUtils.windowed(Arrays.asList("1", "2", "3", "4", "5")
                                                              .stream(),
                                                        2, 0)
                                              .map(window -> window.getAll()
                                                                   .stream()
                                                                   .collect(Collectors.joining()))
                                              .collect(Collectors.toList());
            assertEquals(5, windows.size());
            assertEquals("1", windows.get(0));
            assertEquals("12", windows.get(1));
            assertEquals("123", windows.get(2));
            assertEquals("234", windows.get(3));
            assertEquals("345", windows.get(4));
        }

        //with step
        {
            List<String> windows = StreamUtils.windowed(Arrays.asList("1", "2", "3", "4", "5")
                                                              .stream(),
                                                        1, 1, 2)
                                              .map(window -> window.getAll()
                                                                   .stream()
                                                                   .collect(Collectors.joining()))
                                              .collect(Collectors.toList());
            assertEquals(3, windows.size());
            assertEquals("12", windows.get(0));
            assertEquals("234", windows.get(1));
            assertEquals("45", windows.get(2));
        }
    }

    @Test
    public void testFromStreamSupplier() throws Exception
    {
        List<List<String>> sourceList = Arrays.asList(Arrays.asList("1", "2"), Arrays.asList("1"))
                                              .stream()
                                              .collect(Collectors.toList());
        List<String> list = StreamUtils.fromStreamSupplier(() -> !sourceList.isEmpty() ? sourceList.remove(0)
                                                                                                   .stream()
                : null)
                                       .collect(Collectors.toList());
        assertEquals(Arrays.asList("1", "2", "1"), list);
    }

    @Test
    public void testRemoveHeadStream() throws Exception
    {
        List<String> sourceList = Arrays.asList("a", "b", "c")
                                        .stream()
                                        .collect(Collectors.toList());
        List<String> result = StreamUtils.removeStream(sourceList)
                                         .collect(Collectors.toList());
        assertEquals(Arrays.asList("a", "b", "c"), result);
        assertTrue(sourceList.isEmpty());
    }

    @Test
    public void testWithCounter()
    {
        List<String> elements = StreamUtils.withIntCounter(Arrays.asList("a", "b")
                                                                 .stream())
                                           .map(be -> be.getFirst() + be.getSecond())
                                           .collect(Collectors.toList());
        assertEquals("a0", elements.get(0));
        assertEquals("b1", elements.get(1));
    }

    @Test
    public void testLast() throws Exception
    {
        assertEquals(2, StreamUtils.last(Arrays.asList(1, 2)
                                               .stream())
                                   .intValue());
    }

    @Test
    public void testParallel()
    {
        Set<String> result = StreamUtils.parallel(IntStream.range(0, 1000)
                                                           .boxed(),
                                                  i -> "value" + i)
                                        .collect(Collectors.toSet());

        assertEquals(result.size(), 1000);
        assertTrue(result.contains("value0"));
        assertTrue(result.contains("value999"));
    }

    @Test
    public void testBuilder() throws Exception
    {
        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f", "g"), StreamUtils.builder()
                                                                                  .add("a")
                                                                                  .addAll("b", "c")
                                                                                  .addAll(Arrays.asList("d", "e"))
                                                                                  .addAll(Arrays.asList("f", "g")
                                                                                                .stream())
                                                                                  .build()
                                                                                  .collect(Collectors.toList()));
    }

    @Test
    public void testGenerate() throws Exception
    {
        assertEquals(Arrays.asList(0, 5, 10, 15), StreamUtils.generate()
                                                             .intStream()
                                                             .unlimited(5)
                                                             .limit(4)
                                                             .mapToObj(v -> v)
                                                             .collect(Collectors.toList()));

        assertEquals(Arrays.asList(0, 1, 2, 3), StreamUtils.generate()
                                                           .intStream()
                                                           .unlimitedWithTerminationPredicate(ii -> ii > 3)
                                                           .mapToObj(v -> v)
                                                           .collect(Collectors.toList()));

        assertEquals(Arrays.asList(IntUnaryBiElement.of(0, 0), IntUnaryBiElement.of(0, 1), IntUnaryBiElement.of(1, 0), IntUnaryBiElement.of(1, 1)),
                     StreamUtils.generate()
                                .biIntStream()
                                .withLeftSide(0, 2)
                                .withRightSide(0, 2)
                                .collect(Collectors.toList()));
    }

    @Test
    public void testGenerateRandomNumbers() throws Exception
    {
        List<Integer> numbers = StreamUtils.generate()
                                           .intStream()
                                           .unlimited()
                                           .withRandomNumbers(10)
                                           .limit(50)
                                           .boxed()
                                           .collect(Collectors.toList());
        assertEquals(50, numbers.size());
        assertTrue(numbers.stream()
                          .allMatch(value -> value >= 0 && value <= 10));
        assertTrue(numbers.stream()
                          .distinct()
                          .count() > 1);
    }

    @Test
    public void testWithFilterAllOnAnyFilterFails() throws Exception
    {
        assertEquals(Arrays.asList(true, true), StreamUtils.withFilterAllOnAnyFilterFails(Arrays.asList(true, true, false, true, true)
                                                                                                .stream())
                                                           .filter(value -> value)
                                                           .collect(Collectors.toList()));
    }

    @Test
    public void testMergeOrderedAndSynchronize() throws Exception
    {
        List<LeftAndRight<Integer, Integer>> result = StreamUtils.mergeOrderedAndSynchronize(Arrays.asList(1, 2, 3)
                                                                                                   .stream(),
                                                                                             Arrays.asList(2, 4)
                                                                                                   .stream())
                                                                 .collect(Collectors.toList());
        assertEquals(4, result.size());
    }

    @Test
    public void testAggregate() throws Exception
    {
        Stream<List<String>> aggregate = StreamUtils.aggregate(Arrays.asList("a", "b", "x", "c")
                                                                     .stream(),
                                                               StringUtils.equalsAnyFilter("a", "c"), StringUtils.equalsAnyFilter("b"),
                                                               group -> Stream.of(group.collect(Collectors.toList())));
        assertEquals(Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c")), aggregate.collect(Collectors.toList()));
    }

    @Test
    public void testRedundant() throws Exception
    {
        assertEquals(Arrays.asList("a", "ab"), Stream.of("a")
                                                     .flatMap(StreamUtils.redundant(element -> element, element -> element + "b"))
                                                     .collect(Collectors.toList()));
    }

    @Test
    public void testSplitOne() throws Exception
    {
        {
            BiElement<Optional<String>, Stream<String>> oneAndRest = StreamUtils.splitOne(Arrays.asList("a", "b", "c")
                                                                                                .stream());
            assertEquals("a", oneAndRest.getFirst()
                                        .get());
            assertEquals(Arrays.asList("b", "c"), oneAndRest.getSecond()
                                                            .collect(Collectors.toList()));
        }
        {
            BiElement<Optional<String>, Stream<String>> oneAndRest = StreamUtils.splitOne(null);
            assertEquals(false, oneAndRest.getFirst()
                                          .isPresent());
            assertEquals(Collections.emptyList(), oneAndRest.getSecond()
                                                            .collect(Collectors.toList()));
        }
    }

    @Test
    public void testRecursiveFlattened() throws Exception
    {
        assertEquals(Arrays.asList(1, 2, 3, 11, 12, 13, 101, 102, 103), StreamUtils.recursiveFlattened(Arrays.asList(1, 11, 101)
                                                                                                             .stream(),
                                                                                                       value -> value % 10 <= 2 ? Stream.of(value + 1)
                                                                                                               : Stream.empty())
                                                                                   .collect(Collectors.toList()));
    }

    @Test
    public void testFilterAndConsume() throws Exception
    {
        List<String> consumedElements = new ArrayList<>();
        List<String> filteredElements = StreamUtils.filterAndConsume(Arrays.asList("a", "b", "B", "c")
                                                                           .stream(),
                                                                     value -> value.toLowerCase()
                                                                                   .equals("b"),
                                                                     value -> consumedElements.add(value))
                                                   .collect(Collectors.toList());
        assertEquals(Arrays.asList("a", "c"), filteredElements);
        assertEquals(Arrays.asList("b", "B"), consumedElements);
    }

    @Test
    public void testCartesianProductOf() throws Exception
    {
        List<BiElement<String, String>> product = StreamUtils.cartesianProductOf(Arrays.asList("a", "b")
                                                                                       .stream(),
                                                                                 Arrays.asList("1", "2")
                                                                                       .stream())
                                                             .collect(Collectors.toList());
        assertEquals(4, product.size());
        assertEquals("a1,a2,b1,b2", product.stream()
                                           .map(bi -> bi.getFirst() + bi.getSecond())
                                           .collect(Collectors.joining(",")));
    }

    @Test
    public void testEnsureNumberOfElements() throws Exception
    {
        assertEquals(Arrays.asList("0"), StreamUtils.ensureNumberOfElements(Stream.empty(), 1, index -> "" + index)
                                                    .collect(Collectors.toList()));
        assertEquals(Arrays.asList("0", "1"), StreamUtils.ensureNumberOfElements(Stream.empty(), 2, index -> "" + index)
                                                         .collect(Collectors.toList()));
        assertEquals(Arrays.asList("A"), StreamUtils.ensureNumberOfElements(Stream.of("A"), 1, index -> "" + index)
                                                    .collect(Collectors.toList()));
        assertEquals(Arrays.asList("A", "0"), StreamUtils.ensureNumberOfElements(Stream.of("A"), 2, index -> "" + index)
                                                         .collect(Collectors.toList()));
        assertEquals(Collections.emptyList(), StreamUtils.ensureNumberOfElements(Stream.empty(), 0, index -> "" + index)
                                                         .collect(Collectors.toList()));
    }

    @Test
    public void testUntil() throws Exception
    {
        assertEquals(Arrays.asList(0, 1), StreamUtils.generate()
                                                     .intStream()
                                                     .unlimited()
                                                     .until(i -> i == 2)
                                                     .boxed()
                                                     .collect(Collectors.toList()));
    }

    @Test
    public void testMerger() throws Exception
    {
        List<UnaryMergeEntry<String>> result = StreamUtils.merger()
                                                          .ofSorted()
                                                          .unary()
                                                          .withIdentityFunction(String.class, char.class, value -> value.charAt(0))
                                                          .withSourceStream(Stream.of("1a", "2a", "4a"))
                                                          .withSourceStream(Stream.of("1b", "2b", "3b"))
                                                          .merge()
                                                          .collect(Collectors.toList());

        assertEquals(Arrays.asList("1a1b", "2a2b", "3b", "4a"), result.stream()
                                                                      .map(entry -> entry.getFirst()
                                                                                         .orElse("")
                                                                              + entry.getSecond()
                                                                                     .orElse(""))
                                                                      .collect(Collectors.toList()));
    }

    @Test
    public void testTakeOptionalUntilEmpty() throws Exception
    {
        Iterator<String> iterator = Arrays.asList("a", "b")
                                          .iterator();
        assertEquals(Arrays.asList("a", "b"), StreamUtils.takeOptionalUntilEmpty(() -> Optional.of(iterator)
                                                                                               .filter(Iterator::hasNext)
                                                                                               .map(Iterator::next))
                                                         .collect(Collectors.toList()));
    }

    @Test
    public void testSplitByFilter() throws Exception
    {
        {
            SplittedStream<String> splittedStream = StreamUtils.splitByFilter(Arrays.asList("a", "b", "c")
                                                                                    .stream(),
                                                                              value -> org.apache.commons.lang3.StringUtils.equals(value, "b"));
            assertEquals(Arrays.asList("a", "c"), splittedStream.excluded()
                                                                .collect(Collectors.toList()));
            assertEquals(Arrays.asList("b"), splittedStream.included()
                                                           .collect(Collectors.toList()));
        }
        {
            SplittedStream<String> splittedStream = StreamUtils.splitByFilter(Arrays.asList("a", "b", "c")
                                                                                    .stream(),
                                                                              value -> org.apache.commons.lang3.StringUtils.equals(value, "b"));
            assertEquals(Arrays.asList("b"), splittedStream.included()
                                                           .collect(Collectors.toList()));
            assertEquals(Arrays.asList("a", "c"), splittedStream.excluded()
                                                                .collect(Collectors.toList()));
        }
    }

    @Test
    public void testPipeline() throws Exception
    {
        assertEquals(Arrays.asList("ab"), StreamUtils.pipeline()
                                                     .source(Stream.of("a"))
                                                     .andSource(Stream.of("b"))
                                                     .combineAsOptionals((a, b) -> a.flatMap(ia -> b.map(ib -> ia + ib)))
                                                     .stream()
                                                     .collect(Collectors.toList()));
        assertEquals(Arrays.asList("ab"), StreamUtils.pipeline()
                                                     .sources(Stream.of("a"), Stream.of("b"))
                                                     .combine((a, b) -> a + b)
                                                     .stream()
                                                     .collect(Collectors.toList()));
        assertEquals(Arrays.asList("aa", "bb"), StreamUtils.pipeline()
                                                           .source(Stream.of("a", "b"))
                                                           .fork()
                                                           .combine((a, b) -> a + b)
                                                           .stream()
                                                           .collect(Collectors.toList()));
        assertEquals(Arrays.asList("a1a2", "b1b2"), StreamUtils.pipeline()
                                                               .seed(Stream.of("a", "b"))
                                                               .batch(2)
                                                               .sources(seeds -> seeds.map(a -> a + "1"), seeds -> seeds.map(b -> b + "2"))
                                                               .combine((a, b) -> a + b)
                                                               .stream()
                                                               .collect(Collectors.toList()));
    }

    @Test
    public void testMapWithPrevious()
    {
        BiFunction<String, Integer, String> mapper = (previous, value) -> Optional.ofNullable(previous)
                                                                                  .map(previousValue -> previousValue + "-")
                                                                                  .orElse("")
                + value;
        assertEquals(List.of("0", "0-1", "0-1-2"), StreamUtils.mapWithPrevious(IntStream.range(0, 3)
                                                                                        .boxed(),
                                                                               mapper)
                                                              .toList());
    }

    @Test
    public void testReduceWithPrevious()
    {
        BiFunction<String, Integer, String> mapper = (previous, value) -> Optional.ofNullable(previous)
                                                                                  .map(previousValue -> previousValue + "-")
                                                                                  .orElse("")
                + value;
        assertEquals("0-1-2", StreamUtils.reduceWithPrevious(IntStream.range(0, 3)
                                                                      .boxed(),
                                                             mapper)
                                         .get());
    }

    @Test
    public void testDistinctByPredicate() throws Exception
    {
        List<String> result = Stream.of("apple", "ant", "banana", "avocado")
                                    .filter(StreamUtils.distinctBy(s -> s.charAt(0)))
                                    .collect(Collectors.toList());
        assertEquals(Arrays.asList("apple", "banana"), result);
    }

    @Test
    public void testDistinctByStream() throws Exception
    {
        List<String> result = StreamUtils.distinctBy(Stream.of("apple", "ant", "banana", "avocado"), s -> s.charAt(0))
                                         .collect(Collectors.toList());
        assertEquals(Arrays.asList("apple", "banana"), result);
    }

    @Test
    public void testDistinctByAllUnique() throws Exception
    {
        List<String> result = Stream.of("apple", "banana", "cherry")
                                    .filter(StreamUtils.distinctBy(s -> s.charAt(0)))
                                    .collect(Collectors.toList());
        assertEquals(Arrays.asList("apple", "banana", "cherry"), result);
    }

    @Test
    public void testDistinctByAllSameKey() throws Exception
    {
        List<String> result = Stream.of("ant", "ape", "avocado")
                                    .filter(StreamUtils.distinctBy(s -> s.charAt(0)))
                                    .collect(Collectors.toList());
        assertEquals(Arrays.asList("ant"), result);
    }

    @Test
    public void testDistinctByNullStream() throws Exception
    {
        Stream<String> nullStream = null;
        List<String> result = StreamUtils.distinctBy(nullStream, s -> s.charAt(0))
                                         .collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDistinctByEmptyStream() throws Exception
    {
        List<String> result = StreamUtils.distinctBy(Stream.<String>empty(), s -> s.charAt(0))
                                         .collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDistinctByPreservesOrder() throws Exception
    {
        List<Integer> result = StreamUtils.distinctBy(Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3), i -> i)
                                          .collect(Collectors.toList());
        assertEquals(Arrays.asList(3, 1, 4, 5, 9, 2, 6), result);
    }

    @Test
    public void testEnumerate() throws Exception
    {
        // basic case: three elements get indices 0, 1, 2
        List<BiElement<Long, String>> result = StreamUtils.enumerate(Stream.of("a", "b", "c"))
                                                          .collect(Collectors.toList());
        assertEquals(3, result.size());
        assertEquals(Long.valueOf(0L), result.get(0)
                                             .getFirst());
        assertEquals("a", result.get(0)
                                .getSecond());
        assertEquals(Long.valueOf(1L), result.get(1)
                                             .getFirst());
        assertEquals("b", result.get(1)
                                .getSecond());
        assertEquals(Long.valueOf(2L), result.get(2)
                                             .getFirst());
        assertEquals("c", result.get(2)
                                .getSecond());

        // empty stream
        List<BiElement<Long, String>> empty = StreamUtils.enumerate(Stream.<String>empty())
                                                         .collect(Collectors.toList());
        assertTrue(empty.isEmpty());

        // null stream
        List<BiElement<Long, String>> nullResult = StreamUtils.enumerate((Stream<String>) null)
                                                              .collect(Collectors.toList());
        assertTrue(nullResult.isEmpty());

        // single element gets index 0
        List<BiElement<Long, String>> single = StreamUtils.enumerate(Stream.of("x"))
                                                          .collect(Collectors.toList());
        assertEquals(1, single.size());
        assertEquals(Long.valueOf(0L), single.get(0)
                                             .getFirst());
        assertEquals("x", single.get(0)
                                .getSecond());
    }

    @Test
    public void testInterleave() throws Exception
    {
        // basic: A longer than B → stops when B is exhausted
        assertEquals(Arrays.asList(1, 10, 2, 20), StreamUtils.interleave(Stream.of(1, 2, 3), Stream.of(10, 20))
                                                             .collect(Collectors.toList()));

        // equal length
        assertEquals(Arrays.asList(1, 10, 2, 20), StreamUtils.interleave(Stream.of(1, 2), Stream.of(10, 20))
                                                             .collect(Collectors.toList()));

        // A shorter than B → stops when A is exhausted
        assertEquals(Arrays.asList(1, 10), StreamUtils.interleave(Stream.of(1), Stream.of(10, 20, 30))
                                                      .collect(Collectors.toList()));

        // A empty → result is empty
        assertEquals(Arrays.asList(), StreamUtils.interleave(Stream.<Integer>empty(), Stream.of(10, 20))
                                                 .collect(Collectors.toList()));

        // B empty → result is empty
        assertEquals(Arrays.asList(), StreamUtils.interleave(Stream.of(1, 2), Stream.<Integer>empty())
                                                 .collect(Collectors.toList()));

        // null A → treated as empty
        assertEquals(Arrays.asList(), StreamUtils.interleave(null, Stream.of(10, 20))
                                                 .collect(Collectors.toList()));

        // null B → treated as empty
        assertEquals(Arrays.asList(), StreamUtils.interleave(Stream.of(1, 2), null)
                                                 .collect(Collectors.toList()));
    }

    @Test
    public void testFromPageProviderWithSinglePage()
    {
        assertEquals(List.of("A0:0"), StreamUtils.fromPageProvider()
                                                 .withPageSize(100)
                                                 .usingPageProvider(paging -> Page.of(List.of("A" + paging.getPageIndex() + ":" + paging.getStartIndex()),
                                                                                      true))
                                                 .asElements()
                                                 .toList());
    }

    @Test
    public void testFromPageProviderWithMultiplePages()
    {
        List<Integer> elements = IntStream.range(0, 10)
                                          .boxed()
                                          .toList();
        assertEquals(elements, StreamUtils.fromPageProvider()
                                          .withPageSize(3)
                                          .usingPageProvider(paging -> Page.of(elements.subList(paging.getStartIndex(),
                                                                                                Math.min(elements.size(),
                                                                                                         paging.getStopIndexExclusive())),
                                                                               paging.getStopIndexExclusive() >= elements.size()))
                                          .asElements()
                                          .toList());
    }

    @Test
    public void testFromPageProviderWithPageIndexBasedProvider()
    {
        List<Integer> elements = IntStream.range(0, 10)
                                          .boxed()
                                          .toList();
        assertEquals(elements, StreamUtils.fromPageProvider()
                                          .withPageSize(2)
                                          .usingPageProvider(paging -> Page.of(elements.subList(paging.getPageSize() * paging.getPageIndex(),
                                                                                                Math.min(elements.size(),
                                                                                                         paging.getStopIndexExclusive())),
                                                                               paging.getStopIndexExclusive() >= elements.size()))
                                          .asElements()
                                          .toList());
    }

    @Test
    public void testFromPageProviderWithSkippedPages()
    {
        List<Integer> elements = IntStream.range(0, 10)
                                          .boxed()
                                          .toList();
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(elements.subList(6, elements.size()), StreamUtils.fromPageProvider()
                                                                      .withPageSize(3)
                                                                      .usingPageProvider(paging ->
                                                                      {
                                                                          pageHits.add(paging.getPageIndex());
                                                                          return Page.of(elements.subList(paging.getStartIndex(),
                                                                                                          Math.min(elements.size(),
                                                                                                                   paging.getStopIndexExclusive())),
                                                                                         paging.getStopIndexExclusive() >= elements.size());
                                                                      })
                                                                      .stream()
                                                                      .skip(2)
                                                                      .limit(2)
                                                                      .flatMap(StreamablePage::stream)
                                                                      .toList());
        // the skipped pages are not retrieved, since a page is only resolved when the consumer accesses it
        assertEquals(List.of(2, 3), pageHits);
    }

    @Test
    public void testFromPageProviderWithSkippedElements()
    {
        List<Integer> elements = IntStream.range(0, 10)
                                          .boxed()
                                          .toList();
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(elements.subList(6, elements.size()), StreamUtils.fromPageProvider()
                                                                      .withPageSize(3)
                                                                      .usingPageProvider(paging ->
                                                                      {
                                                                          pageHits.add(paging.getPageIndex());
                                                                          return Page.of(elements.subList(paging.getStartIndex(),
                                                                                                          Math.min(elements.size(),
                                                                                                                   paging.getStopIndexExclusive())),
                                                                                         paging.getStopIndexExclusive() >= elements.size());
                                                                      })
                                                                      .asElements()
                                                                      .stream()
                                                                      .skip(6)
                                                                      .toList());
        // the skip moves the probe of the first resolved element to page 2, so the pages 0 and 1 are not retrieved at all
        assertEquals(List.of(2, 3), pageHits);
    }

    @Test
    public void testFromPageProviderWithPartiallyFilledPages()
    {
        List<Page<Object>> pages = List.of(Page.of(List.of("A", "B"), false), Page.of(List.of("C", "D", "E"), true));
        assertEquals(List.of("A", "B", "C", "D", "E"), StreamUtils.fromPageProvider()
                                                                  .withPageSize(100)
                                                                  .usingPageProvider(paging -> pages.get(paging.getPageIndex()))
                                                                  .asElements()
                                                                  .toList());
    }

    @Test
    public void testFromPageProviderWithDeterminedLastPage()
    {
        // a partially filled page is the last page
        List<Integer> elements = IntStream.range(0, 5)
                                          .boxed()
                                          .toList();
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(elements, StreamUtils.fromPageProvider()
                                          .withPageSize(3)
                                          .usingPageProvider(paging ->
                                          {
                                              pageHits.add(paging.getPageIndex());
                                              return Page.of(elements.subList(Math.min(elements.size(), paging.getStartIndex()),
                                                                              Math.min(elements.size(), paging.getStopIndexExclusive())));
                                          })
                                          .asElements()
                                          .toList());
        assertEquals(List.of(0, 1), pageHits);
    }

    @Test
    public void testFromPageProviderWithDeterminedLastPageOnExactPageSizeMultiple()
    {
        // the element count is an exact multiple of the page size, so the trailing empty page determines the end and must not emit an element
        List<Integer> elements = IntStream.range(0, 6)
                                          .boxed()
                                          .toList();
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(elements, StreamUtils.fromPageProvider()
                                          .withPageSize(3)
                                          .usingPageProvider(paging ->
                                          {
                                              pageHits.add(paging.getPageIndex());
                                              return Page.of(elements.subList(Math.min(elements.size(), paging.getStartIndex()),
                                                                              Math.min(elements.size(), paging.getStopIndexExclusive())));
                                          })
                                          .asElements()
                                          .toList());
        assertEquals(List.of(0, 1, 2), pageHits);
    }

    @Test
    public void testFromPageProviderWithDeterminedLastPageAndWithoutAnyElement()
    {
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(List.of(), StreamUtils.fromPageProvider()
                                           .withPageSize(3)
                                           .usingPageProvider(paging ->
                                           {
                                               pageHits.add(paging.getPageIndex());
                                               return Page.of(List.of());
                                           })
                                           .asElements()
                                           .toList());
        assertEquals(List.of(0), pageHits);
    }

    @Test
    public void testFromPageProviderWithDeterminedLastPageAndSkippedElements()
    {
        List<Integer> elements = IntStream.range(0, 10)
                                          .boxed()
                                          .toList();
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(List.of(6, 7, 8, 9), StreamUtils.fromPageProvider()
                                                     .withPageSize(3)
                                                     .usingPageProvider(paging ->
                                                     {
                                                         pageHits.add(paging.getPageIndex());
                                                         return Page.of(elements.subList(Math.min(elements.size(), paging.getStartIndex()),
                                                                                         Math.min(elements.size(), paging.getStopIndexExclusive())));
                                                     })
                                                     .asElements()
                                                     .stream()
                                                     .skip(6)
                                                     .toList());
        assertEquals(List.of(2, 3), pageHits);
    }

    @Test
    public void testFromPageProviderWithSkipBeyondTheLastElement()
    {
        List<Integer> elements = IntStream.range(0, 10)
                                          .boxed()
                                          .toList();
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(List.of(), StreamUtils.fromPageProvider()
                                           .withPageSize(3)
                                           .usingPageProvider(paging ->
                                           {
                                               pageHits.add(paging.getPageIndex());
                                               return Page.of(elements.subList(Math.min(elements.size(), paging.getStartIndex()),
                                                                               Math.min(elements.size(), paging.getStopIndexExclusive())),
                                                              paging.getStopIndexExclusive() >= elements.size());
                                           })
                                           .asElements()
                                           .stream()
                                           .skip(100)
                                           .toList());
        // only the page of the probed anchor element is retrieved, no element is emitted
        assertEquals(List.of(33), pageHits);
    }

    @Test
    public void testFromPageProviderWithoutAnyElement()
    {
        List<Integer> pageHits = new ArrayList<>();
        assertEquals(List.of(), StreamUtils.fromPageProvider()
                                           .withPageSize(3)
                                           .usingPageProvider(paging ->
                                           {
                                               pageHits.add(paging.getPageIndex());
                                               return Page.of(List.of(), true);
                                           })
                                           .asElements()
                                           .toList());
        assertEquals(List.of(0), pageHits);
    }

    @Test
    public void testTakeUntilLastElementIncluded()
    {
        assertEquals(List.of("A", "B"),
                     StreamUtils.takeUntilLastElementIncluded(Stream.of("A", "B", "C"), element -> org.apache.commons.lang3.StringUtils.equals("B", element))
                                .toList());
        assertEquals(List.of("A", "B"),
                     StreamUtils.takeUntilLastElementIncluded(Stream.of("A", "B"), element -> org.apache.commons.lang3.StringUtils.equals("B", element))
                                .toList());
        assertEquals(List.of("A"),
                     StreamUtils.takeUntilLastElementIncluded(Stream.of("A"), element -> org.apache.commons.lang3.StringUtils.equals("B", element))
                                .toList());
        assertEquals(List.of(), StreamUtils.takeUntilLastElementIncluded(Stream.<String>empty(), element -> true)
                                           .toList());
        assertEquals(List.of(), StreamUtils.<String>takeUntilLastElementIncluded(null, element -> true)
                                           .toList());
        assertEquals(List.of("A", "B"), StreamUtils.takeUntilLastElementIncluded(Stream.of("A", "B"), null)
                                                   .toList());
    }

    @Test
    public void testTakeUntilInclusive()
    {
        assertEquals(List.of("A", "B"), StreamUtils.takeUntilInclusive(Stream.of("A", "B", "C"), "B"::equals)
                                                   .toList());
        assertEquals(List.of("A", "B"), StreamUtils.takeUntilInclusive(Stream.of("A", "B"), "B"::equals)
                                                   .toList());
        assertEquals(List.of("A"), StreamUtils.takeUntilInclusive(Stream.of("A"), "B"::equals)
                                              .toList());
        assertEquals(List.of("B"), StreamUtils.takeUntilInclusive(Stream.of("B", "A"), "B"::equals)
                                              .toList());
        assertEquals(List.of(), StreamUtils.takeUntilInclusive(Stream.<String>empty(), "B"::equals)
                                           .toList());
    }

    @Test
    public void testTakeUntilExclusive()
    {
        assertEquals(List.of("A"), StreamUtils.takeUntilExclusive(Stream.of("A", "B", "C"), "B"::equals)
                                              .toList());
        assertEquals(List.of("A"), StreamUtils.takeUntilExclusive(Stream.of("A", "B"), "B"::equals)
                                              .toList());
        assertEquals(List.of("A"), StreamUtils.takeUntilExclusive(Stream.of("A"), "B"::equals)
                                              .toList());
        assertEquals(List.of(), StreamUtils.takeUntilExclusive(Stream.of("B", "A"), "B"::equals)
                                           .toList());
        assertEquals(List.of(), StreamUtils.takeUntilExclusive(Stream.<String>empty(), "B"::equals)
                                           .toList());
        assertEquals(List.of(0, 1, 2, 3, 4), StreamUtils.takeUntilExclusive(Stream.iterate(0, element -> element + 1), element -> element >= 5)
                                                        .toList());
        assertEquals(List.of(0, 1, 2, 3, 4), StreamUtils.takeUntilExclusive(IntStream.range(0, 2000)
                                                                                     .boxed(),
                                                                            element -> element >= 5)
                                                        .parallel()
                                                        .toList());
    }

    @Test
    public void testSequentialized()
    {
        assertEquals(List.of("A", "B", "C"), StreamUtils.sequentialized(Stream.of("A", "B", "C"))
                                                        .toList());
        assertEquals(List.of(), StreamUtils.sequentialized(null)
                                           .toList());

        // a sorted source must not break on the missing comparator
        assertEquals(List.of(0, 1, 2), StreamUtils.sequentialized(IntStream.range(0, 3)
                                                                           .boxed()
                                                                           .sorted())
                                                  .toList());

        // the size stays known, so a count does not need to traverse the stream
        assertEquals(1000, StreamUtils.sequentialized(IntStream.range(0, 1000)
                                                               .boxed())
                                      .count());

        // a downstream parallel call must neither split the source nor break consumer side state
        List<Integer> encounterOrder = Collections.synchronizedList(new ArrayList<>());
        StreamUtils.sequentialized(IntStream.range(0, 1000)
                                            .boxed())
                   .parallel()
                   .forEach(encounterOrder::add);
        assertEquals(IntStream.range(0, 1000)
                              .boxed()
                              .toList(),
                     encounterOrder);
    }

    @Test
    public void testSequentializedClosesSourceStream()
    {
        AtomicBoolean closed = new AtomicBoolean(false);
        try (Stream<String> stream = StreamUtils.sequentialized(Stream.of("A", "B")
                                                                      .onClose(() -> closed.set(true))))
        {
            assertEquals(List.of("A", "B"), stream.toList());
        }
        assertTrue(closed.get());
    }

    @Test
    public void testTakeUntilObservedTermination()
    {
        // an element which is resolved by the consumer terminates the stream
        {
            List<Integer> resolvedIndexes = new ArrayList<>();
            Stream<Supplier<Integer>> source = Stream.iterate(0, index -> index + 1)
                                                     .map(index -> () ->
                                                     {
                                                         resolvedIndexes.add(index);
                                                         return index;
                                                     });
            BiFunction<Supplier<Integer>, TerminationSignal, Supplier<Integer>> decorator = (supplier, signal) -> () ->
            {
                Integer value = supplier.get();
                signal.terminateIf(value >= 3);
                return value;
            };
            assertEquals(List.of(0, 1, 2, 3), StreamUtils.takeUntilObservedTermination(source, decorator)
                                                         .map(Supplier::get)
                                                         .toList());
            assertEquals(List.of(0, 1, 2, 3), resolvedIndexes);
        }

        // an element which is skipped is never resolved, so the skipped elements do not cost anything
        {
            List<Integer> resolvedIndexes = new ArrayList<>();
            Stream<Supplier<Integer>> source = Stream.iterate(0, index -> index + 1)
                                                     .map(index -> () ->
                                                     {
                                                         resolvedIndexes.add(index);
                                                         return index;
                                                     });
            BiFunction<Supplier<Integer>, TerminationSignal, Supplier<Integer>> decorator = (supplier, signal) -> () ->
            {
                Integer value = supplier.get();
                signal.terminateIf(value >= 3);
                return value;
            };
            assertEquals(List.of(2, 3), StreamUtils.takeUntilObservedTermination(source, decorator)
                                                   .skip(2)
                                                   .map(Supplier::get)
                                                   .toList());
            assertEquals(List.of(2, 3), resolvedIndexes);
        }

        // a consumer which never resolves an element relies on its own short circuiting
        assertEquals(3, StreamUtils.takeUntilObservedTermination(Stream.iterate(0, index -> index + 1), (index, signal) -> index)
                                   .limit(3)
                                   .toList()
                                   .size());

        assertEquals(List.of(), StreamUtils.takeUntilObservedTermination(Stream.<String>empty(), (element, signal) -> element)
                                           .toList());
        assertEquals(List.of("A", "B"), StreamUtils.takeUntilObservedTermination(Stream.of("A", "B"), null)
                                                   .toList());
    }

    @Test
    public void testTakeUntilLastElementIncludedWithParallelStream()
    {
        assertEquals(List.of(0, 1, 2, 3, 4, 5), StreamUtils.takeUntilLastElementIncluded(IntStream.range(0, 2000)
                                                                                                  .boxed(),
                                                                                         element -> element >= 5)
                                                           .parallel()
                                                           .toList());
        assertEquals(List.of(0, 1, 2, 3, 4, 5), StreamUtils.takeUntilLastElementIncluded(IntStream.range(0, 2000)
                                                                                                  .boxed()
                                                                                                  .parallel(),
                                                                                         element -> element >= 5)
                                                           .toList());
    }

    @Test
    public void testTakeUntilLastElementIncludedWithInfiniteStream()
    {
        AtomicInteger sourceElementCounter = new AtomicInteger();
        AtomicInteger predicateCounter = new AtomicInteger();

        assertEquals(List.of(0, 1, 2), StreamUtils.takeUntilLastElementIncluded(Stream.iterate(0, element -> element + 1)
                                                                                      .peek(element -> sourceElementCounter.incrementAndGet()),
                                                                                element ->
                                                                                {
                                                                                    predicateCounter.incrementAndGet();
                                                                                    return element >= 2;
                                                                                })
                                                  .toList());
        assertEquals(3, sourceElementCounter.get());
        assertEquals(3, predicateCounter.get());
    }

    @Test
    public void testTakeUntilLastElementIncludedClosesSourceStream()
    {
        AtomicBoolean closed = new AtomicBoolean(false);
        try (Stream<String> stream = StreamUtils.takeUntilLastElementIncluded(Stream.of("A", "B", "C")
                                                                                    .onClose(() -> closed.set(true)),
                                                                              element -> org.apache.commons.lang3.StringUtils.equals("B", element)))
        {
            assertEquals(List.of("A", "B"), stream.toList());
        }
        assertTrue(closed.get());
    }

    @Test
    public void testLazyLoading()
    {
        assertEquals(List.of(0, 1, 2), StreamUtils.lazyLoading(Stream.of(() -> 0, () -> 1, () -> 2))
                                                  .toList());
        assertEquals(List.of(1), StreamUtils.lazyLoading(Stream.of(() -> 0, () -> 1, () -> 2))
                                            .skip(1)
                                            .limit(1)
                                            .toList());
        {
            AtomicInteger counter = new AtomicInteger(0);
            assertEquals(List.of(0, 1), StreamUtils.lazyLoading(IntStream.range(0, 10)
                                                                         .mapToObj(i -> () -> counter.getAndIncrement()))
                                                   .skip(5)
                                                   .limit(2)
                                                   .toList());
        }
    }

}
