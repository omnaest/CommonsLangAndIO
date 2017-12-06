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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helpoer methods for {@link Set}s
 *
 * @author Omnaest
 */
public class SetUtils
{
	@SafeVarargs
	public static <E> Set<E> merge(Collection<E>... collections)
	{
		return Arrays	.asList(collections)
						.stream()
						.filter(collection -> collection != null)
						.flatMap(collection -> collection.stream())
						.collect(Collectors.toSet());
	}

	public static <E> E last(Set<E> set)
	{
		E retval = null;
		for (E element : set)
		{
			retval = element;
		}
		return retval;
	}

	public static <E> E first(Set<E> set)
	{
		E retval = null;
		if (set != null && !set.isEmpty())
		{
			retval = set.iterator()
						.next();
		}
		return retval;
	}
}
