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

import java.util.Arrays;

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
}
