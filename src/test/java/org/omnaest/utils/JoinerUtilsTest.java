package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * @see JoinerUtils
 * @author omnaest
 */
public class JoinerUtilsTest
{

    @Test
    public void testJoinInner() throws Exception
    {
        List<String> list1 = Arrays.asList("0", "1");
        List<Integer> list2 = Arrays.asList(0, 1);
        List<String> result = JoinerUtils.join(list1)
                                         .with(list2)
                                         .inner()
                                         .usingPrimaryKey(e -> e, e -> String.valueOf(e))
                                         .map(e -> e.getFirst() + e.getSecond())
                                         .collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals("00", result.get(0));
        assertEquals("11", result.get(1));

    }

    @Test
    public void testCartesianJoin() throws Exception
    {
        List<String> list1 = Arrays.asList("0", "1");
        List<Integer> list2 = Arrays.asList(0, 1);
        List<String> result = JoinerUtils.join(list1)
                                         .with(list2)
                                         .cartesian()
                                         .map(e -> e.getFirst() + e.getSecond())
                                         .collect(Collectors.toList());
        assertEquals(4, result.size());
        assertEquals("00", result.get(0));
        assertEquals("01", result.get(1));
        assertEquals("10", result.get(2));
        assertEquals("11", result.get(3));

    }

}
