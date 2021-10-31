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

import org.junit.Test;

public class NumberUtilsTest
{

    @Test
    public void testFormatter() throws Exception
    {
        String result = NumberUtils.formatter()
                                   .format(1.00001);
        assertEquals("1.00001", result);
    }

    @Test
    public void testFormatter2() throws Exception
    {
        String result = NumberUtils.formatter()
                                   .format(100001);
        assertEquals("100001", result);
    }

    @Test
    public void testFormatterWithPercentage() throws Exception
    {
        String result = NumberUtils.formatter()
                                   .asPercentage()
                                   .format(0.05);
        assertEquals("5%", result);
    }

    @Test
    public void testFormatterWithPercentage2() throws Exception
    {
        String result = NumberUtils.formatter()
                                   .asPercentage()
                                   .withMinimumFractionDigits(3)
                                   .format(0.00005);
        assertEquals("0.005%", result);
    }

    @Test
    public void testWithFractionDigits()
    {
        assertEquals("3.3333", NumberUtils.formatter()
                                          .withFractionDigits(4)
                                          .format(3.33333));
        assertEquals("1.0000", NumberUtils.formatter()
                                          .withFractionDigits(4)
                                          .format(1));
    }

}
