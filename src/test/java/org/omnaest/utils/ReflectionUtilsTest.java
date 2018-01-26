package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class ReflectionUtilsTest
{

    @Test
    public void testNewInstance() throws Exception
    {
        assertEquals(Arrays.asList("a", "b"), ReflectionUtils.newInstance(ArrayList.class, Arrays.asList("a", "b")));
    }

}
