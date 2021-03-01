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
package org.omnaest.utils.iterator;

import java.util.Iterator;
import java.util.Queue;
import java.util.function.Function;

/**
 * A consuming {@link Iterator} over a given {@link Queue} source
 * 
 * @author omnaest
 * @param <E>
 */
public class QueueIterator<E> implements Iterator<E>
{
	private Queue<E>				queue;
	private Function<Queue<E>, E>	elementDrainFunction	= Queue::poll;

	/**
	 * Defines the source {@link Queue} and the drain function like {@link Queue#poll()}
	 * 
	 * @param queue
	 * @param elementDrainFunction
	 */
	public QueueIterator(Queue<E> queue, Function<Queue<E>, E> elementDrainFunction)
	{
		super();
		this.queue = queue;
		this.elementDrainFunction = elementDrainFunction;
	}

	/**
	 * Similar to {@link #QueueIterator(Queue, Function)} with {@link Queue#poll()} method as drain function
	 * 
	 * @param queue
	 */
	public QueueIterator(Queue<E> queue)
	{
		super();
		this.queue = queue;
	}

	@Override
	public boolean hasNext()
	{
		return !this.queue.isEmpty();
	}

	@Override
	public E next()
	{

		return this.elementDrainFunction.apply(this.queue);
	}

}
