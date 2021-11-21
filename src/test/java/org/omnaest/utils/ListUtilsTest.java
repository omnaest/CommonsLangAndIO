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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.utils.duration.DurationCapture;

public class ListUtilsTest
{
    @Test
    public void testMergedList() throws Exception
    {
        assertEquals(Arrays.asList("1", "2", "3", "4"), ListUtils.mergedList(Arrays.asList("1", "2"), Arrays.asList("3", "4")));
    }

    @Test
    public void testCompare() throws Exception
    {
        assertEquals(0, ListUtils.compare(Arrays.asList("1", "2"), Arrays.asList("1", "2"), String::compareTo));
        assertEquals(-1, ListUtils.compare(Arrays.asList("1", "2"), Arrays.asList("1", "2", "3"), ComparatorUtils::compare));
        assertEquals(-1, ListUtils.compare(Arrays.asList("1", "2"), Arrays.asList("1", "3"), ComparatorUtils::compare));
        assertEquals(1, ListUtils.compare(Arrays.asList("1", "3"), Arrays.asList("1", "2", "3"), ComparatorUtils::compare));
    }

    @Test
    public void testInverse() throws Exception
    {
        assertEquals(Arrays.asList("2", "1"), ListUtils.inverse(Arrays.asList("1", "2")));
    }

    @Test
    public void testProjection()
    {
        List<String> projection = ListUtils.aggregation()
                                           .withQualifiedSource()
                                           .withReadAggregation(source -> source.getFirstElement() + "" + source.getSecondElement())
                                           .usingSources(new String[] { "A", "B", "C" }, new Integer[] { 1, 2, 3 })
                                           .build();
        assertEquals(Arrays.asList("A1", "B2", "C3"), projection);
    }

    @Test
    public void testAddToNew()
    {
        List<String> list = ListUtils.addToNew(Arrays.asList("1", "2"), "3");
        assertEquals(Arrays.asList("1", "2", "3"), list);
    }

    @Test
    public void testAddTo()
    {
        List<String> list = ListUtils.addTo(new ArrayList<>(Arrays.asList("1", "2")), "3");
        assertEquals(Arrays.asList("1", "2", "3"), list);
    }

    private static interface TestElement
    {
        public int getCount();

        public String getName();

        public TestElement getParent();
    }

    private static class TestElementImpl implements TestElement
    {
        private TestElement parent = null;
        private String      name;
        private int         count;

        public TestElementImpl(String name, int count)
        {
            super();
            this.name = name;
            this.count = count;
        }

        public TestElementImpl(TestElement parent, String name, int count)
        {
            super();
            this.parent = parent;
            this.name = name;
            this.count = count;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public int getCount()
        {
            return this.count;
        }

        @Override
        public TestElement getParent()
        {
            return this.parent;
        }

    }

    @Test
    public void testToMemoryOptimizedList() throws Exception
    {
        List<TestElement> sourceList = new ArrayList<>();
        int numberOfElements = 100;
        for (int ii = 0; ii < numberOfElements; ii++)
        {
            sourceList.add(new TestElementImpl(new TestElementImpl("parent name", ii), "name" + ii, ii));
        }
        List<TestElement> list = ListUtils.toMemoryOptimizedList(TestElement.class, sourceList);

        for (int ii = 0; ii < numberOfElements; ii++)
        {
            assertEquals("name" + ii, list.get(ii)
                                          .getName());
        }
        for (int ii = 0; ii < numberOfElements; ii++)
        {
            assertEquals(ii, list.get(ii)
                                 .getCount());
        }
    }

    @Test
    @Ignore
    public void testToMemoryOptimizedListPerformance() throws Exception
    {
        List<TestElement> sourceList = new ArrayList<>();
        int numberOfElements = 1000000;
        int depth = 5;
        for (int ii = 0; ii < numberOfElements; ii++)
        {
            TestElement element = null;
            for (int jj = 0; jj < depth; jj++)
            {
                element = new TestElementImpl(element, "name" + ii + "." + jj, ii);
            }
            sourceList.add(element);
        }

        System.out.println("Collecting");
        List<TestElement> list = DurationCapture.newInstance()
                                                .measure(() -> ListUtils.toMemoryOptimizedList(TestElement.class, sourceList))
                                                .doWithResult(System.out::println)
                                                .getReturnValue();

        System.out.println("Waiting");
        ThreadUtils.sleepSilently(20, TimeUnit.SECONDS);

        System.out.println("Clearing source list...");
        sourceList.clear();

        System.out.println("Waiting");
        ThreadUtils.sleepSilently(20, TimeUnit.SECONDS);

        System.out.println("Reading...");

        System.out.println(list.stream()
                               .skip(numberOfElements - 1000)
                               .limit(10)
                               .map(test -> test.getName() + ":" + test.getCount())
                               .collect(Collectors.joining("\n")));
    }

    @Test
    @Ignore
    public void testToMemoryOptimizedListPerformanceCompared() throws Exception
    {
        List<TestElement> sourceList = new ArrayList<>();
        int numberOfElements = 1000000;
        int depth = 5;
        for (int ii = 0; ii < numberOfElements; ii++)
        {
            TestElement element = null;
            for (int jj = 0; jj < depth; jj++)
            {
                element = new TestElementImpl(element, "name" + ii + "." + jj, ii);
            }
            sourceList.add(element);
        }

        System.out.println("Waiting");
        ThreadUtils.sleepSilently(20, TimeUnit.SECONDS);

        System.out.println("Clearing source list...");
        sourceList.clear();

        System.out.println("Waiting");
        ThreadUtils.sleepSilently(20, TimeUnit.SECONDS);

    }

    @Test
    public void testSetTo()
    {
        List<String> list = ListUtils.setTo(new ArrayList<String>(), 10, Arrays.asList("A", "B")
                                                                               .stream());
        assertEquals(12, list.size());
        assertEquals("A", list.get(10));
        assertEquals("B", list.get(11));
    }

    @Test
    public void testGet() throws Exception
    {
        assertEquals("1", ListUtils.get(Arrays.asList("1", "2"), 0));
        assertEquals("2", ListUtils.get(Arrays.asList("1", "2"), 1));
        assertNull(ListUtils.get(Arrays.asList("1", "2"), -1));
        assertNull(ListUtils.get(Arrays.asList("1", "2"), 2));
        assertNull(ListUtils.get(null, 0));
    }

    @Test
    public void testEnsureSize() throws Exception
    {
        List<String> list = ListUtils.ensureSize(new ArrayList<>(), 3);
        assertEquals(3, list.size());
    }

    @Test
    public void testToArray() throws Exception
    {
        List<String> source = Arrays.asList("a", "b", "c");
        String[] array = ListUtils.toArray(source, String.class);
        assertEquals(Arrays.asList(array), source);
    }

    @Test
    public void testLast()
    {
        assertEquals(Arrays.asList("2", "3", "4"), ListUtils.last(3, Arrays.asList("1", "2", "3", "4")));
        assertEquals(Arrays.asList(), ListUtils.last(0, Arrays.asList("1", "2", "3", "4")));
        assertEquals(Arrays.asList("3", "4"), ListUtils.last(3, Arrays.asList("3", "4")));
        assertEquals(Arrays.asList(), ListUtils.last(3, Arrays.asList()));
        assertEquals(Arrays.asList(), ListUtils.last(3, null));
    }

    @Test
    public void testOptionalLast()
    {
        assertEquals("3", ListUtils.optionalLast(Arrays.asList("1", "2", "3"), 0)
                                   .get());
        assertEquals("2", ListUtils.optionalLast(Arrays.asList("1", "2", "3"), 1)
                                   .get());
        assertEquals("1", ListUtils.optionalLast(Arrays.asList("1", "2", "3"), 2)
                                   .get());
        assertEquals(false, ListUtils.optionalLast(Arrays.asList("1", "2", "3"), 3)
                                     .isPresent());
        assertEquals(false, ListUtils.optionalLast(Arrays.asList("1", "2", "3"), -1)
                                     .isPresent());
    }

    @Test
    public void testGetRandomElement() throws Exception
    {
        assertEquals("a", ListUtils.getRandomElement(Arrays.asList("a"))
                                   .get());
        assertEquals(2, IntStream.range(0, 1000)
                                 .mapToObj(ii -> ListUtils.getRandomElement(Arrays.asList("a", "b"))
                                                          .get())
                                 .distinct()
                                 .count());
        ListUtils.getRandomElement(Arrays.asList())
                 .ifPresent(e -> fail());
        ListUtils.getRandomElement(null)
                 .ifPresent(e -> fail());

    }

    @Test
    public void testToListFromIterator()
    {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals(list, ListUtils.toList(list.iterator()));
    }

    @Test
    public void testToListEArray() throws Exception
    {
        assertEquals(Arrays.asList("a", "b"), ListUtils.toList("a", "b"));
    }

    @Test
    public void testAllCombinations() throws Exception
    {
        assertEquals(SetUtils.toSet(SetUtils.toSet("a"), SetUtils.toSet("b"), SetUtils.toSet("c"), SetUtils.toSet("a", "b"), SetUtils.toSet("a", "c"),
                                    SetUtils.toSet("b", "c"), SetUtils.toSet("a", "b", "c")

        ), ListUtils.allCombinations(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testAllPermutations() throws Exception
    {
        assertEquals(SetUtils.toSet(Arrays.asList("a", "b", "c"), Arrays.asList("b", "c", "a"), Arrays.asList("c", "a", "b"), Arrays.asList("c", "b", "a"),
                                    Arrays.asList("b", "a", "c"), Arrays.asList("a", "c", "b")),
                     ListUtils.allPermutations(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testSublistsFromStart() throws Exception
    {
        assertEquals(Arrays.asList(Arrays.asList("a", "b", "c"), Arrays.asList("a", "b"), Arrays.asList("a")),
                     ListUtils.sublistsFromStart(Arrays.asList("a", "b", "c"))
                              .collect(Collectors.toList()));
    }

    @Test
    public void testSublist() throws Exception
    {
        assertEquals(Arrays.asList(2, 3), ListUtils.sublist(Arrays.asList(1, 2, 3), 1));
        assertEquals(Arrays.asList(), ListUtils.sublist(null, 1));
    }

    @Test
    public void testShingle() throws Exception
    {
        assertEquals(Arrays.asList("a", "b", "c", "ab", "bc", "abc"), ListUtils.shingle(Arrays.asList("a", "b", "c"))
                                                                               .map(list -> list.stream()
                                                                                                .collect(Collectors.joining()))
                                                                               .collect(Collectors.toList()));
    }

    @Test
    public void testBuilder() throws Exception
    {
        assertEquals(Arrays.asList("a", "b", "c"), ListUtils.builder()
                                                            .add("a")
                                                            .add("b")
                                                            .addIf(true, "c")
                                                            .addIf(false, "d")
                                                            .build());
    }

    @Test
    public void testAreElementsDistinct() throws Exception
    {
        assertTrue(ListUtils.areElementsDistinct(Arrays.asList("a", "b")));
        assertFalse(ListUtils.areElementsDistinct(Arrays.asList("a", "a")));
    }

    @Test
    public void testSplitLast() throws Exception
    {
        assertEquals(Arrays.asList("a", "b"), ListUtils.splitLast(Arrays.asList("a", "b", "c"))
                                                       .getFirst());
        assertEquals("c", ListUtils.splitLast(Arrays.asList("a", "b", "c"))
                                   .getSecond()
                                   .get());
        assertEquals(false, ListUtils.splitLast(Collections.emptyList())
                                     .getSecond()
                                     .isPresent());
        assertEquals(Collections.emptyList(), ListUtils.splitLast(Collections.emptyList())
                                                       .getFirst());
        assertEquals(false, ListUtils.splitLast(null)
                                     .getSecond()
                                     .isPresent());
        assertEquals(Collections.emptyList(), ListUtils.splitLast(null)
                                                       .getFirst());
    }

    @Test
    public void testGetOptional() throws Exception
    {
        assertEquals("1", ListUtils.getOptional(Arrays.asList("1", "2", "3"), 0)
                                   .get());
        assertEquals("2", ListUtils.getOptional(Arrays.asList("1", "2", "3"), 1)
                                   .get());
        assertEquals("3", ListUtils.getOptional(Arrays.asList("1", "2", "3"), 2)
                                   .get());
        assertFalse(ListUtils.getOptional(Arrays.asList("1", "2", "3"), -1)
                             .isPresent());
        assertFalse(ListUtils.getOptional(Arrays.asList("1", "2", "3"), 3)
                             .isPresent());
        assertFalse(ListUtils.getOptional(null, 0)
                             .isPresent());
    }
}
