package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.omnaest.utils.element.bi.BiElement;

public class CollectorUtilsTest
{

    @Test
    public void testGroupingByUnique() throws Exception
    {
        Map<String, BiElement<String, String>> result = MapUtils.builder()
                                                                .put("key1", "value1")
                                                                .put("key2", "value2")
                                                                .build()
                                                                .entrySet()
                                                                .stream()
                                                                .map(entry -> BiElement.of(entry.getKey(), entry.getValue()))
                                                                .collect(CollectorUtils.groupingByUnique(entry -> entry.getFirst()));
        assertEquals(2, result.size());
        assertEquals("value2", result.get("key2")
                                     .getSecond());
    }

    @Test
    public void testToValueMappedMap() throws Exception
    {
        Map<String, String> map = MapUtils.builder()
                                          .put("key1", "value1")
                                          .put("key2", "value2")
                                          .build()
                                          .entrySet()
                                          .stream()
                                          .collect(CollectorUtils.toValueMappedMap(entry -> entry.getValue() + "X"));
        assertEquals("value1X", map.get("key1"));
    }

}
