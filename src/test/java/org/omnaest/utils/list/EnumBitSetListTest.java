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
package org.omnaest.utils.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.omnaest.utils.ThreadUtils;

public class EnumBitSetListTest
{

	private enum TestEnum
	{
		A, B, C, D, E
	}

	@Test
	public void testSize() throws Exception
	{
		List<TestEnum> list = new EnumBitSetList<>(TestEnum.class);
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());

		list.add(TestEnum.A);
		assertEquals(1, list.size());
		assertFalse(list.isEmpty());
	}

	@Test
	public void testGetAndAdd() throws Exception
	{
		List<TestEnum> list = new EnumBitSetList<>(TestEnum.class);

		list.add(TestEnum.A);
		assertEquals(TestEnum.A, list.get(0));

		list.add(TestEnum.B);
		assertEquals(TestEnum.B, list.get(1));

		list.add(TestEnum.C);
		assertEquals(TestEnum.C, list.get(2));

		list.add(0, TestEnum.C);
		assertEquals(TestEnum.C, list.get(0));
		assertEquals(TestEnum.A, list.get(1));

		assertEquals(new EnumBitSetList<>(TestEnum.class, Arrays.asList(TestEnum.C, TestEnum.A, TestEnum.B, TestEnum.C)), list);
	}

	@Test
	public void testToArray()
	{
		List<TestEnum> sourceList = Arrays.asList(TestEnum.C, TestEnum.A, TestEnum.B, TestEnum.C);
		List<TestEnum> list = new EnumBitSetList<>(TestEnum.class, sourceList);
		TestEnum[] testEnums = list.toArray(new TestEnum[0]);
		assertEquals(sourceList, Arrays.asList(testEnums));
	}

	@Test
	public void testRemove() throws Exception
	{
		{
			List<TestEnum> list = new EnumBitSetList<>(TestEnum.class, Arrays.asList(TestEnum.C, TestEnum.A, TestEnum.B, TestEnum.C));
			list.remove(0);
			assertEquals(new EnumBitSetList<>(TestEnum.class, Arrays.asList(TestEnum.A, TestEnum.B, TestEnum.C)), list);
		}
		{
			List<TestEnum> list = new EnumBitSetList<>(TestEnum.class, Arrays.asList(TestEnum.C, TestEnum.A, TestEnum.B, TestEnum.C));
			list.remove(1);
			assertEquals(new EnumBitSetList<>(TestEnum.class, Arrays.asList(TestEnum.C, TestEnum.B, TestEnum.C)), list);
		}
		{
			List<TestEnum> list = new EnumBitSetList<>(TestEnum.class, Arrays.asList(TestEnum.C, TestEnum.A, TestEnum.B, TestEnum.C));
			list.remove(3);
			assertEquals(new EnumBitSetList<>(TestEnum.class, Arrays.asList(TestEnum.C, TestEnum.A, TestEnum.B)), list);
		}
	}

	@Test
	public void memoryConsumptionTest()
	{
		List<TestEnum> list = new ArrayList<>();
		for (int ii = 0; ii < 100000000; ii++)
		{
			list.add(TestEnum.values()[ii % TestEnum.values().length]);
		}

		System.gc();
		ThreadUtils.sleepSilently(10, TimeUnit.SECONDS);

		list = new EnumBitSetList<>(TestEnum.class, list);

		System.gc();
		ThreadUtils.sleepSilently(10, TimeUnit.SECONDS);
	}

}
