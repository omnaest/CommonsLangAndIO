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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Helper for {@link Consumer} instances
 * 
 * @author omnaest
 */
public class ConsumerUtils
{
	/**
	 * Returns a {@link Consumer} which does accept the given element once. If it accepts the {@link Consumer#accept(Object)} method of the given
	 * {@link Consumer} is called, otherwise not.
	 * 
	 * @param consumer
	 * @return
	 */
	public static <E> Consumer<E> consumeOnce(Consumer<E> consumer)
	{
		return new Consumer<E>()
		{
			private AtomicBoolean done = new AtomicBoolean(false);

			@Override
			public void accept(E t)
			{
				if (!this.done.getAndSet(true))
				{
					consumer.accept(t);
				}
			}
		};
	}
}
