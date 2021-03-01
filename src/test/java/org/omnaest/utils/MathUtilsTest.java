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
package org.omnaest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.MathUtils.AverageAndStandardDeviation;

public class MathUtilsTest
{

    @Test
    public void testCalculateAverage() throws Exception
    {
        AverageAndStandardDeviation averageAndStandardDeviation = MathUtils.calculateAverage(Arrays.asList(1, 2, 2, 2, 2, 2, 3));
        assertEquals(2, averageAndStandardDeviation.getAverage(), 0.001);
    }

    @Test
    public void testCalculateAverage2() throws Exception
    {
        AverageAndStandardDeviation averageAndStandardDeviation = MathUtils.calculateAverage(Arrays.asList(600, 470, 170, 430, 300));
        assertEquals(394, averageAndStandardDeviation.getAverage(), 0.001);
        assertEquals(164.71, averageAndStandardDeviation.getStandardDeviation(), 0.1);
    }

    @Test
    public void testCalculateAverage3() throws Exception
    {
        AverageAndStandardDeviation averageAndStandardDeviation = MathUtils.calculateAverage(Arrays.asList());
        assertEquals(0, averageAndStandardDeviation.getAverage(), 0.001);
        assertEquals(0, averageAndStandardDeviation.getStandardDeviation(), 0.001);
    }

    @Test
    public void testRandom() throws Exception
    {
        IntStream.range(0, 100)
                 .forEach(ii ->
                 {
                     assertTrue(MathUtils.random(-0.3, 0.5) <= 0.5);
                     assertTrue(MathUtils.random(-0.3, 0.5) >= -0.3);
                 });
    }
}
