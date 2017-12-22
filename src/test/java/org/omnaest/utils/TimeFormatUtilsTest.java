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

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @see TimeFormatUtils
 * @author omnaest
 */
public class TimeFormatUtilsTest
{

	@Test
	public void testFormat() throws Exception
	{
		assertEquals("10 sec", TimeFormatUtils	.format()
												.duration(10, TimeUnit.SECONDS)
												.asString());
	}

	@Test
	public void testFormatCanonical() throws Exception
	{
		assertEquals("1 h 59 min 10 sec", TimeFormatUtils	.format()
															.duration(3600 + 59 * 60 + 10, TimeUnit.SECONDS)
															.asCanonicalString());
	}

}
