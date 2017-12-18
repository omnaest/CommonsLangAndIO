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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class StringUtilsTest
{

	@Test
	public void testSplitToStream() throws Exception
	{
		List<String> tokens = StringUtils	.splitToStream("abc")
											.collect(Collectors.toList());
		assertEquals("a", tokens.get(0));
		assertEquals("b", tokens.get(1));
		assertEquals("c", tokens.get(2));
	}

	@Test
	public void testReverse() throws Exception
	{
		assertEquals("321", StringUtils.reverse("123"));
	}

}