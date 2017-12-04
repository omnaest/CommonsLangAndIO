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
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class ArrayUtilsTest
{

	@Test
	public void testDeepClone() throws Exception
	{
		String[][] matrix = new String[][] { new String[] { "1", "2" }, new String[] { "3", "4" } };

		String[][] clone = ArrayUtils.deepClone(matrix);
		assertArrayEquals(matrix, clone);

		clone[1][1] = "x";
		assertNotSame(matrix, clone);
		assertNotSame("x", matrix[1][1]);
		assertNotSame(matrix[1], clone[1]);
		assertArrayEquals(matrix[0], clone[0]);
	}

}
