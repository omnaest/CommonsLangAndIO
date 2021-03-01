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

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.junit.Test;

/**
 * @see QueueUtils
 * @author Omnaest
 */
public class QueueUtilsTest
{
	@Test
	public void testDrainAll() throws Exception
	{
		Queue<String> queue = new LinkedBlockingDeque<>();
		queue.addAll(Arrays.asList("1", "2", "3"));

		List<String> drained = QueueUtils.drainAll(queue);
		assertEquals(Arrays.asList("1", "2", "3"), drained);
	}

	@Test
	public void testDrain() throws Exception
	{
		Queue<String> queue = new LinkedBlockingDeque<>();
		queue.addAll(Arrays.asList("1", "2", "3"));

		List<String> drained = QueueUtils.drain(2, queue);
		assertEquals(Arrays.asList("1", "2"), drained);
	}

}
