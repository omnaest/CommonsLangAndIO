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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
	public void testRoundRobinIterator() throws Exception
	{
		assertFalse(IteratorUtils	.roundRobinIterator(Collections.emptyList())
									.hasNext());
		assertTrue(IteratorUtils.roundRobinIterator(Arrays.asList("1"))
								.hasNext());
	}

}
