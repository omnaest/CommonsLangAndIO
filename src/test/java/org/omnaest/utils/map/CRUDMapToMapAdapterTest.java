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
package org.omnaest.utils.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.MapUtils;

public class CRUDMapToMapAdapterTest
{
	@Test
	public void test()
	{
		Map<String, String> map = CRUDMap	.of(MapUtils.builder()
														.put("key1", "value1")
														.put("key2", "value2")
														.build())
											.toMap();
		assertEquals(2, map.size());
		assertFalse(map.isEmpty());
		assertEquals(2, map	.keySet()
							.size());
		assertEquals(	Arrays.asList("key1", "key2")
							.stream()
							.collect(Collectors.toSet()),
						map.keySet());
		assertEquals(	Arrays	.asList("value1", "value2")
								.stream()
								.collect(Collectors.toSet()),
						map	.values()
							.stream()
							.collect(Collectors.toSet()));
		assertEquals(2, map	.entrySet()
							.size());
		assertEquals("value1", map	.entrySet()
									.stream()
									.filter(entry -> entry	.getKey()
															.equals("key1"))
									.findAny()
									.get()
									.getValue());
	}
}
