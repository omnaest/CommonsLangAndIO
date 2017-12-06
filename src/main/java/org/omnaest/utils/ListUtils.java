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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import org.omnaest.utils.list.ComparableList;
import org.omnaest.utils.list.ComparableListDecorator;

public class ListUtils
{
	public static <E> E last(List<E> list)
	{
		E retval = null;

		if (list != null && !list.isEmpty())
		{
			retval = list.get(list.size() - 1);
		}

		return retval;
	}

	public static <E> E first(List<E> list)
	{
		E retval = null;

		if (list != null && !list.isEmpty())
		{
			retval = list.get(0);
		}

		return retval;
	}

	@SafeVarargs
	public static <E> List<E> mergedList(Collection<E>... lists)
	{
		List<E> retlist = new ArrayList<>();
		Arrays	.asList(lists)
				.forEach(list -> retlist.addAll(list));
		return retlist;
	}

	public static <E> List<E> shuffled(Collection<E> collection)
	{
		List<E> retlist = new ArrayList<>();
		if (collection != null)
		{
			collection.addAll(collection);
		}
		Collections.shuffle(retlist);
		return retlist;
	}

	public static <E> Comparator<List<E>> comparator(Comparator<E> comparator)
	{
		return new Comparator<List<E>>()
		{
			@Override
			public int compare(List<E> o1, List<E> o2)
			{
				return ComparatorUtils	.<List<E>>chainedComparator(IntStream	.range(0, Math.max(o1.size(), o2.size()))
																				.mapToObj(index -> new Comparator<List<E>>()
																				{
																					@Override
																					public int compare(List<E> o1, List<E> o2)
																					{
																						E left = index < o1.size() ? o1.get(index) : null;
																						E right = index < o2.size() ? o2.get(index) : null;
																						return comparator.compare(left, right);
																					}
																				}))
										.compare(o1, o2);

			}
		};
	}

	public static <E> int compare(List<E> o1, List<E> o2, Comparator<E> comparator)
	{
		return comparator(comparator).compare(o1, o2);
	}

	public static <E extends Comparable<E>> ComparableList<E> comparable(List<E> list)
	{
		return new ComparableListDecorator<>(list, ComparatorUtils::compare);
	}

	public static <E> ComparableList<E> comparable(List<E> list, Comparator<E> comparator)
	{
		return new ComparableListDecorator<>(list, comparator);
	}

	public static <E> List<E> defaultIfNull(List<E> list)
	{
		return list != null ? list : Collections.emptyList();
	}

	/**
	 * Returns a new {@link List} with the elements of the given {@link Collection} in reverse order.<br>
	 * <br>
	 * Return null if the given {@link Collection} is null.
	 * 
	 * @param collection
	 * @return
	 */
	public static <E> List<E> inverse(Collection<E> collection)
	{
		List<E> retlist = null;
		if (collection != null)
		{
			retlist = new ArrayList<>(collection);
			Collections.reverse(retlist);
		}
		return retlist;
	}

	/**
	 * Adds a given element to the given {@link List} or returns a new {@link List} instance if the given {@link List} is null
	 * 
	 * @param list
	 * @param element
	 * @return given list instance
	 */
	public static <E> List<E> addTo(List<E> list, E element)
	{
		if (list == null)
		{
			list = new ArrayList<>();
		}
		list.add(element);
		return list;
	}

	@SafeVarargs
	public static <E> List<E> of(E... elements)
	{
		return new ArrayList<>(Arrays.asList(elements));
	}

	@SafeVarargs
	public static <E> List<E> modified(List<E> list, UnaryOperator<List<E>>... modifiers)
	{
		List<E> retlist = list;
		if (modifiers != null)
		{
			for (UnaryOperator<List<E>> modifier : modifiers)
			{
				retlist = modifier.apply(retlist);
			}
		}
		return retlist;
	}

	public static <E> boolean isNotEmpty(List<E> list)
	{
		return list != null && !list.isEmpty();
	}

}
