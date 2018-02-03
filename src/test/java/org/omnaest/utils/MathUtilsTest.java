package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

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
}
