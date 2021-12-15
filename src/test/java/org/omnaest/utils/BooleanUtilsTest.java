package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BooleanUtilsTest
{
    @Test
    public void testToIntValue() throws Exception
    {
        assertEquals(1, BooleanUtils.toIntValue(true, 1, 0));
        assertEquals(0, BooleanUtils.toIntValue(false, 1, 0));
    }
}
