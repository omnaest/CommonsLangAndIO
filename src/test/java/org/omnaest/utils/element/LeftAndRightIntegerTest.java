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
package org.omnaest.utils.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LeftAndRightIntegerTest
{

	@Test
	public void testGetMin() throws Exception
	{
		assertEquals(1, new LeftAndRightInteger(1, 2)	.getMin()
														.intValue());
		assertEquals(1, new LeftAndRightInteger(2, 1)	.getMin()
														.intValue());
		assertEquals(1, new LeftAndRightInteger(1, null).getMin()
														.intValue());
		assertEquals(1, new LeftAndRightInteger(null, 1).getMin()
														.intValue());
		assertNull(new LeftAndRightInteger(null, null).getMin());
	}

	@Test
	public void testGetMax() throws Exception
	{
		assertEquals(2, new LeftAndRightInteger(1, 2)	.getMax()
														.intValue());
		assertEquals(2, new LeftAndRightInteger(2, 1)	.getMax()
														.intValue());
		assertEquals(2, new LeftAndRightInteger(null, 2).getMax()
														.intValue());
		assertEquals(2, new LeftAndRightInteger(2, null).getMax()
														.intValue());
		assertNull(new LeftAndRightInteger(null, null).getMax());
	}

}
