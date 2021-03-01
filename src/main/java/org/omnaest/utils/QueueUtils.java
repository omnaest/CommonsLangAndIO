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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * Utils for {@link Queue} operations
 *
 * @see Queue
 * @see BlockingQueue
 * @author Omnaest
 */
public class QueueUtils
{

	public static <E> List<E> drain(int number, Queue<E> queue)
	{
		List<E> retlist = new ArrayList<>();

		E element = null;
		for (int ii = 0; ii < number && (ii == 0 || element != null); ii++)
		{
			element = queue.poll();
			if (element != null)
			{
				retlist.add(element);
			}
		}

		return retlist;
	}

	public static <E> List<E> drainAll(Queue<E> queue)
	{
		List<E> retlist = new ArrayList<>();

		E element = null;
		do
		{
			element = queue.poll();
			if (element != null)
			{
				retlist.add(element);
			}
		} while (element != null);

		return retlist;
	}
}
