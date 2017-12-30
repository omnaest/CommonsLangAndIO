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

import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.omnaest.utils.StreamUtils.Drainage;

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
                                      .map(array -> Arrays.asList(array)
                                                          .iterator())
                                      .flatMap(iterator -> StreamUtils.fromIterator(iterator)
                                                                      .peek(value -> counter.getAndIncrement()));

        List<String> collect = stream.limit(1)
                                     .collect(Collectors.toList());

        assertEquals(1, collect.size());
        assertEquals(2, counter.get());
    }

    @Test
    public void testConcat() throws Exception
    {
        List<String> collect = StreamUtils.concat(Arrays.asList(Arrays.asList("1", "2")
                                                                      .stream(),
                                                                Arrays.asList("3", "4")
                                                                      .stream())
                                                        .stream())
                                          .collect(Collectors.toList());
        assertEquals(Arrays.asList("1", "2", "3", "4"), collect);
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
        assertEquals(Arrays.asList("one", "two"), StreamUtils.fromReader(new StringReader("one\ntwo"))
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
    public void testFramed() throws Exception
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
            assertArrayEquals(new String[] { "4", "5", null }, frames.get(1));
        }
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

}
