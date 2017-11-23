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
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class ComparatorUtils
{

	public static <T, R extends Comparable<? super R>> Comparator<T> chainedComparator(Function<T, R> function)
	{
		return chainedComparator(Arrays	.asList(function)
										.stream()
										.map(ifunction -> new Comparator<T>()
										{
											@Override
											public int compare(T o1, T o2)
											{
												return ComparatorUtils.compare(ifunction.apply(o1), ifunction.apply(o2));
											}
										}));
	}

	public static <T, R1 extends Comparable<? super R1>, R2 extends Comparable<? super R2>> Comparator<T> chainedComparator(Function<T, R1> function1,
																															Function<T, R2> function2)
	{
		return chainedComparator(new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				return ComparatorUtils.compare(function1.apply(o1), function1.apply(o2));
			}
		}, new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				return ComparatorUtils.compare(function2.apply(o1), function2.apply(o2));
			}
		});
	}

	public static <T, R1 extends Comparable<? super R1>, R2 extends Comparable<? super R2>, R3 extends Comparable<? super R3>> Comparator<T> chainedComparator(	Function<T, R1> function1,
																																								Function<T, R2> function2,
																																								Function<T, R3> function3)
	{
		return chainedComparator(new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				return ComparatorUtils.compare(function1.apply(o1), function1.apply(o2));
			}
		}, new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				return ComparatorUtils.compare(function2.apply(o1), function2.apply(o2));
			}
		}, new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				return ComparatorUtils.compare(function3.apply(o1), function3.apply(o2));
			}
		});
	}

	@SafeVarargs
	public static <T, R extends Comparable<? super R>> Comparator<T> chainedComparator(Function<T, R>... functions)
	{
		return chainedComparator(Arrays	.asList(functions)
										.stream()
										.map(function -> new Comparator<T>()
										{
											@Override
											public int compare(T o1, T o2)
											{
												return ComparatorUtils.compare(function.apply(o1), function.apply(o2));
											}
										}));
	}

	@SafeVarargs
	public static <T> Comparator<T> chainedComparator(Comparator<T>... comparators)
	{
		return chainedComparator(Arrays	.asList(comparators)
										.stream());
	}

	public static <T> Comparator<T> chainedComparator(Stream<Comparator<T>> comparators)
	{
		return new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				int retval = 0;

				for (Comparator<T> comparator : IterableUtils.from(comparators.filter(comparator -> comparator != null)))
				{
					retval = comparator.compare(o1, o2);
					if (retval != 0)
					{
						break;
					}
				}

				return retval;
			}
		};
	}

	/**
	 * @see StringUtils#compare(String, String)
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static <E> int compare(String str1, String str2)
	{
		return StringUtils.compare(str1, str2);
	}

	/**
	 * @see ObjectUtils#compare(Comparable, Comparable)
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static <E extends Comparable<? super E>> int compare(E c1, E c2)
	{
		return ObjectUtils.compare(c1, c2);
	}

	@SuppressWarnings({ "unchecked" })
	public static int compareObject(Object o1, Object o2)
	{
		if (o1 instanceof Comparable && o2 instanceof Comparable)
		{
			return compare((Comparable<Object>) o1, (Comparable<Object>) o2);
		}
		else
		{
			return StringUtils.compare(Objects.toString(o1), Objects.toString(o2));
		}
	}
}
