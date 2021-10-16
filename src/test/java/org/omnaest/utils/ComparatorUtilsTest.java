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

import java.util.Comparator;

import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

public class ComparatorUtilsTest
{

    @Test
    public void testChainedComparator() throws Exception
    {
        assertEquals(-1, ComparatorUtils.chainedComparator((String s) -> Integer.valueOf(NumberUtils.toInt(s)), (String s) -> s)
                                        .compare("01", "1"));
    }

    private static class Wrapper

    {
        private int value;

        public Wrapper(int value)
        {
            super();
            this.value = value;
        }

        public int getValue()
        {
            return this.value;
        }

    }

    @Test
    public void testBuilder() throws Exception
    {
        assertEquals(-1, ComparatorUtils.builder()
                                        .of(new Wrapper(1), new Wrapper(2))
                                        .with((w1, w2) -> 0)
                                        .and(Wrapper::getValue)
                                        .compare());

        assertEquals(1, ComparatorUtils.builder()
                                       .of(new Wrapper(2), new Wrapper(1))
                                       .with((w1, w2) -> 0)
                                       .and(Wrapper::getValue, Integer::valueOf)
                                       .compare());
        assertEquals(-1, ComparatorUtils.builder()
                                        .of(new Wrapper(2), new Wrapper(1))
                                        .with((w1, w2) -> 0)
                                        .and(Wrapper::getValue, Integer::valueOf)
                                        .reverse()
                                        .compare());
    }

    @Test
    public void testComparatorMapping()
    {
        Comparator<Integer> comparator = ComparatorUtils.builder()
                                                        .<Integer, String>of(String::valueOf)
                                                        .natural();
        assertEquals(true, ComparatorUtils.isBefore(comparator, 1, 2));
        assertEquals(true, ComparatorUtils.isAfter(comparator, 2, 1));
        assertEquals(true, ComparatorUtils.isBefore(comparator, 10, 2));
    }

}
