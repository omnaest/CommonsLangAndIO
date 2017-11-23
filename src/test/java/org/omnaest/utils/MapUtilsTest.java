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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.element.LeftAndRight;

/**
 * @see MapUtils
 * @author omnaest
 */
public class MapUtilsTest
{

	@Test
	public void testInvert() throws Exception
	{
		Map<String, String> map = MapUtils	.builder()
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
		assertEquals(Arrays.asList("key1", "key2", "key3"), map	.keySet()
																.stream()
																.collect(Collectors.toList()));

	}

	@Test
	public void testJoin() throws Exception
	{
		Map<String, LeftAndRight<String>> join = MapUtils.join(	MapUtils.builder()
																		.put("1", "value1")
																		.put("2", "value2.1")
																		.build(),
																MapUtils.builder()
																		.put("2", "value2.2")
																		.put("3", "value3")
																		.build());

		assertEquals(3, join.size());
		assertEquals("value1", join	.get("1")
									.getLeft());
		assertEquals(null, join	.get("1")
								.getRight());
		assertEquals("value2.1", join	.get("2")
										.getLeft());
		assertEquals("value2.2", join	.get("2")
										.getRight());
		assertEquals(null, join	.get("3")
								.getLeft());
		assertEquals("value3", join	.get("3")
									.getRight());

	}

}
