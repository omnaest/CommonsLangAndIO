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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

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
		List<String> projection = ListUtils.projection(	source -> source.getFirstElement() + source.getSecondElement(), new String[] { "A", "B", "C" },
														new Integer[] { 1, 2, 3 });
		assertEquals(Arrays.asList("A1", "B2", "C3"), projection);
	}

	@Test
	public void testAddToNew()
	{
		List<String> list = ListUtils.addToNew(Arrays.asList("1", "2"), "3");
		assertEquals(Arrays.asList("1", "2", "3"), list);
	}

	private static interface TestElement
	{

		public int getCount();

		public String getName();

	}

	private static class TestElementImpl implements TestElement
	{
		private String	name;
		private int		count;

		public TestElementImpl(String name, int count)
		{
			super();
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

	}

	@Test
	public void testToMemoryOptimizedList() throws Exception
	{
		List<TestElement> sourceList = new ArrayList<>();
		int numberOfElements = 100;
		for (int ii = 0; ii < numberOfElements; ii++)
		{
			sourceList.add(new TestElementImpl("name" + ii, ii));
		}
		List<TestElement> list = ListUtils.toMemoryOptimizedList(TestElement.class, sourceList);

		for (int ii = 0; ii < numberOfElements; ii++)
		{
			assertEquals("name" + ii, list	.get(ii)
											.getName());
		}
		for (int ii = 0; ii < numberOfElements; ii++)
		{
			assertEquals(ii, list	.get(ii)
									.getCount());
		}
	}

	@Test
	//@Ignore
	public void testToMemoryOptimizedListPerformance() throws Exception
	{
		List<TestElement> sourceList = new ArrayList<>();
		for (int ii = 0; ii < 200000; ii++)
		{
			sourceList.add(new TestElementImpl("name" + ii, ii));
		}
		List<TestElement> list = ListUtils.toMemoryOptimizedList(TestElement.class, sourceList);

		System.out.println(list	.stream()
								.map(test -> test.getName() + ":" + test.getCount())
								.collect(Collectors.joining("\n")));
	}
}
