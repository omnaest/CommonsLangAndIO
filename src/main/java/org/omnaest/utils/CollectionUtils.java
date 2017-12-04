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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper methods regarding {@link Collection}s
 *
 * @author Omnaest
 */
public class CollectionUtils
{

	/**
	 * Defines the {@link Set}s of added,removed and shared elements between to {@link Collection}s
	 *
	 * @see CollectionUtils#delta(Collection, Collection)
	 * @author Omnaest
	 * @param <E>
	 */
	public static class CollectionDelta<E>
	{
		private Set<E>	added;
		private Set<E>	removed;
		private Set<E>	shared;

		protected CollectionDelta(Set<E> added, Set<E> removed, Set<E> shared)
		{
			super();
			this.added = added;
			this.removed = removed;
			this.shared = shared;
		}

		public Set<E> getAdded()
		{
			return this.added;
		}

		public Set<E> getRemoved()
		{
			return this.removed;
		}

		public Set<E> getShared()
		{
			return this.shared;
		}

		@Override
		public String toString()
		{
			return "[added=" + this.added + ", removed=" + this.removed + ", shared=" + this.shared + "]";
		}

	}

	/**
	 * Calculates the {@link CollectionDelta} for two {@link Collection}s
	 *
	 * @param previous
	 * @param after
	 * @return
	 */
	public static <E> CollectionDelta<E> delta(Collection<E> previous, Collection<E> after)
	{
		if (previous == null)
		{
			previous = Collections.emptyList();
		}
		if (after == null)
		{
			after = Collections.emptyList();
		}

		Set<E> previousSet = previous	.stream()
										.collect(Collectors.toSet());
		Set<E> afterSet = after	.stream()
								.collect(Collectors.toSet());

		Set<E> shared = previousSet	.stream()
									.filter(e -> afterSet.contains(e))
									.collect(Collectors.toSet());

		Set<E> removed = previousSet.stream()
									.filter(e -> !shared.contains(e))
									.collect(Collectors.toSet());
		Set<E> added = afterSet	.stream()
								.filter(e -> !shared.contains(e))
								.collect(Collectors.toSet());

		return new CollectionDelta<>(added, removed, shared);
	}

	public static <E> E last(Collection<E> collection)
	{
		return ListUtils.last((collection == null ? Stream.<E>empty() : collection.stream()).collect(Collectors.toList()));
	}

	public static <E> E first(Collection<E> collection)
	{
		E retval = null;
		if (collection != null)
		{
			Iterator<E> iterator = collection.iterator();
			if (iterator.hasNext())
			{
				retval = iterator.next();
			}
		}
		return retval;
	}

	public static <E> boolean isNotEmpty(Collection<E> collection)
	{
		return collection != null && !collection.isEmpty();
	}
}
