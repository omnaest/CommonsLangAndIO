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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
		List<String> projection = ListUtils	.projection()
											.withQualifiedSource()
											.withReadProjection(source -> source.getFirstElement() + "" + source.getSecondElement())
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
		private TestElement	parent	= null;
		private String		name;
		private int			count;

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
		List<TestElement> list = DurationCapture.newInstanc()
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

		System.out.println(list	.stream()
								.skip(numberOfElements - 1000)
								.limit(10)
								.map(test -> test.getName() + ":" + test.getCount())
								.collect(Collectors.joining("\n")));
	}

	@Test
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
}
