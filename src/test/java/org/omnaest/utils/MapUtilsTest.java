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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.MapUtils.MapDelta;
import org.omnaest.utils.element.lar.UnaryLeftAndRight;

/**
 * @see MapUtils
 * @author omnaest
 */
public class MapUtilsTest
{

    @Test
    public void testInvert() throws Exception
    {
        Map<String, String> map = MapUtils.builder()
                                          .put("key1", "value1")
                                          .put("key2", "value1")
                                          .put("key3", "value2")
                                          .build();

        Map<String, List<String>> inverse = MapUtils.invert(map);
        assertEquals(2, inverse.size());
        assertEquals(Arrays.asList("key1", "key2"), inverse.get("value1"));
        assertEquals(Arrays.asList("key3"), inverse.get("value2"));
    }

    @Test
    public void testInvertMultivalue() throws Exception
    {
        Map<String, List<String>> map = MapUtils.builder()
                                                .put("key1", Arrays.asList("value1"))
                                                .put("key2", Arrays.asList("value1"))
                                                .put("key3", Arrays.asList("value2"))
                                                .build();

        Map<String, List<String>> inverse = MapUtils.invertMultiValue(map);
        assertEquals(2, inverse.size());
        assertEquals(Arrays.asList("key1", "key2"), inverse.get("value1"));
        assertEquals(Arrays.asList("key3"), inverse.get("value2"));
    }

    @Test
    public void testBuilder() throws Exception
    {
        Map<String, List<String>> map = MapUtils.builder()
                                                .put("key2", Arrays.asList("value1"))
                                                .put("key1", Arrays.asList("value1"))
                                                .put("key3", Arrays.asList("value2"))
                                                .useFactory(() -> new TreeMap<>())
                                                .build();
        assertEquals(3, map.size());
        assertEquals(Arrays.asList("key1", "key2", "key3"), map.keySet()
                                                               .stream()
                                                               .collect(Collectors.toList()));

    }

    @Test
    public void testJoin() throws Exception
    {
        Map<String, UnaryLeftAndRight<String>> join = MapUtils.join(MapUtils.builder()
                                                                            .put("1", "value1")
                                                                            .put("2", "value2.1")
                                                                            .build(),
                                                                    MapUtils.builder()
                                                                            .put("2", "value2.2")
                                                                            .put("3", "value3")
                                                                            .build());

        assertEquals(3, join.size());
        assertEquals("value1", join.get("1")
                                   .getLeft());
        assertEquals(null, join.get("1")
                               .getRight());
        assertEquals("value2.1", join.get("2")
                                     .getLeft());
        assertEquals("value2.2", join.get("2")
                                     .getRight());
        assertEquals(null, join.get("3")
                               .getLeft());
        assertEquals("value3", join.get("3")
                                   .getRight());

    }

    @Test
    public void testMultiJoin() throws Exception
    {
        Map<String, List<String>> join = MapUtils.join(MapUtils.builder()
                                                               .put("1", "value1")
                                                               .put("2", "value2.1")
                                                               .build(),
                                                       MapUtils.builder()
                                                               .put("2", "value2.2")
                                                               .put("3", "value3")
                                                               .build(),
                                                       MapUtils.builder()
                                                               .put("4", "value4")
                                                               .put("2", "value2.3")
                                                               .build());

        assertEquals(4, join.size());
        assertEquals("value1", join.get("1")
                                   .get(0));
        assertEquals(null, join.get("1")
                               .get(1));
        assertEquals(null, join.get("1")
                               .get(2));
        assertEquals("value2.1", join.get("2")
                                     .get(0));
        assertEquals("value2.2", join.get("2")
                                     .get(1));
        assertEquals("value2.3", join.get("2")
                                     .get(2));
        assertEquals(null, join.get("3")
                               .get(0));
        assertEquals("value3", join.get("3")
                                   .get(1));
        assertEquals(null, join.get("3")
                               .get(2));
        assertEquals(null, join.get("4")
                               .get(0));
        assertEquals(null, join.get("4")
                               .get(1));
        assertEquals("value4", join.get("4")
                                   .get(2));

    }

    @Test
    public void testSortedMap() throws Exception
    {
        Map<String, String> map = MapUtils.builder()
                                          .put("key1", "value1")
                                          .put("key2", "value1")
                                          .put("key3", "value2")
                                          .useSortedMap(ComparatorUtils.builder()
                                                                       .of(String.class)
                                                                       .withIdentity()
                                                                       .reverse()
                                                                       .build())
                                          .build();

        assertEquals(3, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals(Arrays.asList("key3", "key2", "key1"), map.keySet()
                                                               .stream()
                                                               .collect(Collectors.toList()));
    }

    @Test
    public void testToKeyFilteredMap() throws Exception
    {
        Map<String, String> map = MapUtils.toKeyFilteredMap(MapUtils.builder()
                                                                    .put("key1", "value1")
                                                                    .put("key2", "value2")
                                                                    .build(),
                                                            Arrays.asList("key2"));
        assertEquals(1, map.size());
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void testContainsAll() throws Exception
    {
        assertTrue(MapUtils.containsAll(MapUtils.builder()
                                                .put("key1", "value1")
                                                .put("key2", "value2")
                                                .build(),
                                        MapUtils.builder()
                                                .put("key1", "value1")
                                                .build()));

        assertFalse(MapUtils.containsAll(MapUtils.builder()
                                                 .put("key2", "value2")
                                                 .build(),
                                         MapUtils.builder()
                                                 .put("key1", "value1")
                                                 .build()));
    }

    @Test
    public void testDelta() throws Exception
    {
        MapDelta<String, String> delta = MapUtils.delta(MapUtils.builder()
                                                                .put("key1", "value1")
                                                                .put("key2", "value2")
                                                                .put("key3", "value3")
                                                                .build(),
                                                        MapUtils.builder()
                                                                .put("key2", "value2")
                                                                .put("key3", "value3New")
                                                                .put("key4", "value4")
                                                                .build());
        assertEquals(SetUtils.toSet("key1"), delta.getKeyChanges()
                                                  .getRemoved());
        assertEquals(SetUtils.toSet("key2", "key3"), delta.getKeyChanges()
                                                          .getShared());
        assertEquals(SetUtils.toSet("key4"), delta.getKeyChanges()
                                                  .getAdded());

        assertEquals(SetUtils.toSet("key1", "key3", "key4"), delta.getChanges()
                                                                  .keySet());
        assertEquals("value1", delta.getChanges()
                                    .get("key1")
                                    .getPrevious());
        assertEquals(null, delta.getChanges()
                                .get("key1")
                                .getNext());
        assertEquals("value3", delta.getChanges()
                                    .get("key3")
                                    .getPrevious());
        assertEquals("value3New", delta.getChanges()
                                       .get("key3")
                                       .getNext());
        assertEquals(null, delta.getChanges()
                                .get("key4")
                                .getPrevious());
        assertEquals("value4", delta.getChanges()
                                    .get("key4")
                                    .getNext());
    }

    @Test
    public void testMerge() throws Exception
    {
        assertEquals(MapUtils.builder()
                             .put("key1", "value1")
                             .put("key2", "value2")
                             .put("key3", "value3")
                             .build(),
                     MapUtils.merge(MapUtils.builder()
                                            .put("key1", "value1")
                                            .put("key2", "value2")
                                            .build(),
                                    MapUtils.builder()
                                            .put("key3", "value3")
                                            .put("key2", "value2")
                                            .build()));
    }

    @Test
    public void testMapValues() throws Exception
    {
        assertEquals(MapUtils.builder()
                             .put("a", 1)
                             .build(),
                     MapUtils.mapValues(MapUtils.builder()
                                                .put("a", new AtomicInteger(1))
                                                .build(),
                                        AtomicInteger::get));
    }

    @Test
    public void testToValueSortedMap() throws Exception
    {
        assertEquals(Arrays.asList("c", "b1", "b2", "a"), MapUtils.toValueSortedMap(MapUtils.builder()
                                                                                            .put("a", "3")
                                                                                            .put("b1", "2")
                                                                                            .put("b2", "2")
                                                                                            .put("c", "1")
                                                                                            .build())
                                                                  .keySet()
                                                                  .stream()
                                                                  .collect(Collectors.toList()));
    }

    @Test
    public void testToReverseValueSortedMap() throws Exception
    {
        assertEquals(Arrays.asList("c", "b2", "b1", "a"), MapUtils.toReverseValueSortedMap(MapUtils.builder()
                                                                                                   .put("a", "1")
                                                                                                   .put("b1", "2")
                                                                                                   .put("b2", "2")
                                                                                                   .put("c", "3")
                                                                                                   .build())
                                                                  .keySet()
                                                                  .stream()
                                                                  .collect(Collectors.toList()));
    }

    @Test
    public void testPartition() throws Exception
    {
        List<Map<String, String>> partitions = MapUtils.partition(MapUtils.builder()
                                                                          .put("key1", "value1")
                                                                          .put("key2", "value2")
                                                                          .put("key3", "value3")
                                                                          .build(),
                                                                  2)
                                                       .collect(Collectors.toList());
        assertEquals(2, partitions.size());
        assertEquals(SetUtils.toSet("key1", "key2"), partitions.get(0)
                                                               .keySet());
        assertEquals(SetUtils.toSet("key3"), partitions.get(1)
                                                       .keySet());
        assertEquals(SetUtils.toSet("value1", "value2"), partitions.get(0)
                                                                   .values()
                                                                   .stream()
                                                                   .collect(Collectors.toSet()));
    }

    @Test
    public void testToMapByCollection() throws Exception
    {
        Map<String, Object> map = MapUtils.toMap(Arrays.asList("1", "2"));
        assertEquals(2, map.size());
        assertEquals(SetUtils.toSet("1", "2"), map.keySet());
    }
}
