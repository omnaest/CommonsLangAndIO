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
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.CollectionUtils.CollectionDelta;

/**
 * @see CollectionUtils
 * @author Omnaest
 */
public class CollectionUtilsTest
{

	@Test
	public void testDelta() throws Exception
	{
		CollectionDelta<String> collectionDelta = CollectionUtils.delta(Arrays.asList("a", "b"), Arrays.asList("b", "c"));

		assertEquals(	Arrays.asList("a")
							.stream()
							.collect(Collectors.toSet()),
						collectionDelta.getRemoved());
		assertEquals(	Arrays.asList("b")
							.stream()
							.collect(Collectors.toSet()),
						collectionDelta.getShared());
		assertEquals(	Arrays.asList("c")
							.stream()
							.collect(Collectors.toSet()),
						collectionDelta.getAdded());
	}

	@Test
	public void testLast() throws Exception
	{
		assertEquals("b", CollectionUtils.last(Arrays.asList("a", "b")));
	}

}
