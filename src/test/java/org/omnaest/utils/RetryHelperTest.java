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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * @see RetryHelper
 * @author omnaest
 */
public class RetryHelperTest
{
	@Test
	public void testRetryIntIntTimeUnitRetryOperation() throws Exception
	{
		AtomicInteger counter = new AtomicInteger();
		boolean success = RetryHelper.retry(5, 50, TimeUnit.MILLISECONDS, () ->
		{
			int current = counter.getAndIncrement();
			if (current < 3)
			{
				throw new Exception("Test");
			}
			else
			{
				return true;
			}
		});
		assertTrue(success);
	}

}
