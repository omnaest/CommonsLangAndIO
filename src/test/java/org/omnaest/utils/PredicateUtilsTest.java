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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class PredicateUtilsTest
{

    @Test
    public void testNotNull() throws Exception
    {
        assertTrue(PredicateUtils.notNull()
                                 .test(1));
        assertFalse(PredicateUtils.notNull()
                                  .test(null));
    }

    @Test
    public void testUntil() throws Exception
    {
        assertEquals(Arrays.asList("a", "b"), Arrays.asList("a", "b", "c", "d")
                                                    .stream()
                                                    .filter(PredicateUtils.until(e -> e.equals("c")))
                                                    .collect(Collectors.toList()));
    }

    @Test
    public void testMatchesType() throws Exception
    {
        assertTrue(PredicateUtils.<Double, Number>matchesType(Double.class)
                                 .test(1.2));
    }

    @Test
    public void testIsCollectionNotContaining() throws Exception
    {
        assertTrue(PredicateUtils.isCollectionNotContaining(Arrays.asList("a", "b"))
                                 .test("c"));
        assertFalse(PredicateUtils.isCollectionNotContaining(Arrays.asList("a", "b"))
                                  .test("a"));

        assertTrue(PredicateUtils.isCollectionNotContaining(Arrays.asList("a", "b"))
                                 .<String>from(value -> StringUtils.removeEnd(value, "1"))
                                 .test("c1"));
        assertFalse(PredicateUtils.isCollectionNotContaining(Arrays.asList("a", "b"))
                                  .<String>from(value -> StringUtils.removeEnd(value, "1"))
                                  .test("a1"));
    }

    @Test
    public void testIsMapNotContainingKey() throws Exception
    {
        assertTrue(PredicateUtils.isMapNotContainingKey(MapUtils.builder()
                                                                .put("key1", null)
                                                                .build())
                                 .test("key2"));
        assertFalse(PredicateUtils.isMapNotContainingKey(MapUtils.builder()
                                                                 .put("key1", null)
                                                                 .build())
                                  .test("key1"));
    }

}
