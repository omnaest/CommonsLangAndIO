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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Arrays;

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

    @Test
    public void testReverse() throws Exception
    {
        String[] reverse = ArrayUtils.reverse(new String[] { "a", "b", "c" });
        assertEquals(Arrays.asList("c", "b", "a"), Arrays.asList(reverse));
    }

    @Test
    public void testSubArrayStartingFromMatching()
    {
        assertArrayEquals(null, ArrayUtils.subArrayStartingFromMatching(null, (Byte[]) null));
        assertArrayEquals(new Byte[] { 10, 20, 30 }, ArrayUtils.subArrayStartingFromMatching(value -> value != 0, new Byte[] { 0, 0, 10, 20, 30 }));
        assertArrayEquals(new Byte[] {}, ArrayUtils.subArrayStartingFromMatching(null, new Byte[] { 0, 0, 10, 20, 30 }));

        assertArrayEquals(new byte[] { 10, 20, 30 }, ArrayUtils.subArrayStartingFromMatching(value -> value != 0, new byte[] { 0, 0, 10, 20, 30 }));
    }

}
