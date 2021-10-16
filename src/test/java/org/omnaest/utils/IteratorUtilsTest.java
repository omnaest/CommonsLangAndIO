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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.Test;

/**
 * @see IteratorUtils
 * @author omnaest
 */
public class IteratorUtilsTest
{

    @Test
    public void testDrain() throws Exception
    {
        Iterator<String> roundRobinIterator = IteratorUtils.roundRobinIterator(Arrays.asList("1", "2", "3"));
        List<String> drained = IteratorUtils.drain(roundRobinIterator, 10);
        assertEquals(Arrays.asList("1", "2", "3", "1", "2", "3", "1", "2", "3", "1"), drained);
    }

    @Test
    public void testDrainWithTerminatePredicate() throws Exception
    {
        Iterator<String> roundRobinIterator = IteratorUtils.roundRobinIterator(Arrays.asList("1", "2", "3"));
        List<String> drained = IteratorUtils.drain(roundRobinIterator, element -> element.equals("2"));
        assertEquals(Arrays.asList("1", "2"), drained);
    }

    @Test
    public void testRoundRobinIterator() throws Exception
    {
        assertFalse(IteratorUtils.roundRobinIterator(Collections.emptyList())
                                 .hasNext());
        assertTrue(IteratorUtils.roundRobinIterator(Arrays.asList("1"))
                                .hasNext());
    }

    @Test
    public void testRoundRobinIteratorWithConcurrentModification() throws Exception
    {
        List<String> list = new ArrayList<>(Arrays.asList("2", "3"));
        Iterator<String> iterator = IteratorUtils.roundRobinIterator(list);
        assertTrue(iterator.hasNext());
        assertEquals("2", iterator.next());

        list.add(0, "1");
        assertTrue(iterator.hasNext());
        assertEquals("1", iterator.next());
    }

    @Test
    public void testWithConsumerListener() throws Exception
    {
        List<String> buffer = new ArrayList<>();
        Iterator<String> iterator = IteratorUtils.withConsumerListener(Arrays.asList("1", "2")
                                                                             .iterator(),
                                                                       e -> buffer.add(e));
        assertEquals(StreamUtils.fromIterator(iterator)
                                .collect(Collectors.toList()),
                     buffer);
    }

    @Test
    public void testToIterable() throws Exception
    {
        assertEquals(Arrays.asList("1", "2"), StreamUtils.fromIterable(IteratorUtils.toIterable(() -> Arrays.asList("1", "2")
                                                                                                            .iterator()))
                                                         .collect(Collectors.toList()));
    }

    @Test
    public void testFrom() throws Exception
    {
        Iterator<Character> iterator = IteratorUtils.from("1234");
        assertTrue(iterator.hasNext());
        assertEquals(Character.valueOf('1'), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(Character.valueOf('2'), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(Character.valueOf('3'), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(Character.valueOf('4'), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testWrapIntoConcurrent() throws Exception
    {
        assertEquals(Arrays.asList("a", "b", "c"), ListUtils.toList(IteratorUtils.wrapIntoConcurrent(Arrays.asList("a", "b", "c")
                                                                                                           .iterator())));
    }

    @Test
    public void testSplitInto() throws Exception
    {
        assertEquals(5, IteratorUtils.splitInto(5, Arrays.asList("1", "2", "3", "4", "5")
                                                         .spliterator())
                                     .size());
        assertEquals(Arrays.asList("1", "2", "3", "4", "5")
                           .stream()
                           .collect(Collectors.toSet()),
                     IteratorUtils.splitInto(5, Arrays.asList("1", "2", "3", "4", "5")
                                                      .spliterator())
                                  .stream()
                                  .flatMap(spliterator -> StreamSupport.stream(spliterator, false))
                                  .collect(Collectors.toSet()));
        assertEquals(IntStream.range(0, 1000)
                              .boxed()
                              .collect(Collectors.toSet()),
                     IteratorUtils.splitInto(5, IntStream.range(0, 1000)
                                                         .boxed()
                                                         .spliterator())
                                  .stream()
                                  .flatMap(spliterator -> StreamSupport.stream(spliterator, false))
                                  .collect(Collectors.toSet()));

    }

}
